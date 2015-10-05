package ru.thewizardplusplus.wizardbudget;

public class SmsData {
	public SmsData(double spending, double residue, String comment) {
		this.spending = spending;
		this.residue = residue;
		this.comment = comment;
	}

	public double getSpending() {
		return spending;
	}

	public double getResidue() {
		return residue;
	}

	public String getComment() {
		return comment;
	}

	private double spending = 0.0;
	private double residue = 0.0;
	private String comment = "";
}
