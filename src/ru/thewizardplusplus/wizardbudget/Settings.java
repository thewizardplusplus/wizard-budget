package ru.thewizardplusplus.wizardbudget;

import java.util.*;

import android.content.*;
import android.preference.*;

import org.bostonandroid.datepreference.*;

public class Settings {
	public static Settings getCurrent(Context context) {
		Settings settings = new Settings();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		settings.use_custom_date = preferences.getBoolean("preference_use_custom_date", false);
		settings.custom_date_base_day = DatePreference.getDateFor(preferences, "preference_custom_date_base_day");

		return settings;
	}

	public boolean isUseCustomDate() {
		return use_custom_date;
	}

	public Calendar getCustomDateBaseDay() {
		return custom_date_base_day;
	}

	private boolean use_custom_date = false;
	private Calendar custom_date_base_day = Calendar.getInstance();
}
