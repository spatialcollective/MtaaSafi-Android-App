package com.sc.mtaa_safi.SystemUtils;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;

import com.sc.mtaa_safi.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static final String  USERNAME = "username", SCREEN_WIDTH = "swidth",
                                LAT = "lat", LNG = "lon", LOCATION_TIMESTAMP = "loc_tstamp",
                                ADMIN = "admin", ADMIN_ID = "adminId", SAVED_REPORT_COUNT = "srcount",
                                SIGN_IN_STATUS="sign_in_status", USER_ID = "user_id";

    public static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(context.getPackageName() + "_preferences",
                Context.MODE_MULTI_PROCESS);
    }

    public static Boolean getSignInStatus(Context context){
        return getSharedPrefs(context).getBoolean(SIGN_IN_STATUS, false);
    }

    public static void setSignInStatus(Context context, Boolean status){
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putBoolean(SIGN_IN_STATUS, status);
        editor.commit();
    }

    public static String getUserId(Context context){
        return getSharedPrefs(context).getString(USER_ID, "");
    }

    public static void setUserId(Context context, String userId){
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(USER_ID, userId);
        editor.commit();
    }


    public static String getUserName(Context context) {
        return getSharedPrefs(context).getString(USERNAME, "");
    }
    public static int getScreenWidth(Context context) {
        return getSharedPrefs(context).getInt(SCREEN_WIDTH, 400);
    }
    public static Location getLocation(Context context) {
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(getSharedPrefs(context).getFloat(LAT, 0));
        loc.setLongitude(getSharedPrefs(context).getFloat(LNG, 0));
        loc.setTime(getSharedPrefs(context).getLong(LOCATION_TIMESTAMP, 0));
        return loc;
    }
    public static String getSelectedAdminName(Context context) {
        return getSharedPrefs(context).getString(ADMIN, context.getResources().getString(R.string.nearby));
    }
    public static long getSelectedAdminId(Context context) {
        return getSharedPrefs(context).getLong(ADMIN_ID, -1);
    }

    public static void saveUserName(Context context, String username) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        //editor.putString(USERNAME, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME).replaceAll("\"",""));
        editor.putString(USERNAME, username);
        editor.commit();
    }
    
    public static void saveScreenWidth(Context context, int width) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putInt(SCREEN_WIDTH, width);
        editor.commit();
    }
    public static void saveLocation(Context context, Location loc) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putFloat(LNG, (float) loc.getLongitude());
        editor.putFloat(LAT, (float) loc.getLatitude());
        editor.putLong(LOCATION_TIMESTAMP, loc.getTime());
        editor.commit();
    }
    public static void saveSelectedAdmin(Context context, String name, long id) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putLong(ADMIN_ID, id);
        editor.putString(ADMIN, name);
        editor.commit();
    }

    public static String getElapsedTime(Long timestamp) {
        return getHumanReadableTimeElapsed(System.currentTimeMillis() - timestamp, new Date(timestamp));
    }

    public static String getHumanReadableTimeElapsed(long timeElapsed, Date date) {
        long second = 1000, minute = 60 * second, hour = 60 * minute,
                day = 24 * hour, week = 7 * day, year = 365 * day;

        if (timeElapsed > year)
            return new SimpleDateFormat("dd LLL' '''yy").format(date);
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

    public static int getSavedReportCount(Context context){
        return getSharedPrefs(context).getInt(SAVED_REPORT_COUNT, 0);
    }

    public static void saveSavedReportCount(Context context, int count) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putInt(SAVED_REPORT_COUNT, count);
        editor.commit();
    }
}
