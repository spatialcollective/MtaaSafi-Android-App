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
import com.sc.mtaa_safi.common.BaseActivity;

public class UploadingActivity extends BaseActivity {

    final static String UPLOAD_TAG = "upload";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ReportUploadingFragment frag = null;
        if (savedInstanceState != null)
            frag = (ReportUploadingFragment) getSupportFragmentManager().getFragment(savedInstanceState, UPLOAD_TAG);
        if (frag == null){
            frag = new ReportUploadingFragment();
            Bundle args = new Bundle();
            if (getIntent().getData() != null ) {
                args.putString("ORDER", "descending");
                frag.setArguments(args);
            }
        }
        getSupportFragmentManager().beginTransaction()
            .replace(android.R.id.content, frag, UPLOAD_TAG)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        ReportUploadingFragment frag = (ReportUploadingFragment) getSupportFragmentManager().findFragmentByTag(UPLOAD_TAG);
        if (frag != null)
            getSupportFragmentManager().putFragment(bundle, UPLOAD_TAG, frag);
    }

    @Override
    public void finish() {
        super.finish();
        ReportUploadingFragment ruf = (ReportUploadingFragment) getSupportFragmentManager().findFragmentByTag(UPLOAD_TAG);
        if (ruf != null && ruf.uploader != null)
            ruf.uploader.cancel(true);
    }
}
