package com.sc.mtaasafi.android.feed.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.feed.MainActivity;

import org.w3c.dom.Text;

/**
 * Created by Agree on 11/28/2014.
 */
public class OnboardNewReportFragment extends Fragment implements Animation.AnimationListener{
    RelativeLayout describe, photograph, send;
    TextView saveReports;
    Animation fadeIn;
    int scene;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        super.onCreateView(inflater, container, savedState);
        View view = inflater.inflate(R.layout.fragment_onboarding_newreports, container, false);
        scene = 0;
        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedState){
        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(500);
        fadeIn.setStartOffset(850);
        fadeIn.setAnimationListener(this);
        ImageView logo = (ImageView) view.findViewById(R.id.newReportIcon);
        logo.getLayoutParams().height = ((MainActivity) getActivity()).getScreenWidth() / 3;
        logo.getLayoutParams().width = ((MainActivity) getActivity()).getScreenWidth() / 3;
        logo.requestLayout();
        describe = (RelativeLayout) view.findViewById(R.id.describeProblem);
        photograph = (RelativeLayout) view.findViewById(R.id.takePhotos);
        send = (RelativeLayout) view.findViewById(R.id.sendReport);
        saveReports = (TextView) view.findViewById(R.id.canSendSave);
    }
    @Override
    public void setUserVisibleHint(boolean userVisible){
        super.setUserVisibleHint(userVisible);
        if(userVisible && getView() != null){
            describe.startAnimation(fadeIn);
            scene = 0;
        } else if(getView() != null){
            describe.setVisibility(View.INVISIBLE);
            photograph.setVisibility(View.INVISIBLE);
            send.setVisibility(View.INVISIBLE);
            saveReports.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onAnimationEnd(Animation animation) {
        switch(scene){
            case 0:
                describe.setVisibility(View.VISIBLE);
                describe.clearAnimation();
                photograph.startAnimation(fadeIn);
                break;
            case 1:
                photograph.clearAnimation();
                photograph.setVisibility(View.VISIBLE);
                send.startAnimation(fadeIn);
                break;
            case 2:
                send.clearAnimation();
                send.setVisibility(View.VISIBLE);
                saveReports.startAnimation(fadeIn);
                break;
            case 3:
                saveReports.setVisibility(View.VISIBLE);
                saveReports.clearAnimation();
                break;
        }
        scene++;
    }
    @Override public void onAnimationRepeat(Animation animation) {}
    @Override public void onAnimationStart(Animation animation) {}
}
