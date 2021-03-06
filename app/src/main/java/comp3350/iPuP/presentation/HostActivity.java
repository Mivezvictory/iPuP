package comp3350.iPuP.presentation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.app.DialogFragment;
import android.widget.ToggleButton;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import comp3350.iPuP.R;
import comp3350.iPuP.objects.DAOException;
import comp3350.iPuP.objects.DateFormatter;
import comp3350.iPuP.business.AccessParkingSpots;
import comp3350.iPuP.objects.ParkingSpot;
import comp3350.iPuP.objects.TimeSlot;

public class HostActivity extends Activity implements DateFragmentObserver
{
    protected AccessParkingSpots accessParkingSpots;
    private String repetitionInfo;
    protected String name;
    protected DateFormatter df;

    boolean dateFrom;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        accessParkingSpots = new AccessParkingSpots();

        repetitionInfo = "";

        name = getIntent().getStringExtra(getResources().getString(R.string.extra_name));

        Calendar c = Calendar.getInstance();
        df = new DateFormatter();

        if (c.get(Calendar.MINUTE) > 30)
            c.set(Calendar.MINUTE, 30);
        else c.set(Calendar.MINUTE, 0);

        TextView tv = findViewById(R.id.textViewFromDate);
        tv.setText(df.getDateFormat().format(c.getTime()));

        tv = findViewById(R.id.textViewFromTime);
        tv.setText(df.getTimeFormat().format(c.getTime()));

        c.add(Calendar.HOUR_OF_DAY,1);
        tv = findViewById(R.id.textViewToDate);
        tv.setText(df.getDateFormat().format(c.getTime()));

