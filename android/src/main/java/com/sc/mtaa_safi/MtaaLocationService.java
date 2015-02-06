package com.sc.mtaa_safi;

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
import android.widget.Toast;

import com.sc.mtaa_safi.SystemUtils.LogTags;


public class MtaaLocationService extends Service {
    public Location mLocation;
    LocationManager mLocationManager;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private final IBinder mBinder = new LocalBinder();
    public MtaaLocationService() { }
    public class LocalBinder extends Binder {
        public MtaaLocationService getService() { return MtaaLocationService.this; }
    }

    @Override
    public void onCreate() { startLocationMgmt(); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    public Location getLocation() {
//        Log.e("MtaaLocationService", "getting location");
        if (mLocation != null)
            return mLocation;

        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //Log.e("MtaaLocationService", "got nothing");

        return null;
    }

    public boolean hasLocation() {
        if (mLocation != null)
            return true;
        Toast.makeText(this, "No location detected", Toast.LENGTH_SHORT);
        return false;
    }


    private void startLocationMgmt() {
//        Log.e("MtaaLocationService", "managing location");
        mLocation = null;
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
//                Log.e("MtaaLocationService", "location changed");
                if (newLocationIsBetter(location))
                    mLocation = location;
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TWO_MINUTES, 10, locationListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TWO_MINUTES, 10, locationListener);
    }

    private boolean newLocationIsBetter(Location location) {
//        Log.e("MtaaLocationService", "got a new location");
        if (mLocation == null)
            return true;
//        Log.e("MtaaLocationService", "old location was not null");
        long timeDelta = location.getTime() - mLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer)
            return true;
        else if (isSignificantlyOlder)
            return false;
//        Log.e("MtaaLocationService", "insignificant time difference");

//        Log.e("MtaaLocationService", "Accuracy is: " + location.getAccuracy());
        int accuracyDelta = (int) (location.getAccuracy() - mLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                mLocation.getProvider());
//        Log.e("MtaaLocationService", "Provider is: " + location.getProvider());

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
