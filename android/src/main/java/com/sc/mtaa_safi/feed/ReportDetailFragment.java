package com.sc.mtaa_safi.feed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.feed.comments.Comment;
import com.sc.mtaa_safi.feed.comments.CommentAdapter;
import com.sc.mtaa_safi.feed.comments.CommentSender;
import com.sc.mtaa_safi.feed.comments.NewCommentLayout;

import java.text.SimpleDateFormat;


public class ReportDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    Report mReport;
    private String distance = "None";
    Location currentLocation;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public AQuery aq;
    ImageView[] media;
    public ViewPager viewPager;
    RelativeLayout bottomView;

    public static final String USERNAME = "username", REFRESH_KEY= "refresh", COMMENT = "comment";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        aq = new AQuery(getActivity());
    }

    public void setData(Report r) { mReport = r; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_report_detail, container, false);
        if (savedState != null)
            mReport = new Report(savedState);
        if (NetworkUtils.isOnline(getActivity()))
            new CommentSender(getActivity(), mReport.serverId).execute();
        currentLocation = ((MainActivity) getActivity()).getLocation();
        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViewPager(view);
        setDataInViews(view);
        addImages(view);
        setClickListeners(view);
    }

    private void setDataInViews(View view) {
        bottomView = (RelativeLayout) getView().findViewById(R.id.report_BottomView);
        updateVote((VoteButton) view.findViewById(R.id.topVote));
        updateVote((VoteButton) view.findViewById(R.id.bottomVote));
        updateDetails(view.findViewById(R.id.top_layout));
        updateDetails(view.findViewById(R.id.report_BottomView));
        addComments(view);
    }

    private void updateDetails(View view) {
        ((TextView) view.findViewById(R.id.r_username)).setText(mReport.userName);
        ((TextView) view.findViewById(R.id.r_content)).setText(mReport.content);
        ((TextView) view.findViewById(R.id.r_timestamp)).setText(createHumanReadableTimestamp());
        ((TextView) view.findViewById(R.id.itemLocation)).setText(mReport.locationDescript);
    }

    private void updateVote(VoteButton voter) {
        final View wholeView = getView();
        voter.mServerId = mReport.serverId;
        voter.mReportUri = Report.getUri(mReport.dbId);
        voter.setCheckedState(mReport.upVoted, mReport.upVoteCount, null);
        voter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wholeView.findViewById(R.id.bottomVote) == view)
                    ((VoteButton) wholeView.findViewById(R.id.topVote)).setCheckedState(true, mReport.upVoteCount + 1, null);
                else
                    ((VoteButton) wholeView.findViewById(R.id.bottomVote)).setCheckedState(true, mReport.upVoteCount + 1, null);
        }});
    }

    @Override
    public void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        String commentText = ((TextView) getView().findViewById(R.id.commentEditText)).getText().toString();
        if (commentText != null)
            outstate.putString(Contract.Comments.COLUMN_CONTENT, commentText);
        outstate = mReport.saveState(outstate);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }
    @Override
    public void onStop() {
        super.onStop();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    }
    private String createHumanReadableTimestamp() {
        return new SimpleDateFormat("H:mm:ss dd-MM-yyyy")
                .format(new java.util.Date(mReport.timeStamp));
    }

    private void addComments(View view) {
        NewCommentLayout commentLayout = (NewCommentLayout) view.findViewById(R.id.new_comment_bar);
        commentLayout.addData(mReport);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.comments);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CommentAdapter(getActivity(), null);
        mRecyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Contract.Comments.COMMENTS_URI,
            Comment.PROJECTION, Comment.getSelection(mReport.serverId), null, Comment.DEFAULT_SORT);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        ((CommentAdapter) mAdapter).swapCursor(cursor); }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { ((CommentAdapter) mAdapter).swapCursor(null); }


