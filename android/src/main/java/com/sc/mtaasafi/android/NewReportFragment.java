package com.sc.mtaasafi.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.ArrayList;
import java.util.List;

public class NewReportFragment extends Fragment {
    MainActivity mActivity;
    DescriptionEditText details;
    TextView attachPicsTV;
    Button reportBtn;
    ImageView[] picPreviews;
    ArrayList<String> pics;
    String detailsString;
    int lastPreviewClicked;

    private final int PIC1 = 0,
            PIC2 = PIC1 + 1,
            PIC3 = PIC2 + 1,
            TOTAL_PICS = PIC3 + 1;

    private final String DEETS_KEY = "details",
            picsKey = "pics",
            LASTPREVIEW_KEY = "last_preview";

    AQuery aq;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mActivity = (MainActivity) getActivity();
        aq = new AQuery(mActivity);
        pics = new ArrayList<String>();
        picPreviews = new ImageView[TOTAL_PICS];
        Log.e(LogTags.NEWREPORT, "OnCreate " + this.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedState) {
        // Inflate the layout for this fragment
        Log.e(LogTags.NEWREPORT, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_new_report, container, false);
        details = (DescriptionEditText) view.findViewById(R.id.newReportDetails);
        picPreviews[0] = (ImageView) view.findViewById(R.id.pic1);
        picPreviews[1] = (ImageView) view.findViewById(R.id.pic2);
        picPreviews[2] = (ImageView) view.findViewById(R.id.pic3);

        reportBtn = (Button) view.findViewById(R.id.reportButton);
        reportBtn.setClickable(false);

        attachPicsTV = (TextView) view.findViewById(R.id.attachMorePicturesText);
        setListeners();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        Log.e(LogTags.NEWREPORT, "onActivityCreated");

        if (savedState != null) {
            detailsString = savedState.getString(DEETS_KEY);
            if (detailsString != null) {
                details.setText(detailsString);
            }
            lastPreviewClicked = savedState.getInt(LASTPREVIEW_KEY);
//            restorePics(savedState.getStringArrayList(picsKey));
            restorePics();
            Log.e(LogTags.NEWREPORT, "onActivityCreated: lastPreviewClicked: " + lastPreviewClicked);

        } else {
            for (int i = 0; i < TOTAL_PICS; i++) {
                pics.add(null);
            }
        }

        Bundle args = getArguments();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e(LogTags.NEWREPORT, "onResume");
        restorePics();
//        if (mCurrentPhotopath != null) {
//            Log.e(LogTags.NEWREPORT, "Current photo path: " + mCurrentPhotopath);
//            onPhotoTaken(mCurrentPhotopath);
////            setArguments(null);
//        }
    }
    private void restorePics() {
        pics = mActivity.getPics();
        for (int i = 0; i < TOTAL_PICS; i++) {
            if (pics.get(i)!= null) {
                // decode byte[] from string, add to pics list, create a thumb from the byte[],
                // add it to the preview.
                aq.id(picPreviews[i]).image(getThumbnail(pics.get(i)));
            }
        }
        determineEmptyPicsText();
    }

    private void setListeners() {
        details.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                attemptEnableReport();
            }
        });

        picPreviews[PIC1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPicPreviewClicked(PIC1);
            }
        });
        picPreviews[PIC2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPicPreviewClicked(PIC2);
            }
        });
        picPreviews[PIC3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPicPreviewClicked(PIC3);
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendReport();
            }
        });
    }

    public void onPicPreviewClicked(int previewClicked) {
        lastPreviewClicked = previewClicked;
//        setRetainInstance(true);
        mActivity.takePicture((NewReportFragment) getParentFragment(), lastPreviewClicked);
    }

    public void sendReport() {
        mActivity.beamItUp(createNewReport());
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(
                mActivity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(details.getWindowToken(), 0);
        mActivity.goToFeed();
    }

    public Report createNewReport() {
        // TODO: figure out how to manage pics in report class
        return new Report(details.getText().toString(), mActivity.mUsername, mActivity.getLocation(),
                null);
    }

    // called when the edit texts' listeners detect a change in their texts
    public void attemptEnableReport() {
        boolean hasEmptyPics = false;
        if (pics != null && !pics.isEmpty()) {
            for (int i = 0; i < TOTAL_PICS; i++) {
                if (pics.get(i) == null) {
//                    Log.e(LogTags.NEWREPORT, "pic" + i + " is null");
                    hasEmptyPics = true;
                    break;
                }
            }
        }
//        Log.e(LogTags.NEWREPORT, "Do u have details: " + !details.getText().toString().isEmpty()
//                + ". Have all pics: " + !hasEmptyPics);
        if (!details.getText().toString().isEmpty() && !hasEmptyPics) {
            reportBtn.setClickable(true);
            reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_clickable));
        } else {
            reportBtn.setClickable(false);
            reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_unclickable));
        }
    }

    // Returns 100x100px thumbnail to populate picPreviews.
    private Bitmap getThumbnail(String picPath) {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(picPath), 100, 100, true);
    }

    private void determineEmptyPicsText() {
        int emptyPics = 0;
        for (int i = 0; i < pics.size(); i++) {
            if (pics.get(i) == null)
                emptyPics++;
        }
        if (emptyPics > 1)
            attachPicsTV.setText("Attach " + emptyPics + " more pictures:");
        else if (emptyPics == 1)
            attachPicsTV.setText("Attach " + emptyPics + " more picture:");
        else
            attachPicsTV.setVisibility(View.INVISIBLE);
        attemptEnableReport();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DEETS_KEY, details.getText().toString());
        outState.putInt(LASTPREVIEW_KEY, lastPreviewClicked);
    }

}