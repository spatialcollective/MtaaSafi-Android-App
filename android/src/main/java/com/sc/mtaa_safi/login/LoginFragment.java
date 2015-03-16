package com.sc.mtaa_safi.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.feed.NewsFeedFragment;

import java.util.Arrays;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private UiLifecycleHelper uiHelper;
    private ProgressDialog progressDialog;
    private Session.StatusCallback callback = new Session.StatusCallback(){
        @Override
        public void call(final Session session, final SessionState state, final Exception exception){
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.fb_signin_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile"));
        loginButton.setFragment(this);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void goToFeed(){
        FragmentManager manager = getActivity().getSupportFragmentManager();
        NewsFeedFragment newsFeedFrag = new NewsFeedFragment();
        manager.beginTransaction()
                .replace(R.id.fragment_container, newsFeedFrag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void signInUser(Session session){
        final Context context = getActivity();
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Logging you in...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser graphUser, Response response) {
                if (graphUser != null) {
                    Utils.setUserId(context, graphUser.getId());
                    //graphUser.getUsername() doesn't work
                    Utils.saveUserName(context, graphUser.getFirstName()+" "+graphUser.getLastName());
                    Log.e(TAG+" userdata", Utils.getUserName(context));
                    progressDialog.dismiss();
                } else {
                    Log.i("userdata", "user null");
                }
            }
        }).executeAsync();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception){
        if (state.isOpened() && !Utils.getSignInStatus(getActivity())){
            Utils.setSignInStatus(getActivity(),true);
            signInUser(session);
            //goToFeed();
            Log.i(TAG, "Logged in");
        }
        else if (state.isClosed()){
            Utils.setSignInStatus(getActivity(),false);
            Log.i(TAG, "Logged out");
        }
    }

}
