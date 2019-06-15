package com.project.travel.story.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.project.travel.story.R;

public class DialogUtility {

    public void editTextDialog(final Context context, String title, String hint, final String errorText,
                               final TextView listItem, int maxLines, int maxLength, boolean isMultiLine){
        final AlertDialog builder = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_description, null);
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        final TextInputEditText editText = dialogView.findViewById(R.id.editText_dialog_description);
        TextInputLayout textInputLayoutDialog = dialogView.findViewById(R.id.textInputLayoutDialog);
        setMaxLines(editText, maxLines);
        if (isMultiLine) editText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        Button buttonOK = dialogView.findViewById(R.id.buttonOK);
        Button buttonClose = dialogView.findViewById(R.id.buttonClose);
        InputValidation validation = new InputValidation(context);

        titleTextView.setText(title);
        editText.setHint(hint);
        textInputLayoutDialog.setHint(hint);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.dismiss();
            }
        });
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validation.isInputEditTextFilled(editText, textInputLayoutDialog, errorText)){
                    listItem.setText(editText.getText());
                    builder.dismiss();
                }
            }
        });

        builder.setView(dialogView);
        builder.show();
    }

    public void signOutDialog(final Context context, final Activity activity){
        final AlertDialog builder = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_yes_no, null);
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        Button buttonOK = dialogView.findViewById(R.id.buttonOK);
        Button buttonClose = dialogView.findViewById(R.id.buttonClose);
        titleTextView.setText(R.string.sign_out_dialog);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.dismiss();
            }
        });
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
                mGoogleSignInClient.signOut();
                FirebaseAuth.getInstance().signOut();
                activity.finishAffinity();
                builder.dismiss();
            }
        });

        builder.setView(dialogView);
        builder.show();
    }

    private void setMaxLines(EditText editText, int maxLines){
        editText.setMaxLines(maxLines);
        editText.addTextChangedListener(new EditTextLinesLimiter(editText, maxLines));
    }

}
