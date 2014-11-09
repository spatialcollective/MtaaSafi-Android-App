package com.sc.mtaasafi.android;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import android.location.Location;

import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

// Created by Agree on 9/4/2014.
public class Report {
    public boolean upVoted = false;
    public int serverId, dbId, pendingState = -1, upVoteCount;
    public double latitude, longitude;
    public String locationDescript, content, timeStamp, timeElapsed, userName;
    public ArrayList<String> mediaPaths;
    public Uri uri;
    public Location location;
            
    public static final String[] PROJECTION = new String[] {
            Contract.Entry._ID,
            Contract.Entry.COLUMN_SERVER_ID,
            Contract.Entry.COLUMN_CONTENT,
            Contract.Entry.COLUMN_LOCATION,
            Contract.Entry.COLUMN_TIMESTAMP,
            Contract.Entry.COLUMN_LAT,
            Contract.Entry.COLUMN_LNG,
            Contract.Entry.COLUMN_USERNAME,
            Contract.Entry.COLUMN_MEDIAURL1,
            Contract.Entry.COLUMN_MEDIAURL2,
            Contract.Entry.COLUMN_MEDIAURL3,
            Contract.Entry.COLUMN_PENDINGFLAG,
            Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS,
            Contract.Entry.COLUMN_UPVOTE_COUNT,
            Contract.Entry.COLUMN_USER_UPVOTED
    };
    // for Report objects created by the user to send to the server
    public Report(String details, String userName, Location location,
                  ArrayList<String> picPaths) {
        this.content = details;
        this.locationDescript = "";
        this.pendingState = 0;
        this.timeStamp = createTimeStamp();
        this.userName = userName;
        this.latitude = location.getLatitude();
        this.longitude =  location.getLongitude();
        this.mediaPaths = picPaths;
        Log.e(LogTags.NEWREPORT, "In Report(): # pics" +
                mediaPaths.get(0).toString() + ". " +
                mediaPaths.get(1).toString() + ". " +
                mediaPaths.get(2).toString());
        this.serverId = this.dbId = 0;
    }

