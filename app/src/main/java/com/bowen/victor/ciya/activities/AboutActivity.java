package com.bowen.victor.ciya.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.bowen.victor.ciya.R;

/**
 * Created by Victor on 7/28/2015.
 */
public class AboutActivity extends ActionBarActivity {
    Toolbar toolbar;
    ActionBar ab;
    TextView versionNumber;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        initToolBar();
        initFields();
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
