package ru.thewizardplusplus.wizardbudget;

import java.io.*;
import java.util.regex.*;
import java.util.*;

import android.content.*;
import android.net.*;
import android.app.*;
import android.widget.*;
import android.appwidget.*;
import android.database.sqlite.*;

public class Utils {
	public static void showNotification(
		Context context,
		String title,
		String message,
		File file
	) {
		Intent main_intent = new Intent(context, MainActivity.class);
		PendingIntent main_pending_intent = PendingIntent.getActivity(
			context,
			0,
			main_intent,
			0
		);

		@SuppressWarnings("deprecation")
		Notification.Builder notification_builder =
			new Notification
			.Builder(context)
			.setTicker(title)
			.setSmallIcon(R.drawable.app_icon)
			.setContentTitle(title)
			.setContentText(message)
			.setContentIntent(main_pending_intent);

		if (file != null) {
			Uri fileUri = DefaultFileProvider.getUriForFile(
				context,
				context.getPackageName() + ".DefaultFileProvider",
				file
			);

			Intent view_intent = new Intent(Intent.ACTION_VIEW);
			view_intent.setDataAndType(fileUri, "text/xml");
			view_intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

			PendingIntent view_pending_intent = PendingIntent.getActivity(
				context,
				notification_id,
				view_intent,
				PendingIntent.FLAG_UPDATE_CURRENT
			);
			notification_builder.addAction(
				android.R.drawable.ic_menu_view,
				context.getString(R.string.backup_notification_action_view),
				view_pending_intent
			);

			Intent share_intent = new Intent(Intent.ACTION_SEND);
			share_intent.putExtra(Intent.EXTRA_STREAM, fileUri);
			share_intent.setType("text/xml");
			share_intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

			Intent chooser_intent = Intent.createChooser(share_intent, null);

			PendingIntent share_pending_intent = PendingIntent.getActivity(
				context,
				notification_id,
				chooser_intent,
				PendingIntent.FLAG_UPDATE_CURRENT
			);
			notification_builder.addAction(
				android.R.drawable.ic_menu_share,
				context.getString(R.string.backup_notification_action_share),
				share_pending_intent
			);
		}

		Notification notification = notification_builder.getNotification();

		NotificationManager notifications = (NotificationManager)context
			.getSystemService(Context.NOTIFICATION_SERVICE);
		notifications.notify(notification_id, notification);

		notification_id++;
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
			residue_string = residue_string.replaceAll("[^\\d-\\.,]", "");
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

	public static void updateBuyWidget(Context context) {
		AppWidgetManager widget_manager = AppWidgetManager.getInstance(context);

		ComponentName widget = new ComponentName(context, BuyWidget.class);
		RemoteViews views = BuyWidget.getUpdatedViews(context);
		widget_manager.updateAppWidget(widget, views);

		int[] widget_ids = widget_manager.getAppWidgetIds(widget);
		widget_manager.notifyAppWidgetViewDataChanged(
			widget_ids,
			R.id.buy_list
		);
	}

	public static void updateCurrencyWidget(Context context) {
		AppWidgetManager widget_manager = AppWidgetManager.getInstance(context);

		ComponentName widget = new ComponentName(context, CurrencyWidget.class);
		RemoteViews views = CurrencyWidget.getUpdatedViews(context);
		widget_manager.updateAppWidget(widget, views);

		int[] widget_ids = widget_manager.getAppWidgetIds(widget);
		widget_manager.notifyAppWidgetViewDataChanged(
			widget_ids,
			R.id.currency_list
		);
	}

	public static void updateLimitWidget(Context context) {
		RemoteViews views = LimitWidget.getUpdatedViews(context);
		ComponentName widget = new ComponentName(context, LimitWidget.class);
		AppWidgetManager.getInstance(context).updateAppWidget(widget, views);
	}

	public static SQLiteDatabase getDatabase(Context context) {
		DatabaseHelper database_helper = new DatabaseHelper(context);
		return database_helper.getWritableDatabase();
	}

	public static void setAlarms(Context context) {
		setMonthlyBuyAlarm(context);
		setBackupAlarm(context);
		setCurrenciesAlarm(context);
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
		PendingIntent pending_intent = PendingIntent.getBroadcast(
			context,
			0,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT
		);

		AlarmManager alarm_manager = (AlarmManager)context.getSystemService(
			Context.ALARM_SERVICE
		);
		alarm_manager.setInexactRepeating(
			AlarmManager.RTC,
			calendar.getTimeInMillis(),
			MONTHLY_BUY_ALARM_PERIOD_IN_MS,
			pending_intent
		);
	}

	public static void setBackupAlarm(Context context) {
		Calendar calendar = Calendar.getInstance();
		// the starting point should be in the future, to avoid immediate call
		calendar.set(
			Calendar.DAY_OF_MONTH,
			calendar.get(Calendar.DAY_OF_MONTH) + 1
		);
		// clear() don't work with HOUR_OF_DAY
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);

		Intent intent = new Intent(context, BackupReceiver.class);
		PendingIntent pending_intent = PendingIntent.getBroadcast(
			context,
			0,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT
		);

		AlarmManager alarm_manager = (AlarmManager)context.getSystemService(
			Context.ALARM_SERVICE
		);
		alarm_manager.setInexactRepeating(
			AlarmManager.RTC,
			calendar.getTimeInMillis(),
			BACKUP_ALARM_PERIOD_IN_MS,
			pending_intent
		);
	}

	public static void setCurrenciesAlarm(Context context) {
		Calendar calendar = Calendar.getInstance();
		// the starting point should be in the future, to avoid immediate call
		calendar.set(
			Calendar.DAY_OF_MONTH,
			calendar.get(Calendar.DAY_OF_MONTH) + 1
		);
		// clear() don't work with HOUR_OF_DAY
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);

		Intent intent = new Intent(context, CurrenciesReceiver.class);
		PendingIntent pending_intent = PendingIntent.getBroadcast(
			context,
			0,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT
		);

		AlarmManager alarm_manager = (AlarmManager)context.getSystemService(
			Context.ALARM_SERVICE
		);
		alarm_manager.setInexactRepeating(
			AlarmManager.RTC,
			calendar.getTimeInMillis(),
			CURRENCIES_ALARM_PERIOD_IN_MS,
			pending_intent
		);
	}

	private static final long MONTHLY_BUY_ALARM_PERIOD_IN_MS =
		30
		* AlarmManager.INTERVAL_DAY;
	private static final long BACKUP_ALARM_PERIOD_IN_MS =
		AlarmManager.INTERVAL_DAY;
	private static final long CURRENCIES_ALARM_PERIOD_IN_MS =
		AlarmManager.INTERVAL_DAY;

	private static int notification_id = 0;
}
