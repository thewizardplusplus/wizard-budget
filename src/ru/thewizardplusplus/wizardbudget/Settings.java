package ru.thewizardplusplus.wizardbudget;

import java.util.*;

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

		settings.use_custom_date = preferences.getBoolean(
			"preference_use_custom_date",
			false
		);
		settings.custom_date_base_day = DatePreference.getDateFor(
			preferences,
			"preference_custom_date_base_day"
		);

		settings.parse_sms = preferences.getBoolean(
			"preference_parse_sms",
			false
		);
		settings.sms_number_pattern = preferences.getString(
			"preference_sms_number_pattern",
			context.getString(R.string.preference_sms_number_pattern_default)
		);
		settings.sms_spending_pattern = preferences.getString(
			"preference_sms_spending_pattern",
			context.getString(R.string.preference_sms_spending_pattern_default)
		);
		settings.sms_income_pattern = preferences.getString(
			"preference_sms_income_pattern",
			context.getString(R.string.preference_sms_income_pattern_default)
		);
		settings.sms_spending_comment_pattern = preferences.getString(
			"preference_sms_spending_comment_pattern",
			context.getString(R.string.preference_sms_spending_comment_pattern_default)
		);
		settings.sms_income_comment_pattern = preferences.getString(
			"preference_sms_income_comment_pattern",
			context.getString(R.string.preference_sms_income_comment_pattern_default)
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

	public boolean isUseCustomDate() {
		return use_custom_date;
	}

	public Calendar getCustomDateBaseDay() {
		return custom_date_base_day;
	}

	public boolean isParseSms() {
		return parse_sms;
	}

	public String getSmsNumberPattern() {
		return sms_number_pattern;
	}

	public String getSmsSpendingPattern() {
		return sms_spending_pattern;
	}

	public String getSmsIncomePattern() {
		return sms_income_pattern;
	}

	public String getSmsSpendingCommentPattern() {
		return sms_spending_comment_pattern;
	}

	public String getSmsIncomeCommentPattern() {
		return sms_income_comment_pattern;
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

	private Context context;
	private String current_page = DEFAULT_PAGE;
	private String active_spending = DEFAULT_SPENDING;
	private boolean use_custom_date = false;
	private Calendar custom_date_base_day = Calendar.getInstance();
	private boolean parse_sms = false;
	private String sms_number_pattern = "";
	private String sms_spending_pattern = "";
	private String sms_income_pattern = "";
	private String sms_spending_comment_pattern = "";
	private String sms_income_comment_pattern = "";

	private Settings(Context context) {
		this.context = context;
	}
}