// ==========================   Images   ===============================

    private void setClickListeners(View view) {
        view.findViewById(R.id.media1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { enterImageViewer(0); }});
        view.findViewById(R.id.media2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { enterImageViewer(1); }});
        view.findViewById(R.id.media3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { enterImageViewer(2); }});
    }

    public void addImages(View view) {
        updateDetails(view.findViewById(R.id.report_BottomView));
        int mediaHeight = ((MainActivity) getActivity()).getScreenHeight()/4;
        media = new ImageView[3];
        media[0] = (ImageView) view.findViewById(R.id.media1);
        media[1] = (ImageView) view.findViewById(R.id.media2);
        media[2] = (ImageView) view.findViewById(R.id.media3);
        for (int i = 0; i < mReport.mediaPaths.size(); i++) {
            media[i].getLayoutParams().height = mediaHeight;
            media[i].requestLayout();
            aq.id(media[i]).image(mReport.mediaPaths.get(i));
        }
    }

    private void setUpViewPager(View view) {
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new ImageSlideAdapter(getChildFragmentManager(), mReport.mediaPaths.toArray(new String[mReport.mediaPaths.size()])));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

    private class ImageSlideAdapter extends FragmentPagerAdapter {
        String[] mediaPaths;
        public ImageSlideAdapter(FragmentManager fm, String[] mediaPaths) {
            super(fm);
            this.mediaPaths = mediaPaths;
        }
        @Override
        public int getCount() { return mediaPaths.length; }
        @Override
        public Fragment getItem(int i) {
            ImageFragment iF = new ImageFragment();
            Bundle args = new Bundle();
            args.putString("mediaPath", mediaPaths[i]);
            iF.setArguments(args);
            return iF;
        }
    }

// ==========================   Picture Animation   ===============================

    private void enterImageViewer(int i){
        getView().setBackgroundColor(getResources().getColor(R.color.DarkSlateGray));
        viewPager.setCurrentItem(i);
        if(PrefUtils.SDK > Build.VERSION_CODES.HONEYCOMB_MR2)
            enterImageAnimation(i);
        else{
            viewPager.setVisibility(View.VISIBLE);
            fadeInBottomView();
        }
        getView().findViewById(R.id.top_layout).setVisibility(View.INVISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void enterImageAnimation(int i) {
        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();
        bottomView.bringToFront();
        bottomView.getParent().requestLayout();
        ((View) bottomView.getParent()).invalidate();
        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        media[i].getGlobalVisibleRect(startBounds);
        getView().getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }
        viewPager.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        viewPager.setPivotX(0f);
        viewPager.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                for(ImageView image : media)
                    image.setAlpha(1f);
                fadeInBottomView();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                for(ImageView image : media)
                    image.setAlpha(1f);
            }
        });
        set.play(ObjectAnimator.ofFloat(viewPager, View.X,
                startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(viewPager, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(viewPager, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(viewPager,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(300);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
    }
    public void fadeInBottomView(){
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(300);
        AnimationSet anim = new AnimationSet(false); //change to false
        anim.addAnimation(fadeIn);
        bottomView.startAnimation(anim);
        bottomView.setVisibility(View.VISIBLE);
    }
    public void exitImageViewer(){
        getView().setBackgroundColor(getResources().getColor(R.color.White));
        getView().findViewById(R.id.top_layout).setVisibility(View.VISIBLE);
        fadeOutBottomView();
    }
    private void fadeOutBottomView(){
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                bottomView.setVisibility(View.INVISIBLE);
                if (PrefUtils.SDK > Build.VERSION_CODES.HONEYCOMB_MR2)
                    exitImageAnimation(viewPager.getCurrentItem());
//                getActivity().getActionBar().show();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        bottomView.startAnimation(fadeOut);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void exitImageAnimation(int i){
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();
        viewPager.bringToFront();
        viewPager.bringToFront();
        viewPager.getParent().requestLayout();
        ((View)viewPager.getParent()).invalidate();

        media[i].getGlobalVisibleRect(startBounds);
        getView().getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);
        AnimatorSet set = new AnimatorSet();

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }
        final float startScaleFinal = startScale;
        set.play(ObjectAnimator
                .ofFloat(viewPager, View.X, startBounds.left))
                .with(ObjectAnimator
                        .ofFloat(viewPager,
                                View.Y,startBounds.top))
                .with(ObjectAnimator
                        .ofFloat(viewPager,
                                View.SCALE_X, startScaleFinal))
                .with(ObjectAnimator
                        .ofFloat(viewPager,
                                View.SCALE_Y, startScaleFinal));
        set.setDuration(300);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                for(ImageView image : media)
                    image.setAlpha(1f);
                viewPager.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                for(ImageView image : media)
                    image.setAlpha(1f);
                viewPager.setVisibility(View.INVISIBLE);
            }
        });
        set.start();
    }
}
