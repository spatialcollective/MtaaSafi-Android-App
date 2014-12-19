package com.sc.mtaa_safi;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import android.location.Location;

import com.sc.mtaa_safi.SystemUtils.LogTags;
import com.sc.mtaa_safi.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Report {
    public boolean upVoted = false;
    public int serverId, dbId, pendingState = -1, upVoteCount, inProgress = 0;
    public String locationDescript, content, timeElapsed, userName;
    public long timeStamp;
    public ArrayList<String> mediaPaths = new ArrayList<String>();
    public String[] mediaUrls = {Contract.Entry.COLUMN_MEDIAURL1, Contract.Entry.COLUMN_MEDIAURL2, Contract.Entry.COLUMN_MEDIAURL3};
    public Location location;
            
    public static final String[] PROJECTION = new String[] {
            Contract.Entry.COLUMN_ID,
            Contract.Entry.COLUMN_SERVER_ID,
            Contract.Entry.COLUMN_CONTENT,
            Contract.Entry.COLUMN_HUMAN_LOC,
            Contract.Entry.COLUMN_TIMESTAMP,
            Contract.Entry.COLUMN_LAT,
            Contract.Entry.COLUMN_LNG,
            Contract.Entry.COLUMN_LOC_ACC,
            Contract.Entry.COLUMN_LOC_TIME,
            Contract.Entry.COLUMN_LOC_PROV,
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
    public Report(String details, String userName, Location newLocation, ArrayList<String> picPaths) {
        this.content = details;
        this.locationDescript = "";
        this.pendingState = 0;
        this.timeStamp = System.currentTimeMillis();
        this.userName = userName;
        this.location = newLocation;
        this.mediaPaths = picPaths;
        this.serverId = this.dbId = 0;
    }

    public Report(Bundle bundle) {
        serverId = bundle.getInt(Contract.Entry.COLUMN_SERVER_ID);
        dbId = bundle.getInt(Contract.Entry.COLUMN_ID);
        content = bundle.getString(Contract.Entry.COLUMN_CONTENT);
        locationDescript = bundle.getString(Contract.Entry.COLUMN_HUMAN_LOC);
        timeStamp = bundle.getLong(Contract.Entry.COLUMN_TIMESTAMP);
        userName = bundle.getString(Contract.Entry.COLUMN_USERNAME);
        for (int i = 0; i < mediaUrls.length; i++) {
            if (bundle.getString(mediaUrls[i]) != null && !bundle.getString(mediaUrls[i]).isEmpty())
                mediaPaths.add(bundle.getString(mediaUrls[i]));
        }
        
        location = new Location("report_location");
        location.setLatitude(bundle.getDouble(Contract.Entry.COLUMN_LAT));
        location.setLongitude(bundle.getDouble(Contract.Entry.COLUMN_LNG));
        location.setAccuracy(bundle.getFloat(Contract.Entry.COLUMN_LOC_ACC));
        location.setTime(bundle.getLong(Contract.Entry.COLUMN_LOC_TIME));
        location.setProvider(bundle.getString(Contract.Entry.COLUMN_LOC_PROV));

        upVoted = bundle.getBoolean(Contract.Entry.COLUMN_USER_UPVOTED);
        upVoteCount = bundle.getInt(Contract.Entry.COLUMN_UPVOTE_COUNT);
    }

    public Report(Cursor c) {
        serverId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID));
        dbId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ID));
        pendingState = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG));
        content = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_CONTENT));
        locationDescript = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_HUMAN_LOC));
        timeStamp = c.getLong(c.getColumnIndex(Contract.Entry.COLUMN_TIMESTAMP));
        timeElapsed = getElapsedTime(timeStamp);
        userName = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_USERNAME));
        if(userName.equals(""))
            userName = "Unknown user";

        for (int i = 0; i < mediaUrls.length; i++) {
            if (c.getString(c.getColumnIndex(mediaUrls[i])) != null && !c.getString(c.getColumnIndex(mediaUrls[i])).isEmpty())
                mediaPaths.add(c.getString(c.getColumnIndex(mediaUrls[i])));
        }

        upVoted = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_USER_UPVOTED)) > 0;
        upVoteCount = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPVOTE_COUNT));

        location = new Location("ReportLocation");
        location.setLatitude(c.getDouble(c.getColumnIndex(Contract.Entry.COLUMN_LAT)));
        location.setLongitude(c.getDouble(c.getColumnIndex(Contract.Entry.COLUMN_LNG)));
        location.setAccuracy(c.getFloat(c.getColumnIndex(Contract.Entry.COLUMN_LOC_ACC)));
        location.setTime(c.getLong(c.getColumnIndex(Contract.Entry.COLUMN_LOC_TIME)));
        location.setProvider(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_LOC_PROV)));
    }

    public Report(JSONObject jsonData, int pending) throws JSONException {
        serverId = jsonData.getInt("unique_id");
        locationDescript = jsonData.getString(Contract.Entry.COLUMN_HUMAN_LOC);
        content = jsonData.getString(Contract.Entry.COLUMN_CONTENT);
        timeStamp = jsonData.getLong(Contract.Entry.COLUMN_TIMESTAMP);
        timeElapsed = getElapsedTime(this.timeStamp);
        userName = jsonData.getString(Contract.Entry.COLUMN_USERNAME);
        upVoteCount = jsonData.getInt(Contract.Entry.COLUMN_UPVOTE_COUNT);
        upVoted = jsonData.getBoolean(Contract.Entry.COLUMN_USER_UPVOTED);
        pendingState = pending;
        
        JSONArray mediaPathsInJSON = jsonData.getJSONArray("mediaURLs");
        for (int i = 0; i < mediaPathsInJSON.length(); i++)
            mediaPaths.add(mediaPathsInJSON.get(i).toString());

        location = new Location("ReportLocation");
        location.setLatitude(jsonData.getDouble(Contract.Entry.COLUMN_LAT));
        location.setLongitude(jsonData.getDouble(Contract.Entry.COLUMN_LNG));
    }

    public ContentValues getContentValues() {
        ContentValues reportValues = new ContentValues();
        reportValues.put(Contract.Entry.COLUMN_SERVER_ID, serverId);
        reportValues.put(Contract.Entry.COLUMN_HUMAN_LOC, locationDescript);
        reportValues.put(Contract.Entry.COLUMN_CONTENT, content);
        reportValues.put(Contract.Entry.COLUMN_TIMESTAMP, timeStamp);

        reportValues.put(Contract.Entry.COLUMN_USERNAME, userName);
        for (int i = 0; i < mediaPaths.size(); i++)
            reportValues.put(mediaUrls[i], mediaPaths.get(i));
        reportValues.put(Contract.Entry.COLUMN_PENDINGFLAG, pendingState);
        reportValues.put(Contract.Entry.COLUMN_UPVOTE_COUNT, upVoteCount);
        if (upVoted)
            reportValues.put(Contract.Entry.COLUMN_USER_UPVOTED, 1);
        else
            reportValues.put(Contract.Entry.COLUMN_USER_UPVOTED, 0);

        reportValues.put(Contract.Entry.COLUMN_LAT, location.getLatitude());
        reportValues.put(Contract.Entry.COLUMN_LNG, location.getLongitude());
        reportValues.put(Contract.Entry.COLUMN_LOC_ACC, location.getAccuracy());
        reportValues.put(Contract.Entry.COLUMN_LOC_TIME, location.getTime());
        reportValues.put(Contract.Entry.COLUMN_LOC_PROV, location.getProvider());
        return reportValues;
    }

    public Bundle saveState(Bundle output) {
        output.putString(Contract.Entry.COLUMN_CONTENT, content);
        output.putString(Contract.Entry.COLUMN_HUMAN_LOC, locationDescript);
        output.putLong(Contract.Entry.COLUMN_TIMESTAMP, timeStamp);
        output.putString(Contract.Entry.COLUMN_USERNAME, userName);
        for (int i = 0; i < mediaPaths.size(); i++)
            output.putString(mediaUrls[i], mediaPaths.get(i));
        output.putInt(Contract.Entry.COLUMN_SERVER_ID, serverId);
        output.putInt(Contract.Entry.COLUMN_ID, dbId);
        output.putBoolean(Contract.Entry.COLUMN_USER_UPVOTED, upVoted);
        output.putInt(Contract.Entry.COLUMN_UPVOTE_COUNT, upVoteCount);
        output.putDouble(Contract.Entry.COLUMN_LAT, location.getLatitude());
        output.putDouble(Contract.Entry.COLUMN_LNG, location.getLongitude());
        output.putFloat(Contract.Entry.COLUMN_LOC_ACC, location.getAccuracy());
        output.putLong(Contract.Entry.COLUMN_LOC_TIME, location.getTime());
        output.putString(Contract.Entry.COLUMN_LOC_PROV, location.getProvider());
        return output;
    }
    
    public static Uri getUri(int dbId) {
        return Contract.Entry.CONTENT_URI.buildUpon().appendPath(Integer.toString(dbId)).build();
    }

    public String getJsonStringRep() throws JSONException, IOException, NoSuchAlgorithmException {
        JSONObject json = new JSONObject();
        json.put(Contract.Entry.COLUMN_CONTENT, this.content);
        json.put(Contract.Entry.COLUMN_TIMESTAMP, this.timeStamp);
        json.put(Contract.Entry.COLUMN_USERNAME, this.userName);

        JSONObject loc = new JSONObject();
        loc.put(Contract.Entry.COLUMN_LAT, location.getLatitude());
        loc.put(Contract.Entry.COLUMN_LNG, location.getLongitude());
        loc.put(Contract.Entry.COLUMN_LOC_ACC, location.getAccuracy());
        loc.put("timestamp", location.getTime());
        loc.put(Contract.Entry.COLUMN_LOC_PROV, location.getProvider());
        json.put("location", loc);

        json.put("picHashes", new JSONArray());
        for (int i = 0; i < mediaPaths.size(); i++)
            json.accumulate("picHashes", getSHA1forPic(i));
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

    public byte[] getBytesForPic(int i) throws IOException {
        File file = new File(mediaPaths.get(i));
        byte[] b = new byte[(int) file.length()];
        Log.e("File path", file.getAbsolutePath());
        FileInputStream inputStream = new FileInputStream(file); /* Remove BufferedInputStream */
        inputStream.read(b);
        inputStream.close();
        return b;
    }
    // from: http://stackoverflow.com/questions/5980658/how-to-sha1-hash-a-string-in-android
    // I believe the non-accepted answer on that page may be better. -DK
    public String getSHA1forPic(int i) throws IOException, NoSuchAlgorithmException {
        byte[] b = getBytesForPic(i);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(b, 0, b.length); // shoud be utf-8 not iso-8859-1
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
