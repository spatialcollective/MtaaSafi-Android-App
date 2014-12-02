package com.sc.mtaasafi.android.newReport;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.CompletionInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;

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
