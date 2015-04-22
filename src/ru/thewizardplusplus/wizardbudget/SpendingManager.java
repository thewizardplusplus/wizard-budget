package ru.thewizardplusplus.wizardbudget;

import java.text.*;
import java.util.*;
import java.io.*;

import javax.xml.parsers.*;

import org.json.*;
import org.xmlpull.v1.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.webkit.*;

public class SpendingManager {
	public SpendingManager(Context context) {
		this.context = context;
	}

	@JavascriptInterface
	public String getSpendingsSum() {
		SQLiteDatabase database = Utils.getDatabase(context);
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
		SQLiteDatabase database = Utils.getDatabase(context);
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
		boolean moved = spendings_cursor.moveToFirst();
		while (moved) {
			try {
				JSONObject spending = new JSONObject();
				spending.put("id", spendings_cursor.getDouble(0));

				long timestamp = spendings_cursor.getLong(1);
				spending.put("timestamp", String.valueOf(timestamp));
				spending.put("date", formatDate(timestamp));
				spending.put("time", formatTime(timestamp));

				spending.put("amount", spendings_cursor.getDouble(2));
				String comment = spendings_cursor.getString(3);
				spending.put("comment", comment);

				boolean has_credit_card_tag = false;
				String credit_card_tag =
					Settings
					.getCurrent(context)
					.getCreditCardTag();
				if (!credit_card_tag.isEmpty()) {
					String[] tags = comment.split(",");
					for (String tag: tags) {
						if (tag.trim().equals(credit_card_tag)) {
							has_credit_card_tag = true;
							break;
						}
					}
				}
				spending.put("has_credit_card_tag", has_credit_card_tag);

				spendings.put(spending);
			} catch (JSONException exception) {}

			moved = spendings_cursor.moveToNext();
		}

		database.close();
		return spendings.toString();
	}

	@JavascriptInterface
	public String getSpendingsFromSms() {
		Uri uri = Uri.parse("content://sms/inbox");
		Cursor cursor = context.getContentResolver().query(
			uri,
			null,
			null,
			null,
			null
		);
		JSONArray spendings = new JSONArray();
		if (cursor.moveToFirst()) {
			do {
				long timestamp = 0;
				try {
					String timestamp_string =
						cursor
						.getString(cursor.getColumnIndexOrThrow("date"))
						.toString();
					timestamp = Long.valueOf(timestamp_string) / 1000L;
				} catch (NumberFormatException exception) {
					continue;
				}

				String number =
					cursor
					.getString(cursor.getColumnIndexOrThrow("address"))
					.toString();
				String text =
					cursor
					.getString(cursor.getColumnIndexOrThrow("body"))
					.toString();

				SmsData sms_data = Utils.getSpendingFromSms(
					context,
					number,
					text
				);
				if (sms_data == null) {
					continue;
				}

				try {
					JSONObject spending = new JSONObject();
					spending.put("timestamp", timestamp);
					spending.put("date", formatDate(timestamp));
					spending.put("time", formatTime(timestamp));
					spending.put("amount", sms_data.getSpending());

					spendings.put(spending);
				} catch (JSONException exception) {
					continue;
				}
			} while (cursor.moveToNext());
		}

		cursor.close();
		return spendings.toString();
	}

	@JavascriptInterface
	public String getSpendingTags() {
		SQLiteDatabase database = Utils.getDatabase(context);
		Cursor spendings_cursor = database.query(
			"spendings",
			new String[]{"comment"},
			null,
			null,
			null,
			null,
			null
		);

		JSONArray tags = new JSONArray();
		boolean moved = spendings_cursor.moveToFirst();
		while (moved) {
			String comment = spendings_cursor.getString(0);
			if (!comment.isEmpty()) {
				String[] comment_parts = comment.split(",");
				for (String part: comment_parts) {
					String trimmed_part = part.trim();
					if (!trimmed_part.isEmpty()) {
						tags.put(trimmed_part);
					}
				}
			}

			moved = spendings_cursor.moveToNext();
		}

		database.close();
		return tags.toString();
	}

