package com.sc.mtaasafi.android;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Agree on 9/4/2014.
 * NewsFeedLoader receives posts written by the user from the main activity.
 * It also gives the main activity posts to put in the FeedFragment.
 * NewsFeedLoader takes posts at POJOs, converts them to JSON-formatted strings and
 * then pushes them to the server via ServerRelay, which communicates directly with the server
 * at the byte level.
 */
public class NewsFeedLoader extends AsyncTaskLoader<List<Report>> {

    List<Report> mReports;
    Context context;

//    public interface NewsFeedLoaderCallbacks{
//        void onFeedUpdate(List<Report> posts);
//        int getScreenWidth();
//        void onUpdateFailed();
//        void backupDataToFile(String dataString) throws IOException;
//        String getJsonStringFromFile() throws IOException;
//        void updatePendingReportProgress(int progress);
//        void onReportUploadSuccess();
//        void onUploadFailed(String failMessage);
//    }
//
//    public NewsFeedLoaderCallbacks mActivity;
    private int currentField;

    private static final String BASE_WRITE_URL = "http://app.spatialcollective.com/add_post",
                                READ_URL = "http://app.spatialcollective.com/get_posts/",
                                NEXT_REPORT_PIECE_KEY = "nextfield",
                                REPORT_ID_KEY = "pid";

    public NewsFeedLoader(Context context) {
        super(context);
//        mActivity = activity;
        this.context = context;
        Log.d("Constructing", "done");
    }

    @Override
    public List<Report> loadInBackground() {
//        final Context context = getContext();
        Log.d("Backgrounding", "start");
        return GET(READ_URL + 400);//context.getScreenWidth()
    }

    @Override
    public void deliverResult(List<Report> reports) {
        // activity.onFeedUpdate(result);
        Log.d("Delivering", reports.toString());
        mReports = reports;
        if (isStarted())
            super.deliverResult(reports);
    }
    
    @Override protected void onStartLoading() {
        Log.d("Start Loading", "...");
        if (mReports != null)
            deliverResult(mReports);
    }

    private List<Report> GET(String url) {
        String resultString;
        JSONArray resultJson;

        try {
            resultString = getDataFromServer(url);
            resultJson = convertStringToJson(resultString);
            // activity.backupDataToFile(resultString);
            return createReportsFromJson(resultJson);
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                resultString = "{'hello': 'there'}";//activity.getJsonStringFromFile();
                resultJson = convertStringToJson(resultString);
                return createReportsFromJson(resultJson);
            } catch (Exception e) {
                // activity.onUpdateFailed();
            }
        }
        return new ArrayList<Report>();
    }

    private String getDataFromServer(String url) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
        InputStream inputStream = httpResponse.getEntity().getContent();
        if (inputStream != null)
            return convertInputStreamToString(inputStream);
        return "error";
    }

    private List<Report> createReportsFromJson(JSONArray jsonData) throws JSONException {
        int len = jsonData.length();
        List<Report> listContent = new ArrayList<Report>(len);
        for (int i = 0; i < len; i++)
            listContent.add(new Report(jsonData.getJSONObject(i)));
        return listContent;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder(inputStream.available());
        String line;

        while((line = bufferedReader.readLine()) != null)
            result.append(line);
        inputStream.close();
        return result.toString();
    }

    private JSONArray convertStringToJson(String input) throws JSONException {
        JSONArray jsonArray = new JSONArray(input);
        if (jsonArray.length() == 1 && jsonArray.getJSONObject(0).getString("error") != null)
            throw new JSONException("Server returned error");
        return jsonArray;
    }
}
