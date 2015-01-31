package com.sc.mtaa_safi.feed;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
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
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sc.mtaa_safi.MtaaLocationService;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.AlertDialogFragment;
import com.sc.mtaa_safi.SystemUtils.ComplexPreferences;
import com.sc.mtaa_safi.SystemUtils.LogTags;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.database.SyncUtils;
import com.sc.mtaa_safi.newReport.NewReportActivity;
import com.sc.mtaa_safi.uploading.UploadingActivity;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends ActionBarActivity implements
        AlertDialogFragment.AlertDialogListener,
        SwipeRefreshLayout.OnRefreshListener {

    ReportDetailFragment detailFragment;
    NewsFeedFragment newsFeedFrag;
    ComplexPreferences cp;
    static final int    REQUEST_CODE_PICK_ACCOUNT = 1000;
    public final static String NEWSFEED_TAG = "newsFeed", DETAIL_TAG = "details", ONBOARD_TAG= "onboard";

    private MtaaLocationService mBoundService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((MtaaLocationService.LocalBinder)service).getService();
            //Toast.makeText(this, "Location Service Enabled", Toast.LENGTH_SHORT).show();
        }
        public void onServiceDisconnected(ComponentName className) { mBoundService = null; } // This should never happen
    };

    void bindLocationService() {
        Log.e("MainActivity", "binding to location");
        bindService(new Intent(this, MtaaLocationService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        restoreFragment(savedInstanceState);
        cp = PrefUtils.getPrefs(this);
    }

    private void restoreFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            detailFragment = (ReportDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, DETAIL_TAG);
        if (detailFragment == null)
            goToFeed(savedInstanceState);
    }

    public void goToFeed(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            newsFeedFrag = (NewsFeedFragment) getSupportFragmentManager().getFragment(savedInstanceState, NEWSFEED_TAG);
        if (newsFeedFrag == null) {
            newsFeedFrag = new NewsFeedFragment();
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, newsFeedFrag, NEWSFEED_TAG)
                .commit();
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

    public void goToNewReport(View view) {
        goToNewReport();
    }
    public void goToNewReport() {
        Intent intent = new Intent();
        intent.setClass(this, NewReportActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        supportInvalidateOptionsMenu();
        bindLocationService();
        determineUsername();
        Log.e(LogTags.MAIN_ACTIVITY, "onStart");
        cp.putObject(PrefUtils.SCREEN_WIDTH, getScreenWidth());
        cp.commit();
        setUpWeirdGPlayStuff();
    }
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        if (detailFragment != null && detailFragment.isAdded())
            getSupportFragmentManager().putFragment(bundle, DETAIL_TAG, detailFragment);
        if (newsFeedFrag != null)
            getSupportFragmentManager().putFragment(bundle, NEWSFEED_TAG, newsFeedFrag);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED && requestCode == REQUEST_CODE_PICK_ACCOUNT)
            Toast.makeText(this, "You must pick an account to proceed", Toast.LENGTH_SHORT).show();
        else if (requestCode == REQUEST_CODE_PICK_ACCOUNT){
            saveUserName(data);
            goToOnboarding();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        addUploadAction(menu);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CharSequence titleChar = item.getTitle();
        if (titleChar == null) {
            onBackPressed();
            return true;
        }
        if (titleChar.toString().equals("Upload Saved Reports"))
            uploadSavedReports();
        else
            return super.onOptionsItemSelected(item);
        return true;
    }

    private void addUploadAction(Menu menu){
        int savedReportCt = getSavedReportCount(this);
        int drawable = 0;
        switch (savedReportCt) {
            case 1: drawable = R.drawable.button_uploadsaved1; break;
            case 2: drawable = R.drawable.button_uploadsaved2; break;
            case 3: drawable = R.drawable.button_uploadsaved3; break;
            case 4: drawable = R.drawable.button_uploadsaved4; break;
            case 5: drawable = R.drawable.button_uploadsaved5; break;
            case 6: drawable = R.drawable.button_uploadsaved6; break;
            case 7: drawable = R.drawable.button_uploadsaved7; break;
            case 8: drawable = R.drawable.button_uploadsaved8; break;
            case 9: drawable = R.drawable.button_uploadsaved9; break;
            default:
                if (savedReportCt > 9)
                    drawable = R.drawable.button_uploadsaved9plus;
        }
        if (drawable != 0)
            menu.add(0, 0, 0, "Upload Saved Reports").setIcon(drawable)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    public static int getSavedReportCount(Activity ac){
        String[] projection = new String[1];
        projection[0] = Contract.Entry.COLUMN_ID;
        Cursor c = ac.getContentResolver().query(
                Contract.Entry.CONTENT_URI,
                projection,
                Contract.Entry.COLUMN_PENDINGFLAG + " >= 0 ", null, null);
        int count = c.getCount();
        c.close();
        return count;
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

    public void uploadSavedReports() {
        Intent intent = new Intent().setClass(this, UploadingActivity.class)
                .setAction(String.valueOf(0));
        startActivity(intent);
    }
    public int getScreenWidth() { return getWindowManager().getDefaultDisplay().getWidth(); }
    public int getScreenHeight() { return getWindowManager().getDefaultDisplay().getHeight(); }

    public Location getLocation() { return mBoundService.getLocation(); }
    private void onLocationDisabled() {
        AlertDialogFragment.showAlert(AlertDialogFragment.LOCATION_FAILED, this, getSupportFragmentManager());
    }

    @Override
    public void onRefresh() {
        Location loc = getLocation();
        if (NetworkUtils.isOnline(this) && loc != null) {
            cp.putObject(PrefUtils.LOCATION, loc);
            SyncUtils.TriggerRefresh();
        } else
            ((NewsFeedFragment) getSupportFragmentManager().findFragmentByTag(NEWSFEED_TAG))
                    .refreshFailed();
//        if (loc == null)
//            AlertDialogFragment.showAlert(AlertDialogFragment.LOCATION_FAILED, this, getSupportFragmentManager());
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

    private void setUpWeirdGPlayStuff() {
        int gPlayCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        switch (gPlayCode) {
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
                break;
        }
    }

    private void goToOnboarding(){
//        OnboardingFragment onboardingFragment = (OnboardingFragment)
//                                                    getSupportFragmentManager().findFragmentByTag(ONBOARD_TAG);
//        if(onboardingFragment != null)
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, onboardingFragment, ONBOARD_TAG)
//                    .commit();
//        else
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new OnboardingFragment(), ONBOARD_TAG)
//                    .commit();
//
//        getSupportActionBar().hide();
    }
}