    public Report(Cursor c) {
        content = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_CONTENT));
        locationDescript = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_LOCATION));
        timeStamp = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_TIMESTAMP));
        timeElapsed = getElapsedTime(timeStamp);
        userName = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_USERNAME));
        if(userName.equals(""))
            userName = "Unknown user";

        mediaPaths = new ArrayList<String>();
        mediaPaths.add(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL1)));
        mediaPaths.add(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL2)));
        mediaPaths.add(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL3)));

        serverId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID));
        dbId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ID));
        uri = Contract.Entry.CONTENT_URI.buildUpon().appendPath(Integer.toString(dbId)).build();
        pendingState = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG));

        upVoted = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_USER_UPVOTED)) > 0;
        upVoteCount = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPVOTE_COUNT));

        latitude = c.getDouble(c.getColumnIndex(Contract.Entry.COLUMN_LAT));
        longitude = c.getDouble(c.getColumnIndex(Contract.Entry.COLUMN_LNG));
        location = new Location("ReportLocation");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
    }

    public Report(JSONObject jsonData, int pending) throws JSONException {
        serverId = jsonData.getInt("unique_id");
        locationDescript = jsonData.getString(Contract.Entry.COLUMN_LOCATION);
        content = jsonData.getString(Contract.Entry.COLUMN_CONTENT);
        timeStamp = jsonData.getString(Contract.Entry.COLUMN_TIMESTAMP);
        timeElapsed = getElapsedTime(this.timeStamp);
        userName = jsonData.getString(Contract.Entry.COLUMN_USERNAME);
        latitude = jsonData.getDouble(Contract.Entry.COLUMN_LAT);
        longitude = jsonData.getDouble(Contract.Entry.COLUMN_LNG);
        upVoteCount = jsonData.getInt(Contract.Entry.COLUMN_UPVOTE_COUNT);
        upVoted = jsonData.getBoolean("upvoted"); // Contract.Entry.COLUMN_USER_UPVOTED);
        pendingState = pending;
        
        JSONArray mediaPathsInJSON = jsonData.getJSONArray("mediaURLs");
        mediaPaths = new ArrayList<String>();
        for (int i = 0; i < mediaPathsInJSON.length(); i++)
            mediaPaths.add(mediaPathsInJSON.get(i).toString());
    }

    public ContentValues getContentValues() {
        ContentValues reportValues = new ContentValues();
        reportValues.put(Contract.Entry.COLUMN_SERVER_ID, serverId);
        reportValues.put(Contract.Entry.COLUMN_LOCATION, locationDescript);
        reportValues.put(Contract.Entry.COLUMN_CONTENT, content);
        reportValues.put(Contract.Entry.COLUMN_TIMESTAMP, timeStamp);
        reportValues.put(Contract.Entry.COLUMN_LAT, latitude);
        reportValues.put(Contract.Entry.COLUMN_LNG, longitude);
        reportValues.put(Contract.Entry.COLUMN_USERNAME, userName);
        reportValues.put(Contract.Entry.COLUMN_MEDIAURL1, mediaPaths.get(0));
        reportValues.put(Contract.Entry.COLUMN_MEDIAURL2, mediaPaths.get(1));
        reportValues.put(Contract.Entry.COLUMN_MEDIAURL3, mediaPaths.get(2));
        reportValues.put(Contract.Entry.COLUMN_PENDINGFLAG, pendingState);
        if (upVoted)
            reportValues.put(Contract.Entry.COLUMN_USER_UPVOTED, 1);
        else
            reportValues.put(Contract.Entry.COLUMN_USER_UPVOTED, 0);
        return reportValues;
    }
    
    public static Uri getUri(int dbId) {
        return Contract.Entry.CONTENT_URI.buildUpon()
            .appendPath(Integer.toString(dbId)).build();
    }

    public String getJsonStringRep() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Contract.Entry.COLUMN_CONTENT, this.content);
        json.put(Contract.Entry.COLUMN_TIMESTAMP, this.timeStamp);
        json.put(Contract.Entry.COLUMN_USERNAME, this.userName);
        json.put(Contract.Entry.COLUMN_LAT, this.latitude);
        json.put(Contract.Entry.COLUMN_LNG, this.longitude);
        return json.toString();
    }

    public static String getDistanceText(Location currentLocation, Location reportLocation){
        float distInMeters = reportLocation.distanceTo(currentLocation);
        String distText;
        if(distInMeters > 1000){
            distText = Float.toString(distInMeters/1000);
            if(distText.indexOf('.') !=-1) // show km within 1 decimal pt
                distText = distText.substring(0, distText.indexOf('.')+2);
            if(distText.endsWith(".0"))// remove any ".0"s
                distText = distText.substring(0, distText.length()-3);
            distText += "km";
        } else if(distInMeters > 30){
            distText = Float.toString(distInMeters);
            if(distText.indexOf('.') != -1)
                distText = distText.substring(0, distText.indexOf('.'));
            distText += "m";
            // if distance is in meters, only show as an integer
        } else
            distText = "here";
        return distText;
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


    private String getEncodedBytesForPic(int i) throws IOException {
        String encoded = Base64.encodeToString(getBytesForPic(i), Base64.DEFAULT);
        Log.e(LogTags.NEWREPORT, "Encoded string size: " + encoded.getBytes().length);
        return encoded;
    }
    public byte[] getBytesForPic(int i) throws IOException {
        File file = new File(mediaPaths.get(i));
        byte[] b = new byte[(int) file.length()];
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        inputStream.read(b);
        inputStream.close();
        return b;
    }
    // from: http://stackoverflow.com/questions/5980658/how-to-sha1-hash-a-string-in-android
    // I believe the non-accepted answer on that page may be better. -DK
    public String getSHA1forPic(int i) throws IOException, NoSuchAlgorithmException {
        String toHash = getEncodedBytesForPic(i);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(toHash.getBytes("UTF-8"), 0, toHash.length()); // shoud be utf-8 not iso-8859-1
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
}
