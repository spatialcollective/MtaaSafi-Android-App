package com.sc.mtaasafi.android.feed;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.sc.mtaasafi.android.SystemUtils.AlertDialogFragment;
import com.sc.mtaasafi.android.SystemUtils.ComplexPreferences;
import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.database.SyncUtils;
import com.sc.mtaasafi.android.newReport.NewReportActivity;

import io.fabric.sdk.android.Fabric;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity implements
        NewsFeedFragment.ReportSelectedListener, AlertDialogFragment.AlertDialogListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    public String mUsername;
    ReportDetailFragment mFragment;
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    ComplexPreferences cp;
                        // onActivityResult
    static final int    REQUEST_CODE_PICK_ACCOUNT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Log.e(LogTags.MAIN_ACTIVITY, "onCreate");
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        mLocationClient = new LocationClient(this, this, this);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null)
            mFragment = (ReportDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mFragment");
        if (mFragment == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new NewsFeedFragment())
                .commit();
        }
        cp = PrefUtils.getPrefs(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putString(PrefUtils.USERNAME, mUsername);
        if (mFragment != null && mFragment.isAdded())
            getSupportFragmentManager().putFragment(bundle, "mFragment", mFragment);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUsername = savedInstanceState.getString(PrefUtils.USERNAME);
        if (mUsername == null || mUsername.equals(""))
            determineUsername();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LogTags.MAIN_ACTIVITY, "onStart");
        cp.putObject(PrefUtils.SCREEN_WIDTH, getScreenWidth());
        cp.commit();
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
            case ConnectionResult.SUCCESS:
                mLocationClient.connect();
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
                uploadSavedReports();
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
        intent.putExtra(PrefUtils.USERNAME, mUsername);
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
        cp.putObject(PrefUtils.USERNAME, mUsername);
        cp.commit();
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
            String savedUserName = cp.getString(PrefUtils.USERNAME, "");
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

    public void uploadSavedReports(){
        Intent intent = new Intent().setClass(this, NewReportActivity.class)
                .putExtra(NewReportActivity.UPLOAD_SAVED_REPORTS_KEY, true)
                .putExtra(PrefUtils.USERNAME, mUsername);
        startActivity(intent);
    }
    public int getScreenWidth(){return getWindowManager().getDefaultDisplay().getWidth();}

    public int getScreenHeight(){
        return getWindowManager().getDefaultDisplay().getHeight();
    }
    // ======================Google Play Services:======================
    public Location getLocation() {
        if(mLocationClient != null && mLocationClient.isConnected()){
            mCurrentLocation = mLocationClient.getLastLocation();
            cp.putObject(PrefUtils.LOCATION, mCurrentLocation);
        }
        return mCurrentLocation;
    }

    @Override
    public void onConnected(Bundle bundle) {
        getLocation();
    }
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected from Google Play. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 15000;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try { // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) { // Thrown if Google Play services canceled the original PendingIntent
                e.printStackTrace();
            }
        } else { // If no resolution is available, display a dialog to the user with the error.
            CharSequence text = "Google play connection failed, no resolution";
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    public boolean isLocationEnabled(){
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    public void onLocationDisabled(){
        AlertDialogFragment adf = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(AlertDialogFragment.ALERT_KEY, AlertDialogFragment.LOCATION_FAILED);
        adf.setArguments(bundle);
        adf.show(getSupportFragmentManager(), AlertDialogFragment.ALERT_KEY);
    }

}
