package ru.thewizardplusplus.wizardbudget;

import java.text.*;
import java.util.*;
import java.io.*;

import org.json.*;
import org.xmlpull.v1.*;

import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.net.Uri;
import android.os.*;
import android.util.*;
import android.webkit.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public class SpendingManager {
	public SpendingManager(Context context) {
		this.context = context;
	}

	@JavascriptInterface
	public String getSpendingsSum() {
		SQLiteDatabase database = getDatabase();
		Cursor cursor = database.query(
			"spendings",
			new String[]{"ROUND(SUM(amount), 2)"},
			null,
			null,
			null,
			null,
			null
		);

		double spendings_sum = 0.0;
		boolean moved = cursor.moveToFirst();
		if (moved) {
			spendings_sum = cursor.getDouble(0);
		}

		database.close();
		return String.valueOf(spendings_sum);
	}

	@JavascriptInterface
	public String getAllSpendings() {
		SQLiteDatabase database = getDatabase();
		Cursor start_timestamp_cursor = database.query(
			"spendings",
			new String[]{"MIN(timestamp)"},
			null,
			null,
			null,
			null,
			null
		);

		long start_timestamp = 0;
		boolean moved = start_timestamp_cursor.moveToFirst();
		if (moved) {
			start_timestamp = resetTimestampToDayBegin(
				start_timestamp_cursor.getLong(0)
			);
		}

		Cursor spendings_cursor = database.query(
			"spendings",
			new String[]{"_id", "timestamp", "amount", "comment"},
			null,
			null,
			null,
			null,
			"timestamp DESC"
		);

		JSONArray spendings = new JSONArray();
		moved = spendings_cursor.moveToFirst();
		while (moved) {
			try {
				JSONObject spending = new JSONObject();
				spending.put("id", spendings_cursor.getDouble(0));
				spending.put(
					"date",
					formatDateAsMine(
						spendings_cursor.getLong(1),
						start_timestamp
					)
				);
				spending.put("amount", spendings_cursor.getDouble(2));
				spending.put("comment", spendings_cursor.getString(3));

				spendings.put(spending);
			} catch (JSONException exception) {}

			moved = spendings_cursor.moveToNext();
		}

		database.close();
		return spendings.toString();
	}

	@JavascriptInterface
	public void createSpending(double amount, String comment) {
		ContentValues values = new ContentValues();
		values.put("timestamp", System.currentTimeMillis() / 1000L);
		values.put("amount", amount);
		values.put("comment", comment);

		SQLiteDatabase database = getDatabase();
		database.insert("spendings", null, values);
		database.close();
	}

	@JavascriptInterface
	public void updateSpending(int id, double amount, String comment) {
		ContentValues values = new ContentValues();
		values.put("amount", amount);
		values.put("comment", comment);

		SQLiteDatabase database = getDatabase();
		database.update("spendings", values, "_id = ?", new String[]{String.valueOf(id)});
		database.close();
	}

	@JavascriptInterface
	public void deleteSpending(int id) {
		SQLiteDatabase database = getDatabase();
		database.delete("spendings", "_id = ?", new String[]{String.valueOf(id)});
		database.close();
	}

	@JavascriptInterface
	public void backup() {
		SQLiteDatabase database = getDatabase();
		Cursor cursor = database.query(
			"spendings",
			new String[]{"timestamp", "amount", "comment"},
			null,
			null,
			null,
			null,
			"timestamp"
		);

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

				SimpleDateFormat date_format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss",
					Locale.US
				);
				boolean moved = cursor.moveToFirst();
				while (moved) {
					serializer.startTag("", "spending");

					Date date = new Date(cursor.getLong(0) * 1000L);
					String formatted_date = date_format.format(date);
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

				serializer.endTag("", "spendings");
				serializer.endDocument();

				DateFormat notification_timestamp_format = DateFormat
					.getDateInstance(DateFormat.DEFAULT, Locale.US);
				String notification_timestamp = notification_timestamp_format
					.format(current_date);
				showBackupNotification(
					context.getString(R.string.app_name),
					"Backuped at " + notification_timestamp + ".",
					file
				);
			} finally {
				writter.close();
			}
		} catch (IOException exception) {}

		database.close();
	}

	public void restore(InputStream in) {
		String sql = "";
		try {
			Element history = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(in)
				.getDocumentElement();
			history.normalize();
			NodeList days = history.getElementsByTagName("day");
			for (int i = 0; i < days.getLength(); i++) {
				if (days.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element day = (Element)days.item(i);
					if (!day.hasAttribute("date")) {
						continue;
					}

					NodeList foods = day.getElementsByTagName("food");
					for (int j = 0; j < foods.getLength(); j++) {
						if (foods.item(j).getNodeType() == Node.ELEMENT_NODE) {
							Element food = (Element)foods.item(j);
							if (
								!food.hasAttribute("weight")
								|| !food.hasAttribute("calories")
								) {
								continue;
							}

							if (!sql.isEmpty()) {
								sql += ",";
							}
							sql += "("
								+ food.getAttribute("weight") + ","
								+ food.getAttribute("calories") + ","
								+ "'" + day.getAttribute("date") + "'"
								+ ")";
						}
					}
				}
			}
		} catch (ParserConfigurationException exception) {
			processRestoreException();
			return;
		} catch (SAXException exception) {
			processRestoreException();
			return;
		} catch (IOException exception) {
			processRestoreException();
			return;
		} catch (DOMException exception) {
			processRestoreException();
			return;
		}

		/*if (!sql.isEmpty()) {
			SQLiteDatabase database = database_helper.getWritableDatabase();
			database.execSQL("DELETE FROM day_data_list");
			database.execSQL(
				"INSERT INTO day_data_list"
				+ "(weight, calories, date)"
				+ "VALUES" + sql
			);

			database.close();
		}*/
	}

	private static final String BACKUPS_DIRECTORY = "#wizard-budget";
	private static final long DAYS_IN_MY_YEAR = 300;
	private static final int NOTIFICATION_ID = 0;

	private Context context;

	private SQLiteDatabase getDatabase() {
		DatabaseHelper database_helper = new DatabaseHelper(context);
		return database_helper.getWritableDatabase();
	}

	private String formatDateAsMine(long timestamp, long start_timestamp) {
		timestamp = resetTimestampToDayBegin(timestamp);

		long days = (timestamp - start_timestamp) / (24 * 60 * 60);
		long day = days % DAYS_IN_MY_YEAR + 1;
		long year = days / DAYS_IN_MY_YEAR + 1;

		return
			(day < 10 ? "0" : "") + String.valueOf(day) + "."
			+ (year < 10 ? "0" : "") + String.valueOf(year) + ".";
	}

	private long resetTimestampToDayBegin(long timestamp) {
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(timestamp * 1000L);
		time.set(Calendar.HOUR_OF_DAY, 0);
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);

		return time.getTimeInMillis() / 1000L;
	}

	public void showBackupNotification(
		String title,
		String message,
		File file
	) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "text/xml");
		PendingIntent pending_intent = PendingIntent.getActivity(
			context,
			0,
			intent,
			0
		);

		@SuppressWarnings("deprecation")
		Notification notification = new Notification.Builder(context)
			.setTicker(title)
			.setSmallIcon(R.drawable.app_icon)
			.setContentTitle(title)
			.setContentText(message)
			.setContentIntent(pending_intent)
			.getNotification();

		NotificationManager notifications = (NotificationManager)context
			.getSystemService(
				Context.NOTIFICATION_SERVICE
			);
		notifications.notify(NOTIFICATION_ID, notification);
	}

	private void processRestoreException() {
		Utils.showAlertDialog(
			context,
			context.getString(R.string.error_message_box_title),
			context.getString(R.string.restore_backup_error_message)
		);
	}
}
