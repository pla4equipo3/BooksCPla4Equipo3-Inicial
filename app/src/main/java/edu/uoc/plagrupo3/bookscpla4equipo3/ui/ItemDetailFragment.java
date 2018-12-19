package edu.uoc.plagrupo3.bookscpla4equipo3.ui;

import android.Manifest;
import android.app.Activity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import edu.uoc.plagrupo3.bookscpla4equipo3.R;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.Libro;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.LibroDatos;
import edu.uoc.plagrupo3.bookscpla4equipo3.net.TestearRed;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";
    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private FusedLocationProviderClient mFusedLocationClient;

    private Libro mItem;
    private TextView descripcion,autor,fechaP,titulo,disponible;
    private ImageView fotoLibro;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference mDatabaseRef;
   // private FloatingActionButton buttonFavoritos;
    private Button buttonLiberar,buttonReservar;
    private ImageView imageGPS;
    private ItemDetailActivity itemDetailActivity;

    private String latitud,longitud;

   public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
           // int n = LibroDatos.listalibros.size();
           //int p = getArguments().getInt(ARG_ITEM_ID);
           //mItem = LibroDatos.listalibros.get(Integer.parseInt(getArguments().getString(ARG_ITEM_ID)));
            mItem = LibroDatos.listalibros.get(getArguments().getInt(ARG_ITEM_ID));

          //  mItem = LibroDatos.getBooks().get(getArguments().getInt(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

            if (appBarLayout != null) appBarLayout.setTitle(mItem.getTitulo());
             else activity.setTitle(mItem.getTitulo());

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        descripcion = (TextView) rootView.findViewById(R.id.textViewDescripcion);
        titulo = (TextView)rootView.findViewById(R.id.textViewTitulo);
        autor = (TextView)rootView.findViewById(R.id.textViewAutor);
        fechaP = (TextView)rootView.findViewById(R.id.textViewFecha);
        fotoLibro = (ImageView)rootView.findViewById(R.id.imageViewFotoLibro);
        disponible = (TextView)rootView.findViewById(R.id.textViewDisponible);
       // buttonFavoritos = (FloatingActionButton)rootView.findViewById(R.id.buttonFavorito);
        buttonLiberar = (Button)rootView.findViewById(R.id.buttonLiberar);
        buttonReservar = (Button)rootView.findViewById(R.id.buttonReservar);
        imageGPS = (ImageView) rootView.findViewById(R.id.imageButtonGPS);


        user = FirebaseAuth.getInstance().getCurrentUser(); // obtenemos usuario actual

        database=FirebaseDatabase.getInstance();
        mDatabaseRef=database.getReference();

        if (mItem != null) {
            autor.setText("By "+mItem.getAutor());
            titulo.setText(mItem.getTitulo());
            fechaP.setText(mItem.getFechapub());
            descripcion.setText(mItem.getDescripcion());
            if (mItem.getDisponible().equalsIgnoreCase("true"))
            {
                disponible.setText(R.string.Disponible);
                disponible.setTextColor(Color.GREEN);

            }else{
                disponible.setText(R.string.NoDisponible);
                disponible.setTextColor(Color.RED);

            }
            Picasso.get().load(mItem.getUrlimage()).error(R.drawable.no_photo).placeholder(R.drawable.logo).fit().centerCrop().into(fotoLibro);


           }

           buttonReservar.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   try {
                       if (TestearRed.isNetworkConnected(getContext()))
                           booking();
                       else Toast.makeText(getContext(),getResources().getString(R.string.OperacionNo),Toast.LENGTH_LONG).show();
                   }catch (NullPointerException ex){ex.printStackTrace();}


               }
           });

           buttonLiberar.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   try {
                       if (TestearRed.isNetworkConnected(getContext()))
                           free();
                       else Toast.makeText(getContext(),getResources().getString(R.string.OperacionNo),Toast.LENGTH_LONG).show();
                   }catch (NullPointerException ex){ex.printStackTrace();}


               }
           });

           imageGPS.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   cargarMapa();
               }
           });




        return rootView;
    }


    private void booking()
    {
        if (disponible.getText().toString().equalsIgnoreCase(getResources().getString(R.string.Disponible)))
        {
           // Hacemos la reserva ,obtenemos el usuario y la clave del libro y actualizamos datos

            if (user!=null){
                String emailUser = user.getEmail();
                String clave = mItem.getKeylibro();

                mDatabaseRef.child("books").child(clave).child("disponible").setValue("false");
                mDatabaseRef.child("books").child(clave).child("usuarioactivo").setValue(emailUser);
                Toast.makeText(getContext(),getResources().getString(R.string.LibroReservado),Toast.LENGTH_LONG).show();
                goItemListActivity();
                }

        }else{
            Toast.makeText(getContext(),getResources().getString(R.string.NoDisponible),Toast.LENGTH_LONG).show();
            }

    }

    private void free(){

       // liberamos el libro en casa de que el libro no este disponible y que el usuario de la aplicacion sea el mismo que hay en la base de datos
       if (disponible.getText().toString().equalsIgnoreCase(getResources().getString(R.string.Disponible)))
       {
           Toast.makeText(getContext(),getResources().getString(R.string.LiberadoOk),Toast.LENGTH_LONG).show();

       }else{
           String usuarioActivo = mItem.getUsuarioactivo();


           if (usuarioActivo!=null)
           {
               if (usuarioActivo.equalsIgnoreCase(user.getEmail())){
                   mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
                   preparaUbicacion();


               }else{
                   Toast.makeText(getContext(),getResources().getString(R.string.LiberadoNot),Toast.LENGTH_LONG).show();

               }
           }

       }


    }
    private void goItemListActivity() {
        //método para volver al login
        Intent intent = new Intent(getContext(), ItemListActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

    }


    private void preparaUbicacion() {
        //Comprobamos si tenemos permiso para obtener la ubicación, y de no ser así lo pedimos
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PETICION_PERMISO_LOCALIZACION);
        } else {
            //Tenemos el permiso y solcicitamos la ubicación
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Log.e("TAG", "Donde estoy111 " + location.getLatitude());
                                //Actualizamos las etiquetas con la ubicación obtenida
                                latitud =String.valueOf(location.getLatitude());
                                longitud=String.valueOf(location.getLongitude());
                                String clave = mItem.getKeylibro();
                                try {
                                    mDatabaseRef.child("books").child(clave).child("disponible").setValue("true");
                                    mDatabaseRef.child("books").child(clave).child("usuarioactivo").setValue("sin");
                                    mDatabaseRef.child("books").child(clave).child("latitud").setValue(latitud);
                                    mDatabaseRef.child("books").child(clave).child("longitud").setValue(longitud);

                                    Toast.makeText(getContext(), getResources().getString(R.string.LibroLiberado), Toast.LENGTH_LONG).show();
                                    goItemListActivity();

                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }


                            }
                        }
                    });
        }

    }


    private void cargarMapa(){

        Intent intent = new Intent(this.getContext(), MapsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, mItem.getId());
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PETICION_PERMISO_LOCALIZACION) {
            //Controlamos la respuesta ala petición del permiso sobre la ubicación
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("TAG", "Permiso concedido");
                try {
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        Log.e("TAG", "Donde estoy" + location.getLatitude());

                                        latitud =String.valueOf(location.getLatitude());
                                        longitud=String.valueOf(location.getLongitude());


                                    }
                                }
                            });
                } catch (SecurityException e) {
                }
            } else {
                Log.e("TAG", "Permiso denegado");

            }
        }





    }
/*
    private void dialogCloseSesion() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(getResources().getString(R.string.MenuMisFavoritos));
        alertDialog.setMessage(getResources().getString(R.string.DialogAñadirFavorito)+" "+mItem.getTitulo()+"?");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.SI),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                      LibroDatos.addFavourite(mItem.getId()); // método añadir a favorito
                      dialog.dismiss();

                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.NO),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LibroDatos.deleteFavourite(mItem.getId()); // método borrar de favoritos
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

  */

}
