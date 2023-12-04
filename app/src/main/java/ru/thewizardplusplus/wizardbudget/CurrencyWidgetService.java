package ru.thewizardplusplus.wizardbudget;

import android.widget.*;
import android.content.*;

public class CurrencyWidgetService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new CurrencyWidgetFactory(this.getApplicationContext());
	}
}
