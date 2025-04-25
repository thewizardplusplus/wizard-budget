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

	@JavascriptInterface
	public String findCurrentLimitDayRange() {
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
			LocalDate start = limit_dates.get(index - 1);
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

		JSONObject range_as_json = new JSONObject();
		try {
			range_as_json.put("start", formatDate(range.getStart()));
			range_as_json.put("end", formatDate(range.getEnd()));
		} catch (JSONException exception) {}

		return range_as_json.toString();
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
