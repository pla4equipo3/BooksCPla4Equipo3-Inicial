package edu.uoc.plagrupo3.bookscpla4equipo3.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import edu.uoc.plagrupo3.bookscpla4equipo3.R;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.Libro;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.LibroDatos;
import edu.uoc.plagrupo3.bookscpla4equipo3.sharePreferences.LoadLanguage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class AddBook extends  AppCompatActivity  {

    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private static final int GALLERY_INTENT = 1;
    private static final String BOOK_REFERENCE="books";

    private DatabaseReference mDatabase;
    private FusedLocationProviderClient mFusedLocationClient;

    private TextView nLongitud, nLatitud,labelLongitud,labelLatitud;
    private TextInputLayout titulo;
    private TextInputLayout autor;
    private TextInputLayout fecha;
    private EditText descripcion;
    private ImageView botonUbicacion;
    private ImageView imagenLibro;
    private FloatingActionButton buttonSubirLibro;
    private ProgressBar progressBar;
    private Uri fotoSeleccionada;
    private StorageReference mStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LoadLanguage(this).Change();
        setContentView(R.layout.activity_add_book);
        setTitle(R.string.title_addBooks);

        //Preparamos la variables de los controles, para crear el libro al pulsar el botón
        imagenLibro = (ImageView) findViewById(R.id.newimageViewFotoLibro);
        botonUbicacion = (ImageView) findViewById(R.id.imageViewLocation);
        buttonSubirLibro = (FloatingActionButton)findViewById(R.id.floatingActionButton);
        titulo = (TextInputLayout) findViewById(R.id.textInputLayoutTitulo);
        autor = (TextInputLayout) findViewById(R.id.textInputLayoutAutor);
        descripcion = (EditText) findViewById(R.id.editTextDescripcion);
        fecha = (TextInputLayout) findViewById(R.id.textInputLayoutFecha);
        nLongitud = (TextView) findViewById(R.id.textViewLongitud);
        nLatitud = (TextView) findViewById(R.id.textViewLatitud);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        labelLongitud = (TextView)findViewById(R.id.textViewLabelLongitud);
        labelLatitud = (TextView)findViewById(R.id.textViewLabelLatitud);

        mStorage= FirebaseStorage.getInstance().getReference(); // obtener referencia al FirebaseStore
        mDatabase= FirebaseDatabase.getInstance().getReference(BOOK_REFERENCE); // referencia a FirebaseDatabase
        fotoSeleccionada = Uri.parse("android.resource://" + getPackageName()
                + "/drawable/" + "addbook");


        imagenLibro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,getResources().getString(R.string.ElegirFoto)),GALLERY_INTENT);


            }
        });
        buttonSubirLibro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarFormulario();

            }
        });


        botonUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Este icono permite actualizar las etiquetas con la ubicicación actual, si disponemos de permisos
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                preparaUbicacion();
            }
        });





}
    private void cleanViews(){
        autor.getEditText().setText("");
        titulo.getEditText().setText("");
        fecha.getEditText().setText("");
        descripcion.setText("");
        labelLatitud.setVisibility(View.GONE);
        labelLongitud.setVisibility(View.GONE);
        nLatitud.setText("");
        nLongitud.setText("");
        imagenLibro.setImageResource(R.drawable.addbook);

    }


