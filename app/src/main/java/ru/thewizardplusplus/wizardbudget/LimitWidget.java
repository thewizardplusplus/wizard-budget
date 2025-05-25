package ru.thewizardplusplus.wizardbudget;

import java.time.*;
import java.time.format.*;
import java.text.*;
import java.util.*;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;

public class LimitWidget extends AppWidgetProvider {
	public static RemoteViews getUpdatedViews(Context context) {
		RemoteViews views = new RemoteViews(
			context.getPackageName(),
			R.layout.limit_widget
		);

		Intent widget_intent = new Intent(context, MainActivity.class);
		widget_intent.putExtra(Settings.SETTING_NAME_CURRENT_PAGE, "history");
		widget_intent.putExtra(Settings.SETTING_NAME_CURRENT_SEGMENT, "limits");

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
		views.setOnClickPendingIntent(
			R.id.limit_widget_stub,
			widget_pending_intent
		);

		Settings settings = Settings.getCurrent(context);
		boolean is_consider_limits = settings.isConsiderLimits();
		if (is_consider_limits) {
			LimitManager limit_manager = new LimitManager(context);
			LimitData limit_data = limit_manager.getLimitData();

			views.setTextViewText(
				R.id.limits_range_start,
				formatDateForView(limit_data.getRange().getStart())
			);
			views.setTextViewText(
				R.id.limits_range_end,
				formatDateForView(limit_data.getRange().getEnd())
			);

			double current_range_sum = limit_data.getCurrentRangeSpendingsSum();
			double maximal_range_sum = limit_data.getMaximalRangeSpendingsSum();
			views.setTextViewText(
				R.id.current_range_spendings_sum_view,
				formatDouble(current_range_sum)
			);
			views.setTextColor(
				R.id.current_range_spendings_sum_view,
				current_range_sum <= maximal_range_sum
					? Color.rgb(0x2b, 0xaa, 0x2b)
					: Color.rgb(0xff, 0x44, 0x44)
			);
			views.setTextViewText(
				R.id.maximal_range_spendings_sum_view,
				formatDouble(maximal_range_sum)
			);

			views.setTextViewText(
				R.id.remaining_amount_view,
				formatDouble(limit_data.getRemainingAmount())
			);
			views.setTextViewText(
				R.id.remaining_days_view,
				limit_data.getRemainingDaysWithUnits()
			);

			double current_day_sum = limit_data.getCurrentDaySpendingsSum();
			double maximal_day_sum = limit_data.getMaximalDaySpendingsSum();
			views.setTextViewText(
				R.id.current_day_spendings_sum_view,
				formatDouble(current_day_sum)
			);
			views.setTextColor(
				R.id.current_day_spendings_sum_view,
				current_day_sum <= maximal_day_sum
					? Color.rgb(0x2b, 0xaa, 0x2b)
					: Color.rgb(0xff, 0x44, 0x44)
			);
			views.setTextViewText(
				R.id.maximal_day_spendings_sum_view,
				formatDouble(maximal_day_sum)
			);

			if (limit_data.getRemainingDays() == 1) {
				views.setTextViewText(R.id.maximal_tomorrow_spendings_sum_view, "—");
			} else {
				double maximal_tomorrow_sum =
					limit_data.getMaximalTomorrowSpendingsSum();
				views.setTextViewText(
					R.id.maximal_tomorrow_spendings_sum_view,
					formatDouble(maximal_tomorrow_sum)
				);

				double maximal_first_day_sum =
					limit_data.getMaximalFirstDaySpendingsSum();
				if (
					maximal_tomorrow_sum > maximal_first_day_sum
					|| Math.abs(maximal_tomorrow_sum - maximal_first_day_sum) <= 1e-6
				) {
					views.setViewVisibility(
						R.id.day_without_spendings_container,
						View.GONE
					);
				} else {
					views.setViewVisibility(
						R.id.day_without_spendings_container,
						View.VISIBLE
					);

					views.setTextViewText(
						R.id.day_without_spendings_view,
						limit_data.getDaysWithoutSpendingsWithUnits()
					);
					views.setTextViewText(
						R.id.maximal_first_day_spendings_sum_view,
						formatDouble(maximal_first_day_sum)
					);
				}
			}
		}

		views.setDisplayedChild(
			R.id.limit_widget_view_flipper,
			is_consider_limits ? 0 : 1
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

	private static String formatDateForView(LocalDate date) {
		DateTimeFormatter date_format = DateTimeFormatter.ofLocalizedDate(
			FormatStyle.MEDIUM
		);
		return date.format(date_format);
	}

	private static String formatDateForSearch(LocalDate date) {
		return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	private static String formatDouble(double value) {
		DecimalFormat decimal_format = new DecimalFormat(
			"#0.0#",
			new DecimalFormatSymbols(Locale.US)
		);
		return decimal_format.format(value);
	}
}
