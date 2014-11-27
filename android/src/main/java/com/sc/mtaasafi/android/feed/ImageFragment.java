package com.sc.mtaasafi.android.feed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sc.mtaasafi.android.R;

/**
 * Created by Agree on 11/5/2014.
 */
public class ImageFragment extends Fragment {
    String mediaPath;
    ReportDetailFragment mParent;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            mediaPath = getArguments().getString("mediaPath");
        mParent = (ReportDetailFragment) getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_report_image, container, false);
        ImageView reportDetailImage = (ImageView) view.findViewById(R.id.report_detail_image);
        if(mediaPath != null)
            mParent.aq.id(reportDetailImage).image(mediaPath);
        reportDetailImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ReportDetailFragment) getParentFragment()).exitImageViewer();
            }
        });
        return view;
    }
}
