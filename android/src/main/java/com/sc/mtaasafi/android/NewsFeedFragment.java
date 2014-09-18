package com.sc.mtaasafi.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewsFeedFragment extends ListFragment {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Button newPostButton;
    private Button newPhotoButton;
    String currentPhotoPath;
    private final String MESSAGE = "message";
    private final String PHOTO = "source";
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

        newPhotoButton = (Button) view.findViewById(R.id.newPhotoButton);
        newPhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        takePicture();
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

    public void updateFeed(List<FeedItem> posts){
        fa.updateFeed(posts);
    }

    private void sendPost(Bundle params){
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss")
                .format(new java.util.Date (System.currentTimeMillis()));
        Location location = mActivity.getLocation();
        String content = (String) params.get(MESSAGE);
        byte[] photo = (byte[]) params.get(PHOTO);
        mActivity.beamItUp(new PostData("Agree", timestamp, location.getLatitude(),
                location.getLongitude(), content, photo));
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

    private void takePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex){
                Toast.makeText(getActivity(), "Couldn't create file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null){
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            final Bundle params = new Bundle();
            final EditText input = new EditText(getActivity());
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            Log.e("BYTEARRAY", bytearrayoutputstream.toString());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytearrayoutputstream);
            final byte[] bytearray = bytearrayoutputstream.toByteArray();
            new AlertDialog.Builder(getActivity())
                    .setTitle("New Post")
                    .setView(input)
                    .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            params.putString(MESSAGE, String.valueOf(input.getText()));
                            params.putByteArray(PHOTO, bytearray);
                            sendPost(params);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            params.putString("message", "Photo from the app");

        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
