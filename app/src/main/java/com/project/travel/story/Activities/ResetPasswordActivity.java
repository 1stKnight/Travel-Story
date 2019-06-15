package com.project.travel.story.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.project.travel.story.R;
import com.project.travel.story.Utility.InputValidation;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResetPasswordActivity extends AppCompatActivity {


    @BindView(R.id.textInputLayoutEmail)
    TextInputLayout textInputLayoutEmail;
    @BindView(R.id.editTextEmail)
    TextInputEditText editTextEmail;
    @BindView(R.id.progress_bar_layout)
    RelativeLayout progress;
    @BindView(R.id.progress_bar_text)
    TextView progressText;

    InputValidation validation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);

        progress.setVisibility(View.GONE);
        progressText.setText(getResources().getString(R.string.resetting_password));
        validation = new InputValidation(this);
    }


    public void resetPassword(View view){
        if(validateFields()){
            FirebaseAuth auth = FirebaseAuth.getInstance();
            progress.setVisibility(View.VISIBLE);
            auth.sendPasswordResetEmail(editTextEmail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progress.setVisibility(View.GONE);
                                Toast.makeText(ResetPasswordActivity.this, "Reset email sent. \n Check your mailbox",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            } else{
                                progress.setVisibility(View.GONE);
                                Toast.makeText(ResetPasswordActivity.this, "Can't send email.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void backToLogin(View view){
     finish();
    }

    private boolean validateFields(){
        boolean isValid = true;
        if(!validation.isInputEditTextFilled(editTextEmail, textInputLayoutEmail,
                getString(R.string.error_message_email))) isValid = false;
        else if(!validation.isInputEditTextEmail(editTextEmail, textInputLayoutEmail,
                getString(R.string.error_message_email))) isValid = false;
        return isValid;
    }
}
