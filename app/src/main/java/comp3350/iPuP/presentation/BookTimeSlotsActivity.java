package comp3350.iPuP.presentation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

import comp3350.iPuP.R;
import comp3350.iPuP.business.AccessParkingSpots;
import comp3350.iPuP.objects.DAOException;
import comp3350.iPuP.objects.ParkingSpot;
import comp3350.iPuP.objects.TimeSlot;

public class BookTimeSlotsActivity extends AppCompatActivity {
    String testSPOTIDFORSCREEN; //TODO: Make the ID come from previous screen instead
    ParkingSpot currSpot;
    private AccessParkingSpots accessParkingSpots=new AccessParkingSpots();
    ArrayList<TimeSlot> timesToShow;
    ArrayList<TimeSlot> bookedSlots=new ArrayList<TimeSlot>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_time_slots);

        testSPOTIDFORSCREEN="88 Plaza Drivemarker";

        try
        {
            currSpot=accessParkingSpots.getSpotBYID(testSPOTIDFORSCREEN);
            timesToShow = accessParkingSpots.getFreeTimeSlotsByID(testSPOTIDFORSCREEN);
        }catch (DAOException exd){
            Toast.makeText(this, exd.getMessage(), Toast.LENGTH_LONG).show();
        }

        TextView hostName=(TextView)findViewById(R.id.parkingSpotHostName);
        hostName.setText(currSpot.getName());
        TextView spotAddress=(TextView)findViewById(R.id.parkingSpotAddress);
        spotAddress.setText(currSpot.getAddress());
        TextView hostPhoneNumber=(TextView)findViewById(R.id.parkingSpotPhoneNumber);
        hostPhoneNumber.setText(currSpot.getPhone());
        TextView hostEmailAddress=(TextView)findViewById(R.id.parkingSpotHostEmail);
        hostEmailAddress.setText(currSpot.getEmail());
        TextView rate=(TextView)findViewById(R.id.parkingSpotChargeRate);
        rate.setText(Double.toString(currSpot.getRate()));

        ListView tSlots=(ListView)findViewById(R.id.timeSlotsList);
        tSlots.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ArrayAdapter<TimeSlot> timeListAdapter=new ArrayAdapter<TimeSlot>(this,
                R.layout.time_slot_item_layout,R.id.timeSlotCheckItem, timesToShow);
        tSlots.setAdapter(timeListAdapter);
        tSlots.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TimeSlot currSlot= timesToShow.get(i);
                if (bookedSlots.contains(currSlot)){
                    bookedSlots.remove(currSlot);
                }else{
                    bookedSlots.add(currSlot);
                }

            }
        });
    }


    public void buttonPerform(View v){
        bookSelectedSlotsInDB(bookedSlots);
    }


    public boolean bookSelectedSlotsInDB(ArrayList<TimeSlot> theArray){
        //TODO: Add the full functionality for this function!
        if(theArray.size()>0) {

            Toast.makeText(this, theArray.get(0).toString(), Toast.LENGTH_LONG).show();
        }
        return true;
    }
}
