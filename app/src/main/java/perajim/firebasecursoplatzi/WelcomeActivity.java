package perajim.firebasecursoplatzi;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class WelcomeActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private TextView tvUserDetail;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Button btnSignOut;
    public static final String TAG = "WelcomeActivity";
    private GoogleApiClient googleApiClient;

    private ImageView imvPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        tvUserDetail = (TextView) findViewById(R.id.tvUserDetail);
        btnSignOut = (Button) findViewById(R.id.btnSignOut);
        imvPhoto = (ImageView) findViewById(R.id.imvPhoto);
        initialize();
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }
    private  void signOut(){
        firebaseAuth.signOut();
            if(Auth.GoogleSignInApi !=  null) {
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(WelcomeActivity.this, "Error al salir", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        if(LoginManager.getInstance() != null){
            LoginManager.getInstance().logOut();
        }
    }
    public void initialize(){
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {


            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                   tvUserDetail.setText("IDUser "+firebaseUser.getUid() + "\n Email: "+firebaseUser.getEmail());
                    Picasso.with(WelcomeActivity.this).load(firebaseUser.getPhotoUrl()).into(imvPhoto);
                }else{
                    Log.w(TAG, "onAuthStateChanged - signed_out");
                }

            }
        };
        //Inicializacion de Google Acoount

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
