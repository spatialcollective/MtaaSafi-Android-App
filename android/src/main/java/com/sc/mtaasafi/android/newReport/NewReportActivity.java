package com.sc.mtaasafi.android.newReport;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.sc.mtaasafi.android.feed.MainActivity;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.newReport.NewReportFragment;
import com.sc.mtaasafi.android.newReport.ReportUploadingFragment;

import java.util.ArrayList;
import java.util.List;

public class NewReportActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private LocationClient mLocationClient;
    private ComplexPreferences cp;
    private List<String> savedReportKeys;
    public final static String  REPORT_KEY = "pendingReport",
                                SAVED_REPORTS_KEY = "savedReportKeys",
                                UPLOAD_SAVED_REPORTS_KEY = "uploadSavedReports",
                                UPLOAD_TAG= "upload",
                                NEW_REPORT_TAG= "newreport";
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
        mLocationClient.connect();
    }

    protected  void onResume(){
        super.onResume();
        Intent intent = getIntent();
        if(intent != null){
            if(intent.getBooleanExtra(UPLOAD_SAVED_REPORTS_KEY, false)){
                // the activity is supposed to upload its saved reports
                ReportUploadingFragment ruf = (ReportUploadingFragment) getSupportFragmentManager()                                                .findFragmentByTag(UPLOAD_TAG);
                if(ruf == null){
                    uploadSavedReports();
                }
            }
        }
        if(intent != null && intent.getBooleanExtra(UPLOAD_SAVED_REPORTS_KEY, false)) // the activity is supposed to upload its saved reports
            uploadSavedReports();
    }

    @Override
    protected void onStop(){
        mLocationClient.disconnect();
        super.onStop();
    }

    public int getScreenWidth() { return getWindowManager().getDefaultDisplay().getWidth(); }

    public int getScreenHeight() { return getWindowManager().getDefaultDisplay().getHeight(); }

    // ======================Google Play Services Setup:======================
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 15000;

    public Location getLocation() {
        Location mCurrentLocation;
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
        return null;
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
        if (connectionResult.hasResolution()) {
            try { // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) { // Thrown if Google Play services canceled the original PendingIntent
                e.printStackTrace();
            }
        } else { // If no resolution is available, display a dialog to the user with the error.
            CharSequence text = "Google play connection failed, no resolution";
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    public boolean isLocationEnabled(){
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void onLocationDisabled(){
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

    public void attemptSave(View view) {
        NewReportFragment nrf = (NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG);
        if (getLocation() == null) {
            Toast.makeText(this, "Cannot access location, make sure location services enabled", Toast.LENGTH_SHORT).show();
        } else {
            saveReport(nrf.createNewReport(cp.getString(PrefUtils.USERNAME, ""), getLocation()));
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

    // save the report to the complex preferences and then go back to the main activity
    public void saveReport(Report report){
        savedReportKeys.add(report.timeStamp);
        cp.putObject(report.timeStamp, report);
        cp.putObject(SAVED_REPORTS_KEY, savedReportKeys);
        cp.commit();
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class)
              .putExtra(SAVED_REPORTS_KEY, true);
        startActivity(intent);
        Log.e("SAVE REPORT", cp.getObject(report.timeStamp, Report.class).details + " Saved reports: " + cp.getObject(SAVED_REPORTS_KEY, List.class).size());
    }

    public int getSavedReportCount(){ return savedReportKeys.size(); }

//    public Report popSavedReport(){
//        if(!savedReportKeys.isEmpty()){
//            Report report = cp.getObject(savedReportKeys.get(0), Report.class);
//            cp.remove(savedReportKeys.get(0));
//            cp.commit();
//            savedReportKeys.remove(0);
//            return report;
//        }
//        return null;
//    }
    public Report getTopSavedReport(){
        if(!savedReportKeys.isEmpty())
            return cp.getObject(savedReportKeys.get(0), Report.class);
        return null;
    }

    public void removeTopSavedReport(){
        if(!savedReportKeys.isEmpty()){
            cp.remove(savedReportKeys.get(0));
            savedReportKeys.remove(0);
            cp.remove(SAVED_REPORTS_KEY);
            cp.putObject(SAVED_REPORTS_KEY, savedReportKeys);
            cp.commit();
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

    public ArrayList<String> getSavedReportSummaries(){
        ArrayList<String> summaries = new ArrayList<String>();
        cp.remove(SAVED_REPORTS_KEY);
        for(String key : savedReportKeys){
            Report r = cp.getObject(key, Report.class);
            String details = r.details;
            summaries.add(details);
        }
        return summaries;
    }
}
