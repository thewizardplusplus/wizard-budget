package ru.thewizardplusplus.wizardbudget;

import java.util.*;

import android.content.*;
import android.widget.*;

public class CurrencyWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
	public CurrencyWidgetFactory(Context context) {
		this.context = context;
	}

	@Override
	public void onCreate() {
		items = new ArrayList<String>();
	}

	@Override
	public void onDestroy() {}

	@Override
	public void onDataSetChanged() {
		items.clear();

		CurrencyManager currency_manager = new CurrencyManager(context);
		items = currency_manager.getAllCurrenciesForWidget();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		Intent click_intent = new Intent();
		click_intent.putExtra(Settings.SETTING_NAME_CURRENT_PAGE, "currencies");

		RemoteViews view = new RemoteViews(
			context.getPackageName(),
			R.layout.currency_widget_list_item
		);
		view.setTextViewText(
			R.id.currency_widget_list_item_title,
			items.get(position)
		);
		view.setOnClickFillInIntent(
			R.id.currency_widget_list_item_title,
			click_intent
		);

		return view;
	}

	@Override
	public RemoteViews getLoadingView() {
		RemoteViews view = new RemoteViews(
			context.getPackageName(),
			R.layout.currency_widget_list_item
		);
		view.setTextViewText(R.id.currency_widget_list_item_title, "Loading...");

		return view;
	}

	private Context context;
	private List<String> items;
}
