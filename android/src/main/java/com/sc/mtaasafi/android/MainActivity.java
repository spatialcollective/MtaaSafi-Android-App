package com.sc.mtaasafi.android;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.AccountPicker;

import io.fabric.sdk.android.Fabric;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class MainActivity extends ActionBarActivity implements
        NewsFeedFragment.ReportSelectedListener, AlertDialogFragment.AlertDialogListener {

    private SharedPreferences sharedPref;
    private Report reportDetailReport;
    public String mUsername;
    NewsFeedFragment newsfeedFragment;

    public static final String USERNAME_KEY = "username",
                        HAS_REPORT_DETAIL_KEY = "report_detail",
                        REPORT_DETAIL_KEY = "report_detail",
                        FEED_FRAG_TAG = "feed",
                        DETAIl_FRAG_TAG = "detail";

                        // onActivityResult
    static final int    REQUEST_CODE_PICK_ACCOUNT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Log.e(LogTags.MAIN_ACTIVITY, "onCreate");
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        goToFeed();

    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putString(USERNAME_KEY, mUsername);
        if (reportDetailReport != null)
            reportDetailReport.saveState(REPORT_DETAIL_KEY, bundle);
        bundle.putBoolean(HAS_REPORT_DETAIL_KEY, reportDetailReport != null);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUsername = savedInstanceState.getString(USERNAME_KEY);
        if (mUsername == null || mUsername.equals(""))
            determineUsername();
        // mPager.setCurrentItem(savedInstanceState.getInt(CURRENT_FRAGMENT_KEY));
        if (savedInstanceState.getBoolean(HAS_REPORT_DETAIL_KEY))
            reportDetailReport = new Report(REPORT_DETAIL_KEY, savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LogTags.MAIN_ACTIVITY, "onStart");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e(LogTags.MAIN_ACTIVITY, "onResume");
        determineUsername();
//        Intent intent = getIntent();
        // If activity wasn't launched after a saveReport event & user has saved reports pending,
        // remind them
//        if(intent == null || intent.getBooleanExtra(NewReportActivity.SAVED_REPORTS_KEY, false)){
//            ComplexPreferences cp = ComplexPreferences.getComplexPreferences(this, NewReportActivity.PREF_KEY, MODE_PRIVATE);
//            List<String> savedReports = cp.getObject(NewReportActivity.SAVED_REPORTS_KEY, List.class);
//            if(savedReports != null && !savedReports.isEmpty()) {
//                launchAlert(AlertDialogFragment.SAVED_REPORTS);
//            }
//        }
    }
    @Override
    protected void onStop(){
        // Disconnecting the client invalidates it.
        Log.e(LogTags.MAIN_ACTIVITY, "onStop");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        goToFeed();
    }

    public int getScreenWidth(){
        return getWindowManager().getDefaultDisplay().getWidth();
    }

    public void backupDataToFile(String dataString) throws IOException {
        FileOutputStream outputStream = openFileOutput("serverBackup.json", Context.MODE_PRIVATE);
        outputStream.write(dataString.getBytes());
        outputStream.close();
    }

    public String getJsonStringFromFile() throws IOException {
        FileInputStream jsonFileStream = openFileInput("serverBackup.json");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(jsonFileStream));
        StringBuilder jsonString = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
            jsonString.append(line);
        return jsonString.toString();
    }

    public void launchAlert(int alertCode) {
        AlertDialogFragment adf = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(AlertDialogFragment.ALERT_KEY, alertCode);
        adf.setArguments(bundle);
        adf.setAlertDialogListener(this);
        adf.show(getSupportFragmentManager(), AlertDialogFragment.ALERT_KEY);
    }

    @Override
    public void onAlertButtonPressed(String eventKey) {
        if(eventKey == AlertDialogFragment.RE_FETCH_FEED){

        } else if(eventKey == AlertDialogFragment.SEND_SAVED_REPORTS){
            Intent intent = new Intent().setClass(this, NewReportActivity.class)
                                        .putExtra(NewReportActivity.UPLOAD_SAVED_REPORTS_KEY, true)
                                        .putExtra(USERNAME_KEY, mUsername);
            startActivity(intent);
        }

    }

    // ======================Fragment Navigation:======================
    public void goToFeed(){
        FragmentManager manager = getSupportFragmentManager();
        newsfeedFragment = new NewsFeedFragment();
        manager.beginTransaction()
                .replace(R.id.fragment_container, newsfeedFragment, FEED_FRAG_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    public void goToDetailView(Report report){
        reportDetailReport = report;
        FragmentManager manager = getSupportFragmentManager();
        ReportDetailFragment rdf = new ReportDetailFragment();
        Bundle args = new Bundle();
        reportDetailReport.saveState(REPORT_DETAIL_KEY, args);
        rdf.setArguments(args);
        manager.beginTransaction()
                .replace(R.id.fragment_container, rdf)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

//        mPager.setCurrentItem(FRAGMENT_REPORTDETAIL);
        Log.e("GO TO DETAIL VIEW", report.title);
    }
    public void getReportDetailReport(ReportDetailFragment rdf){
        if (reportDetailReport != null)
            rdf.updateView(reportDetailReport);
    }

    public void goToNewReport(){
        Intent intent = new Intent();
        intent.setClass(this, NewReportActivity.class);
        intent.putExtra(USERNAME_KEY, mUsername);
        // intent.putExtra("index", index);
        startActivity(intent);
    }

    public void refreshFeed(){
        goToFeed();
        FragmentManager manager = getSupportFragmentManager();
        NewsFeedFragment nff = (NewsFeedFragment) manager.findFragmentByTag(FEED_FRAG_TAG);
        nff.refreshFeed();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED && requestCode == REQUEST_CODE_PICK_ACCOUNT)
            Toast.makeText(this, "You must pick an account to proceed", Toast.LENGTH_SHORT).show();
        else if (requestCode == REQUEST_CODE_PICK_ACCOUNT)
            setUserName(data);
    }

    private void setUserName(Intent data) {
        String retrievedUserName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        mUsername = retrievedUserName;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USERNAME_KEY, retrievedUserName);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu items for use in the action bar
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.action_bar, menu);
         return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id._action_report:
                goToNewReport();
                return true;
            case R.id.action_refresh:
                refreshFeed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public int getActionBarHeight(){
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) 
            return TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        return 0;
    }

    private void determineUsername() {
        if (mUsername == null || mUsername.equals("")) {
            String savedUserName = sharedPref.getString(USERNAME_KEY, "");
            if (savedUserName.equals(""))
                pickUserAccount();
            else
                mUsername = savedUserName;
        }
        Toast.makeText(this, "UserName:" + mUsername, Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USERNAME_KEY, mUsername);
        editor.commit();
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }
}
