package ru.thewizardplusplus.wizardbudget;

import java.text.*;
import java.util.*;

import org.json.*;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.graphics.*;
import android.widget.*;

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

		Intent widget_update_hours_intent = new Intent(
			context,
			MainActivity.class
		);
		widget_update_hours_intent.putExtra(
			Settings.SETTING_NAME_CURRENT_SEGMENT,
			"hours"
		);
		widget_update_hours_intent.putExtra(
			Settings.SETTING_NAME_NEED_UPDATE_HOURS,
			true
		);

		PendingIntent widget_update_hours_pending_intent =
			PendingIntent.getActivity(
				context,
				HOURS_PAGE_REQUEST_CODE,
				widget_update_hours_intent,
				PendingIntent.FLAG_UPDATE_CURRENT
			);
		views.setOnClickPendingIntent(
			R.id.widget_update_hours_button_small,
			widget_update_hours_pending_intent
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

		Settings settings = Settings.getCurrent(context);
		double hours_difference = 0;
		double hours_working_off = 0;
		String hours_working_off_mode = "normal";
		try {
			JSONObject hours_data = new JSONObject(settings.getHoursData());
			hours_difference = hours_data.optDouble("difference");
			hours_working_off = hours_data.optDouble("working_off");
			hours_working_off_mode = hours_data.optString("working_off_mode", "normal");
		} catch(JSONException exception) {}

		DecimalFormat format = new DecimalFormat(
			"#0.0#",
			new DecimalFormatSymbols(Locale.US)
		);
		views.setTextViewText(
			R.id.widget_lack_hours,
			format.format(hours_difference)
		);
		if (hours_difference <= 0.0) {
			views.setTextColor(
				R.id.widget_lack_hours,
				Color.rgb(0x2b, 0xaa, 0x2b)
			);
		} else {
			views.setTextColor(
				R.id.widget_lack_hours,
				Color.rgb(0xff, 0x44, 0x44)
			);
		}

		if (!hours_working_off_mode.equals("none")) {
			if (!hours_working_off_mode.equals("infinity")) {
				views.setTextViewText(
					R.id.widget_working_off_hours,
					format.format(hours_working_off)
				);

				if (hours_working_off <= settings.getWorkingOffLimit()) {
					views.setTextColor(
						R.id.widget_working_off_hours,
						Color.rgb(0x2b, 0xaa, 0x2b)
					);
				} else {
					views.setTextColor(
						R.id.widget_working_off_hours,
						Color.rgb(0xff, 0x44, 0x44)
					);
				}
			} else {
				views.setTextViewText(R.id.widget_working_off_hours, "\u221e");
				views.setTextColor(
					R.id.widget_working_off_hours,
					Color.rgb(0xff, 0x44, 0x44)
				);
			}
		} else {
			views.setTextViewText(R.id.widget_working_off_hours, "\u2014");
			views.setTextColor(
				R.id.widget_working_off_hours,
				Color.rgb(0x2b, 0xaa, 0x2b)
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
	private static final int HOURS_PAGE_REQUEST_CODE = 2;
}
