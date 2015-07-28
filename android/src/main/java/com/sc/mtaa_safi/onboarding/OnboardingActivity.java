package com.sc.mtaa_safi.onboarding;

import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.feed.MainActivity;
import com.sc.mtaa_safi.R;

public class OnboardingActivity extends AppIntro {

    // Please DO NOT override onCreate. Use init
    @Override
    public void init(Bundle savedInstanceState) {

        // Add your slide's fragments here
        // AppIntro will automatically generate the dots indicator and buttons.
//        addSlide(first_fragment);
//        addSlide(second_fragment);

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest
        addSlide(OnboardingFragment.newInstance(R.layout.onboarding_overview));
        addSlide(OnboardingFragment.newInstance(R.layout.onboarding_new_report));
        addSlide(OnboardingFragment.newInstance(R.layout.onboarding_voting));
        addSlide(OnboardingFragment.newInstance(R.layout.onboarding_nav));
        addSlide(OnboardingFragment.newInstance(R.layout.onboarding_refresh));

        // OPTIONAL METHODS
        // Override bar/separator color
        setBarColor(getResources().getColor(R.color.mtaa_safi_blue));
//        setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button
//        showSkipButton(false);
//        showDoneButton(false);

        // Turn vibration on and set intensity
        // NOTE: you will probably need to ask VIBRATE permesssion in Manifest
//        setVibrate(true);
//        setVibrateIntensity(30);
    }

    private void loadMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSkipPressed() {
        Utils.setHasOnboarded(this);
        loadMainActivity();
    }

    @Override
    public void onDonePressed() {
        Utils.setHasOnboarded(this);
        loadMainActivity();
    }
}
