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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
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
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
    Context context;
    Activity activity;

    Events createEvent;

    String eventId;
    String eventInfo;
    Date eventDate;
    double eventLat;
    double eventLng;

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
    Location myLocation;
    LatLng centerOfMap;
    private final int ZOOM_DISTANCE = 13;


    Spinner categorySpin;
    String selectedCategory, cater;

    AutoCompleteTextView autoCompView;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    Button createEventBtn, timeBtn, dateBtn;

    ArrayAdapter<PlaceDetails> autoAdapter;

    PlacesAPI placesAPI;

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
        return view;
    }

    //MAP SECTION ==========================================================================
    /*
    Google Maps stuff
    Display the first map location the user sees when creating a new event.
    Should be able to move around while keeping the marker centered (static).
     */
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

    //Set up Map fragment
    private void setUpMap(){
        //Toast.makeText(context.getApplicationContext(), "Setting up map", Toast.LENGTH_SHORT).show();
        mMap.setMyLocationEnabled(true);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria, true);
        myLocation = locationManager.getLastKnownLocation(provider);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if(myLocation == null){
            buildAlertMessageNoGps();
            return;
        }
        double latitude = myLocation.getLatitude();
        double longitude = myLocation.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_DISTANCE));
        //centerMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("You are here").snippet("Consider yourself located"));
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                //Gets coordinates of center
                centerOfMap = mMap.getCameraPosition().target;
                //centerMarker.setPosition(centerOfMap);

            }
        });
    }

    @Override
    public void onMapReady() {
        setUpMapIfNeeded();

    }

    //GPS =========================================================================================
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



    //Bottom half fill in section ============================================================================

    public void initialize(){
        setUpMapIfNeeded();

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

        if(catNames == null){
            catNames = new ArrayList<String>();
        }else{
            catNames.clear();
        }

        searchCategories = new ArrayList<Category>();
        ev = new ArrayList<Events>();
        categoryArray = new ArrayList<>();
        cEventDateTime = Calendar.getInstance();

        autoCompView = (AutoCompleteTextView) view.findViewById(R.id.create_auto_complete);
        autoCompView.setThreshold(0);

        autoAdapter = new GooglePlacesAutoCompleteAdapter(context, R.layout.list_item);
        autoCompView.setAdapter(autoAdapter);

        placesAPI = new PlacesAPI();


    }

    public void initClickListeners(){
        //After selecting auto complete item, map will zoom into that location
        autoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlaceDetails placeItem = (PlaceDetails)parent.getItemAtPosition(position);
                String placeId = placeItem.getId();

                Log.v("ID", "Searching: " + placeId);
                setUpMapIfNeeded();
                //Repositions marker to selected location in async thread
                placesAPI.getLatLngByID(mMap, placeId);

                autoCompView.setText(placeItem.getName().toString());
            }
        });

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
                if (categoryArray.size() != 0)
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
                if (e == null) {
                    for (int i = 0; i < categories.size(); i++) {
                        categoryArray.add(categories.get(i));
                    }
                    fillCategorySpinner();
                }
            }
        });
    }

    //Fill spinner with values from parse
    private void fillCategorySpinner(){

        for( int i = 0 ; i < categoryArray.size(); i++){
            Category categoryType = (Category) categoryArray.get(i);
            String categoryName = categoryType.getName();
            catNames.add(categoryName);

        }

        ArrayAdapter<String> adapt = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, catNames);
        categorySpin.setAdapter(adapt);
        categorySpin.setSelection(1);

        fillPublicPrivateSpinner();

    }

    //Fill spinner public or private
    private void fillPublicPrivateSpinner(){
        Spinner spinner = (Spinner) view.findViewById(R.id.cTabPPSpin);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.public_private, android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }

    /**
     * Creates the event and saves it in parse db.
     * Once saved, create a new Intent that redirects the user to EventDetails
     * @param v
     */
    public void createEvent(View v) {

        EditText temp;
        temp = (EditText) view.findViewById(R.id.cTabMemberEdit);
        if(temp.getText().toString().equalsIgnoreCase("")){
            temp.setText("0");
        }

        int maxMember = Integer.parseInt(temp.getText().toString());

        temp = (EditText) view.findViewById(R.id.cTabDescEdit);
        eventInfo = temp.getText().toString();


        String category = selectedCategory;
        if(createEvent == null){
            createEvent = (Events) ParseObject.create("Events");

        }

        createEvent.put("Max", maxMember);
        createEvent.put("Description", eventInfo);
        createEvent.put("Host", ParseUser.getCurrentUser());
        //test
        cEventDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        eventDate = cEventDateTime.getTime();
        createEvent.put("Date", eventDate);


        //get id from category
        //replace id with category id
        String categoryID = getCategoryID(category);
        createEvent.put("Category", ParseObject.createWithoutData("Category", categoryID));
        ParseGeoPoint point = new ParseGeoPoint(centerOfMap.latitude, centerOfMap.longitude);
        createEvent.put("Location", point);
        eventLat = point.getLatitude();
        eventLng = point.getLongitude();

        createEvent.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                eventId = createEvent.getObjectId();
                startEventDetailActivity();
            }
        });

        //Adds the host as an attendee of the created event
        Attendee attend = new Attendee();
        attend.setEvent((Events)createEvent);
        attend.setUser(ParseUser.getCurrentUser().getObjectId());
        attend.saveInBackground();
        String eventTime = cEventDateTime.getTime() + "";
        Toast.makeText(context.getApplicationContext(), "Event Time: " + eventTime, Toast.LENGTH_SHORT).show();
    }

    //Should have one static method that can be called from HomeTab as well to reduce redundancy
    public void startEventDetailActivity(){
        //Start Event Details activity
        //Pass field values into intent extras to save time in displaying information
        Intent i = new Intent(context.getApplicationContext(), EventDetails.class);

        i.putExtra("EventId", eventId);
        i.putExtra("EventDate", eventDate.getTime());
        i.putExtra("EventTitle",eventInfo);
        i.putExtra("EventLat", eventLat);
        i.putExtra("EventLong", eventLng);
        context.startActivity(i);
        activity.finish();
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
