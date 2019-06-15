package com.project.travel.story.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.project.travel.story.R;
import com.project.travel.story.Utility.InputValidation;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChangePasswordActivity extends AppCompatActivity {

    @BindView(R.id.textInputLayoutPassword)
    TextInputLayout textInputLayoutPassword;
    @BindView(R.id.textInputLayoutNewPassword)
    TextInputLayout textInputLayoutNewPassword;
    @BindView(R.id.editTextPassword)
    TextInputEditText editTextPassword;
    @BindView(R.id.editTextNewPassword)
    TextInputEditText editTextNewPassword;

    @BindView(R.id.textViewEmail)
    AppCompatTextView textViewEmail;

    @BindView(R.id.progress_bar_layout)
    RelativeLayout progress;
    @BindView(R.id.progress_bar_text)
    TextView progressText;

    FirebaseUser user;
    String email;

    InputValidation validation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);

        validation = new InputValidation(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();

        textViewEmail.setText(getResources().getString(R.string.your_email) + " " + email);

        progressText.setText(getResources().getString(R.string.changing_password));
        progress.setVisibility(View.GONE);

    }

    public void changePassword(View view) {
        if(validateFields()) {
            progress.setVisibility(View.VISIBLE);
            AuthCredential credential = EmailAuthProvider.getCredential(email,editTextPassword.getText().toString());
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //auth success
                        user.updatePassword(editTextNewPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    //change success
                                    progress.setVisibility(View.GONE);
                                    Toast.makeText(ChangePasswordActivity.this, getString(R.string.password_changed)
                                            , Toast.LENGTH_SHORT).show();
                                    finish();
                                }else {
                                    progress.setVisibility(View.GONE);
                                    try{
                                        throw task.getException();
                                    }catch (FirebaseAuthWeakPasswordException e) {
                                        Toast.makeText(ChangePasswordActivity.this, getString(R.string.error_weak_password)
                                                , Toast.LENGTH_SHORT).show();
                                    }catch (FirebaseAuthInvalidCredentialsException e) {
                                        Toast.makeText(ChangePasswordActivity.this, getString(R.string.error_wrong_password)
                                                , Toast.LENGTH_SHORT).show();
                                    }catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(ChangePasswordActivity.this, "Authentication failed: " + task.getException().getMessage()
                                                , Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }else {
                        progress.setVisibility(View.GONE);
                        //auth failed
                       try{
                           throw task.getException();
                       } catch (FirebaseAuthUserCollisionException e) {
                           Toast.makeText(ChangePasswordActivity.this, getString(R.string.error_user_exists)
                                   , Toast.LENGTH_SHORT).show();
                       }catch (FirebaseAuthWeakPasswordException e) {
                           Toast.makeText(ChangePasswordActivity.this, getString(R.string.error_weak_password)
                                   , Toast.LENGTH_SHORT).show();

                       }catch (FirebaseAuthInvalidCredentialsException e) {
                           Toast.makeText(ChangePasswordActivity.this, getString(R.string.error_wrong_password)
                                   , Toast.LENGTH_SHORT).show();
                       }catch (Exception e) {
                           e.printStackTrace();
                           Toast.makeText(ChangePasswordActivity.this, "Authentication failed: " + task.getException().getMessage()
                                   , Toast.LENGTH_SHORT).show();
                       }
                    }
                }
            });
        }
    }

    public void backToSettings(View view) {
        finish();
    }

    private boolean validateFields(){
        boolean isValid = true;
        if(!validation.isInputEditTextFilled(editTextPassword, textInputLayoutPassword,
                getString(R.string.error_message_password))) isValid = false;
        if(!validation.isInputEditTextFilled(editTextNewPassword, textInputLayoutNewPassword,
                getString(R.string.error_message_password))) isValid = false;
        return isValid;
    }
}
