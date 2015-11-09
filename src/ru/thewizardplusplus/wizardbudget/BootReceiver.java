package ru.thewizardplusplus.wizardbudget;

import android.content.*;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Utils.setAlarms(context);
		}
	}
}
