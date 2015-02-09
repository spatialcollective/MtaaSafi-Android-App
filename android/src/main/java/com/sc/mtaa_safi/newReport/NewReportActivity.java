package com.sc.mtaa_safi.newReport;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import com.sc.mtaa_safi.MtaaLocationService;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.ComplexPreferences;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.uploading.UploadingActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class NewReportActivity extends ActionBarActivity {
    private ComplexPreferences cp;
    public final static String NEW_REPORT_TAG = "newreport";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreFragment(savedInstanceState);
        cp = PrefUtils.getPrefs(this);
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

    private MtaaLocationService mBoundService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((MtaaLocationService.LocalBinder) service).getService();
//            Toast.makeText(this, "Location Service Enabled", Toast.LENGTH_SHORT).show();
        }
        public void onServiceDisconnected(ComponentName className) { mBoundService = null; } // This should never happen
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
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    public Location getLocation() {
        return mBoundService.getLocation();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        NewReportFragment frag = (NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG);
        if (frag != null) // are we sure this isn't holding the fragment longer than necessary?
            getSupportFragmentManager().putFragment(bundle, NEW_REPORT_TAG, frag);
    }

    public void attemptSave(View view) {
        Log.e("New Report Activity", "attempting save");
        try {
            if (mBoundService.hasLocation()) {
                saveNewReport((NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG));
                finish();
            }
        } catch (Exception e) {}
    }

    public void attemptBeamOut(View view) {
        try {
            if (mBoundService.hasLocation()) {
                NewReportFragment nrf = (NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG);
                Uri newReportUri = saveNewReport(nrf);
                Intent intent = new Intent();
                intent.setClass(this, UploadingActivity.class);
                intent.setData(newReportUri);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {}
    }

    public Uri saveNewReport(NewReportFragment frag) throws JSONException {
        JSONObject locationJSON = new JSONObject();
        if (frag.adminText == frag.selectedAdmin.trim()) {
            locationJSON.put("admin", frag.selectedAdmin.trim());
            locationJSON.put("adminId", frag.selectedAdminId);
        } else
            locationJSON.put("admin", frag.adminText);

        Report newReport = new Report(frag.detailsText, cp.getString(PrefUtils.USERNAME, ""), getLocation(), frag.picPaths, locationJSON.toString());
        Log.e("New Report Activity", "inserting");
        return getContentResolver().insert(Contract.Entry.CONTENT_URI, newReport.getContentValues());
    }

    public void takePic(View view) {
        ((NewReportFragment) getSupportFragmentManager().findFragmentByTag(NEW_REPORT_TAG)).takePicture();
    }
}
