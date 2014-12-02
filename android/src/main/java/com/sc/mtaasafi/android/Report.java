package com.sc.mtaasafi.android;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import android.location.Location;

import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
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
import java.util.Date;

// Created by Agree on 9/4/2014.
public class Report {
    public boolean upVoted = false;
    public int serverId, dbId, pendingState = -1, upVoteCount, inProgress = 0;
    public double latitude, longitude;
    public String locationDescript, content, timeElapsed, userName;
    public long timeStamp;
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
        this.timeStamp = System.currentTimeMillis();
        this.userName = userName;
        this.latitude = location.getLatitude();
        this.longitude =  location.getLongitude();
        this.mediaPaths = picPaths;
        this.serverId = this.dbId = 0;
    }

    public Report(Bundle bundle) {
        mediaPaths = new ArrayList<String>();
        content = bundle.getString(Contract.Entry.COLUMN_CONTENT);
        locationDescript = bundle.getString(Contract.Entry.COLUMN_LOCATION);
        timeStamp = bundle.getLong(Contract.Entry.COLUMN_TIMESTAMP);
        userName = bundle.getString(Contract.Entry.COLUMN_USERNAME);
        mediaPaths.add(bundle.getString(Contract.Entry.COLUMN_MEDIAURL1));
        mediaPaths.add(bundle.getString(Contract.Entry.COLUMN_MEDIAURL2));
        mediaPaths.add(bundle.getString(Contract.Entry.COLUMN_MEDIAURL3));
        serverId = bundle.getInt(Contract.Entry.COLUMN_SERVER_ID);
        dbId = bundle.getInt(Contract.Entry.COLUMN_ID);
        double lat = bundle.getDouble(Contract.Entry.COLUMN_LAT);
        double lon = bundle.getDouble(Contract.Entry.COLUMN_LNG);
        location = new Location("report_location");
        location.setLatitude(lat);
        location.setLongitude(lon);
        upVoted = bundle.getBoolean(Contract.Entry.COLUMN_USER_UPVOTED);
        upVoteCount = bundle.getInt(Contract.Entry.COLUMN_UPVOTE_COUNT);
        // make sure this method's only called after inflation
    }

    public Report(Cursor c) {
        content = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_CONTENT));
        locationDescript = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_LOCATION));
        timeStamp = c.getLong(c.getColumnIndex(Contract.Entry.COLUMN_TIMESTAMP));
        timeElapsed = getElapsedTime(timeStamp);
        userName = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_USERNAME));
        if(userName.equals(""))
            userName = "Unknown user";

        mediaPaths = new ArrayList<String>();
        mediaPaths.add(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL1)));
        String mediaPath2 = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL2));
        if(mediaPath2 != null)
            mediaPaths.add(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL2)));
        String mediaPath3 = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL3));
        if(mediaPath3 != null)
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
        timeStamp = jsonData.getLong(Contract.Entry.COLUMN_TIMESTAMP);
        timeElapsed = getElapsedTime(this.timeStamp);
        userName = jsonData.getString(Contract.Entry.COLUMN_USERNAME);
        latitude = jsonData.getDouble(Contract.Entry.COLUMN_LAT);
        longitude = jsonData.getDouble(Contract.Entry.COLUMN_LNG);
        upVoteCount = jsonData.getInt(Contract.Entry.COLUMN_UPVOTE_COUNT);
        upVoted = jsonData.getBoolean(Contract.Entry.COLUMN_USER_UPVOTED);
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
        if(mediaPaths.get(1) != null)
            reportValues.put(Contract.Entry.COLUMN_MEDIAURL2, mediaPaths.get(1));
        if(mediaPaths.get(2) != null)
            reportValues.put(Contract.Entry.COLUMN_MEDIAURL3, mediaPaths.get(2));
        reportValues.put(Contract.Entry.COLUMN_PENDINGFLAG, pendingState);
        reportValues.put(Contract.Entry.COLUMN_UPVOTE_COUNT, upVoteCount);
        if (upVoted)
            reportValues.put(Contract.Entry.COLUMN_USER_UPVOTED, 1);
        else
            reportValues.put(Contract.Entry.COLUMN_USER_UPVOTED, 0);
        return reportValues;
    }

    public Bundle saveState(Bundle output) {
        output.putString(Contract.Entry.COLUMN_CONTENT, content);
        output.putString(Contract.Entry.COLUMN_LOCATION, locationDescript);
        output.putLong(Contract.Entry.COLUMN_TIMESTAMP, timeStamp);
        output.putString(Contract.Entry.COLUMN_USERNAME, userName);
        output.putString(Contract.Entry.COLUMN_MEDIAURL1, mediaPaths.get(0));
        output.putString(Contract.Entry.COLUMN_MEDIAURL2, mediaPaths.get(1));
        output.putString(Contract.Entry.COLUMN_MEDIAURL3, mediaPaths.get(2));
        output.putInt(Contract.Entry.COLUMN_SERVER_ID, serverId);
        output.putInt(Contract.Entry.COLUMN_ID, dbId);
        output.putDouble(Contract.Entry.COLUMN_LAT, location.getLatitude());
        output.putDouble(Contract.Entry.COLUMN_LNG, location.getLongitude());
        output.putBoolean(Contract.Entry.COLUMN_USER_UPVOTED, upVoted);
        output.putInt(Contract.Entry.COLUMN_UPVOTE_COUNT, upVoteCount);
        return output;
    }
    
    public static Uri getUri(int dbId) {
        return Contract.Entry.CONTENT_URI.buildUpon().appendPath(Integer.toString(dbId)).build();
    }
    public static int serverIdToDBId(Context c, int serverId){
        String[] projection = new String[1];
        projection[0] = Contract.Entry.COLUMN_ID;
        Cursor cursor = c.getContentResolver().query(Contract.Entry.CONTENT_URI, projection,
                                                    Contract.Entry.COLUMN_SERVER_ID + " = " + serverId,
                                                    null, null);
        if(cursor.moveToNext()){
            int dbId = cursor.getInt(cursor.getColumnIndex(Contract.Entry.COLUMN_ID));
            cursor.close();
            return dbId;
        }
        return -1;
    }

    public String getJsonStringRep() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Contract.Entry.COLUMN_CONTENT, this.content);
        json.put(Contract.Entry.COLUMN_TIMESTAMP, this.timeStamp);
        json.put(Contract.Entry.COLUMN_USERNAME, this.userName);
        json.put(Contract.Entry.COLUMN_LAT, this.latitude);
        json.put(Contract.Entry.COLUMN_LNG, this.longitude);
