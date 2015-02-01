package ru.thewizardplusplus.wizardbudget;

import java.util.*;
import java.util.regex.*;

import android.content.*;
import android.preference.*;

import org.bostonandroid.datepreference.*;

public class Settings {
	public static final String SETTING_NAME_CURRENT_PAGE = "current_page";
	public static final String SETTING_NAME_ACTIVE_SPENDING = "active_spending";

	public static Settings getCurrent(Context context) {
		Settings settings = new Settings(context);

		SharedPreferences preferences =
			PreferenceManager
			.getDefaultSharedPreferences(context);
		settings.current_page = preferences.getString(
			SETTING_NAME_CURRENT_PAGE,
			DEFAULT_PAGE
		);
		settings.active_spending = preferences.getString(
			SETTING_NAME_ACTIVE_SPENDING,
			DEFAULT_SPENDING
		);

		settings.credit_card_tag = preferences.getString(
			"preference_credit_card_tag",
			DEFAULT_CREDIT_CARD_TAG
		);
		settings.save_backup_to_dropbox = preferences.getBoolean(
			"preference_save_backup_to_dropbox",
			true
		);

		settings.use_custom_date = preferences.getBoolean(
			"preference_use_custom_date",
			false
		);
		Date current_date = new Date();
		settings.custom_date_base_day = DatePreference.getDateFor(
			preferences,
			"preference_custom_date_base_day",
			current_date
		);

		settings.parse_sms = preferences.getBoolean(
			"preference_parse_sms",
			false
		);
		try {
			settings.sms_number_pattern = Pattern.compile(
				preferences.getString(
					"preference_sms_number_pattern",
					context.getString(R.string.preference_sms_number_pattern_default)
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
					context.getString(R.string.preference_sms_spending_pattern_default)
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
					context.getString(R.string.preference_sms_income_pattern_default)
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

	public String getActiveSpending() {
		return active_spending;
	}

	public void setActiveSpending(String active_spending) {
		this.active_spending = active_spending;
	}

	public String getCreditCardTag() {
		return credit_card_tag;
	}

	public boolean isSaveBackupToDropbox() {
		return save_backup_to_dropbox;
	}

	public boolean isUseCustomDate() {
		return use_custom_date;
	}

	public Calendar getCustomDateBaseDay() {
		return custom_date_base_day;
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
		editor.putString(SETTING_NAME_ACTIVE_SPENDING, active_spending);
		editor.commit();
	}

	private static final String DEFAULT_PAGE = "history";
	private static final String DEFAULT_SPENDING = "null";
	private static final String DEFAULT_CREDIT_CARD_TAG = "credit card";

	private Context context;
	private String current_page = DEFAULT_PAGE;
	private String active_spending = DEFAULT_SPENDING;
	private String credit_card_tag = DEFAULT_CREDIT_CARD_TAG;
	private boolean save_backup_to_dropbox = true;
	private boolean use_custom_date = false;
	private Calendar custom_date_base_day = Calendar.getInstance();
	private boolean parse_sms = false;
	private Pattern sms_number_pattern;
	private Pattern sms_spending_pattern;
	private Pattern sms_income_pattern;
	private String sms_spending_comment = "";
	private String sms_income_comment = "";
	private boolean backup_notification = true;
	private boolean restore_notification = true;
	private boolean sms_parsing_notification = true;
	private boolean sms_import_notification = true;
	private boolean dropbox_notification = true;

	private Settings(Context context) {
		this.context = context;
	}
}
