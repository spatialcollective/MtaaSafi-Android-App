package com.sc.mtaasafi.android;

import android.content.Context;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;
import java.util.List;

public class NewReportActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private ComplexPreferences cp;
    private NewReportFragment mFragment;
    private List<String> savedReportKeys;
    public final static String  REPORT_KEY = "pendingReport",
                                PREF_KEY = "myPrefs",
                                SAVED_REPORT_KEY_KEY = "savedReportKeys",
                                UPLOAD_SAVED_REPORTS_KEY = "uploadSavedReports";
    public String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userName = getPreferences(Context.MODE_PRIVATE).getString(MainActivity.USERNAME_KEY, "");
        if (savedInstanceState != null)
            mFragment = (NewReportFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mFragment");
        if (mFragment == null) {
            Log.e("Creating Activity", "fragment was null...");
            mFragment = new NewReportFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, mFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
        mLocationClient = new LocationClient(this, this, this);
        cp = ComplexPreferences.getComplexPreferences(this, PREF_KEY, MODE_PRIVATE);
        savedReportKeys = cp.getObject(SAVED_REPORT_KEY_KEY, List.class);
        if(savedReportKeys == null)
            savedReportKeys = new ArrayList<String>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LogTags.MAIN_ACTIVITY, "onStart");
    }
    protected  void onResume(){
        super.onResume();
        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals(""))
            onLocationDisabled();
        mLocationClient.connect();
        if(getIntent() != null){
            if(getIntent().getBooleanExtra(UPLOAD_SAVED_REPORTS_KEY, false)) // the activity is supposed to upload its saved reports
                uploadSavedReports();
        }
    }
    @Override
    protected void onStop(){
        mLocationClient.disconnect();
        super.onStop();
    }

    public int getScreenWidth(){
        return getWindowManager().getDefaultDisplay().getWidth();
    }

    public Location getLocation() {
        if(mLocationClient != null)
            mCurrentLocation = mLocationClient.getLastLocation();
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
        getSupportFragmentManager().putFragment(bundle, "mFragment", mFragment);
        cp.putObject(SAVED_REPORT_KEY_KEY, savedReportKeys);
        cp.commit();
    }

    public void beamUpReport(Report report){
        FragmentManager manager = getSupportFragmentManager();
        ReportUploadingFragment uploadingFragment = new ReportUploadingFragment();
        Bundle bundle = new Bundle();
        report.saveState(bundle);
        uploadingFragment.setArguments(bundle);
        manager.beginTransaction()
                .replace(android.R.id.content, uploadingFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
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

    public void saveReport(Report report){
        savedReportKeys.add(report.timeStamp);
        cp.putObject(report.timeStamp, report);
        cp.putObject(SAVED_REPORT_KEY_KEY, savedReportKeys);
        cp.commit();
        Log.e("SAVE REPORT", cp.getObject(report.timeStamp, Report.class).details + " Saved reports: " + cp.getObject(SAVED_REPORT_KEY_KEY, List.class).size());
    }
    public void clearNewReportData(){
//        a;dsljfas;lfdkjas;dlfkjas;dlfjas;lkfdjasjasd;lfkjasdf
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
                .replace(android.R.id.content, uploadingFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

    }
    public void removeTopSavedReport(){
        cp.remove(savedReportKeys.get(0));
        savedReportKeys.remove(0);
        cp.putObject(SAVED_REPORT_KEY_KEY, savedReportKeys);
        cp.commit();
        Log.e(LogTags.BACKEND_W, "SavedReportsRemaining: " + cp.getObject(SAVED_REPORT_KEY_KEY, List.class).size());
    }
}
