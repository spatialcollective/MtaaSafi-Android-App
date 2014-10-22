package com.sc.mtaasafi.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 10/19/14.
 */
public class ReportsDataSource {

	private SQLiteDatabase database;
	private ReportDatabase dbHelper;
	private String[] allColumns = {
            ReportContract.Entry.COLUMN_ID,
            ReportContract.Entry.COLUMN_TITLE,
            ReportContract.Entry.COLUMN_DETAILS,
            ReportContract.Entry.COLUMN_TIMESTAMP,
            ReportContract.Entry.COLUMN_LAT,
            ReportContract.Entry.COLUMN_LNG,
            ReportContract.Entry.COLUMN_USERNAME,
            ReportContract.Entry.COLUMN_PICS,
            ReportContract.Entry.COLUMN_MEDIAURLS };

	public ReportsDataSource(Context context) {
		dbHelper = new ReportDatabase(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Report createReport(Bundle bundle) {
		ContentValues values = new ContentValues();
		values.put(ReportContract.Entry.COLUMN_TITLE, bundle.getString(Report.TITLE_KEY));
        values.put(ReportContract.Entry.COLUMN_DETAILS, bundle.getString(Report.DETAILS_KEY));
        values.put(ReportContract.Entry.COLUMN_TIMESTAMP, bundle.getString(Report.TIMESTAMP_KEY));
        values.put(ReportContract.Entry.COLUMN_LAT, bundle.getString(Report.LAT_KEY));
        values.put(ReportContract.Entry.COLUMN_LNG, bundle.getString(Report.LNG_KEY));
        values.put(ReportContract.Entry.COLUMN_USERNAME, bundle.getString(Report.USERNAME_KEY));
        values.put(ReportContract.Entry.COLUMN_PICS, bundle.getString(Report.PICS_KEY));
        values.put(ReportContract.Entry.COLUMN_MEDIAURLS, bundle.getString(Report.MEDIAURLS_KEY));

		long insertId = database.insert(ReportContract.Entry.TABLE_NAME, null, values);
		Cursor cursor = database.query(ReportContract.Entry.TABLE_NAME,
            allColumns, ReportContract.Entry.COLUMN_ID + " = " + insertId, null,
            null, null, null);
		cursor.moveToFirst();
		Report newReport = new Report(cursor);
		cursor.close();
		return newReport;
	}

	public void deleteReport(Report report) {
		long id = report.id;
		System.out.println("Report deleted with id: " + id);
		database.delete(ReportContract.Entry.TABLE_NAME, ReportContract.Entry.COLUMN_ID
		+ " = " + id, null);
	}

	public List<Report> getAllReports() {
		List<Report> reports = new ArrayList<Report>();

		Cursor cursor = database.query(ReportContract.Entry.TABLE_NAME,
		allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Report report = new Report(cursor);
			reports.add(report);
			cursor.moveToNext();
		}

		cursor.close();
		return reports;
	}
}
