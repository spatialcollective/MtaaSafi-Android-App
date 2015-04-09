package com.sc.mtaa_safi;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Report {
    private Gson gson = new Gson();
    public boolean upVoted = false;
    public int serverId, dbId, userId, adminId, status, pendingState = -1, upVoteCount, inProgress = 0;
    public String locationDescript, content, timeElapsed, userName, locationJSON;
    public long timeStamp;
    public ArrayList<String> media = new ArrayList<String>();
    public Location location;
            
    public static final String[] PROJECTION = new String[] {
            Contract.Entry.COLUMN_ID,
            Contract.Entry.COLUMN_SERVER_ID,
            Contract.Entry.COLUMN_CONTENT,
            Contract.Entry.COLUMN_HUMAN_LOC,
            Contract.Entry.COLUMN_TIMESTAMP,
            Contract.Entry.COLUMN_STATUS,
            Contract.Entry.COLUMN_LAT,
            Contract.Entry.COLUMN_LNG,
            Contract.Entry.COLUMN_ADMIN_ID,
            Contract.Entry.COLUMN_LOC_ACC,
            Contract.Entry.COLUMN_LOC_TIME,
            Contract.Entry.COLUMN_LOC_PROV,
            Contract.Entry.COLUMN_LOC_DATA,
            Contract.Entry.COLUMN_USERNAME,
            Contract.Entry.COLUMN_USERID,
            Contract.Entry.COLUMN_MEDIA,
            Contract.Entry.COLUMN_PENDINGFLAG,
            Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS,
            Contract.Entry.COLUMN_UPVOTE_COUNT,
            Contract.Entry.COLUMN_USER_UPVOTED
    };
    // for Report objects created by the user to send to the server
    public Report(String details, int status, String userName, Location newLocation, ArrayList<String> picPaths, String locationJSON) {
        this.serverId = this.dbId = 0;
        this.content = details;
        this.status = status;
        this.locationDescript = "";
        this.pendingState = 0;
        this.timeStamp = System.currentTimeMillis();
        this.userName = userName;
        this.location = newLocation;
        this.locationJSON = locationJSON;
        this.media = picPaths;
    }

    public Report(Bundle bundle) {
        serverId = bundle.getInt(Contract.Entry.COLUMN_SERVER_ID);
        dbId = bundle.getInt(Contract.Entry.COLUMN_ID);
        content = bundle.getString(Contract.Entry.COLUMN_CONTENT);
        status = bundle.getInt(Contract.Entry.COLUMN_STATUS);
        locationDescript = bundle.getString(Contract.Entry.COLUMN_HUMAN_LOC);
        timeStamp = bundle.getLong(Contract.Entry.COLUMN_TIMESTAMP);
        userName = bundle.getString(Contract.Entry.COLUMN_USERNAME);
        userId = bundle.getInt(Contract.Entry.COLUMN_USERID);
        upVoted = bundle.getBoolean(Contract.Entry.COLUMN_USER_UPVOTED);
        upVoteCount = bundle.getInt(Contract.Entry.COLUMN_UPVOTE_COUNT);
        adminId = bundle.getInt(Contract.Entry.COLUMN_ADMIN_ID);
        locationJSON = bundle.getString(Contract.Entry.COLUMN_LOC_DATA);
        
        location = new Location("report_location");
        location.setLatitude(bundle.getDouble(Contract.Entry.COLUMN_LAT));
        location.setLongitude(bundle.getDouble(Contract.Entry.COLUMN_LNG));
        location.setAccuracy(bundle.getFloat(Contract.Entry.COLUMN_LOC_ACC));
        location.setTime(bundle.getLong(Contract.Entry.COLUMN_LOC_TIME));
        location.setProvider(bundle.getString(Contract.Entry.COLUMN_LOC_PROV));

        media = gson.fromJson(bundle.getString(Contract.Entry.COLUMN_MEDIA), new TypeToken<ArrayList<String>>() {
        }.getType());
    }

    public Report(Cursor c)  {
        serverId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID));
        dbId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ID));
        pendingState = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG));
        content = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_CONTENT));
        status = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_STATUS));
        locationDescript = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_HUMAN_LOC));
        timeStamp = c.getLong(c.getColumnIndex(Contract.Entry.COLUMN_TIMESTAMP));
        timeElapsed = Utils.getElapsedTime(timeStamp);
        userName = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_USERNAME));
        userId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_USERID));
        adminId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ADMIN_ID));
        if(userName.equals(""))
            userName = "Unknown user";

        upVoted = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_USER_UPVOTED)) > 0;
        upVoteCount = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPVOTE_COUNT));
        locationJSON = c.getString(c.getColumnIndexOrThrow(Contract.Entry.COLUMN_LOC_DATA));

        location = new Location("ReportLocation");
        location.setLatitude(c.getDouble(c.getColumnIndex(Contract.Entry.COLUMN_LAT)));
        location.setLongitude(c.getDouble(c.getColumnIndex(Contract.Entry.COLUMN_LNG)));
        location.setAccuracy(c.getFloat(c.getColumnIndex(Contract.Entry.COLUMN_LOC_ACC)));
        location.setTime(c.getLong(c.getColumnIndex(Contract.Entry.COLUMN_LOC_TIME)));
        location.setProvider(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_LOC_PROV)));

        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        if (c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIA)) != null && !c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIA)).isEmpty())
            media = gson.fromJson(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIA)), type);
    }

    public Report(JSONObject jsonData, int pending) throws JSONException {
        serverId = jsonData.getInt("unique_id");
        locationDescript = jsonData.getString(Contract.Entry.COLUMN_HUMAN_LOC);
        content = jsonData.getString(Contract.Entry.COLUMN_CONTENT);
        status = jsonData.getInt(Contract.Entry.COLUMN_STATUS);
        timeStamp = jsonData.getLong(Contract.Entry.COLUMN_TIMESTAMP);
        timeElapsed = Utils.getElapsedTime(this.timeStamp);
        userName = jsonData.getString(Contract.Entry.COLUMN_USERNAME);
        userId = jsonData.getInt(Contract.Entry.COLUMN_USERID);
        adminId = jsonData.getInt(Contract.Entry.COLUMN_ADMIN_ID);

        upVoteCount = jsonData.getInt(Contract.Entry.COLUMN_UPVOTE_COUNT);
        upVoted = jsonData.getBoolean(Contract.Entry.COLUMN_USER_UPVOTED);
        pendingState = pending;
        try {
            locationJSON = jsonData.getString(Contract.Entry.COLUMN_LOC_DATA);
        }catch (JSONException e) {
            locationJSON = "";
        }

        location = new Location("ReportLocation");
        location.setLatitude(jsonData.getDouble(Contract.Entry.COLUMN_LAT));
        location.setLongitude(jsonData.getDouble(Contract.Entry.COLUMN_LNG));

        JSONArray mediaIdsJSON = jsonData.getJSONArray(Contract.Entry.COLUMN_MEDIA);
        for (int i = 0; i < mediaIdsJSON.length(); i++)
            media.add(mediaIdsJSON.get(i) + "");
    }

    public ContentValues getContentValues() {
        ContentValues reportValues = new ContentValues();
        reportValues.put(Contract.Entry.COLUMN_SERVER_ID, serverId);
        reportValues.put(Contract.Entry.COLUMN_HUMAN_LOC, locationDescript);
        reportValues.put(Contract.Entry.COLUMN_CONTENT, content);
        reportValues.put(Contract.Entry.COLUMN_STATUS, status);
        reportValues.put(Contract.Entry.COLUMN_TIMESTAMP, timeStamp);
        reportValues.put(Contract.Entry.COLUMN_USERNAME, userName);
        reportValues.put(Contract.Entry.COLUMN_USERID, userId);
        reportValues.put(Contract.Entry.COLUMN_ADMIN_ID, adminId);
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
        if ( locationJSON != null)
            reportValues.put(Contract.Entry.COLUMN_LOC_DATA, locationJSON);
        else
            reportValues.put(Contract.Entry.COLUMN_LOC_DATA, "");

        reportValues.put(Contract.Entry.COLUMN_MEDIA, gson.toJson(media));
        return reportValues;
    }

    public Bundle saveState(Bundle output) {
        output.putInt(Contract.Entry.COLUMN_SERVER_ID, serverId);
        output.putInt(Contract.Entry.COLUMN_ID, dbId);
        output.putString(Contract.Entry.COLUMN_CONTENT, content);
        output.putString(Contract.Entry.COLUMN_STATUS, status);
        output.putString(Contract.Entry.COLUMN_HUMAN_LOC, locationDescript);
        output.putLong(Contract.Entry.COLUMN_TIMESTAMP, timeStamp);
        output.putString(Contract.Entry.COLUMN_USERNAME, userName);
        output.putInt(Contract.Entry.COLUMN_USERID, userId);
        output.putInt(Contract.Entry.COLUMN_ADMIN_ID, adminId);
        output.putBoolean(Contract.Entry.COLUMN_USER_UPVOTED, upVoted);
        output.putInt(Contract.Entry.COLUMN_UPVOTE_COUNT, upVoteCount);
        output.putDouble(Contract.Entry.COLUMN_LAT, location.getLatitude());
        output.putDouble(Contract.Entry.COLUMN_LNG, location.getLongitude());
        output.putFloat(Contract.Entry.COLUMN_LOC_ACC, location.getAccuracy());
        output.putLong(Contract.Entry.COLUMN_LOC_TIME, location.getTime());
        output.putString(Contract.Entry.COLUMN_LOC_PROV, location.getProvider());

        output.putString(Contract.Entry.COLUMN_MEDIA, gson.toJson(media));
        return output;
    }
    
    public static Uri getUri(int dbId) {
        return Contract.Entry.CONTENT_URI.buildUpon().appendPath(Integer.toString(dbId)).build();
    }

    public static ArrayList<String> getMediaList(String mediaString) {
        Gson statGson = new Gson();
        ArrayList<String> imgs = new ArrayList<String>();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        if (mediaString != null && !mediaString.isEmpty())
            imgs = statGson.fromJson(mediaString, type);
        return imgs;
    }

    public JSONObject getJsonRep() throws JSONException, IOException, NoSuchAlgorithmException {
        JSONObject json = new JSONObject();
        json.put(Contract.Entry.COLUMN_CONTENT, this.content);
        json.put(Contract.Entry.COLUMN_STATUS, this.status);
        json.put(Contract.Entry.COLUMN_TIMESTAMP, this.timeStamp);
        json.put(Contract.Entry.COLUMN_USERNAME, this.userName);
        json.put(Contract.Entry.COLUMN_USERID, this.userId);
        json.put(Contract.Entry.COLUMN_ADMIN_ID, this.adminId);

        if (this.locationJSON != null)
            json.put(Contract.Entry.COLUMN_LOC_DATA, this.locationJSON);
        else
            json.put(Contract.Entry.COLUMN_LOC_DATA, "");

        JSONObject loc = new JSONObject();
        loc.put(Contract.Entry.COLUMN_LAT, location.getLatitude());
        loc.put(Contract.Entry.COLUMN_LNG, location.getLongitude());
        loc.put(Contract.Entry.COLUMN_LOC_ACC, location.getAccuracy());
        loc.put("timestamp", location.getTime());
        loc.put(Contract.Entry.COLUMN_LOC_PROV, location.getProvider());
        json.put("location", loc);

        json.put("picHashes", new JSONArray());
        for (int i = 0; i < media.size(); i++) {
            try { 
                Integer.parseInt(media.get(i)); 
            } catch(NumberFormatException e) { 
                json.accumulate("picHashes", getSHA1forPic(i));
            }
        }
        return json;
    }

    public ContentValues updateValues(JSONObject response) throws JSONException {
        pendingState = response.getInt("nextfield");
        ContentValues updateValues = new ContentValues();
        
        if (pendingState == 1) {
            updateValues.put(Contract.Entry.COLUMN_HUMAN_LOC, response.getString("output"));
            updateValues.put(Contract.Entry.COLUMN_SERVER_ID, response.getInt("id"));
            serverId = response.getInt("id");
        } else if (pendingState >= 2) {
            media.set(pendingState - 2, response.getString("output"));
            updateValues.put(Contract.Entry.COLUMN_MEDIA, gson.toJson(media));
        }

        if (pendingState > media.size())
            pendingState = -1;
        if (pendingState > 0)
            updateValues.put(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS, 1);
        else
            updateValues.put(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS, 0);
        updateValues.put(Contract.Entry.COLUMN_PENDINGFLAG, pendingState);

        return updateValues;
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
        if (distInMeters > 1000) {
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

    public byte[] getBytesForPic(int i) throws IOException {
        byte[] b = new byte[8];
        try { 
            Integer.parseInt(media.get(i));
        } catch (NumberFormatException e) { 
            File file = new File(media.get(i));
            b = new byte[(int) file.length()];
            Log.e("Report", ", File path" + file.getAbsolutePath());
            FileInputStream inputStream = new FileInputStream(file); /* Remove BufferedInputStream */
            inputStream.read(b);
            inputStream.close();
        }
        return b;
    }
    // from: http://stackoverflow.com/questions/5980658/how-to-sha1-hash-a-string-in-android
    // I believe the non-accepted answer on that page may be better. -DK
    public String getSHA1forPic(int i) throws IOException, NoSuchAlgorithmException {
        byte[] b = getBytesForPic(i);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(b, 0, b.length); // should be utf-8 not iso-8859-1
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
