package com.sc.mtaasafi.android.newReport;

/**
 * Created by Agree on 10/7/2014.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

// An EditText that lets you use actions ("Done", "Go", etc.) on multi-line edits.
public class SafiEditText extends EditText{
    public SafiEditText(Context context)
    {
        super(context);
    }

    public SafiEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SafiEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        InputConnection conn = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return conn;
    }
}