package com.bowen.victor.ciya.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.adapters.GooglePlacesAutoCompleteAdapter;
import com.bowen.victor.ciya.fragments.CreateTab;
import com.bowen.victor.ciya.structures.Attendee;
import com.bowen.victor.ciya.structures.PlaceDetails;
import com.bowen.victor.ciya.tools.GPSTracker;
import com.bowen.victor.ciya.tools.WorkAround;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

/**
 * Created by Victor on 6/10/2015.
 */
public class CreateEvent extends ActionBarActivity implements GoogleApiClient.OnConnectionFailedListener {

    CreateTab createTab;
    FragmentTransaction transaction;
    Fragment fragment;
    ProgressBar progressBar;
    Toolbar toolbar;
    ActionBar actionBar;
    AutoCompleteTextView autoCompView;
    ArrayAdapter<PlaceDetails> autoAdapter;
    ImageView cancelBtn;
    GPSTracker tracker;
    LatLngBounds latLngBounds;
    private static boolean justCreated = false;

    protected GoogleApiClient mGoogleApiClient;


    public void setActionBarTitle(String title){
        actionBar.setTitle(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        WorkAround.setNotificationBarColor(this, R.color.colorPrimaryDark);
        tracker = new GPSTracker(this);
        latLngBounds = convertCenterAndRadiusToBounds(getLatLng(), 100);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();


        fragment = CreateTab.newInstance(CreateEvent.this);
        progressBar = (ProgressBar) findViewById(R.id.chatRoomProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        transaction = getSupportFragmentManager().beginTransaction();

        setUpActionBar();

        //setActionBarTitle("TEST");
        new SetUpBackground().execute();

    }

    public LatLng getLatLng(){
        if(tracker.canGetLocation()){
            double lat = tracker.getLatitude();
            double lon = tracker.getLongitude();
            tracker.stopUsingGPS();
            return new LatLng(lat, lon);
        }
        return null;
    }

    public LatLngBounds convertCenterAndRadiusToBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if(tracker.canGetLocation()){
            tracker.stopUsingGPS();
        }
        super.onStop();
    }

    public void setUpActionBar(){
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);


        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.search_bar, null);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        actionBar.setCustomView(v, layoutParams);

        cancelBtn = (ImageView)actionBar.getCustomView().findViewById(R.id.cancel);
        cancelBtn.setVisibility(View.GONE);
        autoCompView = (AutoCompleteTextView) actionBar.getCustomView().findViewById(R.id.autoCompleteSearch);
        autoCompView.setThreshold(0);

        //autoAdapter = new GooglePlacesAutoCompleteAdapter(this, R.layout.list_item);
        autoAdapter = new GooglePlacesAutoCompleteAdapter(this, R.layout.list_item, mGoogleApiClient, latLngBounds);

        autoCompView.setAdapter(autoAdapter);

    }

    public void initOnClickListener(){
        autoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlaceDetails placeItem = (PlaceDetails) parent.getItemAtPosition(position);
                callFragmentMethod(placeItem);

                autoCompView.setText(placeItem.getName().toString());
            }
        });

        autoCompView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    cancelBtn.setVisibility(View.VISIBLE);
                } else {
                    cancelBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCompView.setText("");
                cancelBtn.setVisibility(View.GONE);
            }
        });
        //check if fields were entered, then display the X button


    }
    //call fragment method example
    public void callFragmentMethod(PlaceDetails placeDetails){
        CreateTab fragment = (CreateTab)getSupportFragmentManager().findFragmentById(R.id.createFrame);
        fragment.relocatePinPoint(placeDetails);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("CreateEventConnection", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    private class SetUpBackground extends AsyncTask<Void, Void, Void> {
        ArrayList<Attendee> attendeesArr;

        @Override
        protected Void doInBackground(Void... params) {

            transaction.replace(R.id.createFrame, fragment);
            return null;
        }

        @Override
        protected void onPostExecute(Void none){
            progressBar.setVisibility(View.GONE);
            transaction.commit();
            initOnClickListener();
            if(tracker.canGetLocation()){
                tracker.stopUsingGPS();
            }
        }
    }



}
