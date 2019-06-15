package com.project.travel.story.Activities;

import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatTextView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.project.travel.story.Utility.InputValidation;
import com.project.travel.story.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    @BindView(R.id.textInputLayoutPubName)
    TextInputLayout textInputLayoutPubName;
    @BindView(R.id.textInputLayoutEmail)
    TextInputLayout textInputLayoutEmail;
    @BindView(R.id.textInputLayoutPassword)
    TextInputLayout textInputLayoutPassword;
    @BindView(R.id.textInputLayoutConfirmPassword)
    TextInputLayout textInputLayoutConfirmPassword;

    @BindView(R.id.editTextPubName)
    TextInputEditText editTextPubName;
    @BindView(R.id.editTextEmail)
    TextInputEditText editTextEmail;
    @BindView(R.id.editTextPassword)
    TextInputEditText editTextPassword;
    @BindView(R.id.editTextConfirmPassword)
    TextInputEditText editTextConfirmPassword;

    @BindView(R.id.progress_bar_layout)
    RelativeLayout progress;
    @BindView(R.id.progress_bar_text)
    TextView progressText;

    final Calendar myCalendar = Calendar.getInstance();

    InputValidation validation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        ButterKnife.bind(this);

        validation = new InputValidation(this);

        progressText.setText(getResources().getString(R.string.signing_up));
        progress.setVisibility(View.GONE);
    }

    public void registration (View view){
        if(validateFields()){
            progress.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                progress.setVisibility(View.GONE);
                                Toast.makeText(SignUpActivity.this, "Registration success.", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                progress.setVisibility(View.GONE);
                                try{
                                    throw task.getException();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.error_user_exists)
                                            , Toast.LENGTH_SHORT).show();
                                } catch (FirebaseAuthInvalidUserException e) {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.error_invalid_email)
                                            , Toast.LENGTH_SHORT).show();

                                }catch (FirebaseAuthWeakPasswordException e) {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.error_weak_password)
                                            , Toast.LENGTH_SHORT).show();

                                }catch (FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.error_wrong_password)
                                            , Toast.LENGTH_SHORT).show();
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage()
                                            , Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    public void loginLink(View view){
        finish();
    }

    private boolean validateFields(){
        boolean isValid = true;
        if(!validation.isInputEditTextFilled(editTextPubName, textInputLayoutPubName,
                getString(R.string.error_message_nickname))) {
            isValid = false;
        }
        else if(!validation.isInputEditTextСontainsSpecialSymbols(editTextPubName,
                    textInputLayoutPubName, getString(R.string.error_message_nickname))) isValid = false;
        if(!validation.isInputEditTextFilled(editTextEmail, textInputLayoutEmail,
                getString(R.string.error_message_email))) isValid = false;
        else if(!validation.isInputEditTextEmail(editTextEmail, textInputLayoutEmail,
                getString(R.string.error_message_email))) isValid = false;
        if(!validation.isInputEditTextFilled(editTextPassword, textInputLayoutPassword,
                getString(R.string.error_message_password))) isValid = false;
        if(!validation.isInputEditTextFilled(editTextConfirmPassword, textInputLayoutConfirmPassword,
                getString(R.string.error_message_confirm_password))) isValid = false;
        else if(!validation.isInputEditTextMatches(editTextPassword, editTextConfirmPassword,
                textInputLayoutConfirmPassword, getString(R.string.error_password_match))) isValid = false;
        return isValid;
    }
}
