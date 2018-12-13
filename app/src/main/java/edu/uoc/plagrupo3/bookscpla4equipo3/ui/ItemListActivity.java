package edu.uoc.plagrupo3.bookscpla4equipo3.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.LayoutInflaterCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;


import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import edu.uoc.plagrupo3.bookscpla4equipo3.sharePreferences.ChangeLanguage;
import edu.uoc.plagrupo3.bookscpla4equipo3.R;
import edu.uoc.plagrupo3.bookscpla4equipo3.adapter.SimpleItemRecyclerViewAdapter;
import edu.uoc.plagrupo3.bookscpla4equipo3.net.TestearRed;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.Libro;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.LibroDatos;
import io.realm.Realm;
import io.realm.RealmResults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ItemListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    private boolean mTwoPane;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private GoogleApiClient googleApiClient;
    private String nameUser, emailUser, ic_icon;
    private Uri photo;
    private RecyclerView mMyRecycler;
    private ConnectivityManager mConnectivityManager;
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    private static final String BOOK_REFERENCE="books";
    private static final String TAG="Debug";
    ChangeLanguage idioma;
    SimpleItemRecyclerViewAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
        super.onCreate(savedInstanceState);

        loadLanguage();//cargar idioma
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        LibroDatos.conexion = Realm.getDefaultInstance();

        mMyRecycler = (RecyclerView)findViewById(R.id.item_list);
        assert mMyRecycler !=null;
        mMyRecycler.hasFixedSize();

        mDatabase= FirebaseDatabase.getInstance().getReference(BOOK_REFERENCE);
       // mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //  .requestIdToken(getString(R.string.default_web_client_id)) // obtenemos token
                .requestEmail()
                .build();//configurar el inicio de sesión de Google para solicitar los datos de usuario requeridos por la aplicación


        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build(); // mediante googleApiClient podemos tner acceso a las apis de google o sea la de autentificacion


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser(); // si no hay usuario activo vuelve al login
                if (user == null) goLoginActivity();
            }
        };

        overrideDrawerImageLoaderPicasso(); // método para inicializar la carga de imagen de perfil con la libreria picasso


        //Preparación opciones de menú con sus iconos. En recurso String están los nombres de las opciones
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(0).withName(R.string.MenuListarLibros)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_th_list)
                        .sizeDp(24));
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.MenuMisFavoritos)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_user_plus)
                        .sizeDp(24));
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.MenuMisReservas)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_user_tag)
                        .sizeDp(24));
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.MenuAñadirLibro)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_plus)
                        .sizeDp(24));
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.MenuConfiguracion)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_cog)
                        .sizeDp(24));

        PrimaryDrawerItem item6 = new PrimaryDrawerItem().withIdentifier(5).withName(R.string.MenuCerrarSesion)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_lock)
                        .sizeDp(24));


        PrimaryDrawerItem item7 = new PrimaryDrawerItem().withIdentifier(6).withName(R.string.MenuCompartirconMisApps)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_share_alt_square)
                        .sizeDp(24));

        PrimaryDrawerItem item8 = new PrimaryDrawerItem().withIdentifier(7).withName(R.string.MenuCompartirconWhatsapp)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_whatsapp)
                        .sizeDp(24));


        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) { //recuperar datos de usuario
            emailUser = user.getEmail();
            nameUser = user.getDisplayName();
            photo = user.getPhotoUrl();
        }

        AccountHeader headerResult = new AccountHeaderBuilder()

                .withActivity(this)
                //  .withHeaderBackground(R.drawable.logo)
                .addProfiles(
                        new ProfileDrawerItem().withName(nameUser).withEmail(emailUser).withIcon(photo)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })

                .build();


