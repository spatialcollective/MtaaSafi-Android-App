package com.sc.mtaasafi.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ReportDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 9;
    private static final String DATABASE_NAME = "mtaasafi.db";
    private static final String REPORT_TABLE_CREATE = "create table "
            + ReportContract.Entry.TABLE_NAME + "("
            + ReportContract.Entry.COLUMN_ID + " integer primary key autoincrement, "
            + ReportContract.Entry.COLUMN_SERVER_ID + " integer, "
            + ReportContract.Entry.COLUMN_LOCATION + " text not null, "
            + ReportContract.Entry.COLUMN_CONTENT + " text not null, "
            + ReportContract.Entry.COLUMN_TIMESTAMP + " text not null, "
            + ReportContract.Entry.COLUMN_LAT + " text not null, "
            + ReportContract.Entry.COLUMN_LNG + " text not null, "
            + ReportContract.Entry.COLUMN_USERNAME + " text not null, "
            + ReportContract.Entry.COLUMN_MEDIAURL1 + " text not null, "
            + ReportContract.Entry.COLUMN_MEDIAURL2 + " text not null, "
            + ReportContract.Entry.COLUMN_MEDIAURL3 + " text not null, "
            + ReportContract.Entry.COLUMN_UPVOTE_COUNT + " integer default 0, "
            + ReportContract.Entry.COLUMN_USER_UPVOTED + " integer default 0"
            + ")";

    private static final String UPVOTE_TABLE_CREATE = "create table "
            + ReportContract.UpvoteLog.TABLE_NAME + "("
            + ReportContract.UpvoteLog.COLUMN_ID + " integer primary key autoincrement, "
            + ReportContract.UpvoteLog.COLUMN_SERVER_ID + " integer"
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
        db.execSQL("DROP TABLE IF EXISTS " + ReportContract.Entry.TABLE_NAME);
        onCreate(db);
    }
}