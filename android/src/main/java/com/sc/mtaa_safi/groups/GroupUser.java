package com.sc.mtaa_safi.groups;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.RemoteException;

import com.sc.mtaa_safi.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ishuah on 9/10/15.
 */
public class GroupUser {
    private String name;
    private int _id, serverId;
    public static final String[] USER_PROJECTION = new String[]{
            Contract.Tag._ID,
            Contract.Tag.COLUMN_SERVER_ID,
            Contract.Tag.COLUMN_NAME
    };

    public GroupUser(String name, int serverId){
        this.name = name;
        this.serverId = serverId;
    }

    public GroupUser(String name, int serverId, int _id){
        this.name = name;
        this.serverId = serverId;
        this._id = _id;
    }

    public String getUserName(){
        return this.name;
    }

    public int getServerId(){
        return this.serverId;
    }

    public int get_id(){
        return this._id;
    }

    @Override
    public String toString(){
        return this.name;
    }

    public Uri save(Context context) throws SQLiteConstraintException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.User.COLUMN_SERVER_ID, serverId);
        contentValues.put(Contract.User.COLUMN_NAME, name);
        return  context.getContentResolver().insert(Contract.User.USER_URI, contentValues);
    }

    public static void addUsers(JSONObject serverJSON, ContentProviderClient contentProviderClient) throws JSONException, RemoteException, OperationApplicationException {
        if (serverJSON != null){
            JSONArray groupArray = serverJSON.getJSONArray("objects");
            if (groupArray != null) {
                for (int i = 0; i < groupArray.length(); i++) {
                    JSONObject group = groupArray.getJSONObject(i);
                    JSONArray users = group.getJSONArray("admins");

                }
                updateDB(groupArray, contentProviderClient);
            }
        }
    }

    public static  void updateDB(JSONArray userArray, ContentProviderClient contentProviderClient) throws RemoteException, OperationApplicationException, JSONException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        for (int i=0; i<userArray.length(); i++)
            addContentProviderOp(userArray.getJSONObject(i), batch, contentProviderClient);
        contentProviderClient.applyBatch(batch);
    }

    public static void addContentProviderOp(JSONObject userArray, ArrayList<ContentProviderOperation> batch, ContentProviderClient contentProviderClient) throws JSONException, RemoteException {
        if(!(contentProviderClient.query(Contract.Tag.TAG_URI, USER_PROJECTION, Contract.Tag.COLUMN_NAME +" = '"+ userArray.get("name")+"'", null, null).getCount() > 0)){
            batch.add(ContentProviderOperation.newInsert(Contract.Tag.TAG_URI)
                            .withValue(Contract.Tag.COLUMN_SERVER_ID, userArray.get("id"))
                            .withValue(Contract.Tag.COLUMN_NAME, userArray.getString("name"))
                            .build()
            );
        }
    }
}
