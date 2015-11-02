package ru.thewizardplusplus.wizardbudget;

import android.content.*;
import android.widget.*;

public class BuyResetReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		final Context context_copy = context;
		new Thread(
			new Runnable() {
				@Override
				public void run() {
					BuyManager buy_manager = new BuyManager(context_copy);
					buy_manager.resetMonthlyBuy();

					Utils.updateBuyWidget(context_copy);

					if (
						Settings
						.getCurrent(context_copy)
						.isMonthlyResetNotification()
					) {
						Utils.showNotification(
							context_copy,
							context_copy.getString(R.string.app_name),
							"Monthly purchases have been reset.",
							null
						);
					}
				}
			}
		).start();
	}
}
