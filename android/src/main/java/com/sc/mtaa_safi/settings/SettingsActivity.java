package com.sc.mtaa_safi.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sc.mtaa_safi.R;

public class SettingsActivity extends PreferenceActivity {
    public static String NEW = "pref_notify_new", COMMENTS = "pref_notify_comments", VOTES = "pref_notify_votes";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void setContentView(int layoutResID) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.settings, new LinearLayout(this), false);
        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById(R.id.content_wrapper);
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);
        setContentView(contentView);
    }

    public void done(View v) { finish(); }
}