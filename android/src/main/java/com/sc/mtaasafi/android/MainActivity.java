package com.sc.mtaasafi.android;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
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
import com.google.android.gms.location.LocationClient;
import com.sc.mtaasafi.android.adapter.FragmentAdapter;

import io.fabric.sdk.android.Fabric;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        NewsFeedFragment.ReportSelectedListener {

    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private SharedPreferences sharedPref;
    private Report reportDetailReport, pendingReport;
    private NewReportFragment newReportFragment;
    public String mUsername, mCurrentPhotoPath;
    private int currentFragment, nextPieceKey, lastPreviewClicked;
    ArrayList<String> picPaths;

    private final int TOTAL_PICS = 3;

    NonSwipePager mPager;
    FragmentAdapter fa;

    static final String USERNAME_KEY = "username",
                        CURRENT_PHOTO_PATH_KEY = "photo_path",
                        CURRENT_FRAGMENT_KEY = "current_fragment",
                        HAS_REPORT_DETAIL_KEY = "report_detail",
                        PIC_PATHS_KEY = "picPaths",
                        PENDING_PIECE_KEY ="next_field",
                        PENDING_REPORT_TYPE_KEY = "report_to_send_id",
                        REPORT_DETAIL_KEY = "report_detail";

                        // onActivityResult
    static final int    REQUEST_CODE_PICK_ACCOUNT = 1000,
                        REQUEST_IMAGE_CAPTURE = 1,

                        // FragmentPager
                        FRAGMENT_FEED = 0,
                        FRAGMENT_REPORTDETAIL = 2,
                        FRAGMENT_NEWREPORT = 1;

    // Called by the server communicator if it cannot successfully receive posts from the server
    public void onUpdateFailed() {
        final Toast toast = Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT);
        runOnUiThread(new Runnable() {
            public void run() {
                toast.show();
            }
        });
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

    public int getScreenWidth(){
        return getWindowManager().getDefaultDisplay().getWidth();
    }

    public void setNewReportFragmentListener(NewReportFragment nrf){
        newReportFragment = nrf;
//        if(pendingReport != null){
//            for(int i = 0; i < nextPieceKey+1; i++){
//                newReportFragment.onPostUpdate(i);
//            }
//        }
    }

    public void clearNewReportData() {
        pendingReport = null;
        nextPieceKey = -1; // TRANSACTION_COMPLETE
        picPaths.clear();
        for (int i = 0; i < TOTAL_PICS; i++)
            picPaths.add(null);
        goToFeed();
        newReportFragment = null;
    }

    // ======================Fragment Navigation:======================
    public void goToFeed(){
        mPager.setCurrentItem(FRAGMENT_FEED);
        currentFragment = FRAGMENT_FEED;
    }

    public void goToDetailView(Report report){
        reportDetailReport = report;
        mPager.setCurrentItem(FRAGMENT_REPORTDETAIL);
        Log.e("GO TO DETAIL VIEW", report.title);
        currentFragment = FRAGMENT_REPORTDETAIL;
    }
    public void getReportDetailReport(ReportDetailFragment rdf){
        if (reportDetailReport != null)
            rdf.updateView(reportDetailReport);
    }

    public void goToNewReport(){
        mPager.setCurrentItem(FRAGMENT_NEWREPORT);
        currentFragment = FRAGMENT_NEWREPORT;
    }

    // ======================Google Play Services Setup:======================
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 15000;

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = mLocationClient.getLastLocation();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED && requestCode == REQUEST_CODE_PICK_ACCOUNT)
            Toast.makeText(this, "You must pick an account to proceed", Toast.LENGTH_SHORT).show();
        if (requestCode == REQUEST_IMAGE_CAPTURE){
            Log.e(LogTags.PHOTO, "onActivityResult");
            onPhotoTaken();
            mPager.setCurrentItem(FRAGMENT_NEWREPORT);
        }
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

    public Location getLocation() {
        mCurrentLocation = mLocationClient.getLastLocation();
        return mCurrentLocation;
    }

    // ======================Picture-taking Logic:======================
    // Called by the new report fragment to launch a take picture activity.
    public void takePicture(NewReportFragment nrf, int previewClicked){
        Log.e(LogTags.PHOTO, "takePicture");
        lastPreviewClicked = previewClicked;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
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

    private void onPhotoTaken(){
        File file = new File(mCurrentPhotoPath);
        if(file != null && file.length() != 0)
            picPaths.set(lastPreviewClicked, mCurrentPhotoPath);
        Log.e(LogTags.PHOTO, picPaths.toString());
    }

    public ArrayList<String> getPics(){
        return picPaths;
    }

    // ======================Activity Setup:======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Log.e(LogTags.MAIN_ACTIVITY, "onCreate");
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);
        mLocationClient = new LocationClient(this, this, this);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        fa = new FragmentAdapter(getSupportFragmentManager());
        mPager = (NonSwipePager)findViewById(R.id.pager);
        mPager.setAdapter(fa);

        picPaths = new ArrayList<String>();
        for(int i = 0; i < TOTAL_PICS; i++)
            picPaths.add(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LogTags.MAIN_ACTIVITY, "onStart");

        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals("")) {
            // TODO: show a dialog fragment that will say you need to turn on location to make this thing work
            // If they say yes, send them to Location Settings
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        mLocationClient.connect();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        mUsername = savedInstanceState.getString(USERNAME_KEY);
//        if (mUsername == null)
//            determineUsername();
//
//        mCurrentPhotoPath = savedInstanceState.getString(CURRENT_PHOTO_PATH_KEY);
//        currentFragment = savedInstanceState.getInt(CURRENT_FRAGMENT_KEY);
//        picPaths = new ArrayList(
//                    savedInstanceState.getStringArrayList(PIC_PATHS_KEY)
//                    .subList(0, TOTAL_PICS));
//        Log.e(LogTags.NEWREPORT, "current item " + currentFragment);
//        if (savedInstanceState.getBoolean(HAS_REPORT_DETAIL_KEY))
//            reportDetailReport = new Report(REPORT_DETAIL_KEY, savedInstanceState);
//        if (savedInstanceState.getInt(PENDING_REPORT_TYPE_KEY) != -1) { // NO_REPORT_TO_SEND
//            goToNewReport();
//            nextPieceKey = savedInstanceState.getInt(PENDING_PIECE_KEY);
//            pendingReport = new Report(PENDING_REPORT_TYPE_KEY, savedInstanceState);
//            beamUpNewReport(pendingReport);
//        }
    }
    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putString(USERNAME_KEY, mUsername);
        bundle.putString(CURRENT_PHOTO_PATH_KEY, mCurrentPhotoPath);
        bundle.putInt(CURRENT_FRAGMENT_KEY, currentFragment);
        if(reportDetailReport != null){
            reportDetailReport.saveState(REPORT_DETAIL_KEY, bundle);
        }
        bundle.putBoolean(HAS_REPORT_DETAIL_KEY, reportDetailReport != null);
        bundle.putStringArrayList(PIC_PATHS_KEY, picPaths);
        bundle.putInt(PENDING_PIECE_KEY, nextPieceKey);
        if (pendingReport != null)
            pendingReport.saveState(PENDING_REPORT_TYPE_KEY, bundle);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e(LogTags.MAIN_ACTIVITY, "onResume");
        determineUsername();
        mPager.setCurrentItem(currentFragment);
    }
    @Override
    protected void onStop(){
        // Disconnecting the client invalidates it.
        Log.e(LogTags.MAIN_ACTIVITY, "onStop");
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(pendingReport == null && currentFragment != FRAGMENT_FEED)
            goToFeed();
        getSupportActionBar().show();
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
