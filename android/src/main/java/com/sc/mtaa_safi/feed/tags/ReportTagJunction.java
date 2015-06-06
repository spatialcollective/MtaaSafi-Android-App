package com.sc.mtaa_safi.feed.tags;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;

import com.sc.mtaa_safi.database.Contract;

import org.json.JSONArray;

/**
 * Created by ishuah on 6/5/15.
 */
public class ReportTagJunction {

    public static final  String[] REPORT_TAG_PROJECTION = new String[] {
            Contract.ReportTagJunction.COLUMN_FK_REPORT,
            Contract.Tag.COLUMN_NAME
    };

    public static Uri save(Context context, int fkReport, int fkTag) throws SQLiteConstraintException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.ReportTagJunction.COLUMN_FK_REPORT, fkReport);
        contentValues.put(Contract.ReportTagJunction.COLUMN_FK_TAG, fkTag);
        return  context.getContentResolver().insert(Contract.ReportTagJunction.REPORT_TAG_URI, contentValues);
    }

    public static JSONArray getReportTags(Context context, int fkReport){

        Cursor cursor = context.getContentResolver().query(
                Contract.ReportTagJunction.REPORT_TAG_URI,
                REPORT_TAG_PROJECTION,
                Contract.ReportTagJunction.COLUMN_FK_REPORT + " == " + fkReport, null, null);
        JSONArray tags = new JSONArray();
        while (cursor.moveToNext())
            tags.put(cursor.getString(1));
        return  tags;
    }
}
