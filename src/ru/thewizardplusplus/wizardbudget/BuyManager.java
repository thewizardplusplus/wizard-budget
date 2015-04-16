package ru.thewizardplusplus.wizardbudget;

import android.content.*;
import android.database.sqlite.*;
import android.database.*;
import org.json.*;
import android.webkit.*;
import android.renderscript.*;

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

	@JavascriptInterface
	public void createBuy(String name, double cost) {
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("cost", cost);
		values.put("status", 1L);

		SQLiteDatabase database = Utils.getDatabase(context);
		long maximal_priority = getMaximalPriority(database);
		values.put("priority", maximal_priority + 1);

		database.insert("buys", null, values);
		database.close();
	}

	@JavascriptInterface
	public void deleteBuy(int id) {
		SQLiteDatabase database = Utils.getDatabase(context);
		database.delete("buys", "_id = ?", new String[]{String.valueOf(id)});
		database.close();
	}

	private long getMaximalPriority(SQLiteDatabase database) {
		Cursor cursor = database.query(
			"buys",
			new String[]{"MAX(priority)"},
			null,
			null,
			null,
			null,
			null
		);

		long maximal_priority = 0L;
		boolean moved = cursor.moveToFirst();
		if (moved) {
			maximal_priority = cursor.getLong(0);
		}

		return maximal_priority;
	}

	private Context context;
}
