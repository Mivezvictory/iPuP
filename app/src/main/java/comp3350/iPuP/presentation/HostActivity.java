package comp3350.iPuP.presentation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import comp3350.iPuP.R;
import comp3350.iPuP.objects.ParkingSpot;
import comp3350.iPuP.business.AccessParkingSpots;

public class HostActivity extends Activity
{
    private AccessParkingSpots accessParkingSpots;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        accessParkingSpots = new AccessParkingSpots();
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void buttonConfirmOnClick(View v)
    {
        EditText edit =  (EditText) findViewById(R.id.editName);
        String name = edit.getText().toString();
        edit =  (EditText) findViewById(R.id.editAddress);
        String address = edit.getText().toString();
        edit =  (EditText) findViewById(R.id.editEmail);
        String email = edit.getText().toString();
        edit =  (EditText) findViewById(R.id.editPhone);
        String phone = edit.getText().toString();
        Double rate = 0.0;
        DatePicker datePicker =  (DatePicker) findViewById(R.id.datePickerDate);
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();
        TimePicker timePickerStart =  (TimePicker) findViewById(R.id.timePickerStart);
        int hour = timePickerStart.getHour();
        int minute = timePickerStart.getMinute();
        LocalDateTime start = LocalDateTime.of(year, month+1, day+1, hour, minute);
        TimePicker timePickerEnd =  (TimePicker) findViewById(R.id.timePickerEnd);
        hour = timePickerEnd.getHour();
        minute = timePickerEnd.getMinute();
        LocalDateTime end = LocalDateTime.of(year, month+1, day+1, hour, minute);

        ParkingSpot newParkingSpot = new ParkingSpot(start,end,address,name,phone,email,rate);
        String rtn = accessParkingSpots.insertParkingSpot(newParkingSpot);

        if (rtn == null)
        {
            Toast.makeText(this, "New advertisement created!", Toast.LENGTH_LONG).show();
        } else
        {
            Toast.makeText(this, "Failed to create new advertisement!", Toast.LENGTH_LONG).show();
        }

        finish();
    }
}