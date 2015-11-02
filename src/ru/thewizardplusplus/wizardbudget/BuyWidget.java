package ru.thewizardplusplus.wizardbudget;

import android.appwidget.*;
import android.content.*;
import android.widget.*;
import android.app.*;
import android.net.*;

public class BuyWidget extends AppWidgetProvider {
	@Override
	public void onUpdate(
		Context context,
		AppWidgetManager widget_manager,
		int[] widget_ids
	) {
		super.onUpdate(context, widget_manager, widget_ids);

		for (int widget_id: widget_ids) {
			Intent item_intent = new Intent(context, BuyWidgetService.class);
			item_intent.setData(Uri.parse(item_intent.toUri(Intent.URI_INTENT_SCHEME)));

			Intent click_intent = new Intent(context, MainActivity.class);
			PendingIntent click_pending_intent = PendingIntent.getActivity(
				context,
				0,
				click_intent,
				PendingIntent.FLAG_UPDATE_CURRENT
			);

			RemoteViews views = new RemoteViews(
				context.getPackageName(),
				R.layout.buy_widget
			);
			views.setRemoteAdapter(R.id.buy_list, item_intent);
			views.setPendingIntentTemplate(R.id.buy_list, click_pending_intent);

			widget_manager.updateAppWidget(widget_id, views);
			widget_manager.notifyAppWidgetViewDataChanged(widget_id, R.id.buy_list);
		}
	}
}
