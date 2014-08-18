package com.sc.mtaasafi.android;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

public class LoginActivity extends FragmentActivity {
    private LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null){
            loginFragment = new LoginFragment();
            getSupportFragmentManager()
            .beginTransaction()
            .add(android.R.id.content, loginFragment)
            .commit();
        } else {
            loginFragment = (LoginFragment) getSupportFragmentManager()
             .findFragmentById(android.R.id.content);
        }
    }
}
