package ru.thewizardplusplus.wizardbudget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
	public static RemoteViews getUpdatedViews(Context context) {
		RemoteViews views = new RemoteViews(
			context.getPackageName(),
			R.layout.widget
		);

		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pending_intent = PendingIntent.getActivity(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.widget_container, pending_intent);

		SpendingManager spending_manager = new SpendingManager(context);
		String spendings_sum = spending_manager.getSpendingsSum();
		views.setTextViewText(
			R.id.widget_spendings_sum,
			spendings_sum
		);
		if (Double.valueOf(spendings_sum) <= 0.0) {
			views.setTextColor(
				R.id.widget_spendings_sum,
				Color.rgb(0x99, 0xcc, 0)
			);
		} else {
			views.setTextColor(
				R.id.widget_spendings_sum,
				Color.rgb(0xff, 0x44, 0x44)
			);
		}

		return views;
	}

	@Override
	public void onUpdate(
		Context context,
		AppWidgetManager widget_manager,
		int[] widget_ids
	) {
		RemoteViews views = getUpdatedViews(context);
		widget_manager.updateAppWidget(widget_ids, views);
	}
}
