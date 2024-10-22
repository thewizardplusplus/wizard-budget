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
				"timestamp DESC, _id"
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
					+ "ORDER BY timestamp DESC, _id;",
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
					+ "ORDER BY timestamp DESC, _id;",
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

	public List<String> getAllCurrenciesForWidget() {
		SQLiteDatabase database = Utils.getDatabase(context);
		Cursor currencies_cursor = database.rawQuery(
				"SELECT currencies.* "
					+ "FROM currencies "
					+ "JOIN ("
						+ "SELECT code, max(timestamp) AS 'max_timestamp' "
						+ "FROM currencies "
						+ "GROUP BY code"
					+ ") sub_query "
					+ "ON currencies.code = sub_query.code AND currencies.timestamp = sub_query.max_timestamp "
					+ "ORDER BY timestamp DESC, _id;",
				null
			);

		List<String> currencies = new ArrayList<String>();
		boolean moved = currencies_cursor.moveToFirst();
		while (moved) {
			String code = currencies_cursor.getString(3);
			double rate = currencies_cursor.getDouble(4);
			double inverted_rate = 1 / rate;

			String currency = String.format("%1$s: ", code);
			if (rate >= inverted_rate) {
				currency += String.format(
					"1 RUB = %2$.2f %1$s",
					code,
					rate
				);
			} else {
				currency += String.format(
					"1 %1$s = %2$.2f RUB",
					code,
					inverted_rate
				);
			}

			// the principle of selecting and displaying available currencies:
			// - selection:
			//   - support all currencies whose symbols are included in the Font Awesome library version 4.2.0
			//   - if one symbol corresponds to several currencies, support those currencies that are included in the list of "Most traded currencies" for 2022
			// - displaying:
			//   - if a unique symbol is found for a currency, use it
			//   - if a special prefix is available for a currency with a non-unique symbol (see the "World Bank Editorial Style Guide 2020" document, appendix D), use it
			//   - otherwise use the ISO 4217 code as the prefix
			//   - for all other currencies for which no symbol is found, just use the ISO 4217 code
			// see: https://fontawesome.com/v4/icons/#currency-icons
			// see: https://en.wikipedia.org/wiki/Template:Most_traded_currencies
			// see: https://openknowledge.worldbank.org/bitstream/handle/10986/33367/33304.pdf
			currency = currency
				// dollar
				.replace("AUD", "$A")
				.replace("CAD", "Can$")
				.replace("HKD", "HK$")
				.replace("NZD", "$NZ")
				.replace("SGD", "S$")
				.replace("TWD", "NT$")
				.replace("USD", "US$")
				// peso
				.replace("ARS", "Arg$")
				.replace("CLP", "Ch$")
				.replace("COP", "Col$")
				.replace("MXN", "Mex$")
				// others
				.replace("BRL", "R$")
				.replace("CNY", "RMB\u00a5")
				.replace("EUR", "\u20ac")
				.replace("GBP", "\u00a3")
				.replace("ILS", "\u20aa")
				.replace("INR", "\u20b9")
				.replace("JPY", "\u00a5")
				.replace("KRW", "\u20a9")
				.replace("RUB", "\u20bd")
				.replace("TRY", "\u20ba")
				.replace("KZT", "\u20b8");

			currencies.add(currency);

			moved = currencies_cursor.moveToNext();
		}

		database.close();
		return currencies;
	}

	@JavascriptInterface
	public void createCurrency(long timestamp, String code, double rate) {
		ContentValues values = new ContentValues();
		values.put("timestamp", timestamp);
		values.put("date", formatDate(timestamp));
		values.put("code", code);
		values.put("rate", rate);

		SQLiteDatabase database = Utils.getDatabase(context);
		database.insertWithOnConflict(
			"currencies",
			null,
			values,
			SQLiteDatabase.CONFLICT_REPLACE
		);
		database.close();
	}

	public void updateCurrencies() {
		try {
			String api_key = Settings.getCurrent(context).getExchangeRateApiKey();
			if (api_key.isEmpty()) {
				return;
			}

			String url = "https://v6.exchangerate-api.com/v6/" + api_key + "/latest/" + BASE_CURRENCY;
			final CurrencyManager self = this;
			final String[] currencies = Settings.getCurrent(context).getUsedCurrencies().split(",");
			HttpRequestTask task = new HttpRequestTask(
				new HashMap<String, String>(),
				new HttpRequestTask.OnSuccessListener() {
					@Override
					public void onSuccess(String data) {
						try {
							JSONObject parsed_data = new JSONObject(data);
							if (!parsed_data.getString("result").equals("success")) {
								return;
							}

							long timestamp = parsed_data.getLong("time_last_update_unix");
							JSONObject conversion_rates = parsed_data.getJSONObject("conversion_rates");
							for (int index = 0; index < currencies.length; index++) {
								String currency = currencies[index];
								if (!conversion_rates.has(currency)) {
									continue;
								}

								double rate = conversion_rates.getDouble(currency);
								self.createCurrency(timestamp, currency, rate);
							}

							Utils.updateCurrencyWidget(context);
							self.showNotification(timestamp);
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
