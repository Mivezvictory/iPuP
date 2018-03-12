package comp3350.iPuP.persistence;

import org.hsqldb.Types;
import org.w3c.dom.DOMException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import comp3350.iPuP.objects.Booking;
import comp3350.iPuP.objects.DAOException;
import comp3350.iPuP.objects.DateFormatter;
import comp3350.iPuP.objects.ParkingSpot;
import comp3350.iPuP.objects.TimeSlot;

public class DataAccessObject implements DataAccess
{
	private PreparedStatement pstmt, pstmt2, pstmt3;
	private Statement stmt;
	private Connection con;
	private ResultSet rss, rsp;

	private String dbName;
	private String dbType;

    private DateFormatter df = new DateFormatter();

    private ArrayList<Booking> bookingSpotsOfAUser;

	private ArrayList<ParkingSpot> parkingSpots;
    private ArrayList<ParkingSpot> parkingSpotsOfAUser;

	private String cmdString;
	private int updateCount;
	private static String EOF = "  ";


	public DataAccessObject(String dbName)
	{
		this.dbName = dbName;
	}


	public void open(String dbPath) throws DAOException
	{
		String url;
		try
		{
			// Setup for HSQL
			dbType = "HSQL";
			Class.forName("org.hsqldb.jdbcDriver").newInstance();
			url = "jdbc:hsqldb:file:" + dbPath; // stored on disk mode
			con = DriverManager.getConnection(url, "iPuP", "iPuP");
			stmt = con.createStatement();
		}
		catch (Exception e)
		{
			processSQLError(e);
			throw new DAOException("Error in opening "+dbType+" database "+dbPath+"!",e);
		}
		System.out.println("Opened " +dbType +" database " +dbPath);
	}


	public void close() throws DAOException
	{
		try
		{	// commit all changes to the database
			cmdString = "shutdown compact";
			rss = stmt.executeQuery(cmdString);
			con.close();
		}
		catch (Exception e)
		{
			processSQLError(e);
			throw new DAOException("Error in closing "+dbType+" database "+dbName+"!",e);
		}
		System.out.println("Closed " +dbType +" database " +dbName);
	}


	private boolean doesParkingSpotExists(String address, String name) throws DAOException
    {
        boolean result = false;

        try {
            cmdString = "SELECT * FROM PARKINGSPOTS WHERE ADDRESS = ? AND NAME = ?";
            pstmt = con.prepareStatement(cmdString);
            pstmt.setString(1, address);
            pstmt.setString(2, name);

            rsp = pstmt.executeQuery();

            if (rsp.next())
            {
                result = true;
            }

            rsp.close();
        } catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in checking if ParkingSpot object with address = "+address+" and name = "+name+" exists!",sqle);
        }

