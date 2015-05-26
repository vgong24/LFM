package com.example.victor.lfm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
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
 * Created by Victor on 2/14/2015.
 */
public class EventDetails extends ActionBarActivity implements CustomMapFragment.OnMapReadyListener {
    TextView attendeeTotal;
    TextView eventDetailTime;
    String objId;
    Button join, cancel;
    TextView joinTxtView;

    Events evnt;
    ListView attendeeListView;
    TextView attenderName;
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

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_event_click);
        setContentView(R.layout.event_details);
        gmap = ((MapFragment) getFragmentManager().findFragmentById(R.id.details_map)).getMap();

        toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();


        //Setting up Header
        Intent prevInfo = getIntent();
        sdf = new SimpleDateFormat();
        objId = prevInfo.getExtras().getString("EventId");
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
        initializeFields();
        fillContent();

        ParseQuery<Events> query = ParseQuery.getQuery("Events");
        query.getInBackground(objId, new GetCallback<Events>() {
            @Override
            public void done(Events events, ParseException e) {
                if(e==null){
                    Toast.makeText(getApplicationContext(),"Information gathered!", Toast.LENGTH_SHORT).show();
                    //attendeeTotal.setText(events.getMax() + "");
                    //SimpleDateFormat sdf = new SimpleDateFormat();
                    //eventDetailTime.setText(sdf.format(events.getDate().getTime()));

                    evnt = events;
                    //initOnClicks();

                    fillAttendeesList(evnt);
                }else{
                    //Toast.makeText(getApplicationContext(),"Did not find Event + "+objId, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void initializeFields(){
        /*
        attendeeTotal = (TextView) findViewById(R.id.attendeeTotalView);
        eventDetailTime = (TextView) findViewById(R.id.eventDetailTime);

        join = (Button) findViewById(R.id.joinBtn);
        cancel = (Button) findViewById(R.id.eventDetailCancelBtn);
        attendeeListView = (ListView) findViewById(R.id.listView2);

        */
        attendeeListView = (ListView)findViewById(R.id.detail_attendee_listview);
        joinTxtView = (TextView)findViewById(R.id.join_view);
        event_description = (TextView) findViewById(R.id.detail_description);

        attendees = new ArrayList<Attendee>();
        attendeeUsers = new ArrayList<>();
    }

    public void fillContent(){
        event_description.setText(eventDescription);
    }

    public void initOnClicks(){
        joinTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    private void fillAttendeesList(Events eventID){
        ParseQuery<Attendee> query = ParseQuery.getQuery("Attendees");
        query.whereEqualTo("Event", eventID);
        query.findInBackground(new FindCallback<Attendee>() {
            @Override
            public void done(List<Attendee> attendeelist, ParseException e) {
                for(int i = 0; i<attendeelist.size();i++){
                    //ParseUser user = (ParseUser) attendeelist.get(i).get("User");
                    //_User user = attendeelist.get(i).getUserObject();
                    ParseUser user = (ParseUser) attendeelist.get(i).get("User");
                    try {
                        user.fetchIfNeeded();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                    attendeeUsers.add(user);
                    attendees.add(attendeelist.get(i));

                }

                populateList(attendees);
            }
        });

    }


    private void populateList(ArrayList<Attendee> attArr){
        //AttendeeListAdapter attendeeListAdapter= new AttendeeListAdapter(R.layout.attendee_list_view, attArr);
        AttendeeListAdapter attendeeListAdapter= new AttendeeListAdapter(R.layout.attendee_list_view, attArr);
        attendeeListView.setAdapter(attendeeListAdapter);

    }

    @Override
    public void onMapReady() {

    }

    private class AttendeeListAdapter extends ArrayAdapter<Attendee> {
        int viewListXML;
        ArrayList<Attendee> attendeeArrayList;

        public AttendeeListAdapter(int viewListXML, ArrayList<Attendee> attendeesArr){//Example R.layout.event_list_item, events
            super(EventDetails.this, viewListXML, attendeesArr);
            this.viewListXML = viewListXML;
            this.attendeeArrayList = attendeesArr;

        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null)
                view = getLayoutInflater().inflate(viewListXML, parent, false);
            ParseUser player = attendeeUsers.get(position);
            String playerName = player.getString("username");

            final ImageView attendeePic = (ImageView) view.findViewById(R.id.attendeeProfilePic);
            //**=============================================Display profile pic in attendees list

            ParseFile thumbnail = null;
            if((thumbnail = (player.getParseFile("profilePicture"))) != null){
                thumbnail.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e == null) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                            if (bmp != null) {
                                Bitmap resizedbitmap = Bitmap.createScaledBitmap(bmp, 100, 100, true);
                                attendeePic.setImageBitmap(resizedbitmap);
                            }
                        } else {
                            Log.e("paser after download", "null");

                        }
                    }
                });

            }else {
                Log.e("parse file", " null");
                attendeePic.setPadding(10,10,10,10);

            }

            attenderName = (TextView) view.findViewById(R.id.attendeeListViewName);
            attenderName.setText(playerName);

            return view;

        }

    }

}
