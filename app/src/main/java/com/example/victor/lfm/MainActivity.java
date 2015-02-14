package com.example.victor.lfm;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.parse.Parse;
import com.parse.ParseUser;
import com.parse.*;

import java.util.Calendar;
import java.util.*;


public class MainActivity extends ActionBarActivity {

    Button logOut = null;
    private static final int TIME_DIALOG_ID = 0 ;
    TextView timeView;
    Button timeBtn;
    private TextView timeText;

    ArrayList<Category> categories = new ArrayList<Category>();
    ArrayList<Date> dates = new ArrayList<Date>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTabs();

        timeView = (TextView) findViewById(R.id.timeView);
        timeBtn = (Button) findViewById(R.id.timeBtn);
        timeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new Mytimepicker(timeView);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

        logOut = (Button) findViewById(R.id.logout_btn);
        logOut.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                startActivity(new Intent(v.getContext(), DispatchActivity.class));
            }
        });

        buttonMaker();
        //Toast.makeText(getApplicationContext(), categories.get(1).getCat(), Toast.LENGTH_SHORT).show();
        /*
        for (Category c: categories) {
            Toast.makeText(getApplicationContext(), c.getCat(), Toast.LENGTH_SHORT).show();

        }*/


    }

    public void initFields(){

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

    private void initTabs(){
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

        public Mytimepicker(TextView txtview){
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
            String strHrsToShow = (datetime.get(Calendar.HOUR) == 0) ?"12":datetime.get(Calendar.HOUR)+"";
            timeTxt.setText(strHrsToShow + ":" + datetime.get(Calendar.MINUTE)+" " + am_pm);
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
                    for(int i = 0; i < event.size(); i++) {
                        //event.get(i).fetchIfNeeded();
                        Toast.makeText(getApplicationContext(), event.get(i).getCat().getName() + "", Toast.LENGTH_SHORT).show();
                        categories.add(event.get(i).getCat());
                        dates.add(event.get(i).getDate());
                    }
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }

            }

        });

    }

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

}
