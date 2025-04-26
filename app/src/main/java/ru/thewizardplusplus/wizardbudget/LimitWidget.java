package ru.thewizardplusplus.wizardbudget;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.graphics.*;
import android.widget.*;

public class LimitWidget extends AppWidgetProvider {
	public static RemoteViews getUpdatedViews(Context context) {
		RemoteViews views = new RemoteViews(
			context.getPackageName(),
			R.layout.limit_widget
		);

		Intent widget_intent = new Intent(context, MainActivity.class);
		PendingIntent widget_pending_intent = PendingIntent.getActivity(
			context,
			CURRENT_PAGE_REQUEST_CODE,
			widget_intent,
			PendingIntent.FLAG_UPDATE_CURRENT
		);
		views.setOnClickPendingIntent(
			R.id.limit_widget_container,
			widget_pending_intent
		);

		views.setTextViewText(R.id.limits_range_start, "2025-02-01");
		views.setTextViewText(R.id.limits_range_end, "2025-03-01");

		views.setTextViewText(R.id.remaining_days_view, "10 days");

		views.setTextViewText(R.id.maximal_range_spendings_sum_view, "100000");
		views.setTextViewText(R.id.current_range_spendings_sum_view, "75000");
		views.setTextColor(
			R.id.current_range_spendings_sum_view,
			Color.rgb(0x2b, 0xaa, 0x2b)
		);

		views.setTextViewText(R.id.remaining_amount_view, "25000");

		views.setTextViewText(R.id.maximal_day_spendings_sum_view, "2500");
		views.setTextViewText(R.id.current_day_spendings_sum_view, "1800");
		views.setTextColor(
			R.id.current_day_spendings_sum_view,
			Color.rgb(0x2b, 0xaa, 0x2b)
		);

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
	}

	private static final int CURRENT_PAGE_REQUEST_CODE = 0;
}
