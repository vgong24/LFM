package com.example.victor.lfm;

/**
 * Created by Victor on 4/6/2015.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by hp1 on 21-01-2015.
 */
public class Tab2 extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.tab_2,container,false);
        TextView tv = (TextView)v.findViewById(R.id.textView);
        tv.setText("It worked!");
        return v;
    }
}