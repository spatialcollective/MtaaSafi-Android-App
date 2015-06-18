package com.sc.mtaa_safi.SystemUtils;

import android.content.Context;

import com.sc.mtaa_safi.R;

/**
 * Created by ishuah on 6/18/15.
 */
public final class URLConstants {
    public static final String REPORT_GET_URL = "v1/report/?format=json&limit=0",
                                REPORT_POST_URL = "add_post/",
                                REPORT_STREAM_URL = "add_post_from_stream/",
                                COMMENT_POST_URL = "post_comments/",
                                UPVOTE_POST_URL = "post_upvotes/",
                                LOCATION_GET_URL = "get_location_data/",
                                SIGN_IN_URL = "sign_in_user/",
                                TAG_GET_URL = "v1/tag/?format=json",
                                HISTORY_GET_URL = "get_report_history/";

    public static String buildURL(Context context, String endpoint){
        return context.getString(R.string.base_url) + endpoint;
    }
}
