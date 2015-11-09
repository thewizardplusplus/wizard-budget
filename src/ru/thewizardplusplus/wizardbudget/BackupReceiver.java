package ru.thewizardplusplus.wizardbudget;

import android.content.*;

public class BackupReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		final Context context_copy = context;
		new Thread(
			new Runnable() {
				@Override
				public void run() {
					if (Settings.getCurrent(context_copy).isDailyAutobackup()) {
						BackupManager backup_manager = new BackupManager(context_copy);
						String filename = backup_manager.backup();

						if (Settings.getCurrent(context_copy).isSaveBackupToDropbox()) {
							DropboxClient dropbox = new DropboxClient(context_copy, filename, true);
							dropbox.saveFile();
						}
					}
				}
			}
		).start();
	}
}
