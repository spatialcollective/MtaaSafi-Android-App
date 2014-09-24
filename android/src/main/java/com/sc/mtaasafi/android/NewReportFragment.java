package com.sc.mtaasafi.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        pics = new HashMap();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_report, container, false);
        title = (EditText) view.findViewById(R.id.reportTitle);
        details = (EditText) view.findViewById(R.id.reportDetails);
        report = (Button) view.findViewById(R.id.reportButton);
        report.setClickable(false);
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
    //
}
