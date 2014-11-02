package com.sc.mtaasafi.android.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.net.Uri;

public class ReportProvider extends ContentProvider {
    ReportDatabase mDatabaseHelper;
    private static final String AUTHORITY = ReportContract.CONTENT_AUTHORITY;
    public static final int ROUTE_ENTRIES = 1,
                            ROUTE_ENTRIES_ID = 2,
                            ROUTE_UPVOTES = 3,
                            ROUTE_UPVOTES_ID = 4;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new ReportDatabase(getContext());
        return true;
    }

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        static {
            sUriMatcher.addURI(AUTHORITY, "entries", ROUTE_ENTRIES);
            sUriMatcher.addURI(AUTHORITY, "entries/*", ROUTE_ENTRIES_ID);
            sUriMatcher.addURI(AUTHORITY, "upvotes", ROUTE_UPVOTES);
            sUriMatcher.addURI(AUTHORITY, "upvotes/*", ROUTE_UPVOTES_ID);
        }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ENTRIES:
                return ReportContract.Entry.CONTENT_TYPE;
            case ROUTE_ENTRIES_ID:
                return ReportContract.Entry.CONTENT_ITEM_TYPE;
            case ROUTE_UPVOTES:
                return ReportContract.UpvoteLog.CONTENT_TYPE;
            case ROUTE_UPVOTES_ID:
                return ReportContract.UpvoteLog.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_ENTRIES_ID:
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where(ReportContract.Entry._ID + "=?", id);
            case ROUTE_ENTRIES:
                // Return all known entries.
                builder.table(ReportContract.Entry.TABLE_NAME)
                       .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_UPVOTES_ID:
                String upvoteId = uri.getLastPathSegment();
                builder.where(ReportContract.Entry._ID + "=?", upvoteId);
            case ROUTE_UPVOTES:
                builder.table(ReportContract.UpvoteLog.TABLE_NAME)
                        .where(selection, selectionArgs);
                Cursor cursor = builder.query(db, projection, sortOrder);
                Context context = getContext();
                assert context != null;
                cursor.setNotificationUri(context.getContentResolver(), uri);
                return cursor;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case ROUTE_ENTRIES:
                long reportId = db.insertOrThrow(ReportContract.Entry.TABLE_NAME, null, values);
                result = Uri.parse(ReportContract.Entry.CONTENT_URI + "/" + reportId);
                break;
            case ROUTE_ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_UPVOTES:
                long upvoteId = db.insertOrThrow(ReportContract.UpvoteLog.TABLE_NAME, null, values);
                result = Uri.parse(ReportContract.Entry.CONTENT_URI + "/" + upvoteId);
                break;
            case ROUTE_UPVOTES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(ReportContract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(ReportContract.Entry.TABLE_NAME)
                        .where(ReportContract.Entry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(ReportContract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(ReportContract.Entry.TABLE_NAME)
                       .where(ReportContract.Entry._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }
}
