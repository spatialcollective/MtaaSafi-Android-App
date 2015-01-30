package com.sc.mtaa_safi.feed.comments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.database.Contract;

/**
 * Created by lenovo on 12/8/2014.
 */
public class CommentsFragment extends android.support.v4.app.DialogFragment
        implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
    NewCommentLayout mNewComment;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public Report mReport;
    public CommentsFragment(){ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_comments, null, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.comments);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mNewComment = (NewCommentLayout) view.findViewById(R.id.new_comment_bar);
//        mNewComment.addData(mReport);

        mAdapter = new CommentAdapter(getActivity(), null, mReport.serverId);
        mRecyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
        view.findViewById(R.id.leave_comments).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { dismiss(); }
        });
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), Contract.Comments.COMMENTS_URI,
                Comment.PROJECTION, Comment.getSelection(mReport.serverId), null, Comment.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        ((CommentAdapter) mAdapter).swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        ((CommentAdapter) mAdapter).swapCursor(null);
    }
}
