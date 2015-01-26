package ru.thewizardplusplus.wizardbudget;

import android.preference.*;
import android.os.*;
import android.app.*;
import java.util.regex.*;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle saved_instance_state) {
		super.onCreate(saved_instance_state);
		addPreferencesFromResource(R.xml.preferences);

		addValidator("preference_sms_number_pattern", "Invalid syntax of SMS number pattern.");
		addValidator("preference_sms_spending_pattern", "Invalid syntax of SMS spending pattern.");
		addValidator("preference_sms_income_pattern", "Invalid syntax of SMS income pattern.");
		addValidator("preference_sms_spending_comment_pattern", "Invalid syntax of SMS spending comment pattern.");
		addValidator("preference_sms_income_comment_pattern", "Invalid syntax of SMS income comment pattern.");
	}

	private void addValidator(String preference_name, String error_message) {
		final String error_message_copy = error_message;
		EditTextPreference sms_number_pattern = (EditTextPreference)getPreferenceScreen().findPreference(preference_name);
		sms_number_pattern.setOnPreferenceChangeListener(
			new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object new_value) {
					Boolean result = false;
					try {
						Pattern.compile((String)new_value, Pattern.CASE_INSENSITIVE);
						result = true;
					} catch (PatternSyntaxException exception) {
						showAlert("Error!", error_message_copy + " " + exception.getMessage());
					}

					return result;
				}
			}
		);
	}

	private void showAlert(String title, String message) {
		new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(android.R.string.ok, null)
			.show();
	}
}
