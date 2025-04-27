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

	public boolean isMonthly() {
		return is_monthly;
	}

	private String name;
	private double cost;
	private boolean is_monthly;
}
