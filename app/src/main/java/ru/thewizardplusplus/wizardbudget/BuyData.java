package ru.thewizardplusplus.wizardbudget;

import android.text.*;
import android.text.style.*;
import android.graphics.*;

public class BuyData {
	public BuyData(String name, double cost, boolean is_monthly) {
		this.name = name;
		this.cost = cost;
		this.is_monthly = is_monthly;
	}

	public String getName() {
		return name;
	}

	public double getCost() {
		return cost;
	}

	public CharSequence getMonthlyFlag() {
		String monthly_flag = is_monthly ? "[MONTHLY]" : "";

		SpannableString formatted_monthly_flag = new SpannableString(monthly_flag); 
		formatted_monthly_flag.setSpan(new StyleSpan(Typeface.ITALIC), 0, monthly_flag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return formatted_monthly_flag;
	}

	private String name;
	private double cost;
	private boolean is_monthly;
}
