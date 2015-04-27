package ru.thewizardplusplus.wizardbudget;

import android.preference.*;
import android.os.*;
import android.app.*;

import java.util.regex.*;
import android.view.View.*;
import android.content.*;

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

		final Context context = this;
		getPreferenceScreen()
			.findPreference("preference_dropbox_passwords_reset_button")
			.setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						showAlert(
							"Warning!",
							"Are you sure you want to reset passwords of backuping Dropbox app to defaults?",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									EditTextPreference dropbox_app_key =
										(EditTextPreference)getPreferenceScreen()
										.findPreference("preference_dropbox_app_key");
									dropbox_app_key.setText(context.getString(R.string.preference_dropbox_app_key_default));

									EditTextPreference dropbox_app_secret =
										(EditTextPreference)getPreferenceScreen()
										.findPreference("preference_dropbox_app_secret");
									dropbox_app_secret.setText(context.getString(R.string.preference_dropbox_app_secret_default));
								}
							},
							true
						);

						return true;
					}
				}
			);
	}

	@SuppressWarnings("deprecation")
	private void addRegexpValidator(String preference_name, String error_message) {
		final String error_message_copy = error_message;
		EditTextPreference preference =
			(EditTextPreference)getPreferenceScreen()
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
