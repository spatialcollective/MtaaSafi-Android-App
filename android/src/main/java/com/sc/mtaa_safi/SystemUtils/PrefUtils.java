package com.sc.mtaa_safi.SystemUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Agree on 10/27/2014.
 */
public class PrefUtils {
    public static final String  USERNAME = "username",
                                LAT = "lat",
                                LON = "lon",
                                LOCATION = "location",
                                LOCATION_TIMESTAMP = "loc_tstamp",
                                SCREEN_WIDTH = "swidth";

    private static final String PREF_KEY = "myPrefs";
    public final static int SDK = Build.VERSION.SDK_INT;

    public static ComplexPreferences getPrefs(Context context){
        return ComplexPreferences.getComplexPreferences(context, PREF_KEY, Activity.MODE_PRIVATE);
    }
    public static int getTimeSinceInMinutes(float since){
        float diffMillis = System.currentTimeMillis() - since;
        float diffSeconds = diffMillis/1000;
        float diffMinutes = diffSeconds/60;
        return (int) diffMinutes;
    }
    public static String trimUsername(String userName){
        if(userName.indexOf('"') != -1){ // trim quotation marks
            userName = userName.substring(1, userName.length()-1);
        }
        return userName;
    }

    public static String getSimpleTimeStamp(String timestamp) {
        SimpleDateFormat fromFormat = new SimpleDateFormat("H:mm:ss dd-MM-yyyy");
        SimpleDateFormat displayFormat = new SimpleDateFormat("K:mm a  d MMM yy");
        try {
            return displayFormat.format(fromFormat.parse(timestamp));
        } catch (Exception e) {
            return timestamp;
        }
    }
    public static String getSimpleTimeStamp(long timestamp) {
        return new SimpleDateFormat("H:mm:ss dd-MM-yyyy")
                .format(new java.util.Date(timestamp));
    }
    public static String getElapsedTime(long timestamp) {
        return getElapsedTime(getSimpleTimeStamp(timestamp));
    }

    public static String getElapsedTime(String timestamp) {
        if (timestamp != null) {
            SimpleDateFormat df = new SimpleDateFormat("H:mm:ss dd-MM-yyyy");
            try {
                long postEpochTime = df.parse(timestamp).getTime();
                long currentEpochTime = System.currentTimeMillis();
                return getHumanReadableTimeElapsed(currentEpochTime - postEpochTime, df.parse(timestamp));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return timestamp;
    }

    public static String getHumanReadableTimeElapsed(long timeElapsed, Date date) {
        long second = 1000,
                minute = 60 * second,
                hour = 60* minute,
                day = 24 * hour,
                week = 7 * day,
                year = 365 * day;

        if (timeElapsed > year)
            return new SimpleDateFormat("dd LLL yy").format(date);
        else if (timeElapsed > week)
            return new SimpleDateFormat("dd LLL").format(date);
        else if (timeElapsed > 1.5 * day)
            return (long) Math.floor(timeElapsed/day) + " days";
        else if (timeElapsed > day)
            return "1 day";
        else if (timeElapsed > hour)
            return (long) Math.floor(timeElapsed/hour) + " hours";
        else if (timeElapsed > minute)
            return (long) Math.floor(timeElapsed/minute) + " min";
        return "just now";
    }
}
