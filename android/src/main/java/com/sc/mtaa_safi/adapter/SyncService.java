package com.sc.mtaa_safi.adapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

 /*For performance, only one sync adapter will be initialized within this application's context.
 * Note: The SyncService itself is not notified when a new sync occurs. It's role is to
 * manage the lifecycle of our {@link SyncAdapter} and provide a handle to said SyncAdapter to the
 * OS on request. */
 
public class SyncService extends Service {
    private static final String TAG = "SyncService";

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null)
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
