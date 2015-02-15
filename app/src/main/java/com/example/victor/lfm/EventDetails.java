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


    ArrayList<Attendee> attendees = new ArrayList<Attendee>();
    ArrayAdapter<Events> adapter;
    AttendeeListAdapter attendeeListAdapter;
    ListView eventListView;


    public EventDetails(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_click);
        attendeeTotal = (TextView) findViewById(R.id.attendeeTotalView);
        join = (Button) findViewById(R.id.joinBtn);
        attendeeListView = (ListView) findViewById(R.id.listView2);


        Intent prevInfo = getIntent();
        objId = prevInfo.getExtras().getString("EventId");
        ParseQuery<Events> query = ParseQuery.getQuery("Events");
        query.getInBackground(objId, new GetCallback<Events>() {
            @Override
            public void done(Events events, ParseException e) {
                if(e==null){
                    attendeeTotal.setText(events.getMax() + "");
                    evnt = events;

                }else{
                    //Toast.makeText(getApplicationContext(),"Did not find Event + "+objId, Toast.LENGTH_SHORT).show();
                }
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject attendee = ParseObject.create("Attendees");
                attendee.put("Event", evnt);
                attendee.put("User", ParseUser.getCurrentUser().getObjectId());
                attendee.saveInBackground();
                Toast.makeText(getApplicationContext(), "Joined Event", Toast.LENGTH_SHORT).show();


            }
        });

    }
    private void fillAttendeesList(){
        ParseQuery<Attendee> query = ParseQuery.getQuery("Attendees");
        query.whereEqualTo("EventId", objId);
        query.findInBackground(new FindCallback<Attendee>() {
            @Override
            public void done(List<Attendee> attendeelist, ParseException e) {
                attendees = (ArrayList<Attendee>) attendeelist;

            }
        });


    }


    private void populateList(){
        AttendeeListAdapter attendeeListAdapter= new AttendeeListAdapter(R.layout.event_list_view, attendees);
        attendeeListView.setAdapter(attendeeListAdapter);


    }
    private class AttendeeListAdapter extends ArrayAdapter<Attendee> {
        int viewListXML;
        ArrayList<Events> eventArray;

        public AttendeeListAdapter(int viewListXML, ArrayList<Attendee> attendees){//Example R.layout.event_list_item, events
            super(EventDetails.this, viewListXML, attendees);
            this.viewListXML = viewListXML;

        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null)
                view = getLayoutInflater().inflate(viewListXML, parent, false);

            ParseObject player = attendees.get(position);

            Events currentEvent = eventArray.get(position);
            TextView category = (TextView) view.findViewById(R.id.eventCategoryView);
            category.setText(currentEvent.getCat().getName());
            TextView capacity = (TextView) view.findViewById(R.id.eventCapacityView);
            capacity.setText(currentEvent.getMax()+"");

            return view;

        }

    }

}
