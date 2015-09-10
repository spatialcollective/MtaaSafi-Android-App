package com.sc.mtaa_safi.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class Contract {
    private Contract() { }
    
    public static final String CONTENT_AUTHORITY = "com.sc.mtaa_safi";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_ENTRIES = "entries",
                                 PATH_LOCATION = "locations",
                                 PATH_UPVOTES = "upvotes",
                                 PATH_COMMENTS = "comments",
                                 PATH_ADMINS = "admins",
                                 PATH_LANDMARKS = "landmarks",
                                 PATH_TAGS = "tags",
                                PATH_REPORT_TAGS = "reporttags",
                                PATH_USERS = "users",
                                PATH_GROUPS = "groups",
                                PATH_GROUP_REPORTS = "groupreports",
                                PATH_GROUP_USERS = "groupusers";
    public static String REPORTS_JOIN_LOCATIONS = "reports LEFT OUTER JOIN locations ON reports.location=locations.location_id";
    public static String REPORTS_JOIN_TAGS = "reporttags LEFT OUTER JOIN tags ON reporttags.fk_tag=tags._id ";
    public static String GROUPS_JOIN_USERS = "groupusers LEFT OUTER JOIN users ON groupusers.fk_user=users._id";
    public static String GROUP_JOIN_REPORTS = "groupreports LEFT OUTER JOIN entries ON groupreports.fk_report=entries._id";

    public static class Entry implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENTRIES).build();
        public static final String TABLE_NAME = "reports",
                COLUMN_ID = "_id",
                COLUMN_SERVER_ID = "server_id",
                COLUMN_DESCRIPTION = "description",
                COLUMN_PLACE_DESCRIPT = "name",
                COLUMN_TIMESTAMP = "timestamp",
                COLUMN_STATUS = "status",
                COLUMN_ADMIN_ID = "geo_admin",  // id
                COLUMN_USERID = "owner", // id
                COLUMN_USERNAME = "username", //owner.user.username
                COLUMN_LOCATION = "location", // misused in some places?
                COLUMN_MEDIA = "media_set",
                COLUMN_PENDINGFLAG = "pending",
                COLUMN_UPLOAD_IN_PROGRESS = "uploadActive",
                COLUMN_UPVOTE_COUNT = "upvote_count",
                COLUMN_USER_UPVOTED = "upvoted",
            	COLUMN_PARENT_REPORT = "parent_id";
    }

    public static class MtaaLocation implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri LOCATION_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();
        public static final String TABLE_NAME = "locations",
                COLUMN_ID = "location_id",
                COLUMN_LAT = "latitude",
                COLUMN_LNG= "longitude",
                COLUMN_LOC_ACC = "accuracy",
                COLUMN_LOC_TIME = "loc_time",
                COLUMN_LOC_PROV = "provider";
    }

    public static class UpvoteLog implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri UPVOTE_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_UPVOTES).build();
        public static final String TABLE_NAME = "upvotes",
                COLUMN_ID = "_id",
                COLUMN_SERVER_ID = "server_id",
                COLUMN_LAT = "latitude",
                COLUMN_LON= "longitude";
    }

    public static class Comments implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.comments";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.comment";
        public static final Uri COMMENTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMMENTS).build();
        public static final String TABLE_NAME = "comments",
                COLUMN_SERVER_ID = "commentId", // comment's unique id for the server
                COLUMN_CONTENT = "comment",
                COLUMN_TIMESTAMP = "timestamp",
                COLUMN_USERNAME = "username",
                COLUMN_REPORT_ID = "reportId"; // server id of the report associated with the comment
    }

    public static class Admin implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri ADMIN_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ADMINS).build();
        public static final String TABLE_NAME = "admins",
                COLUMN_SERVER_ID = "_id",
                COLUMN_NAME = "admin";
    }

    public static class Landmark implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri LANDMARK_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LANDMARKS).build();
        public static final String TABLE_NAME = "landmarks",
                COLUMN_SERVER_ID = "landmarkId",
                COLUMN_NAME = "landmark",
                COLUMN_LONGITUDE = "longitude",
                COLUMN_LATITUDE = "latitude",
                COLUMN_FK_ADMIN = "fk_admin";
    }

    public static class Tag implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri TAG_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TAGS).build();
        public static final String TABLE_NAME = "tags",
                COLUMN_SERVER_ID = "serverId",
                COLUMN_NAME = "tag";
    }

    public static class ReportTagJunction implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri REPORT_TAG_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REPORT_TAGS).build();
        public static final String TABLE_NAME = "reporttags",
                COLUMN_FK_REPORT = "fk_report",
                COLUMN_FK_TAG = "fk_tag";
    }

    public static class User implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri USER_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_USERS).build();
        public static final String TABLE_NAME = "users",
                COLUMN_SERVER_ID = "serverId",
                COLUMN_NAME = "name";
    }

    public static class Group implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri GROUP_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GROUPS).build();
        public static final String TABLE_NAME = "groups",
                COLUMN_SERVER_ID = "serverId",
                COLUMN_NAME = "name",
                COLUMN_DESCRIPTION = "description";
    }

    public static class GroupUserJunction implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri GROUP_USER_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GROUP_USERS).build();
        public static final String TABLE_NAME = "groupusers",
                COLUMN_FK_USER = "fk_user",
                COLUMN_FK_GROUP = "fk_group",
                COLUMN_ADMIN = "is_admin";
    }

    public static class GroupReportJunction implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.entry";
        public static final Uri GROUP_REPORT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GROUP_REPORTS).build();
        public static final String TABLE_NAME = "groupreports",
                COLUMN_FK_REPORT = "fk_report",
                COLUMN_FK_GROUP = "fk_group";
    }

}
