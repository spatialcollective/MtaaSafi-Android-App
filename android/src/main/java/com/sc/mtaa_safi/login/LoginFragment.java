package com.sc.mtaa_safi.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.feed.NewsFeedFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public final static String NEWSFEED_TAG = "newsFeed";

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }

        if (Utils.getSignInStatus(getActivity()))
            goToFeed(savedInstanceState);
    }

    private void goToFeed(Bundle savedInstanceState){
        FragmentManager manager = getActivity().getSupportFragmentManager();
        NewsFeedFragment newsFeedFrag = null;
        if (savedInstanceState != null){
             newsFeedFrag = (NewsFeedFragment) manager.getFragment(savedInstanceState, NEWSFEED_TAG);
        }
        if (newsFeedFrag == null) {
            newsFeedFrag = new NewsFeedFragment();
            manager.beginTransaction()
                   .replace(R.id.fragment_container, newsFeedFrag, NEWSFEED_TAG)
                   .commit();
        }
    }

    private void signInUser(Session session){
        final Context context = getActivity();
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser graphUser, Response response) {
                if (graphUser != null) {
                    Utils.setUserId(context, graphUser.getId());
                    //check for early adopters' email
                    String useremail = Utils.getUserName(context);
                    Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
                    Matcher m = p.matcher(useremail);
                    if(m.matches())
                        Utils.saveEmail(context, useremail);

                    //graphUser.getUsername() doesn't work
                    Utils.saveUserName(context, graphUser.getFirstName()+" "+graphUser.getLastName());

                    beamUpUserData(context);
                }
            }
        }).executeAsync();
    }

    private void beamUpUserData(Context context){
        JSONObject userdata = new JSONObject();
        try {
            userdata.put("social_id", Utils.getUserId(context));
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
        if (state.isOpened() && !Utils.getSignInStatus(getActivity())){
            Utils.setSignInStatus(getActivity(),true);
            signInUser(session);
        }
        else if (state.isClosed()){
            Utils.setSignInStatus(getActivity(),false);
        }
    }

}