        tv = findViewById(R.id.textViewToTime);
        tv.setText(df.getTimeFormat().format(c.getTime()));
    }

    public void onFromDateClick(View v)
    {
        dateFrom = true;
        DialogFragment dateFragment = DatePickerFragment.newInstance();
        dateFragment.show(getFragmentManager(),"DatePicker");
    }

    public void onToDateClick(View v)
    {
        dateFrom = false;
        DialogFragment dateFragment = DatePickerFragment.newInstance();
        dateFragment.show(getFragmentManager(),"DatePicker");
    }

    public void onFromTimeClick(View v)
    {
        Intent fromTimeIntent = new Intent(HostActivity.this, TimePickerActivity.class);
        HostActivity.this.startActivityForResult(fromTimeIntent, 1);
    }

    public void onToTimeClick(View v)
    {
        Intent toTimeIntent = new Intent(HostActivity.this, TimePickerActivity.class);
        HostActivity.this.startActivityForResult(toTimeIntent, 2);
    }

    public void onRepeatClick(View v)
    {
        if (((ToggleButton)v).isChecked()) {
            Intent repeatIntent = new Intent(HostActivity.this, RepeatActivity.class);
            TextView dateFrom = findViewById(R.id.textViewFromDate);
            repeatIntent.putExtra(getResources().getString(R.string.extra_date), dateFrom.getText());
            HostActivity.this.startActivityForResult(repeatIntent, 0);
        }
        else
        {
            repetitionInfo = "";
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void buttonConfirmOnClick(View v)
    {
        EditText edit = findViewById(R.id.editTextAddress);
        String address = edit.getText().toString();
        edit = findViewById(R.id.editTextEmail);
        String email = edit.getText().toString();
        edit = findViewById(R.id.editTextPhone);
        String phone = edit.getText().toString();
        edit = findViewById(R.id.editTextRate);
        String rateStr = edit.getText().toString();
        Double rate = Double.parseDouble(rateStr.equals("") ? "0": rateStr);

        boolean valid = true;

        TextView tv;
        tv = findViewById(R.id.textViewFromDate);
        String fromStr = tv.getText().toString();
        tv = findViewById(R.id.textViewFromTime);
        fromStr += ", " + tv.getText().toString();
        tv = findViewById(R.id.textViewToDate);
        String toStr = tv.getText().toString();
        tv = findViewById(R.id.textViewToTime);
        toStr += ", " + tv.getText().toString();
        TimeSlot timeSlot = null;
        try
        {
            timeSlot = TimeSlot.parseString(fromStr + " - " + toStr);
        }
        catch (ParseException e)
        {
            valid = false;
        }

        if (address.equals(""))
        {
            valid = false;
            EditText text = findViewById(R.id.editTextAddress);
            text.setBackgroundColor(getResources().getColor(R.color.colorWarning));
        }
        else
        {
            EditText text = findViewById(R.id.editTextAddress);
            text.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        }

        if (rate == 0)
        {
            valid = false;
            EditText text = findViewById(R.id.editTextRate);
            text.setBackgroundColor(getResources().getColor(R.color.colorWarning));
        }
        else
        {
            EditText text = findViewById(R.id.editTextRate);
            text.setBackgroundColor(getResources().getColor(R.color.colorLightGrey));
        }

        if (email.equals("") && phone.equals(""))
        {
            valid = false;
            EditText text = findViewById(R.id.editTextPhone);
            text.setHint("Enter either phone");
            text.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            text = findViewById(R.id.editTextEmail);
            text.setHint("or email");
            text.setBackgroundColor(getResources().getColor(R.color.colorWarning));
        }
        else
        {
            EditText text = findViewById(R.id.editTextPhone);
            text.setHint(getResources().getString(R.string.host_phone));
            text.setBackgroundColor(getResources().getColor(R.color.colorLightGrey));
            text = findViewById(R.id.editTextEmail);
            text.setHint(getResources().getString(R.string.host_email));
            text.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        }

        if (!ParkingSpot.validateEmail(email))
        {
            valid = false;
            EditText text = findViewById(R.id.editTextEmail);
            text.setText("Invalid Email address");
            text.setTextColor(getResources().getColor(R.color.colorBlack));
        }
        else
        {
            EditText text = findViewById(R.id.editTextEmail);
            text.setHint(getResources().getString(R.string.host_email));
            text.setTextColor(getResources().getColor(R.color.colorBlack));
        }

        if (!ParkingSpot.validatePhone(phone))
        {
            valid = false;
            EditText text = findViewById(R.id.editTextPhone);
            text.setText("Invalid phone number");
            text.setTextColor(getResources().getColor(R.color.colorBlack));
        }
        else
        {
            EditText text = findViewById(R.id.editTextPhone);
            text.setHint(getResources().getString(R.string.host_phone));
            text.setTextColor(getResources().getColor(R.color.colorBlack));
        }

        if (timeSlot.getStart().compareTo(timeSlot.getEnd()) >= 0)
        {
            valid = false;
            TextView text = findViewById(R.id.textViewFromTime);
            text.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            text = findViewById(R.id.textViewFromDate);
            text.setBackgroundColor(getResources().getColor(R.color.colorWarning));
        }
        else
        {
            TextView text = findViewById(R.id.textViewFromTime);
            text.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            text = findViewById(R.id.textViewFromDate);
            text.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        }

        if (valid)
        {
            try
            {
                accessParkingSpots.insertParkingSpot(name, timeSlot, repetitionInfo, address, phone, email, rate, latitude, longitude);

                    Toast.makeText(this, "New advertisement created!", Toast.LENGTH_LONG).show();
            }
            catch (DAOException daoe)
            {
                Toast.makeText(this, daoe.getMessage(), Toast.LENGTH_LONG).show();
            }

            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        String ret;
        switch (requestCode)
        {
            case (0) :
                if (resultCode == Activity.RESULT_OK)
                    repetitionInfo = data.getStringExtra(getResources().getString(R.string.extra_repetitionInfo));
                else
                {
                    repetitionInfo = "";
                    ((ToggleButton)findViewById(R.id.toggleButtonRepeat)).setChecked(false);
                }
                break;
            case (1):
                if (resultCode == Activity.RESULT_OK)
                {
                    ret = data.getStringExtra(getResources().getString(R.string.extra_time));
                    ((TextView) findViewById(R.id.textViewFromTime)).setText(ret);
                }
                break;
            case (2):
                if (resultCode == Activity.RESULT_OK)
                {
                    ret = data.getStringExtra(getResources().getString(R.string.extra_time));
                    TextView textView = findViewById(R.id.textViewToTime);
                    textView.setText(ret);
                }
                break;
            case (3):
                if (resultCode == Activity.RESULT_OK)
                {
                    longitude = data.getDoubleExtra(getResources().getString(R.string.extra_long), 0);
                    latitude = data.getDoubleExtra(getResources().getString(R.string.extra_lat), 0);
                }
                break;
        }
    }

    public void buttonCancelOnClick(View view)
    {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void update(Date date)
    {
        TextView tv;
        if (dateFrom)
            tv = findViewById(R.id.textViewFromDate);
        else
            tv = findViewById(R.id.textViewToDate);
        tv.setText(df.getDateFormat().format(date));
    }

    public void onMapClick(View view)
    {
        Intent mapIntent = new Intent(HostActivity.this, HostMapActivity.class);
        HostActivity.this.startActivityForResult(mapIntent, 3);
    }

    public void onMapCancelClick(View view)
    {
        latitude = 0;
        longitude = 0;
    }
}
