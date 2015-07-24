package ru.thewizardplusplus.wizardbudget;

import java.util.regex.*;

import android.preference.*;
import android.os.*;
import android.app.*;
import android.content.*;

public class SettingsActivity extends PreferenceActivity {
	@Override
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle saved_instance_state) {
		super.onCreate(saved_instance_state);
		addPreferencesFromResource(R.xml.preferences);

		PreferenceScreen preference_screen = getPreferenceScreen();
		addRegexpValidator(
			preference_screen,
			Settings.SETTING_NAME_SMS_NUMBER_PATTERN,
			"Invalid syntax of SMS number pattern."
		);
		addRegexpValidator(
			preference_screen,
			Settings.SETTING_NAME_SMS_SPENDING_PATTERN,
			"Invalid syntax of SMS spending pattern."
		);
		addRegexpValidator(
			preference_screen,
			Settings.SETTING_NAME_SMS_INCOME_PATTERN,
			"Invalid syntax of SMS income pattern."
		);

		Pattern working_off_pattern = null;
		try {
			working_off_pattern = Pattern.compile("^\\d+$");
		} catch (PatternSyntaxException exception) {}
		final Pattern working_off_pattern_copy = working_off_pattern;

		EditTextPreference preference_working_off_limit =
			(EditTextPreference)preference_screen
			.findPreference(Settings.SETTING_NAME_WORKING_OFF_LIMIT);
		preference_working_off_limit.setOnPreferenceChangeListener(
			new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(
					Preference preference,
					Object new_value
				) {
					boolean found = working_off_pattern_copy.matcher((String)new_value).find();
					if (!found) {
						showAlert(
							"Error!",
							"Invalid working off value.",
							null,
							false
						);
					}

					return found;
				}
			}
		);
	}

	private void addRegexpValidator(
		PreferenceScreen preference_screen,
		String preference_name,
		String error_message
	) {
		final String error_message_copy = error_message;
		EditTextPreference preference =
			(EditTextPreference)preference_screen
			.findPreference(preference_name);
		preference.setOnPreferenceChangeListener(
			new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(
					Preference preference,
					Object new_value
				) {
					Boolean result = false;
					try {
						Pattern.compile(
							(String)new_value,
							Pattern.CASE_INSENSITIVE
						);

						result = true;
					} catch (PatternSyntaxException exception) {
						showAlert(
							"Error!",
							error_message_copy + " " + exception.getMessage(),
							null,
							false
						);
					}

					return result;
				}
			}
		);
	}

	private void showAlert(
		String title,
		String message,
		DialogInterface.OnClickListener handler,
		boolean show_cancel
	) {
		AlertDialog.Builder builder =
			new AlertDialog
			.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(android.R.string.ok, handler);
		if (show_cancel) {
			builder.setNegativeButton(android.R.string.cancel, null);
		}
		builder.show();
	}
}
