package ru.thewizardplusplus.wizardbudget;

import android.content.*;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			final Context context_copy = context;
			new Thread(
				new Runnable() {
					@Override
					public void run() {
						Utils.setAlarms(context_copy);
					}
				}
			).start();
		}
	}
}
