package ru.thewizardplusplus.wizardbudget;

import android.appwidget.*;
import android.content.*;
import android.widget.*;
import android.app.*;
import android.net.*;
import android.text.*;
import android.text.style.*;
import android.graphics.*;

public class BuyWidget extends AppWidgetProvider {
	public static RemoteViews getUpdatedViews(Context context) {
		Intent item_intent = new Intent(context, BuyWidgetService.class);
		item_intent.setData(
			Uri.parse(item_intent.toUri(Intent.URI_INTENT_SCHEME))
		);

		Intent click_intent = new Intent(context, MainActivity.class);
		PendingIntent click_pending_intent = PendingIntent.getActivity(
			context,
			0,
			click_intent,
			PendingIntent.FLAG_UPDATE_CURRENT
		);

		RemoteViews views = new RemoteViews(
			context.getPackageName(),
			R.layout.buy_widget
		);
		views.setRemoteAdapter(R.id.buy_list, item_intent);
		views.setOnClickPendingIntent(
			R.id.buy_widget_container,
			click_pending_intent
		);
		views.setPendingIntentTemplate(R.id.buy_list, click_pending_intent);
		views.setEmptyView(R.id.buy_list, R.id.buy_empty_list_stub);

		BuyManager buy_manager = new BuyManager(context);
		double total_sum = Double.parseDouble(buy_manager.getAnyCostsSum());
		double monthly_sum = Double.parseDouble(buy_manager.getMonthlyCostsSum());
		views.setTextViewText(
			R.id.simple_monthly_buy_list_sum,
			formatBuySum("Monthly", monthly_sum, Double.NaN)
		);
		views.setTextViewText(
			R.id.monthly_buy_list_sum,
			formatBuySum("Monthly", monthly_sum, total_sum)
		);
		double single_sum = Double.parseDouble(buy_manager.getSingleCostsSum());
		views.setTextViewText(
			R.id.single_buy_list_sum,
			formatBuySum("Single", single_sum, total_sum)
		);

		boolean only_monthly = Settings.getCurrent(context).isOnlyMonthly();
		views.setDisplayedChild(
			R.id.buy_list_sum_view_flipper,
			only_monthly ? 0 : 1
		);

		return views;
	}

	@Override
	public void onUpdate(
		Context context,
		AppWidgetManager widget_manager,
		int[] widget_ids
	) {
		super.onUpdate(context, widget_manager, widget_ids);

		RemoteViews views = getUpdatedViews(context);
		widget_manager.updateAppWidget(widget_ids, views);
		widget_manager.notifyAppWidgetViewDataChanged(
			widget_ids,
			R.id.buy_list
		);
	}

	private static CharSequence formatBuySum(String kind, double sum, double total_sum) {
		String text = !Double.isNaN(total_sum)
			? String.format("%s sum: %.2f / %.2f \u20bd", kind, sum, total_sum)
			: String.format("%s sum: %.2f \u20bd", kind, sum);
		int title_end_index = text.indexOf(':');

		SpannableString formatted_text = new SpannableString(text);
		formatted_text.setSpan(new StyleSpan(Typeface.BOLD), 0, title_end_index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return formatted_text;
	}
}
