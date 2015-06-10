package ru.thewizardplusplus.wizardbudget;

import java.util.regex.*;

import android.content.*;
import android.preference.*;

public class Settings {
	public static final String SETTING_NAME_CURRENT_PAGE = "current_page";
	public static final String SETTING_NAME_CURRENT_SEGMENT = "current_segment";
	public static final String SETTING_NAME_ACTIVE_SPENDING = "active_spending";
	public static final String SETTING_NAME_ACTIVE_BUY = "active_buy";
	public static final String SETTING_NAME_STATS_RANGE = "stats_range";
	public static final String SETTING_NAME_STATS_TAGS = "stats_tags";
	public static final String SETTING_NAME_DROPBOX_TOKEN = "dropbox_token";
	public static final String SETTING_NAME_WORKED_HOURS = "worked_hours";
	public static final String SETTING_NAME_WORK_CALENDAR = "work_calendar";
	public static final String SETTING_NAME_HOURS_DATA = "hours_data";

	public static Settings getCurrent(Context context) {
		Settings settings = new Settings(context);

		SharedPreferences preferences =
			PreferenceManager
			.getDefaultSharedPreferences(context);
		settings.current_page = preferences.getString(
			SETTING_NAME_CURRENT_PAGE,
			DEFAULT_PAGE
		);
		settings.current_segment = preferences.getString(
			SETTING_NAME_CURRENT_SEGMENT,
			DEFAULT_SEGMENT
		);
		settings.active_spending = preferences.getString(
			SETTING_NAME_ACTIVE_SPENDING,
			DEFAULT_SPENDING
		);
		settings.active_buy = preferences.getString(
			SETTING_NAME_ACTIVE_BUY,
			DEFAULT_BUY
		);
		settings.stats_range = preferences.getLong(
			SETTING_NAME_STATS_RANGE,
			DEFAULT_STATS_RANGE
		);
		settings.stats_tags = preferences.getString(
			SETTING_NAME_STATS_TAGS,
			""
		);
		settings.dropbox_token = preferences.getString(
			SETTING_NAME_DROPBOX_TOKEN,
			""
		);
		settings.worked_hours = preferences.getString(
			SETTING_NAME_WORKED_HOURS,
			DEFAULT_WORKED_HOURS
		);
		settings.work_calendar = preferences.getString(
			SETTING_NAME_WORK_CALENDAR,
			DEFAULT_WORK_CALENDAR
		);
		settings.hours_data = preferences.getString(
			SETTING_NAME_HOURS_DATA,
			DEFAULT_HOURS_DATA
		);

		settings.credit_card_tag =
			preferences.getString(
				"preference_credit_card_tag",
				DEFAULT_CREDIT_CARD_TAG
			)
			.trim();

		settings.parse_sms = preferences.getBoolean(
			"preference_parse_sms",
			false
		);
		try {
			settings.sms_number_pattern = Pattern.compile(
				preferences.getString(
					"preference_sms_number_pattern",
					context.getString(
						R.string.preference_sms_number_pattern_default
					)
				),
				Pattern.CASE_INSENSITIVE
			);
		} catch (PatternSyntaxException exception) {
			settings.parse_sms = false;
		}
		try {
			settings.sms_spending_pattern = Pattern.compile(
				preferences.getString(
					"preference_sms_spending_pattern",
					context.getString(
						R.string.preference_sms_spending_pattern_default
					)
				),
				Pattern.CASE_INSENSITIVE
			);
		} catch (PatternSyntaxException exception) {
			settings.parse_sms = false;
		}
		try {
			settings.sms_income_pattern = Pattern.compile(
				preferences.getString(
					"preference_sms_income_pattern",
					context.getString(
						R.string.preference_sms_income_pattern_default
					)
				),
				Pattern.CASE_INSENSITIVE
			);
		} catch (PatternSyntaxException exception) {
			settings.parse_sms = false;
		}
		settings.sms_spending_comment = preferences.getString(
			"preference_sms_spending_comment",
			context.getString(R.string.preference_sms_spending_comment_default)
		);
		settings.sms_income_comment = preferences.getString(
			"preference_sms_income_comment",
			context.getString(R.string.preference_sms_income_comment_default)
		);

		settings.save_backup_to_dropbox = preferences.getBoolean(
			"preference_save_backup_to_dropbox",
			false
		);
		settings.dropbox_app_key = preferences.getString(
			"preference_dropbox_app_key",
			context.getString(R.string.preference_dropbox_app_key_default)
		);
		settings.dropbox_app_secret = preferences.getString(
			"preference_dropbox_app_secret",
			context.getString(R.string.preference_dropbox_app_secret_default)
		);

		settings.analysis_harvest = preferences.getBoolean(
			"preference_analysis_harvest",
			false
		);
		settings.harvest_username = preferences.getString(
			"preference_harvest_username",
			""
		);
		settings.harvest_password = preferences.getString(
			"preference_harvest_password",
			""
		);
		settings.harvest_subdomain = preferences.getString(
			"preference_harvest_subdomain",
			""
		);

		settings.backup_notification = preferences.getBoolean(
			"preference_backup_notification",
			true
		);
		settings.restore_notification = preferences.getBoolean(
			"preference_restore_notification",
			true
		);
		settings.sms_parsing_notification = preferences.getBoolean(
			"preference_sms_parsing_notification",
			true
		);
		settings.sms_import_notification = preferences.getBoolean(
			"preference_sms_import_notification",
			true
		);

		return settings;
	}

	public String getCurrentPage() {
		return current_page;
	}

	public void setCurrentPage(String current_page) {
		this.current_page = current_page;
	}

	public String getCurrentSegment() {
		return current_segment;
	}

