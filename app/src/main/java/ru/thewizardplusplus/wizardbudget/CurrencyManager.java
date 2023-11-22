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
	public String getAllCurrencies() {
		SQLiteDatabase database = Utils.getDatabase(context);
		Cursor currencies_cursor = database.query(
			"currencies",
			new String[]{"_id", "timestamp", "code", "rate"},
			null,
			null,
			null,
			null,
			"timestamp DESC, _id DESC"
		);

		JSONArray currencies = new JSONArray();
		boolean moved = currencies_cursor.moveToFirst();
		while (moved) {
			try {
				JSONObject currency = new JSONObject();
				currency.put("id", currencies_cursor.getDouble(0));

				long timestamp = currencies_cursor.getLong(1);
				currency.put("timestamp", String.valueOf(timestamp));
				currency.put("code", currencies_cursor.getString(2));
				currency.put("rate", currencies_cursor.getDouble(3));

				currencies.put(currency);
			} catch (JSONException exception) {}

			moved = currencies_cursor.moveToNext();
		}

		database.close();
		return currencies.toString();
	}

	private Context context;
}
