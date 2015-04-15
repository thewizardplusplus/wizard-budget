package ru.thewizardplusplus.wizardbudget;

import android.content.*;
import android.database.sqlite.*;
import android.database.*;
import org.json.*;
import android.webkit.*;

public class BuyManager {
	public BuyManager(Context context) {
		this.context = context;
	}

	@JavascriptInterface
	public String getAllBuys() {
		SQLiteDatabase database = Utils.getDatabase(context);
		Cursor buys_cursor = database.query(
			"buys",
			new String[]{"_id", "name", "cost", "status"},
			null,
			null,
			null,
			null,
			"status DESC, priority DESC"
		);

		JSONArray buys = new JSONArray();
		boolean moved = buys_cursor.moveToFirst();
		while (moved) {
			try {
				JSONObject buy = new JSONObject();
				buy.put("id", buys_cursor.getLong(0));
				buy.put("name", buys_cursor.getString(1));
				buy.put("cost", buys_cursor.getDouble(2));
				buy.put("status", buys_cursor.getLong(3));

				buys.put(buy);
			} catch (JSONException exception) {}

			moved = buys_cursor.moveToNext();
		}

		database.close();
		return buys.toString();
	}

	private Context context;
}
