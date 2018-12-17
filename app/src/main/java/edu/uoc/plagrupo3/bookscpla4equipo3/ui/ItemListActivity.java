package edu.uoc.plagrupo3.bookscpla4equipo3.ui;

import android.app.Activity;
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
import androidx.appcompat.widget.SearchView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.google.firebase.database.Query;
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
import edu.uoc.plagrupo3.bookscpla4equipo3.sharePreferences.LoadLanguage;
import io.realm.Realm;
import io.realm.RealmResults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


public class ItemListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    private boolean mTwoPane;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private DatabaseReference mFilter;
    private GoogleApiClient googleApiClient;
    private String nameUser, emailUser, ic_icon;
    private Uri photo;
    private RecyclerView mMyRecycler;
    private ConnectivityManager mConnectivityManager;
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    private static final String BOOK_REFERENCE="books";
    private static final String TAG="Debug";
    ChangeLanguage idioma;
    private ValueEventListener eventListener;
    private ChildEventListener childEventListener;
    SimpleItemRecyclerViewAdapter adaptador;
    private android.widget.SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
        super.onCreate(savedInstanceState);

        new LoadLanguage(this).Change(); //cargar idioma
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        LibroDatos.conexion = Realm.getDefaultInstance();
        searchView = (android.widget.SearchView)findViewById(R.id.searh);
        mMyRecycler = (RecyclerView)findViewById(R.id.item_list);
        assert mMyRecycler !=null;
        mMyRecycler.hasFixedSize();

        mDatabase= FirebaseDatabase.getInstance().getReference(BOOK_REFERENCE);
       // mFilter = FirebaseDatabase.getInstance().getReference(BOOK_REFERENCE);
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
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.MenuDisponibles)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_lock_open)
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
            if(photo==null){
                 photo=Uri.parse("android.resource://" + getPackageName()
                        + "/drawable/" + "ic_user");
            }

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
                                try {
                                    if (TestearRed.isNetworkConnected(getApplicationContext()))
                                        loadDataFirebase();
                                    else loadDataRealm();
                                }catch (NullPointerException ex){ex.printStackTrace();}
                                break;

                            case 1:
                                try {
                                    if (TestearRed.isNetworkConnected(getApplicationContext()))
                                        loadBookAvailable();
                                    else Toast.makeText(getApplicationContext(),getResources().getString(R.string.SinInternet),Toast.LENGTH_LONG).show();
                                }catch (NullPointerException ex){ex.printStackTrace();}

                                break;
                            case 2:

                                try {
                                    if (TestearRed.isNetworkConnected(getApplicationContext()))
                                        loadBooking();
                                    else Toast.makeText(getApplicationContext(),getResources().getString(R.string.SinInternet),Toast.LENGTH_LONG).show();
                                }catch (NullPointerException ex){ex.printStackTrace();}

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


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getResources().getString(R.string.Email), Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.Email_Ir), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendEmail();

                            }
                        })
                        .show();
            }
        });


        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
            /*
            if (savedInstanceState!=null) {
               Bundle arguments = new Bundle();
               arguments.putInt(ItemDetailFragment.ARG_ITEM_ID, 0);
               ItemDetailFragment fragment = new ItemDetailFragment();
               fragment.setArguments(arguments);
               getSupportFragmentManager().beginTransaction()
                       .replace(R.id.item_detail_container, fragment)
                       .commit();
            }*/
        }else this.setTitle(R.string.title_item_list);

     searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
         @Override
         public boolean onQueryTextSubmit(String query) {
             return false;
         }

         @Override
         public boolean onQueryTextChange(String newText) {
            // String r = newText.toLowerCase();
             loadFilter(newText); // cargar metodo que filtra por letra

             return false;
         }

     });

    }
    private void loadBooking()
    {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null) {

            Query query = mDatabase.orderByChild("usuarioactivo").equalTo(user.getEmail());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        LibroDatos.listalibros.clear();
                        adaptador.notifyDataSetChanged();
                        Snackbar.make(mMyRecycler, getResources().getString(R.string.SinResultados), Snackbar.LENGTH_SHORT).show();

                    } else {
                        Object object = dataSnapshot.getValue();

                        if (object instanceof Map) {
                            GenericTypeIndicator<Map<String, Libro>> t = new GenericTypeIndicator<Map<String, Libro>>() {
                            };
                            Map<String, Libro> map = dataSnapshot.getValue(t);

                            List<Libro> list = new ArrayList<Libro>(map.values());

                            LibroDatos.listalibros = list;
                            LibroDatos.listalibros.removeAll(Collections.singleton(null));
                            for (int i = 0; i < LibroDatos.listalibros.size(); i++) {
                                //Actualizamos el id puesto que no esta en Firebase
                                LibroDatos.listalibros.get(i).setId(i);

                            }
                        } else {
                            GenericTypeIndicator<ArrayList<Libro>> genericTypeIndicator2 = new GenericTypeIndicator<ArrayList<Libro>>() {
                            };
                            LibroDatos.listalibros = dataSnapshot.getValue(genericTypeIndicator2);
                            LibroDatos.listalibros.removeAll(Collections.singleton(null));
                            for (int i = 0; i < LibroDatos.listalibros.size(); i++) {
                                LibroDatos.listalibros.get(i).setId(i);
                            }
                        }


                        adaptador.setItems(LibroDatos.listalibros);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG,databaseError.getMessage());
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.ErrorServidor),Toast.LENGTH_LONG).show();

                }
            });

            loadRecycler(this, LibroDatos.listalibros, mTwoPane);
        }
    }
    private void loadBookAvailable(){
        Query query = mDatabase.orderByChild("disponible").equalTo("true");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null)
                {  LibroDatos.listalibros.clear();
                    adaptador.notifyDataSetChanged();
                    Snackbar.make(mMyRecycler,getResources().getString(R.string.SinResultados),Snackbar.LENGTH_SHORT).show();

                }
                else {
                    Object object = dataSnapshot.getValue();

                    if (object instanceof Map) {
                        GenericTypeIndicator<Map<String, Libro>> t = new GenericTypeIndicator<Map<String, Libro>>() {};
                        Map<String, Libro> map = dataSnapshot.getValue(t);

                        List<Libro> list = new ArrayList<Libro>(map.values());

                        LibroDatos.listalibros=list;
                        LibroDatos.listalibros.removeAll(Collections.singleton(null));
                        for (int i = 0; i < LibroDatos.listalibros.size(); i++) {
                            //Actualizamos el id puesto que no esta en Firebase
                            LibroDatos.listalibros.get(i).setId(i);

                        }
                    }

                    else
                    {
                        GenericTypeIndicator<ArrayList<Libro>> genericTypeIndicator2 = new GenericTypeIndicator<ArrayList<Libro>>() {
                        };
                        LibroDatos.listalibros = dataSnapshot.getValue(genericTypeIndicator2);
                        LibroDatos.listalibros.removeAll(Collections.singleton(null));
                        for (int i = 0; i < LibroDatos.listalibros.size(); i++) {
                            LibroDatos.listalibros.get(i).setId(i);
                        }
                    }


                    adaptador.setItems(LibroDatos.listalibros);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG,databaseError.getMessage());
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.ErrorServidor),Toast.LENGTH_LONG).show();

            }
        });

        loadRecycler(this,LibroDatos.listalibros,mTwoPane);


    }

    private void loadFilter(String filtro)
    {
        Query query = mDatabase.orderByChild("titulo").startAt(FirstCapitalLetter(filtro)).endAt(FirstCapitalLetter(filtro)+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null)
                {  LibroDatos.listalibros.clear();
                   adaptador.notifyDataSetChanged();
                   Snackbar.make(mMyRecycler,getResources().getString(R.string.SinResultados),Snackbar.LENGTH_SHORT).show();

                }
                else {
                    Object object = dataSnapshot.getValue();

                    if (object instanceof Map) {
                        GenericTypeIndicator<Map<String, Libro>> t = new GenericTypeIndicator<Map<String, Libro>>() {};
                        Map<String, Libro> map = dataSnapshot.getValue(t);

                        List<Libro> list = new ArrayList<Libro>(map.values());

                        LibroDatos.listalibros=list;
                        LibroDatos.listalibros.removeAll(Collections.singleton(null));
                        for (int i = 0; i < LibroDatos.listalibros.size(); i++) {
                            //Actualizamos el id puesto que no esta en Firebase
                            LibroDatos.listalibros.get(i).setId(i);

                        }
                    }

                    else
                    {
                        GenericTypeIndicator<ArrayList<Libro>> genericTypeIndicator2 = new GenericTypeIndicator<ArrayList<Libro>>() {
                        };
                        LibroDatos.listalibros = dataSnapshot.getValue(genericTypeIndicator2);
                        LibroDatos.listalibros.removeAll(Collections.singleton(null));
                        for (int i = 0; i < LibroDatos.listalibros.size(); i++) {
                            LibroDatos.listalibros.get(i).setId(i);
                            }
                    }


                    adaptador.setItems(LibroDatos.listalibros);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG,databaseError.getMessage());
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.ErrorServidor),Toast.LENGTH_LONG).show();

            }
        });

        loadRecycler(this,LibroDatos.listalibros,mTwoPane);


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
        if (connectionReceiver!=null) unregisterReceiver(connectionReceiver);
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
    private void sendEmail()
    {
        String[] TO = {"Pla4.equipo3@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.Email_Tema));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.Email_Mensaje));

        try {
            startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.Email_Enviar)));
            finish();

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,
                    getResources().getString(R.string.Email_NoInstalado), Toast.LENGTH_SHORT).show();
        }
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
                Picasso.get()
                        .load(uri)
                        .placeholder(placeholder)
                        .centerCrop()
                        .fit()
                        .into(imageView);
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

    public static String FirstCapitalLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        } else {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }


    private void push()
    { // metodo de prueba solo verificar que hace el push bien
        Libro libro = new Libro();
        libro.setAutor("Miguel de Cervantes");
        libro.setTitulo("Don Quijote de la mancha");
        libro.setDisponible("false");

        mDatabase.child("11").setValue(libro);
    }



    private void loadDataFirebase(){
        //cargar datos de Firebase
        FirebaseApp.initializeApp(ItemListActivity.this);
        setTitle(R.string.title_item_list);
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
/*
               // LibroDatos.listalibros.removeAll(LibroDatos.listalibros);
                try {
                    LibroDatos.listalibros.clear();
                }catch (UnsupportedOperationException ex){ex.printStackTrace();}
             //   adaptador.notifyDataSetChanged();
                GenericTypeIndicator<Libro> genericTypeIndicator = new GenericTypeIndicator<Libro>() {};

                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Libro libro = snapshot.getValue(genericTypeIndicator);
                    libro.setId(Integer.parseInt(snapshot.getKey()));
                    LibroDatos.listalibros.add(libro);
                        if(!LibroDatos.existsById(libro))
                        {
                            LibroDatos.conexion.beginTransaction();
                            LibroDatos.conexion.insert(libro);
                            LibroDatos.conexion.commitTransaction();
                        }
                }
               // adaptador.setItems(LibroDatos.listalibros);
                adaptador.notifyDataSetChanged();
/*

 */
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Error de lectura.", databaseError.toException());
                Toast.makeText(ItemListActivity.this, getResources().getString(R.string.ErrorCargaFirebase), Toast.LENGTH_LONG).show();
                loadDataRealm();
                    }
        };
        mDatabase.addValueEventListener(eventListener); //registramos oyente
        loadRecycler(this,LibroDatos.listalibros,mTwoPane);


    }



    private void loadDataRealm(){
        //método carga la base de datos local
        Log.d(TAG,"datos" + LibroDatos.listalibros.size());
        LibroDatos.listalibros = LibroDatos.getBooks();
         if (LibroDatos.listalibros.isEmpty())
         {
             Toast.makeText(getApplicationContext(), getResources().getString(R.string.ListaRealmVacia), Toast.LENGTH_LONG).show();

         }else {
             loadRecycler(this, LibroDatos.listalibros, mTwoPane);

         }
    }

    private void loadRecycler(ItemListActivity mParentActivity,List<Libro> libros, Boolean mTwoPane)
    { // método cargar recycler
        adaptador = new SimpleItemRecyclerViewAdapter(mParentActivity,libros,mTwoPane);
        mMyRecycler.setAdapter(adaptador);
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventListener!=null) mDatabase.removeEventListener(eventListener); // remover oyente

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
