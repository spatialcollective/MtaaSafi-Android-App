package com.sc.mtaasafi.android;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

public class NewReportUploader extends AsyncTask<Report, Integer, Integer> {

    NewReportFragment mFragment;
    private static final String BASE_WRITE_URL = "http://app.spatialcollective.com/add_post",
                               	NEXT_REPORT_PIECE_KEY = "nextfield",
                               	REPORT_ID_KEY = "pid";

    public NewReportUploader(NewReportFragment fragment) { mFragment = fragment; }

	@Override
	protected Integer doInBackground(Report... report) {
		Log.e(LogTags.BACKEND_W, "ServerCommunicater.writePost");
		try {
			return writeReportToServer(report[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void onProgressUpdate(Integer... progress) {
		mFragment.updatePostProgress(progress[0]);
	}

	@Override
	protected void onPostExecute(Integer result) {
       mFragment.uploadSuccess();
//		   mFragment.uploadFailure("Unknown Error");
	}

	private int writeReportToServer(Report report) throws JSONException, IOException {
		try {
			HttpResponse response = sendRequest(new Long(0), report.getJsonForText().toString());
			JSONObject jsonResponse = processResponse(response);
			int nextPieceKey = jsonResponse.getInt(NEXT_REPORT_PIECE_KEY);
			report.id = jsonResponse.getInt(REPORT_ID_KEY);
			while (nextPieceKey != -1) {
				jsonResponse = writePieceToServer(report, nextPieceKey);
				nextPieceKey = jsonResponse.getInt(NEXT_REPORT_PIECE_KEY);
			}
			updateProgress(nextPieceKey);
			return nextPieceKey;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private void updateProgress(int nextPiece) {
		Integer[] progress = new Integer[1];
		progress[0] = Integer.valueOf(nextPiece);
		publishProgress(progress);
	}

	private JSONObject writePieceToServer(Report report, int nextPieceKey) throws JSONException, IOException {
		HttpResponse response = sendRequest(report.id, report.getJsonForPic(nextPieceKey).toString());
		updateProgress(nextPieceKey);
		return processResponse(response);
	}

	private JSONObject processResponse(HttpResponse response) throws JSONException, IOException {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode >= 400 && statusCode < 500)
			mFragment.uploadFailure("Client error");
		else if (statusCode >= 500 && statusCode < 600)
			mFragment.uploadFailure("Server error");
		String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
		return new JSONObject(responseString);
	}

	private HttpResponse sendRequest(Long reportId, String contents) throws IOException {
		HttpClient httpclient = new DefaultHttpClient();
		String writeUrl = BASE_WRITE_URL;
		if (reportId != 0)
			writeUrl += "/" + reportId + "/";
		HttpPost httpPost = new HttpPost(writeUrl);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setEntity(new StringEntity(contents));
		return httpclient.execute(httpPost);
	}
}