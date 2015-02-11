package com.sc.mtaa_safi.SystemUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NetworkUtils {

    public static boolean isOnline(Context context) {
        NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected())
            return true;
        return false;
    }

    public static JSONObject makeRequest(String url, String type, JSONObject entity) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpRequestBase httpRequest;
        if (type == "post") {
            httpRequest = new HttpPost(url);
            ((HttpPost) httpRequest).setEntity(new StringEntity(entity.toString()));
        } else
            httpRequest = new HttpGet(url);
        httpRequest.setHeader("Accept", "application/json");
        httpRequest.setHeader("Content-type", "application/json");
        HttpResponse response = httpClient.execute(httpRequest);
        if (response.getStatusLine().getStatusCode() > 400) { /*TODO: alert for statuses > 400*/ }
        return convertHttpResponseToJSON(response);
    }

    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder(inputStream.available());
        String line;
        while((line = bufferedReader.readLine()) != null)
            result.append(line);
        inputStream.close();
        Log.e("Network Utils: Server Response: ", result.toString());
        return result.toString();
    }

    public static JSONObject convertHttpResponseToJSON(HttpResponse response) {
        try {
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            Log.e("Network Utils: Server Response: ", responseString);
            return new JSONObject(responseString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
