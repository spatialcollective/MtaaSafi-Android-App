package com.sc.mtaasafi.android.feed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.SystemUtils.ComplexPreferences;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.database.Contract;
import com.sc.mtaasafi.android.feed.comments.AddCommentBar;
import com.sc.mtaasafi.android.feed.comments.CommentRefresher;
import com.sc.mtaasafi.android.feed.comments.CommentSender;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;


public class ReportDetailFragment extends ListFragment implements AddCommentBar.CommentListener{

    Report mReport;
    private String content, location, time, user, mediaUrl1, mediaUrl2, mediaUrl3, distance;
    private int upvoteCount, serverId, dbId;
    ImageView[] media;
    private Location reportLocation;
    private boolean userVoted;
    public AQuery aq;
    public ViewPager viewPager;
    RelativeLayout bottomView;
    LinearLayout topView;
    SimpleCursorAdapter mAdapter;
    AddCommentBar addComment;

    private final static String REFRESH_KEY= "refresh";
    public static final String USERNAME = "username",
            COMMENT = "comment";

    public final String[] FROM_COLUMNS = new String[]{
            Contract.Comments.COLUMN_CONTENT,
            Contract.Comments.COLUMN_USERNAME,
            Contract.Comments.COLUMN_TIMESTAMP
    };
    public final int[] TO_FIELDS = new int[]{
            R.id.commentText,
            R.id.commentUserName,
            R.id.commentTime
    };
    private static String content_Key ="content",
        location_Key = "location",
        time_Key = "time",
        user_Key = "user",
        mediaUrl1_Key = "url1",
        mediaUrl2_Key = "url2",
        mediaUrl3_Key = "url3",
        serverId_Key = "serverId",
        dbId_Key = "dbId",
        lat_Key = "lat",
        lon_Key = "lon",
        userVoted_Key = "userVoted",
        upvoteCount_Key = "upvoteCt";
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        aq = new AQuery(getActivity());
    }

    public void setData(Report r) {
        mReport = r;
        content = r.content;
        location = r.locationDescript;
        time = getSimpleTimeStamp(r.timeStamp);
        user = r.userName;
        if (user.equals(""))
            user = "Unknown user";
        mediaUrl1 = r.mediaPaths.get(0);
        mediaUrl2 = r.mediaPaths.get(1);
        mediaUrl3 = r.mediaPaths.get(2);
        serverId = r.serverId;
        dbId = r.dbId;
        userVoted = r.upVoted;
        upvoteCount = r.upVoteCount;
        reportLocation = new Location("report location");
        reportLocation.setLatitude(r.latitude);
        reportLocation.setLongitude(r.longitude);
        refreshComments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_report_detail, container, false);
        if(savedState != null)
            restore(savedState);
        Location currentLocation = ((MainActivity) getActivity()).getLocation();
        if(currentLocation != null)
            distance = Report.getDistanceText(currentLocation, reportLocation);
        else
            distance = "error";

        return view;
    }

    private void restore(Bundle instate){
        content = instate.getString(content_Key);
        location = instate.getString(location_Key);
        time = instate.getString(time_Key);
        user = instate.getString(user_Key);
        mediaUrl1 = instate.getString(mediaUrl1_Key);
        mediaUrl2 = instate.getString(mediaUrl2_Key);
        mediaUrl3 = instate.getString(mediaUrl3_Key);
        serverId = instate.getInt(serverId_Key);
        dbId = instate.getInt(dbId_Key);
        double lat = instate.getDouble(lat_Key);
        double lon = instate.getDouble(lon_Key);
        reportLocation = new Location("report_location");
        reportLocation.setLatitude(lat);
        reportLocation.setLongitude(lon);
        userVoted = instate.getBoolean(userVoted_Key);
        upvoteCount = instate.getInt(upvoteCount_Key);
        // make sure this method's only called after inflation
        if(addComment != null)
            addComment.restore(instate);
    }
    @Override
    public void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        if(viewPager.getVisibility() == View.VISIBLE)
            updateTopVote();
        else
            updateBottomVote();
        AddCommentBar addCommentBar = (AddCommentBar)
                getView().findViewById(R.id.add_comment_bar);
        if(addCommentBar != null)
            addCommentBar.save(outstate);
        outstate.putString(content_Key, content);
        outstate.putString(location_Key, location);
        outstate.putString(time_Key,time);
        outstate.putString(user_Key, user);
        outstate.putString(mediaUrl1_Key, mediaUrl1);
        outstate.putString(mediaUrl2_Key, mediaUrl2);
        outstate.putString(mediaUrl3_Key, mediaUrl3);
        outstate.putInt(serverId_Key, serverId);
        outstate.putInt(dbId_Key, dbId);
        outstate.putDouble(lat_Key, reportLocation.getLatitude());
        outstate.putDouble(lon_Key, reportLocation.getLongitude());
        outstate.putBoolean(userVoted_Key, userVoted);
        outstate.putInt(upvoteCount_Key, upvoteCount);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public void onStop() {
        super.onStop();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        topView = (LinearLayout) view.findViewById(R.id.top_layout);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        String[] mediaUrls ={mediaUrl1, mediaUrl2, mediaUrl3};
        viewPager.setAdapter(new ImageSlideAdapter(getChildFragmentManager(), mediaUrls));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        addComment = (AddCommentBar) view.findViewById(R.id.add_comment_bar);
        addComment.setCommentListener(this);
        setClickListeners(view);
        updateView(view);
        if(savedInstanceState != null)
            restore(savedInstanceState);
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.comment_view, null,
                FROM_COLUMNS, TO_FIELDS, 0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                return false;
            }
        });
        setListAdapter(mAdapter);
    }
    private void setClickListeners(View view) {
        view.findViewById(R.id.media1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {enterImageViewer(0);}});
        view.findViewById(R.id.media2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {enterImageViewer(1);}});
        view.findViewById(R.id.media3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {enterImageViewer(2);}});
    }

    private void enterImageViewer(int i){
        updateBottomVote();
        getView().setBackgroundColor(getResources().getColor(R.color.DarkSlateGray));
        viewPager.setCurrentItem(i);
        if(PrefUtils.SDK > Build.VERSION_CODES.HONEYCOMB_MR2)
            enterImageAnimation(i);
        else{
            viewPager.setVisibility(View.VISIBLE);
            fadeInBottomView();
        }
        topView.setVisibility(View.INVISIBLE);
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
        updateTopVote();
        topView.setVisibility(View.VISIBLE);
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
                if(PrefUtils.SDK > Build.VERSION_CODES.HONEYCOMB_MR2)
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
    public void updateView(View view) {
        VoteInterface topVote = (VoteInterface) view.findViewById(R.id.topVote);
        topVote.updateData(upvoteCount, userVoted, serverId);
        ((TextView) view.findViewById(R.id.reportViewContent)).setText(content);
        ((TextView) view.findViewById(R.id.itemLocation)).setText(location);
        ((TextView) view.findViewById(R.id.itemDistance)).setText(distance);
        ((TextView) view.findViewById(R.id.reportViewTimeStamp)).setText(time);
        ((TextView) view.findViewById(R.id.reportViewUsername)).setText(user);

        bottomView = (RelativeLayout) view.findViewById(R.id.report_BottomView);
        updateBottomView(bottomView);
        int mediaHeight = ((MainActivity) getActivity()).getScreenHeight()/4;
        media = new ImageView[3];
        media[0] = (ImageView) view.findViewById(R.id.media1);
        media[1] = (ImageView) view.findViewById(R.id.media2);
        media[2] = (ImageView) view.findViewById(R.id.media3);
        media[0].getLayoutParams().height = mediaHeight;
        media[0].requestLayout();
        media[1].getLayoutParams().height = mediaHeight;
        media[1].requestLayout();
        media[2].getLayoutParams().height = mediaHeight;
        media[2].requestLayout();
        aq.id(media[0]).image(mediaUrl1);
        aq.id(media[1]).image(mediaUrl2);
        aq.id(media[2]).image(mediaUrl3);
    }
    private void updateBottomView(View view){
        String bottomUserDisplay;
        if(user.length() > 16){
            bottomUserDisplay = user.substring(0, 14) + "...";
        } else
            bottomUserDisplay = user.toString();
        ((TextView) view.findViewById(R.id.bottomContent)).setText(content);
        ((TextView) view.findViewById(R.id.bottomUsername)).setText(bottomUserDisplay);
        ((TextView) view.findViewById(R.id.bottomTimestamp)).setText(getSimpleTimeStamp(time));
        ((TextView) view.findViewById(R.id.itemLocation)).setText(location);
        ((TextView) view.findViewById(R.id.itemDistance)).setText(distance);
        updateBottomVote();
    }
    private void updateTopVote(){
        VoteInterface bottomVote = (VoteInterface) bottomView.findViewById(R.id.bottomVote);
        VoteInterface topVote = (VoteInterface) getView().findViewById(R.id.topVote);
        userVoted = bottomVote.userVoted;
        upvoteCount = bottomVote.voteCount;
        topVote.updateData(bottomVote.voteCount, bottomVote.userVoted, serverId);
    }
    // called upon inflation as well as whenever the viewpager is about to become visible
    private void updateBottomVote(){
        VoteInterface bottomVote = (VoteInterface) bottomView.findViewById(R.id.bottomVote);
        bottomVote.setBottomMode();
        VoteInterface topVote = (VoteInterface) getView().findViewById(R.id.topVote);
        userVoted = topVote.userVoted;
        upvoteCount = topVote.voteCount;
        bottomVote.updateData(topVote.voteCount, topVote.userVoted, serverId);
    }
    private String getSimpleTimeStamp(String timestamp) {
        SimpleDateFormat fromFormat = new SimpleDateFormat("H:mm:ss dd-MM-yyyy");
        SimpleDateFormat displayFormat = new SimpleDateFormat("K:mm a  d MMM yy");
        try {
            return displayFormat.format(fromFormat.parse(timestamp));
        } catch (Exception e) {
            return timestamp;
        }
    }

// ================= Comment Listener ================
    @Override
    public void sendComment(String comment) throws JSONException {
        setRetainInstance(true);
        JSONObject commentData = new JSONObject();
        ComplexPreferences cp = PrefUtils.getPrefs(getActivity());
        long timeSince = AddCommentBar.getLastCommentTimeStamp(serverId, getActivity());
        commentData.put(Contract.Comments.COLUMN_USERNAME, cp.getString(PrefUtils.USERNAME, ""))
                .put(Contract.Comments.COLUMN_TIMESTAMP, System.currentTimeMillis()/1000)
                .put(Contract.Comments.COLUMN_REPORT_ID, serverId)
                .put("last_comment_timestamp", timeSince)
                .put(Contract.Comments.COLUMN_CONTENT, comment);
        VoteInterface.recordUpvoteLog(getActivity(), commentData);
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        new CommentSender(this).execute(commentData);
        Log.e("SendComment!", commentData.toString());
    }

    @Override
    public void commentActionFinished(JSONObject result)
            throws RemoteException, OperationApplicationException, JSONException {
        VoteInterface.recordUpvoteLog(getActivity(), result);
        AddCommentBar.updateCommentsTable(result, getActivity());
        updateCommentsList();
        if((Integer) result.getInt(Contract.Comments.COLUMN_SERVER_ID) != null
                && addComment != null)
            addComment.clearText();
    }

    public void updateCommentsList(){
        String notSelect = " NOT NULL";
        String selection = Contract.Comments.COLUMN_REPORT_ID + " = "+ serverId + " AND "
                + Contract.Comments.COLUMN_USERNAME + notSelect + " AND "
                + Contract.Comments.COLUMN_TIMESTAMP + notSelect + " AND "
                + Contract.Comments.COLUMN_USERNAME + notSelect + " AND "
                + Contract.Comments.COLUMN_CONTENT + notSelect;
        String[] projection = new String[]{ Contract.Comments._ID,
                Contract.Comments.COLUMN_CONTENT,
                Contract.Comments.COLUMN_USERNAME,
                Contract.Comments.COLUMN_TIMESTAMP};
        String sort = Contract.Comments.COLUMN_TIMESTAMP + " ASC";
        Cursor c = getActivity().getContentResolver().query(Contract.Comments.COMMENTS_URI,
                projection, selection, null, sort);
        mAdapter.changeCursor(c);
        Log.e("Cursor count", "Cursor Count: " + c.getCount() + ". LV count: " + getListView().getCount());
        mAdapter.notifyDataSetChanged();
    }

    public void refreshComments(){
        try {
            long sinceTime = AddCommentBar.getLastCommentTimeStamp(serverId, getActivity());
            JSONObject refreshData = null;
            refreshData = new JSONObject().put("ReportId", serverId)
                                          .put("Since", sinceTime);
            new CommentRefresher(this).execute(VoteInterface.recordUpvoteLog(getActivity(), refreshData));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class ImageSlideAdapter extends FragmentPagerAdapter {
        String[] mediaPaths;
        public ImageSlideAdapter(FragmentManager fm, String[] mediaPaths) {
            super(fm);
            this.mediaPaths = mediaPaths;
        }
        @Override
        public int getCount() {return mediaPaths.length;}
        @Override
        public Fragment getItem(int i) {
            ImageFragment iF = new ImageFragment();
            Bundle args = new Bundle();
            args.putString("mediaPath", mediaPaths[i]);
            iF.setArguments(args);
            return iF;
        }
    }
}
