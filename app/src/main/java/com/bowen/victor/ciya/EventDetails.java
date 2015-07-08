package com.bowen.victor.ciya;

import android.content.Intent;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    ProgressBar progressBar;


    ArrayList<Attendee> attendees;
    ArrayList<ParseUser> attendeeUsers;
    Toolbar toolbar;
    ActionBar ab;
    SimpleDateFormat sdf;
    String hostId;
    boolean isHost;
    String currentUserId;
    String userAttendeeId;
    AttendeeListAdapter attendeeListAdapter;

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
        new SetUpBackground().execute(evnt);

    }

    //Initialize textviews, arraylists, Intent extras
    public void initFields(){
        progressBar = (ProgressBar) findViewById(R.id.eventDetailProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        //Get data from previous Intent in HomeTab.java @ readySelect method
        prevInfo = getIntent();
        isHost = false;
        currentUserId = ParseUser.getCurrentUser().getObjectId();

        hostId = prevInfo.getExtras().getString("EventHost");

        if(hostId.equalsIgnoreCase(currentUserId)){
            isHost = true;

        }

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
        joinTxtView.setEnabled(false);

        event_description = (TextView) findViewById(R.id.detail_description);
        //Setup lists
        attendees = new ArrayList<Attendee>();
        attendeeUsers = new ArrayList<>();
    }
    //Fill in the description/Title of the event
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
        if(isHost){
            getMenuInflater().inflate(R.menu.details_toolbar_host, menu);
        }else{
            getMenuInflater().inflate(R.menu.details_toolbar, menu);

        }
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

    public void initOnClicks(final boolean hasJoined){
        //If current user hasn't joined the event yet
        joinTxtView.setEnabled(true);
        if(!hasJoined && !isHost){
            joinTxtView.setText("Join");
            joinTxtView.setBackgroundResource(R.color.GreenJoin);
            joinTxtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    joinEventAsAttendee(evnt);
                }
            });
        }else{
            joinTxtView.setText("Leave");
            joinTxtView.setBackgroundResource(R.color.RedExit);
            joinTxtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isHost) {
                        Toast.makeText(getApplicationContext(), "Leaving group", Toast.LENGTH_SHORT).show();
                        //Leave event as attendee
                        leaveEventAsAttendee(evnt);
                    } else {
                        Toast.makeText(getApplicationContext(), " Host Leaveing group (not implemented)", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }

    }
    public void joinEventAsAttendee(Events eventJoining){
        Toast.makeText(getApplicationContext(), "Joining", Toast.LENGTH_SHORT).show();
        Attendee attend = new Attendee();
        attend.setEvent(eventJoining);
        attend.setUser(currentUserId);
        attend.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                //once clicked, refresh page
                new SetUpBackground().execute(evnt);
            }
        });
    }

    public void leaveEventAsAttendee(final Events eventLeaving){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Attendees");
        Log.v("leaveEvent", "attendeeId: " + userAttendeeId);
        query.getInBackground(userAttendeeId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    parseObject.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.v("deleted", "deleted attendee");
                            new SetUpBackground().execute(eventLeaving);
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onMapReady() {

    }

    private void populateList(ArrayList<Attendee> attArr){

        attendeeListAdapter= new AttendeeListAdapter(getApplicationContext(), R.layout.attendee_list_view, attArr);
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

        private boolean currentlyJoined;

        @Override
        protected void onPreExecute(){
            joinTxtView.setEnabled(false);
        }

        @Override
        protected ArrayList<Attendee> doInBackground(Events... params) {
            currentlyJoined = false;
            if(attendeesArr == null){
                attendeesArr = new ArrayList<>();
            }else{
                attendeesArr.clear();
            }

            ParseQuery<Attendee> query = ParseQuery.getQuery("Attendees");
            query.whereEqualTo("Event", params[0]);
            try {

                List<Attendee> tempList = query.find();
                for( Attendee attend : tempList){
                    //Need to retrieve User data within the attend object
                    ParseUser user = (ParseUser) attend.get("User");
                    user.fetchIfNeeded();
                    if(user.getObjectId().equalsIgnoreCase(currentUserId)){
                        currentlyJoined = true;
                        userAttendeeId = attend.getObjectId();
                    }
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
                Log.d("Count", i + " ");
                //Toast.makeText(getApplicationContext(), attendees.get(i).getUserFirstName(), Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
            populateList(attendees);
            initOnClicks(currentlyJoined);
        }
    }





}
