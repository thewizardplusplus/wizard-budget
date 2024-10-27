package ru.thewizardplusplus.wizardbudget;

import android.text.*;
import android.text.style.*;
import android.graphics.*;

public class CurrencyData {
	public CurrencyData(long timestamp, String code, double rate) {
		this.timestamp = timestamp;
		this.code = code;
		this.rate = rate;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public CharSequence getTitle(long actual_timestamp) {
		String title = String.format("%s:", code);
		title = replaceCurrencies(title);
		return applyActualFlag(title, actual_timestamp);
	}

	public CharSequence getDescription(long actual_timestamp) {
		double inverted_rate = 1 / rate;
		String description = rate >= inverted_rate
			? String.format(
				"1 RUB = %2$.2f %1$s",
				code,
				rate
			)
			: String.format(
				"1 %1$s = %2$.2f RUB",
				code,
				inverted_rate
			);
		description = replaceCurrencies(description);
		return applyActualFlag(description, actual_timestamp);
	}

	public CharSequence getActualFlag(long actual_timestamp) {
		String actual_flag = timestamp < actual_timestamp
			? "[OLDER]"
			: timestamp > actual_timestamp
				? "[NEWER]"
				: "";
		return applyActualFlag(actual_flag, actual_timestamp);
	}

	private long timestamp;
	private String code;
	private double rate;

	private static String replaceCurrencies(String text) {
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
		return text
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
	}

	private CharSequence applyActualFlag(String text, long actual_timestamp) {
		if (timestamp == actual_timestamp) {
			return text;
		}

		SpannableString formatted_text = new SpannableString(text);
		formatted_text.setSpan(new StyleSpan(Typeface.ITALIC), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return formatted_text;
	}
}
