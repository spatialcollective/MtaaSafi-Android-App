package com.sc.mtaa_safi.common;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sc.mtaa_safi.MtaaLocationService;
import com.sc.mtaa_safi.SystemUtils.AlertDialogFragment;
import com.sc.mtaa_safi.SystemUtils.RegisterWithGcm;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.uploading.UploadingActivity;

public abstract class BaseActivity extends ActionBarActivity implements AlertDialogFragment.AlertDialogListener {
    protected LocationListener providerStateChangeListener;

    protected MtaaLocationService mBoundService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((MtaaLocationService.LocalBinder) service).getService();
            if (mBoundService != null && !mBoundService.isGPSEnabled())
                locationDisabled();

            providerStateChangeListener = new LocationListener() {
                public void onLocationChanged(Location location) {}
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                public void onProviderEnabled(String provider) { locationEnabled(); }
                public void onProviderDisabled(String provider) { locationDisabled(); }
            };
//            mBoundService.requestUpdates();
            mBoundService.mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MtaaLocationService.TIME_DIFF, MtaaLocationService.DIST_DIFF, providerStateChangeListener);
        }
        public void onServiceDisconnected(ComponentName className) { mBoundService = null; } // This should never happen
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
    protected void onResume() {
        super.onResume();
        Utils.saveScreenWidth(this, getScreenWidth());
        checkGPlayServices();
        if (mBoundService != null) {
//            mBoundService.requestUpdates();
            if (!mBoundService.isGPSEnabled())
                locationDisabled();
            else
                locationEnabled();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
//        mBoundService.removeUpdates();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mBoundService != null)
            mBoundService.mLocationManager.removeUpdates(providerStateChangeListener);
        unbindService(mConnection);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    public void uploadSavedReports() {
        startActivity(new Intent().setClass(this, UploadingActivity.class).setAction(String.valueOf(0)));
    }

    public int getScreenWidth() { return getWindowManager().getDefaultDisplay().getWidth(); }
    public int getScreenHeight() { return getWindowManager().getDefaultDisplay().getHeight(); }

    public Location getLocation() { return mBoundService.findLocation(this); }
    public boolean hasCoarseLocation() { return mBoundService.hasCoarseLocation(this); }

    private void locationDisabled() {
        AlertDialogFragment alert = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(AlertDialogFragment.ALERT_KEY);
        if (alert != null)
            alert.dismiss();
        AlertDialogFragment.showAlert(AlertDialogFragment.LOCATION_FAILED, this, getSupportFragmentManager());
    }
    private void locationEnabled() {
        AlertDialogFragment alert = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(AlertDialogFragment.ALERT_KEY);
        if (alert != null && alert.isAdded())
            alert.dismiss();
    }

    public void onAlertButtonPressed(int eventKey) {
        switch(eventKey) {
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

    protected void registerWithGcm() {
        if (checkGPlayServices()) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String regid = Utils.getRegistrationId(getApplicationContext());
            if (regid.isEmpty())
                new RegisterWithGcm(this, gcm).execute();
        }
    }

    protected boolean checkGPlayServices() { // Update to use alertdialog interface
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
}
