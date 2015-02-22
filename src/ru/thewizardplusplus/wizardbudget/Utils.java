package ru.thewizardplusplus.wizardbudget;

import java.io.*;

import android.content.*;
import android.net.*;
import android.app.*;
import java.util.regex.*;
import android.widget.*;
import android.appwidget.*;

public class Utils {
	public static void showNotification(
		Context context,
		String title,
		String message,
		File file
	) {
		Intent intent = null;
		if (file != null) {
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), "text/xml");
		} else {
			intent = new Intent(context, MainActivity.class);
		}
		PendingIntent pending_intent = PendingIntent.getActivity(
			context,
			0,
			intent,
			0
		);

		@SuppressWarnings("deprecation")
			Notification notification = new Notification.Builder(context)
			.setTicker(title)
			.setSmallIcon(R.drawable.app_icon)
			.setContentTitle(title)
			.setContentText(message)
			.setContentIntent(pending_intent)
			.getNotification();

		NotificationManager notifications = (NotificationManager)context
			.getSystemService(
			Context.NOTIFICATION_SERVICE
		);
		notifications.notify(notification_id++, notification);
	}

	public static SmsData getSpendingFromSms(
		Context context,
		String number,
		String text
	) {
		Matcher matcher =
			Settings
			.getCurrent(context)
			.getSmsNumberPattern()
			.matcher(number);
		if (!matcher.find()) {
			return null;
		}

		String spending_string = "";
		matcher =
			Settings
			.getCurrent(context)
			.getSmsSpendingPattern()
			.matcher(text);
		if (matcher.find()) {
			spending_string = matcher.group(1);
		} else {
			matcher =
				Settings
				.getCurrent(context)
				.getSmsIncomePattern()
				.matcher(text);
			if (matcher.find()) {
				spending_string = "-" + matcher.group(1);
			} else {
				return null;
			}
		}

		double spending = 0.0;
		try {
			spending = Double.valueOf(spending_string);
		} catch(NumberFormatException exception) {
			return null;
		}

		String comment =
			spending >= 0.0
				? Settings.getCurrent(context).getSmsSpendingComment()
				: Settings.getCurrent(context).getSmsIncomeComment();
		String credit_card_tag =
			Settings
			.getCurrent(context)
			.getCreditCardTag();
		if (!credit_card_tag.isEmpty()) {
			comment += ", " + credit_card_tag;
		}

		return new SmsData(spending, comment);
	}

	public static void updateWidget(Context context) {
		RemoteViews views = Widget.getUpdatedViews(context);
		ComponentName widget = new ComponentName(context, Widget.class);
		AppWidgetManager.getInstance(context).updateAppWidget(widget, views);
	}

	private static int notification_id = 0;
}
