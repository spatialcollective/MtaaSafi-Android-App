package com.sc.mtaa_safi.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ReportDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 32;

    private static final String DATABASE_NAME = "mtaasafi.db";
    private static final String REPORT_TABLE_CREATE = "create table "
            + Contract.Entry.TABLE_NAME + "("
            + Contract.Entry.COLUMN_ID + " integer primary key autoincrement, "
            + Contract.Entry.COLUMN_SERVER_ID + " integer, "
            + Contract.Entry.COLUMN_DESCRIPTION + " text not null, "
            + Contract.Entry.COLUMN_PLACE_DESCRIPT + " text not null, "
            + Contract.Entry.COLUMN_TIMESTAMP + " long, "
            + Contract.Entry.COLUMN_STATUS + " integer default 0, "
            + Contract.Entry.COLUMN_ADMIN_ID + " integer default 0, "
            + Contract.Entry.COLUMN_USERID + " integer not null, "
            + Contract.Entry.COLUMN_USERNAME + " text not null, "
            + Contract.Entry.COLUMN_LOCATION + " integer not null, "
            + Contract.Entry.COLUMN_MEDIA + " text not null, "
            + Contract.Entry.COLUMN_UPVOTE_COUNT + " integer default 0, "
            + Contract.Entry.COLUMN_USER_UPVOTED + " integer default 0, "
            + Contract.Entry.COLUMN_PENDINGFLAG + " integer default -1, "
            + Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS + " integer default 0, "
            + Contract.Entry.COLUMN_PARENT_REPORT + " integer default 0, "
            + "FOREIGN KEY (" + Contract.Entry.COLUMN_LOCATION + ") "
            + "REFERENCES " + Contract.MtaaLocation.TABLE_NAME + "(" + Contract.MtaaLocation._ID + "),"
            + "unique (" + Contract.Entry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE"
        + ")";

    private static final String LOCATION_TABLE_CREATE = "create table "
            + Contract.MtaaLocation.TABLE_NAME + "("
            + Contract.MtaaLocation.COLUMN_ID + " integer primary key autoincrement, "
            + Contract.MtaaLocation.COLUMN_LAT + " double not null, "
            + Contract.MtaaLocation.COLUMN_LNG + " double not null, "
            + Contract.MtaaLocation.COLUMN_LOC_ACC + " float, "
            + Contract.MtaaLocation.COLUMN_LOC_TIME + " long, "
            + Contract.MtaaLocation.COLUMN_LOC_PROV + " text "
        + ")";

    private static final String UPVOTE_TABLE_CREATE = "create table "
            + Contract.UpvoteLog.TABLE_NAME + "("
            + Contract.UpvoteLog.COLUMN_ID + " integer primary key autoincrement, "
            + Contract.UpvoteLog.COLUMN_SERVER_ID + " integer, "
            + Contract.UpvoteLog.COLUMN_LAT + " double, "
            + Contract.UpvoteLog.COLUMN_LON + " double"
        + ")";

    private static final String COMMENT_TABLE_CREATE = "create table "
            + Contract.Comments.TABLE_NAME + "("
            + Contract.Comments._ID + " integer primary key autoincrement, "
            + Contract.Comments.COLUMN_SERVER_ID + " integer not null, "
            + Contract.Comments.COLUMN_REPORT_ID + " integer not null, "
            + Contract.Comments.COLUMN_CONTENT + " text, "
            + Contract.Comments.COLUMN_TIMESTAMP + " long, "
            + Contract.Comments.COLUMN_USERNAME + " text,"
        + "unique (" + Contract.Comments.COLUMN_TIMESTAMP + ", " + Contract.Comments.COLUMN_USERNAME + ") ON CONFLICT REPLACE)";

    private static final String ADMINS_TABLE_CREATE = "create table "
            + Contract.Admin.TABLE_NAME + "("
            + Contract.Admin._ID + " integer primary key, "
            + Contract.Admin.COLUMN_NAME + " text "
        + ")";

    private static final String LANDMARKS_TABLE_CREATE = "create table "
            + Contract.Landmark.TABLE_NAME + "("
            + Contract.Landmark._ID + " integer primary key, "
            + Contract.Landmark.COLUMN_NAME + " text, "
            + Contract.Landmark.COLUMN_LONGITUDE + " real, "
            + Contract.Landmark.COLUMN_LATITUDE + " real, "
            + Contract.Landmark.COLUMN_FK_ADMIN + " integer, "
                + " FOREIGN KEY (" + Contract.Landmark.COLUMN_FK_ADMIN
                + ") REFERENCES admins(" + Contract.Admin._ID + ")"
        + ")";

    private static final String TAGS_TABLE_CREATE = "create table "
            + Contract.Tag.TABLE_NAME + "("
            + Contract.Tag._ID + " integer primary key autoincrement, "
            + Contract.Tag.COLUMN_SERVER_ID + " integer, "
            +Contract.Tag.COLUMN_NAME + " text unique "
        +")";

    private static final String REPORT_TAG_TABLE_CREATE = "create table "
            + Contract.ReportTagJunction.TABLE_NAME + "("
            + Contract.ReportTagJunction._ID  + " integer primary key autoincrement, "
            + Contract.ReportTagJunction.COLUMN_FK_REPORT + " integer, "
            + Contract.ReportTagJunction.COLUMN_FK_TAG + " integer, "
            + " UNIQUE ( "
                + Contract.ReportTagJunction.COLUMN_FK_REPORT + ", "
                + Contract.ReportTagJunction.COLUMN_FK_TAG
            + " ) ON CONFLICT REPLACE "
        +")";

    private static final String USER_TABLE_CREATE = "create table "
            + Contract.User.TABLE_NAME + "("
            + Contract.User._ID + " integer primary key autoincrement, "
            + Contract.User.COLUMN_SERVER_ID + " integer, "
            + Contract.User.COLUMN_NAME + " text unique "
        +")";

    private static final String GROUP_TABLE_CREATE = "create table "
            + Contract.Group.TABLE_NAME + "("
            + Contract.Group._ID + " integer primary key autoincrement, "
            + Contract.Group.COLUMN_SERVER_ID + " integer, "
            + Contract.Group.COLUMN_NAME + " text, "
            + Contract.Group.COLUMN_DESCRIPTION + " text "
        +")";

    private static final String GROUP_USER_TABLE_CREATE = "create table "
            + Contract.GroupUserJunction.TABLE_NAME + "("
            + Contract.GroupUserJunction._ID  + " integer primary key autoincrement, "
            + Contract.GroupUserJunction.COLUMN_FK_USER + " integer, "
            + Contract.GroupUserJunction.COLUMN_FK_GROUP + " integer, "
            + " UNIQUE ( "
            + Contract.GroupUserJunction.COLUMN_FK_USER + ", "
            + Contract.GroupUserJunction.COLUMN_FK_GROUP
            + " ) ON CONFLICT REPLACE "
            +")";

    private static final String GROUP_REPORT_TABLE_CREATE = "create table "
            + Contract.GroupReportJunction.TABLE_NAME + "("
            + Contract.GroupReportJunction._ID  + " integer primary key autoincrement, "
            + Contract.GroupReportJunction.COLUMN_FK_REPORT + " integer, "
            + Contract.GroupReportJunction.COLUMN_FK_GROUP + " integer, "
            + " UNIQUE ( "
            + Contract.GroupReportJunction.COLUMN_FK_REPORT + ", "
            + Contract.GroupReportJunction.COLUMN_FK_GROUP
            + " ) ON CONFLICT REPLACE "
            +")";

    public ReportDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(REPORT_TABLE_CREATE);
        database.execSQL(LOCATION_TABLE_CREATE);
        database.execSQL(UPVOTE_TABLE_CREATE);
        database.execSQL(COMMENT_TABLE_CREATE);
        database.execSQL(ADMINS_TABLE_CREATE);
        database.execSQL(LANDMARKS_TABLE_CREATE);
        database.execSQL(TAGS_TABLE_CREATE);
        database.execSQL(REPORT_TAG_TABLE_CREATE);
        database.execSQL(USER_TABLE_CREATE);
        database.execSQL(GROUP_TABLE_CREATE);
        database.execSQL(GROUP_USER_TABLE_CREATE);
        database.execSQL(GROUP_REPORT_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ReportDatabase.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.MtaaLocation.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.UpvoteLog.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Comments.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Admin.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Landmark.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Tag.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.ReportTagJunction.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.User.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Group.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.GroupUserJunction.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.GroupReportJunction.TABLE_NAME);
        onCreate(db);
    }
}
