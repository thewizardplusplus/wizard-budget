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
import java.util.regex.*;

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
		long start_timestamp =
			Settings
				.getCurrent(context)
				.getCustomDateBaseDay()
				.getTimeInMillis()
			/ 1000L;
		boolean moved = spendings_cursor.moveToFirst();
		while (moved) {
			try {
				JSONObject spending = new JSONObject();
				spending.put("id", spendings_cursor.getDouble(0));

				long timestamp = spendings_cursor.getLong(1);
				spending.put("timestamp", String.valueOf(timestamp));
				if (!Settings.getCurrent(context).isUseCustomDate()) {
					spending.put("date", formatDate(timestamp));
				} else {
					spending.put(
						"date",
						formatCustomDate(timestamp, start_timestamp)
					);
				}
				spending.put("time", formatTime(timestamp));

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
	public void updateSpending(
		int id,
		String date,
		String time,
		double amount,
		String comment
	) {
		try {
			long timestamp = 0;
			if (Settings.getCurrent(context).isUseCustomDate()) {
				String[] custom_date_parts = date.split(Pattern.quote("."));
				long day = Long.valueOf(custom_date_parts[0]);
				long year = Long.valueOf(custom_date_parts[1]);
				long days =
					(day < 0 || year < 0 ? -1 : 1)
					* ((Math.abs(day) - (day < 0 || year < 0 ? 0 : 1))
					+ (Math.abs(year) - 1) * DAYS_IN_CUSTOM_YEAR);

				String[] time_parts = time.split(":");
				long hour = Long.valueOf(time_parts[0]);
				long minute = Long.valueOf(time_parts[1]);

				Calendar current_timestamp =
					Settings
					.getCurrent(context)
					.getCustomDateBaseDay();
				current_timestamp.add(Calendar.DAY_OF_MONTH, (int)days);
				current_timestamp.add(Calendar.HOUR_OF_DAY, (int)hour);
				current_timestamp.add(Calendar.MINUTE, (int)minute);

				timestamp = current_timestamp.getTimeInMillis() / 1000L;
			} else {
				String formatted_timestamp = date + " " + time + ":00";
				SimpleDateFormat timestamp_format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss",
					Locale.US
				);
				Date parsed_timestamp = timestamp_format.parse(
					formatted_timestamp
				);
				timestamp = parsed_timestamp.getTime() / 1000L;
			}

			ContentValues values = new ContentValues();
			values.put("timestamp", timestamp);
			values.put("amount", amount);
			values.put("comment", comment);

			SQLiteDatabase database = getDatabase();
			database.update(
				"spendings",
				values,
				"_id = ?",
				new String[]{String.valueOf(id)}
			);
			database.close();
		} catch (ParseException exception) {}
	}

	@JavascriptInterface
	public void deleteSpending(int id) {
		SQLiteDatabase database = getDatabase();
		database.delete(
			"spendings",
			"_id = ?",
			new String[]{String.valueOf(id)}
		);
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

				serializer.endTag("", "spendings");
				serializer.endDocument();

				DateFormat notification_timestamp_format = DateFormat
					.getDateInstance(DateFormat.DEFAULT, Locale.US);
				String notification_timestamp = notification_timestamp_format
					.format(current_date);
				showNotification(
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
			Element spendings = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(in)
				.getDocumentElement();
			spendings.normalize();
			NodeList spending_list = spendings.getElementsByTagName("spending");
			for (int i = 0; i < spending_list.getLength(); i++) {
				if (spending_list.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element spending = (Element)spending_list.item(i);
					if (
						!spending.hasAttribute("date")
						|| !spending.hasAttribute("amount")
						|| !spending.hasAttribute("comment")
					) {
						continue;
					}

					long timestamp = 0;
					try {
						Date date = XML_DATE_FORMAT.parse(
							spending.getAttribute("date")
						);
						timestamp = date.getTime() / 1000L;
					} catch (ParseException exception) {
						continue;
					}

					if (!sql.isEmpty()) {
						sql += ",";
					}
					sql += "("
							+ String.valueOf(timestamp) + ","
							+ spending.getAttribute("amount") + ","
							+ DatabaseUtils.sqlEscapeString(
								spending.getAttribute("comment")
							)
						+ ")";
				}
			}
		} catch (ParserConfigurationException exception) {
			return;
		} catch (SAXException exception) {
			return;
		} catch (IOException exception) {
			return;
		} catch (DOMException exception) {
			return;
		}

		if (!sql.isEmpty()) {
			SQLiteDatabase database = getDatabase();
			database.execSQL("DELETE FROM spendings");
			database.execSQL(
				"INSERT INTO spendings"
				+ "(timestamp, amount, comment)"
				+ "VALUES" + sql
			);
			database.close();

			Date current_date = new Date();
			DateFormat notification_timestamp_format = DateFormat
				.getDateInstance(DateFormat.DEFAULT, Locale.US);
			String notification_timestamp = notification_timestamp_format
				.format(current_date);
			showNotification(
				context.getString(R.string.app_name),
				"Restored at " + notification_timestamp + ".",
				null
			);
		}
	}

	private static final String BACKUPS_DIRECTORY = "#wizard-budget";
	private static final SimpleDateFormat XML_DATE_FORMAT =
		new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss",
			Locale.US
		);
	private static final long DAYS_IN_CUSTOM_YEAR = 300;
	private static final int NOTIFICATION_ID = 0;

	private Context context;

	private SQLiteDatabase getDatabase() {
		DatabaseHelper database_helper = new DatabaseHelper(context);
		return database_helper.getWritableDatabase();
	}

	private String formatDate(long timestamp) {
		Date date = new Date(timestamp * 1000L);
		DateFormat date_format = DateFormat.getDateInstance(
			DateFormat.DEFAULT,
			Locale.US
		);
		return date_format.format(date);
	}

	private String formatCustomDate(long timestamp, long start_timestamp) {
		timestamp = resetTimestampToDayBegin(timestamp);

		long days = (timestamp - start_timestamp) / (24 * 60 * 60);
		long day = days % DAYS_IN_CUSTOM_YEAR;
		if (days >= 0) {
			day += 1;
		}

		long year = days / DAYS_IN_CUSTOM_YEAR;
		if (days >= 0) {
			year += 1;
		} else {
			year -= 1;
		}

		return formatCustomDatePart(day) + "." + formatCustomDatePart(year);
	}

	private long resetTimestampToDayBegin(long timestamp) {
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(timestamp * 1000L);
		time.set(Calendar.HOUR_OF_DAY, 0);
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);

		return time.getTimeInMillis() / 1000L;
	}

	private String formatCustomDatePart(long part) {
		String string_part = String.valueOf(Math.abs(part));
		string_part =
			(part < 0 ? "-" : "")
			+ (string_part.length() == 1 ? "0" : "")
			+ string_part;

		return string_part;
	}

	private String formatTime(long timestamp) {
		Date date = new Date(timestamp * 1000L);
		DateFormat date_format = DateFormat.getTimeInstance(
			DateFormat.DEFAULT,
			Locale.US
		);
		return date_format.format(date);
	}

	public void showNotification(
		String title,
		String message,
		File file
	) {
		Intent intent = null;
		if (file != null) {
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), "text/xml");
		} else {
			intent = new Intent(context, MainActivity.class);
		}
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
}
