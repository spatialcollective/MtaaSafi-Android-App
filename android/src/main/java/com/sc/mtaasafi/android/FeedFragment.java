package com.sc.mtaasafi.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.LoginButton;

import java.util.Arrays;
import java.util.List;
import java.util.zip.Inflater;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class FeedFragment extends ListFragment {

    private Button newPostButton;
    private Context context;
    private FeedAdapter fa;

    public FeedFragment(Context context) {
        super();
        this.context = context;
        fa = new FeedAdapter(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setListAdapter(fa);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);
//        ListView listView = (ListView) view.findViewById(R.id.list);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = new Intent(context, PostView.class);
//                intent.putExtra("content", (String) fa.getItem(i));
//                startActivity(intent);
//            }
//        });
        Log.d("onPostExecute", "Set listView listener");
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.authButton);
        loginButton.setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);
        loginButton.setFragment(this);
        loginButton.setPublishPermissions(Arrays.asList("publish_actions"));

        newPostButton = (Button) view.findViewById(R.id.newPostButton);
        newPostButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                newPost();
            }
        });
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())){
            onSessionStateChange(session, session.getState(), null);
        }
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause(){
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    public void updateFeed(List<FeedItem> newPosts){
        fa.updateFeed(newPosts);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            newPostButton.setVisibility(View.VISIBLE);
        } else if (state.isClosed()) {
            newPostButton.setVisibility(View.INVISIBLE);
        }
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private interface GraphObjectWithId extends GraphObject {
        String getId();
    }

    // TODO: Rework this interaction
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
                        params.putString("message", String.valueOf(input.getText()));
                        sendPost(params);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

    }

    private void sendPost(Bundle params){
        Request request = new Request(Session.getActiveSession(), "mtaasafi/feed", params, HttpMethod.POST, new Request.Callback() {
            @Override
            public void onCompleted(Response response) {
                FacebookRequestError error = response.getError();
                if(error == null){
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Success")
                            .setMessage(response.getGraphObject().cast(GraphObjectWithId.class).getId())
                            .setPositiveButton("Ok", null)
                            .show();
                }else{
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Error")
                            .setMessage(error.getErrorMessage())
                            .setPositiveButton("Ok", null)
                            .show();
                }
            }
        });
        request.executeAsync();
    }
}
