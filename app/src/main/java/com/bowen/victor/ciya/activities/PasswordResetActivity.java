package com.bowen.victor.ciya.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bowen.victor.ciya.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

/**
 * Activity which displays a login screen to the user.
 */
public class PasswordResetActivity extends Activity {
    // UI references.
    private EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_passwordreset);

        // Set up the signup form.
        email = (EditText) findViewById(R.id.emailTxt);

        // Set up the submit button click handler
        Button mActionButton = (Button) findViewById(R.id.action_button);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                reset();
            }
        });
    }

    private void reset() {

        String emailAddr = email.getText().toString().trim();
        ParseUser.requestPasswordResetInBackground(emailAddr, new RequestPasswordResetCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // An email was successfully sent with reset instructions.
                } else {
                    // Something went wrong. Look at the ParseException to see what's up.
                }
            }
        });
    }
}