        return result;
    }


	private boolean doesUserExists(String username) throws DAOException
    {
        boolean result = false;

        try
        {
            cmdString = "SELECT * FROM USERS WHERE USERNAME = ?";
            pstmt = con.prepareStatement(cmdString);
            pstmt.setString(1,username);

            rsp = pstmt.executeQuery();

            if (rsp.next())
            {
                result = true;
            }
        } catch (Exception e)
        {
            processSQLError(e);
            throw new DAOException("Error in checking if User ("+username+") exists!",e);
        }

        return result;
    }


	public long insertDaySlot(TimeSlot daySlot, long spotID) throws DAOException
    {
        long dayslotID;

        try
        {
            cmdString = "INSERT INTO DAYSLOTS (SPOT_ID,STARTDAYTIME,ENDDAYTIME,DELETED) " +
                        "VALUES (?,?,?,?)";
            pstmt2 = con.prepareStatement(cmdString);
            pstmt2.setLong(1, spotID);
            pstmt2.setString(2, df.getSqlDateTimeFormat().format(daySlot.getStart()));
            pstmt2.setString(3, df.getSqlDateTimeFormat().format(daySlot.getEnd()));
            pstmt2.setBoolean(4,false);
            updateCount = pstmt2.executeUpdate();
            checkWarning(pstmt, updateCount);
        }
        catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in inserting DaySlot object with spotID = "+spotID+"!",sqle);
        }

        try
        {
            cmdString = "CALL IDENTITY()";
            rss = stmt.executeQuery(cmdString);

            if (rss.next())
            {
                dayslotID = rss.getLong(1);
            } else
            {
                throw new DAOException("Could not retrieve last auto generated DaySlot ID!");
            }
        } catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Could not retrieve last auto generated DaySlot ID!",sqle);
        }

        return dayslotID;
    }

    public long insertTimeSlot(TimeSlot timeSlot, long daySlotID, long spotID) throws DAOException
    {
        long timeslotID;

        try
        {
            cmdString = "INSERT INTO TIMESLOTS (SPOT_ID,DAYSLOT_ID,STARTDATETIME,ENDDATETIME,DELETED) " +
                        "VALUES (?,?,?,?,?)";
            pstmt3 = con.prepareStatement(cmdString);
            pstmt3.setLong(1, spotID);
            pstmt3.setLong(2, daySlotID);
            pstmt3.setString(3, df.getSqlDateTimeFormat().format(timeSlot.getStart()));
            pstmt3.setString(4, df.getSqlDateTimeFormat().format(timeSlot.getEnd()));
            pstmt3.setBoolean(5,false);

            updateCount = pstmt3.executeUpdate();
            checkWarning(pstmt, updateCount);
        } catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in inserting TimeSlot object with dayslotID = "+daySlotID+" and slotID = "+spotID+"!",sqle);
        }

        try
        {
            cmdString = "CALL IDENTITY()";
            rss = stmt.executeQuery(cmdString);

            if (rss.next())
            {
                timeslotID = rss.getLong(1);
            } else
            {
                throw new DAOException("Could not retrieve last auto generated TimeSlot ID!");
            }
        } catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Could not retrieve last auto generated TimeSlot ID!",sqle);
        }

        return timeslotID;
    }

	public long insertParkingSpot(String username, ParkingSpot currentParkingSpot) throws DAOException
    {
        long spotID;

        try
        {
            if (!doesParkingSpotExists(currentParkingSpot.getAddress(),currentParkingSpot.getName())) {
                cmdString = "INSERT INTO PARKINGSPOTS (USERNAME,NAME,ADDRESS,PHONE,EMAIL,RATE,DELETED) VALUES(?,?,?,?,?,?,?)";
                pstmt = con.prepareStatement(cmdString);
                pstmt.setString(1, username);
                pstmt.setString(2, currentParkingSpot.getName());
                pstmt.setString(3, currentParkingSpot.getAddress());
                pstmt.setString(4, currentParkingSpot.getPhone());
                pstmt.setString(5, currentParkingSpot.getEmail());
                pstmt.setDouble(6, currentParkingSpot.getRate());
                pstmt.setBoolean(7,false);

                //System.out.println(cmdString);
                updateCount = pstmt.executeUpdate();
                checkWarning(pstmt, updateCount);
            }
        }
        catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in creating ParkingSpot object with SPOT_ID = "+currentParkingSpot.getSpotID()+" for Username: "+username+"!",sqle);
        }

        try
        {
            cmdString = "CALL IDENTITY()";
            rss = stmt.executeQuery(cmdString);

            if (rss.next())
            {
                spotID = rss.getLong(1);
            } else
            {
                throw new DAOException("Could not retrieve last auto generated Spot ID!");
            }
        } catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Could not retrieve last auto generated Spot ID!",sqle);
        }

        return spotID;
    }

    public boolean insertUser(String username) throws DAOException
    {
        boolean result = false;

        if (!doesUserExists(username)) { // if user does not exist
            try {
                cmdString = "INSERT INTO USERS VALUES(?)";
                pstmt = con.prepareStatement(cmdString);
                pstmt.setString(1, username);

                updateCount = pstmt.executeUpdate();
                checkWarning(pstmt, updateCount);

                result = true;

            } catch (SQLException sqle) {
                processSQLError(sqle);
                throw new DAOException("Error in creating new user with USERNAME = "+username+"!",sqle);
            }
        }

        return result;
    }

    //TODO: need this?
