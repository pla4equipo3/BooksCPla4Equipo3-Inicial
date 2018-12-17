package edu.uoc.plagrupo3.bookscpla4equipo3.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import edu.uoc.plagrupo3.bookscpla4equipo3.R;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.Libro;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.LibroDatos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class addBook extends  AppCompatActivity  {

    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient mFusedLocationClient;
    TextView nLongitud, nLatitud;
    EditText titulo, autor, fecha, descripcion;
    ImageView botonUbicacion, imagenLibro;
    CheckBox disponible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        //Preparamos la variables de los controles, para crear el libro al pulsar el botón
        titulo = (EditText) findViewById(R.id.newTitulo);
        autor = (EditText) findViewById(R.id.newAutor);
        descripcion = (EditText) findViewById(R.id.newDescripcion);
        fecha = (EditText) findViewById(R.id.newFecha);

        disponible = (CheckBox) findViewById(R.id.newDisponible);

        nLongitud = (TextView) findViewById(R.id.nTextLongitud);
        nLatitud = (TextView) findViewById(R.id.nTextLatitud);

        imagenLibro = (ImageView) findViewById(R.id.newimageViewFotoLibro);
        botonUbicacion = (ImageView) findViewById(R.id.newimageButtonGPS);
        botonUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Este icono permite actualizar las etiquetas con la ubicicación actual, si disponemos de permisos
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                preparaUbicacion();
            }
        });
        Button botonAdd = (Button) findViewById(R.id.buttonAdd);
        botonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Empezamos el proceso de añadir, no controlamos los datos escritos
                addBook();
            }
        });
        Button botonCancel = (Button) findViewById(R.id.buttonCancelar);
        botonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //El botón de Cancelar finaliza la actividad
                finish();
            }
        });
    }


    public void addBook(){
        //Se genera un nuevo libro que luego se añade a firebase
        Libro nuevoLibro = new Libro();
        nuevoLibro.setAutor(autor.getText().toString());
        nuevoLibro.setDescripcion(descripcion.getText().toString());
        nuevoLibro.setFavorito(false);
        nuevoLibro.setLatitud(nLatitud.getText().toString());
        nuevoLibro.setLongitud(nLongitud.getText().toString());
        nuevoLibro.setpublicationdate(fecha.getText().toString());
        nuevoLibro.setTitulo(titulo.getText().toString());
        nuevoLibro.setUrlimage(imagenLibro.getDrawable().toString());
        nuevoLibro.setId(LibroDatos.getBooks().size());
        nuevoLibro.setKeylibro(""+nuevoLibro.getId());
        if (disponible.isChecked())
            nuevoLibro.setDisponible("true");
        else
            nuevoLibro.setDisponible("true");
        mDatabase= FirebaseDatabase.getInstance().getReference("books");
        mDatabase.child(nuevoLibro.getKeylibro()).setValue(nuevoLibro);
        Toast.makeText(this,"EL LIBRO HA SIDO AÑADIDO",Toast.LENGTH_LONG).show();
        finish();
    }


    public void preparaUbicacion() {
        //Comprobamos si tenemos permiso para obtener la ubicación, y de no ser así lo pedimos
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PETICION_PERMISO_LOCALIZACION);
        } else {
            //Tenemos el permiso y solcicitamos la ubicación
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Log.e("TAG", "Donde estoy111 " + location.getLatitude());
                                //Actualizamos las etiquetas con la ubicación obtenida
                                nLatitud.setText("" + location.getLatitude());
                                nLongitud.setText("" + location.getLongitude());
                            }
                        }
                    });
        }
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
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        Log.e("TAG", "Donde estoy" + location.getLatitude());
                                        nLatitud.setText("" + location.getLatitude());
                                        nLongitud.setText("" + location.getLongitude());
                                }
                                }
                            });
                } catch (SecurityException e) {}
            } else {
                Log.e("TAG", "Permiso denegado");
            }
        }
    }


    }
