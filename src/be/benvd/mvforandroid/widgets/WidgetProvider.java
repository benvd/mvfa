/*
	Copyright (C) 2010 Ben Van Daele (vandaeleben@gmail.com)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.benvd.mvforandroid.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import be.benvd.mvforandroid.R;
import be.benvd.mvforandroid.data.DatabaseHelper;

// FIXME: Add layout-land widget.xml
// FIXME: Add widget_*.png files for mdpi/ldpi

public class WidgetProvider extends AppWidgetProvider {

	public static final String UPDATE_DATA_FROM_WIDGET = "be.benvd.mvforandroid.UpdateDataFromWidget";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.v("DEBUG", "onUpdate");
		int amount = appWidgetIds.length;

		for (int i = 0; i < amount; i++) {
			int appWidgetId = appWidgetIds[i];

			RemoteViews views = null;
			views = new RemoteViews(context.getPackageName(), R.layout.widget);

			Intent intent = new Intent(UPDATE_DATA_FROM_WIDGET);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

			updateViews(context, views);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (intent.getAction().equals(UPDATE_DATA_FROM_WIDGET)) {
			// FIXME: Handle widget click action (also add preference to choose between opening the app or updating)
		}
	}

	public void updateViews(Context context, RemoteViews views) {
		Log.v("DEBUG", "updateTextViews");
		DatabaseHelper helper = new DatabaseHelper(context);

		views.setTextViewText(R.id.credit_text, helper.credit.getRemainingCredit() + " €");
		views.setTextViewText(R.id.sms_text, helper.credit.getRemainingSms() + " SMS");
		views.setTextViewText(R.id.data_text, (helper.credit.getRemainingData() / (1024 * 1024)) + " MB");

		helper.close();
	}
}
