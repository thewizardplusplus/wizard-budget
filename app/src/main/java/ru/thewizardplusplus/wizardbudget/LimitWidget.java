package ru.thewizardplusplus.wizardbudget;

import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.text.*;
import java.util.*;

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

		LimitManager limit_manager = new LimitManager(context);
		DateRange<LocalDate> range = limit_manager.findCurrentLimitDayRange();
		views.setTextViewText(
			R.id.limits_range_start,
			formatDateForView(range.getStart())
		);
		views.setTextViewText(
			R.id.limits_range_end,
			formatDateForView(range.getEnd())
		);

		long remaining_days =
			ChronoUnit.DAYS.between(LocalDate.now(), range.getEnd()) + 1;
		views.setTextViewText(
			R.id.remaining_days_view,
			String.format(
				"%d %s",
				remaining_days,
				remaining_days == 1 ? "day" : "days"
			)
		);

		Settings settings = Settings.getCurrent(context);
		double maximal_range_sum = settings.getLimitAmount();
		views.setTextViewText(
			R.id.maximal_range_spendings_sum_view,
			formatDouble(maximal_range_sum)
		);

		SpendingManager spending_manager = new SpendingManager(context);
		double current_range_sum =
			Double.valueOf(spending_manager.getPositiveSpendingsSum(
				formatDateForSearch(range.getStart()),
				formatDateForSearch(range.getEnd())
			));
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

		double remaining_amount = maximal_range_sum - current_range_sum;
		views.setTextViewText(
			R.id.remaining_amount_view,
			formatDouble(remaining_amount)
		);

		double maximal_day_sum = remaining_amount / remaining_days;
		views.setTextViewText(
			R.id.maximal_day_spendings_sum_view,
			formatDouble(maximal_day_sum)
		);

		String current_day_as_string = formatDateForSearch(LocalDate.now());
		double current_day_sum =
			Double.valueOf(spending_manager.getPositiveSpendingsSum(
				current_day_as_string,
				current_day_as_string
			));
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
