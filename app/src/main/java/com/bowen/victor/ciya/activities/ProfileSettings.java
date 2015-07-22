package com.bowen.victor.ciya.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.tools.WorkAround;
import com.parse.ParseUser;

/**
 * Created by Victor on 7/21/2015.
 */
public class ProfileSettings extends Activity {
    TextView fullNameTV, usernameTV;
    EditText firstNameET, lastNameET, phoneNumET, emailET;
    String username, emailAddr;
    ParseUser parseUser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_profile_page);
        WorkAround.setNotificationBarColor(this, R.color.colorPrimaryDark);
        initialize();

    }

    public void initialize(){
        firstNameET = (EditText) findViewById(R.id.firstNameEdit);
        lastNameET = (EditText) findViewById(R.id.lastNameEdit);

        fullNameTV = (TextView) findViewById(R.id.fullname_display);
        usernameTV = (TextView) findViewById(R.id.username_display);

        emailET = (EditText) findViewById(R.id.emailEditText);

        username = parseUser.getUsername();
        emailAddr = parseUser.getEmail();

        usernameTV.setText(username);
        emailET.setText(emailAddr);


    }
}
