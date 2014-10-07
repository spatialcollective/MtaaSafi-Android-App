package com.sc.mtaasafi.android;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Agree on 9/4/2014.
 * Data class for passing data about posts
 */
public class Report {
    public double latitude, longitude;
    public List<byte[]> pics;
    public String   title, details, timeStamp, timeElapsed, userName;
    public List<String> mediaURLs;
    public final static String titleKey = "title",
                            detailsKey = "details",
                            timeStampKey = "timestamp",
                            userNameKey = "user",
                            picsKey = "pics",
                            mediaURLsKey = "mediaURLs",
                            latKey = "latitude",
                            lonKey = "longitude";

    // for Report objects created by the user to send to the server
    public Report(String details, String userName, Location location,
                  List<byte[]> pics) {
        this.details = details;
        this.timeStamp = createTimeStamp();
        this.userName = userName;
        this.latitude = location.getLatitude();
        this.longitude =  location.getLongitude();
        this.pics = pics;
    }

    public Report(JSONObject jsonServerData) {
        try {
            this.title = jsonServerData.getString(titleKey);
            this.details = jsonServerData.getString(detailsKey);
            this.timeStamp = jsonServerData.getString(timeStampKey);
            this.timeElapsed = getElapsedTime(this.timeStamp);
            this.userName = jsonServerData.getString(userNameKey);
//            JSONArray mediaURLsInJSON = jsonServerData.getJSONArray(mediaURLsKey);
//            mediaURLs = new ArrayList<String>();
//            for(int i = 0; i < mediaURLsInJSON.length(); i++){
//              mediaURLs.add(mediaURLsInJSON.get(i).toString());
//            }
            this.latitude = jsonServerData.getLong(latKey);
            this.longitude = jsonServerData.getLong(lonKey);
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
        this.mediaURLs = new ArrayList<String>(Arrays.asList(savedState.getStringArray(mediaURLsKey)));
        this.latitude = savedState.getDouble(latKey);
        this.longitude = savedState.getDouble(lonKey);
    }

    public JSONObject getJson() {
        try {
            JSONObject json = new JSONObject();
            json.put(detailsKey, this.details);
            json.put(timeStampKey, this.timeStamp);
            json.put(userNameKey, this.userName);
            json.put(latKey, this.latitude);
            json.put(lonKey, this.longitude);

            JSONArray jsonPics = new JSONArray();
            for(byte[] pic : pics){
                jsonPics.put(Base64.encodeToString(pic, Base64.DEFAULT));
            }
            json.put(picsKey, jsonPics);

            Log.e(LogTags.JSON, json.toString());
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LogTags.BACKEND_W, "Failed to convert data to JSON");
        }
        return null;
    }

    public Bundle saveState(Bundle outState) {
        outState.putString(titleKey, this.title);
        outState.putString(detailsKey, this.details);
        outState.putString(timeStampKey, this.timeStamp);
        outState.putString(userNameKey, this.userName);
        outState.putStringArray(mediaURLsKey, (String[]) this.mediaURLs.toArray());
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