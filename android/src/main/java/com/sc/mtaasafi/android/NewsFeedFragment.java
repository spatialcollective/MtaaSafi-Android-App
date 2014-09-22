package com.sc.mtaasafi.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

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
    private ImageButton sendPost, attachPic;
    private final String MESSAGE = "message";
    FeedAdapter fa;
    MultiAutoCompleteTextView et;
    MainActivity mActivity;
    byte[] mPic;
    int index;
    int top;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        fa = new FeedAdapter(mActivity, this);
        // Required empty public constructor
        setListAdapter(fa);
        index = top = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_feed, container, false);
        if(savedInstanceState !=null){
            index = savedInstanceState.getInt("index");
            top = savedInstanceState.getInt("top");
        }
        et = (MultiAutoCompleteTextView) view.findViewById(R.id.newPostText);
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        attachPic = (ImageButton) view.findViewById(R.id.attachPic);
        attachPic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                attachPic();
            }
        });
        sendPost = (ImageButton) view.findViewById(R.id.sendPost);
        sendPost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendPost();
            }
        });
        return view;
    }

    private interface GraphObjectWithId extends GraphObject {
        String getId();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            sendPost.setClickable(true);
        } else if (state.isClosed()) {
            sendPost.setClickable(false);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        restoreListPosition();
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())){
            onSessionStateChange(session, session.getState(), null);
        }
    }

    private void newPost(){
//        final Bundle params = new Bundle();
//        final EditText input = new EditText(getActivity());
//        input.setSingleLine(false);
//        params.putString("display", "page");
//        new AlertDialog.Builder(getActivity())
//                .setTitle("New Post")
//                .setView(input)
//                .setPositiveButton("Post", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        params.putString(MESSAGE, String.valueOf(input.getText()));
//                        sendPost(params);
//                    }
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
    }

    public void updateFeed(List<PostData> posts){
        fa.updateFeed(posts);
    }

    public void saveListPosition(){
        index = getListView().getFirstVisiblePosition();
        View v = getListView().getChildAt(0);
        top = (v == null) ? 0 : v.getTop();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        bundle.putInt("top", top);
        onSaveInstanceState(bundle);
    }
    public void restoreListPosition(){
        getListView().setSelectionFromTop(index, top);
    }
    public void onPause(){
        super.onPause();
        saveListPosition();
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

    }
    private void attachPic(){
        
    }
    private void sendPost(){
        // get contents from edit text
        // check if there is a pic to attach.
        // put those together in the bundle I guess?
        if(et.getText() == null || et.getText().equals("")){
            Toast toast = Toast.makeText(mActivity, "Enter text for your post", Toast.LENGTH_SHORT);
        }
        else{
            String content = et.getText().toString();
            String timestamp = new SimpleDateFormat("yyyy-MM-DD'T'H:mm:ss")
                    .format(new java.util.Date(System.currentTimeMillis()));
            Location location = mActivity.getLocation();
            if(mPic != null) {
            }
            mActivity.beamItUp(new PostData("Agree", timestamp, location.getLatitude (),
                    location.getLongitude(), content));

        }
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
