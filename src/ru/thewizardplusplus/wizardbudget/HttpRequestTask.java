package ru.thewizardplusplus.wizardbudget;

import java.net.*;

import android.util.*;
import android.os.*;
import org.apache.http.client.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.*;
import java.io.*;

public class HttpRequestTask extends AsyncTask<URL, Void, String> {
	public interface OnSuccessListener {
		public void onSuccess(String data);
	}

	public HttpRequestTask(OnSuccessListener success_listener) {
		this.success_listener = success_listener;
	}

	@Override
	protected String doInBackground(URL... urls) {
		try {
			HttpGet request = new HttpGet();
			request.setURI(urls[0].toURI());

			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(request);

			StatusLine status = response.getStatusLine();
			int status_code = status.getStatusCode();
			if (status_code != 200) {
				return "error:" + String.valueOf(status_code) + " " + status.getReasonPhrase();
			}

			BufferedReader in = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent())
			);
			StringBuffer buffer = new StringBuffer();
			try {
				while (true) {
					String line = in.readLine();
					if (line == null) {
						break;
					}
					buffer.append(line);
				}
			} catch (IOException exception) {
				return "error:" + exception.getMessage();
			} finally {
				in.close();
			}

			return buffer.toString();
		} catch (URISyntaxException exception) {
			return "error:" + exception.getMessage();
		} catch (ClientProtocolException exception) {
			return "error:" + exception.getMessage();
		} catch (IOException exception) {
			return "error:" + exception.getMessage();
		}
	}

	@Override
	protected void onPostExecute(String result) {
		success_listener.onSuccess(result);
	}

	private OnSuccessListener success_listener;
}
