package comp3350.iPuP.tests.integration;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;

import comp3350.iPuP.application.Main;
import comp3350.iPuP.application.Services;
import comp3350.iPuP.business.AccessParkingSpots;
import comp3350.iPuP.business.AccessUsers;
import comp3350.iPuP.objects.Booking;
import comp3350.iPuP.objects.DAOException;
import comp3350.iPuP.objects.DateFormatter;
import comp3350.iPuP.objects.ParkingSpot;
import comp3350.iPuP.objects.TimeSlot;
import comp3350.iPuP.persistence.DataAccess;
import comp3350.iPuP.persistence.DataAccessObject;

public class BusinessPersistenceSeamTest extends TestCase
{
    private static String dbName = Main.dbName;
    private static String dbPathName = Main.getDBPathName();
    private DataAccess dataAccess;
    private DateFormatter dateFormatter = new DateFormatter();

    public BusinessPersistenceSeamTest(String arg0) { super(arg0); }

    public void testAccessUsers()
    {
        openDataAccess();
        System.out.println("Starting testBusinessPersistenceSeam: Access Users");

        AccessUsers accessUsers;

        try
        {
            accessUsers = new AccessUsers();
            assertFalse(accessUsers.createUser("marker"));
            assertTrue(accessUsers.createUser("tester3"));
            assertFalse(accessUsers.createUser("tester"));
            assertTrue(accessUsers.createUser("tester1"));
            assertTrue(accessUsers.createUser("tester4"));
            assertTrue(accessUsers.createUser("marker1"));
            assertTrue(accessUsers.createUser("marker2"));
            assertTrue(accessUsers.createUser("marker3"));
            assertTrue(accessUsers.createUser("marker4"));
        }
        catch (DAOException daoe)
        {
            fail("DAOException Caught with message: "+daoe.getMessage());
        }

        closeDataAccess();
        System.out.println("Finished testBusinessPersistenceSeam: Access Users");
    }

