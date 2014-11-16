package com.sc.mtaasafi.android.feed;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.SystemUtils.AlertDialogFragment;
import com.sc.mtaasafi.android.SystemUtils.ComplexPreferences;
import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.database.SyncUtils;
import com.sc.mtaasafi.android.newReport.NewReportActivity;
import com.sc.mtaasafi.android.uploading.UploadingActivity;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends ActionBarActivity implements
        NewsFeedFragment.ReportSelectedListener, AlertDialogFragment.AlertDialogListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        SwipeRefreshLayout.OnRefreshListener{

    ReportDetailFragment detailFragment;
    private LocationClient mLocationClient;
    ComplexPreferences cp;
                        // onActivityResult
    static final int    REQUEST_CODE_PICK_ACCOUNT = 1000;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 15000;
    public final static String  NEWSFEED_TAG = "newsFeed",
                                DETAIL_TAG = "details";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Log.e(LogTags.MAIN_ACTIVITY, "onCreate");
        mLocationClient = new LocationClient(this, this, this);
        setContentView(R.layout.activity_main);
        restoreFragment(savedInstanceState);
        cp = PrefUtils.getPrefs(this);
        determineUsername();
    }

    private void restoreFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            detailFragment = (ReportDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, DETAIL_TAG);
        if (detailFragment == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new NewsFeedFragment(), NEWSFEED_TAG)
                .commit();
        }
        cp = PrefUtils.getPrefs(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        if (detailFragment != null && detailFragment.isAdded())
            getSupportFragmentManager().putFragment(bundle, DETAIL_TAG, detailFragment);
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
                AlertDialogFragment.showAlert(AlertDialogFragment.GPLAY_MISSING, this, getSupportFragmentManager());
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                AlertDialogFragment.showAlert(AlertDialogFragment.GPLAY_UPDATE, this, getSupportFragmentManager());
                break;
            case ConnectionResult.SERVICE_DISABLED:
                AlertDialogFragment.showAlert(AlertDialogFragment.GPLAY_DISABLED, this, getSupportFragmentManager());
                break;
            case ConnectionResult.SERVICE_INVALID:
                AlertDialogFragment.showAlert(AlertDialogFragment.GPLAY_INVALID, this, getSupportFragmentManager());
                break;
            case ConnectionResult.SUCCESS:
                String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if (locationProviders == null || locationProviders.equals(""))
                    onLocationDisabled();
                else
                    mLocationClient.connect();
                break;
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        supportInvalidateOptionsMenu();
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

    public void goToDetailView(Report r, int position) {
        detailFragment = new ReportDetailFragment();
        detailFragment.setData(r);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, detailFragment, DETAIL_TAG)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack(null)
            .commit();
    }
    public NewsFeedFragment getNewsFeedFragment(){
        return (NewsFeedFragment) getSupportFragmentManager().findFragmentByTag(NEWSFEED_TAG);
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
            saveUserName(data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        setUpActionBar(menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setUpActionBar(Menu menu){
        int savedReportCt = NewReportActivity.getSavedReportCount(this);
        switch(savedReportCt){
            case 1:
                menu.add(0, 0, 0, "Upload Saved Reports").setIcon(R.drawable.button_uploadsaved1)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                break;
            case 2:
                menu.add(0, 0, 0, "Upload Saved Reports").setIcon(R.drawable.button_uploadsaved2)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                break;
            case 3:
                menu.add(0, 0, 0, "Upload Saved Reports").setIcon(R.drawable.button_uploadsaved3)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                break;
            case 4:
                menu.add(0, 0, 0, "Upload Saved Reports").setIcon(R.drawable.button_uploadsaved4)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                break;
            case 5:
                menu.add(0, 0, 0, "Upload Saved Reports").setIcon(R.drawable.button_uploadsaved5)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                break;
            case 6:
                menu.add(0, 0, 0, "Upload Saved Reports").setIcon(R.drawable.button_uploadsaved6)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                break;
            case 7:
                menu.add(0, 0, 0, "Upload Saved Reports").setIcon(R.drawable.button_uploadsaved7)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                break;
            case 8:
                menu.add(0, 0, 0, "Upload Saved Reports").setIcon(R.drawable.button_uploadsaved8)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                break;
            case 9:
                menu.add(0, 0, 0, "upload_saved_report").setIcon(R.drawable.button_uploadsaved9)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                break;
        }
        if(savedReportCt > 9)
            menu.add(0, 0, 0, "upload_saved_report").setIcon(R.drawable.button_uploadsaved9plus)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        supportInvalidateOptionsMenu();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String title = item.getTitle().toString();
        if(title.equals("New Report")){
            goToNewReport();
        } else if(title.equals("Upload Saved Reports"))
            uploadSavedReports();
        else
            return super.onOptionsItemSelected(item);
        return true;
    }

    private void determineUsername() {
        String savedUserName = cp.getString(PrefUtils.USERNAME, "");
        if (savedUserName == null || savedUserName.equals(""))
            pickUserAccount();
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private void saveUserName(Intent data) {
        cp.putString(PrefUtils.USERNAME, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME).replaceAll("\"",""));
        cp.commit();
    }

    public void uploadSavedReportsClicked(View view) {
        uploadSavedReports();
    }

    public void uploadSavedReports() {
        if(getLocation() != null){
            Intent intent = new Intent().setClass(this, UploadingActivity.class)
                    .setAction(String.valueOf(0));
            startActivity(intent);
        } else
            Toast.makeText(this, "Location services not yet connected", Toast.LENGTH_SHORT);
    }
    public int getScreenWidth() { return getWindowManager().getDefaultDisplay().getWidth(); }
    public int getScreenHeight() { return getWindowManager().getDefaultDisplay().getHeight(); }

    // ======================Google Play Services:======================
    public Location getLocation() {
        if(mLocationClient != null && mLocationClient.isConnected()){
            Location mCurrentLocation = mLocationClient.getLastLocation();
            if(mCurrentLocation == null){
                AlertDialogFragment.showAlert(AlertDialogFragment.LOCATION_FAILED, this, getSupportFragmentManager());
            } else{
                cp.putObject(PrefUtils.LOCATION, mCurrentLocation);
                cp.putObject(PrefUtils.LOCATION_TIMESTAMP, System.currentTimeMillis());
                cp.commit();
            }
            return mCurrentLocation;
        }
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        getLocation();
    }
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected from Google Play. Please re-connect.", Toast.LENGTH_SHORT).show();
    }
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

    private void onLocationDisabled(){
        AlertDialogFragment.showAlert(AlertDialogFragment.LOCATION_FAILED, this, getSupportFragmentManager());
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
            return true;
        return false;
    }

    @Override
    public void onRefresh() {
        if(isOnline() && getLocation() != null)
                SyncUtils.TriggerRefresh();
        else
            ((NewsFeedFragment) getSupportFragmentManager().findFragmentByTag(NEWSFEED_TAG))
                    .refreshFailed();
        if(getLocation() == null)
            AlertDialogFragment.showAlert(AlertDialogFragment.LOCATION_FAILED,
                    this,
                    getSupportFragmentManager());
    }
}
