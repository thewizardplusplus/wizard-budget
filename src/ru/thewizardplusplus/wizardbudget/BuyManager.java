package ru.thewizardplusplus.wizardbudget;

import java.util.*;

import org.json.*;

import android.content.*;
import android.database.sqlite.*;
import android.database.*;
import android.webkit.*;

public class BuyManager {
	public BuyManager(Context context) {
		this.context = context;
	}

	@JavascriptInterface
	public String getCostsSum() {
		SQLiteDatabase database = Utils.getDatabase(context);
		Cursor cursor = database.query(
			"buys",
			new String[]{"ROUND(SUM(cost), 2)"},
			"status = 0",
			null,
			null,
			null,
			null
		);

		double costs_sum = 0.0;
		boolean moved = cursor.moveToFirst();
		if (moved) {
			costs_sum = cursor.getDouble(0);
		}

		database.close();
		return String.valueOf(costs_sum);
	}

	@JavascriptInterface
	public String getAllBuys() {
		SQLiteDatabase database = Utils.getDatabase(context);
		Cursor buys_cursor = database.query(
			"buys",
			new String[]{"_id", "name", "cost", "status", "monthly"},
			null,
			null,
			null,
			null,
			"status, monthly DESC, priority DESC"
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
				buy.put("monthly", buys_cursor.getLong(4));

				buys.put(buy);
			} catch (JSONException exception) {}

			moved = buys_cursor.moveToNext();
		}

		database.close();
		return buys.toString();
	}

	@JavascriptInterface
	public String getBuyNames() {
		SQLiteDatabase database = Utils.getDatabase(context);
		Cursor buys_cursor = database.query(
			"buys",
			new String[]{"name"},
			"status = 0",
			null,
			null,
			null,
			null
		);

		JSONArray names = new JSONArray();
		boolean moved = buys_cursor.moveToFirst();
		while (moved) {
			String name = buys_cursor.getString(0);
			name = name.trim();
			if (!name.isEmpty()) {
				names.put(name);
			}

			moved = buys_cursor.moveToNext();
		}

		database.close();
		return names.toString();
	}

	public List<String> getBuyNamesForWidget(boolean only_monthly) {
		SQLiteDatabase database = Utils.getDatabase(context);
		Cursor buys_cursor = database.query(
			"buys",
			new String[]{"name"},
			"status = 0"
				+ (only_monthly
					? " AND monthly = 1"
					: ""),
			null,
			null,
			null,
			null
		);

		List<String> names = new ArrayList<String>();
		boolean moved = buys_cursor.moveToFirst();
		while (moved) {
			String name = buys_cursor.getString(0);
			name = name.trim();
			if (!name.isEmpty()) {
				names.add(name);
			}

			moved = buys_cursor.moveToNext();
		}

		database.close();
		return names;
	}

	@JavascriptInterface
	public void createBuy(String name, double cost, long monthly) {
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("cost", cost);
		values.put("status", 0L);
		values.put("monthly", monthly);

		SQLiteDatabase database = Utils.getDatabase(context);
		long maximal_priority = getMaximalPriority(database);
		values.put("priority", maximal_priority + 1);

		database.insert("buys", null, values);
		database.close();
	}

	@JavascriptInterface
	public void updateBuy(
		int id,
		String name,
		double cost,
		long status,
		long monthly
	) {
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("cost", cost);
		values.put("status", status);
		values.put("monthly", monthly);

		SQLiteDatabase database = Utils.getDatabase(context);
		database.update(
			"buys",
			values,
			"_id = ?",
			new String[]{String.valueOf(id)}
		);
		database.close();
	}

	@JavascriptInterface
	public void updateBuyOrder(String id_list) {
		SQLiteDatabase database = Utils.getDatabase(context);
		try {
			JSONArray deserialized_id_list = new JSONArray(id_list);
			long priority = deserialized_id_list.length();
			for (int i = 0; i < priority; i++) {
				ContentValues values = new ContentValues();
				values.put("priority", priority - i);

				Long id = deserialized_id_list.getLong(i);
				database.update(
					"buys",
					values,
					"_id = ?",
					new String[]{String.valueOf(id)}
				);
			}
		} catch (JSONException exception) {
			return;
		}

		database.close();
	}

	public void resetMonthlyBuy() {
		ContentValues values = new ContentValues();
		values.put("status", 0L);

		SQLiteDatabase database = Utils.getDatabase(context);
		database.update(
			"buys",
			values,
			"monthly = 1",
			null
		);
		database.close();
	}

	@JavascriptInterface
	public void deleteBuy(int id) {
		SQLiteDatabase database = Utils.getDatabase(context);
		database.delete("buys", "_id = ?", new String[]{String.valueOf(id)});
		database.close();
	}

	@JavascriptInterface
	public void mayBeBuy(String spending_tags) {
		StringBuilder prepared_tags = new StringBuilder();
		try {
			JSONArray deserialized_tags = new JSONArray(spending_tags);
			for (int i = 0; i < deserialized_tags.length(); i++) {
				String tag = deserialized_tags.getString(i);
				tag = DatabaseUtils.sqlEscapeString(tag);

				if (i != 0) {
					prepared_tags.append(", ");
				}
				prepared_tags.append(tag);
			}
		} catch (JSONException exception) {
			return;
		}

		ContentValues values = new ContentValues();
		values.put("status", 1L);

		SQLiteDatabase database = Utils.getDatabase(context);
		database.update(
			"buys",
			values,
			"name IN (" + prepared_tags.toString() + ")",
			null
		);
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
