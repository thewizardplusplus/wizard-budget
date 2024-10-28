package ru.thewizardplusplus.wizardbudget;

import java.util.*;

import android.content.*;
import android.widget.*;

public class BuyWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
	public BuyWidgetFactory(Context context) {
		this.context = context;
	}

	@Override
	public void onCreate() {
		items = new ArrayList<BuyData>();
	}

	@Override
	public void onDestroy() {}

	@Override
	public void onDataSetChanged() {
		items.clear();

		BuyManager buy_manager = new BuyManager(context);
		boolean only_monthly = Settings.getCurrent(context).isOnlyMonthly();
		items = buy_manager.getBuysForWidget(only_monthly);
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
		click_intent.putExtra(Settings.SETTING_NAME_CURRENT_PAGE, "history");
		click_intent.putExtra(Settings.SETTING_NAME_CURRENT_SEGMENT, "buys");

		RemoteViews view = new RemoteViews(
			context.getPackageName(),
			R.layout.buy_widget_list_item
		);
		view.setOnClickFillInIntent(
			R.id.buy_widget_list_item_container,
			click_intent
		);

		BuyData item = items.get(position);
		view.setTextViewText(
			R.id.buy_widget_list_item_name,
			item.getName()
		);
		view.setTextViewText(
			R.id.buy_widget_list_item_cost,
			String.format("%.2f \u20bd", item.getCost())
		);

		return view;
	}

	@Override
	public RemoteViews getLoadingView() {
		RemoteViews view = new RemoteViews(
			context.getPackageName(),
			R.layout.buy_widget_list_item
		);
		view.setTextViewText(R.id.buy_widget_list_item_name, "Loading...");

		return view;
	}

	private Context context;
	private List<BuyData> items;
}
