package ru.thewizardplusplus.wizardbudget;

import java.net.*;

import android.util.*;
import android.os.*;

public class HttpRequestTask extends AsyncTask<URL, Void, String> {
	public interface OnSuccessListener {
		public void onClick(String data);
	}

	public HttpRequestTask(OnSuccessListener success_listener) {
		this.success_listener = success_listener;
	}

	@Override
	protected String doInBackground(URL... urls) {
		try {
			Thread.currentThread().sleep(5000);
		} catch (InterruptedException exception) {}

		return "test";
	}

	@Override
	protected void onPostExecute(String result) {
		success_listener.onClick(result);
	}

	private OnSuccessListener success_listener;
}
