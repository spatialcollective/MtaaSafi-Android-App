package com.sc.mtaa_safi.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.internal.SessionTracker;
import com.facebook.internal.Utility;
import com.facebook.model.GraphUser;
import com.sc.mtaa_safi.SystemUtils.Utils;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FacebookActivity extends Activity {
    private static final String TAG = "FacebookActivity";
    private UiLifecycleHelper uiHelper;
    private SessionTracker mSessionTracker;
    private String mApplicationId;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState sessionState, Exception e) {
            onSessionStateChange(session, sessionState, e);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        initializeSession();
        requestLogin();
    }

    @Override
    public void onResume(){
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void initializeSession(){
        mApplicationId = Utility.getMetadataApplicationId(this);
        mSessionTracker = new SessionTracker(this, callback, null, false);
    }

    private void requestLogin() {
        final Session openSession = mSessionTracker.getOpenSession();

        if (openSession != null) {
            Toast.makeText(this, "Facebook already logged in", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Facebook already logged in");
            finish();
        }
        Session currentSession = mSessionTracker.getSession();
        if (currentSession == null || currentSession.getState().isClosed()) {
            mSessionTracker.setSession(null);
            Session session = new Session.Builder(this)
                    .setApplicationId(mApplicationId).build();
            Session.setActiveSession(session);
            currentSession = session;
            Toast.makeText(this, "Facebook session opening", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Facebook session opening");
        }

        if (!currentSession.isOpened()) {
            Session.OpenRequest openRequest = null;
            openRequest = new Session.OpenRequest(this);

            openRequest.setDefaultAudience(SessionDefaultAudience.EVERYONE);
            openRequest.setPermissions(Arrays.asList("public_profile"));
            openRequest.setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);

            currentSession.openForRead(openRequest);
            Toast.makeText(this, "Facebook session open", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Facebook session open");

        }
    }

    private void getUserData(Session session){
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser graphUser, Response response) {
                if (graphUser != null) {
                    Utils.setFacebookId(getApplicationContext(), graphUser.getId());
                    //check for early adopters' email
                    //Facebook does not have email in user_profile
                    String useremail = Utils.getUserName(getApplicationContext());
                    Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
                    Matcher m = p.matcher(useremail);
                    if(m.matches())
                        Utils.saveEmail(getApplicationContext(), useremail);

                    //graphUser.getUsername() doesn't work
                    Utils.saveUserName(getApplicationContext(), graphUser.getFirstName()+" "+graphUser.getLastName());
                    Log.i(TAG, String.valueOf(graphUser));
                    beamUpUserData(getApplicationContext());
                }
            }
        }).executeAsync();
    }

    private void beamUpUserData(Context context){
        JSONObject userdata = new JSONObject();
        try {
            userdata.put("network", "facebook");
            userdata.put("social_id", Utils.getFacebookId(context));
            userdata.put("name", Utils.getUserName(context));
            if(!Utils.getEmail(context).equals(""))
                userdata.put("email", Utils.getEmail(context));
            UserDataUploader uploader = new UserDataUploader(context, userdata);
            uploader.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception){
        Log.i(TAG, "onsessionstatechange "+state);
        if (state.isOpened()){
            Utils.setSignInStatus(getApplicationContext(),true);
            getUserData(session);
            Toast.makeText(this, "Facebook user is signed in", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Facebook user is signed in");
            finish();
        }
        else if (state.isClosed()){
            Toast.makeText(this, "Facebook user is signed out", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Facebook user is signed out");
            finish();
        }
    }
}
