package com.example.victor.lfm;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Victor on 3/20/2015.
 */
public class Create_TAB implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    TabHost tabhost;
    Context context;
    Activity activity;
    TabHost.TabSpec tabSpec;

    Date date;
    TextView timeView, dateView, filterAddress;
    List<String> catNames;
    ArrayList<Date> dates, searchDates;
    ArrayList<Category> searchCategories;
    ArrayList<Events> ev;
    ArrayList<Category> categoryArray;

    GoogleMap map;
    Marker marker;
    Calendar cEventDateTime;
    LatLng loc;

    Spinner categorySpin;
    String selectedCategory, cater;
    private ParseQueryAdapter<ParseObject> mainAdapter;

    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    Button createEventBtn, timeBtn, dateBtn;

    public Create_TAB(TabHost tabhost, Context context, GoogleApiClient mGoogleApiClient){
        this.tabhost = tabhost;
        this.context = context;
        this.activity = (Activity) context;
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public void initialize(){
        initTab();
        initField();
        initCategories();
        fillCategorySpinner();
        initClickListeners();

    }

    public void initTab(){
        tabSpec = tabhost.newTabSpec("create");
        tabSpec.setContent(R.id.createTab);
        tabSpec.setIndicator("Create");
        tabhost.addTab(tabSpec);
    }

    public void initField(){
        buildGoogleApiClient();

        timeView = (TextView) activity.findViewById(R.id.timeView);
        timeBtn = (Button) activity.findViewById(R.id.timeBtn);
        createEventBtn = (Button) activity.findViewById(R.id.create_button);
        categorySpin = (Spinner) activity.findViewById(R.id.category_spinner);

        dateView = (TextView) activity.findViewById(R.id.dateView);
        dateBtn = (Button) activity.findViewById(R.id.dateBtn);

        dates = new ArrayList<Date>();
        searchDates = new ArrayList<Date>();

        catNames = new ArrayList<String>();
        searchCategories = new ArrayList<Category>();
        ev = new ArrayList<Events>();
        categoryArray = new ArrayList<>();
        cEventDateTime = Calendar.getInstance();

        ArrayAdapter<String> adapt = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, catNames);

        MapFragment mapFrag= (MapFragment)activity.getFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
        map = mapFrag.getMap();
        filterAddress = (TextView) activity.findViewById(R.id.addressText);
    }

    public void initClickListeners(){
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mytimepicker mtp = new Mytimepicker(timeView, cEventDateTime);
                mtp.setContext(context);
                DialogFragment newFragment = mtp;
                newFragment.show(activity.getFragmentManager(), "timePicker");
            }
        });

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(dateView, cEventDateTime);
                newFragment.show(activity.getFragmentManager(), "datePicker");
            }
        });

        createEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEvent(v);

            }
        });

        categorySpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categoryArray.get(position).getName();

                //Toast.makeText(context.getApplicationContext(), selectedCategory, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    public void initCategories(){
        ParseQuery<Category> query = ParseQuery.getQuery("Category");
        query.findInBackground(new FindCallback<Category>() {
            @Override
            public void done(List<Category> categories, ParseException e) {
                if(e==null){
                    for(int i = 0 ; i < categories.size(); i++){
                        categoryArray.add(categories.get(i));
                    }
                }
            }
        });
    }

    //Fill spinner with values from parse
    private void fillCategorySpinner(){

        ParseQueryAdapter.QueryFactory<ParseObject> factory =
                new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    public ParseQuery create() {
                        ParseQuery query = new ParseQuery("Category");
                        return query;
                    }
                };

        ParseQueryAdapter<ParseObject> adapter = new ParseQueryAdapter<ParseObject>(context, factory);
        adapter.setTextKey("Name");
        categorySpin.setAdapter(adapter);
        categorySpin.setSelection(1);

    }


    public void createEvent(View view) {

        EditText temp;
        temp = (EditText) activity.findViewById(R.id.maxMembersInt);
        if(temp.getText().toString().equalsIgnoreCase("")){
            temp.setText("0");
        }

        int maxMember = Integer.parseInt(temp.getText().toString());

        temp = (EditText) activity.findViewById(R.id.createEventInfo);
        String eventInfo = temp.getText().toString();


        String category = selectedCategory;

        ParseObject createEvent = ParseObject.create("Events");

        createEvent.put("Max", maxMember);
        createEvent.put("Description", eventInfo);
        createEvent.put("Host", ParseUser.getCurrentUser());
        //test
        cEventDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        createEvent.put("Date", cEventDateTime.getTime());

        //get id from category
        //replace id with category id
        String categoryID = getCategoryID(category);
        createEvent.put("Category", ParseObject.createWithoutData("Category", categoryID));

        createEvent.saveInBackground();

        Attendee attend = new Attendee();
        attend.setEvent((Events)createEvent);
        attend.setUser(ParseUser.getCurrentUser().getObjectId());
        attend.saveInBackground();
        String eventTime = cEventDateTime.getTime() + "";
        Toast.makeText(context.getApplicationContext(), "Event Time: " + eventTime, Toast.LENGTH_SHORT).show();
    }

    private String getCategoryID(String catStr){
        String result = "";
        for(Category cat : categoryArray){
            if(cat.getName().equalsIgnoreCase(catStr)){
                result = cat.getObjectId().toString();
                return result;
            }
        }
        return result;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
/*
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
    */

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

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            loc = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude() );
        } else {
            Toast.makeText(activity.getApplicationContext(), "No location detected onConnected", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            loc = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude() );
        } else {
            Toast.makeText(activity.getApplicationContext(), "No location detected onMapReady", Toast.LENGTH_SHORT).show();
            loc = new LatLng(21.4513314,-158.0152807);
        }


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));

        map.addMarker(new MarkerOptions()
                .title("Event Location")
                .position(loc));

        filterAddress.setText("My Location");
    }
}