//    public ArrayList<TimeSlot> getDaySlotsForAParkingSpot(String slotID) throws DAOException
//    {
//        ArrayList<TimeSlot> daySlots = new ArrayList<TimeSlot>();
//
//        return daySlots;
//    }

    public ArrayList<ParkingSpot> getParkingSpotsByAddressDate(String address, Date date) throws DAOException
    {
        parkingSpots = new ArrayList<>();

        try {
            cmdString = "SELECT * FROM PARKINGSPOTS P WHERE LCASE(P.ADDRESS) LIKE ? " +
                        "AND EXISTS (SELECT * FROM DAYSLOTS D WHERE D.SPOT_ID = P.SPOT_ID " +
                        "AND ? BETWEEN CAST(D.STARTDAYTIME AS DATE) AND CAST(D.ENDDAYTIME AS DATE))";
            pstmt = con.prepareStatement(cmdString);

            if (address == null)
            {
                pstmt.setString(1,"%");
            } else
            {
                pstmt.setString(1,"%"+address.toLowerCase()+"%");
            }
            pstmt.setString(2, df.getSqlDateFormat().format(date));

            rss = pstmt.executeQuery();

            getParkingSpots(rss, parkingSpots);

            rss.close();
        } catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in getting ParkingSpots ordered by Date: "+df.getSqlDateFormat().format(date)+"!",sqle);
        }

        return parkingSpots;
    }



    public ParkingSpot getParkingSpot(long spotID) throws DAOException
    {
        try
        {
            cmdString = "SELECT * FROM PARKINGSPOTS P WHERE P.SPOT_ID = ? ";
            pstmt = con.prepareStatement(cmdString);

            pstmt.setLong(1, spotID);

            rss = pstmt.executeQuery();


        }
        catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in getting ParkingSpot!", sqle);
        }

        Double rate;
        ParkingSpot ps;
        String name, addr, phone, email;

        try
        {
            rss.next();
            name = rss.getString("NAME");
            addr = rss.getString("ADDRESS");
            phone = rss.getString("PHONE");
            email = rss.getString("EMAIL");
            rate = rss.getDouble("RATE");

            ps = new ParkingSpot(spotID, addr, name, phone, email, rate);
            rss.close();
        }
        catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in ParkingSpot object!",sqle);
        }
        catch (Exception e)
        {
            processSQLError(e);
            throw new DAOException("Error in creating a new ParkingSpot object!",e);
        }
        return ps;
    }

    private void getParkingSpots(ResultSet rs, ArrayList<ParkingSpot> parkingSpots) throws DAOException
    {
        Double rate;
        long spotID;
        ParkingSpot ps;
        String name, addr, phone, email;

        try {
            while (rs.next()) {
                spotID = rs.getLong("SPOT_ID");
                name = rs.getString("NAME");
                addr = rs.getString("ADDRESS");
                phone = rs.getString("PHONE");
                email = rs.getString("EMAIL");
                rate = rs.getDouble("RATE");

                ps = new ParkingSpot(spotID, addr, name, phone, email, rate);
                parkingSpots.add(ps);
            }
        } catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in getting list of ParkingSpot objects!",sqle);
        } catch (Exception e)
        {
            processSQLError(e);
            throw new DAOException("Error in creating a new ParkingSpot object!",e);
        }
    }


    public ArrayList<ParkingSpot> getHostedSpotsOfGivenUser(String username) throws DAOException
    {
        parkingSpotsOfAUser = new ArrayList<>();

        try
        {
            cmdString = "SELECT * FROM PARKINGSPOTS WHERE  USERNAME = ? AND DELETED = FALSE";
            pstmt = con.prepareStatement(cmdString);
            pstmt.setString(1, username);
            rss = pstmt.executeQuery();
            //ResultSetMetaData md = rs.getMetaData();

            getParkingSpots(rss, parkingSpotsOfAUser);

            rss.close();
        } catch (SQLException e)
        {
            processSQLError(e);
            throw new DAOException("Error in getting hosted Parking Spots by "+username+"!",e);
        }

        return parkingSpotsOfAUser;
    }

    public void clearSpotList()
    {
        this.parkingSpots.clear();
    }


	private void checkWarning(Statement st, int updateCount) throws DAOException
	{
		if (updateCount != 1)
		{
            throw new DAOException("Tuple not inserted correctly.");
		}
	}


	private void processSQLError(Exception e)
	{
		String result = "*** SQL Error: " + e.getMessage();

		// Remember, this will NOT be seen by the user!
		e.printStackTrace();
    }


	//added by Kevin
    public ArrayList<Booking> getBookedSpotsOfGivenUser(String username) throws DAOException
    {
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        Date start, end;
        Booking booking;
        long timeslotID;
        String addr;

        bookingSpotsOfAUser = new ArrayList<>();

        try
        {
            cmdString = "SELECT B.USERNAME, B.TIMESLOT_ID, P.SPOT_ID, P.ADDRESS, T.STARTDATETIME, T.ENDDATETIME " +
                        "FROM BOOKINGS B LEFT JOIN PARKINGSPOTS P ON B.SPOT_ID = P.SPOT_ID " +
                        "LEFT JOIN TIMESLOTS T ON B.TIMESLOT_ID = T.TIMESLOT_ID " +
                        "WHERE B.USERNAME = ? AND B.DELETED = FALSE " +
                            "AND NOT T.DAYSLOT_ID IS NULL ORDER BY T.STARTDATETIME";
            //TODO: remove null check on dayslotID after change in database
            pstmt = con.prepareStatement(cmdString);
            pstmt.setString(1, username);
            rss = pstmt.executeQuery();
            //ResultSetMetaData md = rs.getMetaData();

            while (rss.next())
            {
                timeslotID = rss.getLong("TIMESLOT_ID");
                addr = rss.getString("Address");
                start = rss.getTimestamp("Startdatetime");
                end = rss.getTimestamp("Enddatetime");

                calStart.setTime(start);
                calEnd.setTime(end);

                booking = new Booking(username, timeslotID, addr, calStart.getTime(), calEnd.getTime());
                bookingSpotsOfAUser.add(booking);
            }

            rss.close();
        }
        catch (Exception e)
        {
            processSQLError(e);
            throw new DAOException("Error in getting bookings list for User: "+username+"!",e);
        }

        return bookingSpotsOfAUser;
    }

    public void setBookedSpotToDeleted(String username, long timeSlotId) throws DAOException
    {
        try
        {
            cmdString = "UPDATE BOOKINGS SET DELETED = TRUE WHERE USERNAME = ? AND TIMESLOT_ID = ?";
            pstmt = con.prepareStatement(cmdString);
            pstmt.setString(1, username);
            pstmt.setLong(2, timeSlotId);
            updateCount = pstmt.executeUpdate();
            checkWarning(pstmt, updateCount);
        }
        catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in cancelling booking slot with TIMESLOT_ID = "+timeSlotId+"!",sqle);
        }
    }

    @Override
    public void modifyParkingSpot(long spotID, String address, String phone, String email, Double rate) throws DAOException
    {
        try
        {
            cmdString = "UPDATE PARKINGSPOTS SET ADDRESS=?, PHONE=?, EMAIL=?, RATE=? WHERE SPOT_ID=?";
            pstmt = con.prepareStatement(cmdString);
            pstmt.setString(1, address);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);
            pstmt.setDouble(4, rate);
            pstmt.setLong(5, spotID);
            updateCount = pstmt.executeUpdate();
            checkWarning(pstmt, updateCount);
        }
        catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in updateing ParkingSpot with id = "+spotID+"!",sqle);
        }
    }

    //TODO: Make method to get timeslots from database and return arraylist
    public ArrayList<TimeSlot> getTimeSlotsForParkingSpot(long spotID) throws DAOException
    {
	    ArrayList<TimeSlot> returnVal;
	    TimeSlot currSlot;
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        Date start, end;
        long timeSlotID;
        boolean bookedVar = false;

	    try {
            cmdString = "SELECT T.TIMESLOT_ID, T.SPOT_ID, T.STARTDATETIME, T.ENDDATETIME, B.USERNAME" +
                    " FROM TIMESLOTS T LEFT JOIN BOOKINGS B ON T.TIMESLOT_ID=B.TIMESLOT_ID " +
                    "AND B.DELETED=FALSE WHERE T.SPOT_ID=? AND T.DELETED=FALSE" +
                    " ORDER BY T.STARTDATETIME";
            pstmt = con.prepareStatement(cmdString);
            pstmt.setLong(1, spotID);
            rss = pstmt.executeQuery();
            returnVal=new ArrayList<TimeSlot>();
            while (rss.next())
            {
                timeSlotID = rss.getLong("TIMESLOT_ID");
                start = rss.getTimestamp("STARTDATETIME");
                end = rss.getTimestamp("ENDDATETIME");

                calStart.setTime(start);
                calEnd.setTime(end);

                if(rss.getString("TIMESLOT_ID")!=null) {
                    bookedVar = true;
                }

                currSlot=new TimeSlot(calStart.getTime(),calEnd.getTime(),timeSlotID, bookedVar);
                returnVal.add(currSlot);
            }

            rss.close();

        }catch (SQLException SqlEx){
	        processSQLError(SqlEx);
	        throw new DAOException("Error in getting timeslots from parking spot with SPOT_ID" +
                    " = "+spotID+"!",SqlEx);
        }

	    return returnVal;
    }

    //TODO: Confirm if this method should or should not be used.
    public ArrayList<TimeSlot> getUnbookedTimeSlotsForParkingSpot(long spotID) throws DAOException
    {
        ArrayList<TimeSlot> returnVal;
        TimeSlot currSlot;
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        Date start, end;
        long timeSlotID;
        boolean bookedVar=false;

        try {
            cmdString = "SELECT T.TIMESLOT_ID, T.SPOT_ID, T.STARTDATETIME, T.ENDDATETIME, B.USERNAME" +
                    " FROM TIMESLOTS T LEFT JOIN BOOKINGS B ON T.TIMESLOT_ID=B.TIMESLOT_ID " +
                    "AND B.DELETED=FALSE WHERE T.SPOT_ID=? AND T.DELETED=FALSE" +
                    " ORDER BY T.STARTDATETIME";
            pstmt = con.prepareStatement(cmdString);
            pstmt.setLong(1, spotID);
            rss = pstmt.executeQuery();
            returnVal=new ArrayList<TimeSlot>();
            while (rss.next())
            {
                timeSlotID = rss.getLong("TIMESLOT_ID");
                start = rss.getTimestamp("STARTDATETIME");
                end = rss.getTimestamp("ENDDATETIME");

                calStart.setTime(start);
                calEnd.setTime(end);

                if(rss.getString("USERNAME")!=null) {
                    bookedVar = true;
                }else{
                    currSlot=new TimeSlot(calStart.getTime(),calEnd.getTime(),timeSlotID, bookedVar);
                    returnVal.add(currSlot);
                }
            }
            rss.close();

        }catch (SQLException SqlEx){
            processSQLError(SqlEx);
            throw new DAOException("Error in getting timeslots from parking spot with SPOT_ID" +
                    " = "+spotID+"!",SqlEx);
        }

        return returnVal;
    }

    //TODO: Make method to set the deleted field for timeslots in the database to true.
    public ParkingSpot getParkingSpotByID(long spotID) throws DAOException
    {
	   ParkingSpot returnVal = null;
	   String name, address, phone, email ;
	   Double rate;

        try {
            cmdString = "SELECT * FROM PARKINGSPOTS WHERE SPOT_ID = ?";
            pstmt = con.prepareStatement(cmdString);
            pstmt.setLong(1, spotID);
            rss = pstmt.executeQuery();
            while (rss.next())
            {
                name=rss.getString("NAME");
                address=rss.getString("ADDRESS");
                phone=rss.getString("PHONE");
                email=rss.getString("EMAIL");
                rate=rss.getDouble("RATE");
                returnVal=new ParkingSpot(spotID,address,name, phone, email, rate);
            }

            rss.close();

        } catch (Exception SqlEx){ //TODO: Exception catching style here may need to change
            processSQLError(SqlEx);
            throw new DAOException("Error in getting timeslots from parking spot with SPOT_ID" +
                    " = "+spotID+"!",SqlEx);
        }
        return returnVal;
    }

    public boolean bookTimeSlot(String theUser, long timeSlotID, long spotID) throws DAOException
    {
        boolean returnVal = false;

        try {
            cmdString = "INSERT INTO BOOKINGS VALUES(?,?,?,FALSE)";
            pstmt = con.prepareStatement(cmdString);
            pstmt.setString(1, theUser);
            pstmt.setLong(2, timeSlotID);
            pstmt.setLong(3, spotID);
            updateCount = pstmt.executeUpdate();
            checkWarning(pstmt,updateCount);

        } catch (Exception SqlEx){ //TODO: Exception catching style here may need to change
            processSQLError(SqlEx);
            throw new DAOException("Error in booking timeslots for parking spot with SPOT_ID" +
                    " = "+spotID+"!",SqlEx);
        }

	    return returnVal;
    }

    @Override
    public ArrayList<TimeSlot> getDaySlots(long spotID) throws DAOException
    {
        ArrayList<TimeSlot> slots = new ArrayList<>();

        try {
            cmdString = "SELECT * FROM DAYSLOTS D WHERE D.SPOT_ID=? ";
            pstmt = con.prepareStatement(cmdString);

            pstmt.setLong(1, spotID);

            rss = pstmt.executeQuery();
            long daySlotID;
            Date start, end;

            while (rss.next())
            {
                daySlotID = rss.getLong("TIMESLOT_ID");
                start = rss.getTimestamp("Startdatetime");
                end = rss.getTimestamp("Enddatetime");

                slots.add(new TimeSlot(start, end, daySlotID));
            }

            rss.close();
        } catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in getting day slot for ParkingSpot with spotID: "+spotID+"!",sqle);
        }

        return slots;
    }

    @Override
    public ArrayList<TimeSlot> getTimeSlots(long daySlotID) throws DAOException
    {
        ArrayList<TimeSlot> slots = new ArrayList<>();

        try {
            cmdString = "SELECT * FROM TIMESLOTS T WHERE T.SLOT_ID=? ";
            pstmt = con.prepareStatement(cmdString);

            pstmt.setLong(1, daySlotID);

            rss = pstmt.executeQuery();
            long timeSlotID;
            Date start, end;

            while (rss.next())
            {
                timeSlotID = rss.getLong("TIMESLOT_ID");
                start = rss.getTimestamp("Startdatetime");
                end = rss.getTimestamp("Enddatetime");

                slots.add(new TimeSlot(start, end, timeSlotID));
            }

            rss.close();
        } catch (SQLException sqle)
        {
            processSQLError(sqle);
            throw new DAOException("Error in getting time slot for day slot with slotID: "+daySlotID+"!",sqle);
        }

        return slots;
    }
}
