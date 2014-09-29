package com.sc.mtaasafi.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.androidquery.AQuery;

public class NewReportFragment extends Fragment {
    MainActivity mActivity;
    EditText title, details;
    
    Button reportBtn, galleryPicBtn, cameraPicBtn;
    ImageView picPreview;
    byte[] pic;
    boolean isTitleEmpty, isDetailEmpty;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        isTitleEmpty = isDetailEmpty = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_report, container, false);
        reportBtn = (Button) view.findViewById(R.id.reportButton);
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { sendReport(); }
        });

        reportBtn.setClickable(false);
        picPreview = (ImageView) view.findViewById(R.id.picturePreview);

        title = (EditText) view.findViewById(R.id.newReportTitle);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty())
                    isTitleEmpty = false;
                else
                    isTitleEmpty = true;
                attemptEnableReport();
            }
        });

        details = (EditText) view.findViewById(R.id.newReportDetails);
        details.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty())
                    isDetailEmpty = false;
                else
                    isDetailEmpty = true;
                attemptEnableReport();
            }
        });

//        galleryPicBtn = (Button) view.findViewById(R.id.picFromGallery);
//        galleryPicBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sendReport();
//            }
//        });
        cameraPicBtn = (Button) view.findViewById(R.id.picFromCam);
        cameraPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { mActivity.takePicture(); }
        });
        return view;
    }

    public void sendReport() {
        mActivity.beamItUp(createNewReport());
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(
                mActivity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(details.getWindowToken(), 0);
        mActivity.goToFeed();
    }

    public Report createNewReport() {
        String titleString = title.getText().toString();
        String detailsString = details.getText().toString();

        Report newReport = new Report(titleString, detailsString, mActivity.mUsername, mActivity.getLocation());
        if (pic != null)
            newReport.addPic(pic);

        return newPost;
    }

    // called when the edit texts' listeners detect a change in their texts
    public void attemptEnableReport() {
       reportBtn.setClickable(!isTitleEmpty && !isDetailEmpty);
       if (!isTitleEmpty && !isDetailEmpty)
           reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_clickable));
       else
           reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_unclickable));
    }

    public void onPhotoTaken(byte[] pic){
        this.pic = pic;
        Bitmap bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length);
        AQuery aq = new AQuery(getActivity());
        aq.id(picPreview).image(bmp);
    }
}
