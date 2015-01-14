package ru.thewizardplusplus.wizardbudget;

import android.app.*;
import android.os.*;
import android.webkit.*;
import android.widget.*;
import android.content.*;
import android.appwidget.*;
import android.net.*;
import java.io.*;

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
		web_view.addJavascriptInterface(this, "activity");
	}

	@JavascriptInterface
	public void updateWidget() {
		RemoteViews views = Widget.getUpdatedViews(this);
		ComponentName widget = new ComponentName(this, Widget.class);
		AppWidgetManager.getInstance(this).updateAppWidget(widget, views);
	}

	@JavascriptInterface
	public void selectBackupForRestore() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("text/xml");

		startActivityForResult(intent, FILE_SELECT_CODE);
	}

	@Override
	protected void onActivityResult(
		int request_code,
		int result_code,
		Intent data
	) {
		if (
			request_code == FILE_SELECT_CODE
			&& result_code == Activity.RESULT_OK
			) {
			Uri uri = data.getData();
			if (uri != null) {
				String path = uri.getPath();
				if (path != null) {
					String[] path_parts = path.split(":");
					restoreBackup(
						Environment.getExternalStorageDirectory()
						+ "/"
						+ path_parts[path_parts.length > 1 ? 1 : 0]
					);

					WebView web_view = (WebView)findViewById(R.id.web_view);
					web_view.loadUrl("javascript:GUI.refresh()");
				}
			}
		}
	}

	private static final int FILE_SELECT_CODE = 1;

	private void restoreBackup(String filename) {
		File backup_file = new File(filename);
		InputStream in = null;
		try {
			try {
				in = new BufferedInputStream(new FileInputStream(backup_file));

				SpendingManager spending_manager = new SpendingManager(this);
				spending_manager.restore(in);
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (IOException exception) {}
	}
}
