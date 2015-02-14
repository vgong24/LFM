package com.example.victor.lfm;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;

import android.widget.*;

import com.parse.Parse;
import com.parse.ParseUser;
import com.parse.*;

import java.util.Calendar;
import java.util.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends ActionBarActivity {

    Button logOut = null;
    private static final int TIME_DIALOG_ID = 0;
    TextView timeView;
    Button timeBtn;
    TextView dateView;
    Button dateBtn;
    private TextView timeText;


    ArrayList<Events> events = new ArrayList<Events>();
    ArrayAdapter<Events> adapter;
    EventListAdapter eventListAdapter;
    ListView eventListView;

    List<ParseObject> ob;

    String cater;
    Date date;


    ArrayList<Category> categories = new ArrayList<Category>();
    ArrayList<Date> dates = new ArrayList<Date>();
    ArrayList<Category> searchCategories = new ArrayList<Category>();
    ArrayList<Date> searchDates = new ArrayList<Date>();
    ArrayList<Events> ev = new ArrayList<Events>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTabs();
        initFields();

        timeView = (TextView) findViewById(R.id.timeView);
        timeBtn = (Button) findViewById(R.id.timeBtn);
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new Mytimepicker(timeView);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

        dateView = (TextView) findViewById(R.id.dateView);
        dateBtn = (Button) findViewById(R.id.dateBtn);
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(dateView);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        logOut = (Button) findViewById(R.id.logout_btn);
        logOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                startActivity(new Intent(v.getContext(), DispatchActivity.class));
            }
        });


        buttonMaker();

        //searchEvents(null, "Study");
        //Toast.makeText(getApplicationContext(), searchCategories.get(0).getName() + "", Toast.LENGTH_SHORT).show();




    }

    public void initFields() {
        eventListView = (ListView) findViewById(R.id.listView);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initTabs() {
        TabHost tabhost = (TabHost) findViewById(R.id.tabHost);
        tabhost.setup();
        TabHost.TabSpec tabSpec = tabhost.newTabSpec("home");
        tabSpec.setContent(R.id.homeTab);
        tabSpec.setIndicator("Home");
        tabhost.addTab(tabSpec);
        tabSpec = tabhost.newTabSpec("search");
        tabSpec.setContent(R.id.searchTab);
        tabSpec.setIndicator("Search");
        tabhost.addTab(tabSpec);
        tabSpec = tabhost.newTabSpec("create");
        tabSpec.setContent(R.id.createTab);
        tabSpec.setIndicator("Create");
        tabhost.addTab(tabSpec);
        tabSpec = tabhost.newTabSpec("profile");
        tabSpec.setContent(R.id.profileTab);
        tabSpec.setIndicator("Profile");
        tabhost.addTab(tabSpec);
    }


    public static class Mytimepicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        TextView timeTxt;

        public Mytimepicker(TextView txtview) {
            timeTxt = txtview;
        }

        public Mytimepicker() {

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
            timeDialog.setTitle("Test");
            return timeDialog;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
// do something with the time chosen. http://stackoverflow.com/questions/2659954/timepickerdialog-and-am-or-pm/2660148#2660148
            String am_pm = "";
            Calendar datetime = Calendar.getInstance();
            datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            datetime.set(Calendar.MINUTE, minute);
            if (datetime.get(Calendar.AM_PM) == Calendar.AM)
                am_pm = "AM";
            else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
                am_pm = "PM";
            String strHrsToShow = (datetime.get(Calendar.HOUR) == 0) ? "12" : datetime.get(Calendar.HOUR) + "";
            timeTxt.setText(strHrsToShow + ":" + datetime.get(Calendar.MINUTE) + " " + am_pm);
        }
    }


    private void populateList(){
        eventListAdapter = new EventListAdapter(R.layout.event_list_view, events);
        eventListView.setAdapter(eventListAdapter);
    }

    private void testSearchList(ArrayList<Events> ev) {
        eventListAdapter = new EventListAdapter(R.layout.event_list_view, ev);
        eventListView = (ListView) findViewById(R.id.listView2);

        eventListView.setAdapter(eventListAdapter);
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        TextView dateText;

        public DatePickerFragment(TextView tv) {
            dateText = tv;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Calendar datetime = Calendar.getInstance();
            datetime.set(Calendar.YEAR, year);
            datetime.set(Calendar.MONTH, month);
            datetime.set(Calendar.DAY_OF_MONTH, day);
            String strDateToShow = (datetime.get(Calendar.MONTH)+1) + "/"
                    + datetime.get(Calendar.DAY_OF_MONTH) + "/"
                    + datetime.get(Calendar.YEAR);
            dateText.setText(strDateToShow);
        }
    }


    public void buttonMaker() {
        Events e = new Events();

        ParseQuery<Events> query = e.getQuery();
        //query.
        query.addAscendingOrder("Date");
        query.findInBackground(new FindCallback<Events>() {

            public void done(List<Events> event, ParseException e) {

                if (e == null) {
                    for (int i = 0; i < event.size(); i++) {
                        //event.get(i).fetchIfNeeded();

                        events.add(event.get(i));
                        //Toast.makeText(getApplicationContext(), events.get(i).getCat().getName() + "", Toast.LENGTH_SHORT).show();

                        Toast.makeText(getApplicationContext(), event.get(i).getCat().getName() + "", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Nope", Toast.LENGTH_SHORT).show();
                }

                populateList();

            }

        });



    }
    private class EventListAdapter extends ArrayAdapter<Events> {
        int viewListXML;
        ArrayList<Events> eventArray;

        public EventListAdapter(int viewListXML, ArrayList<Events> eventArray){//Example R.layout.event_list_item, events
            super(MainActivity.this, viewListXML, eventArray);
            this.viewListXML = viewListXML;
            this.eventArray = eventArray;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null)
                view = getLayoutInflater().inflate(viewListXML, parent, false);

            Events currentEvent = eventArray.get(position);
            TextView category = (TextView) view.findViewById(R.id.eventCategoryView);
            category.setText(currentEvent.getCat().getName());
            TextView capacity = (TextView) view.findViewById(R.id.eventCapacityView);
            capacity.setText(currentEvent.getMax()+"");




            return view;

        }

    }


    /*
    private class RemoteDataTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Events");
            query.orderByAscending("Date");
            try{
                ob = query.find();
            }catch(ParseException e){

            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            eventListView = (ListView) findViewById(R.id.listView);
            adapter = new ArrayAdapter<Events>(MainActivity.this, R.layout.event_list_view);

            for(ParseObject allEvents : ob){

                adapter.add((Events) allEvents);//changed
            }

            eventListView.setAdapter(adapter);

            eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Intent i = new Intent(MainActivity.this, ) Needs alan's single event page
                   // Toast.makeText(getApplicationContext(), "Clicked at position "+position, Toast.LENGTH_SHORT).show();
                }
            });

        }

    }
    */

    public void createEvent(View view) {

        EditText temp;
        TextView blah;

        temp = (EditText) findViewById(R.id.maxMembersInt);
        int maxMember = Integer.parseInt(temp.getText().toString());

        temp = (EditText) findViewById(R.id.createEventInfo);
        String eventInfo = temp.getText().toString();

        ParseObject gameScore = ParseObject.create("Events");

        gameScore.put("Max", maxMember);
        gameScore.put("Description", eventInfo);

        gameScore.saveInBackground();
    }

    public void eventSearch(View view) {
        EditText temp;

        //Activity
        temp = (EditText) findViewById(R.id.editText3);
        String activity = temp.getText().toString();

        /**
        //Location
        temp = (EditText) findViewById(R.id.editText4);
        String location = temp.getText().toString(); */


        //Toast.makeText(getApplicationContext(), searchEvents(null, "Sports").size() + "", Toast.LENGTH_SHORT).show();
        searchEvents(null, activity);
        testSearchList(ev);

        ev = new ArrayList<>();

    }

    public void searchEvents(Date d, String category) {
        Events e = new Events();
        date = d;
        cater = category;

        ParseQuery<Events> query = e.getQuery();
        query.addAscendingOrder("Date");
        query.findInBackground(new FindCallback<Events>() {

            public void done(List<Events> event, ParseException e) {

                if (e == null) {
                    for (int i = 0; i < event.size(); i++) {
                        //event.get(i).fetchIfNeeded();
                        //Toast.makeText(getApplicationContext(), event.get(i).getCat().getName() + "", Toast.LENGTH_SHORT).show();
                        if(date != null) {
                            if(date.equals(event.get(i).getDate())) {
                                ev.add(event.get(i));
                            }
                        }
                        else if(cater != null) {
                            if (cater.equals(event.get(i).getCat().getName())) {
                                ev.add(event.get(i));
                                //Toast.makeText(getApplicationContext(), searchCategories.size()+"", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }

                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }

            }

        });

    }


}
