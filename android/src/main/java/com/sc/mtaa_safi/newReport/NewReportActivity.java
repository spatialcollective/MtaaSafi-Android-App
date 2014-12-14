package com.sc.mtaa_safi.newReport;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
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

import com.google.android.gms.location.LocationClient;
import com.sc.mtaa_safi.MtaaLocationService;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.ComplexPreferences;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;
import com.sc.mtaa_safi.uploading.UploadingActivity;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.AlertDialogFragment;
import com.sc.mtaa_safi.database.Contract;

public class NewReportActivity extends ActionBarActivity {
    private Location mCurrentLocation;
    private LocationClient mLocationClient;
    private ComplexPreferences cp;
    public final static String NEW_REPORT_TAG = "newreport";
    private Menu menu;
    private boolean sendSaveEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreFragment(savedInstanceState);
        sendSaveEnabled = false;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_remove);
        cp = PrefUtils.getPrefs(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_report, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { return super.onOptionsItemSelected(item); }
    @Override
    public void onResume(){
        super.onResume();
        supportInvalidateOptionsMenu();
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

    private MtaaLocationService mBoundService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((MtaaLocationService.LocalBinder) service).getService();
//            Toast.makeText(this, "Location Service Enabled", Toast.LENGTH_SHORT).show();
        }
        // This should never happen
        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
//            Toast.makeText(this, "Location Service Disconnected", Toast.LENGTH_SHORT).show();
        }
    };
    void bindLocationService() {
        bindService(new Intent(this, MtaaLocationService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindLocationService();
    }
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    public int getScreenWidth() { return getWindowManager().getDefaultDisplay().getWidth(); }
    public int getScreenHeight() { return getWindowManager().getDefaultDisplay().getHeight(); }

    // ======================Google Play Services Setup:======================
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 15000;

    public Location getLocation() {
        return mBoundService.getLocation();
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
            saveNewReport((NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG));
            finish();
        } else
            Toast.makeText(this, "No location detected", Toast.LENGTH_SHORT);
    }

    public Uri saveNewReport(NewReportFragment frag) {
        Report newReport = new Report(frag.detailsText, cp.getString(PrefUtils.USERNAME, ""), getLocation(), frag.picPaths);
        Log.e("New Report Activity", "inserting");
        return getContentResolver().insert(Contract.Entry.CONTENT_URI, newReport.getContentValues());
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
        } else
            Toast.makeText(this, "No location detected", Toast.LENGTH_SHORT);
    }

    public void takePic(View view) {
        ((NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG)).takePicture();
    }

    private boolean transporterHasLocation() {
        if (getLocation() != null)
            return true;
        showLocationOffWarning();
        return false;
    }
}
