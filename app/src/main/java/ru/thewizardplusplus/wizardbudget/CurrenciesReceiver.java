package ru.thewizardplusplus.wizardbudget;

import android.content.*;

public class CurrenciesReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		final Context context_copy = context;
		new Thread(
			new Runnable() {
				@Override
				public void run() {
					if (Settings.getCurrent(context_copy).isDailyCurrencyAutoupdate()) {
						CurrencyManager currency_manager = new CurrencyManager(context_copy);
						currency_manager.updateCurrencies();
					}
				}
			}
		).start();
	}
}
