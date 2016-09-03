package ru.thewizardplusplus.wizardbudget;

import java.io.*;
import java.util.*;
import java.text.*;

import android.content.*;

import com.dropbox.client2.*;
import com.dropbox.client2.android.*;
import com.dropbox.client2.session.*;
import com.dropbox.client2.exception.*;

public class DropboxClient {
	public DropboxClient(
		Context context,
		String filename,
		boolean quiet_mode
	) {
		this.context = context;
		this.filename = filename;
		this.quiet_mode = quiet_mode;
	}

	public void saveFile() {
		String dropbox_token = Settings.getCurrent(context).getDropboxToken();
		if (!dropbox_token.isEmpty()) {
			authenticateViaToken(dropbox_token);
			uploadFile();
		} else if (!quiet_mode) {
			startManualAuthentication();
		}
	}

	public void finishAuthentication() {
		if (
			dropbox_api != null
			&& dropbox_api.getSession().authenticationSuccessful()
		) {
			try {
				dropbox_api.getSession().finishAuthentication();

				String token = dropbox_api.getSession().getOAuth2AccessToken();
				Settings settings = Settings.getCurrent(context);
				settings.setDropboxToken(token);
				settings.save();

				uploadFile();
			} catch (IllegalStateException exception) {}
		}
	}

	private Context context;
	private String filename;
	private boolean quiet_mode = false;
	private DropboxAPI<AndroidAuthSession> dropbox_api;

	private void uploadFile() {
		final Context context_copy = context;
		new Thread(
			new Runnable() {
				@Override
				public void run() {
					try {
						File file = new File(filename);
						FileInputStream in = new FileInputStream(file);
						dropbox_api.putFile(
							"/" + file.getName(),
							in,
							file.length(),
							null,
							null
						);

						if (
							Settings
							.getCurrent(context_copy)
							.isDropboxNotification()
						) {
							Date current_date = new Date();
							DateFormat notification_timestamp_format =
								DateFormat
								.getDateTimeInstance(
									DateFormat.DEFAULT,
									DateFormat.DEFAULT,
									Locale.US
								);
							String notification_timestamp =
								notification_timestamp_format
								.format(current_date);
							Utils.showNotification(
								context_copy,
								context_copy.getString(R.string.app_name),
								"Backup saved to Dropbox at "
									+ notification_timestamp + ".",
								null
							);
						}
					} catch (FileNotFoundException exception) {
					} catch (DropboxException exception) {}
				}
			}
		).start();
	}

	private void authenticateViaToken(String dropbox_token) {
		AppKeyPair app_keys = getAppKeys();
		AndroidAuthSession session = new AndroidAuthSession(
			app_keys,
			dropbox_token
		);
		dropbox_api = new DropboxAPI<AndroidAuthSession>(session);
	}

	private void startManualAuthentication() {
		AppKeyPair app_keys = getAppKeys();
		AndroidAuthSession session = new AndroidAuthSession(app_keys);
		dropbox_api = new DropboxAPI<AndroidAuthSession>(session);
		dropbox_api.getSession().startOAuth2Authentication(context);
	}

	private AppKeyPair getAppKeys() {
		DropboxAccess dropbox_access = new DefaultDropboxAccess();
		String app_secret = dropbox_access.getAppSecret();
		return new AppKeyPair(DropboxAccess.APP_KEY, app_secret);
	}
}
