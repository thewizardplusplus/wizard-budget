package ru.thewizardplusplus.wizardbudget;

import java.text.*;
import java.util.*;

import org.json.*;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.net.*;
import android.webkit.*;

public class SpendingManager {
	public SpendingManager(Context context) {
		this.context = context;
	}

	public double calculateSpendingsSum() {
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
		return spendings_sum;
	}

	@JavascriptInterface
	public String getSpendingsSum() {
		double spendings_sum = calculateSpendingsSum();
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
			"timestamp DESC, _id DESC"
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
			"date DESC"
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
					spending.put("residue", sms_data.getResidue());

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
		JSONArray tags = new JSONArray();
		List<String> tag_list = getTagList();
		for (String tag: tag_list) {
			tags.put(tag);
		}

		return tags.toString();
	}

	@JavascriptInterface
	public String getPrioritiesTags() {
		Map<String, Long> tag_map = new HashMap<String, Long>();
		List<String> tag_list = getTagList();
		for (String tag: tag_list) {
			if (tag_map.containsKey(tag)) {
				tag_map.put(tag, tag_map.get(tag) + 1L);
			} else {
				tag_map.put(tag, 1L);
			}
		}

		JSONObject serialized_tag_map = new JSONObject();
		try {
			for (Map.Entry<String, Long> entry: tag_map.entrySet()) {
				serialized_tag_map.put(entry.getKey(), entry.getValue());
			}
		} catch (JSONException exception) {}

		return serialized_tag_map.toString();
	}

	@JavascriptInterface
	public String getStatsSum(int number_of_last_days, String prefix) {
		SQLiteDatabase database = Utils.getDatabase(context);
		String prefix_length = String.valueOf(prefix.length());
		Cursor spendings_cursor = database.query(
			"spendings",
			new String[]{"ROUND(SUM(amount), 2)"},
			"amount > 0 "
				+ "AND date(timestamp, 'unixepoch')"
					+ ">= date("
						+ "'now',"
						+ "'-"
							+ String.valueOf(Math.abs(number_of_last_days))
							+ " days'"
					+ ")"
				+ "AND comment LIKE "
					+ DatabaseUtils.sqlEscapeString(prefix + "%")
				+ "AND ("
					+ prefix_length + " == 0 "
					+ "OR length(comment) == " + prefix_length + " "
					+ "OR substr(comment, " + prefix_length + " + 1, 1) == ','"
				+ ")",
			null,
			null,
			null,
			null
		);

		double spendings_sum = 0.0;
		boolean moved = spendings_cursor.moveToFirst();
		if (moved) {
			spendings_sum = spendings_cursor.getDouble(0);
		}

		return String.valueOf(spendings_sum);
	}

	@JavascriptInterface
	public String getStats(int number_of_last_days, String prefix) {
		SQLiteDatabase database = Utils.getDatabase(context);
		int prefix_length = prefix.length();
		String prefix_length_in_string = String.valueOf(prefix_length);
		Cursor spendings_cursor = database.query(
			"spendings",
			new String[]{"comment", "amount"},
			"amount > 0 "
				+ "AND date(timestamp, 'unixepoch')"
					+ ">= date("
						+ "'now',"
						+ "'-"
							+ String.valueOf(Math.abs(number_of_last_days))
							+ " days'"
					+ ")"
				+ "AND comment LIKE "
					+ DatabaseUtils.sqlEscapeString(prefix + "%")
				+ "AND ("
					+ prefix_length_in_string + " == 0 "
					+ "OR length(comment) == " + prefix_length_in_string + " "
					+ "OR substr("
						+ "comment,"
						+ prefix_length_in_string + " + 1,"
						+ "1"
					+ ") == ','"
				+ ")",
			null,
			null,
			null,
			null
		);

		Map<String, Double> spendings = new HashMap<String, Double>();
		boolean moved = spendings_cursor.moveToFirst();
		while (moved) {
			String comment = spendings_cursor.getString(0);
			if (prefix_length != 0) {
				if (comment.length() > prefix_length) {
					comment = comment.substring(prefix_length + 1).trim();
				} else if (comment.length() == prefix_length) {
					comment = "";
				}
			}
			if (!comment.isEmpty()) {
				int separator_index = comment.indexOf(",");
				if (separator_index != -1) {
					comment = comment.substring(0, separator_index).trim();
				}
			}

			double amount = spendings_cursor.getDouble(1);
			if (spendings.containsKey(comment)) {
				spendings.put(comment, spendings.get(comment) + amount);
			} else {
				spendings.put(comment, amount);
			}

			moved = spendings_cursor.moveToNext();
		}

		JSONArray serialized_spendings = new JSONArray();
		String empty_comment_replacement = UUID.randomUUID().toString();
		for (Map.Entry<String, Double> entry: spendings.entrySet()) {
			try {
				JSONObject spending = new JSONObject();
				String comment = entry.getKey();
				if (comment.isEmpty()) {
					comment = empty_comment_replacement;
					spending.put("is_rest", true);
				} else {
					spending.put("is_rest", false);
				}
				spending.put("tag", comment);

				double amount = entry.getValue();
				spending.put("sum", amount);

				serialized_spendings.put(spending);
			} catch (JSONException exception) {}
		}

		database.close();
		return serialized_spendings.toString();
	}

