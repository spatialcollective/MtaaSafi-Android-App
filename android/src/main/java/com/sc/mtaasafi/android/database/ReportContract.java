package com.sc.mtaasafi.android.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ReportContract {
    private ReportContract() { }
    
    public static final String CONTENT_AUTHORITY = "com.sc.mtaasafi.android";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_ENTRIES = "entries";
    
    public static class Entry implements BaseColumns {
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENTRIES).build();
        public static final String TABLE_NAME = "reports",
        	COLUMN_ID = "_id",
        	COLUMN_ENTRY_ID = "unique_id",
        	COLUMN_TITLE = "title",
	        COLUMN_DETAILS = "details",
	        COLUMN_TIMESTAMP = "timestamp",
            COLUMN_LAT = "latitude",
            COLUMN_LNG = "longitude",
            COLUMN_USERNAME = "user",
            COLUMN_PICS = "picPaths",
            COLUMN_MEDIAURL1 = "mediaUrl1",
            COLUMN_MEDIAURL2 = "mediaUrl2",
            COLUMN_MEDIAURL3 = "mediaUrl3";
    }
}
