package com.sc.mtaa_safi.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class Contract {
    private Contract() { }
    
    public static final String CONTENT_AUTHORITY = "com.sc.mtaa_safi";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_ENTRIES = "entries",
                                 PATH_UPVOTES = "upvotes",
                                 PATH_COMMENTS = "comments",
                                 PATH_ADMINS = "admins",
                                 PATH_LANDMARKS = "landmarks";


    public static class Entry implements BaseColumns {
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENTRIES).build();
        public static final String TABLE_NAME = "reports",
        	COLUMN_ID = "_id",
        	COLUMN_SERVER_ID = "server_id",
        	COLUMN_HUMAN_LOC = "title",
	        COLUMN_CONTENT = "details",
	        COLUMN_TIMESTAMP = "timestamp",
            COLUMN_LAT = "latitude",
            COLUMN_LNG = "longitude",
            COLUMN_LOC_ACC = "accuracy",
            COLUMN_LOC_TIME = "loc_time",
            COLUMN_LOC_PROV = "provider",
            COLUMN_USERNAME = "user",
            COLUMN_MEDIAURL1 = "mediaUrl1",
            COLUMN_MEDIAURL2 = "mediaUrl2",
            COLUMN_MEDIAURL3 = "mediaUrl3",
            COLUMN_PENDINGFLAG = "pending",
            COLUMN_UPLOAD_IN_PROGRESS = "uploadActive",
            COLUMN_UPVOTE_COUNT = "upvote_count",
            COLUMN_USER_UPVOTED = "upvoted";
    }

    public static class UpvoteLog implements BaseColumns{
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri UPVOTE_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_UPVOTES).build();
        public static final String TABLE_NAME = "upvotes",
            COLUMN_ID = "_id",
            COLUMN_SERVER_ID = "server_id",
            COLUMN_LAT = "latitude",
            COLUMN_LON= "longitude";
    }
    public static class Comments implements BaseColumns{
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri COMMENTS_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMMENTS).build();
        public static final String TABLE_NAME = "comments",
            COLUMN_SERVER_ID = "commentId", // comment's unique id for the server
            COLUMN_CONTENT = "comment",
            COLUMN_TIMESTAMP = "timestamp",
            COLUMN_USERNAME = "username",
            COLUMN_REPORT_ID = "reportId"; // server id of the report associated with the comment
    }

    public static class Admin implements BaseColumns{
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri ADMIN_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ADMINS).build();
        public static final String TABLE_NAME = "admins",
            COLUMN_SERVER_ID = "adminId",
            COLUMN_NAME = "admin";
    }

    public static class Landmark implements BaseColumns {
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri LANDMARK_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LANDMARKS).build();
        public static final String TABLE_NAME = "landmarks",
            COLUMN_SERVER_ID = "landmarkId",
            COLUMN_NAME = "landmark",
            COLUMN_LONGITUDE = "longitude",
            COLUMN_LATITUDE = "latitude",
            COLUMN_FK_ADMIN = "fk_admin";
    }
}
