package com.sc.mtaasafi.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 */
public class NewReportFragment extends Fragment {
    MainActivity mActivity;
    EditText title, details;
    Button report, picFromGallery, picFromCamera;
    GridView gv;
    HashMap pics;
    boolean isTitleEmpty, isDetailEmpty;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        pics = new HashMap();
        isTitleEmpty = isDetailEmpty = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_report, container, false);
        title = (EditText) view.findViewById(R.id.reportTitle);
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

        details = (EditText) view.findViewById(R.id.reportDetails);
        report = (Button) view.findViewById(R.id.reportButton);
        report.setClickable(false);
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

        picFromGallery = (Button) view.findViewById(R.id.picFromGallery);
        picFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
            }
        });
        picFromCamera = (Button) view.findViewById(R.id.picFromCam);
        picFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.takePicture();
            }
        });
        return view;
    }
    public void addPic(int id, byte[] pic){
        pics.put(id, pic);
    }
    public void removePic(int id){
        pics.remove(id);
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
}
