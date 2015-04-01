package com.example.victor.lfm;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;

import android.widget.*;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.*;

import java.text.*;
import java.util.Calendar;
import java.util.*;

import com.google.android.gms.common.api.*;
import com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.common.*;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.*;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.SupportMapFragment;

import android.location.*;
import android.graphics.*;


public class MainActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener{
    //In create tab or search
    Button logOut = null;
    private static final int TIME_DIALOG_ID = 0;


    //In home tab
    Home_TAB hometab;
    Create_TAB createtab;


    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * Initialize Fields Under "Home" tab
         */

        initTabs();
        initFields();


        logOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                Intent intent = new Intent(v.getContext(), DispatchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        buildGoogleApiClient();


        //searchEvents(null, "Study");
        //Toast.makeText(getApplicationContext(), searchCategories.get(0).getName() + "", Toast.LENGTH_SHORT).show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        //Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        //Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        /*
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            loc = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude() );
        } else {
            Toast.makeText(getApplicationContext(), "No location detected onConnected", Toast.LENGTH_SHORT).show();
        }
        */
    }


    //Profile page tab
    public void initFields() {
        logOut = (Button) findViewById(R.id.logout_btn);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(item.getItemId()){
            case R.id.action_reload:
                hometab.fillEventList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //Intetion is to create all tabs in different classes
    private void initTabs() {
        final TabHost tabhost = (TabHost) findViewById(R.id.tabHost);
        tabhost.setup();

        hometab = new Home_TAB(tabhost, MainActivity.this);
        hometab.initialize();

        TabHost.TabSpec tabSpec;

        tabSpec = tabhost.newTabSpec("search");
        tabSpec.setContent(R.id.searchTab);
        tabSpec.setIndicator("Search");
        tabhost.addTab(tabSpec);


        createtab = new Create_TAB(tabhost, MainActivity.this, mGoogleApiClient);
        createtab.initialize();


        tabSpec = tabhost.newTabSpec("profile");
        tabSpec.setContent(R.id.profileTab);
        tabSpec.setIndicator("Profile");
        tabhost.addTab(tabSpec);

        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                int selectedTab = tabhost.getCurrentTab();
                switch(selectedTab){
                    case 0:
                        getSupportActionBar().setTitle("Up coming Events");
                        break;
                    case 1:
                        getSupportActionBar().setTitle("Search");
                        break;
                    case 2:
                        getSupportActionBar().setTitle("Create Event");
                        break;
                    default:
                        getSupportActionBar().setTitle("UNSET TITLE");
                        break;

                }
            }
        });
    }

    /*
    private void testSearchList(ArrayList<Events> ev) {
        eventListAdapter = new EventListAdapter(MainActivity.this, R.layout.event_list_view, ev);
        eventListView = (ListView) findViewById(R.id.listView2);

        eventListView.setAdapter(eventListAdapter);
    }*/

    public void eventSearch(View view) {
        EditText temp;

        //Activity
        temp = (EditText) findViewById(R.id.editText3);
        String activity = temp.getText().toString();

        /**
        //Location
        temp = (EditText) findViewById(R.id.editText4);
        String location = temp.getText().toString(); */


        //Toast.makeText(getApplicationContext(), searchEvents(null, "Sports").size() + "", Toast.LENGTH_SHORT).show();
        //searchEvents(null, activity);
        //testSearchList(ev);

        //ev = new ArrayList<>();

    }
/*
    public void searchEvents(Date d, String category) {
        Events e = new Events();
        date = d;
        cater = category;

        ParseQuery<Events> query = e.getQuery();
        query.addAscendingOrder("Date");
        query.findInBackground(new FindCallback<Events>() {

            public void done(List<Events> event, ParseException e) {

                if (e == null) {
                    for (int i = 0; i < event.size(); i++) {
                        //event.get(i).fetchIfNeeded();
                        //Toast.makeText(getApplicationContext(), event.get(i).getCat().getName() + "", Toast.LENGTH_SHORT).show();
                        if(date != null) {
                            if(date.equals(event.get(i).getDate())) {
                                ev.add(event.get(i));
                            }
                        }
                        else if(cater != null) {
                            if (cater.equals(event.get(i).getCat().getName())) {
                                ev.add(event.get(i));


                            }
                        }
                    }

                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }

            }

        });

    }*/


}
