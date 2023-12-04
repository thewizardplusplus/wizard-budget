package ru.thewizardplusplus.wizardbudget;

import java.text.*;
import java.util.*;
import java.net.*;

import org.json.*;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.net.*;
import android.webkit.*;
import android.util.*;

public class CurrencyManager {
	public CurrencyManager(Context context) {
		this.context = context;
	}

	@JavascriptInterface
	public String getAllCurrencies(String mode) {
		SQLiteDatabase database = Utils.getDatabase(context);
		Cursor currencies_cursor = null;
		switch (mode) {
		case "all":
			currencies_cursor = database.query(
				"currencies",
				new String[]{"_id", "timestamp", "date", "code", "rate"},
				null,
				null,
				null,
				null,
				"timestamp DESC, _id DESC"
			);

			break;
		case "last-by-day":
			currencies_cursor = database.rawQuery(
				"SELECT currencies.* "
					+ "FROM currencies "
					+ "JOIN ("
						+ "SELECT date, code, max(timestamp) AS 'max_timestamp' "
						+ "FROM currencies "
						+ "GROUP BY date, code"
					+ ") sub_query "
					+ "ON currencies.code = sub_query.code AND currencies.timestamp = sub_query.max_timestamp "
					+ "ORDER BY timestamp DESC, _id DESC;",
				null
			);

			break;
		case "last-at-all":
			currencies_cursor = database.rawQuery(
				"SELECT currencies.* "
					+ "FROM currencies "
					+ "JOIN ("
						+ "SELECT code, max(timestamp) AS 'max_timestamp' "
						+ "FROM currencies "
						+ "GROUP BY code"
					+ ") sub_query "
					+ "ON currencies.code = sub_query.code AND currencies.timestamp = sub_query.max_timestamp "
					+ "ORDER BY timestamp DESC, _id DESC;",
				null
			);

			break;
		default:
			throw new IllegalArgumentException("unknown mode");
		}

		JSONArray currencies = new JSONArray();
		boolean moved = currencies_cursor.moveToFirst();
		while (moved) {
			try {
				JSONObject currency = new JSONObject();
				currency.put("id", currencies_cursor.getDouble(0));

				long timestamp = currencies_cursor.getLong(1);
				currency.put("timestamp", String.valueOf(timestamp));
				currency.put("date", currencies_cursor.getString(2));
				currency.put("code", currencies_cursor.getString(3));
				currency.put("rate", currencies_cursor.getDouble(4));

				currencies.put(currency);
			} catch (JSONException exception) {}

			moved = currencies_cursor.moveToNext();
		}

		database.close();
		return currencies.toString();
	}

	@JavascriptInterface
	public void createCurrency(long timestamp, String code, double rate) {
		ContentValues values = new ContentValues();
		values.put("timestamp", timestamp);
		values.put("date", formatDate(timestamp));
		values.put("code", code);
		values.put("rate", rate);

		SQLiteDatabase database = Utils.getDatabase(context);
		database.insert("currencies", null, values);
		database.close();
	}

	public void updateCurrencies() {
		Log.d("DEBUG", "CurrencyManager.updateCurrencies()");

		try {
			String api_key = Settings.getCurrent(context).getExchangeRateApiKey();
			if (api_key.isEmpty()) {
				return;
			}

			String url = "https://v6.exchangerate-api.com/v6/" + api_key + "/latest/" + BASE_CURRENCY;
			HttpRequestTask task = new HttpRequestTask(
				new HashMap<String, String>(),
				new HttpRequestTask.OnSuccessListener() {
					@Override
					public void onSuccess(String data) {
						Log.d("DEBUG", data);

						try {
							JSONObject parsed_data = new JSONObject(data);
							if (!parsed_data.getString("result").equals("success")) {
								return;
							}

							long timestamp = parsed_data.getLong("time_last_update_unix");
							JSONObject conversion_rates = parsed_data.getJSONObject("conversion_rates");
							String[] currencies = {"USD", "EUR", "KZT"};
							for (int index = 0; index < currencies.length; index++) {
								String currency = currencies[index];
								double rate = conversion_rates.getDouble(currency);

								Log.d("DEBUG", String.valueOf(timestamp) + " " + currency + " " + String.valueOf(rate));
							}
						} catch(JSONException exception) {}
					}
				}
			);
			task.execute(new URL(url));
		} catch(MalformedURLException exception) {}
	}

	@JavascriptInterface
	public void showNotification(long timestamp) {
		if (!Settings.getCurrent(context).isCurrencyUpdateNotification()) {
			return;
		}

		Date date = new Date(timestamp * 1000L);
		DateFormat notification_timestamp_format = DateFormat.getDateTimeInstance(
			DateFormat.DEFAULT,
			DateFormat.DEFAULT,
			Locale.US
		);
		String notification_timestamp = notification_timestamp_format.format(date);
		Utils.showNotification(
			context,
			context.getString(R.string.app_name),
			"Updated the currencies at " + notification_timestamp + ".",
			null
		);
	}

	private static final String BASE_CURRENCY = "RUB";

	private Context context;

	private String formatDate(long timestamp) {
		Date date = new Date(timestamp * 1000L);
		SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
		return date_format.format(date);
	}
}
