package com.example.victor.lfm;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
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


public class MainActivity extends ActionBarActivity implements OnCameraChangeListener, OnMapReadyCallback,
        ConnectionCallbacks, OnConnectionFailedListener{
    //In create tab or search
    Button logOut, createEventBtn, timeBtn, dateBtn = null;
    private static final int TIME_DIALOG_ID = 0;
    TextView timeView, dateView, timeText, filterAddress;

    //In create tab
    GoogleMap map;
    Marker marker;
    Calendar cEventDateTime;
    LatLng loc;

    //for category spinner
    Spinner categorySpin;
    String selectedCategory, cater;
    private ParseQueryAdapter<ParseObject> mainAdapter;

    //In home tab
    ArrayList<Events> events;
    ArrayAdapter<Events> adapter;
    ArrayList<Category> categoryArray;
    EventListAdapter eventListAdapter;

    ListView eventListView;

    List<ParseObject> ob;
    Date date;

    List<String> catNames;
    ArrayList<Date> dates, searchDates;
    ArrayList<Category> searchCategories;
    ArrayList<Events> ev;

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
        fillEventList();
        initCategories();
        fillCategorySpinner();

        for (Events e: ev) {
            Toast.makeText(getApplicationContext(), e.getCat().getName(), Toast.LENGTH_SHORT).show();
            catNames.add(e.getCat().getName());
        }

        ArrayAdapter<String> adapt = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, catNames);


        /*
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, catNames);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        catNames.setAdapter(dataAdapter);
        catNames.setSelection(1);*/

        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new Mytimepicker(timeView, cEventDateTime);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });


        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(dateView, cEventDateTime);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });


        logOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                startActivity(new Intent(v.getContext(), DispatchActivity.class));
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

                Toast.makeText(getApplicationContext(), selectedCategory, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buildGoogleApiClient();
        MapFragment mapFrag=
                (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        map = mapFrag.getMap();
        //map.setMyLocationEnabled(true);

        filterAddress = (TextView) findViewById(R.id.addressText);
        //searchEvents(null, "Study");
        //Toast.makeText(getApplicationContext(), searchCategories.get(0).getName() + "", Toast.LENGTH_SHORT).show();
    }

    /** GRAB DATA FROM PARSE AND PUT THEM IN ARRAYLIST
     * SO YOU DONT HAVE TO CONSTANTLY REQUEST INFORMATION ALREADY GOTTEN
     *
     *
     */
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


    @Override
    public void onCameraChange(CameraPosition position) {

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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            loc = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude() );
        } else {
            Toast.makeText(getApplicationContext(), "No location detected onConnected", Toast.LENGTH_SHORT).show();
        }
    }


    public void onMapReady(GoogleMap map) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            loc = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude() );
        } else {
            Toast.makeText(getApplicationContext(), "No location detected onMapReady", Toast.LENGTH_SHORT).show();
            loc = new LatLng(21.4513314,-158.0152807);
        }


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));

        map.addMarker(new MarkerOptions()
                .title("Event Location")
                .position(loc));

        filterAddress.setText("My Location");
    }

    public void initFields() {
        timeView = (TextView) findViewById(R.id.timeView);
        timeBtn = (Button) findViewById(R.id.timeBtn);

        dateView = (TextView) findViewById(R.id.dateView);
        dateBtn = (Button) findViewById(R.id.dateBtn);

        logOut = (Button) findViewById(R.id.logout_btn);

        eventListView = (ListView) findViewById(R.id.listView);
        createEventBtn = (Button) findViewById(R.id.create_button);
        categorySpin = (Spinner) findViewById(R.id.category_spinner);

        dates = new ArrayList<Date>();
        searchDates = new ArrayList<Date>();

        events = new ArrayList<Events>();
        catNames = new ArrayList<String>();
        searchCategories = new ArrayList<Category>();
        ev = new ArrayList<Events>();
        categoryArray = new ArrayList<>();

        cEventDateTime = Calendar.getInstance();

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
                fillEventList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initTabs() {
        TabHost tabhost = (TabHost) findViewById(R.id.tabHost);
        tabhost.setup();
        TabHost.TabSpec tabSpec = tabhost.newTabSpec("home");
        tabSpec.setContent(R.id.homeTab);
        tabSpec.setIndicator("Home");
        tabhost.addTab(tabSpec);
        tabSpec = tabhost.newTabSpec("search");
        tabSpec.setContent(R.id.searchTab);
        tabSpec.setIndicator("Search");
        tabhost.addTab(tabSpec);
        tabSpec = tabhost.newTabSpec("create");
        tabSpec.setContent(R.id.createTab);
        tabSpec.setIndicator("Create");
        tabhost.addTab(tabSpec);
        tabSpec = tabhost.newTabSpec("profile");
        tabSpec.setContent(R.id.profileTab);
        tabSpec.setIndicator("Profile");
        tabhost.addTab(tabSpec);
    }
//================Picker Classes====================================================

    public static class Mytimepicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        TextView timeTxt;
        Calendar datetime;

        public Mytimepicker(TextView txtview, Calendar datetime) {
            timeTxt = txtview;
            this.datetime = datetime;
        }

        public Mytimepicker() {

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
            timeDialog.setTitle("Test");
            return timeDialog;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
// do something with the time chosen. http://stackoverflow.com/questions/2659954/timepickerdialog-and-am-or-pm/2660148#2660148
            String am_pm = "";
            Calendar datetime = Calendar.getInstance();
            datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            datetime.set(Calendar.MINUTE, minute);
            if (datetime.get(Calendar.AM_PM) == Calendar.AM)
                am_pm = "AM";
            else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
                am_pm = "PM";
            String strHrsToShow = (datetime.get(Calendar.HOUR) == 0) ? "12" : datetime.get(Calendar.HOUR) + "";
            timeTxt.setText(strHrsToShow + ":" + datetime.get(Calendar.MINUTE) + " " + am_pm);

        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        TextView dateText;
        Calendar datetime;

        public DatePickerFragment(TextView tv, Calendar datetime) {
            dateText = tv;
            this.datetime = datetime;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            //Calendar datetime = Calendar.getInstance();
            datetime.set(Calendar.YEAR, year);
            datetime.set(Calendar.MONTH, month);
            datetime.set(Calendar.DAY_OF_MONTH, day);

            String strDateToShow = (datetime.get(Calendar.MONTH)+1) + "/"
                    + datetime.get(Calendar.DAY_OF_MONTH) + "/"
                    + datetime.get(Calendar.YEAR);
            dateText.setText(strDateToShow);
        }
    }
    //==============================================================================================

    //Populates the Home upcoming events and sets up onItemClick event for each item that brings
    //user to details of that selected event
    private void populateList(){
        eventListAdapter = new EventListAdapter(R.layout.event_list_view, events);
        eventListView.setAdapter(eventListAdapter);
        readySelect();

    }
    //Select Event, take you to EventDetails Activity
    private void readySelect(){
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, EventDetails.class);
                i.putExtra("EventId", events.get(position).getObjectId());
                if (events.size() != 0) {
                    //Toast.makeText(getApplicationContext(), "position: " + position + " EventId of " + events.get(position).getObjectId(), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(), "Arraylist is empty", Toast.LENGTH_SHORT).show();

                }

                startActivity(i);

                // Needs alan's single event page
                //Toast.makeText(getApplicationContext(), "Clicked at position "+position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void testSearchList(ArrayList<Events> ev) {
        eventListAdapter = new EventListAdapter(R.layout.event_list_view, ev);
        eventListView = (ListView) findViewById(R.id.listView2);

        eventListView.setAdapter(eventListAdapter);
    }



    //Gets all the events in database and populates home tab
    //Should change name
    public void fillEventList() {
        Events e = new Events();
        events.clear();
        ParseQuery<Events> query = e.getQuery();
        //query.
        query.addAscendingOrder("Date");
        query.findInBackground(new FindCallback<Events>() {

            public void done(List<Events> event, ParseException e) {

                if (e == null) {
                    for (int i = 0; i < event.size(); i++) {
                        events.add(event.get(i));
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Nope", Toast.LENGTH_SHORT).show();
                }

                populateList();

            }

        });

    }
    private class EventListAdapter extends ArrayAdapter<Events> {
        int viewListXML;
        ArrayList<Events> eventArray;

        public EventListAdapter(int viewListXML, ArrayList<Events> eventArray){//Example R.layout.event_list_item, events
            super(MainActivity.this, viewListXML, eventArray);
            this.viewListXML = viewListXML;
            this.eventArray = eventArray;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null)
                view = getLayoutInflater().inflate(viewListXML, parent, false);

            Events currentEvent = eventArray.get(position);

            ParseQuery<Category> query = ParseQuery.getQuery("Category");

            query.findInBackground(new FindCallback<Category>() {
                @Override
                public void done(List<Category> categories, ParseException e) {
                    if (categories == null) {
                        Log.d("test", "The object was not found...");
                    } else {
                        Log.d("test", "Retrieved the object.");
                        ParseFile fileObject = (ParseFile) categories.get(0).getImage();
                        fileObject.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    Log.d("test", "We've got data in data.");
                                    // use data for something
                                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    Bitmap resizedbitmap=Bitmap.createScaledBitmap(bmp, 100, 100, true);

                                    imageView.setImageBitmap(resizedbitmap);

                                } else {
                                    Log.d("test", "There was a problem downloading the data.");
                                }
                            }
                        });
                    }
                }
            });

            TextView capacity = (TextView) view.findViewById(R.id.eventCapacityView);
            capacity.setText(currentEvent.getMax()+"");
            TextView activity = (TextView) view.findViewById(R.id.eventActivityView);
            activity.setText(currentEvent.getDescr());
            TextView location = (TextView) view.findViewById(R.id.eventLocationView);
            location.setText("Honolulu");
            TextView date = (TextView) view.findViewById(R.id.eventTimeView);
            SimpleDateFormat sdf = new SimpleDateFormat();
            date.setText(sdf.format(currentEvent.getDate().getTime()));




            return view;

        }

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

        ParseQueryAdapter<ParseObject> adapter = new ParseQueryAdapter<ParseObject>(this, factory);
        adapter.setTextKey("Name");
        categorySpin.setAdapter(adapter);
        categorySpin.setSelection(1);

        /**
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
         */

    }


    public void createEvent(View view) {

        EditText temp;
        temp = (EditText) findViewById(R.id.maxMembersInt);
        int maxMember = Integer.parseInt(temp.getText().toString());

        temp = (EditText) findViewById(R.id.createEventInfo);
        String eventInfo = temp.getText().toString();


        String category = selectedCategory;

        Toast.makeText(getApplicationContext(), category, Toast.LENGTH_SHORT).show();

        ParseObject createEvent = ParseObject.create("Events");

        createEvent.put("Max", maxMember);
        createEvent.put("Description", eventInfo);
        createEvent.put("Host", ParseUser.getCurrentUser());
        createEvent.put("Date", cEventDateTime.getTime());

        //get id from category
        //replace id with category id
        String categoryID = getCategoryID(category);
        createEvent.put("Category", ParseObject.createWithoutData("Category", categoryID));

        createEvent.saveInBackground();
    }

    //getid based on category string
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
        searchEvents(null, activity);
        testSearchList(ev);

        ev = new ArrayList<>();

    }

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

    }


}
