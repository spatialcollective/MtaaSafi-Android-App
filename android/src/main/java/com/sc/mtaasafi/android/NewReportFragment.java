package com.sc.mtaasafi.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
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
import android.widget.GridView;
import android.widget.ImageView;

import com.androidquery.AQuery;

import java.text.SimpleDateFormat;


/**
 *
 */
public class NewReportFragment extends Fragment {
    MainActivity mActivity;
    EditText title, details;
    Button report, picFromGallery, picFromCamera;
    ImageView picPreview;
    byte[] pic;
    boolean isTitleEmpty, isDetailEmpty;
    String userName;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        isTitleEmpty = isDetailEmpty = true;
        userName = mActivity.mUsername;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_report, container, false);
        report = (Button) view.findViewById(R.id.reportButton);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendReport();
            }
        });
        report.setClickable(false);
        picPreview = (ImageView) view.findViewById(R.id.picturePreview);

        title = (EditText) view.findViewById(R.id.newReportTitle);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){
                    isTitleEmpty = false;
                }
                else{
                    isTitleEmpty = true;
                }
                attemptEnableReport();
            }
        });

        details = (EditText) view.findViewById(R.id.newReportDetails);
        details.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){
                    isDetailEmpty = false;
                }
                else{
                    isDetailEmpty = true;
                }
                attemptEnableReport();
            }
        });

//        picFromGallery = (Button) view.findViewById(R.id.picFromGallery);
//        picFromGallery.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sendReport();
//            }
//        });
        picFromCamera = (Button) view.findViewById(R.id.picFromCam);
        picFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.takePicture();
            }
        });
        return view;
    }

    public void sendReport(){
        String reportTitle = title.getText().toString();
        String reportDetails = details.getText().toString();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss")
                .format(new java.util.Date(System.currentTimeMillis()));
        Location location = mActivity.getLocation();
        if (pic != null) {
            mActivity.beamItUp(new PostData(userName, timestamp, location.getLatitude(),
                    location.getLongitude(), reportTitle, reportDetails, pic));
        } else {
            mActivity.beamItUp(new PostData(userName, timestamp, location.getLatitude(),
                    location.getLongitude(), reportTitle, reportDetails));
        }
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(
                mActivity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(details.getWindowToken(), 0);
        mActivity.goToFeed();
    }

    // called when the edit texts' listeners detect a change in their texts
    public void attemptEnableReport(){
       report.setClickable(!isTitleEmpty && !isDetailEmpty);
       if(!isTitleEmpty && !isDetailEmpty){
           report.setClickable(true);
           report.setBackgroundColor(getResources().getColor(R.color.report_button_clickable));
       }
       else{
           report.setClickable(false);
           report.setBackgroundColor(getResources().getColor(R.color.report_button_unclickable));
       }
    }
    public void onPhotoTaken(byte[] pic){
        this.pic = pic;
        Bitmap bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length);
        AQuery aq = new AQuery(getActivity());
        aq.id(picPreview).image(bmp);
    }
}
