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

	private Settings(Context context) {
		this.context = context;
	}
}
