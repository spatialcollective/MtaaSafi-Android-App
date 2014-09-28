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
    public byte[] picture;

    public String title, details, timeElapsed, userName, picURL, mediaURL;
    public final static String titleKey = "title",
                            detailsKey = "details",
                            timeElapsedKey = "timestamp",
                            userNameKey = "user",
                            mediaKey = "media",
                            mediaURLKey = "mediaURL",
                            latKey = "latitude",
                            lonKey = "longitude",
                            networksKey = "networksShared";

    public Report() {
        title = details = timeElapsed = userName = picURL = mediaURL = "";
        latitude = longitude = 0;
    }

    // for Report objects created by the user to send to the server
    public Report(String title, String details, String userName, Location location) {
        this.title = title;
        this.details = details;
        this.timeElapsed = createTimeStamp();
        this.userName = userName;
        this.latitude = location.getLatitude();
        this.longitude =  location.getLongitude();
    }

    public Report(JSONObject jsonServerData, List<String> networksShared) {
        try {
            this.title = jsonServerData.getString(titleKey);
            this.details = jsonServerData.getString(detailsKey);
            this.timeElapsed = getElapsedTime(jsonServerData.getString(timeElapsedKey));
            this.userName = jsonServerData.getString(userNameKey);
            this.mediaURL = jsonServerData.getString(mediaURLKey);
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
        this.timeElapsed = getElapsedTime(savedState.getString(timeElapsedKey));
        this.userName = savedState.getString(userNameKey);
        this.mediaURL = savedState.getString(mediaURLKey);
        this.latitude = savedState.getDouble(latKey);
        this.longitude = savedState.getDouble(lonKey);

//        this.networksShared = savedState.getStringArrayList(networksKey, (ArrayList<String>) savedState.networksShared);
    }

    public void addPic(byte[] pic) {
        this.picture = pic;
    }

    public JSONObject getJson() {
        try {
            JSONObject json = new JSONObject();
            json.put(titleKey, this.title);
            json.put(detailsKey, this.details);
            json.put(timeElapsedKey, this.timeElapsed);
            json.put(userNameKey, this.userName);
            json.put(latKey, this.latitude);
            json.put(lonKey, this.longitude);
            if (this.picture != null) {
                String encodedImage = Base64.encodeToString(this.picture, Base64.DEFAULT);
                json.put(mediaKey, encodedImage);
            }
            Log.e(LogTags.JSON, json.toString());
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LogTags.JSON, "Failed to convert data to JSON");
        }
        return null;
    }

    public Bundle saveState(Bundle outState) {
        outState.putString(titleKey, this.title);
        outState.putString(detailsKey, this.details);
        outState.putString(userNameKey, this.userName);
        outState.putString(mediaURLKey, this.mediaURL);
        outState.putString(timeElapsedKey, this.timeElapsed);
        outState.putDouble(latKey, this.latitude);
        outState.putDouble(lonKey, this.longitude);
        outState.putStringArrayList(networksKey, (ArrayList<String>) this.networksShared);
        return outState;
    }


    public static String getHumanReadableTimeElapsed(long timeElapsed, Date date) {
        long second = 1000,
            minute = 60 * second,
            hour = 60* minute,
            day = 24 * hour,
            week = 7 * day,
            year = 365 * day;

        if (timeElapsed > year) {
            return new SimpleDateFormat("dd LLL yy").format(date);
        } else if (timeElapsed > week) {
            return new SimpleDateFormat("dd LLL").format(date);
        } else if (timeElapsed > 1.5 * day) {
            return Math.floor((week - timeElapsed)/day) + " days";
        } else if (timeElapsed > day) {
            return "1 day";
        } else if (timeElapsed > hour) {
            return Math.floor((day - timeElapsed)/hour) + " hours";
        } else if (timeElapsed > minute) {
            return Math.floor((hour - timeElapsed)/minute) + " min";
        }
        return "just now";
    }

        // takes a timestamp in format "yyyy-MM-dd'T'H:mm:ss"
    public static String getElapsedTime(String timestamp) {
        Log.d(LogTags.BACKEND_W, "Received timestamp: " + timestamp);
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
        return new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss")
                .format(new java.util.Date(System.currentTimeMillis()));
    }
}