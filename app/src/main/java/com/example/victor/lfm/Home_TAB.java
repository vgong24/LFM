package com.example.victor.lfm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 3/20/2015.
 */
public class Home_TAB extends Activity{
    ArrayList<Events> events;
    EventListAdapter eventListAdapter;

    Activity activity;
    Context context;

    ListView eventListView;
    TabHost tabhost;
    TabHost.TabSpec tabSpec;

    public Home_TAB(TabHost tabhost, Context context){
        this.tabhost = tabhost;
        this.context = context;
        this.activity = (Activity) context;
    }
    public Home_TAB(){}


    public void initTab(){
        tabSpec = tabhost.newTabSpec("home");
        tabSpec.setContent(R.id.homeTab);
        tabSpec.setIndicator("Home");
        tabhost.addTab(tabSpec);
    }

    public void initialize() {
        initTab();
        initField();
        fillEventList();
    }
    public void initField(){
        events = new ArrayList<Events>();
        eventListView = (ListView) activity.findViewById(R.id.listView);
    }

    private void populateList(){
        eventListAdapter = new EventListAdapter(context, R.layout.event_list_view, events);
        eventListView.setAdapter(eventListAdapter);
        readySelect();

    }
    //Select Event, take you to EventDetails Activity
    private void readySelect(){
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(context.getApplicationContext(), EventDetails.class);
                i.putExtra("EventId", events.get(position).getObjectId());
                if (events.size() != 0) {
                   //Toast.makeText(context, "position: " + position + " EventId of " + events.get(position).getObjectId(), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(context.getApplicationContext(), "Arraylist is empty", Toast.LENGTH_SHORT).show();

                }

                context.startActivity(i);

                // Needs alan's single event page
                //Toast.makeText(getApplicationContext(), "Clicked at position "+position, Toast.LENGTH_SHORT).show();
            }
        });
    }
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
                        Toast.makeText(context, "events.size is: "+events.size() + " ObjectId : " + events.get(i).getObjectId(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Nope", Toast.LENGTH_SHORT).show();
                }

                populateList();

            }

        });

    }



}
