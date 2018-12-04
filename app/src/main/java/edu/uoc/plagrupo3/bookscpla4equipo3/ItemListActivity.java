package edu.uoc.plagrupo3.bookscpla4equipo3;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.LayoutInflaterCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.Libro;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.LibroDatos;
import io.realm.Realm;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    //Variables relacionadas con FireBase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser user;

    //Adapter que utilizamos para mostrar la lista de libros
    private SimpleItemRecyclerViewAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        //Preparación opciones de menú con sus iconos. En recurso String están los nombres de las opciones
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.MenuListarLibros)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_th_list)
                        .sizeDp(24));
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.MenuMisFavoritos)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_user_plus)
                        .sizeDp(24));
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.MenuMisReservas)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_user_tag)
                        .sizeDp(24));
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.MenuAñadirLibro)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_plus)
                        .sizeDp(24));
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.MenuConfiguracion)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_cog)
                        .sizeDp(24));
        PrimaryDrawerItem item6 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.MenuCompartirconFacebook)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_facebook)
                        .sizeDp(24));
        PrimaryDrawerItem item7 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.MenuCompartirconWhatsapp)
                .withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_whatsapp)
                        .sizeDp(24));

//create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        item1,
                        item2,
                        item3,
                        item4,
                        item5,
                        new DividerDrawerItem(),
                        item6,
                        item7
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        return true;
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
        //Inicializamos la base de datos local
        Realm.init(getApplicationContext());
        //Estableemos la conexion con la base de datos
        LibroDatos.conexion = Realm.getDefaultInstance();
        iniciaCarga(false);
        /*View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);*/
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        //Preparamos los datos a mostrar indicando de donde se obtienen los datos
        // y el número de paneles
        adaptador = new SimpleItemRecyclerViewAdapter(this, LibroDatos.listalibros, mTwoPane);
        recyclerView.setAdapter(adaptador);
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private List<Libro> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Control cuando seleccionan un libro
                Libro item = (Libro) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(ItemDetailFragment.ARG_ITEM_ID, item.getId());
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.getId());

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                      List<Libro> items,
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
        public int getItemViewType(int position){
            //Cómo queremos un layout distinto para pares e impares, aquí utilizamos
            //position que nos indica la posición del elemento de la lista para
            //gestionar el viewType de la función onCreateViewHolder
            return position % 2;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.titulolista.setText(mValues.get(position).getTitulo());
            //holder.autorlista.setText(mValues.get(position).getAuthor());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView titulolista;
            //final TextView autorlista;

            ViewHolder(View view) {
                super(view);
                titulolista = (TextView) view.findViewById(R.id.id_text);
                //autorlista = (TextView) view.findViewById(R.id.autor);
            }
        }
        //Método que actuliza los datos de lista.
        public void setItems(List<Libro> items) {
            Log.d("TAG", "actualizando");
            mValues = items;
            notifyDataSetChanged();
            //Indicamos que se ha actualizado la lista y que se tiene que refrescar
        }
    }



    private  void iniciaCarga(boolean actualiza){
        /* Control de Internet
         * En caso de que no haya conexión a la RED no se realizará la carga de los datos
         * desde el servidor FireBase
         * El valor de actualiza es el que indica si estamos creando el adaptador o se
         * refresca la lista
         * */
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        if (actNetInfo != null && actNetInfo.isConnected() && actNetInfo.isAvailable()) {
            Toast.makeText(this, "Red activada, cargando datos desde FireBase", Toast.LENGTH_LONG).show();
            cargaDatosFirebase(actualiza);
        }
        else{
            Toast.makeText(this, "No hay acceso a Internet, se carga la información de la base de datos local", Toast.LENGTH_LONG).show();

            cargarRealm(actualiza);
        }
    }

    private void cargaDatosFirebase(final boolean actualiza){
        //Nuevas característivas de Firebase en el proyecto
        FirebaseApp.initializeApp(ItemListActivity.this);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("books");
        // Leemos la información de la Base de Datos
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ArrayList<Libro>> genericTypeIndicator =new GenericTypeIndicator<ArrayList<Libro>>(){};
                //Obtenemos el listado y lo asignamos a lalista que utilizamos ne la aplicación
                LibroDatos.listalibros=dataSnapshot.getValue(genericTypeIndicator);
                for (int i=0;i<LibroDatos.listalibros.size();i++) {
                    //Actualizamos el id puesto que no esta en Firebase
                    LibroDatos.listalibros.get(i).setId(i);
                    if (!LibroDatos.exists(LibroDatos.listalibros.get(i))){
                        //Si el libro no existe lo añadimos a la base de datos local
                        LibroDatos.conexion.beginTransaction();
                        LibroDatos.conexion.insert(LibroDatos.listalibros.get(i));
                        LibroDatos.conexion.commitTransaction();
                    }
                }
                //El parámetro actualiza indica si es una nueva carga, o actualizar la lista
                if (!actualiza)
                    cargaReciclerView();
                else
                    adaptador.setItems(LibroDatos.listalibros);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Si no se ha posidido leer del servidor firebase
                Toast.makeText(ItemListActivity.this, "No se leído desde el servidor, se carga la información de la base de datos local", Toast.LENGTH_LONG).show();
                cargarRealm(actualiza);
                Log.i("TAG", "Error de lectura.", error.toException());
            }
        });

    }

    //Función encargada de obtener los datos desde la base de datos local, y rellenar la lista
    private void cargarRealm(boolean actualiza){
        /*LibroDatos.conexion.beginTransaction();
        //Recuperamos todos los libros de a base de datos
        final RealmResults<Libro> ls = LibroDatos.conexion.where(Libro.class).findAll();
        LibroDatos.conexion.commitTransaction();*/
        LibroDatos.listalibros = (ArrayList)LibroDatos.getBooks();
        Log.d("TAG","datos" + LibroDatos.listalibros.size());
        //El parámetro actualiza indica si es una nueva carga, o actualizar la lista
        if (!actualiza)
            cargaReciclerView();
        else
            adaptador.setItems(LibroDatos.listalibros);
    }

    //Función que genera el recyclerview
    void cargaReciclerView(){
        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }




}
