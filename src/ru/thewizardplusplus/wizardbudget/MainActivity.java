package ru.thewizardplusplus.wizardbudget;

import android.app.*;
import android.os.*;
import android.webkit.*;

public class MainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		WebView web_view = (WebView)findViewById(R.id.web_view);
		web_view.getSettings().setJavaScriptEnabled(true);
		web_view.loadUrl("file:///android_asset/web/index.html");

		SpendingManager spending_manager = new SpendingManager(this);
		web_view.addJavascriptInterface(spending_manager, "spending_manager");
	}
}
