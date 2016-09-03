package ru.thewizardplusplus.wizardbudget;

public enum BuyType {
	SINGLE,
	MONTHLY,
	ANY;

	public String toCondition() {
		String condition = "";
		switch (this) {
			case SINGLE:
				condition = " AND monthly = 0";
				break;
			case MONTHLY:
				condition = " AND monthly = 1";
				break;
			default:
				break;
		}

		return "status = 0" + condition;
	}
}
