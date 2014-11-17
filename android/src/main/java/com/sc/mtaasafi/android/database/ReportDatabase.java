package com.sc.mtaasafi.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ReportDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 15;
    private static final String DATABASE_NAME = "mtaasafi.db";
    private static final String REPORT_TABLE_CREATE = "create table "
            + Contract.Entry.TABLE_NAME + "("
            + Contract.Entry.COLUMN_ID + " integer primary key autoincrement, "
            + Contract.Entry.COLUMN_SERVER_ID + " integer, "
            + Contract.Entry.COLUMN_LOCATION + " text not null, "
            + Contract.Entry.COLUMN_CONTENT + " text not null, "
            + Contract.Entry.COLUMN_TIMESTAMP + " text not null, "
            + Contract.Entry.COLUMN_LAT + " double not null, "
            + Contract.Entry.COLUMN_LNG + " double not null, "
            + Contract.Entry.COLUMN_USERNAME + " text not null, "
            + Contract.Entry.COLUMN_MEDIAURL1 + " text not null, "
            + Contract.Entry.COLUMN_MEDIAURL2 + " text not null, "
            + Contract.Entry.COLUMN_MEDIAURL3 + " text not null, "
            + Contract.Entry.COLUMN_UPVOTE_COUNT + " integer default 0, "
            + Contract.Entry.COLUMN_USER_UPVOTED + " integer default 0, "
            + Contract.Entry.COLUMN_PENDINGFLAG + " integer default -1, "
            + Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS + " integer default 0"
            + ")";

    private static final String UPVOTE_TABLE_CREATE = "create table "
            + Contract.UpvoteLog.TABLE_NAME + "("
            + Contract.UpvoteLog.COLUMN_ID + " integer primary key autoincrement, "
            + Contract.UpvoteLog.COLUMN_SERVER_ID + " integer, "
            + Contract.UpvoteLog.COLUMN_LAT + " double, "
            + Contract.UpvoteLog.COLUMN_LON + " double"
            + ")";

    public ReportDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(REPORT_TABLE_CREATE);
        database.execSQL(UPVOTE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ReportDatabase.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.UpvoteLog.TABLE_NAME);
        onCreate(db);
    }

}
