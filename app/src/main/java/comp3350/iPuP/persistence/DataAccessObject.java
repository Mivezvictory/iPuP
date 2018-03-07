package comp3350.iPuP.persistence;

import org.hsqldb.Types;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import comp3350.iPuP.objects.DateFormatter;
import comp3350.iPuP.objects.DaySlot;
import comp3350.iPuP.objects.ParkingSpot;
import comp3350.iPuP.objects.TimeSlot;

public class DataAccessObject implements DataAccess
{
	private PreparedStatement pstmt, pstmt2, pstmt3;
	private Statement stmt;
	private Connection con;
	private ResultSet rss, rsp, rsp2, rsp3, genkey;

	private String dbName;
	private String dbType;

    private DateFormatter df = new DateFormatter();

	private ArrayList<ParkingSpot> parkingSpots;

	private String cmdString;
	private int updateCount;
	private String result;
	private static String EOF = "  ";


	public DataAccessObject(String dbName)
	{
		this.dbName = dbName;
	}


	public void open(String dbPath)
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
		}
		System.out.println("Opened " +dbType +" database " +dbPath);
	}


	public void close()
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
		}
		System.out.println("Closed " +dbType +" database " +dbName);
	}


	public String insertDaySlot(String psID, DaySlot daySlot)
    {
        long dsID;
        String theResult = null;

        try
        {
            cmdString = "INSERT INTO DAYSLOTS (PS_ID,STARTDAYTIME,ENDDAYTIME) " +
                        "VALUES (?,?,?)";
            pstmt2 = con.prepareStatement(cmdString);
            pstmt2.setString(1, psID);
            pstmt2.setString(2, df.getSqlDateTimeFormat().format(daySlot.getStart()));
            pstmt2.setString(3, df.getSqlDateTimeFormat().format(daySlot.getEnd()));

            updateCount = pstmt2.executeUpdate();
            theResult = checkWarning(pstmt, updateCount);

            cmdString = "CALL IDENTITY()";
            rss = stmt.executeQuery(cmdString);

            if (rss.next())
            {
                dsID = rss.getLong(1);

                // insert all timeSlots related to this daySlot
                String rtn = insertTimeSlots(psID, dsID, daySlot.getTimeSlots());

                if (rtn != null)
                    return rtn;
            } else
            {
                theResult += "\nCould not retrieve DaySlot ID from Database!";
            }

        } catch (Exception e)
        {
            theResult = processSQLError(e);
        }

        return theResult;
    }


	public String insertDaySlots(String psID, ArrayList<DaySlot> daySlots)
    {
        String rtn = null;

        for (int i = 0; i < daySlots.size(); i++)
        {
            rtn = insertDaySlot(psID, daySlots.get(i));

            if (rtn != null)
            {
                return rtn;
            }
        }

        return rtn;
    }

    public String insertTimeSlot(String psID, Long dsID, Date start, Date end)
    {
        String theResult = null;

        try
        {
            cmdString = "INSERT INTO TIMESLOTS (PS_ID,DS_ID,STARTDATETIME,ENDDATETIME) " +
                    "VALUES (?,?,?,?)";
            pstmt3 = con.prepareStatement(cmdString);
            pstmt3.setString(1, psID);
            if (dsID == null)
            {
                pstmt3.setNull(2, Types.NULL);
            } else
            {
                pstmt3.setLong(2, dsID);
            }
            pstmt3.setString(3, df.getSqlDateTimeFormat().format(start));
            pstmt3.setString(4, df.getSqlDateTimeFormat().format(end));

            updateCount = pstmt3.executeUpdate();
            theResult = checkWarning(pstmt, updateCount);

        } catch (Exception e)
        {
            theResult = processSQLError(e);
        }

        return theResult;
    }


    public String insertTimeSlot(String psID, Long dsID, TimeSlot timeSlot)
    {
        return insertTimeSlot(psID, dsID, timeSlot.getStart(), timeSlot.getEnd());
    }


    public String insertTimeSlots(String psID, Long dsID, ArrayList<TimeSlot> timeSlots)
    {
        String rtn = null;

        for (int i = 0; i < timeSlots.size(); i++)
        {
            rtn = insertTimeSlot(psID, dsID, timeSlots.get(i));

            if (rtn != null)
            {
                return rtn;
            }
        }

        return rtn;
    }


	public String insertParkingSpot(String user, ParkingSpot currentParkingSpot)
    {
        result = null;

        try
        {
            cmdString = "INSERT INTO PARKINGSPOTS VALUES(?,?,?,?,?,?,?)";
            pstmt = con.prepareStatement(cmdString);
            pstmt.setString(1,currentParkingSpot.getSpotID());
            pstmt.setString(2,user);
            pstmt.setString(3,currentParkingSpot.getName());
            pstmt.setString(4,currentParkingSpot.getAddress());
            pstmt.setString(5,currentParkingSpot.getPhone());
            pstmt.setString(6,currentParkingSpot.getEmail());
            pstmt.setDouble(7,currentParkingSpot.getRate());

            //System.out.println(cmdString);
            updateCount = pstmt.executeUpdate();
            result = checkWarning(pstmt, updateCount);

            String rtnt = insertTimeSlot(currentParkingSpot.getSpotID(), null, currentParkingSpot.getStartTime(), currentParkingSpot.getEndTime());

            if (rtnt != null)
                return rtnt;

            String rtnd = insertDaySlots(currentParkingSpot.getSpotID(), currentParkingSpot.getDaySlots());

            if (rtnd != null)
                return rtnd;
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }

        return result;
    }

    public boolean insertUser(String username)
    {
        boolean inserted = false;
        result = null;

        try
        {
            cmdString = "INSERT INTO USERS VALUES(?)";
            pstmt = con.prepareStatement(cmdString);
            pstmt.setString(1, username);

            updateCount = pstmt.executeUpdate();

            if (updateCount == 1)
            {
                inserted = true;
            }

            result = checkWarning(pstmt, updateCount);

        } catch (Exception e)
        {
            result = processSQLError(e);
        }

        return inserted;
    }

    public ArrayList<ParkingSpot> getParkingSpots()
    {
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        Date start, end;
        Double rate;
        long tsId;
        ParkingSpot ps;
        TimeSlot timeSlot;
        String id, name, addr, phone, email;

        parkingSpots = new ArrayList<ParkingSpot>();
        result = null;

        try
        {
            cmdString = "SELECT * FROM PARKINGSPOTS P JOIN TIMESLOTS T ON P.PS_ID = T.PS_ID AND T.DS_ID IS NULL";
            rss = stmt.executeQuery(cmdString);
            //ResultSetMetaData md = rs.getMetaData();

            while (rss.next())
            {
                id = rss.getString("PS_ID");
                name = rss.getString("Name");
                addr = rss.getString("Address");
                phone = rss.getString("Phone");
                email = rss.getString("Email");
                rate = rss.getDouble("Rate");
                start = rss.getDate("Startdatetime");
                end = rss.getDate("Enddatetime");
                tsId = rss.getLong("TS_ID");

                calStart.setTime(start);
                calEnd.setTime(end);

                timeSlot = new TimeSlot(calStart.getTime(), calEnd.getTime(), Long.toString(tsId));

                ps = new ParkingSpot(id, addr, name, phone, email, rate, timeSlot);
//                ps = new ParkingSpot(id.split("_")[0], addr, name, phone, email, rate, isBooked, timeSlot);
                parkingSpots.add(ps);
            }

            rss.close();
        }
        catch (Exception e)
        {
            processSQLError(e);
        }

        return parkingSpots;
    }

