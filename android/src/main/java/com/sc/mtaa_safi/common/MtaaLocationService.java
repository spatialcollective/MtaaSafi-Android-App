package com.sc.mtaa_safi.common;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.sc.mtaa_safi.SystemUtils.Utils;

public class MtaaLocationService extends Service {
    public Location mLocation = null, mCoarseLocation = null;
    public LocationManager mLocationManager;
    private LocationListener mGpsListener, mNetworkListener;
    public static final int TIME_DIFF = 1000 * 60 * 2, DIST_DIFF = 10; // 2 Min, 10 Meters

    private final IBinder mBinder = new LocalBinder();
    public MtaaLocationService() { }
    public class LocalBinder extends Binder {
        public MtaaLocationService getService() { return MtaaLocationService.this; }
    }

    @Override
    public void onCreate() { startLocationMgmt(); }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    public Location findLocation(Context context) {
        Location l = null;
        if (mLocation != null)
            l = mLocation;
        else if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
            l = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        else if (Utils.getLocation(context).getTime() != 0)
            l = Utils.getLocation(context);
        return l;
    }

    public boolean hasLocation(Context context) {
        if (mLocation != null)
            return true;
        Log.v("Mtaa Location Service", "Saved Location time: " + Utils.getLocation(context).getTime());
        if (Utils.getLocation(context).getTime() != 0) {
            mLocation = Utils.getLocation(context);
            return true;
        } else if (mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
            return true;
        return false;
    }
    public boolean hasCoarseLocation(Context context) {
        Location lastLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (mCoarseLocation == null && lastLoc == null && Utils.getCoarseLocation(context).getTime() == 0)
            return false;
        if (mCoarseLocation == null && lastLoc != null) {
            mCoarseLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Utils.saveCoarseLocation(context, mCoarseLocation);
        }
        return true;
    }

    private void startLocationMgmt() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mCoarseLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        createListeners(this);
        requestUpdates();
    }

    public void requestUpdates() {
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_DIFF, DIST_DIFF, mGpsListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_DIFF * 5, DIST_DIFF * 10, mNetworkListener);
    }

    public void removeUpdates() {
        mLocationManager.removeUpdates(mGpsListener);
        mLocationManager.removeUpdates(mNetworkListener);
    }

    private void createListeners(final Context c) {
        mGpsListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (newLocationIsBetter(location)) {
                    mLocation = location;
                    Utils.saveLocation(c, location);
                }
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };

        mNetworkListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                mCoarseLocation = location;
                Utils.saveCoarseLocation(c, location);
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
    }

    public boolean isGPSEnabled() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String providers = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
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

    private boolean newLocationIsBetter(Location location) {
        if (mLocation == null)
            return true;
        long timeDelta = location.getTime() - mLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME_DIFF;
        boolean isSignificantlyOlder = timeDelta < -TIME_DIFF;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer)
            return true;
        else if (isSignificantlyOlder)
            return false;

        int accuracyDelta = (int) (location.getAccuracy() - mLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        boolean isFromSameProvider = isSameProvider(location.getProvider(), mLocation.getProvider());

        if (isMoreAccurate)
            return true;
        else if (isNewer && !isLessAccurate)
            return true;
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
            return true;
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null)
            return provider2 == null;
        return provider1.equals(provider2);
    }

    // use cached location if it's fre$h & accurate enough
//       if (mLocationClient != null && mLocationClient.isConnected() && mCurrentLocation == null) {
//          Location lastLocation = mLocationClient.getLastLocation();
//          long timeElapsedMillis = System.currentTimeMillis() - lastLocation.getTime();
//          float timeElapsedSeconds =(float)(timeElapsedMillis / 1000);
//          float timeElapsedMinutes = timeElapsedSeconds / 60;
//          if (lastLocation.getAccuracy() != 0.0 && lastLocation.getAccuracy() < 30.0 && timeElapsedMinutes < 1.5)
//              mCurrentLocation = lastLocation;
//       }
//       if (mCurrentLocation == null)
//            return mLocationClient.getLastLocation();
//        return mCurrentLocation;
//    }
//    @Override
//    public void onLocationChanged(Location location) { setLocation(location); }
//
//    @Override
//    public void onConnected(Bundle bundle) {
//        LocationRequest mLocationRequest = LocationRequest.create();
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setInterval(2500);
//        mLocationRequest.setFastestInterval(1000);
//        mLocationClient.requestLocationUpdates(mLocationRequest, this);
//    }
//
//    @Override
//    public void onDisconnected() {
//        Toast.makeText(this, "Disconnected from Google Play. Please re-connect.", Toast.LENGTH_SHORT).show();
//    }
//    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 15000;
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        if (connectionResult.hasResolution()) try { // Start an Activity that tries to resolve the error
//                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
//            } catch (IntentSender.SendIntentException e) { // Thrown if Google Play services canceled the original PendingIntent
//                e.printStackTrace();
//            }
//        else // If no resolution is available, display a dialog to the user with the error.
//            Toast.makeText(this, "Google play connection failed, no resolution", Toast.LENGTH_SHORT).show();
//    }
//
}
