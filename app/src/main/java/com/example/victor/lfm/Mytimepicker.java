package com.example.victor.lfm;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Victor on 3/18/2015.
 */
public class Mytimepicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    TextView timeTxt;
    Calendar datetime;
    Context context;
    public Mytimepicker(TextView txtview, Calendar datetime) {
        timeTxt = txtview;
        this.datetime = datetime;
    }

    public Mytimepicker() {

    }
    public static Mytimepicker newInstance(TextView tv, Calendar datetime){
        Mytimepicker f = new Mytimepicker(tv, datetime);
        return f;
    }
    public void setContext(Context context){
        this.context = context;
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
        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        datetime.set(Calendar.MINUTE, minute);
        if (datetime.get(Calendar.AM_PM) == Calendar.AM)
            am_pm = "AM";
        else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
            am_pm = "PM";
        String strHrsToShow = (datetime.get(Calendar.HOUR) == 0) ? "12" : datetime.get(Calendar.HOUR) + "";
        String minStr = String.format("%02d", datetime.get(Calendar.MINUTE));
        timeTxt.setText(strHrsToShow + ":" + minStr + " " + am_pm);
        Toast.makeText(context.getApplicationContext(), "Time is: "+datetime.getTime(), Toast.LENGTH_SHORT).show();

    }
}