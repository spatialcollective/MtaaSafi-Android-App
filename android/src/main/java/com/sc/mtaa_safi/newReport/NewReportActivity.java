package com.sc.mtaa_safi.newReport;

import android.content.Intent;
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
import com.sc.mtaa_safi.uploading.UploadingActivity;

import org.json.JSONException;
import org.json.JSONObject;

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
        JSONObject locationJSON = new JSONObject();
        Report newReport;
        if (frag.adminText == frag.selectedAdmin.trim()) {
            locationJSON.put("admin", frag.selectedAdmin.trim());
            locationJSON.put("adminId", frag.selectedAdminId);
        } else
            locationJSON.put("admin", frag.adminText);
        if (parentReportId != 0)
            newReport = new Report(frag.detailsText, frag.status, Utils.getUserName(this), Utils.getUserId(this), getLocation(), frag.picPaths, locationJSON.toString(), parentReportId);
        else
            newReport = new Report(frag.detailsText, frag.status, Utils.getUserName(this), Utils.getUserId(this), getLocation(), frag.picPaths, locationJSON.toString());
        return newReport.save(this);
    }

    public void takePic(View view) {
        ((NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG)).takePicture();
        /*Intent intent = new Intent();
        intent.setClass(this,ImageCaptureActivity.class);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);*/
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
