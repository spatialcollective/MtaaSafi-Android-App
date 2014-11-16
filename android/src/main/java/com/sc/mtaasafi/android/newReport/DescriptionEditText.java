package com.sc.mtaasafi.android.newReport;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;

import com.sc.mtaasafi.android.R;

public class DescriptionEditText extends EditText {
    public static final int FLOATING_LABEL_NONE = 0;
    public static final int FLOATING_LABEL_NORMAL = 1;
    public static final int FLOATING_LABEL_HIGHLIGHT = 2;

    private float floatingLabelFraction;
    private boolean floatingLabelShown;
    private float focusFraction;

    private ArgbEvaluator focusEvaluator = new ArgbEvaluator();
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    ObjectAnimator labelAnimator;
    ObjectAnimator labelFocusAnimator;
    OnFocusChangeListener interFocusChangeListener;
    OnFocusChangeListener outerFocusChangeListener;

    public DescriptionEditText(Context context) { super(context); }
    public DescriptionEditText(Context context, AttributeSet attrs) { super(context, attrs); }

    public DescriptionEditText(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DescriptionEditText);
        setTextColor(getResources().getColor(R.color.Blue));
        typedArray.recycle();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(null);
        } else {
            setBackgroundDrawable(null);
        }

        setPadding(getPaddingLeft(), getPaddingTop() + 20, getPaddingRight(), getPaddingBottom() + 28);
        initText();
        initFloatingLabel();
    }

    private void initText() {
        if (!TextUtils.isEmpty(getText())) {
            CharSequence text = getText();
            setText(null);
            setHintTextColor(getResources().getColor(R.color.Blue));
            setText(text);
            floatingLabelFraction = 1;
            floatingLabelShown = true;
        } else {
            setHintTextColor(getResources().getColor(R.color.Violet));
            floatingLabelFraction = 0;
            floatingLabelShown = false;
        }
    }

    // use {@link #setPaddings(int, int, int, int)} instead, or the paddingTop and the paddingBottom may be set incorrectly.
    @Deprecated
    @Override
    public final void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
    }

    private void initFloatingLabel() {
            // observe the text changing
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    if (floatingLabelShown) {
                        floatingLabelShown = false;
                        getLabelAnimator().reverse();
                    }
                } else if (!floatingLabelShown) {
                    floatingLabelShown = true;
                    if (getLabelAnimator().isStarted()) {
                        getLabelAnimator().reverse();
                    } else {
                        getLabelAnimator().start();
                    }
                }
            }
        });
//        if (highlightFloatingLabel) {
//            // observe the focus state to animate the floating label's text color appropriately
//            interFocusChangeListener = new OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if (hasFocus) {
//                        if (getLabelFocusAnimator().isStarted()) {
//                            getLabelFocusAnimator().reverse();
//                        } else {
//                            getLabelFocusAnimator().start();
//                        }
//                    } else {
//                        getLabelFocusAnimator().reverse();
//                    }
//                    if (outerFocusChangeListener != null) {
//                        outerFocusChangeListener.onFocusChange(v, hasFocus);
//                    }
//                }
//            };
//            super.setOnFocusChangeListener(interFocusChangeListener);
//        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        paint.setTextSize(20);
        paint.setColor(getResources().getColor(R.color.Gray));

        int spacing = 8, textHeight = 20;
        // draw the floating label
        if (!TextUtils.isEmpty(getHint()) && !TextUtils.isEmpty(getText())) {
            // calculate the vertical position
            int start = textHeight;
            int position = (int) (start - spacing * floatingLabelFraction);

            // calculate the alpha
            int alpha = (int) (floatingLabelFraction * 0xff * (0.74f * focusFraction + 0.26f));
//            paint.setAlpha(alpha);

            // draw the floating label
            canvas.drawText(getHint().toString(), getPaddingLeft() + getScrollX(), position, paint); //getHint().toString()
        }
        // draw the original things
        super.onDraw(canvas);
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        if (interFocusChangeListener == null)
            super.setOnFocusChangeListener(listener);
        else
            outerFocusChangeListener = listener;
    }

    private ObjectAnimator getLabelAnimator() {
        if (labelAnimator == null)
            labelAnimator = ObjectAnimator.ofFloat(this, "floatingLabelFraction", 0f, 1f);
        return labelAnimator;
    }

    private ObjectAnimator getLabelFocusAnimator() {
        if (labelFocusAnimator == null)
            labelFocusAnimator = ObjectAnimator.ofFloat(this, "focusFraction", 0f, 1f);
        return labelFocusAnimator;
    }

    private int getPixel(int dp) {
        Resources r = getContext().getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return (int) px;
    }
}