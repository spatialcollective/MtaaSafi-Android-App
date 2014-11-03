package com.sc.mtaasafi.android.uploading;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.database.ReportContract;

public class UploadingActivity extends ActionBarActivity {

    private ReportUploader uploader;
    final static String UPLOAD_TAG = "upload", ACTION = "action", DATA = "data";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ReportUploadingFragment frag = null;
        if (savedInstanceState != null)
            frag = (ReportUploadingFragment) getSupportFragmentManager().getFragment(savedInstanceState, UPLOAD_TAG);
        if (frag == null)
            frag = new ReportUploadingFragment();
        Bundle args = new Bundle();
//        args.putString(ReportUploadingFragment.ACTION, getIntent().getAction());
//        args.putString(ReportUploadingFragment.DATA, getIntent().getData().toString());
//        frag.setArguments(args);

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
        Log.e("NRA.Finish", "Finish called! Cancelling uploading: " + (uploader != null));
        if (uploader != null)
            uploader.cancel(true);
    }

    private void beamUpReport(Uri pendingReport) {
       if (isOnline()) {

       }
    }

    public boolean isOnline() {
        NetworkInfo netInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
                                    .getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
            return true;
        return false;
    }

//    public void uploadSavedReports(View view) {
//        if (isOnline()) {
//            FragmentManager manager = getSupportFragmentManager();
//            ReportUploadingFragment uploadingFragment = new ReportUploadingFragment();
//            Bundle bundle = new Bundle();
//            bundle.putBoolean(UPLOAD_SAVED_REPORTS_KEY, true);
//            uploadingFragment.setArguments(bundle);
//            manager.beginTransaction()
//                    .replace(android.R.id.content, uploadingFragment, UPLOAD_TAG)
//                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                    .commit();
//        }
//    }

//    public ArrayList<Report> getSavedReports() {
//        Cursor c = getContentResolver().query(
//                ReportContract.Entry.CONTENT_URI,
//                Report.PROJECTION,
//                ReportContract.Entry.COLUMN_PENDINGFLAG + " > 0 ", null, null); // Get all pending
//        ArrayList<Report> savedReports = new ArrayList<Report>();
//        while (c.moveToNext())
//            savedReports.add(new Report(c));
//        c.close();
//        return savedReports;
//    }

    // called by the UploadingFragment on progress update if it's uploading a saved report
    // updates the pending flag value and overwrites the corresponding value with the output from the server
    // Once the report is fully uploaded, the local DB treats it like a post it retrieved from the server
//    public void uploadProgress(int progress, Report report){
//        if(getIntent().getBooleanExtra(UPLOAD_SAVED_REPORTS_KEY, false))
//            updateReportData(progress, report);
//    }
//
//    public void onAlertButtonPressed(int eventKey){
//        ReportUploadingFragment ruf =
//                (ReportUploadingFragment) getSupportFragmentManager().findFragmentByTag(UPLOAD_TAG);
//        if(ruf != null){
//            switch(eventKey){
//                case AlertDialogFragment.ABANDON_REPORTS:
//                    deleteReport(ruf.pendingReport);
//                    break;
//                case AlertDialogFragment.SAVE_REPORTS:
//                    saveReport(ruf.pendingReport, ruf.progress);
//                    break;
//            }
//        }
//        finish();
//    }

}
