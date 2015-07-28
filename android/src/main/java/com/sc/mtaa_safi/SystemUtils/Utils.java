package com.sc.mtaa_safi.SystemUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Utils {
    public static final String  USERNAME = "username", SCREEN_WIDTH = "swidth",
                                LAT = "lat", LNG = "lon", LOCATION_TIMESTAMP = "loc_tstamp",
                                COARSE_LAT = "c_lat", COARSE_LNG = "c_lon", COARSE_LOCATION_TIMESTAMP = "c_loc_tstamp",
                                ADMIN = "admin", ADMIN_ID = "adminId", NEARBY_ADMINS = "nearby_admins", SAVED_REPORT_COUNT = "srcount",
                                FEED_ERROR = "error",
                                ONBOARD_STATUS = "onboard_status", SIGN_IN_STATUS="sign_in_status", USER_ID = "user_id", EMAIL="email",
                                FACEBOOK_UUID="uuid", GOOGLE_PLUS_UUID="gplus_uuid",
                                PROPERTY_REG_ID = "registration_id", PROPERTY_APP_VERSION = "appVersion",
                                SENDER_ID = "46655663326";

    public static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(context.getPackageName() + "_preferences",
                Context.MODE_MULTI_PROCESS);
    }

    public static Boolean hasOnboarded(Context context){
        return getSharedPrefs(context).getBoolean(ONBOARD_STATUS, false);
    }
    public static void setHasOnboarded(Context context){
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putBoolean(ONBOARD_STATUS, true);
        editor.commit();
    }

    public static Boolean isSignedIn(Context context){
        return getSharedPrefs(context).getBoolean(SIGN_IN_STATUS, false);
    }

    public static void setSignInStatus(Context context, Boolean status){
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putBoolean(SIGN_IN_STATUS, status);
        editor.commit();
    }

    public static int getUserId(Context context){
        return getSharedPrefs(context).getInt(USER_ID, -1);
    }

    public static void setUserId(Context context, int userId){
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putInt(USER_ID, userId);
        editor.commit();
    }

    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPrefs(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty() || registrationId == "-1")
            Log.i("Utils", "Registration not found.");
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion)
            Log.i("Utils", "App version changed.");
        return registrationId;
    }
    public static void setRegistrationId(Context context, String regId) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, getAppVersion(context));
        editor.commit();
    }
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) { // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static String getGooglePlusId(Context context){
        return getSharedPrefs(context).getString(GOOGLE_PLUS_UUID, "");
    }

    public static void setGooglePlusId(Context context, String userId){
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(GOOGLE_PLUS_UUID, userId);
        editor.commit();
    }

    public static String getFacebookId(Context context){
        return getSharedPrefs(context).getString(FACEBOOK_UUID, "");
    }

    public static void setFacebookId(Context context, String userId){
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(FACEBOOK_UUID, userId);
        editor.commit();
    }

    public static String getUserName(Context context) {
        return getSharedPrefs(context).getString(USERNAME, "");
    }

    public static String getEmail(Context context){
        return getSharedPrefs(context).getString(EMAIL, "");
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
    public static Location getCoarseLocation(Context context) {
        Location loc = new Location(LocationManager.NETWORK_PROVIDER);
        loc.setLatitude(getSharedPrefs(context).getFloat(COARSE_LAT, 0));
        loc.setLongitude(getSharedPrefs(context).getFloat(COARSE_LNG, 0));
        loc.setTime(getSharedPrefs(context).getLong(COARSE_LOCATION_TIMESTAMP, 0));
        return loc;
    }
    public static String getSelectedAdminName(Context context) {
        return getSharedPrefs(context).getString(ADMIN, context.getResources().getString(R.string.nearby));
    }
    public static String getNearbyAdmins(Context context) {
        return getSharedPrefs(context).getString(NEARBY_ADMINS, "");
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

    public static void saveEmail(Context context, String email){
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(EMAIL, email);
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
    public static void saveCoarseLocation(Context context, Location loc) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putFloat(COARSE_LNG, (float) loc.getLongitude());
        editor.putFloat(COARSE_LAT, (float) loc.getLatitude());
        editor.putLong(COARSE_LOCATION_TIMESTAMP, loc.getTime());
        editor.commit();
    }
    public static void saveSelectedAdmin(Context context, String name, long id) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putLong(ADMIN_ID, id);
        editor.putString(ADMIN, name);
        editor.commit();
    }
    public static void saveNearbyAdmins(Context context, String ids) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(NEARBY_ADMINS, ids);
        editor.commit();
    }

    public static int getSavedReportCount(Context context){
        return getSharedPrefs(context).getInt(SAVED_REPORT_COUNT, 0);
    }

    public static void saveSavedReportCount(Context context, int count) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putInt(SAVED_REPORT_COUNT, count);
        editor.commit();
    }

    public static String getFeedError(Context context) {
        return getSharedPrefs(context).getString(FEED_ERROR, "");
    }
    public static void saveFeedError(Context context, String error) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(FEED_ERROR, error);
        editor.commit();
    }
    public static void removeFeedError(Context context) {
        getSharedPrefs(context).edit().remove(FEED_ERROR).commit();
    }

    public static String createCursorAdminList(String rawString) {
        String[] adminArr = Utils.toStringArray(Utils.toStringList(rawString));
        String sqlStatement = Contract.Entry.COLUMN_ADMIN_ID + " IN(";
        for (int n = 0; n < adminArr.length - 1; n++)
            sqlStatement += adminArr[n] + ", ";
        sqlStatement += adminArr[adminArr.length - 1] + ")";
        return sqlStatement;
    }

    public static ArrayList<String> toStringList(String rawStringOfList) {
        String stringList  = rawStringOfList.replace("[", "").replace("]", "");
        return new ArrayList<>(Arrays.asList(stringList.split(", ")));
    }
    public static String[] toStringArray(List<String> rawStringList) {
        return rawStringList.toArray(new String[rawStringList.size()]);
    }

    public static String createHumanReadableTimestamp(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd' '''yy' at 'HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new java.util.Date(timeStamp));
    }
    public static String getElapsedTime(Long timestamp) {
        return getHumanReadableTimeElapsed(System.currentTimeMillis() - timestamp, new Date(timestamp));
    }
    public static String getHumanReadableTimeElapsed(long timeElapsed, Date date) {
        long second = 1000, minute = 60 * second, hour = 60 * minute,
                day = 24 * hour, week = 7 * day, year = 365 * day;

        SimpleDateFormat sdf = new SimpleDateFormat("dd LLL");
        SimpleDateFormat sdf_y = new SimpleDateFormat("dd LLL' '''yy");
        sdf.setTimeZone(TimeZone.getDefault());
        sdf_y.setTimeZone(TimeZone.getDefault());

        if (timeElapsed > year)
            return sdf_y.format(date);
        else if (timeElapsed > week)
            return sdf.format(date);
        else if (timeElapsed > 2 * day)
            return (long) Math.floor(timeElapsed/day) + " days";
        else if (timeElapsed > day)
            return "1 day";
        else if (timeElapsed > 2 * hour)
            return (long) Math.floor(timeElapsed/hour) + " hours";
        else if (timeElapsed > hour)
            return (long) Math.floor(timeElapsed/hour) + " hour";
        else if (timeElapsed > minute)
            return (long) Math.floor(timeElapsed/minute) + " min";
        return "just now";
    }
}
