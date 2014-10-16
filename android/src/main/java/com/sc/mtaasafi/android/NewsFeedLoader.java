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

// Created by Agree on 9/4/2014.

public class NewsFeedLoader extends AsyncTaskLoader<List<Report>> {

    List<Report> mReports;
    MainActivity mActivity;

    private static final String READ_URL = "http://app.spatialcollective.com/get_posts/";

    public NewsFeedLoader(Context context) {
        super(context);
        mActivity = (MainActivity) context;
    }

    @Override
    public List<Report> loadInBackground() {
        return GET(READ_URL + mActivity.getScreenWidth());
    }

    @Override
    public void deliverResult(List<Report> reports) {
        mReports = reports;
        if (isStarted())
            super.deliverResult(reports);
    }
    
    @Override
    protected void onStartLoading() {
        List<Report> savedReports = getReportsFromFile();
        if (mReports == null && savedReports.size() > 0)
            mReports = savedReports;
        if (mReports != null)
            deliverResult(mReports);
    }

    private List<Report> GET(String url) {
        try {
            String resultString = getDataFromServer(url);
            JSONArray resultJson = convertStringToJson(resultString);
            mActivity.backupDataToFile(resultString);
            return createReportsFromJson(resultJson);
        } catch (Exception ex) {
            e.printStackTrace();
        }
        return new ArrayList<Report>();
    }

    private List<Report> getReportsFromFile() {
        String resultString = mActivity.getJsonStringFromFile();
        JSONArray resultJson = convertStringToJson(resultString);
        return createReportsFromJson(resultJson);
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
