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

package be.benvd.mvforandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import be.benvd.mvforandroid.data.DatabaseHelper;
import be.benvd.mvforandroid.data.MVDataService;

public class TopupsActivity extends Activity {

	public DatabaseHelper helper;
	private Cursor model;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			model.requery();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.topups);

		helper = new DatabaseHelper(this);
		model = helper.topups.getAll();

		ListView topupsList = (ListView) findViewById(R.id.topups_list);
		topupsList.setAdapter(new TopupsAdapter(this, model));
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(MVDataService.TOPUPS_UPDATED));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		helper.close();
	}

	static class TopupsHolder {
		private TextView method = null;

		TopupsHolder(View listItem) {
			method = (TextView) listItem.findViewById(R.id.method);
		}

		public void populateFrom(Cursor c, DatabaseHelper helper) {
			method.setText(helper.topups.getMethod(c));
		}

	}

	class TopupsAdapter extends CursorAdapter {

		public TopupsAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TopupsHolder holder = (TopupsHolder) view.getTag();
			holder.populateFrom(cursor, helper);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View listItem = inflater.inflate(R.layout.topups_list_item, parent, false);
			TopupsHolder holder = new TopupsHolder(listItem);
			listItem.setTag(holder);
			return listItem;
		}

	}

}
