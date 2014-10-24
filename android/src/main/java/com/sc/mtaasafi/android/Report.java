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
    public int id;
    public double latitude, longitude;
    public ArrayList<String> picPaths;
    public String title, details, timeStamp, timeElapsed, userName;
    public ArrayList<String> mediaURLs;
    public final static String titleKey = "title",
            detailsKey = "details",
            timeStampKey = "timestamp",
            userNameKey = "user",
            picsKey = "picPaths",
            mediaURLsKey = "mediaURLs",
            latKey = "latitude",
            lonKey = "longitude",
            idKey = "id";

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
            this.id = 0;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LogTags.JSON, "Failed to convert data from JSON");
        }
    }

    public Report(String report_key, Bundle savedState) {
        this.id = savedState.getInt(report_key+idKey);
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

    public JSONObject getJsonForPic(int i) throws JSONException, IOException {
        return new JSONObject().accumulate(picsKey, getEncodedBytesForPic(i));
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

    public Bundle saveState(String report_key, Bundle outState) {
        outState.putInt(report_key+idKey, this.id);
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