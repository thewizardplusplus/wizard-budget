package ru.thewizardplusplus.wizardbudget;

import android.content.*;
import android.widget.*;

public class BuyResetReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "[BUY RESET]", Toast.LENGTH_LONG).show();
	}
}
