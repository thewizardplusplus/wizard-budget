package ru.thewizardplusplus.wizardbudget;

import android.appwidget.*;
import android.content.*;
import android.widget.*;
import android.app.*;
import android.net.*;

public class CurrencyWidget extends AppWidgetProvider {
	public static RemoteViews getUpdatedViews(Context context) {
		Intent item_intent = new Intent(context, CurrencyWidgetService.class);
		item_intent.setData(
			Uri.parse(item_intent.toUri(Intent.URI_INTENT_SCHEME))
		);

		Intent click_intent = new Intent(context, MainActivity.class);
		PendingIntent click_pending_intent = PendingIntent.getActivity(
			context,
			0,
			click_intent,
			PendingIntent.FLAG_UPDATE_CURRENT
		);

		RemoteViews views = new RemoteViews(
			context.getPackageName(),
			R.layout.currency_widget
		);
		views.setRemoteAdapter(R.id.currency_list, item_intent);
		views.setOnClickPendingIntent(
			R.id.currency_widget_container,
			click_pending_intent
		);
		views.setPendingIntentTemplate(R.id.currency_list, click_pending_intent);
		views.setEmptyView(R.id.currency_list, R.id.currency_empty_list_stub);

		return views;
	}

	@Override
	public void onUpdate(
		Context context,
		AppWidgetManager widget_manager,
		int[] widget_ids
	) {
		super.onUpdate(context, widget_manager, widget_ids);

		RemoteViews views = getUpdatedViews(context);
		widget_manager.updateAppWidget(widget_ids, views);
		widget_manager.notifyAppWidgetViewDataChanged(
			widget_ids,
			R.id.currency_list
		);
	}
}