	public void setCurrentSegment(String current_segment) {
		this.current_segment = current_segment;
	}

	public String getActiveSpending() {
		return active_spending;
	}

	public void setActiveSpending(String active_spending) {
		this.active_spending = active_spending;
	}

	public String getActiveBuy() {
		return active_buy;
	}

	public void setActiveBuy(String active_buy) {
		this.active_buy = active_buy;
	}

	public long getStatsRange() {
		return stats_range;
	}

	public void setStatsRange(long stats_range) {
		this.stats_range = stats_range;
	}

	public String getStatsTags() {
		return stats_tags;
	}

	public void setStatsTags(String stats_tags) {
		this.stats_tags = stats_tags;
	}

	public String getDropboxToken() {
		return dropbox_token;
	}

	public void setDropboxToken(String dropbox_token) {
		this.dropbox_token = dropbox_token;
	}

	public String getWorkedHours() {
		return worked_hours;
	}

	public void setWorkedHours(String worked_hours) {
		this.worked_hours = worked_hours;
	}

	public String getWorkCalendar() {
		return work_calendar;
	}

	public void setWorkCalendar(String work_calendar) {
		this.work_calendar = work_calendar;
	}

	public String getHoursData() {
		return hours_data;
	}

	public void setHoursData(String hours_data) {
		this.hours_data = hours_data;
	}

	public String getCreditCardTag() {
		return credit_card_tag;
	}

	public boolean isParseSms() {
		return parse_sms;
	}

	public Pattern getSmsNumberPattern() {
		return sms_number_pattern;
	}

	public Pattern getSmsSpendingPattern() {
		return sms_spending_pattern;
	}

	public Pattern getSmsIncomePattern() {
		return sms_income_pattern;
	}

	public String getSmsSpendingComment() {
		return sms_spending_comment;
	}

	public String getSmsIncomeComment() {
		return sms_income_comment;
	}

	public boolean isSaveBackupToDropbox() {
		return save_backup_to_dropbox;
	}

	public String getDropboxAppKey() {
		return dropbox_app_key;
	}

	public String getDropboxAppSecret() {
		return dropbox_app_secret;
	}

	public boolean isAnalysisHarvest() {
		return analysis_harvest;
	}

	public String getHarvestUsername() {
		return harvest_username;
	}

	public String getHarvestPassword() {
		return harvest_password;
	}

	public String getHarvestSubdomain() {
		return harvest_subdomain;
	}

	public boolean isBackupNotification() {
		return backup_notification;
	}

	public boolean isRestoreNotification() {
		return restore_notification;
	}

	public boolean isSmsParsingNotification() {
		return sms_parsing_notification;
	}

	public boolean isSmsImportNotification() {
		return sms_import_notification;
	}

	public boolean isDropboxNotification() {
		return dropbox_notification;
	}

	public void save() {
		SharedPreferences preferences =
			PreferenceManager
			.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(SETTING_NAME_CURRENT_PAGE, current_page);
		editor.putString(SETTING_NAME_CURRENT_SEGMENT, current_segment);
		editor.putString(SETTING_NAME_ACTIVE_SPENDING, active_spending);
		editor.putString(SETTING_NAME_ACTIVE_BUY, active_buy);
		editor.putLong(SETTING_NAME_STATS_RANGE, stats_range);
		editor.putString(SETTING_NAME_STATS_TAGS, stats_tags);
		editor.putString(SETTING_NAME_DROPBOX_TOKEN, dropbox_token);
		editor.putString(SETTING_NAME_WORKED_HOURS, worked_hours);
		editor.putString(SETTING_NAME_WORK_CALENDAR, work_calendar);
		editor.putString(SETTING_NAME_HOURS_DATA, hours_data);
		editor.commit();
	}

	private static final String DEFAULT_PAGE = "history";
	private static final String DEFAULT_SEGMENT = "history";
	private static final String DEFAULT_SPENDING = "null";
	private static final String DEFAULT_BUY = "null";
	private static final String DEFAULT_WORKED_HOURS = "null";
	private static final String DEFAULT_WORK_CALENDAR = "null";
	private static final String DEFAULT_HOURS_DATA = "null";
	private static final long DEFAULT_STATS_RANGE = 30;
	private static final String DEFAULT_CREDIT_CARD_TAG = "credit card";

	private Context context;
	private String current_page = DEFAULT_PAGE;
	private String current_segment = DEFAULT_SEGMENT;
	private String active_spending = DEFAULT_SPENDING;
	private String active_buy = DEFAULT_BUY;
	private long stats_range = DEFAULT_STATS_RANGE;
	private String stats_tags = "";
	private String dropbox_token = "";
	private String worked_hours = DEFAULT_WORKED_HOURS;
	private String work_calendar = DEFAULT_WORK_CALENDAR;
	private String hours_data = DEFAULT_HOURS_DATA;
	private String credit_card_tag = DEFAULT_CREDIT_CARD_TAG;
	private boolean parse_sms = false;
	private Pattern sms_number_pattern;
	private Pattern sms_spending_pattern;
	private Pattern sms_income_pattern;
	private String sms_spending_comment = "";
	private String sms_income_comment = "";
	private boolean save_backup_to_dropbox = false;
	private String dropbox_app_key = "";
	private String dropbox_app_secret = "";
	private boolean analysis_harvest = false;
	private String harvest_username = "";
	private String harvest_password = "";
	private String harvest_subdomain = "";
	private boolean backup_notification = true;
	private boolean restore_notification = true;
	private boolean sms_parsing_notification = true;
	private boolean sms_import_notification = true;
	private boolean dropbox_notification = true;

	private Settings(Context context) {
		this.context = context;
	}
}
