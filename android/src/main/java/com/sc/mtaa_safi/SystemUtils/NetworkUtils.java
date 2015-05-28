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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {

    public static boolean isOnline(Context context) {
        NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected())
            return true;
        return false;
    }

    public static JSONObject makeRequest(String url, String type, JSONObject entity) throws IOException, JSONException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpRequestBase httpRequest;
        Log.v("Network utils", "data: "+String.valueOf(entity));
        Log.v("Network utils", "requesting from: " + url);
        if (type == "post") {
            httpRequest = new HttpPost(url);
            ((HttpPost) httpRequest).setEntity(new StringEntity(entity.toString()));
        } else
            httpRequest = new HttpGet(url);
        httpRequest.setHeader("Accept", "application/json");
        httpRequest.setHeader("Content-type", "application/json");
        HttpResponse response = httpClient.execute(httpRequest);
        Log.v("Network Utils", "Response Code: " + response.getStatusLine().toString());
        if (response.getStatusLine().getStatusCode() >= 400) {
            JSONObject json = new JSONObject();
            return json.put("error", response.getStatusLine().toString());
        }
        return convertHttpResponseToJSON(response);
    }

    public static JSONObject streamRequest(String urlString, byte[] data) throws IOException, JSONException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.setConnectTimeout(15000);
        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);
        urlConnection.connect();
        DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
        // Log.v("BYTES 2 SERVER", "Bytes being sent:" + bytes.length);
        out.write(data);
        out.flush();
        out.close();
        InputStream is = urlConnection.getInputStream();
        String response = NetworkUtils.convertInputStreamToString(is);
        is.close();
        urlConnection.disconnect();
        return new JSONObject(response);
    }

    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder(inputStream.available());
        String line;
        while ((line = bufferedReader.readLine()) != null)
            result.append(line);
        inputStream.close();
        Log.v("Network Utils", "Server Response: " + result.toString());
        return result.toString();
    }

    public static JSONObject convertHttpResponseToJSON(HttpResponse response) {
        try {
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            Log.v("Network Utils", "Server Response: " + responseString);
            return new JSONObject(responseString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
