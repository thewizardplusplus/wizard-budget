package ru.thewizardplusplus.wizardbudget;

import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

import android.app.*;
import android.os.*;
import android.webkit.*;
import android.content.*;
import android.net.*;
import android.util.*;

import com.dropbox.client2.*;
import com.dropbox.client2.android.*;
import com.dropbox.client2.session.*;
import com.dropbox.client2.exception.*;
import org.json.*;

public class MainActivity extends Activity {
	@Override
	public void onBackPressed() {
		callGuiFunction("back");
	}

	@JavascriptInterface
	public void updateWidget() {
		Utils.updateWidget(this);
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
			AppKeyPair app_keys = getDropboxAppKeys();
			AndroidAuthSession session = new AndroidAuthSession(
				app_keys,
				dropbox_token
			);
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
		} else if (name.equals(Settings.SETTING_NAME_CURRENT_PAGE)) {
			return settings.getCurrentPage();
		} else if (name.equals(Settings.SETTING_NAME_CURRENT_SEGMENT)) {
			return settings.getCurrentSegment();
		} else if (name.equals(Settings.SETTING_NAME_ACTIVE_SPENDING)) {
			return settings.getActiveSpending();
		} else if (name.equals(Settings.SETTING_NAME_ACTIVE_BUY)) {
			return settings.getActiveBuy();
		} else if (name.equals(Settings.SETTING_NAME_STATS_RANGE)) {
			return String.valueOf(settings.getStatsRange());
		} else if (name.equals(Settings.SETTING_NAME_STATS_TAGS)) {
			return settings.getStatsTags();
		} else {
			return "";
		}
	}

	@JavascriptInterface
	public void setSetting(String name, String value) {
		Settings settings = Settings.getCurrent(this);
		if (name.equals(Settings.SETTING_NAME_CURRENT_PAGE)) {
			settings.setCurrentPage(value);
		} else if (name.equals(Settings.SETTING_NAME_CURRENT_SEGMENT)) {
			settings.setCurrentSegment(value);
		} else if (name.equals(Settings.SETTING_NAME_ACTIVE_SPENDING)) {
			settings.setActiveSpending(value);
		} else if (name.equals(Settings.SETTING_NAME_ACTIVE_BUY)) {
			settings.setActiveBuy(value);
		} else if (name.equals(Settings.SETTING_NAME_STATS_RANGE)) {
			settings.setStatsRange(Long.valueOf(value));
		} else if (name.equals(Settings.SETTING_NAME_STATS_TAGS)) {
			settings.setStatsTags(value);
		}
		settings.save();
	}

	@JavascriptInterface
	public void httpRequest(String name, String url) {
		try {
			final MainActivity self = this;
			final String name_copy = name;
			HttpRequestTask task = new HttpRequestTask(
				new HttpRequestTask.OnSuccessListener() {
					@Override
					public void onSuccess(String data) {
						self.callGuiFunction(
							"setHttpResult",
							new String[]{
								JSONObject.quote(name_copy),
								JSONObject.quote(data)
							}
						);
					}
				}
			);
			task.execute(new URL(url));
		} catch(MalformedURLException exception) {}
	}

	@JavascriptInterface
	public void log(String message) {
		Log.d("web", message);
	}

	@JavascriptInterface
	public void quit() {
		finish();
	}

	@Override
	protected void onCreate(Bundle saved_instance_state) {
		super.onCreate(saved_instance_state);
		setContentView(R.layout.main);

		String current_page =
			getIntent()
			.getStringExtra(Settings.SETTING_NAME_CURRENT_PAGE);
		if (current_page != null) {
			Settings settings = Settings.getCurrent(this);
			settings.setCurrentPage(current_page);
			settings.save();
		}

		WebView web_view = (WebView)findViewById(R.id.web_view);
		web_view.getSettings().setJavaScriptEnabled(true);
		web_view.addJavascriptInterface(this, "activity");
		SpendingManager spending_manager = new SpendingManager(this);
		web_view.addJavascriptInterface(spending_manager, "spending_manager");
		BuyManager buy_manager = new BuyManager(this);
		web_view.addJavascriptInterface(buy_manager, "buy_manager");
		BackupManager backup_manager = new BackupManager(this);
		web_view.addJavascriptInterface(backup_manager, "backup_manager");

		web_view.loadUrl("file:///android_asset/web/index.html");
	}

	@Override
	protected void onResume() {
		super.onResume();

		callGuiFunction("refresh");

		if (
			dropbox_api != null
			&& dropbox_api.getSession().authenticationSuccessful()
		) {
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

	private DropboxAPI<AndroidAuthSession> dropbox_api;
	private String backup_filename = "";

	private void callGuiFunction(String name, String[] arguments) {
		String arguments_string = "";
		for (int i = 0; i < arguments.length; i++) {
			if (i > 0) {
				arguments_string += ",";
			}

			arguments_string += arguments[i];
		}

		WebView web_view = (WebView)findViewById(R.id.web_view);
		web_view.loadUrl("javascript:GUI." + name + "(" + arguments_string + ")");
	}

	private void callGuiFunction(String name) {
		callGuiFunction(name, new String[]{});
	}

	private void restoreBackup(String filename) {
		File backup_file = new File(filename);
		InputStream in = null;
		try {
			try {
				in = new BufferedInputStream(new FileInputStream(backup_file));

				BackupManager backup_manager = new BackupManager(this);
				backup_manager.restore(in);
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
						dropbox_api.putFile(
							"/" + backup.getName(),
							in,
							backup.length(),
							null,
							null
						);

						if (
							Settings
							.getCurrent(context_copy)
							.isDropboxNotification()
						) {
							Date current_date = new Date();
							DateFormat notification_timestamp_format =
								DateFormat
								.getDateTimeInstance(
									DateFormat.DEFAULT,
									DateFormat.DEFAULT,
									Locale.US
								);
							String notification_timestamp =
								notification_timestamp_format
								.format(current_date);
							Utils.showNotification(
								context_copy,
								context_copy.getString(R.string.app_name),
								"Backup saved to Dropbox at "
									+ notification_timestamp + ".",
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
		AppKeyPair app_keys = getDropboxAppKeys();
		AndroidAuthSession session = new AndroidAuthSession(app_keys);
		dropbox_api = new DropboxAPI<AndroidAuthSession>(session);
		dropbox_api.getSession().startOAuth2Authentication(this);
	}

	private AppKeyPair getDropboxAppKeys() {
		Settings settings = Settings.getCurrent(this);
		String app_key = settings.getDropboxAppKey();
		String app_secret = settings.getDropboxAppSecret();
		return new AppKeyPair(app_key, app_secret);
	}
}