//        json.put("accuracy", "High Accuracy On");
        return json.toString();
    }

    public static String getDistanceText(Location currentLocation, Double reportLat, Double reportLng) {
        Location reportLocation = new Location("ReportLocation");
        reportLocation.setLatitude(reportLat);
        reportLocation.setLongitude(reportLng);
        return getDistanceText(currentLocation, reportLocation);
    }

	public String getDistanceText(Location currentLocation) {
        if (location == null)
            return "error";
        return getDistanceText(currentLocation, location);
    }

    public static String getDistanceText(Location current, Location reportLoc) {
        float distInMeters = reportLoc.distanceTo(current);
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

    public static String getElapsedTime(long timestamp) {
        if (timestamp != 0)
            return getHumanReadableTimeElapsed(System.currentTimeMillis() - timestamp, new Date(timestamp));
        return "";
    }

    private String getEncodedBytesForPic(int i) throws IOException {
        String encoded = Base64.encodeToString(getBytesForPic(i), Base64.DEFAULT);
        Log.e(LogTags.NEWREPORT, "Encoded string size: " + encoded.getBytes().length);
        return encoded;
    }
    public byte[] getBytesForPic(int i) throws IOException {
        File file = new File(mediaPaths.get(i));
        byte[] b = new byte[(int) file.length()];
        Log.e("File path", file.getAbsolutePath());
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
