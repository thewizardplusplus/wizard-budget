package ru.thewizardplusplus.wizardbudget;

import java.io.*;

import android.content.*;
import android.net.*;
import android.app.*;

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

	private static int notification_id = 0;
}
