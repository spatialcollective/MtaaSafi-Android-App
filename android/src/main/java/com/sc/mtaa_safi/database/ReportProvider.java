package com.sc.mtaa_safi.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ReportProvider extends ContentProvider {
    ReportDatabase mDatabaseHelper;
    private static final String AUTHORITY = Contract.CONTENT_AUTHORITY;
    public static final int ROUTE_ENTRIES = 1, ROUTE_ENTRIES_ID = 2,
                            ROUTE_USERS = 3, ROUTE_USERS_ID = 4,
                            ROUTE_LOCATIONS = 5, ROUTE_LOCATIONS_ID = 6,
                            ROUTE_UPVOTES = 7, ROUTE_UPVOTES_ID = 8,
                            ROUTE_COMMENTS = 9, ROUTE_COMMENTS_ID = 10,
                            ROUTE_ADMINS = 11, ROUTE_ADMINS_ID = 12,
                            ROUTE_LANDMARKS = 13, ROUTE_LANDMARKS_ID = 14,
                            ROUTE_TAGS = 15, ROUTE_TAGS_ID = 16,
                            ROUTE_REPORT_TAGS = 17, ROUTE_REPORT_TAGS_ID = 18;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new ReportDatabase(getContext());
        return true;
    }

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        static {
            sUriMatcher.addURI(AUTHORITY, "entries", ROUTE_ENTRIES);
            sUriMatcher.addURI(AUTHORITY, "entries/*", ROUTE_ENTRIES_ID);
            sUriMatcher.addURI(AUTHORITY, "locations", ROUTE_LOCATIONS);
            sUriMatcher.addURI(AUTHORITY, "locations/*", ROUTE_LOCATIONS_ID);
            sUriMatcher.addURI(AUTHORITY, "upvotes", ROUTE_UPVOTES);
            sUriMatcher.addURI(AUTHORITY, "upvotes/*", ROUTE_UPVOTES_ID);
            sUriMatcher.addURI(AUTHORITY, "comments", ROUTE_COMMENTS);
            sUriMatcher.addURI(AUTHORITY, "comments/*", ROUTE_COMMENTS_ID);
            sUriMatcher.addURI(AUTHORITY, "admins", ROUTE_ADMINS);
            sUriMatcher.addURI(AUTHORITY, "admins/*", ROUTE_ADMINS_ID);
            sUriMatcher.addURI(AUTHORITY, "landmarks", ROUTE_LANDMARKS);
            sUriMatcher.addURI(AUTHORITY, "landmarks/*", ROUTE_LANDMARKS_ID);
            sUriMatcher.addURI(AUTHORITY, "tags", ROUTE_TAGS);
            sUriMatcher.addURI(AUTHORITY, "tags/*", ROUTE_TAGS_ID);
            sUriMatcher.addURI(AUTHORITY, "reporttags", ROUTE_REPORT_TAGS);
            sUriMatcher.addURI(AUTHORITY, "reporttags/*", ROUTE_REPORT_TAGS_ID);
        }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ENTRIES:
                return Contract.Entry.CONTENT_TYPE;
            case ROUTE_ENTRIES_ID:
                return Contract.Entry.CONTENT_ITEM_TYPE;
            case ROUTE_LOCATIONS:
                return Contract.MtaaLocation.CONTENT_TYPE;
            case ROUTE_LOCATIONS_ID:
                return Contract.MtaaLocation.CONTENT_ITEM_TYPE;
            case ROUTE_UPVOTES:
                return Contract.UpvoteLog.CONTENT_TYPE;
            case ROUTE_UPVOTES_ID:
                return Contract.UpvoteLog.CONTENT_ITEM_TYPE;
            case ROUTE_COMMENTS:
                return Contract.Comments.CONTENT_TYPE;
            case ROUTE_COMMENTS_ID:
                return Contract.Comments.CONTENT_ITEM_TYPE;
            case ROUTE_ADMINS:
                return Contract.Admin.CONTENT_TYPE;
            case ROUTE_ADMINS_ID:
                return Contract.Admin.CONTENT_ITEM_TYPE;
            case ROUTE_LANDMARKS:
                return Contract.Landmark.CONTENT_TYPE;
            case ROUTE_LANDMARKS_ID:
                return Contract.Landmark.CONTENT_ITEM_TYPE;
            case ROUTE_TAGS:
                return Contract.Tag.CONTENT_TYPE;
            case ROUTE_TAGS_ID:
                return Contract.Tag.CONTENT_ITEM_TYPE;
            case ROUTE_REPORT_TAGS:
                return Contract.ReportTagJunction.CONTENT_TYPE;
            case ROUTE_REPORT_TAGS_ID:
                return Contract.ReportTagJunction.CONTENT_ITEM_TYPE;
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
            case ROUTE_ENTRIES_ID: // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.table(Contract.REPORTS_JOIN_LOCATIONS)
                        .mapToTable(Contract.MtaaLocation.COLUMN_LAT, Contract.MtaaLocation.TABLE_NAME)
                        .mapToTable(Contract.MtaaLocation.COLUMN_LNG, Contract.MtaaLocation.TABLE_NAME)
                        .mapToTable(Contract.MtaaLocation.COLUMN_LOC_ACC, Contract.MtaaLocation.TABLE_NAME)
                        .mapToTable(Contract.MtaaLocation.COLUMN_LOC_TIME, Contract.MtaaLocation.TABLE_NAME)
                        .mapToTable(Contract.MtaaLocation.COLUMN_LOC_PROV, Contract.MtaaLocation.TABLE_NAME)
                        .where(Contract.Entry._ID + "=?", id);
            case ROUTE_ENTRIES:
                builder.table(Contract.REPORTS_JOIN_LOCATIONS)
                        .mapToTable(Contract.MtaaLocation.COLUMN_LAT, Contract.MtaaLocation.TABLE_NAME)
                        .mapToTable(Contract.MtaaLocation.COLUMN_LNG, Contract.MtaaLocation.TABLE_NAME)
                        .mapToTable(Contract.MtaaLocation.COLUMN_LOC_ACC, Contract.MtaaLocation.TABLE_NAME)
                        .mapToTable(Contract.MtaaLocation.COLUMN_LOC_TIME, Contract.MtaaLocation.TABLE_NAME)
                        .mapToTable(Contract.MtaaLocation.COLUMN_LOC_PROV, Contract.MtaaLocation.TABLE_NAME)
                        .where(selection, selectionArgs);
                return buildQuery(uri, builder, db, projection, sortOrder);
            case ROUTE_LOCATIONS_ID:
                String locationId = uri.getLastPathSegment();
                builder.where(Contract.MtaaLocation._ID + "=?", locationId);
            case ROUTE_LOCATIONS:
                builder.table(Contract.MtaaLocation.TABLE_NAME)
                        .where(selection, selectionArgs);
                return buildQuery(uri, builder, db, projection, sortOrder);
            case ROUTE_UPVOTES_ID:
                String upvoteId = uri.getLastPathSegment();
                builder.where(Contract.UpvoteLog._ID + "=?", upvoteId);
            case ROUTE_UPVOTES:
                builder.table(Contract.UpvoteLog.TABLE_NAME)
                        .where(selection, selectionArgs);
                return buildQuery(uri, builder, db, projection, sortOrder);
            case ROUTE_COMMENTS_ID:
                String commentId = uri.getLastPathSegment();
                builder.where(Contract.Comments._ID + "=?", commentId);
            case ROUTE_COMMENTS:
                builder.table(Contract.Comments.TABLE_NAME)
                        .where(selection, selectionArgs);
                return buildQuery(uri, builder, db, projection, sortOrder);
            case ROUTE_ADMINS_ID:
                String adminId = uri.getLastPathSegment();
                builder.where(Contract.Admin._ID + "=?", adminId);
            case ROUTE_ADMINS:
                builder.table(Contract.Admin.TABLE_NAME)
                        .where(selection, selectionArgs);
                return buildQuery(uri, builder, db, projection, sortOrder);
            case ROUTE_LANDMARKS_ID:
                String landmarkId = uri.getLastPathSegment();
                builder.where(Contract.Landmark._ID + "=?", landmarkId);
            case ROUTE_LANDMARKS:
                builder.table(Contract.Landmark.TABLE_NAME)
                        .where(selection, selectionArgs);
                return buildQuery(uri, builder, db, projection, sortOrder);
            case ROUTE_TAGS_ID:
                String tagId = uri.getLastPathSegment();
                builder.where(Contract.Tag._ID + "=?", tagId);
            case ROUTE_TAGS:
                builder.table(Contract.Tag.TABLE_NAME)
                        .where(selection, selectionArgs);
                return buildQuery(uri, builder, db, projection, sortOrder);
            case ROUTE_REPORT_TAGS_ID:
                String reportTagId = uri.getLastPathSegment();
                builder.table(Contract.REPORTS_JOIN_TAGS)
                        .mapToTable(Contract.Tag.COLUMN_NAME, Contract.Tag.TABLE_NAME)
                        .where(Contract.ReportTagJunction._ID + "=?", reportTagId);
            case ROUTE_REPORT_TAGS:
                builder.table(Contract.REPORTS_JOIN_TAGS)
                        .mapToTable(Contract.Tag.COLUMN_NAME, Contract.Tag.TABLE_NAME)
                        .where(selection, selectionArgs);
                return buildQuery(uri, builder, db, projection, sortOrder);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    private Cursor buildQuery(Uri uri, SelectionBuilder builder, SQLiteDatabase db, String[] projection, String sortOrder) {
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
        Uri result;
        switch (match) {
            case ROUTE_ENTRIES:
                long reportId = db.insertOrThrow(Contract.Entry.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Entry.CONTENT_URI + "/" + reportId);
                break;
            case ROUTE_ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_LOCATIONS:
                long locationId = db.insertOrThrow(Contract.MtaaLocation.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Entry.CONTENT_URI + "/" + locationId);
                break;
            case ROUTE_LOCATIONS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_UPVOTES:
                long upvoteId = db.insertOrThrow(Contract.UpvoteLog.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Entry.CONTENT_URI + "/" + upvoteId);
                break;
            case ROUTE_UPVOTES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_COMMENTS:
                long commentId = db.insertOrThrow(Contract.Comments.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Entry.CONTENT_URI + "/" + commentId);
                break;
            case ROUTE_COMMENTS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_ADMINS:
                long adminId = db.insertOrThrow(Contract.Admin.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Entry.CONTENT_URI + "/" + adminId);
                break;
            case ROUTE_ADMINS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: "+ uri);
            case ROUTE_LANDMARKS:
                long landmarkId = db.insertOrThrow(Contract.Landmark.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Entry.CONTENT_URI + "/" + landmarkId);
                break;
            case ROUTE_LANDMARKS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " +uri);
            case ROUTE_TAGS:
                long tagId = db.insertOrThrow(Contract.Tag.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Entry.CONTENT_URI +"/"+ tagId);
                break;
            case ROUTE_TAGS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: "+ uri);
            case ROUTE_REPORT_TAGS:
                long reportTagId = db.insertOrThrow(Contract.ReportTagJunction.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Entry.CONTENT_URI+"/"+reportTagId);
                break;
            case ROUTE_REPORT_TAGS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: "+uri);
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
            case ROUTE_LOCATIONS:
                count = builder.table(Contract.MtaaLocation.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_LOCATIONS_ID:
                String locId = uri.getLastPathSegment();
                count = builder.table(Contract.MtaaLocation.TABLE_NAME)
                        .where(Contract.MtaaLocation._ID + "=?", locId)
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
                count = builder.table(Contract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(Contract.Entry.TABLE_NAME)
                       .where(Contract.Entry._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;
            case ROUTE_LOCATIONS:
                count = builder.table(Contract.MtaaLocation.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_LOCATIONS_ID:
                String locId = uri.getLastPathSegment();
                count = builder.table(Contract.MtaaLocation.TABLE_NAME)
                        .where(Contract.MtaaLocation._ID + "=?", locId)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_UPVOTES:
                count = builder.table(Contract.UpvoteLog.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_UPVOTES_ID:
                String upvoteId = uri.getLastPathSegment();
                count = builder.table(Contract.UpvoteLog.TABLE_NAME)
                        .where(Contract.Entry._ID + "=?", upvoteId)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            default: // TODO: implement deleting comments .... eventually
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }
}
