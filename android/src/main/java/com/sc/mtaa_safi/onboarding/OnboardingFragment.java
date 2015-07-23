package com.sc.mtaa_safi.onboarding;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OnboardingFragment extends Fragment {
    private static final String ARG_LAYOUT = "layout";
    private int layout;

    public OnboardingFragment() {}

    public static OnboardingFragment newInstance(int layout) {
        OnboardingFragment sampleSlide = new OnboardingFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT, layout);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().size() != 0)
            layout = getArguments().getInt(ARG_LAYOUT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layout, container, false);
    }
}
