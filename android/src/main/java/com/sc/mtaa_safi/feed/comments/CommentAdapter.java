package com.sc.mtaa_safi.feed.comments;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.RecyclerViewCursorAdapter;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;
import com.sc.mtaa_safi.database.Contract;

public class CommentAdapter extends RecyclerViewCursorAdapter<CommentAdapter.ViewHolder> {
    public CommentAdapter(Context context, Cursor cursor) { super(context, cursor); }

    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor c) {
        holder.mTextView.setText(c.getString(c.getColumnIndex(Contract.Comments.COLUMN_CONTENT)));
        holder.mUserNameView.setText(c.getString(c.getColumnIndex(Contract.Comments.COLUMN_USERNAME)));
        holder.mTimeView.setText(PrefUtils.getElapsedTime(c.getLong(c.getColumnIndex(Contract.Comments.COLUMN_TIMESTAMP))));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView, mUserNameView, mTimeView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.commentText);
            mUserNameView = (TextView) v.findViewById(R.id.commentUserName);
            mTimeView = (TextView) v.findViewById(R.id.commentTime);
        }
    }
}
