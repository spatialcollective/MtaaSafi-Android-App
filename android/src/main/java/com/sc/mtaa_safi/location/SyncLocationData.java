package com.sc.mtaa_safi.location;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.ComplexPreferences;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;
import com.sc.mtaa_safi.database.Contract;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ishuah on 1/21/15.
 */
public class SyncLocationData extends AsyncTask<Integer, Integer, Integer> {
    Context mContext;

    public SyncLocationData(Context context){
        mContext = context;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        JSONObject response;
        try {
            response = getLocationData();
            if (response != null) {
                addLocationDataToDb(response);
                Log.i("Location data sync", "Firing on all cylinders.");
                return 1;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        Log.e("Location data sync", "Houston, we have a problem.");
        return 0;
    }

    private JSONObject getLocationData() throws IOException{
        ComplexPreferences cp = PrefUtils.getPrefs(mContext);
        Location cachedLocation = cp.getObject(PrefUtils.LOCATION, Location.class);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(new HttpGet(mContext.getString(R.string.location_data) + "36.8619/-1.2600/" /*+ cachedLocation.getLongitude() + "/" + cachedLocation.getLatitude() +"/"*/));
        if (response.getStatusLine().getStatusCode() > 400)
            return null;
        return NetworkUtils.convertHttpResponseToJSON(response);
    }

    public void addLocationDataToDb(JSONObject locationJSON)
        throws JSONException, RemoteException, OperationApplicationException{
        JSONArray locationArray = locationJSON.getJSONArray(Contract.Admin.TABLE_NAME);
        if (locationArray !=null){
            ArrayList<ContentProviderOperation> batch = new ArrayList<>();

            for (int i = 0; i < locationArray.length(); i++)
                LocationData.getContentProviderOp(locationArray.getJSONObject(i), batch, mContext, i);
            mContext.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, batch);
            mContext.getContentResolver().notifyChange(Contract.Admin.ADMIN_URI, null, false);
        }
        Cursor c = mContext.getContentResolver().query(Contract.Admin.ADMIN_URI, LocationData.ADMIN_PROJECTION,
                null,null,null);
        Log.i("Admins count", String.valueOf(c.getCount()));
        while (c.moveToNext()){
            Log.i("Admin area",c.getString(c.getColumnIndexOrThrow("admin")));
            Cursor cc = mContext.getContentResolver().query(Contract.Landmark.LANDMARK_URI, LocationData.LANDMARK_PROJECTION, Contract.Landmark.COLUMN_FK_ADMIN + " = " + c.getInt(c.getColumnIndexOrThrow(Contract.Landmark._ID)), null, null);
            while (cc.moveToNext()){
                Log.i("Landmark: ", cc.getString(cc.getColumnIndexOrThrow("landmark")));
            }
            cc.close();
        }
        c.close();
    }
}
