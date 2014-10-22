package com.sc.mtaasafi.android;

import android.accounts.AccountManager;
import android.app.Activity;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
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
import com.sc.mtaasafi.android.adapter.FragmentAdapter;

import io.fabric.sdk.android.Fabric;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class MainActivity extends ActionBarActivity implements
        NewsFeedFragment.ReportSelectedListener {

    private SharedPreferences sharedPref;
    public String mUsername;

    NewsFeedFragment newsfeedFragment;

    static final String USERNAME_KEY = "username",
                        HAS_REPORT_DETAIL_KEY = "report_detail",
                        REPORT_DETAIL_KEY = "report_detail";

                        // onActivityResult
    static final int    REQUEST_CODE_PICK_ACCOUNT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics());
        Log.e(LogTags.MAIN_ACTIVITY, "onCreate");
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        sharedPref = getPreferences(Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);
        newsfeedFragment = (NewsFeedFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putString(USERNAME_KEY, mUsername);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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
//        mPager.setCurrentItem(mPager.getCurrentItem());
    }
    @Override
    protected void onPause(){
        super.onResume();
    }
    @Override
    protected void onStop(){
        // Disconnecting the client invalidates it.
        Log.e(LogTags.MAIN_ACTIVITY, "onStop");
        super.onStop();
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

    public void launchAlert() {
        AlertDialogFragment adf = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(AlertDialogFragment.ALERT_KEY, AlertDialogFragment.UPDATE_FAILED);
        adf.setArguments(bundle);
        adf.show(getSupportFragmentManager(), AlertDialogFragment.ALERT_KEY);
    }

    public void goToDetailView(Report report, int position) {
        // Bundle args = new Bundle();
        // args = report.saveState("some_key", args);
        // ReportDetailFragment reportFrag = new ReportDetailFragment();
        // reportFrag.setArguments(args);

        // getSupportFragmentManager()
        //     .beginTransaction()
        //     .replace(R.id.fragment_container, reportFrag, "reportDetailView")
        //     .addToBackStack(null)
        //     .commit();

        Intent intent = new Intent();
        intent.setClass(this, ReportDetailActivity.class);
        intent.putExtra("index", position);
        startActivity(intent);
    }

    public void goToNewReport(){
        Intent intent = new Intent();
        intent.setClass(this, NewReportActivity.class);
        startActivity(intent);
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
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }
}
