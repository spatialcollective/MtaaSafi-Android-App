package com.sc.mtaasafi.android;


import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.sc.mtaasafi.android.NewsFeedFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, ServerCommunicater.ServerCommCallbacks{
    private NewsFeedFragment feedFragment;
    private NewReportFragment newReportFragment;
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private ServerCommunicater sc;
    private PostData detailPostData;
    private SharedPreferences sharedPref;
    public String mUsername;
    public String mCurrentPhotoPath;
    static final String PREF_USERNAME = "username";
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_PICK_IMAGE = 100;

    // ======================Client-Server Communications:======================

    // Called by the server communicator to add new posts to the feed fragment
    @Override
    public void onFeedUpdate(List<PostData> newPosts) {
        feedFragment.onFeedUpdate(newPosts);
        // on the UI thread, tell the feed fragment that its list has been updated.
        runOnUiThread(new Runnable() {
            public void run() {
                feedFragment.alertFeedUpdate();
            }
        });
    }
    // Called by the server communicator if it cannot successfully receive posts from the server
    // for any reason.
    public void onUpdateFailed(){
        Toast.makeText(this, "Failed to update feed", Toast.LENGTH_SHORT).show();
        AlertDialogFragment adf = new AlertDialogFragment();
        adf.show(getSupportFragmentManager(), "Update_failed_dialog");
        List<PostData> posts = new ArrayList<PostData>();
        for(int i = 0; i < 15; i++){
            PostData pd = new PostData(mUsername,
                    "",
                    0,0,
                    "this is"+i, "my song" + i,
                    null,
                    null);
            posts.add(pd);
            }
        if(feedFragment!=null){
            onFeedUpdate(posts);
        }
    }

    // called by the fragment to update the fragment's feed w new posts.
    // When the server communicator gets the new posts, it will call onFeedUpdate above.
    public void updateFeed(){
        sc.getPosts();
    }

    // takes a post written by the user from the feed fragment, pushes it to server
    public void beamItUp(PostData postData){
        String toastContent = postData.title + " " + postData.timestamp + " Lat: " + postData.latitude
                + " Lon:" + postData.longitude;
        Toast toast = Toast.makeText(this, toastContent, Toast.LENGTH_SHORT);
        toast.show();
        sc.post(postData);
        updateFeed();
    }

    // ======================Post View Fragment:======================

    public void goToDetailView(PostData pd){
        detailPostData = pd;
        PostView postView = new PostView();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, postView, "postView")
                .addToBackStack(null)
                .commit();
    }
    // Called by postView fragment to retrieve the contents of the post it should be displaying.
    public PostData getDetailPostData(){
        return detailPostData;
    }

    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;
        public ErrorDialogFragment(){
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    // ======================Fragment Navigation:======================
    public void goToFeed(){
        feedFragment = new NewsFeedFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, feedFragment, "newsfeed")
                .addToBackStack(null)
                .commit();
        getSupportActionBar().show();
    }


    public void goToNewReport(){
        newReportFragment = new NewReportFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, newReportFragment, "newReport")
                .addToBackStack(null)
                .commit();
        getSupportActionBar().hide();
    }
    // ======================Google Play Services Setup:======================
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 15000;

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Connected to Google Play", Toast.LENGTH_SHORT).show();
        mCurrentLocation = mLocationClient.getLastLocation();
        Toast toast = Toast.makeText(this, "Location: " + mCurrentLocation.getLatitude()
                + " " + mCurrentLocation.getLongitude(), Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected from Google Play. Please re-connect.",
                Toast.LENGTH_SHORT).show();

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
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            CharSequence text = "Google play connection failed, no resolution";
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If activity launched was trying to resolve the connection
        Log.w("SERVICES", "Activity result called");
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
                // and it resolved the connection
                if (resultCode == Activity.RESULT_OK) {
                        Log.w("SERVICES", "Yo the activity was okay--try reconnecting");
                        break;
                }
                else{
                    Log.w("SERVICES", "Activity result was NOT okay");
                }
            case REQUEST_IMAGE_CAPTURE :
                if (resultCode == Activity.RESULT_OK){
                    Log.e(LogTags.FEEDADAPTER, "Activity result" + mCurrentPhotoPath);
                    Bundle extras = data.getExtras();
                    // Get the returned image from extra
                    Bitmap bitmap = (Bitmap) extras.get("data");
//                    Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                    ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytearrayoutputstream);
                    final byte[] bytearray = bytearrayoutputstream.toByteArray();
                    newReportFragment.onPhotoTaken(bytearray);
                }else {
                    Log.w("CAMERA", "Activity result was NOT okay");
                }
            case REQUEST_CODE_PICK_ACCOUNT:
                if (resultCode == Activity.RESULT_OK) {
                    String retrievedUserName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    Toast.makeText(this, "Retrieved: " + retrievedUserName,
                            Toast.LENGTH_SHORT).show();
                    mUsername = retrievedUserName;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(PREF_USERNAME, retrievedUserName);
                    editor.commit();

                }
                else if (resultCode == RESULT_CANCELED) {
                    // The account picker dialog closed without selecting an account.
                    // Notify users that they must pick an account to proceed.
                    Toast.makeText(this, "You must pick an account to proceed",
                        Toast.LENGTH_SHORT).show();
                    }

                }
        }

    public Location getLocation(){
        mCurrentLocation = mLocationClient.getLastLocation();
        return mCurrentLocation;
    }
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            return true;

            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                showErrorFragment(errorDialog);
            }
            return false;
        }
    }

    private void showErrorFragment(Dialog errorDialog){
        ErrorDialogFragment errorFragment =
                new ErrorDialogFragment();
        // Set the dialog in the DialogFragment
        errorFragment.setDialog(errorDialog);
        // Show the error dialog in the DialogFragment
        errorFragment.show(getSupportFragmentManager(),
                "Location Updates");

    }
    // ======================Picture-taking Logic:======================
    // Called by the new report fragment to launch a take picture activity.
    public void takePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex){
                Toast.makeText(this, "Couldn't create file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null){
                //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoPath);
                Log.w(LogTags.FEEDADAPTER, "Take picture: " + Uri.fromFile(photoFile).toString());
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
        else{
            Toast.makeText(this, "Couldn't resolve activity", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

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
        determineUsername();
        if (savedInstanceState == null){
            goToFeed();
        } else {
            feedFragment = (NewsFeedFragment) getSupportFragmentManager()
                    .findFragmentByTag("feedFragment");
        }
    }

    private boolean isConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals("")) {
            // TODO: show a dialog fragment that will say you need to turn on location to make this thing work
            // If they say yes, send them to Location Settings
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
            // Connect the client.
        mLocationClient.connect();
        sc.getPosts();
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
        // Handle presses on the action bar items
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
        if(getSupportFragmentManager().findFragmentByTag("newReport") == null){
            getSupportActionBar().show();
        }
    }
    public void showLogins() {
        DialogFragment newFragment = new AccountsFragment();
        newFragment.show(getSupportFragmentManager(), "accounts");
    }

    private void determineUsername(){
        String savedUserName = sharedPref.getString(PREF_USERNAME, "");
        if(savedUserName.equals("")){
            pickUserAccount();
        }
        else{
            mUsername = savedUserName;
            Toast.makeText(this, "Saved: " + mUsername,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }
}
