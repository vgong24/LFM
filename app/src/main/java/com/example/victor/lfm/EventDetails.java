package com.example.victor.lfm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Methods organized by sections in event_details xml from top to bottom
 * Created by Victor on 2/14/2015.
 */
public class EventDetails extends ActionBarActivity implements CustomMapFragment.OnMapReadyListener {
    Intent prevInfo;
    String objId;
    TextView joinTxtView;

    Events evnt;
    ListView attendeeListView;

    TextView event_description;
    String eventDescription;


    ArrayList<Attendee> attendees;
    ArrayList<ParseUser> attendeeUsers;
    Toolbar toolbar;
    ActionBar ab;
    SimpleDateFormat sdf;

    private GoogleMap gmap;

    public EventDetails(){

    }


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);
        initFields();
        initToolBar();
        initEventDescription();
        setupMap();

        //TODO fill attendees in async task so the user can exit the activity if they make a mistake
        //setupAttendees();
        new SetUpBackground().execute(evnt);
        //initOnClicks();

    }

    //Initialize textviews, arraylists, Intent extras
    public void initFields(){
        //Get data from previous Intent in HomeTab.java @ readySelect method
        prevInfo = getIntent();
        objId = prevInfo.getExtras().getString("EventId");
        evnt = (Events) ParseObject.createWithoutData("Events", objId);
        //Setup header fields
        toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        //Map setup
        gmap = ((MapFragment) getFragmentManager().findFragmentById(R.id.details_map)).getMap();
        //Setup views
        attendeeListView = (ListView)findViewById(R.id.detail_attendee_listview);
        joinTxtView = (TextView)findViewById(R.id.join_view);
        event_description = (TextView) findViewById(R.id.detail_description);
        //Setup lists
        attendees = new ArrayList<Attendee>();
        attendeeUsers = new ArrayList<>();
    }
    public void initEventDescription(){
        event_description.setText(eventDescription);
    }
    //Setup Header
    public void initToolBar(){
        sdf = new SimpleDateFormat();

        long eventtime = prevInfo.getExtras().getLong("EventDate");
        eventDescription = prevInfo.getExtras().getString("EventTitle");

        sdf.applyLocalizedPattern("M/d/yy");
        String date = sdf.format(eventtime);
        sdf.applyLocalizedPattern("h:mm a");
        String time = sdf.format(eventtime);
        String relativeTime;
        if(DateUtils.isToday(eventtime)) {

            //If less than one hour
            Calendar cal = Calendar.getInstance();
            long currentTime = cal.getTimeInMillis();
            if(currentTime > eventtime - DateUtils.HOUR_IN_MILLIS){
                if(currentTime > eventtime){
                    relativeTime = (String) DateUtils.getRelativeTimeSpanString(eventtime,currentTime, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
                    ab.setTitle("Started " + relativeTime);
                }else{
                    relativeTime = (String) DateUtils.getRelativeTimeSpanString(eventtime,currentTime, DateUtils.MINUTE_IN_MILLIS);
                    ab.setTitle("Starting "+ relativeTime);
                }

            }else
                ab.setTitle("Today at " + time);

        }else {
            ab.setTitle(date + " at " + time);
        }

    }

    //Setup Map Section
    public void setupMap(){
        if(gmap!=null){
            double latitude = prevInfo.getExtras().getDouble("EventLat");
            double longitude = prevInfo.getExtras().getDouble("EventLong");
            LatLng eventLatLng = new LatLng(latitude, longitude);
            Marker eventMarker = gmap.addMarker(new MarkerOptions().position(eventLatLng).title("Here"));
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, 13));

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.details_toolbar, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_switch_chat) {
            Toast.makeText(getApplicationContext(), "meep", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initOnClicks(){
        joinTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Joining", Toast.LENGTH_SHORT).show();
                Attendee attend = new Attendee();
                attend.setEvent(evnt);
                attend.setUser(ParseUser.getCurrentUser().getObjectId());
                attend.saveInBackground();
            }
        });
/*
        cancel.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        */
    }

    @Override
    public void onMapReady() {

    }
    
    private void populateList(ArrayList<Attendee> attArr){
        AttendeeListAdapter attendeeListAdapter= new AttendeeListAdapter(getApplicationContext(), R.layout.attendee_list_view, attArr);
        attendeeListView.setAdapter(attendeeListAdapter);

    }


    @Override
    public void onBackPressed(){
        finish();
    }

    /**
     * SEPARATE CLASS]
     * Finds attendee data from database and fills the viewlist in a background thread
     * Allows users to back out of the event details page without having to wait for the table to be filled
     * Also gives more freedom to change the Join Textview depending on whether or not user has already joined or wants to leave
     */
    private class SetUpBackground extends AsyncTask<Events, Void, ArrayList<Attendee>>{
        ArrayList<Attendee> attendeesArr;

        @Override
        protected ArrayList<Attendee> doInBackground(Events... params) {
            attendeesArr = new ArrayList<>();
            ParseQuery<Attendee> query = ParseQuery.getQuery("Attendees");
            query.whereEqualTo("Event", params[0]);
            try {

                List<Attendee> tempList = query.find();
                for( Attendee attend : tempList){
                    //Need to retrieve User data within the attend object
                    ParseUser user = (ParseUser) attend.get("User");
                    user.fetchIfNeeded();
                    attendeesArr.add(attend);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return attendeesArr;
        }

        @Override
        protected void onPostExecute(ArrayList<Attendee> attendees){

            Log.d("Count", "Starting Debug");
            for (int i = 0; i < attendees.size(); i++){
                Log.d("Count", i + " " + attendees.get(i).getUserFirstName());
                //Toast.makeText(getApplicationContext(), attendees.get(i).getUserFirstName(), Toast.LENGTH_SHORT).show();
            }
            populateList(attendees);
            initOnClicks();
        }
    }





}
