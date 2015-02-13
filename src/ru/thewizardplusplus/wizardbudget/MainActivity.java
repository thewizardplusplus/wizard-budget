package ru.thewizardplusplus.wizardbudget;

import java.io.*;
import java.security.*;

import android.app.*;
import android.os.*;
import android.webkit.*;
import android.widget.*;
import android.content.*;
import android.appwidget.*;
import android.net.*;
import android.util.*;
import com.dropbox.client2.*;
import com.dropbox.client2.android.*;
import com.dropbox.client2.session.*;
import com.dropbox.client2.exception.*;
import java.text.*;
import java.util.*;

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
	public void saveToDropbox(String filename) {
		backup_filename = filename;

		String dropbox_token = Settings.getCurrent(this).getDropboxToken();
		if (!dropbox_token.isEmpty()) {
			AndroidAuthSession session = new AndroidAuthSession(APP_KEYS, dropbox_token);
			dropbox_api = new DropboxAPI<AndroidAuthSession>(session);

			saveBackupToDropbox();
		} else {
			startDropboxAuthentication();
		}
	}

	@JavascriptInterface
	public void openSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	@JavascriptInterface
	public String getSetting(String name) {
		Settings settings = Settings.getCurrent(this);
		if (name.equals("credit_card_tag")) {
			return settings.getCreditCardTag();
		} else if (name.equals("save_backup_to_dropbox")) {
			return settings.isSaveBackupToDropbox() ? "true" : "false";
		} else if (name.equals("use_custom_date")) {
			return settings.isUseCustomDate() ? "true" : "false";
		} else if (name.equals(Settings.SETTING_NAME_CURRENT_PAGE)) {
			return settings.getCurrentPage();
		} else if (name.equals(Settings.SETTING_NAME_ACTIVE_SPENDING)) {
			return settings.getActiveSpending();
		} else {
			return "";
		}
	}

	@JavascriptInterface
	public void setSetting(String name, String value) {
		Settings settings = Settings.getCurrent(this);
		if (name.equals(Settings.SETTING_NAME_CURRENT_PAGE)) {
			settings.setCurrentPage(value);
		} else if (name.equals(Settings.SETTING_NAME_ACTIVE_SPENDING)) {
			settings.setActiveSpending(value);
		}
		settings.save();
	}

	@JavascriptInterface
	public void log(String message) {
		Log.d("Web", message);
	}

	@JavascriptInterface
	public void quit() {
		finish();
	}

	@Override
	protected void onCreate(Bundle saved_instance_state) {
		super.onCreate(saved_instance_state);
		setContentView(R.layout.main);

		String current_page = getIntent().getStringExtra(Settings.SETTING_NAME_CURRENT_PAGE);
		if (current_page != null) {
			Settings settings = Settings.getCurrent(this);
			settings.setCurrentPage(current_page);
			settings.save();
		}

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

		if (dropbox_api != null && dropbox_api.getSession().authenticationSuccessful()) {
			try {
				dropbox_api.getSession().finishAuthentication();

				String token = dropbox_api.getSession().getOAuth2AccessToken();
				Settings settings = Settings.getCurrent(this);
				settings.setDropboxToken(token);
				settings.save();

				saveBackupToDropbox();
			} catch (IllegalStateException exception) {}
		}
	}

	@Override
	protected void onActivityResult(
		int request_code,
		int result_code,
		Intent data
	) {
		if (
			result_code == Activity.RESULT_OK
			&& request_code == FILE_SELECT_CODE
		) {
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
	private static final String APP_KEY = "g0395gpeyf78f9o";
	private static final String APP_SECRET = "ahg9sdct9vtmxxb";
	private static final AppKeyPair APP_KEYS = new AppKeyPair(APP_KEY, APP_SECRET);

	private DropboxAPI<AndroidAuthSession> dropbox_api;
	private String backup_filename = "";

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

	private void saveBackupToDropbox() {
		final Context context_copy = this;
		new Thread(
			new Runnable() {
				@Override
				public void run() {
					try {
						File backup = new File(backup_filename);
						FileInputStream in = new FileInputStream(backup);
						dropbox_api.putFile("/" + backup.getName(), in, backup.length(), null, null);

						if (Settings.getCurrent(context_copy).isDropboxNotification()) {
							Date current_date = new Date();
							DateFormat notification_timestamp_format = DateFormat
								.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
							String notification_timestamp = notification_timestamp_format
								.format(current_date);
							Utils.showNotification(
								context_copy,
								context_copy.getString(R.string.app_name),
								"Backup saved to Dropbox at " + notification_timestamp + ".",
								null
							);
						}
					} catch (FileNotFoundException exception) {
					} catch (DropboxException exception) {}
				}
			}
		).start();
	}

	private void startDropboxAuthentication() {
		AndroidAuthSession session = new AndroidAuthSession(APP_KEYS);
		dropbox_api = new DropboxAPI<AndroidAuthSession>(session);
		dropbox_api.getSession().startOAuth2Authentication(this);
	}
}
