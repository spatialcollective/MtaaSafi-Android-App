package com.sc.mtaa_safi.feed.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.sc.mtaa_safi.feed.MainActivity;
import com.sc.mtaa_safi.R;

/**
 * Created by Agree on 11/28/2014.
 */
public class OnboardDoneFragment extends Fragment {
    Animation fadeIn;
    ImageView doneButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        super.onCreateView(inflater, container, savedState);
        View view = inflater.inflate(R.layout.fragment_onboarding_done, container, false);
        fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(500);
        fadeIn.setStartOffset(500);
        doneButton = (ImageView) view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).goToFeed();
                ((MainActivity) getActivity()).getSupportActionBar().show();
                getParentFragment().setRetainInstance(false);
            }
        });
        return view;
    }
}
