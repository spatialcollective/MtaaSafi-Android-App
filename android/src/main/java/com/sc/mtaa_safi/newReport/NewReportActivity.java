package com.sc.mtaa_safi.newReport;

import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.common.BaseActivity;
import com.sc.mtaa_safi.feed.tags.ReportTagJunction;
import com.sc.mtaa_safi.feed.tags.Tag;
import com.sc.mtaa_safi.uploading.UploadingActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

public class NewReportActivity extends BaseActivity {
    public final static String NEW_REPORT_TAG = "newreport";
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    private int parentReportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        parentReportId = intent.getIntExtra("parentReportId", 0);
        if (intent.hasExtra("title"))
            setTitle(intent.getStringExtra("title"));
        restoreFragment(savedInstanceState);
    }

    private void restoreFragment(Bundle savedInstanceState){
        FragmentManager manager = getSupportFragmentManager();
        NewReportFragment frag = null;
        if (savedInstanceState != null)
            frag = (NewReportFragment) getSupportFragmentManager().getFragment(savedInstanceState, NEW_REPORT_TAG);
        if (frag == null)
            frag = new NewReportFragment();
        manager.beginTransaction()
                .replace(android.R.id.content, frag, NEW_REPORT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        NewReportFragment frag = (NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG);
        if (frag != null) // are we sure this isn't holding the fragment longer than necessary?
            getSupportFragmentManager().putFragment(bundle, NEW_REPORT_TAG, frag);
    }

    public void attemptSave(View view) {
        try {
            if (mBoundService.hasLocation(this)) {
                saveNewReport((NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG));
                finish();
            } else {
                Toast.makeText(this, "Still retrieving location...", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {}
    }

    public void attemptBeamOut(View view) {
        try {
            if (mBoundService.hasLocation(this)) {
                NewReportFragment nrf = (NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG);
                Uri newReportUri = saveNewReport(nrf);
                Intent intent = new Intent();
                intent.setClass(this, UploadingActivity.class);
                intent.setData(newReportUri);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Still retrieving location...", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {}
    }

    public Uri saveNewReport(NewReportFragment frag) throws JSONException {
        Report newReport;
        if (parentReportId != 0)
            newReport = new Report(frag.detailsText, frag.status, Utils.getUserName(this), Utils.getUserId(this), getLocation(), frag.picPaths, parentReportId);
        else
            newReport = new Report(frag.detailsText, frag.status, Utils.getUserName(this), Utils.getUserId(this), getLocation(), frag.picPaths);

        Uri reportUri = newReport.save(this, true);

        if (frag.tagsCompletionView.getObjects().size() != 0)
            saveTags(frag.tagsCompletionView.getObjects(), Integer.valueOf(reportUri.getLastPathSegment()));

        return reportUri;
    }

    public void saveTags(List tags, int reportId){
        for (int i = 0; i < tags.size() ; i++) {
            Tag tag = (Tag) tags.get(i);
            if (tag.getServerId() == 0) {
                try {
                    Uri tagUri = tag.save(this);
                    ReportTagJunction.save(this, reportId, Integer.valueOf(tagUri.getLastPathSegment()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ReportTagJunction.save(this, reportId, tag.getId());
            }
        }
    }

    public void takePic(View view) {
        ((NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG)).takePicture();
    }

    public void setStatus(View view) {
        NewReportFragment frag = (NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG);
        if (((RadioButton) view).isChecked())
            switch(view.getId()) {
                case R.id.progress:
                    frag.status = 1;
                    break;
                case R.id.fixed:
                    frag.status = 2;
                    break;
                case R.id.broken:
                default:
                    frag.status = 0;
            }
    }
}
