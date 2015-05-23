package ru.thewizardplusplus.wizardbudget;

import java.net.*;

import android.util.*;
import android.os.*;

public class HttpRequestTask extends AsyncTask<URL, Void, String> {
	public interface OnSuccessListener {
		public void onSuccess(String data);
	}

	public HttpRequestTask(OnSuccessListener success_listener) {
		this.success_listener = success_listener;
	}

	@Override
	protected String doInBackground(URL... urls) {
		URL url = urls[0];
		try {
			Thread.currentThread().sleep(5000);
		} catch (InterruptedException exception) {}

		return "\"" + url.toString() + "\" loaded.";
	}

	@Override
	protected void onPostExecute(String result) {
		success_listener.onSuccess(result);
	}

	private OnSuccessListener success_listener;
}
