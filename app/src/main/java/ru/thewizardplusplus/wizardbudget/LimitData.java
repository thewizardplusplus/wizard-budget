package ru.thewizardplusplus.wizardbudget;

import java.time.*;
import java.time.temporal.*;

public class LimitData {
	public LimitData(
		DateRange<LocalDate> range,
		double maximal_range_spendings_sum,
		double current_range_spendings_sum,
		double current_day_spendings_sum
	) {
		this.range = range;
		this.maximal_range_spendings_sum = maximal_range_spendings_sum;
		this.current_range_spendings_sum = current_range_spendings_sum;
		this.current_day_spendings_sum = current_day_spendings_sum;
	}

	public DateRange<LocalDate> getRange() {
		return range;
	}

	public double getMaximalRangeSpendingsSum() {
		return maximal_range_spendings_sum;
	}

	public double getCurrentRangeSpendingsSum() {
		return current_range_spendings_sum;
	}

	public long getRemainingDays() {
		return ChronoUnit.DAYS.between(LocalDate.now(), range.getEnd()) + 1;
	}

	public String getRemainingDaysWithUnits() {
		long remaining_days = getRemainingDays();
		return String.format(
			"%d %s",
			remaining_days,
			remaining_days == 1 ? "day" : "days"
		);
	}

	public double getRemainingAmount() {
		return maximal_range_spendings_sum - current_range_spendings_sum;
	}

	public double getMaximalDaySpendingsSum() {
		return getRemainingAmount() / getRemainingDays();
	}

	public double getMaximalTomorrowSpendingsSum() {
		return (getRemainingAmount() - current_day_spendings_sum)
			/ (getRemainingDays() - 1);
	}

	public double getCurrentDaySpendingsSum() {
		return current_day_spendings_sum;
	}

	private DateRange<LocalDate> range;
	private double current_range_spendings_sum;
	private double maximal_range_spendings_sum;
	private double current_day_spendings_sum;
}
