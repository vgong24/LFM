package com.bowen.victor.ciya.activities;

/**
 * Created by Victor on 4/6/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bowen.victor.ciya.adapters.EventListAdapter;
import com.bowen.victor.ciya.dbHandlers.FriendListDBHandler;
import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.fragments.FragmentDrawer;
import com.bowen.victor.ciya.services.MessageServiceV2;
import com.bowen.victor.ciya.slidingtab.SlidingTabLayout;
import com.bowen.victor.ciya.slidingtab.ViewPagerAdapter;
import com.bowen.victor.ciya.structures.Attendee;
import com.bowen.victor.ciya.structures.Events;
import com.bowen.victor.ciya.tools.WorkAround;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity_v2 extends ActionBarActivity implements FragmentDrawer.FragmentDrawerListener{

    // Declaring Your View and Variables

    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    FragmentDrawer drawerFragment;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"Events","Party Chat", "Friends"};
    int Numboftabs = Titles.length;
    public static FragmentManager fragmentManager;
    private ProgressDialog progressDialog;
    private static BroadcastReceiver receiver = null;
    static Intent serviceIntent;
    ParseUser currentUser;
    Handler threadHandler;

    ArrayList<Events> invitedEvents;
    CheckForInvites thread;

    private GoogleApiClient mGoogleApiClient;
    GoogleCloudMessaging gcm;

    public static boolean runThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);
        WorkAround.setNotificationBarColor(this, R.color.colorPrimaryDark);
        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());

        currentUser = ParseUser.getCurrentUser();
        if(invitedEvents == null){
            invitedEvents = new ArrayList<>();
        }

        //Connect with sinch services
        showSpinner();
        sinchConnect();

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //Setting up DrawerFragment
        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
        drawerFragment.setDrawerListener(this);


        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs, MainActivity_v2.this);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {
                //When moving between pages, recreate options menu
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        switch(pager.getCurrentItem()){
            case 0:
                getMenuInflater().inflate(R.menu.main_events, menu);
                break;
            case 1:
                getMenuInflater().inflate(R.menu.main_chat, menu);
                break;

            default:
                getMenuInflater().inflate(R.menu.main_friends, menu);
                break;
        }
        //start with no invites

        MenuItem item = menu.findItem(R.id.view_invites_action);
        item.setVisible(false);
        if(invitedEvents.size() > 0){
            item.setVisible(true);
        }
        //menu.findItem(R.id.emptyFriendDB).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.view_invites_action){
            //Display all invited events
            invitesClick();
        }

        if(id == R.id.create_event){
            Intent intent = new Intent(getApplicationContext(), CreateEvent.class);
            startActivity(intent);

        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout_action) {
            //basically on destroy
            stopService(serviceIntent);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            receiver = null;

            //Delete friends from db
            FriendListDBHandler db = new FriendListDBHandler(getApplicationContext());
            db.deleteDatabase();

            ParseUser.logOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity_v2.class);
            finish();
            startActivity(intent);
            return true;
        }
        //Empties database (for testing)
        if(id == R.id.emptyFriendDB){
            /*
            FriendListDBHandler db = new FriendListDBHandler(getApplicationContext());
            db.deleteDatabase();
            //db.dropDatabase();
            */
            Intent intent = new Intent(getApplicationContext(), ProfileSettings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    //Click invites button
    public void invitesClick(){
        //Create alert dialog of list
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invitations");

        EventListAdapter eventListAdapter = new EventListAdapter(MainActivity_v2.this, R.layout.event_item_reddit, invitedEvents);
        builder.setAdapter(eventListAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EventDetails.startEventDetails(MainActivity_v2.this, invitedEvents.get(which));
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    //show a loading spinner while the sinch client starts
    private void showSpinner() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean success = intent.getBooleanExtra("success", false);
                progressDialog.dismiss();
                if (!success) {
                    Toast.makeText(getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("com.bowen.victor.ciya.activities.MainActivity_v2"));
    }

    //shut off sinch client
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(serviceIntent!= null)
            stopService(serviceIntent);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        receiver = null;
        thread.cancel(true);
    }

    @Override
    public void onResume(){
        super.onResume();
        runThread = true;
        thread = new CheckForInvites();
        thread.execute();
    }

    @Override
    public void onPause(){
        super.onPause();
        runThread = false;
        //stop thread
    }

    @Override
    public void onBackPressed(){
        if(drawerFragment.isVisible()){
            drawerFragment.closeDrawer();
        }else{
            super.onBackPressed();
        }
    }



    public void sinchConnect(){
        //new RegisterGcmTask().execute();
        serviceIntent = new Intent(getApplicationContext(), MessageServiceV2.class);
        startService(serviceIntent);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        Log.v("DrawerSelect", "Drawer item: "+ position);
    }

    /* Every few minutes check for invitations
    * After it runs it will either show or remove the invites icon
    * once complete, check again in 5 minutes (if the user is still on this page)
    * Back on resume, run it again to check for quick updates.
    */
    class CheckForInvites extends AsyncTask<Void, Void, List<Events>>{


        @Override
        protected void onPreExecute(){
            invitedEvents.clear();
        }

        @Override
        protected List<Events> doInBackground(Void... params) {
            //Look for invites in background
            ParseQuery<Attendee> query = ParseQuery.getQuery("Attendees");
            query.whereEqualTo("User", currentUser);
            query.whereEqualTo("inviteStatus", Attendee.INVITED);
            try {
                List<Attendee> list = query.find();
                for(Attendee eventAttendee: list){
                    Events events = eventAttendee.getEventObject().fetchIfNeeded();
                    invitedEvents.add(events);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            return invitedEvents;
        }

        @Override
        protected void onPostExecute(List invitedList){
            //Redisplay the invites action icon and reruns the check for 5 minutes
            invalidateOptionsMenu();
            
            if(runThread){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(runThread) {
                            thread = new CheckForInvites();
                            thread.execute();
                        }
                    }
                }, 5* 60 * 1000);

            }

        }
    }
    //Log out of current user and return to login page
    public static void logOut(Context context){
        if(serviceIntent != null)
            context.stopService(serviceIntent);


        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        receiver = null;

        //Delete friends from db
        FriendListDBHandler db = new FriendListDBHandler(context);
        db.deleteDatabase();

        ParseUser.logOut();
        Intent intent = new Intent(context, LoginActivity_v2.class);
        Activity activity = (Activity) context;
        activity.finish();
        context.startActivity(intent);
    }
    //Confirm logout dialog
    public static void logOutConfirm(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sign out of Ciya?");
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logOut(context);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    class RegisterGcmTask extends AsyncTask<Void, Void, String> {
        String msg = "";
        @Override
        protected String doInBackground(Void... voids) {
            try {
                msg = gcm.register("372417304699");
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
            }
            return msg;
        }
        @Override
        protected void onPostExecute(String msg) {
            serviceIntent = new Intent(getApplicationContext(), MessageServiceV2.class);
            serviceIntent.putExtra("regId", msg);
            Log.v("gmcID", msg);
            startService(serviceIntent);
        }
    }

}