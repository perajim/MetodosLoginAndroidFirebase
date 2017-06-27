package perajim.firebasecursoplatzi;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity  implements  GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "MainActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Button btnCreateAccount;
    private Button btnSignIn;
    private SignInButton  btnSignInGoogle;
    private LoginButton btnSignInFacebook;

    private EditText edtEmail;
    private EditText edtPassword;

    private CallbackManager callbackManager;

    private GoogleApiClient googleApiClient;
    private static final int SIGN_IN_GOOGLE_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk .sdkInitialize(getApplicationContext());
        callbackManager         =  CallbackManager.Factory.create();
        btnCreateAccount        = (Button) findViewById(R.id.btnCreateAccount);
        btnSignIn               = (Button) findViewById(R.id.btnSignIn);
        btnSignInGoogle         = (SignInButton) findViewById(R.id.btnSignInGoogle);
        btnSignInFacebook       = (LoginButton) findViewById(R.id.btnSignInFacebook);
        edtEmail                = (EditText) findViewById(R.id.edtEmail);
        edtPassword             = (EditText) findViewById(R.id.edtPassword);
        initialize();
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(edtEmail.getText().toString(), edtPassword.getText().toString());
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(edtEmail.getText().toString(), edtPassword.getText().toString());
            }
        });

        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(i,SIGN_IN_GOOGLE_CODE );

            }
        });



        btnSignInFacebook.setReadPermissions(Arrays.asList("email"));
        btnSignInFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.w(TAG, "Facebook Login Succes: "+ loginResult.getAccessToken().getToken());
                signInFacebookFirebase(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.w(TAG, "Facebook Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.w(TAG, "Facebook Error");
                error.printStackTrace();
            }
        });

    }

    public void initialize(){
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                    Log.w(TAG, "onAuthStateChanged - signed_in"+firebaseUser.getUid());
                    Log.w(TAG, "onAuthStateChanged - signed_in"+firebaseUser.getEmail());
                }else{
                    Log.w(TAG, "onAuthStateChanged - signed_out");
                }

            }
        };

        //Inicializacion de Google Acoount

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }


    private void createAccount(String email, String password){
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Create Account Success", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MainActivity.this, "Create Account Unsuccess", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void signIn(String email, String password){
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(MainActivity.this, "Authentication Success", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
                        startActivity(i);
                    }else{
                        Toast.makeText(MainActivity.this, "Authentication  Unsuccess", Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void signInGoogleFirebase(GoogleSignInResult googleSignInResult){
            if(googleSignInResult.isSuccess()){
                AuthCredential authCredential =
                        GoogleAuthProvider.getCredential(googleSignInResult.getSignInAccount().getIdToken(), null);
                firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Google Authentication Success", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(MainActivity.this, "Google Authentication  Unsuccess", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                Toast.makeText(MainActivity.this, "Google Sign In Authentication  Unsuccess", Toast.LENGTH_LONG).show();
            }
    }


    private void signInFacebookFirebase(AccessToken accessToken){
        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Facebook Authentication Success", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(MainActivity.this, "Facebook Authentication  Unsuccess", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_GOOGLE_CODE){
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            signInGoogleFirebase(googleSignInResult);
        }else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        
    }
}
