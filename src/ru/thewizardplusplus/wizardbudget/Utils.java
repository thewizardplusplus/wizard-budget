package ru.thewizardplusplus.wizardbudget;

import java.io.*;
import java.util.regex.*;

import android.content.*;
import android.net.*;
import android.app.*;
import android.widget.*;
import android.appwidget.*;
import android.database.sqlite.*;
import java.util.*;

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
		spending_string = spending_string.replaceAll("[^\\d-\\.,]", "");

		double spending = 0.0;
		try {
			spending = Double.valueOf(spending_string);
		} catch(NumberFormatException exception) {
			return null;
		}

		double residue = 0.0;
		matcher =
			Settings
			.getCurrent(context)
			.getSmsResiduePattern()
			.matcher(text);
		if (matcher.find()) {
			String residue_string = matcher.group(1);
			try {
				residue = Double.valueOf(residue_string);
			} catch(NumberFormatException exception) {}
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

		return new SmsData(spending, residue, comment);
	}

	public static void updateWidget(Context context) {
		RemoteViews views = Widget.getUpdatedViews(context);
		ComponentName widget = new ComponentName(context, Widget.class);
		AppWidgetManager.getInstance(context).updateAppWidget(widget, views);
	}

	public static SQLiteDatabase getDatabase(Context context) {
		DatabaseHelper database_helper = new DatabaseHelper(context);
		return database_helper.getWritableDatabase();
	}

	public static void setMonthlyBuyAlarm(Context context) {
		Calendar calendar = Calendar.getInstance();
		// the starting point should be in the future, to avoid immediate call
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		// clear() don't work with HOUR_OF_DAY
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);

		Intent intent = new Intent(context, BuyResetReceiver.class);
		PendingIntent pending_intent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarm_manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarm_manager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), ALARM_PERIOD_IN_MS, pending_intent);
	}

	private static final long ALARM_PERIOD_IN_MS = 30 * AlarmManager.INTERVAL_DAY;

	private static int notification_id = 0;
}
