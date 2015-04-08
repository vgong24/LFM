package com.example.victor.lfm;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
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
 * Created by Victor on 4/6/2015.
 */
public class CreateTab extends Fragment /*implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/{
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

    View view;
    public CreateTab (Context context){
        this.context = context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.create_tab, container, false);
        initialize();
        //Toast.makeText(this.getActivity(), "Created View", Toast.LENGTH_SHORT).show();

        return view;
    }

    public void initialize(){

            initField();
            initCategories();
            fillCategorySpinner();
            initClickListeners();


    }
/*
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            initialize();
        }else{
            Log.d("CreateTab", "Fragment is not visible");
        }

    }
*/
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


/*
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
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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

    */

}
