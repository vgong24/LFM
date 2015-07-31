package com.bowen.victor.ciya.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bowen.victor.ciya.R;

/**
 * Created by Victor on 7/28/2015.
 */
public class AboutActivity extends ActionBarActivity {
    Toolbar toolbar;
    ActionBar ab;
    TextView versionNumber;
    LinearLayout licenses;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        initToolBar();
        initFields();
        initOnclick();
    }

    public void initToolBar(){
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("About");

    }

    public void initFields(){
        versionNumber = (TextView) findViewById(R.id.version_number);
        versionNumber.setText("Version " + getResources().getString(R.string.versionNumber));
        licenses = (LinearLayout) findViewById(R.id.license_layout);
    }

    public void initOnclick(){
        licenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LicensesActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return false;
    }

}
