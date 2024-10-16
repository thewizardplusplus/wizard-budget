package ru.thewizardplusplus.wizardbudget;

import java.util.*;
import java.util.regex.*;
import java.text.*;

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
	public static final String SETTING_NAME_HOURS_START_DATE =
		"hours_start_date";
	public static final String SETTING_NAME_HOURS_END_DATE = "hours_end_date";
	public static final String SETTING_NAME_WORKED_HOURS = "worked_hours";
	public static final String SETTING_NAME_WORK_CALENDAR = "work_calendar";
	public static final String SETTING_NAME_HOURS_DATA = "hours_data";
	public static final String SETTING_NAME_NEED_UPDATE_HOURS =
		"need_update_hours";
	public static final String SETTING_NAME_CREDIT_CARD_TAG =
		"preference_credit_card_tag";
	public static final String SETTING_NAME_COLLECT_STATS =
		"preference_collect_stats";
	public static final String SETTING_NAME_ONLY_MONTHLY =
		"preference_only_monthly";
	public static final String SETTING_NAME_CURRENCY_LIST_MODE =
		"preference_currency_list_mode";
	public static final String SETTING_NAME_EXCHANGE_RATE_API_KEY =
		"preference_exchange_rate_api_key";
	public static final String SETTING_NAME_DAILY_CURRENCY_AUTOUPDATE =
		"preference_daily_currency_autoupdate";
	public static final String SETTING_NAME_DAILY_AUTOBACKUP =
		"preference_daily_autobackup";
	public static final String SETTING_NAME_PARSE_SMS = "preference_parse_sms";
	public static final String SETTING_NAME_SMS_NUMBER_PATTERN =
		"preference_sms_number_pattern";
	public static final String SETTING_NAME_SMS_SPENDING_PATTERN =
		"preference_sms_spending_pattern";
	public static final String SETTING_NAME_SMS_SPENDING_COMMENT =
		"preference_sms_spending_comment";
	public static final String SETTING_NAME_SMS_INCOME_PATTERN =
		"preference_sms_income_pattern";
	public static final String SETTING_NAME_SMS_INCOME_COMMENT =
		"preference_sms_income_comment";
	public static final String SETTING_NAME_SMS_RESIDUE_PATTERN =
		"preference_sms_residue_pattern";
	public static final String SETTING_NAME_SMS_NEGATIVE_CORRECTION_COMMENT =
		"preference_sms_negative_correction_comment";
	public static final String SETTING_NAME_SMS_POSITIVE_CORRECTION_COMMENT =
		"preference_sms_positive_correction_comment";
	public static final String SETTING_NAME_SAVE_BACKUP_TO_DROPBOX =
		"preference_save_backup_to_dropbox";
	public static final String SETTING_NAME_ANALYSIS_HARVEST =
		"preference_analysis_harvest";
	public static final String SETTING_NAME_HARVEST_USERNAME =
		"preference_harvest_username";
	public static final String SETTING_NAME_HARVEST_PASSWORD =
		"preference_harvest_password";
	public static final String SETTING_NAME_HARVEST_SUBDOMAIN =
		"preference_harvest_subdomain";
	public static final String SETTING_NAME_WORKING_OFF_LIMIT =
		"preference_working_off_limit";
	public static final String SETTING_NAME_BACKUP_NOTIFICATION =
		"preference_backup_notification";
	public static final String SETTING_NAME_RESTORE_NOTIFICATION =
		"preference_restore_notification";
	public static final String SETTING_NAME_SMS_PARSING_NOTIFICATION =
		"preference_sms_parsing_notification";
	public static final String SETTING_NAME_SMS_IMPORT_NOTIFICATION =
		"preference_sms_import_notification";
	public static final String SETTING_NAME_DROPBOX_NOTIFICATION =
		"preference_dropbox_notification";
	public static final String SETTING_NAME_MONTHLY_RESET_NOTIFICATION =
		"preference_monthly_reset_notification";
	public static final String SETTING_NAME_CURRENCY_UPDATE_NOTIFICATION =
		"preference_currency_update_notification";
	public static final String SETTING_NAME_USED_CURRENCIES =
		"preference_used_currencies";

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

		Date current_timestamp = new Date();
		DateFormat date_format = new SimpleDateFormat(HOURS_DATE_FORMAT);
		String formatted_current_timestamp = date_format.format(
			current_timestamp
		);

		settings.hours_start_date = preferences.getString(
			SETTING_NAME_HOURS_START_DATE,
			formatted_current_timestamp
		);
		settings.hours_end_date = preferences.getString(
			SETTING_NAME_HOURS_END_DATE,
			formatted_current_timestamp
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
		settings.need_update_hours = preferences.getBoolean(
			SETTING_NAME_NEED_UPDATE_HOURS,
			false
		);

		settings.credit_card_tag =
			preferences.getString(
				SETTING_NAME_CREDIT_CARD_TAG,
				DEFAULT_CREDIT_CARD_TAG
			)
			.trim();
		settings.collect_stats = preferences.getBoolean(
			SETTING_NAME_COLLECT_STATS,
			true
		);
		settings.only_monthly = preferences.getBoolean(
			SETTING_NAME_ONLY_MONTHLY,
			false
		);
		settings.currency_list_mode = preferences.getString(
			SETTING_NAME_CURRENCY_LIST_MODE,
			DEFAULT_CURRENCY_LIST_MODE
		);
		settings.exchange_rate_api_key = preferences.getString(
			SETTING_NAME_EXCHANGE_RATE_API_KEY,
			""
		);
		settings.daily_currency_autoupdate = preferences.getBoolean(
			SETTING_NAME_DAILY_CURRENCY_AUTOUPDATE,
			false
		);
		settings.daily_autobackup = preferences.getBoolean(
			SETTING_NAME_DAILY_AUTOBACKUP,
			false
		);

		settings.parse_sms = preferences.getBoolean(
			SETTING_NAME_PARSE_SMS,
			false
		);
		settings.sms_number_pattern_string = preferences.getString(
			SETTING_NAME_SMS_NUMBER_PATTERN,
			context.getString(
				R.string.preference_sms_number_pattern_default
			)
		);
		try {
			settings.sms_number_pattern = Pattern.compile(
				settings.sms_number_pattern_string,
				Pattern.CASE_INSENSITIVE
			);
		} catch (PatternSyntaxException exception) {
			settings.parse_sms = false;
		}
		settings.sms_spending_pattern_string = preferences.getString(
			SETTING_NAME_SMS_SPENDING_PATTERN,
			context.getString(
				R.string.preference_sms_spending_pattern_default
			)
		);
		try {
			settings.sms_spending_pattern = Pattern.compile(
				settings.sms_spending_pattern_string,
				Pattern.CASE_INSENSITIVE
			);
		} catch (PatternSyntaxException exception) {
			settings.parse_sms = false;
		}
		settings.sms_spending_comment = preferences.getString(
			SETTING_NAME_SMS_SPENDING_COMMENT,
			context.getString(R.string.preference_sms_spending_comment_default)
		);
		settings.sms_income_pattern_string = preferences.getString(
			SETTING_NAME_SMS_INCOME_PATTERN,
			context.getString(
				R.string.preference_sms_income_pattern_default
			)
		);
		try {
			settings.sms_income_pattern = Pattern.compile(
				settings.sms_income_pattern_string,
				Pattern.CASE_INSENSITIVE
			);
		} catch (PatternSyntaxException exception) {
			settings.parse_sms = false;
		}
		settings.sms_income_comment = preferences.getString(
			SETTING_NAME_SMS_INCOME_COMMENT,
			context.getString(R.string.preference_sms_income_comment_default)
		);
		settings.sms_residue_pattern_string = preferences.getString(
			SETTING_NAME_SMS_RESIDUE_PATTERN,
			context.getString(
				R.string.preference_sms_residue_pattern_default
			)
		);
		try {
			settings.sms_residue_pattern = Pattern.compile(
				settings.sms_residue_pattern_string,
				Pattern.CASE_INSENSITIVE
			);
		} catch (PatternSyntaxException exception) {
			settings.parse_sms = false;
		}
		settings.sms_negative_correction_comment = preferences.getString(
			SETTING_NAME_SMS_NEGATIVE_CORRECTION_COMMENT,
			context.getString(
				R.string.preference_sms_negative_correction_comment_default
			)
		);
		settings.sms_positive_correction_comment = preferences.getString(
			SETTING_NAME_SMS_POSITIVE_CORRECTION_COMMENT,
			context.getString(
				R.string.preference_sms_positive_correction_comment_default
			)
		);

		settings.save_backup_to_dropbox = preferences.getBoolean(
			SETTING_NAME_SAVE_BACKUP_TO_DROPBOX,
			false
		);

		settings.analysis_harvest = preferences.getBoolean(
			SETTING_NAME_ANALYSIS_HARVEST,
			false
		);
		settings.harvest_username = preferences.getString(
			SETTING_NAME_HARVEST_USERNAME,
			""
		);
		settings.harvest_password = preferences.getString(
			SETTING_NAME_HARVEST_PASSWORD,
			""
		);
		settings.harvest_subdomain = preferences.getString(
			SETTING_NAME_HARVEST_SUBDOMAIN,
			""
		);
		settings.working_off_limit = Double.valueOf(
			preferences.getString(
				SETTING_NAME_WORKING_OFF_LIMIT,
				String.valueOf(DEFAULT_WORKING_OFF_LIMIT)
			)
		);

		settings.backup_notification = preferences.getBoolean(
			SETTING_NAME_BACKUP_NOTIFICATION,
			true
		);
		settings.restore_notification = preferences.getBoolean(
			SETTING_NAME_RESTORE_NOTIFICATION,
			true
		);
		settings.sms_parsing_notification = preferences.getBoolean(
			SETTING_NAME_SMS_PARSING_NOTIFICATION,
			true
		);
		settings.sms_import_notification = preferences.getBoolean(
			SETTING_NAME_SMS_IMPORT_NOTIFICATION,
			true
		);
		settings.dropbox_notification = preferences.getBoolean(
			SETTING_NAME_DROPBOX_NOTIFICATION,
			true
		);
		settings.monthly_reset_notification = preferences.getBoolean(
			SETTING_NAME_MONTHLY_RESET_NOTIFICATION,
			true
		);
		settings.currency_update_notification = preferences.getBoolean(
			SETTING_NAME_CURRENCY_UPDATE_NOTIFICATION,
			true
		);
		settings.used_currencies = preferences.getString(
			SETTING_NAME_USED_CURRENCIES,
			DEFAULT_USED_CURRENCIES
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

	public String getHoursStartDate() {
		return hours_start_date;
	}

	public void setHoursStartDate(String hours_start_date) {
		this.hours_start_date = hours_start_date;
	}

	public String getHoursEndDate() {
		return hours_end_date;
	}

	public void setHoursEndDate(String hours_end_date) {
		this.hours_end_date = hours_end_date;
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

	public boolean isNeedUpdateHours() {
		return need_update_hours;
	}

	public void setNeedUpdateHours(boolean need_update_hours) {
		this.need_update_hours = need_update_hours;
	}

	public String getCreditCardTag() {
		return credit_card_tag;
	}

	public void setCreditCardTag(String credit_card_tag) {
		this.credit_card_tag = credit_card_tag;
	}

	public boolean isCollectStats() {
		return collect_stats;
	}

	public void setCollectStats(boolean collect_stats) {
		this.collect_stats = collect_stats;
	}

	public boolean isOnlyMonthly() {
		return only_monthly;
	}

	public void setOnlyMonthly(boolean only_monthly) {
		this.only_monthly = only_monthly;
	}

	public String getCurrencyListMode() {
		return currency_list_mode;
	}

	public void setCurrencyListMode(
		String currency_list_mode
	) {
		this.currency_list_mode = currency_list_mode;
	}

	public String getExchangeRateApiKey() {
		return exchange_rate_api_key;
	}

	public boolean isDailyCurrencyAutoupdate() {
		return daily_currency_autoupdate;
	}

	public void setDailyCurrencyAutoupdate(boolean daily_currency_autoupdate) {
		this.daily_currency_autoupdate = daily_currency_autoupdate;
	}

	public boolean isDailyAutobackup() {
		return daily_autobackup;
	}

	public void setDailyAutobackup(boolean daily_autobackup) {
		this.daily_autobackup = daily_autobackup;
	}

	public boolean isParseSms() {
		return parse_sms;
	}

	public void setParseSms(boolean parse_sms) {
		this.parse_sms = parse_sms;
	}

	public String getSmsNumberPatternString() {
		return sms_number_pattern_string;
	}

	public Pattern getSmsNumberPattern() {
		return sms_number_pattern;
	}

	public void setSmsNumberPattern(String sms_number_pattern) {
		sms_number_pattern_string = sms_number_pattern;
	}

	public String getSmsSpendingPatternString() {
		return sms_spending_pattern_string;
	}

	public Pattern getSmsSpendingPattern() {
		return sms_spending_pattern;
	}

	public void setSmsSpendingPattern(String sms_spending_pattern) {
		sms_spending_pattern_string = sms_spending_pattern;
	}

	public String getSmsSpendingComment() {
		return sms_spending_comment;
	}

	public void setSmsSpendingComment(String sms_spending_comment) {
		this.sms_spending_comment = sms_spending_comment;
	}

	public String getSmsIncomePatternString() {
		return sms_income_pattern_string;
	}

	public Pattern getSmsIncomePattern() {
		return sms_income_pattern;
	}

	public void setSmsIncomePattern(String sms_income_pattern) {
		sms_income_pattern_string = sms_income_pattern;
	}

	public String getSmsIncomeComment() {
		return sms_income_comment;
	}

	public void setSmsIncomeComment(String sms_income_comment) {
		this.sms_income_comment = sms_income_comment;
	}

	public String getSmsResiduePatternString() {
		return sms_residue_pattern_string;
	}

	public Pattern getSmsResiduePattern() {
		return sms_residue_pattern;
	}

	public void setSmsResiduePattern(String sms_residue_pattern) {
		sms_residue_pattern_string = sms_residue_pattern;
	}

	public String getSmsNegativeCorrectionComment() {
		return sms_negative_correction_comment;
	}

	public void setSmsNegativeCorrectionComment(
		String sms_negative_correction_comment
	) {
		this.sms_negative_correction_comment = sms_negative_correction_comment;
	}

	public String getSmsPositiveCorrectionComment() {
		return sms_positive_correction_comment;
	}

	public void setSmsPositiveCorrectionComment(
		String sms_positive_correction_comment
	) {
		this.sms_positive_correction_comment = sms_positive_correction_comment;
	}

	public boolean isSaveBackupToDropbox() {
		return save_backup_to_dropbox;
	}

	public void setSaveBackupToDropbox(boolean save_backup_to_dropbox) {
		this.save_backup_to_dropbox = save_backup_to_dropbox;
	}

	public boolean isAnalysisHarvest() {
		return analysis_harvest;
	}

	public void setAnalysisHarvest(boolean analysis_harvest) {
		this.analysis_harvest = analysis_harvest;
	}

	public String getHarvestUsername() {
		return harvest_username;
	}

	public void setHarvestUsername(String harvest_username) {
		this.harvest_username = harvest_username;
	}

	public String getHarvestPassword() {
		return harvest_password;
	}

	public String getHarvestSubdomain() {
		return harvest_subdomain;
	}

	public void setHarvestSubdomain(String harvest_subdomain) {
		this.harvest_subdomain = harvest_subdomain;
	}

	public double getWorkingOffLimit() {
		return working_off_limit;
	}

	public void setWorkingOffLimit(double working_off_limit) {
		this.working_off_limit = working_off_limit;
	}

	public boolean isBackupNotification() {
		return backup_notification;
	}

	public void setBackupNotification(boolean backup_notification) {
		this.backup_notification = backup_notification;
	}

	public boolean isRestoreNotification() {
		return restore_notification;
	}

	public void setRestoreNotification(boolean restore_notification) {
		this.restore_notification = restore_notification;
	}

	public boolean isSmsParsingNotification() {
		return sms_parsing_notification;
	}

	public void setSmsParsingNotification(boolean sms_parsing_notification) {
		this.sms_parsing_notification = sms_parsing_notification;
	}

	public boolean isSmsImportNotification() {
		return sms_import_notification;
	}

	public void setSmsImportNotification(boolean sms_import_notification) {
		this.sms_import_notification = sms_import_notification;
	}

	public boolean isDropboxNotification() {
		return dropbox_notification;
	}

	public void setDropboxNotification(boolean dropbox_notification) {
		this.dropbox_notification = dropbox_notification;
	}

	public boolean isMonthlyResetNotification() {
		return monthly_reset_notification;
	}

	public void setMonthlyResetNotification(
		boolean monthly_reset_notification
	) {
		this.monthly_reset_notification = monthly_reset_notification;
	}

	public boolean isCurrencyUpdateNotification() {
		return currency_update_notification;
	}

	public void setCurrencyUpdateNotification(
		boolean currency_update_notification
	) {
		this.currency_update_notification = currency_update_notification;
	}

	public String getUsedCurrencies() {
		String cleaned_used_currencies = used_currencies
			.toUpperCase()
			.replaceAll("[^A-Z,]+", "");
		return !cleaned_used_currencies.isEmpty()
			? cleaned_used_currencies
			: DEFAULT_USED_CURRENCIES;
	}

	public void setUsedCurrencies(String used_currencies) {
		this.used_currencies = used_currencies;
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
		editor.putString(SETTING_NAME_HOURS_START_DATE, hours_start_date);
		editor.putString(SETTING_NAME_HOURS_END_DATE, hours_end_date);
		editor.putString(SETTING_NAME_WORKED_HOURS, worked_hours);
		editor.putString(SETTING_NAME_WORK_CALENDAR, work_calendar);
		editor.putString(SETTING_NAME_HOURS_DATA, hours_data);
		editor.putBoolean(SETTING_NAME_NEED_UPDATE_HOURS, need_update_hours);
		editor.putString(SETTING_NAME_CREDIT_CARD_TAG, credit_card_tag);
		editor.putBoolean(SETTING_NAME_COLLECT_STATS, collect_stats);
		editor.putBoolean(SETTING_NAME_ONLY_MONTHLY, only_monthly);
		editor.putString(SETTING_NAME_CURRENCY_LIST_MODE, currency_list_mode);
		editor.putBoolean(SETTING_NAME_DAILY_CURRENCY_AUTOUPDATE, daily_currency_autoupdate);
		editor.putBoolean(SETTING_NAME_DAILY_AUTOBACKUP, daily_autobackup);
		editor.putBoolean(SETTING_NAME_PARSE_SMS, parse_sms);
		editor.putString(
			SETTING_NAME_SMS_NUMBER_PATTERN,
			sms_number_pattern_string
		);
		editor.putString(
			SETTING_NAME_SMS_SPENDING_PATTERN,
			sms_spending_pattern_string
		);
		editor.putString(
			SETTING_NAME_SMS_SPENDING_COMMENT,
			sms_spending_comment
		);
		editor.putString(
			SETTING_NAME_SMS_INCOME_PATTERN,
			sms_income_pattern_string
		);
		editor.putString(SETTING_NAME_SMS_INCOME_COMMENT, sms_income_comment);
		editor.putString(
			SETTING_NAME_SMS_RESIDUE_PATTERN,
			sms_residue_pattern_string
		);
		editor.putString(
			SETTING_NAME_SMS_NEGATIVE_CORRECTION_COMMENT,
			sms_negative_correction_comment
		);
		editor.putString(
			SETTING_NAME_SMS_POSITIVE_CORRECTION_COMMENT,
			sms_positive_correction_comment
		);
		editor.putBoolean(
			SETTING_NAME_SAVE_BACKUP_TO_DROPBOX,
			save_backup_to_dropbox
		);
		editor.putBoolean(SETTING_NAME_ANALYSIS_HARVEST, analysis_harvest);
		editor.putString(SETTING_NAME_HARVEST_USERNAME, harvest_username);
		editor.putString(SETTING_NAME_HARVEST_SUBDOMAIN, harvest_subdomain);
		editor.putString(
			SETTING_NAME_WORKING_OFF_LIMIT,
			String.valueOf(working_off_limit)
		);
		editor.putBoolean(
			SETTING_NAME_BACKUP_NOTIFICATION,
			backup_notification
		);
		editor.putBoolean(
			SETTING_NAME_RESTORE_NOTIFICATION,
			restore_notification
		);
		editor.putBoolean(
			SETTING_NAME_SMS_PARSING_NOTIFICATION,
			sms_parsing_notification
		);
		editor.putBoolean(
			SETTING_NAME_SMS_IMPORT_NOTIFICATION,
			sms_import_notification
		);
		editor.putBoolean(
			SETTING_NAME_DROPBOX_NOTIFICATION,
			dropbox_notification
		);
		editor.putBoolean(
			SETTING_NAME_MONTHLY_RESET_NOTIFICATION,
			monthly_reset_notification
		);
		editor.putBoolean(
			SETTING_NAME_CURRENCY_UPDATE_NOTIFICATION,
			currency_update_notification
		);
		editor.putString(SETTING_NAME_USED_CURRENCIES, used_currencies);
		editor.commit();
	}

	private static final String DEFAULT_PAGE = "history";
	private static final String DEFAULT_SEGMENT = "history";
	private static final String DEFAULT_SPENDING = "null";
	private static final String DEFAULT_BUY = "null";
	private static final String DEFAULT_WORKED_HOURS = "null";
	private static final String DEFAULT_WORK_CALENDAR = "null";
	private static final String DEFAULT_HOURS_DATA = "null";
	private static final long DEFAULT_STATS_RANGE = 0;
	private static final String DEFAULT_CREDIT_CARD_TAG = "credit card";
	private static final String DEFAULT_CURRENCY_LIST_MODE = "all";
	private static final double DEFAULT_WORKING_OFF_LIMIT = 4.0;
	private static final String DEFAULT_USED_CURRENCIES = "USD,EUR";
	private static final String HOURS_DATE_FORMAT = "yyyy-MM-dd";

	private Context context;
	private String current_page = DEFAULT_PAGE;
	private String current_segment = DEFAULT_SEGMENT;
	private String active_spending = DEFAULT_SPENDING;
	private String active_buy = DEFAULT_BUY;
	private long stats_range = DEFAULT_STATS_RANGE;
	private String stats_tags = "";
	private String dropbox_token = "";
	private String hours_start_date = "";
	private String hours_end_date = "";
	private String worked_hours = DEFAULT_WORKED_HOURS;
	private String work_calendar = DEFAULT_WORK_CALENDAR;
	private String hours_data = DEFAULT_HOURS_DATA;
	private boolean need_update_hours = false;
	private String credit_card_tag = DEFAULT_CREDIT_CARD_TAG;
	private boolean collect_stats = true;
	private boolean only_monthly = false;
	private String currency_list_mode = DEFAULT_CURRENCY_LIST_MODE;
	private String exchange_rate_api_key = "";
	private boolean daily_currency_autoupdate = false;
	private boolean daily_autobackup = false;
	private boolean parse_sms = false;
	private String sms_number_pattern_string = "";
	private Pattern sms_number_pattern;
	private String sms_spending_pattern_string = "";
	private Pattern sms_spending_pattern;
	private String sms_income_pattern_string = "";
	private Pattern sms_income_pattern;
	private String sms_residue_pattern_string = "";
	private Pattern sms_residue_pattern;
	private String sms_spending_comment = "";
	private String sms_income_comment = "";
	private String sms_negative_correction_comment = "";
	private String sms_positive_correction_comment = "";
	private boolean save_backup_to_dropbox = false;
	private boolean analysis_harvest = false;
	private String harvest_username = "";
	private String harvest_password = "";
	private String harvest_subdomain = "";
	private double working_off_limit = DEFAULT_WORKING_OFF_LIMIT;
	private boolean backup_notification = true;
	private boolean restore_notification = true;
	private boolean sms_parsing_notification = true;
	private boolean sms_import_notification = true;
	private boolean dropbox_notification = true;
	private boolean monthly_reset_notification = true;
	private boolean currency_update_notification = true;
	private String used_currencies = DEFAULT_USED_CURRENCIES;

	private Settings(Context context) {
		this.context = context;
	}
}
