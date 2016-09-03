package ru.thewizardplusplus.wizardbudget;

import android.widget.*;
import android.content.*;

public class BuyWidgetService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new BuyWidgetFactory(this.getApplicationContext());
	}
}
