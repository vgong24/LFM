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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.SupportMapFragment;

import android.location.*;
import android.graphics.*;


public class MainActivity extends ActionBarActivity implements OnCameraChangeListener, OnMapReadyCallback {

    Button logOut = null;
    private static final int TIME_DIALOG_ID = 0;
    TextView timeView;
    Button timeBtn;
    TextView dateView;
    Button dateBtn;
    private TextView timeText;

    GoogleMap map;
    TextView filterAddress;
    Marker marker;



    ArrayList<Events> events = new ArrayList<Events>();
    ArrayAdapter<Events> adapter;
    EventListAdapter eventListAdapter;
    ListView eventListView;

    List<ParseObject> ob;

    String cater;
    Date date;


    List<String> catNames = new ArrayList<String>();
    ArrayList<Date> dates = new ArrayList<Date>();
    ArrayList<Category> searchCategories = new ArrayList<Category>();
    ArrayList<Date> searchDates = new ArrayList<Date>();
    ArrayList<Events> ev = new ArrayList<Events>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTabs();
        initFields();

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

        timeView = (TextView) findViewById(R.id.timeView);
        timeBtn = (Button) findViewById(R.id.timeBtn);
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new Mytimepicker(timeView);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

        dateView = (TextView) findViewById(R.id.dateView);
        dateBtn = (Button) findViewById(R.id.dateBtn);
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(dateView);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        logOut = (Button) findViewById(R.id.logout_btn);
        logOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                startActivity(new Intent(v.getContext(), DispatchActivity.class));
            }
        });


        MapFragment mapFrag=
                (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        map = mapFrag.getMap();
        //map.setMyLocationEnabled(true);

        filterAddress = (TextView) findViewById(R.id.addressText);



        buttonMaker();

        //searchEvents(null, "Study");
        //Toast.makeText(getApplicationContext(), searchCategories.get(0).getName() + "", Toast.LENGTH_SHORT).show();




    }

    @Override
    public void onCameraChange(CameraPosition position) {

    }


    public void onMapReady(GoogleMap map) {

        LatLng loc = new LatLng(21.299816,-157.81757900000002 );


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));

        map.addMarker(new MarkerOptions()
                .title("Event Location")
                .position(loc));

        filterAddress.setText("My Location");
    }




    public void initFields() {
        eventListView = (ListView) findViewById(R.id.listView);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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


    public static class Mytimepicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        TextView timeTxt;

        public Mytimepicker(TextView txtview) {
            timeTxt = txtview;
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

    //Populates the Home upcoming events and sets up onItemClick event for each item that brings
    //user to details of that selected event
    private void populateList(){
        eventListAdapter = new EventListAdapter(R.layout.event_list_view, events);
        eventListView.setAdapter(eventListAdapter);
        readySelect();

    }

    private void readySelect(){
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, EventDetails.class);
                i.putExtra("EventId", events.get(position).getObjectId());
                if(events.size() != 0){
                    Toast.makeText(getApplicationContext(), "position: "+position+" EventId of "+ events.get(position).getObjectId(), Toast.LENGTH_SHORT).show();

                }else{
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

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        TextView dateText;

        public DatePickerFragment(TextView tv) {
            dateText = tv;
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
            Calendar datetime = Calendar.getInstance();
            datetime.set(Calendar.YEAR, year);
            datetime.set(Calendar.MONTH, month);
            datetime.set(Calendar.DAY_OF_MONTH, day);
            String strDateToShow = (datetime.get(Calendar.MONTH)+1) + "/"
                    + datetime.get(Calendar.DAY_OF_MONTH) + "/"
                    + datetime.get(Calendar.YEAR);
            dateText.setText(strDateToShow);
        }
    }


    public void buttonMaker() {
        Events e = new Events();

        ParseQuery<Events> query = e.getQuery();
        //query.
        query.addAscendingOrder("Date");
        query.findInBackground(new FindCallback<Events>() {

            public void done(List<Events> event, ParseException e) {

                if (e == null) {
                    for (int i = 0; i < event.size(); i++) {
                        //event.get(i).fetchIfNeeded();

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



            /*
            query.findInBackground(new FindCallback() {
                                        @Override
                                        public void done(List<T> object, ParseException e) {
                                            if (object == null) {
                                                Log.d("test", "The object was not found...");
                                            } else {
                                                Log.d("test", "Retrieved the object.");
                                                ParseFile fileObject = (ParseFile) object.get("Logo");
                                                fileObject.getDataInBackground(new GetDataCallback() {
                                                    public void done(byte[] data, ParseException e) {
                                                        if (e == null) {
                                                            Log.d("test", "We've got data in data.");
                                                            // use data for something
                                                            ImageView imageView = (ImageView) findViewById(R.id.imageView);
                                                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                            imageView.setImageBitmap(bmp);

                                                        } else {
                                                            Log.d("test", "There was a problem downloading the data.");
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });*/




            /*
            byte[] ba = currentEvent.getCat().getImage();
            Bitmap bmp = BitmapFactory.decodeByteArray(ba, 0, ba.length);
            ParseImageView imageView = (ParseImageView) findViewById(R.id.evImageView);

            imageView.setParseFile(fileObject);*/

            //Toast.makeText(getApplicationContext(), currentEvent.getCat().getName(), Toast.LENGTH_SHORT).show();



            //TextView category = (TextView) view.findViewById(R.id.eventCategoryView);
            //category.setText(currentEvent.getCat().getName());
            TextView capacity = (TextView) view.findViewById(R.id.eventCapacityView);
            capacity.setText(currentEvent.getMax()+"");




            return view;

        }

    }


    public void createEvent(View view) {

        EditText temp;
        TextView blah;

        temp = (EditText) findViewById(R.id.maxMembersInt);
        int maxMember = Integer.parseInt(temp.getText().toString());

        temp = (EditText) findViewById(R.id.createEventInfo);
        String eventInfo = temp.getText().toString();

        temp = (EditText) findViewById(R.id.eventCreateCategory);
        String category = temp.getText().toString();
        String catString = "";

        ParseObject gameScore = ParseObject.create("Events");

        try {
            ListIterator<Category> c = Category.getQuery().find().listIterator();
            while(c.hasNext()) {
                Category cat = c.next();
                if(cat.getName().equals(category)) {
                    catString = cat.getObjectId();
                    gameScore.put("Category", Category.getQuery().get(catString));
                }
            }
        } catch (com.parse.ParseException pe) {

        }

        gameScore.put("Max", maxMember);
        gameScore.put("Description", eventInfo);
        gameScore.put("Host", ParseUser.getCurrentUser());

        gameScore.saveInBackground();
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
                                //Toast.makeText(getApplicationContext(), searchCategories.size()+"", Toast.LENGTH_SHORT).show();

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
