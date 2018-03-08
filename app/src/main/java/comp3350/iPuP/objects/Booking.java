package comp3350.iPuP.objects;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ThanhTran on 2018-03-05.
 */

public class Booking
{
    private String username; //the unique username of each user
    private String timeSlotId;
    private String address;
    private Date start;
    private Date end;
    private DateFormatter dateFormatter = new DateFormatter();

    public Booking(String username, String timeSlotId, String address, Date start, Date end)
    {
        this.username = username;
        this.timeSlotId = timeSlotId;
        this.address = address;
        this.start = start;
        this.end = end;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(String timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String toString()
    {
        return "\n" + this.address + " (hold to cancel this booking) \n" + dateFormatter.getDateTimeFormat().format(start) + " - " + dateFormatter.getDateTimeFormat().format(end) + "\n";
    }
}

