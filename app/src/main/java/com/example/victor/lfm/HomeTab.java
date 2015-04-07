package com.example.victor.lfm;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import android.support.v4.app.Fragment;

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

    ProgressBar dialog;

    public HomeTab(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.home_tab, container, false);
        initialize();
        return v;
    }


    public void initialize() {
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
        eventListAdapter = new EventListAdapter(context, R.layout.event_list_view, events);
        eventListView.setAdapter(eventListAdapter);
        readySelect();

    }

    //Select Event, take you to EventDetails Activity
    private void readySelect() {
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
                //dialog.show();
                if (e == null) {
                    for (int i = 0; i < event.size(); i++) {
                        events.add(event.get(i));
                        //Toast.makeText(context, "events.size is: "+events.size() + " ObjectId : " + events.get(i).getObjectId(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context.getApplicationContext(), "Nope", Toast.LENGTH_SHORT).show();
                }

                populateList();
                dialog.setVisibility(View.GONE);
            }

        });


    }
}
