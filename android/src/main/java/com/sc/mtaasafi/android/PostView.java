package com.sc.mtaasafi.android;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class PostView extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);
        TextView textView = (TextView) findViewById(R.id.textView);
        Intent intent = getIntent();
        textView.setText(intent.getExtras().getString("message"));
    }
}
