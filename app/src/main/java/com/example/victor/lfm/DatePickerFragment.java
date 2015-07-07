package com.example.victor.lfm;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by Victor on 3/18/2015.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    TextView dateText;
    Calendar datetime;

    public DatePickerFragment(){

    }


    public static DatePickerFragment newInstance(TextView tv, Calendar datetime){
        DatePickerFragment f = new DatePickerFragment();
        f.dateText = tv;
        f.datetime = datetime;
        return f;
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
        datetime.set(Calendar.YEAR, year);
        datetime.set(Calendar.MONTH, month);
        datetime.set(Calendar.DAY_OF_MONTH, day);

        String strDateToShow = (datetime.get(Calendar.MONTH)+1) + "/"
                + datetime.get(Calendar.DAY_OF_MONTH) + "/"
                + datetime.get(Calendar.YEAR);
        dateText.setText(strDateToShow);
    }
}