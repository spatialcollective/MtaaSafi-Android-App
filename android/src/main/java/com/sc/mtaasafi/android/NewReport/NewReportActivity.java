package com.sc.mtaasafi.android.NewReport;

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
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.sc.mtaasafi.android.AlertDialogFragment;
import com.sc.mtaasafi.android.ComplexPreferences;
import com.sc.mtaasafi.android.LogTags;
import com.sc.mtaasafi.android.MainActivity;
import com.sc.mtaasafi.android.Report;

import java.util.ArrayList;
import java.util.List;

public class NewReportActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private ComplexPreferences cp;
    private List<String> savedReportKeys;
    public final static String  REPORT_KEY = "pendingReport",
                                PREF_KEY = "myPrefs",
                                SAVED_REPORTS_KEY = "savedReportKeys",
                                UPLOAD_SAVED_REPORTS_KEY = "uploadSavedReports",
                                UPLOAD_TAG= "upload",
                                NEW_REPORT_TAG= "newreport";
    public String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userName = getPreferences(Context.MODE_PRIVATE).getString(MainActivity.USERNAME_KEY, "");
        restoreFragment(savedInstanceState);
        mLocationClient = new LocationClient(this, this, this);
        cp = ComplexPreferences.getComplexPreferences(this, PREF_KEY, MODE_PRIVATE);
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
        if (locationProviders == null || locationProviders.equals("")){
            onLocationDisabled();
        }
        mLocationClient.connect();
        Log.e(LogTags.MAIN_ACTIVITY, "onStart");
    }
    protected  void onResume(){
        super.onResume();
        Intent intent = getIntent();
        if(intent != null){
            if(intent.getBooleanExtra(UPLOAD_SAVED_REPORTS_KEY, false)) // the activity is supposed to upload its saved reports
                uploadSavedReports();
            userName = intent.getStringExtra(MainActivity.USERNAME_KEY);
        }
    }
    @Override
    protected void onStop(){
        mLocationClient.disconnect();
        super.onStop();
    }

    public int getScreenWidth(){return getWindowManager().getDefaultDisplay().getWidth();}

    public int getScreenHeight(){
        return getWindowManager().getDefaultDisplay().getHeight();
    }

    public Location getLocation() {
        if(mLocationClient != null && mLocationClient.isConnected()){
            mCurrentLocation = mLocationClient.getLastLocation();
        }
        return mCurrentLocation;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = mLocationClient.getLastLocation();
    }
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected from Google Play. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    // ======================Google Play Services Setup:======================
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 15000;

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
        FragmentManager manager = getSupportFragmentManager();
        NewReportFragment nrf = (NewReportFragment) manager.findFragmentByTag(NEW_REPORT_TAG);
        Report report = nrf.createNewReport(userName, getLocation());
        ReportUploadingFragment uploadingFragment = new ReportUploadingFragment();
        Bundle bundle = new Bundle();
        report.saveState(REPORT_KEY, bundle);
        uploadingFragment.setArguments(bundle);
        manager.beginTransaction()
                .replace(android.R.id.content, uploadingFragment, UPLOAD_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    public void attemptSave(View view) {
        NewReportFragment nrf = (NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG);
        if (getLocation() == null) {
            Toast.makeText(this, "Cannot access location, make sure location services enabled", Toast.LENGTH_SHORT).show();
        } else {
            saveReport(nrf.createNewReport(userName, getLocation()));
            finish();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
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
    public int getSavedReportCount(){
        return savedReportKeys.size();
    }
    public Report getNextSavedReport(){
        return cp.getObject(savedReportKeys.get(0), Report.class);
    }
    public void uploadSavedReports(){
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
    public void removeTopSavedReport(){
        cp.remove(savedReportKeys.get(0));
        savedReportKeys.remove(0);
        cp.putObject(SAVED_REPORTS_KEY, savedReportKeys);
        cp.commit();
        Log.e(LogTags.BACKEND_W, "SavedReportsRemaining: " + cp.getObject(SAVED_REPORTS_KEY, List.class).size());
    }
}
