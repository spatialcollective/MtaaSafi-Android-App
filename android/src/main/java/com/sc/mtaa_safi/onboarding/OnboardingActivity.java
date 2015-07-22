package com.sc.mtaa_safi.onboarding;

import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.feed.MainActivity;

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
        addSlide(AppIntroFragment.newInstance("Use Mtaa Safi to report about and discuss improvement of your local communtiy", "",
                R.drawable.overview, R.color.mtaa_safi_blue));
        addSlide(AppIntroFragment.newInstance("Create new reports by tapping the '+'<image> at the bottom", "",
                R.drawable.new_report, R.color.mtaa_safi_blue));
        addSlide(AppIntroFragment.newInstance("Tell people what's important to you by Up-Voting <image>", "",
                R.drawable.voting, R.color.mtaa_safi_blue));
        addSlide(AppIntroFragment.newInstance("View other communities by pressing '='<image> or swiping right", "",
                R.drawable.nav, R.color.mtaa_safi_blue));
        addSlide(AppIntroFragment.newInstance("Check for new reports by swiping down", "",
                R.drawable.refresh, R.color.mtaa_safi_blue));

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
