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
import comp3350.iPuP.objects.ReservationTime;

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
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        int startHour,startMinute,endHour,endMinute;

        EditText edit =  (EditText) findViewById(R.id.editName);
        String name = edit.getText().toString();
        edit =  (EditText) findViewById(R.id.editAddress);
        String address = edit.getText().toString();
        edit =  (EditText) findViewById(R.id.editEmail);
        String email = edit.getText().toString();
        edit =  (EditText) findViewById(R.id.editPhone);
        String phone = edit.getText().toString();
        edit = (EditText) findViewById(R.id.editRate);
        Double rate = Double.parseDouble(edit.getText().toString());

        DatePicker datePicker =  (DatePicker) findViewById(R.id.datePickerDate);
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        TimePicker timePickerStart =  (TimePicker) findViewById(R.id.timePickerStart);
        TimePicker timePickerEnd =  (TimePicker) findViewById(R.id.timePickerEnd);

        if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
        {
            startHour = timePickerStart.getHour();
            startMinute = timePickerStart.getMinute();
            endHour = timePickerEnd.getHour();
            endMinute = timePickerEnd.getMinute();
        } else
        {
            startHour = timePickerStart.getCurrentHour();
            startMinute = timePickerStart.getCurrentMinute();
            endHour = timePickerEnd.getCurrentHour();
            endMinute = timePickerEnd.getCurrentMinute();
        }

        ReservationTime reservationTime = new ReservationTime(year,month,day,startHour,startMinute,endHour,endMinute);

        ParkingSpot newParkingSpot = new ParkingSpot(reservationTime,address,name,phone,email,rate);
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
