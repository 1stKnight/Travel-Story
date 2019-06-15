package com.project.travel.story.Activities;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.project.travel.story.Utility.InputValidation;
import com.project.travel.story.R;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @BindView(R.id.textInputLayoutEmail)
    TextInputLayout textInputLayoutEmail;
    @BindView(R.id.textInputLayoutPassword)
    TextInputLayout textInputLayoutPassword;
    @BindView(R.id.editTextEmail)
    TextInputEditText editTextEmail;
    @BindView(R.id.editTextPassword)
    TextInputEditText editTextPassword;

    @BindView(R.id.sign_in_google)
    SignInButton googleSignInBtn;

    @BindView(R.id.progress_bar_layout)
    RelativeLayout progress;
    @BindView(R.id.progress_bar_text)
    TextView progressText;

    InputValidation validation;

    GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        ButterKnife.bind(this);
        progressText.setText(getResources().getString(R.string.signing_in));
        progress.setVisibility(View.GONE);

        validation = new InputValidation(this);

        googleSignInBtn.setColorScheme(SignInButton.COLOR_DARK);

        googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
       /* FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            Toast.makeText(SignInActivity.this, "Authentication success.",
                    Toast.LENGTH_SHORT).show();
            Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intentMain);
            finish();
        }*/
    }

    public void registerLink(View view){
        Intent intentRegister = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intentRegister);
    }

    public void signIn(View view){
        if(validateFields()){
            progress.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progress.setVisibility(View.GONE);//
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(SignInActivity.this, "Authentication success.",
                                        Toast.LENGTH_SHORT).show();
                                startMainActivity();
                            } else {
                                progress.setVisibility(View.GONE);
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(SignInActivity.this, getString(R.string.error_user_exists)
                                            , Toast.LENGTH_SHORT).show();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    Toast.makeText(SignInActivity.this, getString(R.string.error_message_password)
                                            , Toast.LENGTH_SHORT).show();

                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(SignInActivity.this, getString(R.string.error_message_password)
                                            , Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(SignInActivity.this, "Authentication failed: " + task.getException().getMessage()
                                            , Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    public void googleSignIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this,"Google SignIn dismissed error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Authentication success.", Toast.LENGTH_SHORT).show();
                            startMainActivity();
                        } else {
                            Toast.makeText(SignInActivity.this, "Authentication Fail", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startMainActivity(){
        Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intentMain);
        finish();
    }

    public void resetLink(View view){
        Intent intentReset = new Intent(getApplicationContext(), ResetPasswordActivity.class);
        startActivity(intentReset);
    }

    private boolean validateFields(){
        boolean isValid = true;
        if(!validation.isInputEditTextFilled(editTextEmail, textInputLayoutEmail,
                getString(R.string.error_message_email))) isValid = false;
        else if(!validation.isInputEditTextEmail(editTextEmail, textInputLayoutEmail,
                getString(R.string.error_message_email))) isValid = false;
        if(!validation.isInputEditTextFilled(editTextPassword, textInputLayoutPassword,
                getString(R.string.error_message_password))) isValid = false;
        return isValid;
    }

}
