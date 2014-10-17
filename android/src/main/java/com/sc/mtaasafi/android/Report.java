package com.sc.mtaasafi.android;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    public int id;
    public double latitude, longitude;
    public ArrayList<String> picPaths;
    public String title, details, timeStamp, timeElapsed, userName;
    public ArrayList<String> mediaURLs;
    public int voteCount;
    public boolean iUpvoted;
    private final static int FIELD_TEXT = 0;
    public final static String titleKey = "title",
                            detailsKey = "details",
                            timeStampKey = "timestamp",
                            userNameKey = "user",
                            picsKey = "picPaths",
                            mediaURLsKey = "mediaURLs",
                            latKey = "latitude",
                            lonKey = "longitude";

    // for Report objects created by the user to send to the server
    public Report(String details, String userName, Location location,
                  ArrayList<String> picPaths) {
        this.details = details;
        this.timeStamp = createTimeStamp();
        this.userName = userName;
        this.latitude = location.getLatitude();
        this.longitude =  location.getLongitude();
        this.picPaths = picPaths;
        Log.e(LogTags.NEWREPORT, "In Report(): # pics" +
                picPaths.get(0).toString() + ". " +
                picPaths.get(1).toString() +". " +
                picPaths.get(2).toString());
    }

    public Report(JSONObject jsonServerData) {
        try {
            this.title = jsonServerData.getString(titleKey);
            this.details = jsonServerData.getString(detailsKey);
            this.timeStamp = jsonServerData.getString(timeStampKey);
            this.timeElapsed = getElapsedTime(this.timeStamp);
            this.userName = jsonServerData.getString(userNameKey);
            JSONArray mediaURLsInJSON = jsonServerData.getJSONArray(mediaURLsKey);
            mediaURLs = new ArrayList<String>();
            for(int i = 0; i < mediaURLsInJSON.length(); i++)
                mediaURLs.add(mediaURLsInJSON.get(i).toString());
            this.latitude = jsonServerData.getLong(latKey);
            this.longitude = jsonServerData.getLong(lonKey);
        } catch (JSONException e) {
            e.printStackTrace();
           Log.e(LogTags.JSON, "Failed to convert data from JSON");
        }
    }

    public Report(String report_key, Bundle savedState) {
        this.title = savedState.getString(report_key+titleKey);
        this.details = savedState.getString(report_key+detailsKey);
        this.timeStamp = savedState.getString(report_key+timeStampKey);
        this.timeElapsed = getElapsedTime(this.timeStamp);
        this.userName = savedState.getString(report_key+userNameKey);
        this.latitude = savedState.getDouble(report_key+latKey);
        this.longitude = savedState.getDouble(report_key+lonKey);
        if (savedState.getStringArray(report_key+mediaURLsKey) != null)
            this.mediaURLs = new ArrayList<String>(Arrays.asList(savedState.getStringArray(report_key+mediaURLsKey)));
        if (savedState.getStringArrayList(report_key+picsKey) != null)
            this.picPaths = savedState.getStringArrayList(report_key+picsKey);
    }

    public JSONObject getJsonForText() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(detailsKey, this.details);
        json.put(timeStampKey, this.timeStamp);
        json.put(userNameKey, this.userName);
        json.put(latKey, this.latitude);
        json.put(lonKey, this.longitude);
        return json;
    }

    public JSONObject getJsonForPic(int i) throws JSONException, FileNotFoundException, IOException {
        return convertPicFileToJson(new JSONObject(), new File(picPaths.get(i - 1)));
    }

    public JSONObject getJson() {
        try {
            JSONObject json = getJsonForText();
            Log.e(LogTags.JSON, "Pics Size: " + picPaths.size());
            for (int i = 0; i < picPaths.size(); i++)
                json = convertPicFileToJson(json, new File(picPaths.get(i)));
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LogTags.BACKEND_W, "Failed to convert data to JSON");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject convertPicFileToJson(JSONObject json, File file) throws IOException, JSONException {
        byte[] b = new byte[(int) file.length()];
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        inputStream.read(b);
        json.accumulate(picsKey, Base64.encodeToString(b, Base64.DEFAULT));
        inputStream.close();
        return json;
    }

    public Bundle saveState(String report_key, Bundle outState) {
        outState.putString(report_key+titleKey, this.title);
        outState.putString(report_key+detailsKey, this.details);
        outState.putString(report_key+timeStampKey, this.timeStamp);
        outState.putString(report_key+userNameKey, this.userName);
        if (mediaURLs != null)
            outState.putStringArray(report_key+mediaURLsKey,
                    this.mediaURLs.toArray(new String[mediaURLs.size()]));
        outState.putDouble(report_key+latKey, this.latitude);
        outState.putDouble(report_key+lonKey, this.longitude);
        if(picPaths != null && !picPaths.isEmpty())
            outState.putStringArrayList(report_key+picsKey, this.picPaths);
        Log.e("REPORT", "SaveState: " + outState.getString(timeStampKey));
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
        return "";
    }

    private String createTimeStamp() {
        return new SimpleDateFormat("H:mm:ss dd-MM-yyyy")
                .format(new java.util.Date(System.currentTimeMillis()));
    }
}
