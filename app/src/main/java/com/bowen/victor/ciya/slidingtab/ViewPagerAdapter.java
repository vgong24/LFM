package com.bowen.victor.ciya.slidingtab;

/**
 * Created by Victor on 4/6/2015.
 */
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bowen.victor.ciya.fragments.ChatTab;
import com.bowen.victor.ciya.fragments.CreateTab;
import com.bowen.victor.ciya.fragments.FriendsTab;
import com.bowen.victor.ciya.fragments.HomeTab;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by hp1 on 21-01-2015.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    Context context;
    GoogleApiClient mGoogleApiClient;
    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created
    //TABS
    HomeTab home;
    CreateTab create;
    ChatTab chat;
    FriendsTab ftab;

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
                //Show Event List
                if(home == null){
                    home = HomeTab.newInstance(context);
                }
                return home;

            case 1:
                //Show Chat rooms
                if(chat == null){
                    chat = ChatTab.newInstance(context);
                }
                return chat;


            case 2:
                //Show Friends
                if(ftab == null){
                    ftab = FriendsTab.newInstance(context);
                }
                return ftab;


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

    @Override
    public int getItemPosition(Object object){
        return POSITION_NONE;
    }
}