package ru.thewizardplusplus.wizardbudget;

import android.content.*;
import android.database.sqlite.*;

public class DatabaseHelper extends SQLiteOpenHelper {
	public DatabaseHelper(Context context) {
		super(context, "database.db", null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(
			"CREATE TABLE spendings ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "timestamp INTEGER NOT NULL,"
				+ "amount REAL NOT NULL,"
				+ "comment TEXT NOT NULL"
			+ ");"
		);
		database.execSQL(
			"CREATE TABLE buys ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "name TEXT NOT NULL,"
				+ "cost REAL NOT NULL,"
				+ "priority INTEGER NOT NULL,"
				+ "status INTEGER NOT NULL"
			+ ");"
		);
	}

	@Override
	public void onUpgrade(
		SQLiteDatabase database,
		int old_version,
		int new_version
	) {}

	private static final int DATABASE_VERSION = 1;
}
