package com.sc.mtaa_safi;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.sc.mtaa_safi.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Community {
    public static String selectedAdmin;

    public static final String[] ADMIN_PROJECTION = new String[]{
            Contract.Admin._ID,
            Contract.Admin.COLUMN_NAME
    };

    public static final String[] ADMIN_FROM = new String[] {
            Contract.Admin._ID,
            Contract.Admin.COLUMN_NAME
    };

    public static final int[] ADMIN_TO = new int[] {
            0,
            android.R.id.text1
    };

    public static final String[] LANDMARK_PROJECTION = new String[]{
            Contract.Landmark._ID,
            Contract.Landmark.COLUMN_NAME,
            Contract.Landmark.COLUMN_LONGITUDE,
            Contract.Landmark.COLUMN_LATITUDE
    };

    public static void addCommunities(JSONObject serverJSON, ContentResolver cr)
            throws JSONException, RemoteException, OperationApplicationException {
        JSONArray placesArray = serverJSON.getJSONArray(Contract.Admin.TABLE_NAME);

        if (placesArray != null)
            updateDB(placesArray, cr);

//        Cursor c = cr.query(Contract.Admin.ADMIN_URI, Community.ADMIN_PROJECTION,
//                null, null, null);
//        while (c.moveToNext()) {
//            Log.i("Admin area",c.getString(c.getColumnIndexOrThrow("admin")));
//            Cursor cc = cr.query(Contract.Landmark.LANDMARK_URI, Community.LANDMARK_PROJECTION, Contract.Landmark.COLUMN_FK_ADMIN + " = " + c.getInt(c.getColumnIndexOrThrow(Contract.Landmark._ID)), null, null);
//            while (cc.moveToNext()){
//                Log.i("Landmark: ", cc.getString(cc.getColumnIndexOrThrow("landmark")));
//            }
//            cc.close();
//        }
//        c.close();
    }

    public static void updateDB(JSONArray placesArray, ContentResolver cr)
            throws RemoteException, OperationApplicationException, JSONException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        for (int i = 0; i < placesArray.length(); i++)
            addContentProviderOp(placesArray.getJSONObject(i), batch, cr, i);
        cr.applyBatch(Contract.CONTENT_AUTHORITY, batch);
        cr.notifyChange(Contract.Admin.ADMIN_URI, null, false);
    }


    public static void addContentProviderOp(JSONObject adminJSON, ArrayList<ContentProviderOperation> batch, ContentResolver cr, int index)
            throws JSONException {
        if (!(cr.query(Contract.Admin.ADMIN_URI, ADMIN_PROJECTION,
                Contract.Admin._ID + " = " + adminJSON.getInt("adminId"), null, null).getCount() > 0)) {
            batch.add(ContentProviderOperation.newInsert(Contract.Admin.ADMIN_URI)
                    .withValue(Contract.Admin._ID, adminJSON.getInt("adminId"))
                    .withValue(Contract.Admin.COLUMN_NAME, adminJSON.getString(Contract.Admin.COLUMN_NAME))
                    .build());
            addLandmarks(adminJSON, batch, index);
        }
    }

    private static void addLandmarks(JSONObject adminJSON, ArrayList<ContentProviderOperation> batch, int index) throws JSONException {
        if (adminJSON.has(Contract.Landmark.TABLE_NAME)) {
            JSONArray landmarksArray = adminJSON.getJSONArray(Contract.Landmark.TABLE_NAME);
            for (int j = 0; j < landmarksArray.length(); j++) {
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

//    public List<String> villages;
//    public HashMap<String, Integer> villageIdMap;
//    public HashMap<String, ArrayList<String>> landmarkMap;
//    public HashMap<String, Integer> landmarkIdMap;

//    private void revealSpinner() {
//    if (landmarkMap.containsKey(villageSelected)) {
//            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
//                    android.R.layout.simple_spinner_item, landmarkMap.get(villageSelected));
//            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            final Spinner landmarkSpinner = (Spinner) getView().findViewById(R.id.landmarkSpinner);
//            landmarkSpinner.setAdapter(dataAdapter);
//            landmarkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    try {
//                        locationJSON.put("landmark", landmarkSpinner.getSelectedItem().toString());
//                        if (landmarkIdMap.containsKey(landmarkSpinner.getSelectedItem().toString()))
//                            locationJSON.put("landmarkId", landmarkIdMap.get(landmarkSpinner.getSelectedItem().toString()));
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//                @Override
//                public void onNothingSelected(AdapterView<?> parent) {
//
//                }
//            });
//            getView().findViewById(R.id.landmarkLayout).setVisibility(View.VISIBLE);
//        }
//  }

//    private void addVillages(){
//        villages = new ArrayList<>();
//        landmarkMap = new HashMap<>();
//        villageIdMap = new HashMap<>();
//        landmarkIdMap = new HashMap<>();
//        Cursor villageCursor = getActivity().getContentResolver().query(Contract.Admin.ADMIN_URI, Community.ADMIN_PROJECTION, null, null, null);
//        while (villageCursor.moveToNext()) {
//            villages.add(villageCursor.getString(villageCursor.getColumnIndexOrThrow(Contract.Admin.COLUMN_NAME)));
//            villageIdMap.put(villageCursor.getString(villageCursor.getColumnIndexOrThrow(Contract.Admin.COLUMN_NAME)), villageCursor.getInt(villageCursor.getColumnIndexOrThrow(Contract.Admin._ID)));
//            Cursor landmarksCursor = getActivity().getContentResolver().query(Contract.Landmark.LANDMARK_URI, Community.LANDMARK_PROJECTION, Contract.Landmark.COLUMN_FK_ADMIN + " = " + villageCursor.getInt(villageCursor.getColumnIndexOrThrow(Contract.Admin._ID)), null, null);
//            while (landmarksCursor.moveToNext()) {
//                addLandmark(landmarksCursor.getString(landmarksCursor.getColumnIndexOrThrow(Contract.Landmark.COLUMN_NAME)), villageCursor.getString(villageCursor.getColumnIndexOrThrow(Contract.Admin.COLUMN_NAME)));
//                landmarkIdMap.put(landmarksCursor.getString(landmarksCursor.getColumnIndexOrThrow(Contract.Landmark.COLUMN_NAME)), landmarksCursor.getInt(landmarksCursor.getColumnIndexOrThrow(Contract.Landmark._ID)));
//            }
//            landmarksCursor.close();
//        }
//        villageCursor.close();
//    }
//
//    private void addLandmark(String landmarkName, String villageName) {
//        ArrayList<String> list;
//        if (landmarkMap.containsKey(villageName))
//            list = landmarkMap.get(villageName);
//        else
//            list = new ArrayList<>();
//        list.add(landmarkName);
//        landmarkMap.put(villageName, list);
//    }
}
