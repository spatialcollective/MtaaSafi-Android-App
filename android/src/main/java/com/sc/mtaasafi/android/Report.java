package com.sc.mtaasafi.android;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import android.location.Location;

import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.database.ReportContract;

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

/**
 * Created by Agree on 9/4/2014.
 * Data class for passing data about posts
 */
public class Report {
    public int serverId, dbId;
    public double latitude, longitude;
    public String title, details, timeStamp, timeElapsed, userName;
    public ArrayList<String> mediaPaths;
    public final static String titleKey = "title",
            detailsKey = "details",
            timeStampKey = "timestamp",
            userNameKey = "user",
            mediaPathsKey = "mediaPaths",
            latKey = "latitude",
            lonKey = "longitude",
            serverIdKey = "id";
    public static final String[] PROJECTION = new String[] {
            ReportContract.Entry._ID,
            ReportContract.Entry.COLUMN_SERVER_ID,
            ReportContract.Entry.COLUMN_TITLE,
            ReportContract.Entry.COLUMN_DETAILS,
            ReportContract.Entry.COLUMN_TIMESTAMP,
            ReportContract.Entry.COLUMN_LAT,
            ReportContract.Entry.COLUMN_LNG,
            ReportContract.Entry.COLUMN_USERNAME,
            ReportContract.Entry.COLUMN_MEDIAURL1,
            ReportContract.Entry.COLUMN_MEDIAURL2,
            ReportContract.Entry.COLUMN_MEDIAURL3
    };
    // for Report objects created by the user to send to the server
    public Report(String details, String userName, Location location,
                  ArrayList<String> picPaths) {
        this.details = details;
        this.timeStamp = createTimeStamp();
        this.userName = userName;
        this.latitude = location.getLatitude();
        this.longitude =  location.getLongitude();
        this.mediaPaths = picPaths;
        Log.e(LogTags.NEWREPORT, "In Report(): # pics" +
                mediaPaths.get(0).toString() + ". " +
                mediaPaths.get(1).toString() +". " +
                mediaPaths.get(2).toString());
        this.serverId = this.dbId = 0;
    }

    // Note: remember to close the cursor when you're finished.
    // Cursor not closed here because it may contain multiple rows of reports
    public Report(Cursor c){
        this.title = c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_TITLE));
        this.details = c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_DETAILS));
        this.timeStamp = c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_TIMESTAMP));
        this.timeElapsed = getElapsedTime(timeStamp);
        this.userName = c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_USERNAME));
        this.latitude = Double.parseDouble(c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_LAT)));
        this.longitude = Double.parseDouble(c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_LNG)));
        this.serverId = c.getInt(c.getColumnIndex(ReportContract.Entry.COLUMN_SERVER_ID));
        this.dbId = c.getInt(c.getColumnIndex(ReportContract.Entry.COLUMN_ID));
        mediaPaths = new ArrayList<String>();
        mediaPaths.add(c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_MEDIAURL1)));
        mediaPaths.add(c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_MEDIAURL2)));
        mediaPaths.add(c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_MEDIAURL3)));
    }

    public Report(JSONObject jsonServerData) {
        try {
            this.title = jsonServerData.getString(titleKey);
            this.details = jsonServerData.getString(detailsKey);
            this.timeStamp = jsonServerData.getString(timeStampKey);
            this.timeElapsed = getElapsedTime(this.timeStamp);
            this.userName = jsonServerData.getString(userNameKey);
            JSONArray mediaPathsInJSON = jsonServerData.getJSONArray(mediaPathsKey);
            mediaPaths = new ArrayList<String>();
            for(int i = 0; i < mediaPathsInJSON.length(); i++)
                mediaPaths.add(mediaPathsInJSON.get(i).toString());
            this.latitude = jsonServerData.getLong(latKey);
            this.longitude = jsonServerData.getLong(lonKey);
            this.serverId = jsonServerData.getInt(serverIdKey);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LogTags.JSON, "Failed to convert data from JSON");
        }
    }

    public Report(String report_key, Bundle savedState) {
        this.serverId = savedState.getInt(report_key+serverIdKey);
        this.title = savedState.getString(report_key+titleKey);
        this.details = savedState.getString(report_key+detailsKey);
        this.timeStamp = savedState.getString(report_key+timeStampKey);
        this.timeElapsed = getElapsedTime(this.timeStamp);
        this.userName = savedState.getString(report_key+userNameKey);
        this.latitude = savedState.getDouble(report_key+latKey);
        this.longitude = savedState.getDouble(report_key+lonKey);
        if (savedState.getStringArray(report_key+mediaPathsKey) != null)
            this.mediaPaths = new ArrayList<String>(Arrays.asList(savedState.getStringArray(report_key+mediaPathsKey)));
        if (savedState.getStringArrayList(report_key+mediaPathsKey) != null)
            this.mediaPaths = savedState.getStringArrayList(report_key+mediaPathsKey);
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

    public JSONObject getJsonForPic(int i) throws JSONException, IOException {
        return new JSONObject().accumulate(mediaPathsKey, getEncodedBytesForPic(i));
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

    public Bundle saveState(String report_key, Bundle outState) {
        outState.putInt(report_key+serverIdKey, this.serverId);
        outState.putString(report_key+titleKey, this.title);
        outState.putString(report_key+detailsKey, this.details);
        outState.putString(report_key+timeStampKey, this.timeStamp);
        outState.putString(report_key+userNameKey, this.userName);
        if (mediaPaths != null)
            outState.putStringArray(report_key+mediaPathsKey,
                    this.mediaPaths.toArray(new String[mediaPaths.size()]));
        outState.putDouble(report_key+latKey, this.latitude);
        outState.putDouble(report_key+lonKey, this.longitude);
        if(mediaPaths != null && !mediaPaths.isEmpty())
            outState.putStringArrayList(report_key+mediaPathsKey, this.mediaPaths);
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