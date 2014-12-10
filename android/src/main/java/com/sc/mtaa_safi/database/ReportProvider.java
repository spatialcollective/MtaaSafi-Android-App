package com.sc.mtaa_safi.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.net.Uri;

public class ReportProvider extends ContentProvider {
    ReportDatabase mDatabaseHelper;
    private static final String AUTHORITY = Contract.CONTENT_AUTHORITY;
    public static final int ROUTE_ENTRIES = 1,
                            ROUTE_ENTRIES_ID = 2,
                            ROUTE_UPVOTES = 3,
                            ROUTE_UPVOTES_ID = 4,
                            ROUTE_COMMENTS =5,
                            ROUTE_COMMENTS_ID = 6,
                            ROUTE_ADMINAREAS = 7,
                            ROUTE_ADMINAREAS_ID = 8,
                            ROUTE_LANDMARKS = 9,
                            ROUTE_LANDMARKS_ID = 10;

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
            sUriMatcher.addURI(AUTHORITY, "comments", ROUTE_COMMENTS);
            sUriMatcher.addURI(AUTHORITY, "comments/*", ROUTE_COMMENTS_ID);
            sUriMatcher.addURI(AUTHORITY, "adminareas", ROUTE_ADMINAREAS);
            sUriMatcher.addURI(AUTHORITY, "adminareas/*", ROUTE_ADMINAREAS_ID);
            sUriMatcher.addURI(AUTHORITY, "landmarks", ROUTE_LANDMARKS);
            sUriMatcher.addURI(AUTHORITY, "landmarks/*", ROUTE_LANDMARKS_ID);

        }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ENTRIES:
                return Contract.Entry.CONTENT_TYPE;
            case ROUTE_ENTRIES_ID:
                return Contract.Entry.CONTENT_ITEM_TYPE;
            case ROUTE_UPVOTES:
                return Contract.UpvoteLog.CONTENT_TYPE;
            case ROUTE_UPVOTES_ID:
                return Contract.UpvoteLog.CONTENT_ITEM_TYPE;
            case ROUTE_COMMENTS:
                return Contract.Comments.CONTENT_TYPE;
            case ROUTE_COMMENTS_ID:
                return Contract.Comments.CONTENT_ITEM_TYPE;
            case ROUTE_ADMINAREAS:
                return Contract.AdminAreas.CONTENT_TYPE;
            case ROUTE_ADMINAREAS_ID:
                return Contract.AdminAreas.CONTENT_ITEM_TYPE;
            case ROUTE_LANDMARKS:
                return Contract.Landmarks.CONTENT_TYPE;
            case ROUTE_LANDMARKS_ID:
                return Contract.Landmarks.CONTENT_ITEM_TYPE;

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
                builder.where(Contract.Entry._ID + "=?", id);
            case ROUTE_ENTRIES:
                builder.table(Contract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs);
                break;

            case ROUTE_UPVOTES_ID:
                String upvoteId = uri.getLastPathSegment();
                builder.where(Contract.UpvoteLog._ID + "=?", upvoteId);
            case ROUTE_UPVOTES:
                builder.table(Contract.UpvoteLog.TABLE_NAME)
                        .where(selection, selectionArgs);
                break;

            case ROUTE_COMMENTS_ID:
                String commentId = uri.getLastPathSegment();
                builder.where(Contract.Comments._ID + "=?", commentId);
            case ROUTE_COMMENTS:
                builder.table(Contract.Comments.TABLE_NAME)
                        .where(selection, selectionArgs);
                break;

            case ROUTE_ADMINAREAS_ID:
                String adminAreaId = uri.getLastPathSegment();
                builder.where(Contract.AdminAreas._ID + "=?", adminAreaId);
            case ROUTE_ADMINAREAS:
                builder.table(Contract.AdminAreas.TABLE_NAME)
                        .where(selection, selectionArgs);
                break;

            case ROUTE_LANDMARKS_ID:
                String landmarkId = uri.getLastPathSegment();
                builder.where(Contract.Landmarks._ID + "=?", landmarkId);
            case ROUTE_LANDMARKS:
                builder.table(Contract.Landmarks.TABLE_NAME)
                        .where(selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return buildQuery(uri, builder, db, projection, sortOrder);
    }
    private Cursor buildQuery(Uri uri, SelectionBuilder builder, SQLiteDatabase db,
                                String[] projection, String sortOrder){
        Cursor cursor = builder.query(db, projection, sortOrder);
        Context context = getContext();
        assert context != null;
        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        long id;
        Uri result;
        switch (match) {
            case ROUTE_ENTRIES:
                id = db.insertOrThrow(Contract.Entry.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Entry.CONTENT_URI + "/" + id);
                break;
            case ROUTE_ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);

            case ROUTE_UPVOTES:
                id = db.insertOrThrow(Contract.UpvoteLog.TABLE_NAME, null, values);
                result = Uri.parse(Contract.UpvoteLog.UPVOTE_URI + "/" + id);
                break;
            case ROUTE_UPVOTES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);

            case ROUTE_COMMENTS:
                id = db.insertOrThrow(Contract.Comments.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Comments.COMMENTS_URI + "/" + id);
                break;
            case ROUTE_COMMENTS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);

            case ROUTE_ADMINAREAS:
                id = db.insertOrThrow(Contract.AdminAreas.TABLE_NAME, null, values);
                result = Uri.parse(Contract.AdminAreas.ADMIN_AREAS_URI + "/" + id);
                break;
            case ROUTE_ADMINAREAS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);

            case ROUTE_LANDMARKS:
                id = db.insertOrThrow(Contract.Landmarks.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Landmarks.LANDMARKS_URI + "/" + id);
                break;
            case ROUTE_LANDMARKS_ID:
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
                count = builder.table(Contract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(Contract.Entry.TABLE_NAME)
                        .where(Contract.Entry._ID + "=?", id)
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
        String id;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(Contract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_ENTRIES_ID:
                id = uri.getLastPathSegment();
                count = builder.table(Contract.Entry.TABLE_NAME)
                       .where(Contract.Entry._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;

            case ROUTE_UPVOTES:
                count = builder.table(Contract.UpvoteLog.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_UPVOTES_ID:
                id = uri.getLastPathSegment();
                count = builder.table(Contract.UpvoteLog.TABLE_NAME)
                        .where(Contract.Entry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;

            case ROUTE_COMMENTS:
                count = builder.table(Contract.Comments.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_COMMENTS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(Contract.Comments.TABLE_NAME)
                        .where(Contract.Entry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;

            case ROUTE_ADMINAREAS:
                count = builder.table(Contract.AdminAreas.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_ADMINAREAS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(Contract.AdminAreas.TABLE_NAME)
                        .where(Contract.Entry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;

            case ROUTE_LANDMARKS:
                count = builder.table(Contract.Landmarks.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_LANDMARKS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(Contract.Landmarks.TABLE_NAME)
                        .where(Contract.Entry._ID + "=?", id)
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
