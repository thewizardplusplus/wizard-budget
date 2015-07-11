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
				serializer.attribute("", "name", "credit_card_tag");
				serializer.attribute("", "value", settings.getCreditCardTag());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "parse_sms");
				serializer.attribute("", "value", settings.isParseSms() ? "true" : "false");
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "sms_number_pattern");
				serializer.attribute("", "value", settings.getSmsNumberPatternString());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "sms_spending_pattern");
				serializer.attribute("", "value", settings.getSmsSpendingPatternString());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "sms_income_pattern");
				serializer.attribute("", "value", settings.getSmsIncomePatternString());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "sms_spending_comment");
				serializer.attribute("", "value", settings.getSmsSpendingComment());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "sms_income_comment");
				serializer.attribute("", "value", settings.getSmsIncomeComment());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "save_backup_to_dropbox");
				serializer.attribute("", "value", settings.isSaveBackupToDropbox() ? "true" : "false");
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "analysis_harvest");
				serializer.attribute("", "value", settings.isAnalysisHarvest() ? "true" : "false");
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "harvest_username");
				serializer.attribute("", "value", settings.getHarvestUsername());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "harvest_subdomain");
				serializer.attribute("", "value", settings.getHarvestSubdomain());
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "backup_notification");
				serializer.attribute("", "value", settings.isBackupNotification() ? "true" : "false");
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "restore_notification");
				serializer.attribute("", "value", settings.isRestoreNotification() ? "true" : "false");
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "sms_parsing_notification");
				serializer.attribute("", "value", settings.isSmsParsingNotification() ? "true" : "false");
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "sms_import_notification");
				serializer.attribute("", "value", settings.isSmsImportNotification() ? "true" : "false");
				serializer.endTag("", "preference");

				serializer.startTag("", "preference");
				serializer.attribute("", "name", "dropbox_notification");
				serializer.attribute("", "value", settings.isDropboxNotification() ? "true" : "false");
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
					new String[]{"name", "cost", "priority", "status"},
					null,
					null,
					null,
					null,
					"status, priority DESC"
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

					serializer.endTag("", "buy");

					moved = cursor.moveToNext();
				}
				serializer.endTag("", "buys");
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
		try {
			Element budget = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(in)
				.getDocumentElement();
			budget.normalize();

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

					if (!buy_sql.isEmpty()) {
						buy_sql += ",";
					}
					buy_sql += "("
							+ DatabaseUtils.sqlEscapeString(
								buy.getAttribute("name")
							) + ","
							+ buy.getAttribute("cost") + ","
							+ buy.getAttribute("priority") + ","
							+ status
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

		if (!spending_sql.isEmpty() || !buy_sql.isEmpty()) {
			SQLiteDatabase database = Utils.getDatabase(context);
			database.execSQL("DELETE FROM spendings");
			database.execSQL(
				"INSERT INTO spendings"
				+ "(timestamp, amount, comment)"
				+ "VALUES" + spending_sql
			);

			database.execSQL("DELETE FROM buys");
			database.execSQL(
				"INSERT INTO buys"
				+ "(name, cost, priority, status)"
				+ "VALUES" + buy_sql
			);
			database.close();

			if (Settings.getCurrent(context).isRestoreNotification()) {
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
					"Restored at " + notification_timestamp + ".",
					null
				);
			}
		}
	}

	private static final String BACKUPS_DIRECTORY = "#wizard-budget";
	private static final long BACKUP_VERSION = 3;
	private static final SimpleDateFormat XML_DATE_FORMAT =
		new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss",
			Locale.US
		);

	private Context context;
}
