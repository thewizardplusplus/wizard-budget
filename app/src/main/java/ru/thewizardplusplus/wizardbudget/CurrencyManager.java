package ru.thewizardplusplus.wizardbudget;

import java.text.*;
import java.util.*;

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

	private Context context;
}
