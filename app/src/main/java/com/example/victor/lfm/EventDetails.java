package com.example.victor.lfm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.List;

/**
 * Created by Victor on 2/14/2015.
 */
public class EventDetails extends Activity {
    TextView attendeeTotal;
    TextView eventDetailTime;
    String objId;
    Button join, cancel;
    Events evnt;
    ListView attendeeListView;
    TextView attenderName;


    ArrayList<Attendee> attendees;
    ArrayList<ParseUser> attendeeUsers;


    public EventDetails(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_click);

        initializeFields();

        Intent prevInfo = getIntent();
        objId = prevInfo.getExtras().getString("EventId");
        ParseQuery<Events> query = ParseQuery.getQuery("Events");
        query.getInBackground(objId, new GetCallback<Events>() {
            @Override
            public void done(Events events, ParseException e) {
                if(e==null){
                    Toast.makeText(getApplicationContext(),"Information gathered!", Toast.LENGTH_SHORT).show();
                    attendeeTotal.setText(events.getMax() + "");
                    SimpleDateFormat sdf = new SimpleDateFormat();
                    eventDetailTime.setText(sdf.format(events.getDate().getTime()));

                    evnt = events;
                    initOnClicks();

                    fillAttendeesList(evnt);
                }else{
                    //Toast.makeText(getApplicationContext(),"Did not find Event + "+objId, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void initializeFields(){
        attendeeTotal = (TextView) findViewById(R.id.attendeeTotalView);
        eventDetailTime = (TextView) findViewById(R.id.eventDetailTime);

        join = (Button) findViewById(R.id.joinBtn);
        cancel = (Button) findViewById(R.id.eventDetailCancelBtn);
        attendeeListView = (ListView) findViewById(R.id.listView2);
        attendees = new ArrayList<Attendee>();
        attendeeUsers = new ArrayList<>();
    }

    public void initOnClicks(){
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Attendee attend = new Attendee();
                attend.setEvent(evnt);
                attend.setUser(ParseUser.getCurrentUser().getObjectId());
                attend.saveInBackground();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
        AttendeeListAdapter attendeeListAdapter= new AttendeeListAdapter(R.layout.attendee_list_view, attArr);
        attendeeListView.setAdapter(attendeeListAdapter);

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
