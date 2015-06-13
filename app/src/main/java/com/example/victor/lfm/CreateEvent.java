package com.example.victor.lfm;

import android.app.Activity;
import android.app.FragmentManager;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Victor on 6/10/2015.
 */
public class CreateEvent extends FragmentActivity {

    CreateTab createTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        Fragment fragment = new CreateTab(CreateEvent.this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.createFrame, fragment);
        transaction.commit();



    }


}
