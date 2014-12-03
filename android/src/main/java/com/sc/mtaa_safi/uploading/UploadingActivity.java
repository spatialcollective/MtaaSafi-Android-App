package com.sc.mtaa_safi.uploading;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

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
        ReportUploadingFragment ruf = (ReportUploadingFragment) getSupportFragmentManager()
                                        .findFragmentByTag(UPLOAD_TAG);
        if(ruf != null && ruf.uploader != null)
            ruf.uploader.cancel(true);
    }
}
