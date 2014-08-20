package com.sc.mtaasafi.android;


import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class HomeScreen extends FragmentActivity {
    private MainFragment mainFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null){
            mainFragment = new MainFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, mainFragment)
                    .commit();
        } else {
            mainFragment = (MainFragment) getSupportFragmentManager()
                    .findFragmentById(android.R.id.content);
        }

        new HttpAsyncTask().execute("http://54.86.130.202:8080/");
    }

    public static String GET(String url){
        InputStream inputStream;
        String result = "";
        try{
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work";
        }catch (Exception e){
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    private boolean isConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls){
            return GET(urls[0]);
        }

        @Override
        protected void onPostExecute(String result){
            try{
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                int len = jsonArray.length();

                final List<String> listContent = new ArrayList<String>(len);
                for(int i = 0; i<len; i++){
                    try{
                        String message = jsonArray.getJSONObject(i).getString("message");
                        listContent.add(message);
                    }catch(Exception e){
                        Log.d("message", e.getLocalizedMessage());
                    }

                }
                ListView listView = (ListView) findViewById(R.id.listView);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(HomeScreen.this, PostView.class);
                        intent.putExtra("message", listContent.get(i));
                        startActivity(intent);
                    }
                });
                listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_expandable_list_item_1, listContent));

            }catch (JSONException e){
                Log.d("JSONObject", e.getLocalizedMessage());
            }

            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
        }
    }
}
