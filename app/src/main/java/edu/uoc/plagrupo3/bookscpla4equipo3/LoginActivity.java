package edu.uoc.plagrupo3.bookscpla4equipo3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG ="SignInActivity";
    private GoogleApiClient googleApiClient;
    public static final int SIGN_IN_CODE = 777;
    private ImageView logo;
    private ProgressBar progressBar;
    private SignInButton botonGoogle;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        rotationLogo(); // metodo que realiza una animación al logo de la app
        botonGoogle =(SignInButton)findViewById(R.id.botonGoogle);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

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




    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectionFailed:" + connectionResult);
    }
}
