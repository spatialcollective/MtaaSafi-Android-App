package com.sc.mtaasafi.android.SystemUtils;

import android.app.Activity;
import android.content.Context;

/**
 * Created by Agree on 10/27/2014.
 */
public class PrefUtils {
    public static final String  USERNAME = "username",
                                LAT = "lat",
                                LON = "lon",
                                LOCATION = "location",
                                LOCATION_TIMESTAMP = "loc_tstamp",
                                SCREEN_WIDTH = "swidth";

    private static final String PREF_KEY = "myPrefs";

    public static ComplexPreferences getPrefs(Context context){
        return ComplexPreferences.getComplexPreferences(context, PREF_KEY, Activity.MODE_PRIVATE);
    }
    public static int getTimeSinceInMinutes(float since){
        float diffMillis = System.currentTimeMillis() - since;
        float diffSeconds = diffMillis/1000;
        float diffMinutes = diffSeconds/60;
        return (int) diffMinutes;
    }
}
