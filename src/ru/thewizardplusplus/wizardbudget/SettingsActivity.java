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
			"preference_sms_number_pattern",
			"Invalid syntax of SMS number pattern."
		);
		addRegexpValidator(
			preference_screen,
			"preference_sms_spending_pattern",
			"Invalid syntax of SMS spending pattern."
		);
		addRegexpValidator(
			preference_screen,
			"preference_sms_income_pattern",
			"Invalid syntax of SMS income pattern."
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
