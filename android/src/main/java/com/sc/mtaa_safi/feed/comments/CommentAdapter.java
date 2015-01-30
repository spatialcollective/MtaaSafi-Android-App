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
    private static final int TYPE_FOOTER = 0, TYPE_ITEM = 1;
    private int mReportId;
    public CommentAdapter(Context context, Cursor cursor, int reportId) {
        super(context, cursor);
        mReportId = reportId;
    }

    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == TYPE_FOOTER) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_comment_layout, parent, false);
            return new FooterHolder(v);
        }
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view, parent, false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor c) {
        if (holder instanceof FooterHolder)
            ((FooterHolder) holder).new_comment.addData(mReportId);

        ((ItemHolder) holder).mTextView.setText(c.getString(c.getColumnIndex(Contract.Comments.COLUMN_CONTENT)));
        ((ItemHolder) holder).mUserNameView.setText(c.getString(c.getColumnIndex(Contract.Comments.COLUMN_USERNAME)));
        ((ItemHolder) holder).mTimeView.setText(PrefUtils.getElapsedTime(c.getLong(c.getColumnIndex(Contract.Comments.COLUMN_TIMESTAMP))));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) return TYPE_FOOTER;
        return TYPE_ITEM;
    }

    public class ViewHolder extends RecyclerView.ViewHolder { public ViewHolder(View v) { super(v); } }

    public class ItemHolder extends CommentAdapter.ViewHolder {
        public TextView mTextView, mUserNameView, mTimeView;

        public ItemHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.commentText);
            mUserNameView = (TextView) v.findViewById(R.id.commentUserName);
            mTimeView = (TextView) v.findViewById(R.id.commentTime);
        }
    }
    public class FooterHolder extends ItemHolder {
        NewCommentLayout new_comment;
        public FooterHolder(View v) {
            super(v);
            new_comment = (NewCommentLayout) v.findViewById(R.id.new_comment_bar);
        }
    }
}
