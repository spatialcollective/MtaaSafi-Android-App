package com.sc.mtaa_safi.login;

import android.app.Activity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.sc.mtaa_safi.MtaaLocationService;
import com.sc.mtaa_safi.SystemUtils.Utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GooglePlusActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 101;
    private GoogleApiClient mGoogleApiClient;

    private boolean mIntentInProgress, mSignInClicked = true;

    private MtaaLocationService mBoundService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((MtaaLocationService.LocalBinder) service).getService();
        }
        public void onServiceDisconnected(ComponentName className) { mBoundService = null; } // This should never happen
    };
    void bindLocationService() {
        bindService(new Intent(this, MtaaLocationService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    @Override
    public void onStart(){
        super.onStart();
        bindLocationService();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop(){
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        unbindService(mConnection);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Utils.setSignInStatus(getApplicationContext(), true);
        getProfileInformation();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!mIntentInProgress && connectionResult.hasResolution()) {
            if (mSignInClicked){
                try {
                    mIntentInProgress = true;
                    startIntentSenderForResult(connectionResult.getResolution().getIntentSender(),
                            RC_SIGN_IN, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }

        }
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        Log.i("GooglePlusActivity activityresult", String.valueOf(responseCode));
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }
            mIntentInProgress = false;
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    private void beamUpUserData(Context context){
        JSONObject userdata = new JSONObject();
        try {
            userdata.put("network", "google");
            userdata.put("social_id", Utils.getGooglePlusId(context));
            userdata.put("name", Utils.getUserName(context));
            if(!Utils.getEmail(context).equals(""))
                userdata.put("email", Utils.getEmail(context));
            UserDataUploader uploader = new UserDataUploader(context, userdata);
            uploader.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personId = currentPerson.getId();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                Utils.setGooglePlusId(getApplicationContext(), personId);
                Utils.saveUserName(getApplicationContext(), personName);
                Utils.saveEmail(getApplicationContext(), email);

                beamUpUserData(getApplicationContext());

                Log.e("GooglePlusActivity", "Name: " + personName + ", email: " + email+
                    "ID: "+personId);
                Toast.makeText(this, personName+" "+email, Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
