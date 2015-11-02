package ru.thewizardplusplus.wizardbudget;

import java.util.*;

import android.content.*;
import android.widget.*;
import android.os.*;

public class BuyWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
	public BuyWidgetFactory(Context context) {
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

		int prefix = (new Random(SystemClock.currentThreadTimeMillis())).nextInt(90) + 10;
		for (int i = 0; i < 10; i++) {
			items.add("Item #" + prefix + "." + i);
		}
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

		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.buy_widget_list_item);
		view.setTextViewText(R.id.buy_widget_list_item_title, items.get(position));
		view.setOnClickFillInIntent(R.id.buy_widget_list_item_title, click_intent);

		return view;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	private Context context;
	private List<String> items;
}
