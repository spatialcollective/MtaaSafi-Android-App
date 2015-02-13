package com.sc.mtaa_safi.feed;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sc.mtaa_safi.MtaaLocationService;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.AlertDialogFragment;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.newReport.NewReportActivity;
import com.sc.mtaa_safi.uploading.UploadingActivity;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends ActionBarActivity implements
        AlertDialogFragment.AlertDialogListener {

    ReportDetailFragment detailFragment;
    NewsFeedFragment newsFeedFrag;
    LocationListener locationListener;
    LocationManager mLocationManager;
    static final int    REQUEST_CODE_PICK_ACCOUNT = 1000;
    public final static String NEWSFEED_TAG = "newsFeed", DETAIL_TAG = "details", ONBOARD_TAG= "onboard";

    private MtaaLocationService mBoundService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((MtaaLocationService.LocalBinder) service).getService();
        }
        public void onServiceDisconnected(ComponentName className) { mBoundService = null; } // This should never happen
    };

    void bindLocationService() {
        bindService(new Intent(this, MtaaLocationService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        restoreFragment(savedInstanceState);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {}
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {
                onLocationEnabled();
            }
            public void onProviderDisabled(String provider) {
                onLocationDisabled();
            }
        };
    }

    private void restoreFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            detailFragment = (ReportDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, DETAIL_TAG);
        if (detailFragment == null)
            goToFeed(savedInstanceState);
    }

    public void goToFeed(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            newsFeedFrag = (NewsFeedFragment) getSupportFragmentManager().getFragment(savedInstanceState, NEWSFEED_TAG);
        if (newsFeedFrag == null) {
            newsFeedFrag = new NewsFeedFragment();
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, newsFeedFrag, NEWSFEED_TAG)
                .commit();
        }
    }

    public void goToDetailView(Report r, int position) {
        detailFragment = new ReportDetailFragment();
        detailFragment.setData(r);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment, DETAIL_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    public void goToNewReport(View view) {
        goToNewReport();
    }
    public void goToNewReport() {
        Intent intent = new Intent();
        intent.setClass(this, NewReportActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        supportInvalidateOptionsMenu();
        bindLocationService();
        pickUserAccount();
        Utils.saveScreenWidth(this, getScreenWidth());
        setUpWeirdGPlayStuff();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60 * 2, 10, locationListener);
        if(!isGPSEnabled())
            onLocationDisabled();
    }
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mLocationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        if (detailFragment != null && detailFragment.isAdded())
            getSupportFragmentManager().putFragment(bundle, DETAIL_TAG, detailFragment);
        if (newsFeedFrag != null)
            getSupportFragmentManager().putFragment(bundle, NEWSFEED_TAG, newsFeedFrag);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED && requestCode == REQUEST_CODE_PICK_ACCOUNT)
            Toast.makeText(this, "You must pick an account to proceed", Toast.LENGTH_SHORT).show();
        else if (requestCode == REQUEST_CODE_PICK_ACCOUNT)
            Utils.saveUserName(this, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void pickUserAccount() {
        if (Utils.getUserName(this).isEmpty()) {
            String[] accountTypes = new String[] {"com.google"};
            Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
        }
    }

    public void uploadSavedReports() {
        Intent intent = new Intent().setClass(this, UploadingActivity.class)
                .setAction(String.valueOf(0));
        startActivity(intent);
    }
    public int getScreenWidth() { return getWindowManager().getDefaultDisplay().getWidth(); }
    public int getScreenHeight() { return getWindowManager().getDefaultDisplay().getHeight(); }

    public Location getLocation() { return mBoundService.findLocation(this); }

    @Override
    public void onAlertButtonPressed(int eventKey) {
        switch(eventKey){
            case AlertDialogFragment.RE_FETCH_FEED:
                break;
            case AlertDialogFragment.SEND_SAVED_REPORTS:
                uploadSavedReports();
                break;
            case AlertDialogFragment.INSTALL_GPLAY:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
                break;
            case AlertDialogFragment.UPDATE_GPLAY:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
                break;
        }
    }

    private void setUpWeirdGPlayStuff() {
        int gPlayCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        switch (gPlayCode) {
            case ConnectionResult.SERVICE_MISSING:
                AlertDialogFragment.showAlert(AlertDialogFragment.GPLAY_MISSING, this, getSupportFragmentManager());
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                AlertDialogFragment.showAlert(AlertDialogFragment.GPLAY_UPDATE, this, getSupportFragmentManager());
                break;
            case ConnectionResult.SERVICE_DISABLED:
                AlertDialogFragment.showAlert(AlertDialogFragment.GPLAY_DISABLED, this, getSupportFragmentManager());
                break;
            case ConnectionResult.SERVICE_INVALID:
                AlertDialogFragment.showAlert(AlertDialogFragment.GPLAY_INVALID, this, getSupportFragmentManager());
                break;
        }
    }

    private void onLocationDisabled(){

        AlertDialogFragment alert = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("alert");
        if (alert != null)
            alert.dismiss();
        AlertDialogFragment.showAlert(AlertDialogFragment.LOCATION_FAILED, this, getSupportFragmentManager());
    }

    private void onLocationEnabled(){
        AlertDialogFragment alert = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("alert");
        if (alert != null && alert.isVisible())
            alert.dismiss();
    }

    public boolean isGPSEnabled(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
            String providers = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return providers.contains(LocationManager.GPS_PROVIDER);
        } else {
            final int locationMode;
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(),
                        Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            switch (locationMode){
                case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                    return true;
                case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                case Settings.Secure.LOCATION_MODE_OFF:
                default:
                    return false;
            }
        }
    }

    private void goToOnboarding(){
//        OnboardingFragment onboardingFragment = (OnboardingFragment)
//                                                    getSupportFragmentManager().findFragmentByTag(ONBOARD_TAG);
//        if(onboardingFragment != null)
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, onboardingFragment, ONBOARD_TAG)
//                    .commit();
//        else
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new OnboardingFragment(), ONBOARD_TAG)
//                    .commit();
//
//        getSupportActionBar().hide();
    }
}
