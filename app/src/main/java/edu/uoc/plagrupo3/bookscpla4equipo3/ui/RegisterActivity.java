package edu.uoc.plagrupo3.bookscpla4equipo3.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import edu.uoc.plagrupo3.bookscpla4equipo3.R;
import edu.uoc.plagrupo3.bookscpla4equipo3.sharePreferences.LoadLanguage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout contenedorEmail;
    private TextInputLayout contenedorPassword;
    private TextInputLayout contenedorUserName;
    private ImageView imagenFoto;
    private Button buttonRegistro;
    private ProgressBar progressBar;
    private static final int GALLERY_INTENT = 1;
    private Uri fotoSeleccionada;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LoadLanguage(this).Change();
        setContentView(R.layout.activity_register);
        setTitle(R.string.activityRegister_name);
        mAuth = FirebaseAuth.getInstance(); //obtenemos instancia de FirebaseAuth
        mStorage= FirebaseStorage.getInstance().getReference(); // obtener referencia al FirebaseStore

        contenedorEmail = (TextInputLayout) findViewById(R.id.textInputLayoutEmail);
        contenedorPassword = (TextInputLayout) findViewById(R.id.textInputLayoutPassw);
        contenedorUserName = (TextInputLayout) findViewById(R.id.textInputLayoutName);
        buttonRegistro = (Button) findViewById(R.id.buttonRegistrar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imagenFoto = (ImageView) findViewById(R.id.imageViewFotoUsuario);

        imagenFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,getResources().getString(R.string.ElegirFoto)),GALLERY_INTENT);

            }
        });
        fotoSeleccionada = Uri.parse("android.resource://" + getPackageName()
                + "/drawable/" + "ic_user"); //foto por defecto

        buttonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarForm_RegistrarUsuario();
            }
        });
    }

        private void validarForm_RegistrarUsuario() {
            //metodo para validar formulario Registro
            String email = contenedorEmail.getEditText().getText().toString().trim();
            String passw = contenedorPassword.getEditText().getText().toString().trim();
            String username = contenedorUserName.getEditText().getText().toString().trim();

            hideKeyboard();
            if (username.isEmpty()){
                contenedorUserName.setError(getResources().getString(R.string.ErrorUserName));
                contenedorUserName.getEditText().requestFocus();
            } else if (!validarEmail(email)) {
                contenedorUserName.setErrorEnabled(false);
                contenedorEmail.setError(getResources().getString(R.string.ErrorEmail));
                contenedorEmail.getEditText().requestFocus();
            } else if (!validarPassword(passw)) {
                contenedorEmail.setErrorEnabled(false);
                contenedorPassword.setError(getResources().getString(R.string.ErrorPassw));
                contenedorPassword.getEditText().requestFocus();
            } else {
                contenedorPassword.setErrorEnabled(false);
                contenedorEmail.setErrorEnabled(false);
                contenedorUserName.setErrorEnabled(false);

                registrarUsuarioFirebase(email,passw,username);

            }

        }

        private void registrarUsuarioFirebase(String email, String passw, String username){
        // metodo para registrar en Firebase
            progressBar.setVisibility(View.VISIBLE);
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, passw)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                 updateUser(username);
                            }else{
                                Log.d("ERROR_REGISTRO",task.getException().getMessage());
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.ErrorRegistro),
                                        Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    });




        }

        private void updateUser(String username){

            try {

                final StorageReference mStorageRef = mStorage.child("FotosUsuario").child(fotoSeleccionada.getLastPathSegment());
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
                            final Uri downloadUri = task.getResult();
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .setPhotoUri(downloadUri)
                                    .build();
                            user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.hintEmail) + ": " + user.getEmail() + " " + getResources().getString(R.string.Ok_Registro), Toast.LENGTH_LONG).show();
                                    goItemListActivity();
                                }
                            });


                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Foto_Error), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }catch (NullPointerException ex)
            {
                ex.printStackTrace();
                Log.e("ERROR",ex.getMessage());
                Toast.makeText(getApplicationContext(),"Error2", Toast.LENGTH_LONG).show();


            }

        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== GALLERY_INTENT && resultCode==RESULT_OK){

            fotoSeleccionada= data.getData();
            if (fotoSeleccionada!= null){
                Picasso.get().load(fotoSeleccionada).centerCrop().fit().into(imagenFoto);

            }else {
                fotoSeleccionada = Uri.parse("android.resource://" + getPackageName()
                        + "/drawable/" + "ic_user");
            }
        }

    }
    private void goItemListActivity()
    {    //abrir main activity
        Intent intent = new Intent(this,ItemListActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
    private boolean validarEmail(String email) {
            //metodo para validar email utilizando la clase Pattern
            Pattern pattern = Patterns.EMAIL_ADDRESS;
            return pattern.matcher(email).matches();
        }
        private boolean validarPassword(String passw){
            // que la contaseña tenga mas de 6 caracteres
            return passw.length()>6;
        }
        private void hideKeyboard() {
            //  metodo para ocultar teclado
            try {
                View view = getCurrentFocus();
                if (view != null) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                            hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();

            }


        }










    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, LoginActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
