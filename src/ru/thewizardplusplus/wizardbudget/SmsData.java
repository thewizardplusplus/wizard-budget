package ru.thewizardplusplus.wizardbudget;

public class SmsData {
	public SmsData(double spending, String comment) {
		this.spending = spending;
		this.comment = comment;
	}

	public double getSpending() {
		return spending;
	}

	public String getComment() {
		return comment;
	}

	private double spending = 0.0;
	private String comment = "";
}
