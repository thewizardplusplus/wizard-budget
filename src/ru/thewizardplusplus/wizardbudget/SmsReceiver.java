package ru.thewizardplusplus.wizardbudget;

import android.content.*;
import android.telephony.*;
import java.util.regex.*;
import android.util.*;

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

						Matcher matcher = Settings.getCurrent(context_copy).getSmsNumberPattern().matcher(number);
						if (!matcher.find()) {
							return;
						}

						String spending_string = "";
						matcher = Settings.getCurrent(context_copy).getSmsSpendingPattern().matcher(text);
						if (matcher.find()) {
							spending_string = matcher.group(1);
						} else {
							matcher = Settings.getCurrent(context_copy).getSmsIncomePattern().matcher(text);
							if (matcher.find()) {
								spending_string = "-" + matcher.group(1);
							} else {
								return;
							}
						}

						double spending = 0.0;
						try {
							spending = Double.valueOf(spending_string);
						} catch(NumberFormatException exception) {
							return;
						}

						String comment = spending >= 0.0 ? Settings.getCurrent(context_copy).getSmsSpendingComment() : Settings.getCurrent(context_copy).getSmsIncomeComment();

						SpendingManager spending_manager = new SpendingManager(context_copy);
						spending_manager.createSpending(spending, comment);

						if (Settings.getCurrent(context_copy).isSmsParsingNotification()) {
							Utils.showNotification(
								context_copy,
								context_copy.getString(R.string.app_name),
								"Imported SMS with " + (spending >= 0.0 ? "spending" : "income") + " " +  String.valueOf(Math.abs(spending)) + " RUB",
								null
							);
						}
					}
				}
			).start();
		}
	}
}
