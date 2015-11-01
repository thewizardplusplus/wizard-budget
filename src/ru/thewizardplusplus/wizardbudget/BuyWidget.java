package ru.thewizardplusplus.wizardbudget;

import android.appwidget.*;
import android.content.*;
import android.widget.*;
import android.app.*;

public class BuyWidget extends AppWidgetProvider {
	public static RemoteViews getUpdatedViews(Context context) {
		return new RemoteViews(
			context.getPackageName(),
			R.layout.buy_widget
		);
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
