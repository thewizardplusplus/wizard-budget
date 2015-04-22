package ru.thewizardplusplus.wizardbudget;

import java.io.*;
import java.util.*;
import java.text.*;

import org.xmlpull.v1.*;

import android.content.*;
import android.webkit.*;
import android.database.sqlite.*;
import android.database.*;
import android.os.*;
import android.util.*;

public class BackupManager {
	public BackupManager(Context context) {
		this.context = context;
	}

	@JavascriptInterface
	public String backup() {
		String filename = "";
		try {
			File directory = new File(
				Environment.getExternalStorageDirectory(),
				BACKUPS_DIRECTORY
			);
			directory.mkdirs();

			Date current_date = new Date();
			SimpleDateFormat file_suffix_format = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss",
				Locale.US
			);
			String file_suffix = file_suffix_format.format(current_date);

			File file = new File(
				directory,
				"database_dump_" + file_suffix + ".xml"
			);
			FileWriter writter = new FileWriter(file);
			try {
				XmlSerializer serializer = Xml.newSerializer();
				serializer.setOutput(writter);
				serializer.setFeature(
					"http://xmlpull.org/v1/doc/features.html#indent-output",
					true
				);
				/*serializer.setProperty(
				 "http://xmlpull.org/v1/doc/properties.html"
				 + "#serializer-indentation",
				 "\t"
				 );*/
				serializer.startDocument("utf-8", true);
				serializer.startTag("", "spendings");

				SQLiteDatabase database = Utils.getDatabase(context);
				Cursor cursor = database.query(
					"spendings",
					new String[]{"timestamp", "amount", "comment"},
					null,
					null,
					null,
					null,
					"timestamp"
				);

				boolean moved = cursor.moveToFirst();
				while (moved) {
					serializer.startTag("", "spending");

					Date date = new Date(cursor.getLong(0) * 1000L);
					String formatted_date = XML_DATE_FORMAT.format(date);
					serializer.attribute("", "date", formatted_date);

					serializer.attribute(
						"",
						"amount",
						String.valueOf(cursor.getDouble(1))
					);
					serializer.attribute("", "comment", cursor.getString(2));
					serializer.endTag("", "spending");

					moved = cursor.moveToNext();
				}

				database.close();

				serializer.endTag("", "spendings");
				serializer.endDocument();

				if (Settings.getCurrent(context).isBackupNotification()) {
					DateFormat notification_timestamp_format =
						DateFormat
						.getDateTimeInstance(
						DateFormat.DEFAULT,
						DateFormat.DEFAULT,
						Locale.US
					);
					String notification_timestamp =
						notification_timestamp_format
						.format(current_date);
					Utils.showNotification(
						context,
						context.getString(R.string.app_name),
						"Backuped at " + notification_timestamp + ".",
						file
					);
				}

				filename = file.getAbsolutePath();
			} finally {
				writter.close();
			}
		} catch (IOException exception) {}

		return filename;
	}

	private static final String BACKUPS_DIRECTORY = "#wizard-budget";
	private static final SimpleDateFormat XML_DATE_FORMAT =
	new SimpleDateFormat(
		"yyyy-MM-dd HH:mm:ss",
		Locale.US
	);

	private Context context;
}