	@JavascriptInterface
	public void createSpending(double amount, String comment) {
		ContentValues values = new ContentValues();
		values.put("timestamp", System.currentTimeMillis() / 1000L);
		values.put("amount", amount);
		values.put("comment", comment);

		SQLiteDatabase database = Utils.getDatabase(context);
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
			String formatted_timestamp = date + " " + time + ":00";
			SimpleDateFormat timestamp_format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss",
				Locale.US
			);
			Date parsed_timestamp = timestamp_format.parse(
				formatted_timestamp
			);
			long timestamp = parsed_timestamp.getTime() / 1000L;

			ContentValues values = new ContentValues();
			values.put("timestamp", timestamp);
			values.put("amount", amount);
			values.put("comment", comment);

			SQLiteDatabase database = Utils.getDatabase(context);
			database.update(
				"spendings",
				values,
				"_id = ?",
				new String[]{String.valueOf(id)}
			);
			database.close();
		} catch (java.text.ParseException exception) {}
	}

	@JavascriptInterface
	public void deleteSpending(int id) {
		SQLiteDatabase database = Utils.getDatabase(context);
		database.delete(
			"spendings",
			"_id = ?",
			new String[]{String.valueOf(id)}
		);
		database.close();
	}

	@JavascriptInterface
	public void importSms(String sms_data) {
		String sql = "";
		try {
			JSONArray spendings = new JSONArray(sms_data);
			for (int i = 0; i < spendings.length(); i++) {
				if (!sql.isEmpty()) {
					sql += ",";
				}

				JSONObject spending = spendings.getJSONObject(i);
				double amount = spending.getDouble("amount");

				String comment =
					amount >= 0.0
						? Settings.getCurrent(context).getSmsSpendingComment()
						: Settings.getCurrent(context).getSmsIncomeComment();
				String credit_card_tag =
					Settings
					.getCurrent(context)
					.getCreditCardTag();
				if (!credit_card_tag.isEmpty()) {
					comment += ", " + credit_card_tag;
				}

				sql += "("
						+ String.valueOf(spending.getLong("timestamp")) + ","
						+ String.valueOf(amount) + ","
						+ DatabaseUtils.sqlEscapeString(comment)
					+ ")";
			}
		} catch (JSONException exception) {}

		if (!sql.isEmpty()) {
			SQLiteDatabase database = Utils.getDatabase(context);
			database.execSQL(
				"INSERT INTO spendings"
				+ "(timestamp, amount, comment)"
				+ "VALUES" + sql
			);
			database.close();

			if (Settings.getCurrent(context).isSmsImportNotification()) {
				Date current_date = new Date();
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
					"SMS imported at " + notification_timestamp + ".",
					null
				);
			}
		}
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
					} catch (java.text.ParseException exception) {
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
			SQLiteDatabase database = Utils.getDatabase(context);
			database.execSQL("DELETE FROM spendings");
			database.execSQL(
				"INSERT INTO spendings"
				+ "(timestamp, amount, comment)"
				+ "VALUES" + sql
			);
			database.close();

			if (Settings.getCurrent(context).isRestoreNotification()) {
				Date current_date = new Date();
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
					"Restored at " + notification_timestamp + ".",
					null
				);
			}
		}
	}

	private static final SimpleDateFormat XML_DATE_FORMAT =
		new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss",
			Locale.US
		);

	private Context context;

	private String formatDate(long timestamp) {
		Date date = new Date(timestamp * 1000L);
		DateFormat date_format = DateFormat.getDateInstance(
			DateFormat.DEFAULT,
			Locale.US
		);
		return date_format.format(date);
	}

	private String formatTime(long timestamp) {
		Date date = new Date(timestamp * 1000L);
		DateFormat date_format = DateFormat.getTimeInstance(
			DateFormat.DEFAULT,
			Locale.US
		);
		return date_format.format(date);
	}
}
