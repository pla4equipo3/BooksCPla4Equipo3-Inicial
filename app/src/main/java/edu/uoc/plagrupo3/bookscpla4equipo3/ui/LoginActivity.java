package edu.uoc.plagrupo3.bookscpla4equipo3.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import edu.uoc.plagrupo3.bookscpla4equipo3.R;
import edu.uoc.plagrupo3.bookscpla4equipo3.sharePreferences.LoadLanguage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG ="SignInActivity";
    private GoogleApiClient googleApiClient;
    public static final int SIGN_IN_CODE = 777;
    private ImageView logo;
    private ProgressBar progressBar;
    private SignInButton botonGoogle;
    private TextInputLayout contenedorEmail;
    private TextInputLayout contenedorPassword;
    private TextView actualizarPassword;
    private Button botonIniciarSesion;
    private Button botonRegistro;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LoadLanguage(this).Change();
        setContentView(R.layout.activity_login);
        rotationLogo(); // metodo que realiza una animación al logo de la app

        contenedorEmail =(TextInputLayout)findViewById(R.id.textInputLayout);
        contenedorPassword=(TextInputLayout)findViewById(R.id.textInputLayout2);
        botonIniciarSesion=(Button) findViewById(R.id.buttonIniciarSesion);
        botonRegistro=(Button)findViewById(R.id.buttonRegistrar);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        actualizarPassword = (TextView) findViewById(R.id.textViewActualizarPassword);

        botonGoogle =(SignInButton)findViewById(R.id.botonGoogle);
        try {
            ((TextView) botonGoogle.getChildAt(0)).setText(getResources().getString(R.string.botonIniciarSesionGoogle));
        } catch (ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // obtenemos token
                .requestEmail()
                .build();//configurar el inicio de sesión de Google para solicitar los datos de usuario requeridos por la aplicación


        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build(); // mediante googleApiClient podemos tner acceso a las apis de google o sea la de autentificacion


        botonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hacemos un intent para conectarnos con las apis de google
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,SIGN_IN_CODE);
            }
        });

        botonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);

                startActivity(intent);
              //  validarForm_RegistrarUsuario();
            }
        });

        botonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarForm_LoginUsuario();

            }
        });

        actualizarPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCuadroDialogoPassw();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();
                // si hay un usuario activo con la sesion abierta lo redireccionamos a la mainActivity
                if (user != null){
                    goItemListActivity();
                }
            }
        };







    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // obtenemos resultado de autenticar

        if (requestCode ==SIGN_IN_CODE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result); // metodo que maneja el resultado
        }


    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()){
            firebaseAuthWithGoogle(result.getSignInAccount()); // metodo que se encarga de la autenficacion con firebase, le mandamos la cuenta
        }
        else{
            Toast.makeText(this,getResources().getString(R.string.ErrorSesionGoogle),Toast.LENGTH_LONG).show();

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount signInAccount) {
        progressBar.setVisibility(View.VISIBLE);
     //   botonGoogle.setVisibility(View.GONE);
     //   logo.setVisibility(View.GONE);

        //creamos credencial y le proporcionamos el token que conseguimos del objeto cuenta
        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(),null);

        //ya tenemos la credencial ahora si podemos autenfiticarnos con firebase,adjuntamos un oyente que nos dira cuando esto termine
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
              //  botonGoogle.setVisibility(View.VISIBLE);
             //   logo.setVisibility(View.VISIBLE);

                if (!task.isSuccessful()) Toast.makeText(getApplicationContext(),getResources().getString(R.string.ErrorSesionFirebase),Toast.LENGTH_LONG).show();

            }

        }) ;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //el oyente empieze a escuchar los cambios
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop(); // remover el oyente
        if (mAuthListener!= null) mAuth.removeAuthStateListener(mAuthListener);

    }


    private void goItemListActivity()
    {    //abrir main activity
        Intent intent = new Intent(this,ItemListActivity.class)
         .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
       finish();
    }

    private void rotationLogo(){
        logo = (ImageView)findViewById(R.id.imageViewLogo);
        final Animation myRotation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim);
        logo.startAnimation(myRotation);
        myRotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                logo.startAnimation(myRotation);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void abrirCuadroDialogoPassw() {
        // método que abre un cuadro de dialogo personalizado para restablecer contraseña

        final View view =getLayoutInflater().inflate(R.layout.dialog_password,null);
        final AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this)
                .setView(view)
                .setTitle(getResources().getString(R.string.RestablecerContrasenyaTitulo))
                .setMessage(getResources().getString(R.string.RestablecerContrasenyaMensaje))
                .setIcon(R.drawable.email_icon)
                .setPositiveButton(getResources().getString(R.string.ButtonAceptar),null)
                .setNegativeButton(getResources().getString(R.string.ButtonCancelar),null)
                .show();
        final TextInputLayout contenedorEmailDialog =(TextInputLayout)view.findViewById(R.id.textInputLayoutEmailDialog);
        Button ok = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo = contenedorEmailDialog.getEditText().getText().toString().trim();
                if (!validarEmail(correo)){

                    contenedorEmailDialog.setErrorEnabled(true);
                    contenedorEmailDialog.setError(getResources().getString(R.string.ErrorEmail));


                }else{

                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(contenedorEmailDialog.getWindowToken(), 0);
                    }catch (NullPointerException ex){
                        ex.printStackTrace();
                    }
                    contenedorEmailDialog.setErrorEnabled(false);
                    restablecerPassword(correo); // llamamos metodo para restablecer contraseña


                }

            }
        });

    }

    private void restablecerPassword(final String email)
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            Toast.makeText(getApplicationContext(),getResources().
                                    getString(R.string.ProgresDialogConfirmacionEnvio)+" "+email,Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);

                        }else{

                            Toast.makeText(getApplicationContext(),getResources().
                                    getString(R.string.ProgresDialogCancelacionEnvio)+" "+email,Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }

                    }
                });

    }

    private void validarForm_LoginUsuario() {
        //Metodo para validar el formulario Login
        String email = contenedorEmail.getEditText().getText().toString().trim();
        String passw = contenedorPassword.getEditText().getText().toString().trim();
        hideKeyboard();
        if (TextUtils.isEmpty(email)) {
            contenedorEmail.setError(getResources().getString(R.string.ErrorEmail2));
            contenedorEmail.getEditText().requestFocus();

        } else if (TextUtils.isEmpty(passw)){
            contenedorEmail.setErrorEnabled(false);
            contenedorPassword.setError(getResources().getString(R.string.ErrorPassw2));
            contenedorPassword.getEditText().requestFocus();
        }else{
            contenedorPassword.setErrorEnabled(false);
            contenedorEmail.setErrorEnabled(false);
            autenticarUsuarioFirebase(email,passw); // llamo al metodo

        }
    }

    private void autenticarUsuarioFirebase(String email, String password){
        //metodo para autenticar usuario segun el resultado de la tarea,abre otra actividad
        //en caso contrario capturo la excepción y muestro al usuario
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.InicioSesion) + user.getEmail(),Toast.LENGTH_LONG).show();
                            goItemListActivity();
                        }else{
                            Log.d("ERROR_SESION",task.getException().getMessage());
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.ErrorInicioSesion),Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);

                    }
                });

    }


    private void validarForm_RegistrarUsuario() {
        //metodo para validar formulario Registro
        String email = contenedorEmail.getEditText().getText().toString().trim();
        String passw = contenedorPassword.getEditText().getText().toString().trim();
        hideKeyboard();
        if (!validarEmail(email)) {
            contenedorEmail.setError(getResources().getString(R.string.ErrorEmail));
            contenedorEmail.getEditText().requestFocus();
        } else if (!validarPassword(passw)) {
            contenedorEmail.setErrorEnabled(false);
            contenedorPassword.setError(getResources().getString(R.string.ErrorPassw));
            contenedorPassword.getEditText().requestFocus();
        } else {
            contenedorPassword.setErrorEnabled(false);
            contenedorEmail.setErrorEnabled(false);
            //Toast.makeText(getApplicationContext(),"Bien",Toast.LENGTH_LONG).show();
            registrarUsuarioFirebase(email,passw);

        }

    }

    private void registrarUsuarioFirebase(String email, String password)
    {
        //metodo para registrar usuario segun el resultado de la tarea,abre otra actividad
        //en caso contrario capturo la excepción y muestro al usuario

        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.hintEmail)+": "+ user.getEmail()+""+getResources().getString(R.string.Ok_Registro),Toast.LENGTH_LONG).show();
                            goItemListActivity();
                        }else{
                            Log.d("ERROR_REGISTRO",task.getException().getMessage());
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.ErrorRegistro),
                                    Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);

                    }
                });
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
                        hideSoftInputFromWindow(view.getWindowToken(),0);
            }
        } catch(NullPointerException ex){
            ex.printStackTrace();

        }
    }




    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectionFailed:" + connectionResult);
    }
}
