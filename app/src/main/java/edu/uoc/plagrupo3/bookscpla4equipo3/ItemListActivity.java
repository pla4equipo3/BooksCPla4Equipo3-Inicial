package edu.uoc.plagrupo3.bookscpla4equipo3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;


import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import edu.uoc.plagrupo3.bookscpla4equipo3.dummy.DummyContent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient googleApiClient;
    public String nameUser,emailUser;
    public Uri photo;
    private String ic_icon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //  .requestIdToken(getString(R.string.default_web_client_id)) // obtenemos token
                .requestEmail()
                .build();//configurar el inicio de sesión de Google para solicitar los datos de usuario requeridos por la aplicación


        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build(); // mediante googleApiClient podemos tner acceso a las apis de google o sea la de autentificacion


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser(); // si no hay usuario activo vuelve al login
                if (user == null)  goLoginActivity();
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


        PrimaryDrawerItem item7 = new PrimaryDrawerItem().withIdentifier(6).withName(R.string.MenuCompartirconFacebook)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_facebook)
                        .sizeDp(24));

        PrimaryDrawerItem item8 = new PrimaryDrawerItem().withIdentifier(7).withName(R.string.MenuCompartirconWhatsapp)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_whatsapp)
                        .sizeDp(24));



       FirebaseUser user= mAuth.getCurrentUser();

       if (user!=null) { //recuperar datos de usuario
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
                        switch ((int) drawerItem.getIdentifier()){
                            case 5:
                                dialogCloseSesion();
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener!= null) mAuth.removeAuthStateListener(mAuthListener);

    }

    private String createImageOnSDCard(int resID)
    {
        /*
        Drawable drawable = getResources().getDrawable(resID);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        */

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),resID);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/"+resID+".png";
        File file = new File(path);
        try {
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
            outputStream.flush();
            outputStream.close();
        }catch (Exception ex){
            ex.printStackTrace();
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.ErrorAlmacenamiento),Toast.LENGTH_LONG).show();
        }
        return file.getPath();

    }
    private void onShareTextAndIconWhatsappFileProvider()
    {  //método compartir whatsapp
        ic_icon = createImageOnSDCard(R.drawable.logo);
        Uri path = FileProvider.getUriForFile(this,"edu.uoc.plagrupo3.bookscpla4equipo3",new File(ic_icon));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT,getResources().getString(R.string.TextoDescriptivoAplicacion));
        shareIntent.putExtra(Intent.EXTRA_STREAM,path);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("image/*");
        shareIntent.setPackage("com.whatsapp");

        try {
            startActivity(shareIntent);

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.ErrorWhatsapp),Toast.LENGTH_LONG).show(); //no esta instalado whatsapp
        }

    }


    private void overrideDrawerImageLoaderPicasso(){
        //inicializa y crea el image loader logic
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
    private void signOut()
    {

        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()){
                    goLoginActivity();
                }
            }
        });

    }

    private void goLoginActivity(){
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void dialogCloseSesion()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(ItemListActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.action_closeSesion));
        alertDialog.setMessage(getResources().getString(R.string.action_pregunta_closeSesion));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.SI),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        signOut();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.NO),
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }



    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, mTwoPane));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
          super.onBackPressed();

    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<DummyContent.DummyItem> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DummyContent.DummyItem item = (DummyContent.DummyItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ItemDetailFragment.ARG_ITEM_ID, item.id);
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                      List<DummyContent.DummyItem> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}
