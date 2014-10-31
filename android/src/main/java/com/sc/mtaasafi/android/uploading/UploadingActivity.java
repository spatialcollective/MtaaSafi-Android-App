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
    protected void onStop() {
//        updateDbWithProgress();
        super.onStop();
    }

    @Override
    public void finish() {
        super.finish();
        Log.e("NRA.Finish", "Finish called! Cancelling uploading: " + (uploader != null));
        if (uploader != null)
            uploader.cancel(true);
    }

    public void setUploader(ReportUploader uploader) {
        this.uploader = uploader;
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

    // Check if the report is currently in the database
    // public void saveReport(Report report, int progress){
    //     if(progress == 0)
    //         progress = 1;
    //     if(progress > 3) // assume progress > 3 means it uploaded successfully
    //         progress = 0;
    //     if(!dbContains(report)){ // add report to DB if it's not there
    //             commitCPO(ContentProviderOperation.newInsert(ReportContract.Entry.CONTENT_URI)
    //                     .withValue(ReportContract.Entry.COLUMN_SERVER_ID, 0)
    //                     .withValue(ReportContract.Entry.COLUMN_TITLE, "")
    //                     .withValue(ReportContract.Entry.COLUMN_DETAILS, report.details)
    //                     .withValue(ReportContract.Entry.COLUMN_TIMESTAMP, report.timeStamp)
    //                     .withValue(ReportContract.Entry.COLUMN_LAT, Double.toString(report.latitude))
    //                     .withValue(ReportContract.Entry.COLUMN_LNG, Double.toString(report.longitude))
    //                     .withValue(ReportContract.Entry.COLUMN_USERNAME, cp.getString(PrefUtils.USERNAME, ""))
    //                     .withValue(ReportContract.Entry.COLUMN_MEDIAURL1, report.mediaPaths.get(0))
    //                     .withValue(ReportContract.Entry.COLUMN_MEDIAURL2, report.mediaPaths.get(1))
    //                     .withValue(ReportContract.Entry.COLUMN_MEDIAURL3, report.mediaPaths.get(2))
    //                     .withValue(ReportContract.Entry.COLUMN_PENDINGFLAG, progress)
    //                     .build());
    //         } else{ // if it is, current report is the one this cpoBuilder is about (ASSUMED--NOT FULLY CONFIRMED)
    //             Log.e("SAVE REPORT", "Report was already in the database, to be updated not saved");
    //             updateDb();
    //         }
//            String[] selectionarg = new String[1];
//            selectionarg[0] = "0";
//            Cursor c1 = getContentResolver().
//                    query(ReportContract.Entry.CONTENT_URI,
//                            Report.PROJECTION,
//                            ReportContract.Entry.COLUMN_PENDINGFLAG + " > ? ", selectionarg, null);
////            while(c1.moveToNext()){
////                Log.e("DATABASE", "Saved report: " + c1.getString(c1.getColumnIndex(ReportContract.Entry.COLUMN_DETAILS)));
////            }
//            c1.close();
    // }

//    private void updateReportData(int progress, Report report){
//        if(cpoBuilder == null){
//            cpoBuilder = ContentProviderOperation.newUpdate(uriFor(report));
//        }
//        this.progress = progress;
//        switch(progress){
//            case -1:
//                cpoBuilder.withValue(ReportContract.Entry.COLUMN_MEDIAURL1, report.mediaPaths.get(2));
//                Log.e("Activity CPOBuilder", "Server output I got: " + report.mediaPaths.get(2));
//                updateDb();
//                break;
//            case 3:
//                cpoBuilder.withValue(ReportContract.Entry.COLUMN_MEDIAURL1, report.mediaPaths.get(1));
//                Log.e("Activity CPOBuilder", "Server output I got: " + report.mediaPaths.get(1));
//            case 2:
//                cpoBuilder.withValue(ReportContract.Entry.COLUMN_MEDIAURL1, report.mediaPaths.get(0));
//                Log.e("Activity CPOBuilder", "Server output I got: "+ report.mediaPaths.get(0));
//            case 1:
//                cpoBuilder.withValue(ReportContract.Entry.COLUMN_SERVER_ID, report.serverId);
//                cpoBuilder.withValue(ReportContract.Entry.COLUMN_TITLE, report.title);
//                Log.e("Activity CPOBuilder", "Server output I got: "+ report.title + " also server id: " + report.serverId);
//        }
//    }



        // called after a report has uploaded successfully or if the activity stops
    // private void updateDb(){
    //     if(cpoBuilder != null){
    //         Log.e("UPDATE DB", " Hey buddy! Update db was called. Proud of you");
    //         if(progress != -1)
    //             cpoBuilder.withValue(ReportContract.Entry.COLUMN_PENDINGFLAG, progress+1);
    //         else
    //             cpoBuilder.withValue(ReportContract.Entry.COLUMN_PENDINGFLAG, 0);
    //         commitCPO(cpoBuilder.build());
    //         cpoBuilder = null;
    //     }
    // }

    // public void deleteReport(Report pendingReport){
    //     if(dbContains(pendingReport))
    //         commitCPO(ContentProviderOperation.newDelete(uriFor(pendingReport)).build());
    // }

    // private Uri uriFor(Report report){
    //     return ReportContract.Entry.CONTENT_URI.buildUpon()
    //             .appendPath(Integer.toString(report.dbId)).build();
    // }

    // private boolean dbContains(Report report){
    //     String[] projection = new String[1];
    //     projection[0] = ReportContract.Entry.COLUMN_ID;
    //     Cursor c = getContentResolver().
    //             query(ReportContract.Entry.CONTENT_URI,
    //                     projection,
    //                     ReportContract.Entry.COLUMN_TIMESTAMP + " = " +'\"' + report.timeStamp + '\"', null, null);
    //     int instanceCt = c.getCount();
    //     c.close();
    //     return instanceCt > 0;
    // }
    
    // private void commitCPO(ContentProviderOperation cpo){
    //     ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
    //     batch.add(cpo);
    //     commitBatch(batch);
    // }

    // private void commitBatch(ArrayList<ContentProviderOperation> batch){
    //     try {
    //         getContentResolver().applyBatch(ReportContract.CONTENT_AUTHORITY, batch);
    //         getContentResolver().notifyChange(ReportContract.Entry.CONTENT_URI, null, false);
    //     } catch (RemoteException e) {
    //         e.printStackTrace();
    //     } catch (OperationApplicationException e) {
    //         e.printStackTrace();
    //     }
    // }
}