private void validarFormulario(){
      String tituloLibro= titulo.getEditText().getText().toString().trim();
      String autorLibro = autor.getEditText().getText().toString().trim();
      String fechaLibro = fecha.getEditText().getText().toString().trim();
      String descripcionLibro = descripcion.getText().toString().trim();
      String latitud = nLatitud.getText().toString();
      String longitud = nLongitud.getText().toString();

      if (tituloLibro.isEmpty()){
          titulo.setError(getResources().getString(R.string.errorTitulo));
          titulo.getEditText().requestFocus();
      }else if (autorLibro.isEmpty()){
          autor.setError(getResources().getString(R.string.errorAutor));
          autor.getEditText().requestFocus();
          titulo.setError(null);
      }else if (fechaLibro.isEmpty()) {
          fecha.setError(getResources().getString(R.string.errorFecha));
          fecha.getEditText().requestFocus();
          autor.setError(null); titulo.setError(null);
      }else if (descripcionLibro.isEmpty()){
          descripcion.setHint(R.string.errorDescripcion);
          descripcion.setHintTextColor(Color.RED);
          fecha.setError(null);titulo.setError(null); autor.setError(null);
          descripcion.requestFocus();
      }else if (latitud.isEmpty()&& longitud.isEmpty()){
          Toast.makeText(getApplicationContext(),getResources().getString(R.string.errorLocalizacion),Toast.LENGTH_LONG).show();

          botonUbicacion.requestFocus();

      }else{
          descripcion.setHint(R.string.nDescripcion);
          descripcion.setHintTextColor(Color.BLACK);
         // Toast.makeText(getApplicationContext(),"correcto",Toast.LENGTH_LONG).show();

          progressBar.setVisibility(View.VISIBLE);
          pushFotoToStorage();



      }


}

 private void pushBook(String UrlImage){
        try {
            String key = (String.valueOf(LibroDatos.getBooks().size()));
            Libro nuevoLibro = new Libro();

            nuevoLibro.setAutor(autor.getEditText().getText().toString().trim());
            nuevoLibro.setTitulo(titulo.getEditText().getText().toString().trim());
            nuevoLibro.setpublicationdate(fecha.getEditText().getText().toString().trim());
            nuevoLibro.setDescripcion(descripcion.getText().toString().trim());

            nuevoLibro.setUrlimage(UrlImage);
            nuevoLibro.setLatitud(nLatitud.getText().toString());
            nuevoLibro.setLongitud(nLongitud.getText().toString());
            nuevoLibro.setDisponible("true");
            nuevoLibro.setKeylibro(key);
            nuevoLibro.setUsuarioactivo("sin");

            mDatabase.child(key).setValue(nuevoLibro);
            Toast.makeText(this,getResources().getString(R.string.LibroSubidoOk),Toast.LENGTH_LONG).show();
            cleanViews();
        }catch (Exception ex){
            ex.printStackTrace();
            Log.e("Error", ex.getMessage());
            Toast.makeText(this,getResources().getString(R.string.LibroSubidoNot),Toast.LENGTH_LONG).show();
        }



 }


 private void pushFotoToStorage(){

     try {

         final StorageReference mStorageRef = mStorage.child("FotosLibros").child(fotoSeleccionada.getLastPathSegment());
         mStorageRef.putFile(fotoSeleccionada).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
             @Override
             public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                 if (!task.isSuccessful()) {
                     throw task.getException();
                 }
                 return mStorageRef.getDownloadUrl();
             }
         }).addOnCompleteListener(new OnCompleteListener<Uri>() {
             @Override
             public void onComplete(@NonNull Task<Uri> task) {
                 if (task.isSuccessful()) {
                     final String downloadUri = task.getResult().toString();
                     pushBook(downloadUri);
                     progressBar.setVisibility(View.GONE);


                 } else {
                     Toast.makeText(getApplicationContext(), getResources().getString(R.string.Foto_Error), Toast.LENGTH_LONG).show();
                     pushBook(imagenLibro.getDrawable().toString());
                     progressBar.setVisibility(View.GONE);
                 }
             }
         });
     }catch (NullPointerException ex)
     {
         ex.printStackTrace();
         progressBar.setVisibility(View.GONE);
         pushBook(imagenLibro.getDrawable().toString());
         Log.e("ERROR",ex.getMessage());
         Toast.makeText(getApplicationContext(),getResources().getString(R.string.Foto_Error), Toast.LENGTH_LONG).show();


     }

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
                                 labelLatitud.setVisibility(View.VISIBLE);
                                 labelLongitud.setVisibility(View.VISIBLE);
                                 nLatitud.setText( String.valueOf(location.getLatitude()));
                                 nLongitud.setText(String.valueOf(location.getLongitude()));
                                 }
                            else{
                                nLatitud.setText("41.3895158");
                                nLongitud.setText("2.1501107");
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
                                        labelLatitud.setVisibility(View.VISIBLE);
                                        labelLongitud.setVisibility(View.VISIBLE);

                                        nLatitud.setText(String.valueOf(location.getLatitude()));
                                        nLongitud.setText(String.valueOf(location.getLongitude()));
                                        //Toast.makeText(getApplicationContext(),"Override",Toast.LENGTH_LONG).show();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== GALLERY_INTENT && resultCode==RESULT_OK){

            fotoSeleccionada= data.getData();
            if (fotoSeleccionada!= null){
                Picasso.get().load(fotoSeleccionada).centerCrop().fit().into(imagenLibro);

            }else {
                fotoSeleccionada = Uri.parse("android.resource://" + getPackageName()
                        + "/drawable/" + "logo");
            }
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, ItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}