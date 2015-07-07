package com.example.victor.lfm;

import android.app.Activity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import android.support.v4.app.Fragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 4/6/2015.
 */
public class HomeTab extends Fragment {
    View v;
    ArrayList<Events> events;
    EventListAdapter eventListAdapter;

    Activity activity;
    Context context;

    ListView eventListView;
    GPSTracker tracker;

    ProgressBar dialog;
    boolean _areEventsLoaded = false;

    /**
     * Alternate viewlist layouts
     * @param context
     */
    //R.layout.event_list_view , R.layout.event_item_reddit
    private final int itemList_xml = R.layout.event_item_reddit;



    public static HomeTab newInstance(Context context) {
        HomeTab homeTab = new HomeTab();
        homeTab.context = context;
        return homeTab;
    }

    public HomeTab(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.home_tab, container, false);
        initialize();
        return v;
    }


    public void initialize() {
        tracker = new GPSTracker(context);
        initField();
        dialog.setVisibility(View.VISIBLE);
        fillEventList();
    }

    public void initField() {
        events = new ArrayList<Events>();
        eventListView = (ListView) v.findViewById(R.id.eventList);
        dialog = (ProgressBar) v.findViewById(R.id.eventsProgressBar);
    }

    private void populateList() {
        eventListAdapter = new EventListAdapter(context, itemList_xml, events);
        eventListView.setAdapter(eventListAdapter);

        readySelect();

    }

    //Select Event, take you to EventDetails Activity
    private void readySelect() {
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Optimize: create a static method to start this activity
                Intent i = new Intent(context.getApplicationContext(), EventDetails.class);
                i.putExtra("EventId", events.get(position).getObjectId());
                i.putExtra("EventDate", events.get(position).getDate().getTime());
                i.putExtra("EventTitle", events.get(position).getDescr());
                i.putExtra("EventLat", events.get(position).getLocation().getLatitude());
                i.putExtra("EventLong", events.get(position).getLocation().getLongitude());
                i.putExtra("EventHost", events.get(position).getHost().getObjectId());


                if (events.size() != 0) {
                    //Toast.makeText(context, "position: " + position + " EventId of " + events.get(position).getObjectId(), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(context.getApplicationContext(), "Arraylist is empty", Toast.LENGTH_SHORT).show();

                }

                context.startActivity(i);

            }
        });
    }

    public void fillEventList() {
        ParseGeoPoint geoPoint = null;

        //Filter contents by gps coordinates
        if(tracker.canGetLocation()){
            geoPoint = new ParseGeoPoint(tracker.getLatitude(), tracker.getLongitude());
        }else{
            tracker.showSettingsAlert();
            return;
        }
        Events e = new Events();
        events.clear();
        ParseQuery<Events> query = e.getQuery();
        query.whereWithinKilometers("Location", geoPoint, 100);
        query.addAscendingOrder("Date");
        query.findInBackground(new FindCallback<Events>() {

            public void done(List<Events> event, ParseException excep) {
                //dialog.show();
                if (excep == null) {
                    for (int i = 0; i < event.size(); i++) {
                        events.add(event.get(i));
                        //Toast.makeText(context, "events.size is: "+events.size() + " ObjectId : " + events.get(i).getObjectId(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context.getApplicationContext(), "Nope", Toast.LENGTH_SHORT).show();
                }
                dialog.setVisibility(View.GONE);
                populateList();

            }

        });

    }


    @Override
    public void onDetach(){
        super.onDetach();
        try{
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        }catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


}
