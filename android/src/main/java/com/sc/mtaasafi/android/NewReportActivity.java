package com.sc.mtaasafi.android;

import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.sc.mtaasafi.android.R;

import java.util.ArrayList;

public class NewReportActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
//        NewReportFragment.PictureTakenListener {

    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    ArrayList<String> picPaths;
    NewReportFragment mFragment;


    static final String CURRENT_PHOTO_PATH_KEY = "photo_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = new NewReportFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, mFragment);
//        transaction.addToBackStack(null);
        transaction.commit();

        mLocationClient = new LocationClient(this, this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LogTags.MAIN_ACTIVITY, "onStart");

        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals(""))
            onLocationDisabled();
        mLocationClient.connect();
    }
    @Override
    protected void onStop(){
        // Disconnecting the client invalidates it.
        Log.e(LogTags.MAIN_ACTIVITY, "onStop");
        mLocationClient.disconnect();
        super.onStop();
    }

    // ======================Picture-taking Logic:======================
    // public void takePicture() {
    //     Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    //     if (takePictureIntent.resolveActivity(this.getPackageManager()) != null){
    //         File photoFile = null;
    //         try {
    //             photoFile = createImageFile(lastPreviewClicked);
    //             takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
    //             startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    //         } catch (IOException ex){
    //             Toast.makeText(this, "Couldn't create file", Toast.LENGTH_SHORT).show();
    //         }
    //     } else {
    //         Toast.makeText(this, "Couldn't resolve activity", Toast.LENGTH_SHORT).show();
    //     }
    // }
// Save the preview clicked in the file path so that you can retrieve it in onActivityResult, which is called
// before on restore instance state.
    // private File createImageFile(int previewClicked) throws IOException {
    //     String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    //     String imageFileName = "#" + previewClicked + "#" + "JPEG_" + timestamp + "_";
    //     File storageDir = Environment.getExternalStoragePublicDirectory(
    //             Environment.DIRECTORY_PICTURES
    //     );
    //     File image = File.createTempFile(imageFileName, ".jpg", storageDir);

    //     mCurrentPhotoPath = image.getAbsolutePath();
    //     return image;
    // }

    public ArrayList<String> getPics(){
        return picPaths;
    }

    public Location getLocation() {
        if(mLocationClient != null)
            mCurrentLocation = mLocationClient.getLastLocation();
        return mCurrentLocation;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = mLocationClient.getLastLocation();
    }
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected from Google Play. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    // ======================Google Play Services Setup:======================
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

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
//        bundle.putString(CURRENT_PHOTO_PATH_KEY, mCurrentPhotoPath);
//        bundle.putStringArrayList(PIC_PATHS_KEY, picPaths);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

//        mCurrentPhotoPath = savedInstanceState.getString(CURRENT_PHOTO_PATH_KEY);
//        picPaths = new ArrayList(
//                    savedInstanceState.getStringArrayList(mFragment.PIC_PATHS_KEY)
//                    .subList(0, mFragment.pic_count));
//        if (savedInstanceState.getBoolean(mFragment.HAS_PENDING_REPORT)) {
//            NewReportFragment nrf = (NewReportFragment) fa.getItem(mPager.getCurrentItem());
//            nrf.beamUpReport();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
