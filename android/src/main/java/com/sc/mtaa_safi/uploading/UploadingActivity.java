package com.sc.mtaa_safi.uploading;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import com.sc.mtaa_safi.MtaaLocationService;
import com.sc.mtaa_safi.R;

public class UploadingActivity extends ActionBarActivity {

    final static String UPLOAD_TAG = "upload", ACTION = "action", DATA = "data";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);

        ReportUploadingFragment frag = null;
        if (savedInstanceState != null)
            frag = (ReportUploadingFragment) getSupportFragmentManager().getFragment(savedInstanceState, UPLOAD_TAG);
        if (frag == null){
            frag = new ReportUploadingFragment();
            Bundle args = new Bundle();
            if(getIntent().getData() != null ){
                args.putString("ORDER", "descending");
                frag.setArguments(args);
            }
        }
        getSupportFragmentManager().beginTransaction()
            .replace(android.R.id.content, frag, UPLOAD_TAG)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit();
    }

    private MtaaLocationService mBoundService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((MtaaLocationService.LocalBinder)service).getService();
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
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        ReportUploadingFragment frag = (ReportUploadingFragment) getSupportFragmentManager().findFragmentByTag(UPLOAD_TAG);
        if (frag != null)
            getSupportFragmentManager().putFragment(bundle, UPLOAD_TAG, frag);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    @Override
    public void finish() {
        super.finish();
        ReportUploadingFragment ruf = (ReportUploadingFragment) getSupportFragmentManager()
                                        .findFragmentByTag(UPLOAD_TAG);
        if (ruf != null && ruf.uploader != null)
            ruf.uploader.cancel(true);
    }
}
