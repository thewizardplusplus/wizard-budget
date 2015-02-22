package ru.thewizardplusplus.wizardbudget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
	public static RemoteViews getUpdatedViews(Context context) {
		RemoteViews views = new RemoteViews(
			context.getPackageName(),
			R.layout.widget
		);

		Intent widget_intent = new Intent(context, MainActivity.class);
		PendingIntent widget_pending_intent = PendingIntent.getActivity(
			context,
			CURRENT_PAGE_REQUEST_CODE,
			widget_intent,
			PendingIntent.FLAG_UPDATE_CURRENT
		);
		views.setOnClickPendingIntent(
			R.id.widget_container,
			widget_pending_intent
		);

		Intent widget_add_intent = new Intent(context, MainActivity.class);
		widget_add_intent.putExtra(
			Settings.SETTING_NAME_CURRENT_PAGE,
			"editor"
		);
		PendingIntent widget_add_pending_intent = PendingIntent.getActivity(
			context,
			EDITOR_PAGE_REQUEST_CODE,
			widget_add_intent,
			PendingIntent.FLAG_UPDATE_CURRENT
		);
		views.setOnClickPendingIntent(
			R.id.widget_add_button_small,
			widget_add_pending_intent
		);

		SpendingManager spending_manager = new SpendingManager(context);
		String spendings_sum = spending_manager.getSpendingsSum();
		views.setTextViewText(
			R.id.widget_spendings_sum,
			spendings_sum
		);
		if (Double.valueOf(spendings_sum) <= 0.0) {
			views.setTextColor(
				R.id.widget_spendings_sum,
				Color.rgb(0x2b, 0xaa, 0x2b)
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

	private static final int CURRENT_PAGE_REQUEST_CODE = 0;
	private static final int EDITOR_PAGE_REQUEST_CODE = 1;
}
