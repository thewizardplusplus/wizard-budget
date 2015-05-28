package ru.thewizardplusplus.wizardbudget;

import java.net.*;
import java.io.*;
import java.util.*;

import android.util.*;
import android.os.*;

import org.apache.http.client.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.*;

public class HttpRequestTask extends AsyncTask<URL, Void, String> {
	public interface OnSuccessListener {
		public void onSuccess(String data);
	}

	public HttpRequestTask(
		Map<String, String> headers,
		OnSuccessListener success_listener
	) {
		this.headers = headers;
		this.success_listener = success_listener;
	}

	@Override
	protected String doInBackground(URL... urls) {
		try {
			HttpGet request = new HttpGet();
			request.setURI(urls[0].toURI());
			for (Map.Entry<String, String> header: headers.entrySet()) {
				request.setHeader(header.getKey(), header.getValue());
			}

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

	private Map<String, String> headers;
	private OnSuccessListener success_listener;
}
