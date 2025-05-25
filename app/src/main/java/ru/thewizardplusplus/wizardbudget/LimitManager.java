package ru.thewizardplusplus.wizardbudget;

import java.time.*;
import java.time.format.*;
import java.util.*;

import org.json.*;

import android.content.*;
import android.webkit.*;

public class LimitManager {
	public LimitManager(Context context) {
		this.context = context;
	}

	public DateRange<LocalDate> findCurrentLimitDayRange() {
		LocalDate today = LocalDate.now();
		List<Integer> limit_days = Settings.getCurrent(context).getLimitDays();

		List<LocalDate> limit_dates = new ArrayList<>();
		for (int limit_day: limit_days) {
			limit_dates.add(toSafeDate(
				today.getYear(),
				today.getMonthValue(),
				limit_day
			));
		}

		LocalDate prev_month_date = today.minusMonths(1);
		limit_dates.add(0, toSafeDate(
			prev_month_date.getYear(),
			prev_month_date.getMonthValue(),
			limit_days.get(limit_days.size() - 1)
		));

		LocalDate next_month_date = today.plusMonths(1);
		limit_dates.add(toSafeDate(
			next_month_date.getYear(),
			next_month_date.getMonthValue(),
			limit_days.get(0)
		));

		DateRange<LocalDate> range = null;
		for (int index = 1; index < limit_dates.size(); index++) {
			// the start is an exclusive boundary
			LocalDate start = limit_dates.get(index - 1).plusDays(1);
			// the end is an inclusive boundary
			LocalDate end = limit_dates.get(index);

			if (!today.isAfter(end)) {
				range = new DateRange<>(start, end);
				break;
			}
		}
		if (range == null) {
			throw new IllegalStateException(
				"unable to find the current limit day range"
			);
		}

		return range;
	}

	@JavascriptInterface
	public String findCurrentLimitDayRangeAsJson() {
		DateRange<LocalDate> range = findCurrentLimitDayRange();

		JSONObject range_as_json = new JSONObject();
		try {
			range_as_json.put("start", formatDate(range.getStart()));
			range_as_json.put("end", formatDate(range.getEnd()));
		} catch (JSONException exception) {}

		return range_as_json.toString();
	}

	public LimitData getLimitData() {
		DateRange<LocalDate> range = findCurrentLimitDayRange();

		Settings settings = Settings.getCurrent(context);
		double maximal_range_spendings_sum = settings.getLimitAmount();

		SpendingManager spending_manager = new SpendingManager(context);
		String current_day_as_string = formatDate(LocalDate.now());
		double current_day_spendings_sum =
			Double.valueOf(spending_manager.getPositiveSpendingsSum(
				current_day_as_string,
				current_day_as_string
			));
		double current_range_spendings_sum =
			Double.valueOf(spending_manager.getPositiveSpendingsSum(
				formatDate(range.getStart()),
				formatDate(range.getEnd())
			))
			- current_day_spendings_sum;

		return new LimitData(
			range,
			maximal_range_spendings_sum,
			current_range_spendings_sum,
			current_day_spendings_sum
		);
	}

	private Context context;

	private LocalDate toSafeDate(int year, int month, int day) {
		int safe_day = Math.min(day, YearMonth.of(year, month).lengthOfMonth());
		return LocalDate.of(year, month, safe_day);
	}

	private String formatDate(LocalDate date) {
		return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}
}
