package com.sc.mtaasafi.android.newReport;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.SystemUtils.AlertDialogFragment;
import com.sc.mtaasafi.android.SystemUtils.ComplexPreferences;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.database.Contract;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.uploading.ReportUploadingFragment;
import com.sc.mtaasafi.android.uploading.UploadingActivity;

public class NewReportActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private LocationClient mLocationClient;
    private ComplexPreferences cp;
    public final static String NEW_REPORT_TAG = "newreport";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreFragment(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_remove);
        mLocationClient = new LocationClient(this, this, this);
        cp = PrefUtils.getPrefs(this);
    }

    private void restoreFragment(Bundle savedInstanceState){
        FragmentManager manager = getSupportFragmentManager();
        NewReportFragment frag = null;
        if (savedInstanceState != null)
            frag = (NewReportFragment) getSupportFragmentManager().getFragment(savedInstanceState, NEW_REPORT_TAG);
        if (frag == null)
            frag = new NewReportFragment();
        manager.beginTransaction()
                .replace(android.R.id.content, frag, NEW_REPORT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals(""))
             showLocationOffWarning();
        else
            mLocationClient.connect();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.disconnect();
    }

    public void uploadSavedReportsClicked(View view) { uploadSavedReports(); }
    public void uploadSavedReports() {
       if(getLocation() != null){
           Intent intent = new Intent().setClass(this, UploadingActivity.class)
                   .setAction(String.valueOf(0));
           startActivity(intent);
       } else
            Toast.makeText(this, "Location services not yet connected", Toast.LENGTH_SHORT);
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
            if (minsSinceLastLocation < 2)
                return cp.getObject(PrefUtils.LOCATION, Location.class);
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

    private void showLocationOffWarning() {
        AlertDialogFragment adf = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(AlertDialogFragment.ALERT_KEY, AlertDialogFragment.LOCATION_FAILED);
        adf.setArguments(bundle);
        adf.show(getSupportFragmentManager(), AlertDialogFragment.ALERT_KEY);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        NewReportFragment frag = (NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG);
        if (frag != null) // are we sure this isn't holding the fragment longer than necessary?
            getSupportFragmentManager().putFragment(bundle, NEW_REPORT_TAG, frag);
    }

    public void attemptSave(View view) {
        Log.e("New Report Activity", "attempting save");
        if (transporterHasLocation()) {
            Log.e("New Report Activity", "have location");
            Uri newReportUri = saveNewReport((NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG));
            Log.e("New Report Activity", "Report inserted. Uri is: " + newReportUri.toString());
            finish();
        }
    }
    public void attemptBeamOut(View view) {
        if (transporterHasLocation()) {
            NewReportFragment nrf =
                    (NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG);
            Uri newReportUri = saveNewReport(nrf);
            Intent intent = new Intent();
            intent.setClass(this, UploadingActivity.class);
            intent.setData(newReportUri);
            startActivity(intent);
            finish();
        }
    }

    private boolean transporterHasLocation() {
        if (getLocation() != null)
            return true;
        showLocationOffWarning();
        return false;
    }

    public Uri saveNewReport(NewReportFragment frag) {
        Report newReport = new Report(frag.detailsText, cp.getString(PrefUtils.USERNAME, ""), getLocation(), frag.picPaths);
        Log.e("New Report Activity", "inserting");
        return getContentResolver().insert(Contract.Entry.CONTENT_URI, newReport.getContentValues());
    }

    public static int getSavedReportCount(Activity ac){
        String[] projection = new String[1];
        projection[0] = Contract.Entry.COLUMN_ID;
        Cursor c = ac.getContentResolver().query(
            Contract.Entry.CONTENT_URI,
            projection,
            Contract.Entry.COLUMN_PENDINGFLAG + " >= 0 ", null, null);
        int count = c.getCount();
        c.close();
        return count;
    }
}
