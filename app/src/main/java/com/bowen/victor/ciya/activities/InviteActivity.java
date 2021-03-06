package com.bowen.victor.ciya.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ProgressBar;

import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.fragments.InviteFragment;
import com.bowen.victor.ciya.structures.Attendee;
import com.bowen.victor.ciya.tools.WorkAround;

import java.util.ArrayList;

/**
 * Created by Victor on 7/9/2015.
 */
public class InviteActivity extends FragmentActivity {
    FragmentTransaction transaction;
    Fragment fragment;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        WorkAround.setNotificationBarColor(this, R.color.colorPrimaryDark);

        Intent prevIntent = getIntent();
        String eventID = prevIntent.getStringExtra("EventID");

        fragment = InviteFragment.newInstance(InviteActivity.this, eventID);
        progressBar = (ProgressBar) findViewById(R.id.chatRoomProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        transaction = getSupportFragmentManager().beginTransaction();
        new SetUpBackground().execute();


    }

    private class SetUpBackground extends AsyncTask<Void, Void, Void> {
        ArrayList<Attendee> attendeesArr;

        @Override
        protected Void doInBackground(Void... params) {

            transaction.replace(R.id.createFrame, fragment);
            return null;
        }

        @Override
        protected void onPostExecute(Void none){
            progressBar.setVisibility(View.GONE);
            transaction.commit();
        }
    }


}
