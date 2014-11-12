package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.ReportContract;

public class ReportUploadingFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    ReportUploader uploader;
    SimpleUploadingCursorAdapter mAdapter;
    private int pendingReportCount, mColor, currentReport;
    private String mText;
    private boolean userCancelled;

    public final static String ACTION = "action", DATA = "data";
    public final static char ACTION_SEND_NEW = 'n',
                    ACTION_SEND_ALL = 'a',
                    ACTION_VIEW = 'v';

    public String[] LIST_FROM_COLUMNS = new String[] {
        ReportContract.Entry.COLUMN_DETAILS,
        ReportContract.Entry.COLUMN_TIMESTAMP,
        ReportContract.Entry.COLUMN_PENDINGFLAG,
        ReportContract.Entry.COLUMN_ID
    };
    private static final int[] LIST_TO_FIELDS = new int[] {
        R.id.itemDetails,
        R.id.timeElapsed,
        R.id.expanded_layout,
        R.id.deleteReportButton
    };
    public ReportUploadingFragment() {}

    @Override
    public void onCreate(Bundle instate){
        super.onCreate(instate);
        setRetainInstance(true);
        pendingReportCount = -1;
        mColor = R.color.mtaa_safi_blue;
        mText = "Uploading...";
        userCancelled = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        super.onCreateView(inflater, container, savedState);
        return inflater.inflate(R.layout.upload_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedState) {
        super.onViewCreated(view, savedState);
        mAdapter = new SimpleUploadingCursorAdapter(getActivity(), R.layout.upload_item,
                null, LIST_FROM_COLUMNS, LIST_TO_FIELDS, 0);
        mAdapter.setViewBinder(new ViewBinder());
        setListAdapter(mAdapter);
        changeHeaderMessage(mText, mColor);
        getLoaderManager().initLoader(0, null, this);
        final ImageButton cancelButton = (ImageButton) view.findViewById(R.id.cancel_button);
        if(cancelButton.getTag() == null){
            cancelButton.setTag("cancel");
        }
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tag = (String) cancelButton.getTag();
                if(tag != null){
                    if(tag.equals("cancel"))
                        cancelSession(view);
                    else if(tag.equals("restart"))
                        restartSession();
                }
            }
        });
    }

    private void cancelSession(View view){
        // tell the adapter to tell the uploader to stop. Once it stops, update the view
        userCancelled = true;
        changeHeaderMessage("Cancelling...", R.color.DarkGray);
        Log.e("cancel session", "cancelling!!");
        view.setAlpha(.5f);
        view.setClickable(false);
        if(uploader != null)
            uploader.cancelSession();
        else
            onSessionCancelled();
    }

    public void onSessionCancelled(){
        changeHeaderMessage("Upload Cancelled", R.color.Crimson);
        ImageButton startStop = (ImageButton) getView().findViewById(R.id.cancel_button);
        startStop.setClickable(true);
        startStop.setAlpha(1.0f);
        startStop.setImageResource(R.drawable.restart_upload_button);
        startStop.setTag("restart");
    }

    private void restartSession(){
        if(mAdapter.getCount() > 1)
            beamUpFirstReport();
        else
            beamUpReport(new Report(mAdapter.getCursor()));
        ImageButton startStop = (ImageButton) getView().findViewById(R.id.cancel_button);
        startStop.setImageResource(R.drawable.cancel_upload_button);
        startStop.setTag("cancel");
    }

    public class ViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int i) {
            if (i == cursor.getColumnIndex(ReportContract.Entry.COLUMN_TIMESTAMP))
                ((TextView) view).setText(Report.getElapsedTime(cursor.getString(i)));
            else if (i == cursor.getColumnIndex(ReportContract.Entry.COLUMN_PENDINGFLAG))
                mAdapter.updateProgressView(cursor.getInt(i), view);
            else if (i == cursor.getColumnIndex(ReportContract.Entry.COLUMN_UPLOAD_IN_PROGRESS))
                mAdapter.indicateRow(cursor.getInt(i), view);
            else if (i == cursor.getColumnIndex(ReportContract.Entry.COLUMN_ID)){
                    view.setTag(cursor.getInt(i));
                if(cursor.getCount() < 2)
                    view.setVisibility(View.INVISIBLE);
            } else
                return false;
            return true;
        }
    }

    private void beamUpFirstReport() {
        if ((uploader == null || uploader.isCancelled()) && mAdapter != null && mAdapter.getCount() > 0)
            beamUpReport(new Report((Cursor) mAdapter.getItem(0)));
        else if (mAdapter.getCount() == 0){
            onUploadsFinished();
            getView().findViewById(R.id.cancel_button).setVisibility(View.INVISIBLE);
        }
    }

    private void beamUpReport(Report pendingReport) {
        userCancelled = false;
        Log.e("RUF", "Beam up report has been called!");
        if (!isOnline() && getView() != null) {
            reportFailure();
            return;
        }
        if (getView() != null)
            changeHeaderMessage("Uploading (" + currentReport + "/" + pendingReportCount + ")",
                    R.color.mtaa_safi_blue);
        uploader = new ReportUploader(getActivity(), pendingReport, this);
        uploader.execute();
    }

    private void onUploadsFinished(){
        changeHeaderMessage("Poa! Upload Success!", R.color.Coral);
        AlphaAnimation anim = new AlphaAnimation(1.0f, 1.0f);
        anim.setDuration(1200);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                getActivity().finish();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        getView().findViewById(R.id.uploadingText).startAnimation(anim);
    }
    public void reportFailure() {
        changeHeaderMessage("You must be online to upload.", R.color.DarkRed);
    }

    public void reportUploadSuccess() {
        changeHeaderMessage("Report uploaded successfully!", R.color.mtaa_safi_blue);
        uploader = null;
        currentReport++;
        if (mAdapter.getCount() > 0)
            beamUpFirstReport();
        else if (getView() != null)
            changeHeaderMessage("Successfully uploaded " + pendingReportCount + " reports.",
                    R.color.mtaa_safi_blue);
    }

    private void changeHeaderMessage(String message, int color) {
        mText = message;
        mColor = color;
        View view = getView();
        if (view == null) return;
        view.findViewById(R.id.uploadingView).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.uploadingText)).setText(message);
        ((TextView) view.findViewById(R.id.uploadingText)).setTextColor(getResources().getColor(color));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), ReportContract.Entry.CONTENT_URI,
            Report.PROJECTION, ReportContract.Entry.COLUMN_PENDINGFLAG + " >= 0 ", null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
        if (pendingReportCount == -1){
            pendingReportCount = mAdapter.getCount();
            currentReport = 1;
        }
        if(!userCancelled)
            beamUpFirstReport();
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { mAdapter.changeCursor(null); }

    public boolean isOnline() {
        NetworkInfo netInfo = ((ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE))
                                    .getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
            return true;
        return false;
    }

    //    private void chooseAction(Bundle args) {
