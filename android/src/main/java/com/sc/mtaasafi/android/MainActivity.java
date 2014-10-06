package com.sc.mtaasafi.android;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        ServerCommunicater.ServerCommCallbacks,
        NewsFeedFragment.ReportSelectedListener {
    private NewsFeedFragment feedFragment;
    private NewReportFragment newReportFragment;
    private ReportDetailFragment reportDetailFragment;
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private ListView feedView;
    private ServerCommunicater sc;
    private Report report;
    private SharedPreferences sharedPref;
    public String mUsername;
    public String mCurrentPhotoPath;
    static final String USERNAME_KEY = "username";
    static final String FEED_FRAG_KEY = "feed";
    static final String NEW_REPORT_KEY = "new_report";
    static final String REPORT_DETAIL_KEY = "report_detail";
    static final String CURRENT_PHOTO_PATH_KEY = "photo_path";
    static String CURRENT_FRAGMENT_KEY = "current_fragment";
    static final String FRAGMENT_NEWREPORT = "1";
    static final String FRAGMENT_REPORTDETAIL = "2";
    static final String FRAGMENT_FEED = "3";
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_PICK_IMAGE = 100;


    // ======================Client-Server Communications:======================

    // Called by the server communicator to add new posts to the feed fragment
    @Override
    public void onFeedUpdate(List<Report> allReports) {
        feedFragment.onFeedUpdate(allReports);
        runOnUiThread(new Runnable() {
            public void run() {
                feedFragment.alertFeedUpdate();
            }
        });
    }

    // Called by the server communicator if it cannot successfully receive posts from the server
    // for any reason.
    public void onUpdateFailed() {
        runOnUiThread(new Runnable() {
            public void run() {
                // Toast.makeText(getApplicationContext(), "Failed to update feed", Toast.LENGTH_SHORT).show();
                AlertDialogFragment adf = new AlertDialogFragment();
                adf.show(getSupportFragmentManager(), "Update_failed_dialog");
            }
        });
    }

    // called by the fragment to update the fragment's feed w new posts.
    // When the server communicator gets the new posts, it will call onFeedUpdate above.
    public void updateFeed(){
        sc.getPosts();
    }
    public int getScreenWidth(){
        return getWindowManager().getDefaultDisplay().getWidth();
    }
    // takes a post written by the user from the feed fragment, pushes it to server
    public void beamItUp(Report report){
//        String toastContent = "user " + report.userName + " " + report.title + " " + report.timeElapsed + " Lat: " + report.latitude
//                + " Lon:" + report.longitude;
//        Toast toast = Toast.makeText(this, toastContent, Toast.LENGTH_SHORT);
//        toast.show();
        sc.post(report);
        updateFeed();
    }

    public void goToDetailView(Report report){
        Bundle args = report.saveState(new Bundle());
        if(reportDetailFragment == null){
            reportDetailFragment = new ReportDetailFragment();
        }
        reportDetailFragment.setArguments(args);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, reportDetailFragment, "reportDetailView")
                .addToBackStack(null)
                .commit();
    }

    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) { mDialog = dialog; }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) { return mDialog; }
    }
    // ======================Fragment Navigation:======================
    public void goToFeed(){
        if(feedFragment == null){
            feedFragment = new NewsFeedFragment();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, feedFragment, "newsfeed")
                .addToBackStack(null)
                .commit();
        getSupportActionBar().show();
    }

    public void goToNewReport(){
        if(newReportFragment == null){
            newReportFragment = new NewReportFragment();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, newReportFragment, "newReport")
                .addToBackStack(null)
                .commit();
    }

    // ======================Google Play Services Setup:======================
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 15000;

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = mLocationClient.getLastLocation();
        // Toast toast = Toast.makeText(this, "Location: " + mCurrentLocation.getLatitude() + " " + mCurrentLocation.getLongitude(), Toast.LENGTH_SHORT);
        // toast.show();
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected from Google Play. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED && requestCode == REQUEST_CODE_PICK_ACCOUNT)
            Toast.makeText(this, "You must pick an account to proceed", Toast.LENGTH_SHORT).show();
        if (resultCode != Activity.RESULT_OK)
            return;
        else if (requestCode == REQUEST_IMAGE_CAPTURE)
            newReportFragment.onPhotoTaken(mCurrentPhotoPath);
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

    public Location getLocation(){
        mCurrentLocation = mLocationClient.getLastLocation();
        return mCurrentLocation;
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            // Log.d("Location Updates", "Google Play services is available.");
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null)
                showErrorFragment(errorDialog);
            return false;
        }
    }

    private void showErrorFragment(Dialog errorDialog){
        ErrorDialogFragment errorFragment =
                new ErrorDialogFragment();
        errorFragment.setDialog(errorDialog);
        errorFragment.show(getSupportFragmentManager(), "Location Updates");
    }

    // ======================Picture-taking Logic:======================
    // Called by the new report fragment to launch a take picture activity.
    public void takePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                // Log.w(LogTags.FEEDADAPTER, "Take picture: " + Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException ex){
                Toast.makeText(this, "Couldn't create file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Couldn't resolve activity", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // ======================Activity Setup:======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationClient = new LocationClient(this, this, this);
        sc = new ServerCommunicater(this);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        if(savedInstanceState != null){
            mUsername = savedInstanceState.getString(USERNAME_KEY);
            mCurrentPhotoPath = savedInstanceState.getString(CURRENT_PHOTO_PATH_KEY);
            FragmentManager manager = getSupportFragmentManager();
            goToNewReport();
            feedFragment = (NewsFeedFragment) manager.getFragment(savedInstanceState, FEED_FRAG_KEY);
            newReportFragment = (NewReportFragment) manager.getFragment(savedInstanceState, NEW_REPORT_KEY);
            reportDetailFragment = (ReportDetailFragment) manager.getFragment(savedInstanceState, REPORT_DETAIL_KEY);
            String currentFragment = savedInstanceState.getString(CURRENT_FRAGMENT_KEY);
            goToNewReport();
//            if(currentFragment.equals(FRAGMENT_NEWREPORT)){
//                goToNewReport();
//            }
//            else{
//                goToFeed();
////            }
        }
        else{
            goToFeed();
        }
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();
        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals("")) {
            // TODO: show a dialog fragment that will say you need to turn on location to make this thing work
            // If they say yes, send them to Location Settings
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        mLocationClient.connect();
//        updateFeed();
    }
    @Override
    protected void onRestoreInstanceState(Bundle bundle){
        mUsername = bundle.getString(USERNAME_KEY);
        if (mUsername == null)
            determineUsername();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putString(USERNAME_KEY, mUsername);
        bundle.putString(CURRENT_PHOTO_PATH_KEY, mCurrentPhotoPath);
        bundle.putString(CURRENT_FRAGMENT_KEY, FRAGMENT_NEWREPORT);
        if(feedFragment != null){
            getSupportFragmentManager().putFragment(bundle, FEED_FRAG_KEY, feedFragment);
        }
        if(newReportFragment != null){
            getSupportFragmentManager().putFragment(bundle, NEW_REPORT_KEY, newReportFragment);
        }
        if(reportDetailFragment != null){
            getSupportFragmentManager().putFragment(bundle, REPORT_DETAIL_KEY, reportDetailFragment);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        determineUsername();
    }
    @Override
    protected void onStop(){
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
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
            case R.id.accounts_menu:
                showLogins();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().findFragmentByTag("newReport") == null)
            getSupportActionBar().show();
    }

    public void showLogins() {
        DialogFragment newFragment = new AccountsFragment();
        newFragment.show(getSupportFragmentManager(), "accounts");
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
