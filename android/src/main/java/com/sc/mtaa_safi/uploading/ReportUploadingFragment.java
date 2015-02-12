package com.sc.mtaa_safi.uploading;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.database.Contract;

public class ReportUploadingFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private UploadingAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ReportUploader uploader;

    public static final int SHOW_CANCEL = 0, SHOW_RETRY = 1, HIDE_CANCEL = -1;
    private int pendingReportCount = -1, 
                mColor = R.color.white,
                mBtnState = SHOW_CANCEL,
                inProgressIndex = 1; // human readable index (starts at 1)
    private String mText = "Uploading...";

    public ReportUploadingFragment() {}

    @Override
    public void onCreate(Bundle instate) {
        super.onCreate(instate);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        super.onCreateView(inflater, container, savedState);
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedState) {
        super.onViewCreated(view, savedState);
        createToolbar(view);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.upload_list);
        // recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new UploadingAdapter(getActivity(), null);
        recyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    private void createToolbar(View view) {
        UploadingActivity act = (UploadingActivity) getActivity();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.upload_toolbar);
        act.setSupportActionBar(toolbar);
        act.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        act.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);
        changeHeader(mText, mColor, mBtnState);

        final ReportUploadingFragment me = this;
        final ImageButton cancelButton = (ImageButton) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tag = (String) cancelButton.getTag();
                if (tag != null) {
                    if (tag.equals("cancel"))
                        cancelSession(ReportUploader.CANCEL_SESSION);
                    else if (tag.equals("restart"))
                        getLoaderManager().restartLoader(0, null, me);
                }
            }
        });
    }

    public void beamUpFirstReport(Cursor c) {
        if ((uploader == null || uploader.isCancelled()) && mAdapter != null && mAdapter.getItemCount() > 0) {
            if (c.moveToFirst()) beamUpReport(new Report(c));
        } else if (mAdapter.getItemCount() == 0 && getView() != null)
            exitSmoothly();
        else if (mAdapter.getItemCount() == 0 && getActivity() != null)
            getActivity().finish();
        else if (mAdapter.getItemCount() == 0)
            setRetainInstance(false);
    }

    private void beamUpReport(Report pendingReport) {
        if (NetworkUtils.isOnline(getActivity()) && getView() != null) {
            changeHeader("Uploading " + inProgressIndex + " of " + pendingReportCount,
                    R.color.white, SHOW_CANCEL);
            uploader = new ReportUploader(getActivity(), pendingReport, this);
            uploader.execute();
        } else if (getView() != null)
            changeHeader("You must be online to upload.", R.color.DarkRed, HIDE_CANCEL);
    }

    private void exitSmoothly() {
        AlphaAnimation anim = new AlphaAnimation(1.0f, 1.0f);
        anim.setDuration(1500);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                getActivity().finish();
            }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        getView().findViewById(R.id.uploadingText).startAnimation(anim);
    }

    public void reportUploadSuccess() {
        changeHeader("Report uploaded successfully!", R.color.white, HIDE_CANCEL);
        uploader = null;
        inProgressIndex++;
        if (mAdapter.getItemCount() == 0  && getView() != null) {
            changeHeader("Successfully uploaded " + pendingReportCount + " reports.",
                    R.color.white, HIDE_CANCEL);
            exitSmoothly();
        }
    }

    public void changeHeader(String message, int color, int btnState) {
        mText = message;
        mColor = color;
        mBtnState = btnState;
        View view = getView();
        if (view == null) return;
        view.findViewById(R.id.uploadingView).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.uploadingText)).setText(message);
        ((TextView) view.findViewById(R.id.uploadingText)).setTextColor(getResources().getColor(color));

        ImageButton cancelBtn = (ImageButton) getView().findViewById(R.id.cancel_button);
        if (btnState == HIDE_CANCEL) {
            cancelBtn.setClickable(false);
            cancelBtn.setAlpha(0f);
        } else if (btnState == SHOW_CANCEL) {
            cancelBtn.setClickable(true);
            cancelBtn.setAlpha(1.0f);
            cancelBtn.setImageResource(R.drawable.ic_cancel);
            cancelBtn.setTag("cancel");
        } else if (btnState == SHOW_RETRY) {
            cancelBtn.setClickable(true);
            cancelBtn.setAlpha(1.0f);
            cancelBtn.setImageResource(R.drawable.ic_upload);
            cancelBtn.setTag("restart");
        }
    }

    public void cancelSession(int reason) {
        changeHeader("Cancelling...", R.color.LightGrey, HIDE_CANCEL);
        if (uploader != null)
            uploader.cancelSession(reason);
        if (reason == ReportUploader.DELETE_BUTTON)
            getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sort = null;
        if (getArguments() != null) {
            String order = getArguments().getString("ORDER");
            if (order != null && order.equals("descending"))
               sort = Contract.Entry.COLUMN_ID + " DESC";
        }
        return new CursorLoader(getActivity(), Contract.Entry.CONTENT_URI,
            Report.PROJECTION, Contract.Entry.COLUMN_PENDINGFLAG + " >= 0 ", null, sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        pendingReportCount = mAdapter.getItemCount() + inProgressIndex - 1;
        boolean shouldAutoStart = uploader == null || uploader.canceller == uploader.DELETE_BUTTON;
        if (pendingReportCount > (inProgressIndex - 1) && shouldAutoStart)
            beamUpFirstReport(cursor);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { mAdapter.swapCursor(null); }
}
