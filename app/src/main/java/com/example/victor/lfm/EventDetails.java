package com.example.victor.lfm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Victor on 2/14/2015.
 */
public class EventDetails extends Activity {
    TextView attendeeTotal;
    String objId;
    Button join;
    Events evnt;
    ListView attendeeListView;


    ArrayList<Attendee> attendees;
    ArrayAdapter<Events> adapter;
    AttendeeListAdapter attendeeListAdapter;
    ListView eventListView;


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
        join = (Button) findViewById(R.id.joinBtn);
        attendeeListView = (ListView) findViewById(R.id.listView2);
        attendees = new ArrayList<Attendee>();
    }

    public void initOnClicks(){
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Attendee attend = new Attendee();
                attend.setEvent(evnt);
                attend.setUser(ParseUser.getCurrentUser().getObjectId());
                attend.saveInBackground();

                //GET USER'S FIRSTNAME FROM USER THROUGH ATTENDEES! BUT HOW?!
                //TODO:
                //String fn = attend.getParseUser("User").getUsername();
                //Toast.makeText(getApplicationContext(),fn, Toast.LENGTH_SHORT).show();


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
                    attendees.add(attendeelist.get(i));

                }
                //Toast.makeText(getApplicationContext(), "Found "+attendees.size()+" attendees", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), attendees.get(1).getParseObject("User").getObjectId(), Toast.LENGTH_SHORT).show();
                //Collections.copy(attendees, attendeelist);
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
            //Find a way to retrieve User's firstname by accessing the "User" column
            Attendee player = attendeeArrayList.get(position);

            TextView attenderName = (TextView) view.findViewById(R.id.attendeeListViewName);
            String userId = player.getParseObject("User").getObjectId();
            String name = "";
            ParseQuery query = ParseQuery.getQuery("_User");
            query.getInBackground(userId, new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        Toast.makeText(getApplicationContext(), "Player: "+ object.getString("firstName"), Toast.LENGTH_SHORT).show();
                    } else {
                        // something went wrong
                    }
                }
            });

            //Toast.makeText(getApplicationContext(),"test", Toast.LENGTH_SHORT).show();
            attenderName.setText("Player "+name);

            return view;

        }

    }

}