    public void testAccessParkingSpotsToView()
    {
        openDataAccess();
        System.out.println("Starting testBusinessPersistenceSeam: Access ParkingSpots To View");

        AccessParkingSpots accessParkingSpots;
        AccessUsers accessUsers;
        ArrayList<ParkingSpot> parkingSpots;

        try
        {
            accessParkingSpots = new AccessParkingSpots();
            accessUsers = new AccessUsers();

            assertFalse(accessUsers.createUser("marker"));

            parkingSpots = accessParkingSpots.getDailySpots("",dateFormatter.getSqlDateFormat().parse("2018-03-28"));
            assertEquals(0,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("",dateFormatter.getSqlDateFormat().parse("2018-06-11"));
            assertEquals(20,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("1",dateFormatter.getSqlDateFormat().parse("2018-06-11"));
            assertEquals(10,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("Pembina",dateFormatter.getSqlDateFormat().parse("2018-06-11"));
            assertEquals(4,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("pembina",dateFormatter.getSqlDateFormat().parse("2018-06-11"));
            assertEquals(4,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("pEmBiNa",dateFormatter.getSqlDateFormat().parse("2018-06-11"));
            assertEquals(4,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("dafoe",dateFormatter.getSqlDateFormat().parse("2018-06-11"));
            assertEquals(0,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("main",dateFormatter.getSqlDateFormat().parse("2018-06-11"));
            assertEquals(1,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("",dateFormatter.getSqlDateFormat().parse("2018-06-12"));
            assertEquals(2,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("plaza",dateFormatter.getSqlDateFormat().parse("2018-06-12"));
            assertEquals(1,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("Plaza",dateFormatter.getSqlDateFormat().parse("2018-06-12"));
            assertEquals(1,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("dafoe",dateFormatter.getSqlDateFormat().parse("2018-06-12"));
            assertEquals(0,parkingSpots.size());
        }
        catch (ParseException pe)
        {
            fail("ParseException Caught with message: "+pe.getMessage());
        }
        catch (DAOException daoe)
        {
            fail("DAOException Caught with message: "+daoe.getMessage());
        }

        closeDataAccess();
        System.out.println("Finished testBusinessPersistenceSeam: Access ParkingSpots To View");
    }

    public void testBookAParkingSpot()
    {
        openDataAccess();
        System.out.println("Starting testBusinessPersistenceSeam: Book A ParkingSpot");

        AccessParkingSpots accessParkingSpots;
        AccessUsers accessUsers;
        ArrayList<ParkingSpot> parkingSpots;
        ParkingSpot aparkingspot;
        ArrayList<TimeSlot> timeSlots;
        TimeSlot timeSlot;
        boolean booked;
        ArrayList<Booking> bookings;
        Booking abooking;

        try
        {
            accessParkingSpots = new AccessParkingSpots();
            accessUsers = new AccessUsers();

            assertFalse(accessUsers.createUser("marker"));

            parkingSpots = accessParkingSpots.getDailySpots("",dateFormatter.getSqlDateFormat().parse("2018-03-28"));
            assertEquals(0,parkingSpots.size());

            parkingSpots = accessParkingSpots.getDailySpots("main",dateFormatter.getSqlDateFormat().parse("2018-06-11"));
            assertEquals(1,parkingSpots.size());

            aparkingspot = parkingSpots.get(0);
            assertEquals("60 Main Street",aparkingspot.getAddress());
            assertEquals("Avocado Stevenson",aparkingspot.getName());
            assertEquals("601-122-1211",aparkingspot.getPhone());
            assertEquals("avocadoisgood@gmail.com",aparkingspot.getEmail());
            assertEquals(5.25,aparkingspot.getRate());

            timeSlots = accessParkingSpots.getFreeTimeSlotsByID(aparkingspot.getSpotID());
            assertEquals(1,timeSlots.size());

            bookings = accessParkingSpots.getMyBookedSpots("marker");
            assertEquals(4,bookings.size());
            assertEquals("\n1000 St. Mary's Rd\nSun, 11 Feb 2018, 6:30 PM - Sun, 11 Feb 2018, 7:00 PM\n",bookings.get(0).toString());
            assertEquals("\n91 Dalhousie Drive (hold to cancel this booking) \nMon, 11 Jun 2018, 10:30 AM - Mon, 11 Jun 2018, 11:00 AM\n",bookings.get(1).toString());
            assertEquals("\n1 Pembina Hwy (hold to cancel this booking) \nMon, 11 Jun 2018, 12:30 PM - Mon, 11 Jun 2018, 1:00 PM\n",bookings.get(2).toString());
            assertEquals("\n1338 Chancellor Drive (hold to cancel this booking) \nMon, 11 Jun 2018, 2:00 PM - Mon, 11 Jun 2018, 2:30 PM\n",bookings.get(3).toString());

            timeSlot = timeSlots.get(0);
            assertEquals("Mon, 11 Jun 2018, 10:30 AM - Mon, 11 Jun 2018, 11:00 AM",timeSlot.toString());

            booked = accessParkingSpots.bookTimeSlots(timeSlots,"marker",aparkingspot.getSpotID());
            assertTrue(booked);

            timeSlots = accessParkingSpots.getFreeTimeSlotsByID(aparkingspot.getSpotID());
            assertEquals(0,timeSlots.size());

            bookings = accessParkingSpots.getMyBookedSpots("marker");
            assertEquals(5,bookings.size());
            assertEquals("\n1000 St. Mary's Rd\nSun, 11 Feb 2018, 6:30 PM - Sun, 11 Feb 2018, 7:00 PM\n",bookings.get(0).toString());
            assertEquals("\n91 Dalhousie Drive (hold to cancel this booking) \nMon, 11 Jun 2018, 10:30 AM - Mon, 11 Jun 2018, 11:00 AM\n",bookings.get(1).toString());
            assertEquals("\n60 Main Street (hold to cancel this booking) \nMon, 11 Jun 2018, 10:30 AM - Mon, 11 Jun 2018, 11:00 AM\n",bookings.get(2).toString());
            assertEquals("\n1 Pembina Hwy (hold to cancel this booking) \nMon, 11 Jun 2018, 12:30 PM - Mon, 11 Jun 2018, 1:00 PM\n",bookings.get(3).toString());
            assertEquals("\n1338 Chancellor Drive (hold to cancel this booking) \nMon, 11 Jun 2018, 2:00 PM - Mon, 11 Jun 2018, 2:30 PM\n",bookings.get(4).toString());

        }
        catch (ParseException pe)
        {
            fail("ParseException Caught with message: "+pe.getMessage());
        }
        catch (DAOException daoe)
        {
            fail("DAOException Caught with message: "+daoe.getMessage());
        }

        closeDataAccess();
        System.out.println("Finished testBusinessPersistenceSeam: Book A ParkingSpot");
    }

    //added by kev
    public void testDeleteBookedSpotsInHistory()
    {
        openDataAccess();
        System.out.println("Starting testBusinessPersistenceSeam: Delete A Booked ParkingSpot");

        AccessParkingSpots accessParkingSpots;
        AccessUsers accessUsers;
        ArrayList<ParkingSpot> parkingSpots;
        ArrayList<Booking> bookings;
        ParkingSpot aparkingspot;
        ArrayList<TimeSlot> daysSlots;
        Booking aBooking;

        try
        {
            accessParkingSpots = new AccessParkingSpots();
            accessUsers = new AccessUsers();

            assertFalse(accessUsers.createUser("marker"));

            bookings = accessParkingSpots.getMyBookedSpots("marker");
            assertEquals(4, bookings.size());

            aBooking = bookings.get(0);
            assertEquals("\n1000 St. Mary's Rd\nSun, 11 Feb 2018, 6:30 PM - Sun, 11 Feb 2018, 7:00 PM\n", aBooking.toString());

            aBooking = bookings.get(1);
            assertEquals("\n91 Dalhousie Drive (hold to cancel this booking) \nMon, 11 Jun 2018, 10:30 AM - Mon, 11 Jun 2018, 11:00 AM\n", aBooking.toString());

            aBooking = bookings.get(2);
            assertEquals("\n1 Pembina Hwy (hold to cancel this booking) \nMon, 11 Jun 2018, 12:30 PM - Mon, 11 Jun 2018, 1:00 PM\n", aBooking.toString());

            aBooking = bookings.get(3);
            assertEquals("\n1338 Chancellor Drive (hold to cancel this booking) \nMon, 11 Jun 2018, 2:00 PM - Mon, 11 Jun 2018, 2:30 PM\n", aBooking.toString());

            aBooking = bookings.get(2);
            accessParkingSpots.cancelThisSpot("marker", aBooking.getTimeSlotId());

            bookings = accessParkingSpots.getMyBookedSpots("marker");
            assertEquals(3, bookings.size());
            aBooking = bookings.get(0);
            assertEquals("\n1000 St. Mary's Rd\nSun, 11 Feb 2018, 6:30 PM - Sun, 11 Feb 2018, 7:00 PM\n", aBooking.toString());

            aBooking = bookings.get(1);
            assertEquals("\n91 Dalhousie Drive (hold to cancel this booking) \nMon, 11 Jun 2018, 10:30 AM - Mon, 11 Jun 2018, 11:00 AM\n", aBooking.toString());

            aBooking = bookings.get(2);
            assertEquals("\n1338 Chancellor Drive (hold to cancel this booking) \nMon, 11 Jun 2018, 2:00 PM - Mon, 11 Jun 2018, 2:30 PM\n", aBooking.toString());

            aBooking = bookings.get(2);
            accessParkingSpots.cancelThisSpot("marker", aBooking.getTimeSlotId());

            bookings = accessParkingSpots.getMyBookedSpots("marker");
            assertEquals(2, bookings.size());
            aBooking = bookings.get(0);
            assertEquals("\n1000 St. Mary's Rd\nSun, 11 Feb 2018, 6:30 PM - Sun, 11 Feb 2018, 7:00 PM\n", aBooking.toString());

            aBooking = bookings.get(1);
            assertEquals("\n91 Dalhousie Drive (hold to cancel this booking) \nMon, 11 Jun 2018, 10:30 AM - Mon, 11 Jun 2018, 11:00 AM\n", aBooking.toString());

            aBooking = bookings.get(1);
            accessParkingSpots.cancelThisSpot("marker", aBooking.getTimeSlotId());

            bookings = accessParkingSpots.getMyBookedSpots("marker");
            assertEquals(1, bookings.size());
            aBooking = bookings.get(0);
            assertEquals("\n1000 St. Mary's Rd\nSun, 11 Feb 2018, 6:30 PM - Sun, 11 Feb 2018, 7:00 PM\n", aBooking.toString());

        }
        catch (DAOException daoe)
        {
            fail("DAOException Caught with message: " + daoe.getMessage());
        }
    }

    private void replaceDbWithDefault() throws DAOException
    {
        Services.closeDataAccess();

        try
        {
            String dbFilePath = System.getProperty("user.dir") + "/" + dbPathName + ".script";
            String defaultDbFilePath = System.getProperty("user.dir") + "/app/src/main/assets/db/" + dbName + ".script";

            File dbFile = new File(dbFilePath);
            File defaultDbFile = new File(defaultDbFilePath);

            if (defaultDbFile.exists())
            {
                InputStream in = new FileInputStream(defaultDbFile);
                FileUtils.copyInputStreamToFile(in, dbFile);
                in.close();
            }
            else
            {
                throw new DAOException("Error in locating default database files!");
            }

        }
        catch (FileNotFoundException fnfe)
        {
            throw new DAOException("Unable to open default database file!", fnfe);
        }
        catch (IOException ioe)
        {
            throw new DAOException("Unable to access database: ", ioe);
        }

        Services.createDataAccess(dbName);
    }

    private void openDataAccess()
    {
        try
        {
            replaceDbWithDefault();
            dataAccess = new DataAccessObject(dbName);
            dataAccess.open(dbPathName);
        }
        catch (DAOException daoe)
        {
            System.err.println(daoe.getMessage());
            System.exit(1);
        }
    }

    private void closeDataAccess()
    {
        try
        {
            replaceDbWithDefault();
            System.out.println("Closed HSQL database " + dbName);
        } catch (DAOException daoe)
        {
            System.err.println(daoe.getMessage());
            System.exit(1);
        }
    }
}
