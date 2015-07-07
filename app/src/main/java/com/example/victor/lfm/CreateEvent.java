package com.example.victor.lfm;

import android.app.Activity;
import android.app.FragmentManager;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 6/10/2015.
 */
public class CreateEvent extends FragmentActivity {

    CreateTab createTab;
    FragmentTransaction transaction;
    Fragment fragment;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        fragment = CreateTab.newInstance(CreateEvent.this);
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
