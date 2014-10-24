package com.sc.mtaasafi.android;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sc.mtaasafi.android.NewReport.NewReportActivity;

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
    public String mUsername;
    ReportDetailFragment mFragment;

    public static final String USERNAME_KEY = "username";

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
        if (savedInstanceState != null)
            mFragment = (ReportDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mFragment");
        if (mFragment == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new NewsFeedFragment())
                .commit();
        }
        ComplexPreferences cp = ComplexPreferences.getComplexPreferences(this, NewReportActivity.PREF_KEY, MODE_PRIVATE);
        List<String> savedReports = cp.getObject(NewReportActivity.SAVED_REPORTS_KEY, List.class);
        if (savedReports != null && !savedReports.isEmpty())
            launchAlert(AlertDialogFragment.SAVED_REPORTS);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putString(USERNAME_KEY, mUsername);
        if (mFragment != null && mFragment.isAdded())
            getSupportFragmentManager().putFragment(bundle, "mFragment", mFragment);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUsername = savedInstanceState.getString(USERNAME_KEY);
        if (mUsername == null || mUsername.equals(""))
            determineUsername();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LogTags.MAIN_ACTIVITY, "onStart");
        int gPlayCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        switch(gPlayCode){
            case ConnectionResult.SERVICE_MISSING:
                launchAlert(AlertDialogFragment.GPLAY_MISSING);
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                launchAlert(AlertDialogFragment.GPLAY_UPDATE);
                break;
            case ConnectionResult.SERVICE_DISABLED:
                launchAlert(AlertDialogFragment.GPLAY_DISABLED);
                break;
            case ConnectionResult.SERVICE_INVALID:
                launchAlert(AlertDialogFragment.GPLAY_INVALID);
                break;
        }
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
    protected void onPause(){
        super.onPause();
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

    public void launchAlert(int alertCode) {
        AlertDialogFragment adf = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(AlertDialogFragment.ALERT_KEY, alertCode);
        adf.setArguments(bundle);
        adf.setAlertDialogListener(this);
        adf.show(getSupportFragmentManager(), AlertDialogFragment.ALERT_KEY);
    }

    @Override
    public void onAlertButtonPressed(int eventKey) {
        switch(eventKey){
            case AlertDialogFragment.RE_FETCH_FEED:
                break;
            case AlertDialogFragment.SEND_SAVED_REPORTS:
                Intent intent = new Intent().setClass(this, NewReportActivity.class)
                        .putExtra(NewReportActivity.UPLOAD_SAVED_REPORTS_KEY, true)
                        .putExtra(USERNAME_KEY, mUsername);
                startActivity(intent);
                break;
            case AlertDialogFragment.INSTALL_GPLAY:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
                break;
            case AlertDialogFragment.UPDATE_GPLAY:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
                break;
        }
    }

    public void goToDetailView(Cursor c, int position) {
        mFragment = new ReportDetailFragment();
        mFragment.setData(c);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, mFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack(null)
            .commit();
    }

    public void goToNewReport(){
        Intent intent = new Intent();
        intent.setClass(this, NewReportActivity.class);
        intent.putExtra(USERNAME_KEY, mUsername);
        // intent.putExtra("index", index);
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
            case R.id.action_refresh:
                SyncUtils.TriggerRefresh();
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
