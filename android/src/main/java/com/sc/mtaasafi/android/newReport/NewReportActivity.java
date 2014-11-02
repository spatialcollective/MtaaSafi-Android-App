package com.sc.mtaasafi.android.newReport;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.sc.mtaasafi.android.SystemUtils.AlertDialogFragment;
import com.sc.mtaasafi.android.SystemUtils.ComplexPreferences;
import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.database.ReportContract;
import com.sc.mtaasafi.android.feed.MainActivity;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.newReport.NewReportFragment;
import com.sc.mtaasafi.android.newReport.ReportUploadingFragment;

import java.util.ArrayList;
import java.util.List;

public class NewReportActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        AlertDialogFragment.AlertDialogListener {

    private LocationClient mLocationClient;
    private ComplexPreferences cp;
    private List<String> savedReportKeys;
    private int progress;
    public final static String  REPORT_KEY = "pendingReport",
                                SAVED_REPORTS_KEY = "savedReportKeys",
                                UPLOAD_SAVED_REPORTS_KEY = "uploadSavedReports",
                                UPLOAD_TAG= "upload",
                                NEW_REPORT_TAG= "newreport";
    private ReportUploader uploader;
    private ContentProviderOperation.Builder cpoBuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreFragment(savedInstanceState);
        
        mLocationClient = new LocationClient(this, this, this);
        cp = PrefUtils.getPrefs(this);
        savedReportKeys = cp.getObject(SAVED_REPORTS_KEY, List.class);
        if(savedReportKeys == null)
            savedReportKeys = new ArrayList<String>();
    }

    // Restore the previous fragment from the savedInstanceState
    private void restoreFragment(Bundle savedInstanceState){
        NewReportFragment nrf;
        FragmentManager manager = getSupportFragmentManager();
        if(savedInstanceState != null){
            ReportUploadingFragment ruf;
            nrf = (NewReportFragment) getSupportFragmentManager().getFragment(savedInstanceState, NEW_REPORT_TAG);
            ruf = (ReportUploadingFragment) getSupportFragmentManager().getFragment(savedInstanceState, UPLOAD_TAG);
            if(ruf != null){ // last session ended w a report uploading
                manager.beginTransaction()
                        .replace(android.R.id.content, ruf, UPLOAD_TAG)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                return;
            } else if(nrf == null){ // edge case handling
                nrf = new NewReportFragment();
            }
        } else{
            Log.e("Creating Activity", "fragment was null...");
            nrf = new NewReportFragment();
        }
        manager.beginTransaction()
                .replace(android.R.id.content, nrf, NEW_REPORT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
    @Override
    protected void onStart() {
        super.onStart();
        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals(""))
            onLocationDisabled();
        else{
            mLocationClient.connect();
        }
    }

    protected  void onResume(){
        super.onResume();
        Intent intent = getIntent();
        if(intent != null){
            if(intent.getBooleanExtra(UPLOAD_SAVED_REPORTS_KEY, false)){
                // the activity is supposed to upload its saved reports
                ReportUploadingFragment ruf =
                        (ReportUploadingFragment) getSupportFragmentManager().findFragmentByTag(UPLOAD_TAG);
                if(ruf == null){
                    uploadSavedReports();
                    Log.e("RUF" , "Report uploading fragment was null...");
                }
            }
        }
    }

    @Override
    protected void onStop(){
        // if this was an upload-saved-reports session, save the current progress and changes made to the DB
        if(getIntent() != null && getIntent().getBooleanExtra(UPLOAD_SAVED_REPORTS_KEY, false))
            updateDb();
        mLocationClient.disconnect();
        super.onStop();
    }
    @Override
    public void finish(){
        super.finish();
        Log.e("NRA.Finish", "Finish called! Cancelling uploading: " + (uploader != null));
        if(uploader!=null)
            uploader.cancel(true);
    }
    public int getScreenWidth() { return getWindowManager().getDefaultDisplay().getWidth(); }

    public int getScreenHeight() { return getWindowManager().getDefaultDisplay().getHeight(); }

    // ======================Google Play Services Setup:======================
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 15000;

    public Location getLocation() {
        Location mCurrentLocation = null;
        if(mLocationClient != null && mLocationClient.isConnected()){
            mCurrentLocation = mLocationClient.getLastLocation();
            if(mCurrentLocation != null){
                cp.putObject(PrefUtils.LOCATION, mCurrentLocation);
                cp.putObject(PrefUtils.LOCATION_TIMESTAMP, System.currentTimeMillis());
                cp.commit();
            }
        } else{
            int minsSinceLastLocation = PrefUtils.getTimeSinceInMinutes(cp.getObject(PrefUtils.LOCATION_TIMESTAMP, Float.class));
            if(minsSinceLastLocation < 2){
                return cp.getObject(PrefUtils.LOCATION, Location.class);
            }
        }
        return mCurrentLocation;
    }

    @Override
    public void onConnected(Bundle bundle) {
        getLocation();
    }
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected from Google Play. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) try { // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) { // Thrown if Google Play services canceled the original PendingIntent
                e.printStackTrace();
            }
        else // If no resolution is available, display a dialog to the user with the error.
            Toast.makeText(this, "Google play connection failed, no resolution", Toast.LENGTH_SHORT).show();
    }
    public boolean isLocationEnabled(){
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void onLocationDisabled(){
        AlertDialogFragment adf = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(AlertDialogFragment.ALERT_KEY, AlertDialogFragment.LOCATION_FAILED);
        adf.setArguments(bundle);
        adf.show(getSupportFragmentManager(), AlertDialogFragment.ALERT_KEY);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        FragmentManager manager = getSupportFragmentManager();
        NewReportFragment nrf = (NewReportFragment) manager.findFragmentByTag(NEW_REPORT_TAG);
        ReportUploadingFragment ruf = (ReportUploadingFragment) manager.findFragmentByTag(UPLOAD_TAG);
        if(nrf != null)
            getSupportFragmentManager().putFragment(bundle, NEW_REPORT_TAG, nrf);
        else if (ruf != null)
            getSupportFragmentManager().putFragment(bundle, UPLOAD_TAG, ruf);
        cp.putObject(SAVED_REPORTS_KEY, savedReportKeys);
        cp.commit();
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle){
        super.onRestoreInstanceState(bundle);
    }

    public void beamUpReport(View view) {
        if(canSend()){
            FragmentManager manager = getSupportFragmentManager();
            NewReportFragment nrf = (NewReportFragment) manager.findFragmentByTag(NEW_REPORT_TAG);
            Report report = nrf.createNewReport(cp.getString(PrefUtils.USERNAME, ""), getLocation());
            ReportUploadingFragment uploadingFragment = new ReportUploadingFragment();
            Bundle bundle = new Bundle();
            report.saveState(REPORT_KEY, bundle);
            uploadingFragment.setArguments(bundle);
            manager.beginTransaction()
                    .replace(android.R.id.content, uploadingFragment, UPLOAD_TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }
    // called by the new report fragment's "save" button
    public void attemptSaveNewReport(View view) {
        NewReportFragment nrf = (NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG);
        if (getLocation() == null) {
            Toast.makeText(this, "Cannot access location, make sure location services enabled", Toast.LENGTH_SHORT).show();
        } else if(nrf != null) {
            saveReport(nrf.createNewReport(cp.getString(PrefUtils.USERNAME, ""), getLocation()), 0);
            finish();
        }
    }

    public boolean canSend() {
        if (!isOnline())
            Toast.makeText(this, "Connect to a network to send your report", Toast.LENGTH_SHORT).show();
        else if (getLocation() == null)
            Toast.makeText(this, "Cannot access location, make sure location services enabled", Toast.LENGTH_SHORT).show();
        else
            return true;
        return false;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
            return true;
        return false;
    }

    public void deleteReport(Report pendingReport){
        if(dbContains(pendingReport))
            commitCPO(ContentProviderOperation.newDelete(uriFor(pendingReport.dbId)).build());
    }

    public static Uri uriFor(int dbId){
        return ReportContract.Entry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(dbId)).build();
    }

    private boolean dbContains(Report report){
        String[] projection = new String[1];
        projection[0] = ReportContract.Entry.COLUMN_ID;
        Cursor c = getContentResolver().
                query(ReportContract.Entry.CONTENT_URI,
                        projection,
                        ReportContract.Entry.COLUMN_TIMESTAMP + " = " +'\"' + report.timeStamp + '\"', null, null);
        int instanceCt = c.getCount();
        c.close();
        return instanceCt > 0;
    }
    // Check if the report is currently in the database
    // if it is
    public void saveReport(Report report, int progress){
        if(progress == 0)
            progress = 1;
        if(progress > 3) // assume progress > 3 means it uploaded successfully
            progress = 0;
        if(!dbContains(report)){ // add report to DB if it's not there
                commitCPO(ContentProviderOperation.newInsert(ReportContract.Entry.CONTENT_URI)
                        .withValue(ReportContract.Entry.COLUMN_SERVER_ID, 0)
                        .withValue(ReportContract.Entry.COLUMN_TITLE, "")
                        .withValue(ReportContract.Entry.COLUMN_DETAILS, report.details)
                        .withValue(ReportContract.Entry.COLUMN_TIMESTAMP, report.timeStamp)
                        .withValue(ReportContract.Entry.COLUMN_LAT, Double.toString(report.latitude))
                        .withValue(ReportContract.Entry.COLUMN_LNG, Double.toString(report.longitude))
                        .withValue(ReportContract.Entry.COLUMN_USERNAME, cp.getString(PrefUtils.USERNAME, ""))
                        .withValue(ReportContract.Entry.COLUMN_MEDIAURL1, report.mediaPaths.get(0))
                        .withValue(ReportContract.Entry.COLUMN_MEDIAURL2, report.mediaPaths.get(1))
                        .withValue(ReportContract.Entry.COLUMN_MEDIAURL3, report.mediaPaths.get(2))
                        .build());
            } else{ // if it is, current report is the one this cpoBuilder is about (ASSUMED--NOT FULLY CONFIRMED)
                Log.e("SAVE REPORT", "Report was already in the database, to be updated not saved");
                updateDb();
            }
//            String[] selectionarg = new String[1];
//            selectionarg[0] = "0";
//            Cursor c1 = getContentResolver().
//                    query(ReportContract.Entry.CONTENT_URI,
//                            Report.PROJECTION,
//                            ReportContract.Entry.COLUMN_PENDINGFLAG + " > ? ", selectionarg, null);
////            while(c1.moveToNext()){
////                Log.e("DATABASE", "Saved report: " + c1.getString(c1.getColumnIndex(ReportContract.Entry.COLUMN_DETAILS)));
////            }
//            c1.close();
    }
    private void commitCPO(ContentProviderOperation cpo){
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        batch.add(cpo);
        commitBatch(batch);
    }

    private void commitBatch(ArrayList<ContentProviderOperation> batch){
        try {
            getContentResolver().applyBatch(ReportContract.CONTENT_AUTHORITY, batch);
            getContentResolver().notifyChange(ReportContract.Entry.CONTENT_URI, null, false);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void uploadSavedReports(){
        if(canSend()){
            FragmentManager manager = getSupportFragmentManager();
            ReportUploadingFragment uploadingFragment = new ReportUploadingFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(UPLOAD_SAVED_REPORTS_KEY, true);
            uploadingFragment.setArguments(bundle);
            manager.beginTransaction()
                    .replace(android.R.id.content, uploadingFragment, UPLOAD_TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    public void setUploader(ReportUploader uploader){
        this.uploader = uploader;
    }

    public ArrayList<Report> getSavedReports(){
        Cursor c = getContentResolver().
                query(ReportContract.Entry.CONTENT_URI,
                        Report.PROJECTION,
                        ReportContract.Entry.COLUMN_MEDIAURL3 + " NOT LIKE 'http%'", null, null); // Get all entries
        ArrayList<Report> savedReports = new ArrayList<Report>();
        while(c.moveToNext()){
            savedReports.add(new Report(c));
        }
        c.close();
        return savedReports;
    }

    public static int getSavedReportCount(Activity ac){
        String[] projection = new String[1];
        projection[0] = ReportContract.Entry.COLUMN_ID;
        Cursor c = ac.getContentResolver().
                    query(ReportContract.Entry.CONTENT_URI,
                          projection,
                          ReportContract.Entry.COLUMN_MEDIAURL3 + " NOT LIKE 'http%'", null, null); // Get all entries
        int count = c.getCount();
        c.close();
        return count;
    }

    // called by the UploadingFragment on progress update if it's uploading a saved report
    // updates the pending flag value and overwrites the corresponding value with the output from the server
    // Once the report is fully uploaded, the local DB treats it like a post it retrieved from the server
    public void uploadProgress(int progress, Report report){
        if(getIntent().getBooleanExtra(UPLOAD_SAVED_REPORTS_KEY, false))
            updateReportData(progress, report);
    }

    private void updateReportData(int progress, Report report){
        if(cpoBuilder == null)
            cpoBuilder = ContentProviderOperation.newUpdate(uriFor(report.dbId));
        this.progress = progress;
        switch(progress){
            case -1:
                cpoBuilder.withValue(ReportContract.Entry.COLUMN_MEDIAURL1, report.mediaPaths.get(2));
                Log.e("Activity CPOBuilder", "Server output I got: " + report.mediaPaths.get(2));
                updateDb();
                break;
            case 3:
                cpoBuilder.withValue(ReportContract.Entry.COLUMN_MEDIAURL1, report.mediaPaths.get(1));
                Log.e("Activity CPOBuilder", "Server output I got: " + report.mediaPaths.get(1));
            case 2:
                cpoBuilder.withValue(ReportContract.Entry.COLUMN_MEDIAURL1, report.mediaPaths.get(0));
                Log.e("Activity CPOBuilder", "Server output I got: "+ report.mediaPaths.get(0));
            case 1:
                cpoBuilder.withValue(ReportContract.Entry.COLUMN_SERVER_ID, report.serverId);
                cpoBuilder.withValue(ReportContract.Entry.COLUMN_TITLE, report.title);
                Log.e("Activity CPOBuilder", "Server output I got: "+ report.title + " also server id: " + report.serverId);
        }
    }
    // called after a report has uploaded successfully or if the activity stops
    private void updateDb(){
        if(cpoBuilder != null){
            Log.e("UPDATE DB", " Hey buddy! Update db was called. Proud of you");
            commitCPO(cpoBuilder.build());
            cpoBuilder = null;
        }
    }

    // provide dialog confirming the person wants to abandon their reports
    // if yes, go back
    // save the existing reports with save currentUploading
    @Override
    public void onBackPressed(){
        AlertDialogFragment adf = new AlertDialogFragment();
        adf.setAlertDialogListener(this);
        Bundle args = new Bundle();
        args.putInt(AlertDialogFragment.ALERT_KEY, AlertDialogFragment.LEAVING_UPLOAD);
        adf.setArguments(args);
        adf.show(getSupportFragmentManager(), AlertDialogFragment.ALERT_KEY);
    }

    public void onAlertButtonPressed(int eventKey){
        ReportUploadingFragment ruf =
                (ReportUploadingFragment) getSupportFragmentManager().findFragmentByTag(UPLOAD_TAG);
        switch(eventKey){
            case AlertDialogFragment.ABANDON_REPORTS:
                if(ruf != null)
                    deleteReport(ruf.pendingReport);
                break;
            case AlertDialogFragment.SAVE_REPORTS:
                if(ruf != null)
                    saveReport(ruf.pendingReport, ruf.progress);
                else
                    attemptSaveNewReport(null);
                break;
            }
        finish();
    }
}