//        if (args != null) {
//            switch(args.getChar(ACTION)) {
//                case ACTION_SEND_NEW:
//                    beamUpReport(Uri.parse(args.getString(DATA)));
//                case ACTION_SEND_ALL:
//                    beamUpPendingReports();
//                    break;
//                case ACTION_VIEW:
//                    viewSuccessful();
//            }
//        }
//    }
    
//     private void setListeners(View view) {
//         view.findViewById(R.id.resendButton).setOnClickListener(new View.OnClickListener(){
//             @Override
//             public void onClick(View view){
//                 Log.e("Resend btn", "Resend called");
//                 uploadingTV.setText("Resending...");
//                 uploadingTV.setTextColor(getResources().getColor(R.color.White));
//                 updatePostProgress(progress, pendingReport);
//                 beamUpReport(pendingReport);
//             }
//         });
//         view.findViewById(R.id.abandonReport).setOnClickListener(new View.OnClickListener(){
//             @Override
//             public void onClick(View view){
//                 uploadingTV.setText("Abandoning report...");
//                 uploadingTV.setTextColor(getResources().getColor(R.color.DarkRed));
//                 // TODO: delete report from DB if it's there
//                 mActivity.deleteReport(pendingReport);
//                 getActivity().finish();
//             }
//         });
//         view.findViewById(R.id.sendLaterButton).setOnClickListener(new View.OnClickListener() {
//             @Override
//             public void onClick (View view){
//                 uploadingTV.setText("Saving report...");
//                 uploadingTV.setTextColor(getResources().getColor(R.color.White));
//                 // if the pendingReport was a saved one, remove the old version before saving the new one
//                 mActivity.saveReport(pendingReport, progress);
//                 Toast.makeText(mActivity, "Report saved for later!", Toast.LENGTH_SHORT).show();
//                 mActivity.finish();
//             }
//         });
//         view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
//             @Override
//             public void onClick(View view) {
//                 cancelReport();
//             }
//         });
//     }
}
