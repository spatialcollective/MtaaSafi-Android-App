package com.sc.mtaa_safi.feed;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.GcmIntentService;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.common.BaseActivity;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.database.ReportDatabase;
import com.sc.mtaa_safi.login.FacebookActivity;
import com.sc.mtaa_safi.login.GooglePlusActivity;
import com.sc.mtaa_safi.login.LoginActivityListener;
import com.sc.mtaa_safi.login.LoginManagerFragment;
import com.sc.mtaa_safi.newReport.NewReportActivity;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity implements LoginActivityListener, ReportUpdateListener {
    ReportDetailFragment detailFragment;
    NewsFeedFragment newsFeedFrag;
    LoginManagerFragment loginManagerFragment;

    private static final int GOOGLE_PLUS_LOGIN = 100, FACEBOOK_LOGIN = 102;
    public final static String NEWSFEED_TAG = "newsFeed", DETAIL_TAG = "details", LOGIN_TAG= "login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());
        registerWithGcm();

        restoreFragment(savedInstanceState);
        if (getIntent().getExtras() != null )
            viewDetailFromNotification();
    }

    private void restoreFragment(Bundle savedInstanceState) {
        loginManagerFragment = initializeLoginManager();
        if (savedInstanceState != null)
            detailFragment = (ReportDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, DETAIL_TAG);
        if (detailFragment == null && Utils.isSignedIn(this))
            goToFeed(savedInstanceState);
        else if (!Utils.isSignedIn(this))
            showLoginManager();
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

    public void goToDetailView(Report r) {
        detailFragment = new ReportDetailFragment();
        detailFragment.setData(r);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment, DETAIL_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }
    private void viewDetailFromNotification() {
        GcmIntentService.resetAll();
        ReportDatabase dbHelper  = new ReportDatabase(this);
        if (getIntent().getIntExtra("reportId", -1) == GcmIntentService.MULTIPLE_UPDATE) {
            newsFeedFrag.setSection(1);
        } else {
            Cursor cursor = dbHelper.getReadableDatabase().query(Contract.Entry.TABLE_NAME,
                    null, Contract.Entry.COLUMN_SERVER_ID + " = " + getIntent().getIntExtra("reportId", -1), null, null, null, null);
            if (cursor.moveToFirst()) {
                Report r = new Report(cursor);
                cursor.close();
                goToDetailView(r);
            } else { Log.e("Main activity", "Cursor is empty..."); }
        }
    }

    public void goToNewReport(View view) {
        goToNewReport();
    }
    public void goToNewReport() {
        Intent intent = new Intent();
        intent.setClass(this, NewReportActivity.class);
        startActivity(intent);
    }

    public void goToUpdateReport(int parentReportId){
        Intent intent = new Intent();
        intent.putExtra("parentReportId", parentReportId);
        intent.putExtra("title", "Update Report");
        intent.setClass(this, NewReportActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        supportInvalidateOptionsMenu();
        // if (!loginManagerFragment.isLoggedIn(this))
        //     showLoginManager();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (detailFragment != null && detailFragment.isAdded())
            getSupportFragmentManager().putFragment(bundle, DETAIL_TAG, detailFragment);
        if (loginManagerFragment != null && loginManagerFragment.isAdded())
            getSupportFragmentManager().putFragment(bundle, LOGIN_TAG, loginManagerFragment);
        if (newsFeedFrag != null && newsFeedFrag.isAdded())
            getSupportFragmentManager().putFragment(bundle, NEWSFEED_TAG, newsFeedFrag);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == FACEBOOK_LOGIN)
            Toast.makeText(this, "Facebook user logged in", Toast.LENGTH_SHORT).show();
        else if (resultCode == RESULT_OK && requestCode == GOOGLE_PLUS_LOGIN)
            Toast.makeText(this, "Google+ user logged in", Toast.LENGTH_SHORT).show();
        if (resultCode == RESULT_OK) {
            registerWithGcm();
            goToFeed(null);
        } else
            showLoginManager();
    }

    public LoginManagerFragment initializeLoginManager() {
        LoginManagerFragment loginManagerFragment = (LoginManagerFragment) getSupportFragmentManager().findFragmentByTag(LOGIN_TAG);
        if (loginManagerFragment == null) {
            loginManagerFragment = new LoginManagerFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(loginManagerFragment, LOGIN_TAG)
                    .commit();
        }
        return loginManagerFragment;
    }

    @Override
    public void startLoginActivity(String network) {
        if (network.equals("google"))
            startActivityForResult(new Intent(this,GooglePlusActivity.class), GOOGLE_PLUS_LOGIN);
        else if (network.equals("facebook"))
            startActivityForResult(new Intent(this,FacebookActivity.class), FACEBOOK_LOGIN);
    }

    public void showLoginManager() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, loginManagerFragment, LOGIN_TAG)
                .commit();
    }
}
