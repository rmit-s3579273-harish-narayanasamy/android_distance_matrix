package com.example.harish.contactview.Controller;

/**
 * Created by HARISH on 9/4/2017.
 */

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.example.harish.contactview.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

//import mad.friend.simple.R;

public class DummyLocationService {
    // PRIVATE PORTION
    private static final String LOG_TAG = DummyLocationService.class.getName();
    private LinkedList<FriendLocation> locationList = new LinkedList<FriendLocation>();
    private static Context context;

    // Singleton
    private DummyLocationService() {
    }

    // This is only a data access object (DAO)
    // You must extract data and place in your model
    public static class FriendLocation {
        public Date time;
        public String id;
        public String name;
        public double latitude;
        public double longitude;

        @Override
        public String toString() {
            return String.format("Time=%s, id=%s, name=%s, lat=%.5f, long=%.5f", DateFormat.getTimeInstance(
                    DateFormat.MEDIUM).format(time), id, name, latitude, longitude);
        }
    }

    // check if the source time is with the range of target time +/- minutes and seconds
    private boolean timeInRange(Date source, Date target, int periodMinutes, int periodSeconds) {
        Calendar sourceCal = Calendar.getInstance();
        sourceCal.setTime(source);

        // set up start and end range match
        // +/- period minutes/seconds to check
        Calendar targetCalStart = Calendar.getInstance();
        targetCalStart.setTime(target);
        targetCalStart.set(Calendar.MINUTE, targetCalStart.get(Calendar.MINUTE) - periodMinutes);
        targetCalStart.set(Calendar.SECOND, targetCalStart.get(Calendar.SECOND) - periodSeconds);
        Calendar targetCalEnd = Calendar.getInstance();
        targetCalEnd.setTime(target);
        targetCalEnd.set(Calendar.MINUTE, targetCalEnd.get(Calendar.MINUTE) + periodMinutes);
        targetCalEnd.set(Calendar.SECOND, targetCalEnd.get(Calendar.SECOND) + periodMinutes);

        // return if source time in the target range
        return sourceCal.after(targetCalStart) && sourceCal.before(targetCalEnd);
    }

    // called internally before usage
    private void parseFile(Context context) {
        locationList.clear();
        // resource reference to dummy_data.txt in res/raw/ folder of your project
        try (Scanner scanner = new Scanner(context.getResources().openRawResource(R.raw.dummy_data))) {
            // match comma and 0 or more whitepace (to catch newlines)
            scanner.useDelimiter(",\\s*");
            while (scanner.hasNext()) {
                FriendLocation friend = new FriendLocation();
                friend.time = DateFormat.getTimeInstance(DateFormat.MEDIUM).parse(scanner.next());
                friend.id = scanner.next();
                friend.name = scanner.next();
                friend.latitude = Double.parseDouble(scanner.next());
                friend.longitude = Double.parseDouble(scanner.next());
                locationList.addLast(friend);
            }
        } catch (Resources.NotFoundException e) {
            Log.i(LOG_TAG, "File Not Found Exception Caught");
        } catch (ParseException e) {
            Log.i(LOG_TAG, "ParseException Caught (Incorrect File Format)");
        }
    }

    // singleton support
    private static class LazyHolder {
        static final DummyLocationService INSTANCE = new DummyLocationService();
    }

    // PUBLIC METHODS

    // singleton
    // thread safe lazy initialisation: see https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
    public static DummyLocationService getSingletonInstance(Context context) {
        DummyLocationService.context = context;
        return LazyHolder.INSTANCE;
    }

    // log contents of file (for testing/logging only)
    public void logAll() {
        log(locationList);
    }

    // log contents of provided list (for testing/logging and example purposes only)
    public void log(List<FriendLocation> locationList) {
        // we reparse file contents for latest data on every call
        parseFile(context);
        for (FriendLocation friend : locationList)
            Log.i(LOG_TAG, friend.toString());
    }

    // the main method you can call periodcally to get data that matches a given time period
    // time +/- period minutes/seconds to check
    public List<FriendLocation> getFriendLocationsForTime(Date time, int periodMinutes, int periodSeconds) {
        // we reparse file contents for latest data on every call
        parseFile(context);
        List<FriendLocation> returnList = new ArrayList<FriendLocation>();
        for (FriendLocation friend : locationList)
            if (timeInRange(friend.time, time, periodMinutes, periodSeconds))
                returnList.add(friend);
        return returnList;
    }
}