package ru.thewizardplusplus.wizardbudget;

import android.preference.*;
import android.os.*;
import android.app.*;

import java.util.regex.*;
import android.view.View.*;

public class SettingsActivity extends PreferenceActivity {
	@Override
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle saved_instance_state) {
		super.onCreate(saved_instance_state);
		addPreferencesFromResource(R.xml.preferences);

		addRegexpValidator(
			"preference_sms_number_pattern",
			"Invalid syntax of SMS number pattern."
		);
		addRegexpValidator(
			"preference_sms_spending_pattern",
			"Invalid syntax of SMS spending pattern."
		);
		addRegexpValidator(
			"preference_sms_income_pattern",
			"Invalid syntax of SMS income pattern."
		);
	}

	@SuppressWarnings("deprecation")
	private void addRegexpValidator(String preference_name, String error_message) {
		final String error_message_copy = error_message;
		addValidator(
			preference_name,
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
							error_message_copy + " " + exception.getMessage()
						);
					}

					return result;
				}
			}
		);
	}

	@SuppressWarnings("deprecation")
	private void addValidator(String preference_name, Preference.OnPreferenceChangeListener validator) {
		EditTextPreference preference =
			(EditTextPreference)getPreferenceScreen()
			.findPreference(preference_name);
		preference.setOnPreferenceChangeListener(validator);
	}

	@SuppressWarnings("deprecation")
	private void addChangeWarning(String preference_name, String warning_message) {
		final String warning_message_copy = warning_message;
		addClickHandler(
			preference_name,
			new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					showAlert("Warning!", warning_message_copy);
					return true;
				}
			}
		);
	}

	@SuppressWarnings("deprecation")
	private void addClickHandler(String preference_name, Preference.OnPreferenceClickListener handler) {
		EditTextPreference preference =
			(EditTextPreference)getPreferenceScreen()
			.findPreference(preference_name);
		preference.setOnPreferenceClickListener(handler);
	}

	private void showAlert(String title, String message) {
		new
			AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(android.R.string.ok, null)
			.show();
	}
}
