package com.sc.mtaasafi.android;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Agree on 9/4/2014.
 * Data class for passing data about posts
 */
public class Report {
    public List<String> networksShared;
    public double latitude, longitude;
    public byte[] pic1, pic2, pic3;

    public String   title, details, timeStamp, timeElapsed, userName,
                    media1URL, media2URL, media3URL;
    public final static String titleKey = "title",
                            detailsKey = "details",
                            timeStampKey = "timestamp",
                            timeElapsedKey = "timeElapsed",
                            userNameKey = "user",
                            pic1Key = "pic1",
                            pic2Key = "pic2",
                            pic3Key = "pic3",
                            media1URLKey = "media1URL",
                            media2URLKey = "media2URL",
                            media3URLKey = "media3URL",
                            latKey = "latitude",
                            lonKey = "longitude";

    public Report() {
        title = details = timeStamp = timeElapsed = userName = media1URL = media2URL = media3URL = "";
        latitude = longitude = 0;
    }

    // for Report objects created by the user to send to the server
    public Report(String details, String userName, Location location,
                  byte[] pic1, byte[] pic2, byte[] pic3) {
        this.details = details;
        this.timeStamp = createTimeStamp();
        this.userName = userName;
        this.latitude = location.getLatitude();
        this.longitude =  location.getLongitude();
        this.pic1 = pic1;
        this.pic2 = pic2;
        this.pic3 = pic3;
    }

    public Report(JSONObject jsonServerData, List<String> networksShared) {
        try {
            this.title = jsonServerData.getString(titleKey);
            this.details = jsonServerData.getString(detailsKey);
            this.timeStamp = jsonServerData.getString(timeStampKey);
            this.timeElapsed = getElapsedTime(this.timeStamp);
            this.userName = jsonServerData.getString(userNameKey);
            this.media1URL = jsonServerData.getString(media1URLKey);
            this.media2URL = jsonServerData.getString(media2URLKey);
            this.media3URL = jsonServerData.getString(media3URLKey);
            this.latitude = jsonServerData.getLong(latKey);
            this.longitude = jsonServerData.getLong(lonKey);
            if (networksShared != null)
                this.networksShared = networksShared;
        } catch (JSONException e) {
            e.printStackTrace();
           Log.e(LogTags.JSON, "Failed to convert data from JSON");
        }
    }

    public Report(Bundle savedState) {
        this.title = savedState.getString(titleKey);
        this.details = savedState.getString(detailsKey);
        this.timeStamp = savedState.getString(timeStampKey);
        this.timeElapsed = getElapsedTime(this.timeStamp);
        this.userName = savedState.getString(userNameKey);
        this.media1URL = savedState.getString(media1URLKey);
        this.media2URL = savedState.getString(media2URLKey);
        this.media3URL = savedState.getString(media3URLKey);
        this.latitude = savedState.getDouble(latKey);
        this.longitude = savedState.getDouble(lonKey);
//        this.networksShared = savedState.getStringArrayList(networksKey, (ArrayList<String>) savedState.networksShared);
    }

    public JSONObject getJson() {
        try {
            JSONObject json = new JSONObject();
            json.put(detailsKey, this.details);
            json.put(timeStampKey, this.timeStamp);
            json.put(timeElapsedKey, this.timeElapsed);
            json.put(userNameKey, this.userName);
            json.put(latKey, this.latitude);
            json.put(lonKey, this.longitude);
            json.put(pic1Key, Base64.encodeToString(this.pic1, Base64.DEFAULT));
            json.put(pic2Key, Base64.encodeToString(this.pic2, Base64.DEFAULT));
            json.put(pic3Key, Base64.encodeToString(this.pic3, Base64.DEFAULT));
            Log.e(LogTags.JSON, json.toString());
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
//            Log.e(LogTags.JSON, "Failed to convert data to JSON");
        }
        return null;
    }

    public Bundle saveState(Bundle outState) {
        outState.putString(titleKey, this.title);
        outState.putString(detailsKey, this.details);
        outState.putString(timeStampKey, this.timeStamp);
        outState.putString(timeElapsedKey, this.timeElapsed);
        outState.putString(userNameKey, this.userName);
        outState.putString(media1URLKey, this.media1URL);
        outState.putString(media2URLKey, this.media2URL);
        outState.putString(media3URLKey, this.media3URL);
        outState.putDouble(latKey, this.latitude);
        outState.putDouble(lonKey, this.longitude);
        return outState;
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

        // takes a timestamp in format "yyyy-MM-dd'T'H:mm:ss"
    public static String getElapsedTime(String timestamp) {
//        Log.d(LogTags.BACKEND_W, "Received timestamp: " + timestamp);
        SimpleDateFormat df = new SimpleDateFormat("H:mm:ss dd-MM-yyyy");
        try {
            long postEpochTime = df.parse(timestamp).getTime();
            long currentEpochTime = System.currentTimeMillis();
            return getHumanReadableTimeElapsed(currentEpochTime - postEpochTime, df.parse(timestamp));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String createTimeStamp() {
        return new SimpleDateFormat("H:mm:ss dd-MM-yyyy")
                .format(new java.util.Date(System.currentTimeMillis()));
    }
}