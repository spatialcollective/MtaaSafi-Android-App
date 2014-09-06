package com.sc.mtaasafi.android;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.facebook.SessionLoginBehavior;
import com.facebook.widget.LoginButton;
import java.util.Arrays;

public class AccountsFragment extends Fragment {

    public AccountsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.accounts, container, false);
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.authButton);
        loginButton = (LoginButton) view.findViewById(R.id.authButton);
        loginButton.setFragment(this);
        loginButton.setPublishPermissions(Arrays.asList("publish_actions"));
        return view;
    }
}
