package com.sc.mtaa_safi.feed;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sc.mtaa_safi.MtaaLocationService;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.AlertDialogFragment;
import com.sc.mtaa_safi.SystemUtils.GcmIntentService;
import com.sc.mtaa_safi.SystemUtils.RegisterWithGcm;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.database.ReportDatabase;
import com.sc.mtaa_safi.login.FacebookActivity;
import com.sc.mtaa_safi.login.GooglePlusActivity;
import com.sc.mtaa_safi.login.LoginActivityListener;
import com.sc.mtaa_safi.login.LoginManagerFragment;
import com.sc.mtaa_safi.newReport.NewReportActivity;
import com.sc.mtaa_safi.uploading.UploadingActivity;

import java.util.concurrent.atomic.AtomicInteger;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends ActionBarActivity implements
        AlertDialogFragment.AlertDialogListener, LoginActivityListener {

    ReportDetailFragment detailFragment;
    NewsFeedFragment newsFeedFrag;
    LoginManagerFragment loginManagerFragment;
    LocationListener locationListener;

    private static final int GOOGLE_PLUS_LOGIN = 100, FACEBOOK_LOGIN = 102;
    public final static String NEWSFEED_TAG = "newsFeed", DETAIL_TAG = "details", LOGIN_TAG= "login";

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
        registerWithGcm();
        restoreFragment(savedInstanceState);
        if (getIntent().getExtras() != null )
            viewDetailFromNotification();
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

    private void viewDetailFromNotification() {
        GcmIntentService.resetAll();
        ReportDatabase dbHelper  = new ReportDatabase(this);
        if (getIntent().getIntExtra("reportId", -1) == GcmIntentService.MULTIPLE_UPDATE) {
            newsFeedFrag.setSection(1);
        } else {
            Cursor cursor = dbHelper.getReadableDatabase().query(Contract.Entry.TABLE_NAME,
                    null, Contract.Entry.COLUMN_SERVER_ID + " = " + getIntent().getIntExtra("reportId", -1), null, null, null, null);
            if (cursor.moveToFirst()) {
                Report r = new Report(cursor);
                cursor.close();
                goToDetailView(r);
            } else { Log.e("Main activity", "Cursor is empty..."); }
        }
    }

    private void restoreFragment(Bundle savedInstanceState) {
        loginManagerFragment = initializeLoginManager();
        if (savedInstanceState != null)
            detailFragment = (ReportDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, DETAIL_TAG);
        if (detailFragment == null && Utils.isSignedIn(this))
            goToFeed(savedInstanceState);
        else if (!Utils.isSignedIn(this))
            showLoginManager();
    }

    private void registerWithGcm() {
        if (checkGPlayServices()) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String regid = Utils.getRegistrationId(getApplicationContext());
            if (regid.isEmpty())
                new RegisterWithGcm(this, gcm).execute();
        }
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

    public void goToDetailView(Report r) {
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
        // if (!loginManagerFragment.isLoggedIn(this))
        //     showLoginManager();
        Utils.saveScreenWidth(this, getScreenWidth());
        if (!isGPSEnabled())
            onLocationDisabled();
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkGPlayServices();
    }
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (detailFragment != null && detailFragment.isAdded())
            getSupportFragmentManager().putFragment(bundle, DETAIL_TAG, detailFragment);
        if (loginManagerFragment != null && loginManagerFragment.isAdded())
            getSupportFragmentManager().putFragment(bundle, LOGIN_TAG, loginManagerFragment);
        if (newsFeedFrag != null && newsFeedFrag.isAdded())
            getSupportFragmentManager().putFragment(bundle, NEWSFEED_TAG, newsFeedFrag);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == FACEBOOK_LOGIN)
            Toast.makeText(this, "Facebook user logged in", Toast.LENGTH_SHORT).show();
        else if (resultCode == RESULT_OK && requestCode == GOOGLE_PLUS_LOGIN)
            Toast.makeText(this, "Google+ user logged in", Toast.LENGTH_SHORT).show();
        if (resultCode == RESULT_OK) {
            registerWithGcm();
            goToFeed(null);
        } else
            showLoginManager();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    public LoginManagerFragment initializeLoginManager() {
        LoginManagerFragment loginManagerFragment = (LoginManagerFragment) getSupportFragmentManager().findFragmentByTag(LOGIN_TAG);
        if (loginManagerFragment == null) {
            loginManagerFragment = new LoginManagerFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(loginManagerFragment, LOGIN_TAG)
                    .commit();
        }
        return loginManagerFragment;
    }

    public void showLoginManager() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, loginManagerFragment, LOGIN_TAG)
                .commit();
    }

    public void uploadSavedReports() {
        startActivity(new Intent().setClass(this, UploadingActivity.class)
                .setAction(String.valueOf(0)));
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

    private boolean checkGPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000).show();
            else
                finish();
            return false;
        }
        return true;
    }

    private void onLocationDisabled() {
        AlertDialogFragment alert = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("alert");
        if (alert != null)
            alert.dismiss();
        AlertDialogFragment.showAlert(AlertDialogFragment.LOCATION_FAILED, this, getSupportFragmentManager());
    }
    private void onLocationEnabled() {
        AlertDialogFragment alert = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("alert");
        if (alert != null && alert.isAdded())
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
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            switch (locationMode) {
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

    @Override
    public void startLoginActivity(String network) {
        if (network.equals("google"))
            startActivityForResult(new Intent(this,GooglePlusActivity.class), GOOGLE_PLUS_LOGIN);
        else if(network.equals("facebook"))
            startActivityForResult(new Intent(this,FacebookActivity.class), FACEBOOK_LOGIN);
    }
}
