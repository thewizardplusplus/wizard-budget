package ru.thewizardplusplus.wizardbudget;

import android.content.*;
import android.telephony.*;

public class SmsReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (
			intent != null
			&& intent.getAction() != null
			&& intent.getAction().equalsIgnoreCase(
				"android.provider.Telephony.SMS_RECEIVED"
			)
		) {
			Object[] pdus = (Object[])intent.getExtras().get("pdus");
			SmsMessage[] messages = new SmsMessage[pdus.length];
			for (int i = 0; i < pdus.length; i++) {
				messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
			}

			final String number = messages[0].getDisplayOriginatingAddress();

			StringBuilder body = new StringBuilder();
			for (int i = 0; i < messages.length; i++) {
				body.append(messages[i].getMessageBody());
			}
			final String text = body.toString();

			final Context context_copy = context;
			new Thread(
				new Runnable() {
					@Override
					public void run() {
						if (!Settings.getCurrent(context_copy).isParseSms()) {
							return;
						}

						SmsData sms_data = Utils.getSpendingFromSms(
							context_copy,
							number,
							text
						);
						if (sms_data == null) {
							return;
						}

						SpendingManager spending_manager = new SpendingManager(
							context_copy
						);
						spending_manager.createSpending(
							sms_data.getSpending(),
							sms_data.getComment()
						);

						double residue = sms_data.getResidue();
						spending_manager.createCorrection(residue);

						Utils.updateWidget(context_copy);
						if (
							Settings
							.getCurrent(context_copy)
							.isSmsParsingNotification()
						) {
							Utils.showNotification(
								context_copy,
								context_copy.getString(R.string.app_name),
								"Imported SMS with "
									+ (sms_data.getSpending() >= 0.0
										? "spending"
										: "income") + " "
									+ String.valueOf(
										Math.abs(sms_data.getSpending())
									) + " RUB.",
								null
							);
						}
					}
				}
			).start();
		}
	}
}
