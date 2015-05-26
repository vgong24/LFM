package com.example.victor.lfm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Victor on 4/6/2015.
 */
public class CreateTab extends Fragment implements CustomMapFragment.OnMapReadyListener{
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

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    Button createEventBtn, timeBtn, dateBtn;

    Marker centerMarker;

    //Setup map
    LocationManager locationManager;
    Criteria criteria;

    View view;
    public CreateTab (Context context){
        this.context = context;
        activity = (Activity) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.create_tab, container, false);
        initialize();

        //Create the map interface and replaces it with the default fragment placeholder in xml
        mMapFragment = CustomMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.map2, mMapFragment).commitAllowingStateLoss();

        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        if(mMap != null){
            setUpMap();
        }
        if(mMap == null){
            mMap = mMapFragment.getMap();
        }
        if(mMap != null){
            setUpMap();
        }
    }

    private void setUpMapIfNeeded() {
        if(mMapFragment == null){
            mMapFragment = CustomMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map2, mMapFragment).commitAllowingStateLoss();
        }
        if (mMap == null) {
            //mMap = ((SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map2)).getMap();
            mMap = mMapFragment.getMap();
        }
        if(mMap != null){
            setUpMap();
        }
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();

        if(!activity.isFinishing()) {
            if (mMap != null) {
                MainActivity_v2.fragmentManager.beginTransaction().remove(getChildFragmentManager().findFragmentById(R.id.map2)).commitAllowingStateLoss();
                mMap = null;
                mMapFragment = null;
            }
        }

    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        //Toast.makeText(context, "destroyed", Toast.LENGTH_SHORT).show();

    }

    //Set up Map fragment
    private void setUpMap(){
        //Toast.makeText(context.getApplicationContext(), "Setting up map", Toast.LENGTH_SHORT).show();
        mMap.setMyLocationEnabled(true);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria, true);
        Location myLocation = locationManager.getLastKnownLocation(provider);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if(myLocation == null){
            buildAlertMessageNoGps();
            return;
        }
        double latitude = myLocation.getLatitude();
        double longitude = myLocation.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        centerMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("You are here").snippet("Consider yourself located"));
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                //Gets coordinates of center
                LatLng centerOfMap = mMap.getCameraPosition().target;
                centerMarker.setPosition(centerOfMap);

            }
        });
    }
    //Asks User to turn on GPS if it is turned off
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /*
    Google Maps stuff
    Display the first map location the user sees when creating a new event.
    Should be able to move around while keeping the marker centered (static).
     */
    @Override
    public void onMapReady() {
       setUpMapIfNeeded();

    }


    public void initialize(){

            initField();
            initCategories();
            fillCategorySpinner();
            initClickListeners();


    }

    public void initField(){

            timeView = (TextView) view.findViewById(R.id.cTabTimeView);
            timeBtn = (Button) view.findViewById(R.id.cTabTimeBtn);
            createEventBtn = (Button) view.findViewById(R.id.cTabCreateBtn);
            categorySpin = (Spinner) view.findViewById(R.id.cTabCatSpin);

            dateView = (TextView) view.findViewById(R.id.cTabDateView);
            dateBtn = (Button) view.findViewById(R.id.cTabDateBtn);

            dates = new ArrayList<Date>();
            searchDates = new ArrayList<Date>();

            catNames = new ArrayList<String>();
            searchCategories = new ArrayList<Category>();
            ev = new ArrayList<Events>();
            categoryArray = new ArrayList<>();
            cEventDateTime = Calendar.getInstance();

            ArrayAdapter<String> adapt = new ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item, catNames);

        /*===================================================================
        MapFragment mapFrag= (MapFragment)activity.getFragmentManager().findFragmentById(R.id.map2);
        mapFrag.getMapAsync(this);
        map = mapFrag.getMap();

        */
            filterAddress = (TextView) view.findViewById(R.id.cTabMapAddrView);


    }

    public void initClickListeners(){
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mytimepicker mtp = new Mytimepicker(timeView, cEventDateTime);
                mtp.setContext(context);
                DialogFragment newFragment = mtp;
                newFragment.show(getActivity().getFragmentManager(), "timePicker");
            }
        });

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = DatePickerFragment.newInstance(dateView, cEventDateTime);
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
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
                if(categoryArray.size() != 0)
                    selectedCategory = categoryArray.get(position).getName();
                else
                    selectedCategory = "none";
                //IF OUTOFBOUNDSEXCEPTION: MOST LIKELY NOT CONNECTED IN TIME
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


    public void createEvent(View v) {

        EditText temp;
        temp = (EditText) view.findViewById(R.id.cTabMemberEdit);
        if(temp.getText().toString().equalsIgnoreCase("")){
            temp.setText("0");
        }

        int maxMember = Integer.parseInt(temp.getText().toString());

        temp = (EditText) view.findViewById(R.id.cTabDescEdit);
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



}
