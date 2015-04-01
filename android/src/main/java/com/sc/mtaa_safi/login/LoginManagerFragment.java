package com.sc.mtaa_safi.login;

import android.app.Activity;
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
import android.widget.Button;

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

public class LoginManagerFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "LoginManager";
    protected Button mGooglePlusButton, mFacebookButton;
    private static final String FACEBOOK_LOGIN = "facebook", GOOGLEPLUS_LOGIN = "google";
    private LoginActivityListener loginActivityListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        mGooglePlusButton = (Button) view.findViewById(R.id.button_googleplus);
        mFacebookButton = (Button) view.findViewById(R.id.button_facebook);

        mGooglePlusButton.setOnClickListener(this);
        mFacebookButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_googleplus:
                loginActivityListener.startLoginActivity(GOOGLEPLUS_LOGIN);
                break;
            case R.id.button_facebook:
                loginActivityListener.startLoginActivity(FACEBOOK_LOGIN);
                break;
        }
    }

    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            loginActivityListener = (LoginActivityListener) activity;
        }catch (ClassCastException exception){
            Log.e(TAG, "MainActivity does not implement LoginActivityListener");
        }
    }

    public boolean isLoggedIn(Context context){
        Log.i(TAG, "Checking if user is logged in");
        boolean status = Utils.getSignInStatus(context);
        Log.e(TAG, String.valueOf(status));
        return status;
    }

}
