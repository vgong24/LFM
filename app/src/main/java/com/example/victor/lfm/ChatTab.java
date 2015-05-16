package com.example.victor.lfm;

import android.support.v4.app.Fragment;import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Victor on 5/15/2015.
 */
public class ChatTab extends Fragment{
    Context context;

    public ChatTab(Context context){
        this.context = context;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.tab_2,container,false);
        TextView tv = (TextView)v.findViewById(R.id.textView);
        tv.setText("It worked!");
        return v;
    }

}
