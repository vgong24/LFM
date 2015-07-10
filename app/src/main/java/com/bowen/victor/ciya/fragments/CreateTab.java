package com.bowen.victor.ciya.fragments;

import android.app.Activity;
import android.app.DialogFragment;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bowen.victor.ciya.activities.EventDetails;
import com.bowen.victor.ciya.tools.GPSTracker;
import com.bowen.victor.ciya.activities.MainActivity_v2;
import com.bowen.victor.ciya.structures.PlaceDetails;
import com.bowen.victor.ciya.tools.PlacesAPI;
import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.adapters.GooglePlacesAutoCompleteAdapter;
import com.bowen.victor.ciya.structures.Attendee;
import com.bowen.victor.ciya.structures.Category;
import com.bowen.victor.ciya.structures.Events;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
    TextView timeView, dateView;
    EditText cMembers, cDescription;
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

    GPSTracker gpsTracker;


    Spinner categorySpin;
    String selectedCategory, cater;

    AutoCompleteTextView autoCompView;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    Button createEventBtn, timeBtn, dateBtn;

    ArrayAdapter<PlaceDetails> autoAdapter;

    PlacesAPI placesAPI;

    LocationListener locationListener;

    Marker centerMarker;

    //Setup map
    LocationManager locationManager;
    Criteria criteria;
    String currentUserId;

    View view;
    Bundle bundle;

    public static CreateTab newInstance(Context context, Bundle bundle){
        CreateTab createTab = new CreateTab();
        createTab.context = context;
        createTab.activity = (Activity) context;
        createTab.bundle = bundle;
        return createTab;
    }
    public static CreateTab newInstance(Context context){
        CreateTab createTab = new CreateTab();
        createTab.context = context;
        createTab.activity = (Activity) context;
        return createTab;
    }

    public CreateTab(){

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
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        double latitude;
        double longitude;
        //Use gpstracker to get location
        if(gpsTracker.canGetLocation()){
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
        }else{
            gpsTracker.showSettingsAlert();
            latitude = 21.3000;
            longitude = -157.8167;
        }


        LatLng latLng = new LatLng(latitude, longitude);
        centerOfMap = latLng;

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


    //Bottom half fill in section ============================================================================

    public void initialize(){
        //Init location
        gpsTracker = new GPSTracker(context);
        setUpMapIfNeeded();

        initField();
        initCategories();
        fillCategorySpinner();
        initClickListeners();


    }

    public void initField(){
        currentUserId = ParseUser.getCurrentUser().getObjectId();

        timeView = (TextView) view.findViewById(R.id.cTabTimeView);
        cMembers = (EditText) view.findViewById(R.id.cTabMemberEdit);
        cDescription = (EditText) view.findViewById(R.id.cTabDescEdit);

        createEventBtn = (Button) view.findViewById(R.id.cTabCreateBtn);
        createEventBtn.setEnabled(false);

        categorySpin = (Spinner) view.findViewById(R.id.cTabCatSpin);

        dateView = (TextView) view.findViewById(R.id.cTabDateView);

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
                PlaceDetails placeItem = (PlaceDetails) parent.getItemAtPosition(position);
                String placeId = placeItem.getId();

                Log.v("ID", "Searching: " + placeId);
                setUpMapIfNeeded();
                //Repositions marker to selected location in async thread
                placesAPI.getLatLngByID(mMap, placeId);

                autoCompView.setText(placeItem.getName().toString());
            }
        });

        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mytimepicker mtp = Mytimepicker.newInstance(timeView, cEventDateTime);
                mtp.setContext(context);
                DialogFragment newFragment = mtp;
                newFragment.show(getActivity().getFragmentManager(), "timePicker");
            }
        });

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = DatePickerFragment.newInstance(dateView, cEventDateTime);
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
            }
        });

        //Keep eventBtn disabled until description is filled
        cDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0){
                    createEventBtn.setEnabled(true);
                }else{
                    createEventBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
        categorySpin.setSelection(0);

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
                if (e == null) {
                    eventId = createEvent.getObjectId();
                    addAttendee();
                    //startEventDetailActivity();
                } else {
                    Toast.makeText(context, "Did not save successfully", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        });


    }

    public void  addAttendee(){
        //Adds the host as an attendee of the created event
        Attendee attend = new Attendee();
        attend.setEvent((Events)createEvent);
        attend.setUser(currentUserId);
        attend.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                startEventDetailActivity();
            }
        });
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
        i.putExtra("EventHost", ParseUser.getCurrentUser().getObjectId());
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
