package com.sc.mtaasafi.android;

import android.database.Cursor;
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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// Created by Agree on 9/4/2014.
public class Report {
    public long id;
    public double latitude, longitude;
    public ArrayList<String> picPaths;
    public String title, details, timeStamp, timeElapsed, userName;
    public ArrayList<String> mediaURLs;
    public final static String TITLE_KEY = "title",
                            DETAILS_KEY = "details",
                            TIMESTAMP_KEY = "timestamp",
                            USERNAME_KEY = "user",
                            PICS_KEY = "picPaths",
                            MEDIAURLS_KEY = "mediaURLs",
                            LAT_KEY = "latitude",
                            LNG_KEY = "longitude",
                            ID_KEY = "id";

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
        this.id = 0;
    }

    public Report(Cursor cursor) {
        this.id = cursor.getLong(0);
        this.title = cursor.getString(cursor.getColumnIndex(TITLE_KEY));
        this.details = cursor.getString(cursor.getColumnIndex(DETAILS_KEY));
        this.timeStamp = cursor.getString(cursor.getColumnIndex(TIMESTAMP_KEY));
        this.userName = cursor.getString(cursor.getColumnIndex(USERNAME_KEY));
        this.latitude = cursor.getLong(cursor.getColumnIndex(LAT_KEY));
        this.longitude = cursor.getLong(cursor.getColumnIndex(LNG_KEY));

        this.timeElapsed = getElapsedTime(this.timeStamp);
    }

    public Report(JSONObject jsonServerData) {
        try {
            this.id = 0;
            this.title = jsonServerData.getString(TITLE_KEY);
            this.details = jsonServerData.getString(DETAILS_KEY);
            this.timeStamp = jsonServerData.getString(TIMESTAMP_KEY);
            this.timeElapsed = getElapsedTime(this.timeStamp);
            this.userName = jsonServerData.getString(USERNAME_KEY);
            this.latitude = jsonServerData.getLong(LAT_KEY);
            this.longitude = jsonServerData.getLong(LNG_KEY);

            JSONArray mediaURLsInJSON = jsonServerData.getJSONArray(MEDIAURLS_KEY);
            mediaURLs = new ArrayList<String>();
            for(int i = 0; i < mediaURLsInJSON.length(); i++)
                mediaURLs.add(mediaURLsInJSON.get(i).toString());
        } catch (JSONException e) {
            e.printStackTrace();
           Log.e(LogTags.JSON, "Failed to convert data from JSON");
        }
    }

    public Report(Bundle savedState) {
        this.id = savedState.getLong(ID_KEY);
        this.title = savedState.getString(TITLE_KEY);
        this.details = savedState.getString(DETAILS_KEY);
        this.timeStamp = savedState.getString(TIMESTAMP_KEY);
        this.timeElapsed = getElapsedTime(this.timeStamp);
        this.userName = savedState.getString(USERNAME_KEY);
        this.latitude = savedState.getDouble(LAT_KEY);
        this.longitude = savedState.getDouble(LNG_KEY);
        if (savedState.getStringArray(MEDIAURLS_KEY) != null)
            this.mediaURLs = new ArrayList<String>(Arrays.asList(savedState.getStringArray(MEDIAURLS_KEY)));
        if (savedState.getStringArrayList(PICS_KEY) != null)
            this.picPaths = savedState.getStringArrayList(PICS_KEY);
    }

    public JSONObject getJsonForText() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(DETAILS_KEY, this.details);
        json.put(TIMESTAMP_KEY, this.timeStamp);
        json.put(USERNAME_KEY, this.userName);
        json.put(LAT_KEY, this.latitude);
        json.put(LNG_KEY, this.longitude);
        return json;
    }

    public JSONObject getJsonForPic(int i) throws JSONException, IOException {
        return new JSONObject().accumulate(PICS_KEY, getEncodedBytesForPic(i));
    }

    private String getEncodedBytesForPic(int i) throws IOException {
        String encoded = Base64.encodeToString(getBytesForPic(i), Base64.DEFAULT);
        Log.e(LogTags.NEWREPORT, "Encoded string size: " + encoded.getBytes().length);
        return encoded;
    }
    public byte[] getBytesForPic(int i) throws IOException {
        File file = new File(picPaths.get(i));
        byte[] b = new byte[(int) file.length()];
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        inputStream.read(b);
        inputStream.close();
        return b;
    }

    public Bundle saveState(Bundle outState) {
        outState.putLong(ID_KEY, this.id);
        outState.putString(TITLE_KEY, this.title);
        outState.putString(DETAILS_KEY, this.details);
        outState.putString(TIMESTAMP_KEY, this.timeStamp);
        outState.putString(USERNAME_KEY, this.userName);
        if (mediaURLs != null)
            outState.putStringArray(MEDIAURLS_KEY,
                    this.mediaURLs.toArray(new String[mediaURLs.size()]));
        outState.putDouble(LAT_KEY, this.latitude);
        outState.putDouble(LNG_KEY, this.longitude);
        if(picPaths != null && !picPaths.isEmpty())
            outState.putStringArrayList(PICS_KEY, this.picPaths);
        Log.e("REPORT", "SaveState: " + outState.getString(TIMESTAMP_KEY));
        return outState;
    }

    // from: http://stackoverflow.com/questions/5980658/how-to-sha1-hash-a-string-in-android
    public String getSHA1forPic(int i) throws IOException, NoSuchAlgorithmException {
        String toHash = getEncodedBytesForPic(i);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(toHash.getBytes("iso-8859-1"), 0, toHash.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    private String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
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
