package com.sc.mtaa_safi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MtaaLocationService extends Service {
    public Location mLocation;
    LocationManager mLocationManager;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private final IBinder mBinder = new LocalBinder();
    public MtaaLocationService() { }
    public class LocalBinder extends Binder {
        public MtaaLocationService getService() {
            return MtaaLocationService.this;
        }
    }

    @Override
    public void onCreate() { startLocationMgmt(); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    public Location getLocation() {
        if (mLocation != null)
            return mLocation;
        else
            return mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private void startLocationMgmt() {
        mLocation = null;
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (newLocationIsBetter(location))
                    mLocation = location;
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private boolean newLocationIsBetter(Location location) {
        Log.e("MtaaLocationService", "got a new location");
        if (mLocation == null)
            return true;

        long timeDelta = location.getTime() - mLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer)
            return true;
        else if (isSignificantlyOlder)
            return false;
        Log.e("MtaaLocationService", "insignificant time difference");

        Log.e("MtaaLocationService", "Accuracy is: " + location.getAccuracy());
        int accuracyDelta = (int) (location.getAccuracy() - mLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                mLocation.getProvider());
        Log.e("MtaaLocationService", "Provider is: " + location.getProvider());

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
}
