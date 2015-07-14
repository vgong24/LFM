package com.bowen.victor.ciya.fragments;

import android.app.Activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;;
import android.widget.Toast;

import com.bowen.victor.ciya.activities.CreateEvent;
import com.bowen.victor.ciya.activities.EventDetails;
import com.bowen.victor.ciya.adapters.EventRecyclerAdapter;
import com.bowen.victor.ciya.dbHandlers.FriendListDBHandler;
import com.bowen.victor.ciya.tools.GPSTracker;
import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.adapters.EventListAdapter;
import com.bowen.victor.ciya.structures.Events;
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
    FriendListDBHandler db;

    ProgressBar dialog;
    boolean _areEventsLoaded = false;


    /**
     * Alternate viewlist layouts
     * @param context
     */
    //R.layout.event_list_view , R.layout.event_item_reddit
    private final int itemList_xml = R.layout.event_item_reddit;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;


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
        db = new FriendListDBHandler(context);
        initField();
        dialog.setVisibility(View.VISIBLE);
        fillEventList();
    }

    public void initField() {
        events = new ArrayList<Events>();
        //eventListView = (ListView) v.findViewById(R.id.eventList);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.event_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh items
                fillEventList();
            }

        });

        dialog = (ProgressBar) v.findViewById(R.id.eventsProgressBar);
    }

    private void populateList() {
        /*
        eventListAdapter = new EventListAdapter(context, itemList_xml, events);
        eventListView.setAdapter(eventListAdapter);
        */
        mAdapter = new EventRecyclerAdapter(context, events);
        mRecyclerView.setAdapter(mAdapter);

        //readySelect();
        //finished displaying so stop refresh listener
        mSwipeRefreshLayout.setRefreshing(false);

    }

    //Select Event, take you to EventDetails Activity
    private void readySelect() {
        //eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Optimize: create a static method to start this activity
                EventDetails.startEventDetails(context, events.get(position));

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
        query.whereNotEqualTo("privacy", CreateTab.PRIVATE);
        query.addAscendingOrder("Date");
        query.findInBackground(new FindCallback<Events>() {

            public void done(List<Events> event, ParseException excep) {
                //dialog.show();
                if (excep == null) {
                    //Check if privacy setting is friend, if not friend, don't add
                    for(Events eventObject: event) {
                        try {

                            if (eventObject.getPrivacy().equalsIgnoreCase(CreateTab.FRIEND)) {
                                //check if friend is yours
                                try {
                                    String hostId = eventObject.getHost().fetchIfNeeded().getObjectId();
                                    if (db.profileExists(hostId) > 0) {
                                        events.add(eventObject);
                                    }

                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }

                            } else {
                                events.add(eventObject);
                            }
                        }catch (Exception e){
                            //If privacy setting is null, assume public
                            events.add(eventObject);
                        }
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
