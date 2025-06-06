package ru.thewizardplusplus.wizardbudget;

import java.io.*;
import java.util.*;
import java.net.*;

import org.json.*;

import android.annotation.*;
import android.app.*;
import android.os.*;
import android.webkit.*;
import android.content.*;
import android.net.*;
import android.util.*;

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
	public void updateBuyWidget() {
		Utils.updateBuyWidget(this);
	}

	@JavascriptInterface
	public void updateCurrencyWidget() {
		Utils.updateCurrencyWidget(this);
	}

	@JavascriptInterface
	public void updateLimitWidget() {
		Utils.updateLimitWidget(this);
	}

	@JavascriptInterface
	public void selectBackupForRestore() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("text/xml");

		startActivityForResult(intent, FILE_SELECT_CODE);
	}

	@JavascriptInterface
	public void saveToDropbox(String filename) {
		dropbox = new DropboxClient(this, filename, false);
		dropbox.saveFile();
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
		} else if (name.equals("collect_stats")) {
			return settings.isCollectStats() ? "true" : "false";
		} else if (name.equals(Settings.SETTING_NAME_STATS_RANGE)) {
			return String.valueOf(settings.getStatsRange());
		} else if (name.equals(Settings.SETTING_NAME_STATS_TAGS)) {
			return settings.getStatsTags();
		} else if (name.equals(Settings.SETTING_NAME_HOURS_START_DATE)) {
			return settings.getHoursStartDate();
		} else if (name.equals(Settings.SETTING_NAME_HOURS_END_DATE)) {
			return settings.getHoursEndDate();
		} else if (name.equals("analysis_harvest")) {
			return settings.isAnalysisHarvest() ? "true" : "false";
		} else if (name.equals("harvest_username")) {
			return settings.getHarvestUsername();
		} else if (name.equals("harvest_password")) {
			return settings.getHarvestPassword();
		} else if (name.equals("harvest_subdomain")) {
			return settings.getHarvestSubdomain();
		} else if (name.equals("working_off_limit")) {
			return String.valueOf(settings.getWorkingOffLimit());
		} else if (name.equals(Settings.SETTING_NAME_WORKED_HOURS)) {
			return settings.getWorkedHours();
		} else if (name.equals(Settings.SETTING_NAME_WORK_CALENDAR)) {
			return settings.getWorkCalendar();
		} else if (name.equals(Settings.SETTING_NAME_HOURS_DATA)) {
			return settings.getHoursData();
		} else if (name.equals(Settings.SETTING_NAME_NEED_UPDATE_HOURS)) {
			return settings.isNeedUpdateHours() ? "true" : "false";
		} else if (name.equals(Settings.SETTING_NAME_CURRENCY_LIST_MODE)) {
			return settings.getCurrencyListMode();
		} else if (name.equals(Settings.SETTING_NAME_EXCHANGE_RATE_API_KEY)) {
			return settings.getExchangeRateApiKey();
		} else if (name.equals(Settings.SETTING_NAME_USED_CURRENCIES)) {
			return settings.getUsedCurrencies();
		} else if (name.equals("consider_limits")) {
			return settings.isConsiderLimits() ? "true" : "false";
		} else if (name.equals(Settings.SETTING_NAME_LIMIT_DAYS)) {
			return settings.getLimitDaysAsString();
		} else if (name.equals(Settings.SETTING_NAME_LIMIT_AMOUNT)) {
			return String.valueOf(settings.getLimitAmount());
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
		} else if (name.equals(Settings.SETTING_NAME_HOURS_START_DATE)) {
			settings.setHoursStartDate(value);
		} else if (name.equals(Settings.SETTING_NAME_HOURS_END_DATE)) {
			settings.setHoursEndDate(value);
		} else if (name.equals(Settings.SETTING_NAME_WORKED_HOURS)) {
			settings.setWorkedHours(value);
		} else if (name.equals(Settings.SETTING_NAME_WORK_CALENDAR)) {
			settings.setWorkCalendar(value);
		} else if (name.equals(Settings.SETTING_NAME_HOURS_DATA)) {
			settings.setHoursData(value);
		} else if (name.equals(Settings.SETTING_NAME_NEED_UPDATE_HOURS)) {
			settings.setNeedUpdateHours(value.equals("true"));
		}
		settings.save();
	}

	@JavascriptInterface
	public void httpRequest(String name, String url, String headers) {
		try {
			callGuiFunction(
				"addLoadingLogMessage",
				new String[]{
					JSONObject.quote(
						"Start the "
							+ JSONObject.quote(name)
							+ " HTTP request."
					)
				}
			);

			Map<String, String> header_map = new HashMap<String, String>();
			try {
				JSONObject headers_json = new JSONObject(headers);
				@SuppressWarnings("unchecked")
				Iterator<String> i = headers_json.keys();
				while (i.hasNext()) {
					String header_name = i.next();
					String header_value = headers_json.optString(header_name);
					header_map.put(header_name, header_value.toString());
				}
			} catch(JSONException exception) {}

			final MainActivity self = this;
			final String name_copy = name;
			HttpRequestTask task = new HttpRequestTask(
				header_map,
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
	public void notify(String message) {
		Utils.showNotification(this, "Web", message, null);
	}

	@JavascriptInterface
	public void quit() {
		finish();
	}

	@Override
	@SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle saved_instance_state) {
		super.onCreate(saved_instance_state);
		setContentView(R.layout.main);

		Utils.setAlarms(this);

		String current_page =
			getIntent()
			.getStringExtra(Settings.SETTING_NAME_CURRENT_PAGE);
		if (current_page != null) {
			Settings settings = Settings.getCurrent(this);
			settings.setCurrentPage(current_page);
			settings.save();
		}

		String current_segment =
			getIntent()
			.getStringExtra(Settings.SETTING_NAME_CURRENT_SEGMENT);
		if (current_segment != null) {
			Settings settings = Settings.getCurrent(this);
			settings.setCurrentSegment(current_segment);
			settings.save();
		}

		boolean need_update_hours =
			getIntent()
			.getBooleanExtra(Settings.SETTING_NAME_NEED_UPDATE_HOURS, false);
		if (need_update_hours) {
			Settings settings = Settings.getCurrent(this);
			settings.setNeedUpdateHours(true);
			settings.save();
		}

		WebView web_view = (WebView)findViewById(R.id.web_view);
		web_view.getSettings().setJavaScriptEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			web_view.getSettings().setAllowUniversalAccessFromFileURLs(true);
		}
		web_view.setWebViewClient(
			new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(
					WebView view,
					String url
				) {
					try {
						Intent intent = new Intent(
							Intent.ACTION_VIEW,
							Uri.parse(url)
						);
						startActivity(intent);
					} catch(ActivityNotFoundException exception) {}

					return true;
				}
			}
		);

		final MainActivity self = this;
		web_view.setWebChromeClient(
			new WebChromeClient() {
				@Override
				public boolean onConsoleMessage(ConsoleMessage message) {
					String message_as_string = String.format(
						"%s:%d [%s] %s",
						message.sourceId(),
						message.lineNumber(),
						message.messageLevel().toString(),
						message.message()
					);

					self.log(message_as_string);

					return super.onConsoleMessage(message);
				}
			}
		);

		web_view.addJavascriptInterface(this, "activity");
		SpendingManager spending_manager = new SpendingManager(this);
		web_view.addJavascriptInterface(spending_manager, "spending_manager");
		BuyManager buy_manager = new BuyManager(this);
		web_view.addJavascriptInterface(buy_manager, "buy_manager");
		BackupManager backup_manager = new BackupManager(this);
		web_view.addJavascriptInterface(backup_manager, "backup_manager");
		CurrencyManager currency_manager = new CurrencyManager(this);
		web_view.addJavascriptInterface(currency_manager, "currency_manager");
		LimitManager limit_manager = new LimitManager(this);
		web_view.addJavascriptInterface(limit_manager, "limit_manager");

		web_view.loadUrl("file:///android_asset/web/index.html");
	}

	@Override
	protected void onResume() {
		super.onResume();

		callGuiFunction("refresh");

		if (dropbox != null) {
			dropbox.finishAuthentication();
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

	private DropboxClient dropbox;

	private void callGuiFunction(String name, String[] arguments) {
		String arguments_string = "";
		for (int i = 0; i < arguments.length; i++) {
			if (i > 0) {
				arguments_string += ",";
			}

			arguments_string += arguments[i];
		}

		final WebView web_view = (WebView)findViewById(R.id.web_view);
		final String name_copy = name;
		final String arguments_string_copy = arguments_string;
		web_view.post(new Runnable() {
			@Override
			public void run() {
				web_view.loadUrl(
					"javascript:GUI."
					+ name_copy
					+ "("
					+ arguments_string_copy
					+ ")"
				);
			}
		});
	}

	private void callGuiFunction(String name) {
		callGuiFunction(name, new String[]{});
	}

	@SuppressWarnings("unused")
	private void guiDebug(String message) {
		callGuiFunction("debug", new String[]{JSONObject.quote(message)});
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
}
