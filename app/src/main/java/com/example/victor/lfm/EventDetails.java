package com.example.victor.lfm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

/**
 * Created by Victor on 2/14/2015.
 */
public class EventDetails extends Activity {
    TextView attendeeTotal;
    String objId;

    public EventDetails(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_click);
        attendeeTotal = (TextView) findViewById(R.id.attendeeTotalView);
        Intent prevInfo = getIntent();
        objId = prevInfo.getExtras().getString("EventId");
        ParseQuery<Events> query = ParseQuery.getQuery("Events");
        query.getInBackground(objId, new GetCallback<Events>() {
            @Override
            public void done(Events events, ParseException e) {
                if(e==null){
                    attendeeTotal.setText(events.getMax() + "");


                }else{
                    //Toast.makeText(getApplicationContext(),"Did not find Event + "+objId, Toast.LENGTH_SHORT).show();
                }
            }
        });






    }

}
