package com.sc.mtaa_safi.newReport;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.CompletionInfo;
import android.widget.AutoCompleteTextView;

/**
 * Created by lenovo on 12/2/2014.
 */
public class SafiAutoCompleteText extends AutoCompleteTextView {
    NewReportFragment nrf;
    public SafiAutoCompleteText(Context context, AttributeSet attrs) {
        super(context, attrs);

    }
    @Override
    public void onCommitCompletion(CompletionInfo info){
        super.onCommitCompletion(info);
        info.getText();
        Log.e("Autocomplete", info.getText().toString());
    }

}