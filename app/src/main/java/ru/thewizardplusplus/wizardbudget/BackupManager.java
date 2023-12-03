package ru.thewizardplusplus.wizardbudget;

import java.io.*;
import java.util.*;
import java.text.*;

import javax.xml.parsers.*;

import org.xmlpull.v1.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import android.content.*;
import android.webkit.*;
import android.database.sqlite.*;
import android.database.*;
import android.os.*;
import android.util.*;

public class BackupManager {
	public BackupManager(Context context) {
		this.context = context;
	}

	@JavascriptInterface
	public String backup() {
		String filename = "";
		try {
			File directory = new File(
				Environment.getExternalStorageDirectory(),
				BACKUPS_DIRECTORY
			);
			directory.mkdirs();

			Date current_date = new Date();
			SimpleDateFormat file_suffix_format = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss",
				Locale.US
			);
			String file_suffix = file_suffix_format.format(current_date);

			File file = new File(
				directory,
				"database_dump_" + file_suffix + ".xml"
			);
			FileWriter writter = new FileWriter(file);
			try {
				XmlSerializer serializer = Xml.newSerializer();
				serializer.setOutput(writter);
				serializer.setFeature(
					"http://xmlpull.org/v1/doc/features.html#indent-output",
					true
				);
				/*serializer.setProperty(
				 "http://xmlpull.org/v1/doc/properties.html"
				 + "#serializer-indentation",
				 "\t"
				 );*/
				serializer.startDocument("utf-8", true);
				serializer.startTag("", "budget");
				serializer.attribute(
					"",
					"version",
					String.valueOf(BACKUP_VERSION)
				);

				Settings settings = Settings.getCurrent(context);
				serializer.startTag("", "preferences");
				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_STATS_RANGE
				);
				serializer.attribute(
					"",
					"value",
					String.valueOf(settings.getStatsRange())
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_STATS_TAGS
				);
				serializer.attribute("", "value", settings.getStatsTags());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_HOURS_START_DATE
				);
				serializer.attribute("", "value", settings.getHoursStartDate());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_HOURS_END_DATE
				);
				serializer.attribute("", "value", settings.getHoursEndDate());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_CREDIT_CARD_TAG
				);
				serializer.attribute("", "value", settings.getCreditCardTag());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_COLLECT_STATS
				);
				serializer.attribute(
					"",
					"value",
					settings.isCollectStats() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_ONLY_MONTHLY
				);
				serializer.attribute(
					"",
					"value",
					settings.isOnlyMonthly() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_CURRENCY_LIST_MODE
				);
				serializer.attribute("", "value", settings.getCurrencyListMode());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_DAILY_AUTOBACKUP
				);
				serializer.attribute(
					"",
					"value",
					settings.isDailyAutobackup() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_PARSE_SMS
				);
				serializer.attribute(
					"",
					"value",
					settings.isParseSms() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_SMS_NUMBER_PATTERN
				);
				serializer.attribute(
					"",
					"value",
					settings.getSmsNumberPatternString()
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_SMS_SPENDING_PATTERN
				);
				serializer.attribute(
					"",
					"value",
					settings.getSmsSpendingPatternString()
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_SMS_SPENDING_COMMENT
				);
				serializer.attribute(
					"",
					"value",
					settings.getSmsSpendingComment()
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_SMS_INCOME_PATTERN
				);
				serializer.attribute(
					"",
					"value",
					settings.getSmsIncomePatternString()
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_SMS_INCOME_COMMENT
				);
				serializer.attribute(
					"",
					"value",
					settings.getSmsIncomeComment()
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_SMS_RESIDUE_PATTERN
				);
				serializer.attribute(
					"",
					"value",
					settings.getSmsResiduePatternString()
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_SMS_NEGATIVE_CORRECTION_COMMENT
				);
				serializer.attribute(
					"",
					"value",
					settings.getSmsNegativeCorrectionComment()
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_SMS_POSITIVE_CORRECTION_COMMENT
				);
				serializer.attribute(
					"",
					"value",
					settings.getSmsPositiveCorrectionComment()
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_SAVE_BACKUP_TO_DROPBOX
				);
				serializer.attribute(
					"",
					"value",
					settings.isSaveBackupToDropbox() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_ANALYSIS_HARVEST
				);
				serializer.attribute(
					"",
					"value",
					settings.isAnalysisHarvest() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_HARVEST_USERNAME
				);
				serializer.attribute(
					"",
					"value",
					settings.getHarvestUsername()
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_HARVEST_SUBDOMAIN
				);
				serializer.attribute(
					"",
					"value",
					settings.getHarvestSubdomain()
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_WORKING_OFF_LIMIT
				);
				serializer.attribute(
					"",
					"value",
					String.valueOf(settings.getWorkingOffLimit())
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_BACKUP_NOTIFICATION
				);
				serializer.attribute(
					"",
					"value",
					settings.isBackupNotification() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_RESTORE_NOTIFICATION
				);
				serializer.attribute(
					"",
					"value",
					settings.isRestoreNotification() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_SMS_PARSING_NOTIFICATION
				);
				serializer.attribute(
					"",
					"value",
					settings.isSmsParsingNotification() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_SMS_IMPORT_NOTIFICATION
				);
				serializer.attribute(
					"",
					"value",
					settings.isSmsImportNotification() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_DROPBOX_NOTIFICATION
				);
				serializer.attribute(
					"",
					"value",
					settings.isDropboxNotification() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_MONTHLY_RESET_NOTIFICATION
				);
				serializer.attribute(
					"",
					"value",
					settings.isMonthlyResetNotification() ? "true" : "false"
				);
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute(
					"",
					"name",
					Settings.SETTING_NAME_CURRENCY_UPDATE_NOTIFICATION
				);
				serializer.attribute(
					"",
					"value",
					settings.isCurrencyUpdateNotification() ? "true" : "false"
				);
				serializer.endTag("", "preference");
				serializer.endTag("", "preferences");

				SQLiteDatabase database = Utils.getDatabase(context);
				serializer.startTag("", "spendings");
				Cursor cursor = database.query(
					"spendings",
					new String[]{"timestamp", "amount", "comment"},
					null,
					null,
					null,
					null,
					"timestamp"
				);

				boolean moved = cursor.moveToFirst();
				while (moved) {
					serializer.startTag("", "spending");

					Date date = new Date(cursor.getLong(0) * 1000L);
					String formatted_date = XML_DATE_FORMAT.format(date);
					serializer.attribute("", "date", formatted_date);

					serializer.attribute(
						"",
						"amount",
						String.valueOf(cursor.getDouble(1))
					);
					serializer.attribute("", "comment", cursor.getString(2));
					serializer.endTag("", "spending");

					moved = cursor.moveToNext();
				}
				serializer.endTag("", "spendings");

				serializer.startTag("", "buys");
				cursor = database.query(
					"buys",
					new String[]{
						"name",
						"cost",
						"priority",
						"status",
						"monthly"
					},
					null,
					null,
					null,
					null,
					"status, monthly DESC, priority DESC"
				);

				moved = cursor.moveToFirst();
				while (moved) {
					serializer.startTag("", "buy");
					serializer.attribute("", "name", cursor.getString(0));
					serializer.attribute(
						"",
						"cost",
						String.valueOf(cursor.getDouble(1))
					);
					serializer.attribute(
						"",
						"priority",
						String.valueOf(cursor.getLong(2))
					);

					long status = cursor.getLong(3);
					serializer.attribute(
						"",
						"purchased",
						status == 0 ? "false" : "true"
					);

					long monthly = cursor.getLong(4);
					serializer.attribute(
						"",
						"monthly",
						monthly == 0 ? "false" : "true"
					);

					serializer.endTag("", "buy");

					moved = cursor.moveToNext();
				}
				serializer.endTag("", "buys");

				serializer.startTag("", "currencies");
				cursor = database.query(
					"currencies",
					new String[]{"timestamp", "code", "rate"},
					null,
					null,
					null,
					null,
					"timestamp"
				);

				moved = cursor.moveToFirst();
				while (moved) {
					serializer.startTag("", "currency");

					Date date = new Date(cursor.getLong(0) * 1000L);
					String formatted_date = XML_DATE_FORMAT.format(date);
					serializer.attribute("", "date", formatted_date);

					serializer.attribute("", "code", cursor.getString(1));
					serializer.attribute("", "rate", String.valueOf(cursor.getDouble(2)));
					serializer.endTag("", "currency");

					moved = cursor.moveToNext();
				}
				serializer.endTag("", "currencies");
				database.close();

				serializer.endTag("", "budget");
				serializer.endDocument();

				if (Settings.getCurrent(context).isBackupNotification()) {
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
						"Backuped at " + notification_timestamp + ".",
						file
					);
				}

				filename = file.getAbsolutePath();
			} finally {
				writter.close();
			}
		} catch (IOException exception) {}

		return filename;
	}

	public void restore(InputStream in) {
		String spending_sql = "";
		String buy_sql = "";
		String currency_sql = "";
		try {
			Element budget = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(in)
				.getDocumentElement();
			budget.normalize();

			Settings settings = Settings.getCurrent(context);
			NodeList preference_list = budget.getElementsByTagName(
				"preference"
			);
			for (int i = 0; i < preference_list.getLength(); i++) {
				if (
					preference_list.item(i).getNodeType() == Node.ELEMENT_NODE
				) {
					Element preference = (Element)preference_list.item(i);
					if (
						!preference.hasAttribute("name")
						|| !preference.hasAttribute("value")
					) {
						continue;
					}

					String name = preference.getAttribute("name");
					String value = preference.getAttribute("value");
					boolean boolean_value = value.equals("true");
					if (name.equals(Settings.SETTING_NAME_STATS_RANGE)) {
						settings.setStatsRange(Long.valueOf(value));
					} else if (name.equals(Settings.SETTING_NAME_STATS_TAGS)) {
						settings.setStatsTags(value);
					} else if (
						name.equals(Settings.SETTING_NAME_HOURS_START_DATE)
					) {
						settings.setHoursStartDate(value);
					} else if (
						name.equals(Settings.SETTING_NAME_HOURS_END_DATE)
					) {
						settings.setHoursEndDate(value);
					} else if (
						name.equals(Settings.SETTING_NAME_CREDIT_CARD_TAG)
					) {
						settings.setCreditCardTag(value);
					} else if (
						name.equals(Settings.SETTING_NAME_COLLECT_STATS)
					) {
						settings.setCollectStats(boolean_value);
					} else if (
						name.equals(Settings.SETTING_NAME_ONLY_MONTHLY)
					) {
						settings.setOnlyMonthly(boolean_value);
					} else if (
						name.equals(Settings.SETTING_NAME_CURRENCY_LIST_MODE)
					) {
						settings.setCurrencyListMode(value);
					} else if (
						name.equals(Settings.SETTING_NAME_DAILY_AUTOBACKUP)
					) {
						settings.setDailyAutobackup(boolean_value);
					} else if (name.equals(Settings.SETTING_NAME_PARSE_SMS)) {
						settings.setParseSms(boolean_value);
					} else if (
						name.equals(Settings.SETTING_NAME_SMS_NUMBER_PATTERN)
					) {
						settings.setSmsNumberPattern(value);
					} else if (
						name.equals(Settings.SETTING_NAME_SMS_SPENDING_PATTERN)
					) {
						settings.setSmsSpendingPattern(value);
					} else if (
						name.equals(Settings.SETTING_NAME_SMS_SPENDING_COMMENT)
					) {
						settings.setSmsSpendingComment(value);
					} else if (
						name.equals(Settings.SETTING_NAME_SMS_INCOME_PATTERN)
					) {
						settings.setSmsIncomePattern(value);
					} else if (
						name.equals(Settings.SETTING_NAME_SMS_INCOME_COMMENT)
					) {
						settings.setSmsIncomeComment(value);
					} else if (
						name.equals(Settings.SETTING_NAME_SMS_RESIDUE_PATTERN)
					) {
						settings.setSmsResiduePattern(value);
					} else if (
						name.equals(
							Settings
							.SETTING_NAME_SMS_NEGATIVE_CORRECTION_COMMENT
						)
					) {
						settings.setSmsNegativeCorrectionComment(value);
					} else if (
						name.equals(
							Settings
							.SETTING_NAME_SMS_POSITIVE_CORRECTION_COMMENT
						)
					) {
						settings.setSmsPositiveCorrectionComment(value);
					} else if (
						name.equals(
							Settings.SETTING_NAME_SAVE_BACKUP_TO_DROPBOX
						)
					) {
						settings.setSaveBackupToDropbox(boolean_value);
					} else if (
						name.equals(Settings.SETTING_NAME_ANALYSIS_HARVEST)
					) {
						settings.setAnalysisHarvest(boolean_value);
					} else if (
						name.equals(Settings.SETTING_NAME_HARVEST_USERNAME)
					) {
						settings.setHarvestUsername(value);
					} else if (
						name.equals(Settings.SETTING_NAME_HARVEST_SUBDOMAIN)
					) {
						settings.setHarvestSubdomain(value);
					} else if (
						name.equals(Settings.SETTING_NAME_WORKING_OFF_LIMIT)
					) {
						settings.setWorkingOffLimit(Double.valueOf(value));
					} else if (
						name.equals(Settings.SETTING_NAME_BACKUP_NOTIFICATION)
					) {
						settings.setBackupNotification(boolean_value);
					} else if (
						name.equals(Settings.SETTING_NAME_RESTORE_NOTIFICATION)
					) {
						settings.setRestoreNotification(boolean_value);
					} else if (
						name.equals(
							Settings.SETTING_NAME_SMS_PARSING_NOTIFICATION
						)
					) {
						settings.setSmsParsingNotification(boolean_value);
					} else if (
						name.equals(
							Settings.SETTING_NAME_SMS_IMPORT_NOTIFICATION
						)
					) {
						settings.setSmsImportNotification(boolean_value);
					} else if (
						name.equals(Settings.SETTING_NAME_DROPBOX_NOTIFICATION)
					) {
						settings.setDropboxNotification(boolean_value);
					} else if (
						name.equals(
							Settings.SETTING_NAME_MONTHLY_RESET_NOTIFICATION
						)
					) {
						settings.setMonthlyResetNotification(boolean_value);
					} else if (
						name.equals(
							Settings.SETTING_NAME_CURRENCY_UPDATE_NOTIFICATION
						)
					) {
						settings.setCurrencyUpdateNotification(boolean_value);
					}
				}
			}
			settings.save();

			NodeList spending_list = budget.getElementsByTagName("spending");
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

					if (!spending_sql.isEmpty()) {
						spending_sql += ",";
					}
					spending_sql += "("
							+ String.valueOf(timestamp) + ","
							+ spending.getAttribute("amount") + ","
							+ DatabaseUtils.sqlEscapeString(
								spending.getAttribute("comment")
							)
						+ ")";
				}
			}

			NodeList buy_list = budget.getElementsByTagName("buy");
			for (int i = 0; i < buy_list.getLength(); i++) {
				if (buy_list.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element buy = (Element)buy_list.item(i);
					if (
						!buy.hasAttribute("name")
						|| !buy.hasAttribute("cost")
						|| !buy.hasAttribute("priority")
						|| !buy.hasAttribute("purchased")
					) {
						continue;
					}

					String status =
						buy.getAttribute("purchased").equals("false")
							? "0"
							: "1";

					String monthly =
						!buy.hasAttribute("monthly")
						|| buy.getAttribute("monthly").equals("false")
							? "0"
							: "1";

					if (!buy_sql.isEmpty()) {
						buy_sql += ",";
					}
					buy_sql += "("
							+ DatabaseUtils.sqlEscapeString(
								buy.getAttribute("name")
							) + ","
							+ buy.getAttribute("cost") + ","
							+ buy.getAttribute("priority") + ","
							+ status + ","
							+ monthly
						+ ")";
				}
			}

			NodeList currency_list = budget.getElementsByTagName("currency");
			for (int i = 0; i < currency_list.getLength(); i++) {
				if (currency_list.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element currency = (Element)currency_list.item(i);
					if (
						!currency.hasAttribute("date")
						|| !currency.hasAttribute("code")
						|| !currency.hasAttribute("rate")
					) {
						continue;
					}

					long timestamp = 0;
					try {
						Date date = XML_DATE_FORMAT.parse(
							currency.getAttribute("date")
						);
						timestamp = date.getTime() / 1000L;
					} catch (java.text.ParseException exception) {
						continue;
					}

					String date = CURRENCY_DATE_FORMAT.format(
						new Date(timestamp * 1000L)
					);

					if (!currency_sql.isEmpty()) {
						currency_sql += ",";
					}
					currency_sql += "("
							+ String.valueOf(timestamp) + ","
							+ DatabaseUtils.sqlEscapeString(date) + ","
							+ DatabaseUtils.sqlEscapeString(
								currency.getAttribute("code")
							) + ","
							+ currency.getAttribute("rate")
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

		if (!spending_sql.isEmpty() || !buy_sql.isEmpty() || !currency_sql.isEmpty()) {
			SQLiteDatabase database = Utils.getDatabase(context);
			if (!spending_sql.isEmpty()) {
				database.execSQL("DELETE FROM spendings");
				database.execSQL(
					"INSERT INTO spendings"
					+ "(timestamp, amount, comment)"
					+ "VALUES" + spending_sql
				);
			}

			if (!buy_sql.isEmpty()) {
				database.execSQL("DELETE FROM buys");
				database.execSQL(
					"INSERT INTO buys"
					+ "(name, cost, priority, status, monthly)"
					+ "VALUES" + buy_sql
				);
			}

			if (!currency_sql.isEmpty()) {
				database.execSQL("DELETE FROM currencies");
				database.execSQL(
					"INSERT INTO currencies"
					+ "(timestamp, date, code, rate)"
					+ "VALUES" + currency_sql
				);
			}
			database.close();
		}

		if (Settings.getCurrent(context).isRestoreNotification()) {
			Date current_date = new Date();
			DateFormat notification_timestamp_format =
				DateFormat.getDateTimeInstance(
					DateFormat.DEFAULT,
					DateFormat.DEFAULT,
					Locale.US
				);
			String notification_timestamp =
				notification_timestamp_format.format(
					current_date
				);
			Utils.showNotification(
				context,
				context.getString(R.string.app_name),
				"Restored at " + notification_timestamp + ".",
				null
			);
		}
	}

	private static final String BACKUPS_DIRECTORY = ".wizard-budget";
	private static final long BACKUP_VERSION = 6;
	private static final SimpleDateFormat XML_DATE_FORMAT =
		new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss",
			Locale.US
		);
	private static final SimpleDateFormat CURRENCY_DATE_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd", Locale.US);

	private Context context;
}
