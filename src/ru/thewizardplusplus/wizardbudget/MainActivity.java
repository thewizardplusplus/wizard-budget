package ru.thewizardplusplus.wizardbudget;

import android.app.*;
import android.os.*;
import android.webkit.*;
import android.widget.*;
import android.content.*;
import android.appwidget.*;
import android.net.*;
import java.io.*;

public class MainActivity extends Activity {
	@Override
	public void onBackPressed() {
		callGuiFunction("back");
	}

	@JavascriptInterface
	public void updateWidget() {
		RemoteViews views = Widget.getUpdatedViews(this);
		ComponentName widget = new ComponentName(this, Widget.class);
		AppWidgetManager.getInstance(this).updateAppWidget(widget, views);
	}

	@JavascriptInterface
	public void selectBackupForRestore() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("text/xml");

		startActivityForResult(intent, FILE_SELECT_CODE);
	}

	@JavascriptInterface
	public void openSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	@JavascriptInterface
	public String getSetting(String name) {
		Settings settings = Settings.getCurrent(this);
		if (name.equals("use_custom_date")) {
			return settings.isUseCustomDate() ? "true" : "false";
		} else if (name.equals("current_page")) {
			return settings.getCurrentPage();
		} else {
			return "";
		}
	}

	@JavascriptInterface
	public void setSetting(String name, String value) {
		Settings settings = Settings.getCurrent(this);
		if (name.equals("current_page")) {
			settings.setCurrentPage(value);
		}
		settings.save();
	}

	@JavascriptInterface
	public void quit() {
		finish();
	}

	@Override
	protected void onCreate(Bundle saved_instance_state) {
		super.onCreate(saved_instance_state);
		setContentView(R.layout.main);

		WebView web_view = (WebView)findViewById(R.id.web_view);
		web_view.getSettings().setJavaScriptEnabled(true);
		web_view.loadUrl("file:///android_asset/web/index.html");

		SpendingManager spending_manager = new SpendingManager(this);
		web_view.addJavascriptInterface(spending_manager, "spending_manager");
		web_view.addJavascriptInterface(this, "activity");
	}

	@Override
	protected void onResume() {
		super.onResume();
		callGuiFunction("refresh");
	}

	@Override
	protected void onActivityResult(
		int request_code,
		int result_code,
		Intent data
	) {
		if (result_code == Activity.RESULT_OK && request_code == FILE_SELECT_CODE) {
			Uri uri = data.getData();
			if (uri != null) {
				String path = uri.getPath();
				if (path != null) {
					String[] path_parts = path.split(":");
					restoreBackup(
						Environment.getExternalStorageDirectory()
						+ "/"
						+ path_parts[path_parts.length > 1 ? 1 : 0]
					);

					callGuiFunction("refresh");
				}
			}
		}
	}

	private static final int FILE_SELECT_CODE = 1;

	private void callGuiFunction(String name) {
		WebView web_view = (WebView)findViewById(R.id.web_view);
		web_view.loadUrl("javascript:GUI." + name + "()");
	}

	private void restoreBackup(String filename) {
		File backup_file = new File(filename);
		InputStream in = null;
		try {
			try {
				in = new BufferedInputStream(new FileInputStream(backup_file));

				SpendingManager spending_manager = new SpendingManager(this);
				spending_manager.restore(in);
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (IOException exception) {}
	}
}
