package com.sc.mtaasafi.android.feed;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CompoundButton;

public class VoteButton extends CompoundButton {
    public int serverId;

    public VoteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnCheckedChangeListener(new MyListener());
    }

    private class MyListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            Log.e("Vote Button", "totes clicked that shit");
        }
    }
}
