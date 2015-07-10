package com.bowen.victor.ciya.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.services.MessageServiceV2;
import com.parse.LogInCallback;
import com.parse.ParseUser;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity_v2 extends Activity {

    private Button signUpButton;
    private Button loginButton;
    private EditText usernameField;
    private EditText passwordField;
    private String username;
    private String password;
    private Intent intent;
    private Intent serviceIntent;

    //TODO: Set up User's name when creating new profile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = new Intent(getApplicationContext(), MainActivity_v2.class);
        serviceIntent = new Intent(getApplicationContext(), MessageServiceV2.class);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            //startService(serviceIntent);
            startActivity(intent);
        }

        setContentView(R.layout.activity_login_v2);

        loginButton = (Button) findViewById(R.id.loginButton);
        signUpButton = (Button) findViewById(R.id.signupButton);
        usernameField = (EditText) findViewById(R.id.loginUsername);
        passwordField = (EditText) findViewById(R.id.loginPassword);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = usernameField.getText().toString();
                password = passwordField.getText().toString();

                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    public void done(ParseUser user, com.parse.ParseException e) {
                        if (user != null) {
                            //startService(serviceIntent);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Wrong username/password combo",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(LoginActivity_v2.this, SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onDestroy() {

        //stopService(new Intent(this, MessageService.class));
        super.onDestroy();
    }
}