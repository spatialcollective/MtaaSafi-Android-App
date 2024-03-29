package com.sc.mtaasafi.android;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

// Borrowed from https://gist.github.com/skyfishjy/443b7448f59be978bc59
public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
 
    private Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;
 
    public RecyclerViewCursorAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }
 
    public Cursor getCursor() { return mCursor; }
    public Context getContext() { return mContext; }
 
    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null)
            return mCursor.getCount();
        return 0;
    }
 
    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position))
            return mCursor.getLong(mRowIdColumn);
        return 0;
    }
 
    @Override
    public void setHasStableIds(boolean hasStableIds) { super.setHasStableIds(true); }
 
    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);
    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        if (!mDataValid)
            throw new IllegalStateException("this should only be called when the cursor is valid");
        if (!mCursor.moveToPosition(position))
            throw new IllegalStateException("couldn't move cursor to position " + position);
        onBindViewHolder(viewHolder, mCursor);
    }
 
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null)
            old.close();
    }
 
    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null)
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null)
                mCursor.registerDataSetObserver(mDataSetObserver);
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }
 
    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }
 
        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
        }
    }
}