//    public String setSpotToBooked(String spotID, String slotID)
//    {
//        boolean isBooked;
//        String bookMessage = "Not Booked";
//
//        result = null;
//
//        try
//        {
////            cmdString = "SELECT * FROM ParkingSpots WHERE PS_ID = ?";
//            cmdString = "UPDATE PARKINGSPOTS SET IS_BOOKED = ? WHERE PS_ID = ? AND IS_BOOKED = FALSE";
//            pstmt = con.prepareStatement(cmdString);
//            pstmt.setBoolean(1, true);
//            pstmt.setString(2, spotID);
////            pstmt.setString(2, spotID + "_" + slotID);
//            updateCount = pstmt.executeUpdate();
//
//            if (updateCount == 0)
//            {
//                bookMessage = "Already Booked";
//            } else
//            {
//                bookMessage = "Booked";
//            }
//
////            if (rsp.next())
////            {
////                isBooked = rsp.getBoolean("Is_Booked");
////
////                if (isBooked)
////                {
////                    bookMessage = "Already Booked";
////                }
////                else
////                {
////                    cmdString = "Update ParkingSpots Set Is_Booked=? where PS_ID=?";
////                    pstmt = con.prepareStatement(cmdString);
////                    pstmt.setBoolean(1, true);
////                    pstmt.setString(2,spotID + "_" + slotID);
////                    //System.out.println(cmdString);
////                    updateCount = pstmt.executeUpdate();
////                    result = checkWarning(pstmt, updateCount);
////
////                    bookMessage = "Booked";
////                }
////            }
////
////            rsp.close();
//        }
//        catch (Exception e)
//        {
//            result = processSQLError(e);
//        }
//
//        return bookMessage;
//    }


    public void clearSpotList()
    {
        this.parkingSpots.clear();
    }


	public String checkWarning(Statement st, int updateCount)
	{
		String result;

		result = null;
		try
		{
			SQLWarning warning = st.getWarnings();
			if (warning != null)
			{
				result = warning.getMessage();
			}
		}
		catch (Exception e)
		{
			result = processSQLError(e);
		}
		if (updateCount != 1)
		{
			result = "Tuple not inserted correctly.";
		}
		return result;
	}


	public String processSQLError(Exception e)
	{
		String result = "*** SQL Error: " + e.getMessage();

		// Remember, this will NOT be seen by the user!
		e.printStackTrace();
		
		return result;
	}


	//added by Kevin
    public ArrayList<ParkingSpot> getSpotsOfGivenUser(String username)
    {
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        Date start, end;
        Double rate;
        long tsId;
        ParkingSpot ps;
        TimeSlot timeSlot;
        String id, name, addr, phone, email;

        parkingSpots = new ArrayList<ParkingSpot>();
        result = null;


        try
        {
            cmdString = "SELECT * FROM PARKINGSPOTS P JOIN TIMESLOTS T ON P.PS_ID = T.PS_ID AND T.DS_ID IS NULL WHERE P.USER_ID = '" + username + "'";
            //cmdString = "SELECT * FROM PARKINGSPOTS P JOIN DAYSLOTS D ON P.PS_ID = D.PS_ID AND D.DS_ID IS NULL WHERE P.USER_ID = '" + username + "'";
            rss = stmt.executeQuery(cmdString);
            //ResultSetMetaData md = rs.getMetaData();

            while (rss.next())
            {
                id = rss.getString("PS_ID");
                name = rss.getString("Name");
                addr = rss.getString("Address");
                phone = rss.getString("Phone");
                email = rss.getString("Email");
                rate = rss.getDouble("Rate");
                start = rss.getDate("Startdatetime");
                end = rss.getDate("Enddatetime");
                tsId = rss.getLong("TS_ID");
                //userId = rss.getString("USER_ID");

                calStart.setTime(start);
                calEnd.setTime(end);
                timeSlot = new TimeSlot(calStart.getTime(), calEnd.getTime(), Long.toString(tsId));

                ps = new ParkingSpot(id, addr, name, phone, email, rate, timeSlot);
//                ps = new ParkingSpot(id.split("_")[0], addr, name, phone, email, rate, isBooked, timeSlot);
                parkingSpots.add(ps);
            }

            rss.close();
        }
        catch (Exception e)
        {
            processSQLError(e);
        }

        return parkingSpots;
    }


}
