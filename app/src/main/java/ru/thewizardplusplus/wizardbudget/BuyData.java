package ru.thewizardplusplus.wizardbudget;

public class BuyData {
	public BuyData(String name, double cost) {
		this.name = name;
		this.cost = cost;
	}

	public String getName() {
		return name;
	}

	public double getCost() {
		return cost;
	}

	private String name;
	private double cost;
}
