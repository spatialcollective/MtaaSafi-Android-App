package com.sc.mtaasafi.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import java.text.SimpleDateFormat;
import java.util.List;

public class NewsFeedFragment extends ListFragment {
    private Button newPostButton;
    private final String MESSAGE = "message";
    FeedAdapter fa;
    MainActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        fa = new FeedAdapter(mActivity);
        // Required empty public constructor
        setListAdapter(fa);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_feed, container, false);
        newPostButton = (Button) view.findViewById(R.id.newPostButton);
        newPostButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                newPost();
            }
        });
        return view;
    }

    private interface GraphObjectWithId extends GraphObject {
        String getId();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            newPostButton.setVisibility(View.VISIBLE);
        } else if (state.isClosed()) {
            newPostButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())){
            onSessionStateChange(session, session.getState(), null);
        }
    }

    private void newPost(){
        final Bundle params = new Bundle();
        final EditText input = new EditText(getActivity());
        input.setSingleLine(false);
        params.putString("display", "page");
        new AlertDialog.Builder(getActivity())
                .setTitle("New Post")
                .setView(input)
                .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        params.putString(MESSAGE, String.valueOf(input.getText()));
                        sendPost(params);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void updateFeed(List<PostData> posts){
        fa.updateFeed(posts);
    }

    private void sendPost(Bundle params){
        String timestamp = new SimpleDateFormat("yyyy-MM-DD'T'H:mm:ss")
                .format(new java.util.Date (System.currentTimeMillis()));
        Location location = mActivity.getLocation();
        String content = (String) params.get(MESSAGE);
        mActivity.beamItUp(new PostData("Agree", timestamp, location.getLatitude (),
                location.getLongitude(), content));
//        Request request = new Request(Session.getActiveSession(), "mtaasafi/feed", params, HttpMethod.POST, new Request.Callback() {
//            @Override
//            public void onCompleted(Response response) {
//                FacebookRequestError error = response.getError();
//                if(error == null){
//                    new AlertDialog.Builder(getActivity())
//                            .setTitle("Success")
//                            .setMessage(response.getGraphObject().cast(GraphObjectWithId.class).getId())
//                            .setPositiveButton("Ok", null)
//                            .show();
//                }else{
//                    new AlertDialog.Builder(getActivity())
//                            .setTitle("Error")
//                            .setMessage(error.getErrorMessage())
//                            .setPositiveButton("Ok", null)
//                            .show();
//                }
//            }
//        });
//        request.executeAsync();
    }
}
