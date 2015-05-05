package com.sc.mtaa_safi.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ReportDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 28;
    private static final String DATABASE_NAME = "mtaasafi.db";
    private static final String REPORT_TABLE_CREATE = "create table "
            + Contract.Entry.TABLE_NAME + "("
            + Contract.Entry.COLUMN_ID + " integer primary key autoincrement, "
            + Contract.Entry.COLUMN_SERVER_ID + " integer, "
            + Contract.Entry.COLUMN_HUMAN_LOC + " text not null, "
            + Contract.Entry.COLUMN_CONTENT + " text not null, "
            + Contract.Entry.COLUMN_TIMESTAMP + " long, "
            + Contract.Entry.COLUMN_STATUS + " integer default 0, "
            + Contract.Entry.COLUMN_LAT + " double not null, "
            + Contract.Entry.COLUMN_LNG + " double not null, "
            + Contract.Entry.COLUMN_ADMIN_ID + " integer default 0, "
            + Contract.Entry.COLUMN_LOC_ACC + " float, "
            + Contract.Entry.COLUMN_LOC_TIME + " long, "
            + Contract.Entry.COLUMN_LOC_PROV + " text, "
            + Contract.Entry.COLUMN_LOC_DATA + " text, "
            + Contract.Entry.COLUMN_USERNAME + " text not null, "
            + Contract.Entry.COLUMN_USERID + " integer default 0, "
            + Contract.Entry.COLUMN_MEDIA + " text not null, "
            + Contract.Entry.COLUMN_UPVOTE_COUNT + " integer default 0, "
            + Contract.Entry.COLUMN_USER_UPVOTED + " integer default 0, "
            + Contract.Entry.COLUMN_PENDINGFLAG + " integer default -1, "
            + Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS + " integer default 0,"
            + Contract.Entry.COLUMN_PARENT_REPORT + " integer default 0"
            + ")";

    private static final String UPVOTE_TABLE_CREATE = "create table "
            + Contract.UpvoteLog.TABLE_NAME + "("
            + Contract.UpvoteLog.COLUMN_ID + " integer primary key autoincrement, "
            + Contract.UpvoteLog.COLUMN_SERVER_ID + " integer, "
            + Contract.UpvoteLog.COLUMN_LAT + " double, "
            + Contract.UpvoteLog.COLUMN_LON + " double"
            + ")";

    private static final String COMMENTS_TABLE_CREATE = "create table "
            + Contract.Comments.TABLE_NAME + "("
            + Contract.Comments._ID + " integer primary key autoincrement, "
            + Contract.Comments.COLUMN_SERVER_ID + " integer not null, "
            + Contract.Comments.COLUMN_REPORT_ID + " integer not null, "
            + Contract.Comments.COLUMN_CONTENT + " text, "
            + Contract.Comments.COLUMN_TIMESTAMP + " long, "
            + Contract.Comments.COLUMN_USERNAME + " text"
            + ")";

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
            
    public ReportDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(REPORT_TABLE_CREATE);
        database.execSQL(UPVOTE_TABLE_CREATE);
        database.execSQL(COMMENTS_TABLE_CREATE);
        database.execSQL(ADMINS_TABLE_CREATE);
        database.execSQL(LANDMARKS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ReportDatabase.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.UpvoteLog.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Comments.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Admin.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Landmark.TABLE_NAME);
        onCreate(db);
    }
}
