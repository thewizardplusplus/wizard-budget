package ru.thewizardplusplus.wizardbudget;

public class DateRange<DateType> {
	public DateRange(DateType start, DateType end) {
		this.start = start;
		this.end = end;
	}

	public DateType getStart() {
		return start;
	}

	public DateType getEnd() {
		return end;
	}

	private DateType start;
	private DateType end;
}
