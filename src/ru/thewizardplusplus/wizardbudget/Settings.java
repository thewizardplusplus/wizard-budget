package ru.thewizardplusplus.wizardbudget;
import android.content.*;
import android.preference.*;

public class Settings {
	public static Settings getCurrent(Context context) {
		Settings settings = new Settings();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		settings.use_custom_date = preferences.getBoolean("preference_use_custom_date", false);

		return settings;
	}

	public boolean isUseCustomDate() {
		return use_custom_date;
	}

	private boolean use_custom_date = false;
}
