package com.sc.mtaa_safi.location;


import android.content.ContentProviderOperation;
import android.content.Context;

import com.sc.mtaa_safi.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ishuah on 1/21/15.
 */
public class LocationData {

    public static final String[] ADMIN_PROJECTION = new String[]{
        Contract.Admin._ID,
        Contract.Admin.COLUMN_NAME
    };

    public static final String[] LANDMARK_PROJECTION = new String[]{
        Contract.Landmark._ID,
        Contract.Landmark.COLUMN_NAME,
        Contract.Landmark.COLUMN_LONGITUDE,
        Contract.Landmark.COLUMN_LATITUDE
    };

    public static void getContentProviderOp(JSONObject adminJSON, ArrayList<ContentProviderOperation> batch, Context context, int index)
        throws JSONException {
        if(!(context.getContentResolver().query(Contract.Admin.ADMIN_URI, LocationData.ADMIN_PROJECTION,
                Contract.Admin._ID + " = " + adminJSON.getInt(Contract.Admin.COLUMN_SERVER_ID),null,null).getCount() > 0)) {
            batch.add(ContentProviderOperation.newInsert(Contract.Admin.ADMIN_URI)
                    .withValue(Contract.Admin._ID, adminJSON.getInt(Contract.Admin.COLUMN_SERVER_ID))
                    .withValue(Contract.Admin.COLUMN_NAME, adminJSON.getString(Contract.Admin.COLUMN_NAME))
                    .build());
            if(adminJSON.has(Contract.Landmark.TABLE_NAME)){
                JSONArray landmarksArray = adminJSON.getJSONArray(Contract.Landmark.TABLE_NAME);
                for(int j = 0; j < landmarksArray.length(); j++){
                    JSONObject landmarkJSON = landmarksArray.getJSONObject(j);
                    batch.add(ContentProviderOperation.newInsert(Contract.Landmark.LANDMARK_URI)
                        .withValueBackReference(Contract.Landmark.COLUMN_FK_ADMIN, index)
                        .withValue(Contract.Landmark._ID, landmarkJSON.getInt(Contract.Landmark.COLUMN_SERVER_ID))
                        .withValue(Contract.Landmark.COLUMN_NAME, landmarkJSON.getString(Contract.Landmark.COLUMN_NAME))
                        .withValue(Contract.Landmark.COLUMN_LONGITUDE, landmarkJSON.getLong(Contract.Landmark.COLUMN_LONGITUDE))
                        .withValue(Contract.Landmark.COLUMN_LATITUDE, landmarkJSON.getLong(Contract.Landmark.COLUMN_LATITUDE))
                        .build());
                }
            }
        }
    }

}
