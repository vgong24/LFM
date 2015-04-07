package com.example.victor.lfm;

/**
 * Created by Victor on 4/6/2015.
 */
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by hp1 on 21-01-2015.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    Context context;
    GoogleApiClient mGoogleApiClient;
    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb, Context context) {
        super(fm);
        this.context = context;
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }
    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }
    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb, Context context, GoogleApiClient mGoogleApiClient) {
        super(fm);
        this.context = context;
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
        this.mGoogleApiClient = mGoogleApiClient;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0:
                HomeTab home = new HomeTab(context);
                return home;
            case 1:
                Tab2 tab2 = new Tab2();
                return tab2;
            case 2:
                CreateTab createTab = new CreateTab(context, mGoogleApiClient);
                return createTab;

            default:
                break;

        }
    return null;

    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}