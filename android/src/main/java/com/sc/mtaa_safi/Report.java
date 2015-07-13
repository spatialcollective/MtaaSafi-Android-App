package com.sc.mtaa_safi;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;

import com.sc.mtaa_safi.feed.comments.Comment;
import com.sc.mtaa_safi.feed.tags.ReportTagJunction;
import com.sc.mtaa_safi.feed.tags.Tag;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Report {
    private Gson gson = new Gson();
    public boolean upVoted = false;
    public String description, placeDescript, timeElapsed, userName;
    public int serverId, dbId, userId, adminId, status, pendingState = -1, upVoteCount, inProgress = 0, parentReportId=0;
    public long timeStamp;
    public ArrayList<String> media = new ArrayList<String>();
    public Location location;
            
    public static final String[] PROJECTION = new String[] {
            Contract.Entry.COLUMN_ID,
            Contract.Entry.COLUMN_SERVER_ID,
            Contract.Entry.COLUMN_DESCRIPTION,
            Contract.Entry.COLUMN_PLACE_DESCRIPT,
            Contract.Entry.COLUMN_TIMESTAMP,
            Contract.Entry.COLUMN_STATUS,
            Contract.Entry.COLUMN_ADMIN_ID,
            Contract.Entry.COLUMN_USERID,
            Contract.Entry.COLUMN_USERNAME,
            Contract.Entry.COLUMN_LOCATION,
            Contract.Entry.COLUMN_MEDIA,
            Contract.Entry.COLUMN_PENDINGFLAG,
            Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS,
            Contract.Entry.COLUMN_UPVOTE_COUNT,
            Contract.Entry.COLUMN_USER_UPVOTED,
            Contract.Entry.COLUMN_PARENT_REPORT,
            Contract.MtaaLocation.COLUMN_LAT,
            Contract.MtaaLocation.COLUMN_LNG,
            Contract.MtaaLocation.COLUMN_LOC_ACC,
            Contract.MtaaLocation.COLUMN_LOC_TIME,
            Contract.MtaaLocation.COLUMN_LOC_PROV
    };

    // for Report objects created by the user to send to the server
    public Report(String description, int status, String userName, int userId, Location newLocation, ArrayList<String> picPaths) {
        initialize(description, status, userName, userId, newLocation, picPaths);
    }
    public Report(String description, int status, String userName, int userId, Location newLocation, ArrayList<String> picPaths, int parentReportId) {
        initialize(description, status, userName, userId, newLocation, picPaths);
        this.parentReportId = parentReportId;
    }
    private void initialize(String description, int status, String userName, int userId, Location newLocation, ArrayList<String> picPaths) {
        this.serverId = this.dbId = 0;
        this.description = description;
        this.status = status;
        this.placeDescript = "";
        this.pendingState = 0;
        this.timeStamp = System.currentTimeMillis();
        this.userName = userName;
        this.userId = userId;
        this.location = newLocation;
        this.media = picPaths;

    }

    public Report(Bundle bundle) {
        dbId = bundle.getInt(Contract.Entry.COLUMN_ID);
        serverId = bundle.getInt(Contract.Entry.COLUMN_SERVER_ID);
        description = bundle.getString(Contract.Entry.COLUMN_DESCRIPTION);
        placeDescript = bundle.getString(Contract.Entry.COLUMN_PLACE_DESCRIPT);
        status = bundle.getInt(Contract.Entry.COLUMN_STATUS);
        timeStamp = bundle.getLong(Contract.Entry.COLUMN_TIMESTAMP);
        userName = bundle.getString(Contract.Entry.COLUMN_USERNAME);
        userId = bundle.getInt(Contract.Entry.COLUMN_USERID);
        upVoted = bundle.getBoolean(Contract.Entry.COLUMN_USER_UPVOTED);
        upVoteCount = bundle.getInt(Contract.Entry.COLUMN_UPVOTE_COUNT);
        adminId = bundle.getInt(Contract.Entry.COLUMN_ADMIN_ID);
        parentReportId = bundle.getInt(Contract.Entry.COLUMN_PARENT_REPORT);

        location = new Location("report_location");
        location.setLatitude(bundle.getDouble(Contract.MtaaLocation.COLUMN_LAT));
        location.setLongitude(bundle.getDouble(Contract.MtaaLocation.COLUMN_LNG));
        location.setAccuracy(bundle.getFloat(Contract.MtaaLocation.COLUMN_LOC_ACC));
        location.setTime(bundle.getLong(Contract.MtaaLocation.COLUMN_LOC_TIME));
        location.setProvider(bundle.getString(Contract.MtaaLocation.COLUMN_LOC_PROV));

        media = gson.fromJson(bundle.getString(Contract.Entry.COLUMN_MEDIA), new TypeToken<ArrayList<String>>() {
        }.getType());
    }

    public Report(Cursor c)  {
        dbId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ID));
        serverId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID));
        description = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_DESCRIPTION));
        placeDescript = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_PLACE_DESCRIPT));
        status = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_STATUS));
        timeStamp = c.getLong(c.getColumnIndex(Contract.Entry.COLUMN_TIMESTAMP));
        timeElapsed = Utils.getElapsedTime(timeStamp);
        userName = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_USERNAME));
        userId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_USERID));
        adminId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ADMIN_ID));
        if (userName == null || userName.equals(""))
            userName = "Unknown User";

        parentReportId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_PARENT_REPORT));
        pendingState = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG));
        upVoted = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_USER_UPVOTED)) > 0;
        upVoteCount = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPVOTE_COUNT));

        location = new Location("ReportLocation");
        location.setLatitude(c.getDouble(c.getColumnIndex(Contract.MtaaLocation.COLUMN_LAT)));
        location.setLongitude(c.getDouble(c.getColumnIndex(Contract.MtaaLocation.COLUMN_LNG)));
        location.setAccuracy(c.getFloat(c.getColumnIndex(Contract.MtaaLocation.COLUMN_LOC_ACC)));
        location.setTime(c.getLong(c.getColumnIndex(Contract.MtaaLocation.COLUMN_LOC_TIME)));
        location.setProvider(c.getString(c.getColumnIndex(Contract.MtaaLocation.COLUMN_LOC_PROV)));

        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        if (c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIA)) != null && !c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIA)).isEmpty())
            media = gson.fromJson(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIA)), type);
    }

    public Report(JSONObject jsonData, int pending, ArrayList<String> voteList, Context context) throws JSONException, SQLiteConstraintException {
        getBasicData(jsonData);
        getUserData(jsonData);
        getUpvoteData(jsonData, voteList);
        getLocationData(jsonData);
        pendingState = pending;
        addMedia(jsonData);
        addComments(jsonData, context);
        addTags(jsonData, context);
    }
    private void getBasicData(JSONObject jsonData) throws JSONException {
        serverId = jsonData.getInt("id");
        description = jsonData.getString(Contract.Entry.COLUMN_DESCRIPTION);
        placeDescript = jsonData.getString(Contract.Entry.COLUMN_PLACE_DESCRIPT);
        status = jsonData.getInt(Contract.Entry.COLUMN_STATUS);
        timeStamp = jsonData.getLong(Contract.Entry.COLUMN_TIMESTAMP);
        timeElapsed = Utils.getElapsedTime(this.timeStamp);
        if (jsonData.has("geo_admin"))
            adminId = jsonData.getJSONObject("geo_admin").getInt("id");
        if (jsonData.has(Contract.Entry.COLUMN_PARENT_REPORT))
            parentReportId = jsonData.getInt(Contract.Entry.COLUMN_PARENT_REPORT);
    }
    private void getUserData(JSONObject jsonData) throws JSONException {
        userName = jsonData.getJSONObject("owner").getString(Contract.Entry.COLUMN_USERNAME);
        userId = jsonData.getJSONObject("owner").getInt("id");
    }
    private void getUpvoteData(JSONObject jsonData, ArrayList<String> voteList) throws JSONException {
        upVoteCount = jsonData.getJSONArray("upvote_set").length();
        if (voteList.contains(Integer.toString(serverId)))
            upVoted = true;
        else
            upVoted = false;
    }
    private void getLocationData(JSONObject jsonData) throws JSONException {
        location = new Location("ReportLocation");
        JSONArray coords = jsonData.getJSONArray("shapes").getJSONObject(0).getJSONObject("shape").getJSONArray("coordinates");
        location.setLatitude(coords.getDouble(1));
        location.setLongitude(coords.getDouble(0));
    }
    private void addMedia(JSONObject jsonData) throws JSONException {
        JSONArray mediaArray = jsonData.getJSONArray(Contract.Entry.COLUMN_MEDIA);
        for (int i = 0; i < mediaArray.length(); i++)
            media.add(mediaArray.getJSONObject(i).getInt("id") + "");
    }
    private void addComments(JSONObject jsonObject, Context context) throws JSONException {
        if (jsonObject.has("comment_set") && jsonObject.getJSONArray("comment_set").length() > 0) {
            for (int j = 0; j < jsonObject.getJSONArray("comment_set").length(); j++) {
                new Comment(jsonObject.getJSONArray("comment_set").getJSONObject(j), serverId, context).save();
            }
        }
    }
    private void addTags(JSONObject jsonObject, Context context) throws JSONException, SQLiteConstraintException {
        if (jsonObject.has("tags") && jsonObject.getJSONArray("tags").length() > 0){
            for (int i = 0; i < jsonObject.getJSONArray("tags").length(); i++){
                ReportTagJunction.save(context, serverId, jsonObject.getJSONArray("tags").getJSONObject(i).getInt("id"));
            }
        }
    }

    public Uri save(Context context, boolean created) {
        if (created)
            Utils.saveSavedReportCount(context, Utils.getSavedReportCount(context) + 1);
        Uri locUri = context.getContentResolver().insert(Contract.MtaaLocation.LOCATION_URI, getLocationContentValues());
        long locId = Integer.valueOf(locUri.getLastPathSegment());
        ContentValues cv = getContentValues();
        cv.put(Contract.Entry.COLUMN_LOCATION, locId);
        return context.getContentResolver().insert(Contract.Entry.CONTENT_URI, cv);
    }

    public ArrayList<ContentProviderOperation> createContentProviderOperation(ArrayList<ContentProviderOperation> batch) {
        batch.add(ContentProviderOperation.newInsert(Contract.MtaaLocation.LOCATION_URI)
                .withValues(getLocationContentValues())
                .build());
        batch.add(ContentProviderOperation.newInsert(Contract.Entry.CONTENT_URI)
                .withValues(getContentValues())
                .withValueBackReference(Contract.Entry.COLUMN_LOCATION, 0)
                .build());
        return batch;
    }

    public ContentValues getContentValues() {
        ContentValues reportValues = new ContentValues();
        reportValues.put(Contract.Entry.COLUMN_SERVER_ID, serverId);
        reportValues.put(Contract.Entry.COLUMN_DESCRIPTION, description);
        reportValues.put(Contract.Entry.COLUMN_PLACE_DESCRIPT, placeDescript);
        reportValues.put(Contract.Entry.COLUMN_STATUS, status);
        reportValues.put(Contract.Entry.COLUMN_TIMESTAMP, timeStamp);
        reportValues.put(Contract.Entry.COLUMN_USERID, userId);
        reportValues.put(Contract.Entry.COLUMN_USERNAME, userName);
        reportValues.put(Contract.Entry.COLUMN_ADMIN_ID, adminId);
        reportValues.put(Contract.Entry.COLUMN_PENDINGFLAG, pendingState);
        reportValues.put(Contract.Entry.COLUMN_UPVOTE_COUNT, upVoteCount);
        if (parentReportId != 0)
            reportValues.put(Contract.Entry.COLUMN_PARENT_REPORT, parentReportId);
        if (upVoted)
            reportValues.put(Contract.Entry.COLUMN_USER_UPVOTED, 1);
        else
            reportValues.put(Contract.Entry.COLUMN_USER_UPVOTED, 0);
        reportValues.put(Contract.Entry.COLUMN_MEDIA, gson.toJson(media));

        return reportValues;
    }

    public ContentValues getLocationContentValues() {
        ContentValues locValues = new ContentValues();
        locValues.put(Contract.MtaaLocation.COLUMN_LAT, location.getLatitude());
        locValues.put(Contract.MtaaLocation.COLUMN_LNG, location.getLongitude());
        locValues.put(Contract.MtaaLocation.COLUMN_LOC_ACC, location.getAccuracy());
        locValues.put(Contract.MtaaLocation.COLUMN_LOC_TIME, location.getTime());
        locValues.put(Contract.MtaaLocation.COLUMN_LOC_PROV, location.getProvider());
        return locValues;
    }

    public Bundle saveState(Bundle output) {
        output.putInt(Contract.Entry.COLUMN_SERVER_ID, serverId);
        output.putInt(Contract.Entry.COLUMN_ID, dbId);
        output.putString(Contract.Entry.COLUMN_DESCRIPTION, description);
        output.putString(Contract.Entry.COLUMN_PLACE_DESCRIPT, placeDescript);
        output.putInt(Contract.Entry.COLUMN_STATUS, status);
        output.putLong(Contract.Entry.COLUMN_TIMESTAMP, timeStamp);
        output.putString(Contract.Entry.COLUMN_USERNAME, userName);
        output.putInt(Contract.Entry.COLUMN_USERID, userId);
        output.putInt(Contract.Entry.COLUMN_ADMIN_ID, adminId);
        output.putBoolean(Contract.Entry.COLUMN_USER_UPVOTED, upVoted);
        output.putInt(Contract.Entry.COLUMN_UPVOTE_COUNT, upVoteCount);

        output.putDouble(Contract.MtaaLocation.COLUMN_LAT, location.getLatitude());
        output.putDouble(Contract.MtaaLocation.COLUMN_LNG, location.getLongitude());
        output.putFloat(Contract.MtaaLocation.COLUMN_LOC_ACC, location.getAccuracy());
        output.putLong(Contract.MtaaLocation.COLUMN_LOC_TIME, location.getTime());
        output.putString(Contract.MtaaLocation.COLUMN_LOC_PROV, location.getProvider());

        output.putInt(Contract.Entry.COLUMN_PARENT_REPORT, parentReportId);
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
        json.put(Contract.Entry.COLUMN_DESCRIPTION, this.description);
        json.put(Contract.Entry.COLUMN_PLACE_DESCRIPT, this.placeDescript);
        json.put(Contract.Entry.COLUMN_STATUS, this.status);
        json.put(Contract.Entry.COLUMN_TIMESTAMP, this.timeStamp);
        json.put(Contract.Entry.COLUMN_USERNAME, this.userName);
        json.put(Contract.Entry.COLUMN_USERID, this.userId);
        json.put(Contract.Entry.COLUMN_ADMIN_ID, this.adminId);
        if (this.parentReportId !=0)
            json.put(Contract.Entry.COLUMN_PARENT_REPORT, this.parentReportId);

        JSONObject loc = new JSONObject();
        loc.put(Contract.MtaaLocation.COLUMN_LAT, location.getLatitude());
        loc.put(Contract.MtaaLocation.COLUMN_LNG, location.getLongitude());
        loc.put(Contract.MtaaLocation.COLUMN_LOC_ACC, location.getAccuracy());
        loc.put("timestamp", location.getTime());
        loc.put(Contract.MtaaLocation.COLUMN_LOC_PROV, location.getProvider());
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
            updateValues.put(Contract.Entry.COLUMN_SERVER_ID, response.getInt("id"));
            updateValues.put(Contract.Entry.COLUMN_PLACE_DESCRIPT, response.getString("output"));
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
