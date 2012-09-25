package com.pingtest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class PingReceiver extends BroadcastReceiver {
	private boolean isUploading = false;
	private boolean allowUpload = false;
	private Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		if (intent.getAction().equals(PingtestActions.ACTION_START_UPLOAD)) {
			Log.v("Upload", "START");

			allowUpload = true;

			if (!isUploading) {
				UploadAsyncTask task = new UploadAsyncTask();
				task.execute();
			}

		} else if (intent.getAction()
				.equals(PingtestActions.ACTION_STOP_UPLOAD)) {

			Log.v("Upload", "STOP");
			allowUpload = false;

		}
	}

	private class UploadAsyncTask extends AsyncTask<Void, Void, Void> {

		DatabaseOperator dbo;
		List<CellInfo> cellInfos;
		List<Long> ids;

		private HttpClient httpClient;
		private HttpParams httpParams;
		private HttpResponse httpResponse;
		private HttpPost httpPost;
		private static final int TIMEOUT = 5000;
		private static final String URL = "http://sskaje.me/sskaje/test.php";

		protected void onPreExecute() {

			Log.i("Upload", "prepare upload");
			dbo = new DatabaseOperator(PingReceiver.this.context);
			cellInfos = dbo.queryUnupload();
			Log.i("Upload", "cellInfos.size() = " + cellInfos.size());
			ids = new ArrayList<Long>();

			PingReceiver.this.isUploading = true;

		}

		protected Void doInBackground(Void... arg0) {

			String str = new String();
			// Cat str
			for (int i = 0; i < cellInfos.size(); i++) {

				try {
					ids.add(cellInfos.get(i).id);

					cellInfos.get(i).put("formatTimestamp", cellInfos.get(i).fTimestamp);
					
					Iterator iterator = cellInfos.get(i).keys();
					while (iterator.hasNext()) {

						String key = (String) iterator.next();

						str += key + "=" + cellInfos.get(i).get(key).toString()
								+ "\t";
					}
					str = str.substring(0, str.length() - 2) + "\n";
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			Log.i("Upload", "ids.size() = " + ids.size());

			httpParams = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);

			String userAgent = "PingTest/Android";
			HttpProtocolParams.setUserAgent(httpParams, userAgent);

			httpClient = new DefaultHttpClient(httpParams);

			httpPost = new HttpPost(URL);

			try {
				httpPost.setEntity(new StringEntity(str, HTTP.UTF_8));
				httpResponse = httpClient.execute(httpPost);

				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					if (httpResponse.getEntity() != null) {

						dbo.updateUnuloadToOk(ids);
						Log.i("Upload", "upload succeed");
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Random random = new Random();
			// if (random.nextInt() % 100 > 80) {
			// dbo.updateUnuloadToOk(cellInfo.id);
			// Log.i("Upload", "upload succeed");
			// } else {
			// Log.i("Upload", "upload failed");
			// }
			return null;
		}

		protected void onPostExecute(Void params) {

			PingReceiver.this.isUploading = false;
			// sleep 3s
			try {
				Log.i("Upload", "sleep...");
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if ((dbo.queryUnuploadCount() > 0) && allowUpload) {
				Log.i("Upload", "continue upload");
				UploadAsyncTask task = new UploadAsyncTask();
				task.execute();
			} else if (!allowUpload) {
				Log.i("Upload", "not allow upload");
			} else {
				Log.i("Upload", "no need to upload");
			}
		}

	}

}
