package com.sc.mtaa_safi.feed.tags;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.RemoteException;

import com.sc.mtaa_safi.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by ishuah on 6/2/15.
 */
public class Tag {
    private String tagText;
    private int serverId, _id;
    public static final String[] TAG_PROJECTION = new String[]{
            Contract.Tag._ID,
            Contract.Tag.COLUMN_SERVER_ID,
            Contract.Tag.COLUMN_NAME
    };

    public static final String[] TAG_FROM = new String[]{
            Contract.Tag.COLUMN_NAME
    };

    public static final int[] TAG_TO = new int[]{
            android.R.id.text1
    };

    public Tag(String tagText){
        this.tagText = tagText;
        this.serverId = 0;
    }

    public Tag(String tagText, int serverId){
        this.tagText = tagText;
        this.serverId = serverId;

    }

    public Tag(String tagText, int _id, int serverId){
        this.tagText = tagText;
        this.serverId = serverId;
        this._id = _id;
    }

    public String getTagText(){
        return this.tagText;
    }

    public int getServerId(){
        return  this.serverId;
    }

    public int getId(){
        return this._id;
    }

    @Override
    public String toString(){
        return this.tagText;
    }

    public Uri save(Context context) throws SQLiteConstraintException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Tag.COLUMN_SERVER_ID, serverId);
        contentValues.put(Contract.Tag.COLUMN_NAME, tagText);
        return  context.getContentResolver().insert(Contract.Tag.TAG_URI, contentValues);
    }

    public static void addTags(JSONObject serverJSON, ContentProviderClient contentProviderClient) throws JSONException, RemoteException, OperationApplicationException {
        if (serverJSON != null){
            JSONArray tagsArray = serverJSON.getJSONArray("objects");
            if (tagsArray != null)
                updateDB(tagsArray, contentProviderClient);
        }
    }

    public static  void updateDB(JSONArray tagsArray, ContentProviderClient contentProviderClient) throws RemoteException, OperationApplicationException, JSONException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        for (int i=0; i<tagsArray.length(); i++)
            addContentProviderOp(tagsArray.getJSONObject(i), batch, contentProviderClient);
        contentProviderClient.applyBatch(batch);
    }

    public static void addContentProviderOp(JSONObject tagsArray, ArrayList<ContentProviderOperation> batch, ContentProviderClient contentProviderClient) throws JSONException, RemoteException {
        if(!(contentProviderClient.query(Contract.Tag.TAG_URI, TAG_PROJECTION, Contract.Tag.COLUMN_NAME +" = '"+ tagsArray.get("name")+"'", null, null).getCount() > 0)){
            batch.add(ContentProviderOperation.newInsert(Contract.Tag.TAG_URI)
                    .withValue(Contract.Tag.COLUMN_SERVER_ID, tagsArray.get("id"))
                    .withValue(Contract.Tag.COLUMN_NAME, tagsArray.getString("name"))
                    .build()
            );
        }
    }

}