	@JavascriptInterface
	public void createSpending(double amount, String comment) {
		ContentValues values = new ContentValues();
		long current_timestamp = resetSeconds(
			System.currentTimeMillis()
			/ 1000L
		);
		values.put("timestamp", current_timestamp);
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
		double residue = 0.0;
		boolean residue_found_tryed = false;
		String credit_card_tag = Settings.getCurrent(context).getCreditCardTag();
		try {
			JSONArray spendings = new JSONArray(sms_data);
			for (int i = 0; i < spendings.length(); i++) {
				if (!sql.isEmpty()) {
					sql += ",";
				}

				JSONObject spending = spendings.getJSONObject(i);
				long timestamp = resetSeconds(spending.getLong("timestamp"));
				double amount = spending.getDouble("amount");

				if (!residue_found_tryed) {
					residue = spending.getDouble("residue");
					residue_found_tryed = true;
				}

				String comment =
					amount >= 0.0
						? Settings.getCurrent(context).getSmsSpendingComment()
						: Settings.getCurrent(context).getSmsIncomeComment();
				if (!comment.isEmpty() && !credit_card_tag.isEmpty()) {
					comment += ", ";
				}
				comment += credit_card_tag;

				sql += "("
						+ String.valueOf(timestamp) + ","
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

			if (residue != 0.0) {
				Date current_date = new Date();
				long timestamp = resetSeconds(current_date.getTime() / 1000L);

				double spendings_sum = calculateSpendingsSum();
				double correction = -1 * residue - spendings_sum;

				String comment = "";
				if (correction < 0) {
					comment = Settings.getCurrent(context).getSmsPositiveCorrectionComment();
				} else {
					comment = Settings.getCurrent(context).getSmsNegativeCorrectionComment();
				}
				if (!comment.isEmpty() && !credit_card_tag.isEmpty()) {
					comment += ", ";
				}
				comment += credit_card_tag;

				database.execSQL(
					"INSERT INTO spendings"
					+ "(timestamp, amount, comment)"
					+ "VALUES ("
						+ String.valueOf(timestamp) + ","
						+ String.valueOf(correction) + ","
						+ DatabaseUtils.sqlEscapeString(comment)
					+ ")"
				);
			}
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

	private List<String> getTagList() {
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

		List<String> tags = new ArrayList<String>();
		boolean moved = spendings_cursor.moveToFirst();
		while (moved) {
			String comment = spendings_cursor.getString(0);
			if (!comment.isEmpty()) {
				String[] comment_parts = comment.split(",");
				for (String part: comment_parts) {
					String trimmed_part = part.trim();
					if (!trimmed_part.isEmpty()) {
						tags.add(trimmed_part);
					}
				}
			}

			moved = spendings_cursor.moveToNext();
		}

		database.close();
		return tags;
	}

	long resetSeconds(long timestamp) {
		return timestamp / 60 * 60;
	}
}