//create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        item1,
                        item2,
                        item3,
                        item4,
                        item5,
                        item6,
                        new DividerDrawerItem(),
                        item7,
                        item8
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        switch ((int) drawerItem.getIdentifier()) {
                            case 0:
                                loadDataFirebase();
                                break;

                            case 1:
                                loadFavourites();
                                break;
                            case 2:

                                break;
                            case 3:
                                // push();
                                break;
                            case 4:
                                dialogChangeLanguage();

                                break;
                            case 5:
                                dialogCloseSesion();
                                break;
                            case 6:
                                onShareTextAndIconFileProvider();
                                break;

                            case 7:
                                onShareTextAndIconWhatsappFileProvider();
                                break;




                        }


                        return false;
                    }

                })
                .build();

    /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
      */

        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState!=null) {
               Bundle arguments = new Bundle();
               arguments.putInt(ItemDetailFragment.ARG_ITEM_ID, 0);
               ItemDetailFragment fragment = new ItemDetailFragment();
               fragment.setArguments(arguments);
               getSupportFragmentManager().beginTransaction()
                       .replace(R.id.item_detail_container, fragment)
                       .commit();
            }
        }else this.setTitle(R.string.title_item_list);


    }



    public BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        // crea un receiver anonimo para procesar los cambios de estdo de red, si hay internet cargo Firebase sino Bd local
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (TestearRed.isNetworkConnected(context)) {
                    Snackbar.make(mMyRecycler, getResources().getString(R.string.RedActivada), Snackbar.LENGTH_LONG).show();
                    loadDataFirebase();
                } else {
                    Snackbar.make(mMyRecycler, getResources().getString(R.string.ErrorRed), Snackbar.LENGTH_LONG).show();
                    loadDataRealm();
                }

            }catch (NullPointerException ex )
                {ex.printStackTrace();}
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, filter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionReceiver);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener); // iniciamos oyente de inicio de sesión
       /*
       metodo para versiones superiores a sdk 24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            mNetworkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    Toast.makeText(ItemListActivity.this,
                            "Conexión disponible",
                            Toast.LENGTH_LONG).show();
                    // cargaDatosFirebase();
                }


                @Override
                public void onLost(Network network) {
                    Toast.makeText(ItemListActivity.this,
                            "Conexión perdida",
                            Toast.LENGTH_LONG).show();
                    // cargarRealm();
                }
            };
            mConnectivityManager.registerDefaultNetworkCallback(mNetworkCallback);

        }*/

}

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) mAuth.removeAuthStateListener(mAuthListener);
      //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //  if (mNetworkCallback!=null) mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
      //  }


    }

    private String createImageOnSDCard(int resID) {


        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resID);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + resID + ".png";
        File file = new File(path);
        try {
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.ErrorAlmacenamiento), Toast.LENGTH_LONG).show();
        }
        return file.getPath();

    }

    private void onShareTextAndIconWhatsappFileProvider() {  //método compartir whatsapp
        ic_icon = createImageOnSDCard(R.drawable.logo);
        Uri path = FileProvider.getUriForFile(this, "edu.uoc.plagrupo3.bookscpla4equipo3", new File(ic_icon));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.TextoDescriptivoAplicacion));
        shareIntent.putExtra(Intent.EXTRA_STREAM, path);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("image/*");
        shareIntent.setPackage("com.whatsapp");

        try {
            startActivity(shareIntent);

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.ErrorWhatsapp), Toast.LENGTH_LONG).show(); //no esta instalado whatsapp
        }

    }
    private void onShareTextAndIconFileProvider() // método compartir con cualquier app
    {
        ic_icon = createImageOnSDCard(R.drawable.logo);
        Uri path = FileProvider.getUriForFile(this,"edu.uoc.plagrupo3.bookscpla4equipo3",new File(ic_icon));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT,getResources().getString(R.string.TextoDescriptivoAplicacion));
        shareIntent.putExtra(Intent.EXTRA_STREAM,path);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent,getResources().getString(R.string.Detalles_app)));

    }


    private void overrideDrawerImageLoaderPicasso() {
        //inicializa y crea el image loader logic sino no carga la foto en el drawer
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.get().load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.get().cancelRequest(imageView);
            }

            /*
            @Override
            public Drawable placeholder(Context ctx) {
                return super.placeholder(ctx);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                return super.placeholder(ctx, tag);
            }
            */
        });
    }

    private void signOut() {


        mAuth.signOut(); //cerrar sesión con Firebase

        // cerrar sesión con google
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    goLoginActivity();
                }
            }
        });

    }

    private void goLoginActivity() {
        //método para volver al login
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void dialogCloseSesion() {
        //cuadro diálogo cerrar sesión
        AlertDialog alertDialog = new AlertDialog.Builder(ItemListActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.MenuCerrarSesion));
        alertDialog.setMessage(getResources().getString(R.string.action_pregunta_closeSesion));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.SI),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        signOut();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.NO),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }


    private void dialogChangeLanguage()
    { //cuadro dialogo elegir idioma

        String[] idiomas = getResources().getStringArray(R.array.idioma_array); // array de idiomas definido en los recursos
        int itemSelected = 0;
         AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
           alertDialog.setTitle(getResources().getString(R.string.DialogIdioma))
                   .setSingleChoiceItems(idiomas, itemSelected, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                       }
                   })
                   .setPositiveButton(R.string.ButtonAceptar, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {

                           int selectedItem =((AlertDialog)dialog).getListView().getCheckedItemPosition();
                           idioma = new ChangeLanguage(getApplicationContext());
                           switch (selectedItem){
                               case 0:
                                   idioma.saveLanguage("es");
                                   break;
                               case 1:
                                   idioma.saveLanguage("ca");
                                   break;
                               case 2:
                                   idioma.saveLanguage("en");
                                   }
                           idioma=null;
                           recreate(); //reiniciar actividad para que reciba la configuración con el idioma seleccionado
                           dialog.dismiss();

                       }
                   })
                   .setNegativeButton(R.string.ButtonCancelar, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                       }
                   });
           alertDialog.show();


           }
    private void loadLanguage()
        {
            idioma = new ChangeLanguage(this);

            Locale locale = new Locale(idioma.getLangCode());
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
            idioma=null;
        }


    private void push()
    { // metodo de prueba solo verificar que hace el push bien
        Libro libro = new Libro();
        libro.setAutor("Jane Austen");
        libro.setTitulo("Orgullo y prejuicio");
        libro.setDisponible("true");

        mDatabase.child("10").setValue(libro);
    }

    private void loadDataFirebase(){
       // Toast.makeText(getApplicationContext(),"cargando datos Firebase",Toast.LENGTH_LONG).show();

        FirebaseApp.initializeApp(ItemListActivity.this);
        setTitle(R.string.title_item_list);


        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //TODO por hacer cuando hay un nodo que se modifica
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //TODO por hacer cuando hay un nodo que se borra
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // Leemos la información de la Base de Datos
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ArrayList<Libro>> genericTypeIndicator =new GenericTypeIndicator<ArrayList<Libro>>(){};

                 LibroDatos.listalibros=dataSnapshot.getValue(genericTypeIndicator);
                for (int i=0;i<LibroDatos.listalibros.size();i++) {
                    //Actualizamos el id puesto que no esta en Firebase
                    LibroDatos.listalibros.get(i).setId(i);

                    if (!LibroDatos.existsById(LibroDatos.listalibros.get(i))){
                        //Si el libro no existe lo añadimos a la base de datos local
                        LibroDatos.conexion.beginTransaction();
                        LibroDatos.conexion.insert(LibroDatos.listalibros.get(i));
                        LibroDatos.conexion.commitTransaction();
                    }

                }
                adaptador.setItems(LibroDatos.listalibros);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ItemListActivity.this, getResources().getString(R.string.ErrorCargaFirebase), Toast.LENGTH_LONG).show();
                loadDataRealm();
                Log.d(TAG, "Error de lectura.", error.toException());
            }
        });

        loadRecycler(this,LibroDatos.listalibros,mTwoPane);



    }

    private void loadDataRealm(){
       //Toast.makeText(getApplicationContext(),"cargando datos Bd local",Toast.LENGTH_LONG).show();

       LibroDatos.listalibros = LibroDatos.getBooks();
        Log.d(TAG,"datos" + LibroDatos.listalibros.size());

        loadRecycler(this,LibroDatos.listalibros,mTwoPane);


    }

    private void loadRecycler(ItemListActivity mParentActivity,List<Libro> libros, Boolean mTwoPane)
    { // método cargar recycler
        adaptador = new SimpleItemRecyclerViewAdapter(mParentActivity,libros,mTwoPane);
        mMyRecycler.setAdapter(adaptador);
    }

    private void loadFavourites()
    {

        LibroDatos.listalibros =LibroDatos.getAllFavourite();
        if (LibroDatos.listalibros.isEmpty())
        {
            Snackbar.make(mMyRecycler, getResources().getString(R.string.ListaFavoritosVacia), Snackbar.LENGTH_LONG).show();

        }else{
            setTitle(R.string.MenuMisFavoritos);
            loadRecycler(this,LibroDatos.listalibros,mTwoPane);

        }

    }





    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }




}
