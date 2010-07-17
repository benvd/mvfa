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

public class UsageActivity extends Activity {

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

		setContentView(R.layout.usage);

		helper = new DatabaseHelper(this);

		ListView usageList = (ListView) findViewById(R.id.usage_list);
		model = helper.usage.getAll();
		usageList.setAdapter(new UsageAdapter(model));
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(MVDataService.USAGE_UPDATED));
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

	static class UsageHolder {
		private TextView contact = null;

		UsageHolder(View listItem) {
			contact = (TextView) listItem.findViewById(R.id.contact);
		}

		void populateFrom(Cursor c, DatabaseHelper helper) {
			contact.setText(helper.usage.getContact(c));
		}
	}

	class UsageAdapter extends CursorAdapter {
		UsageAdapter(Cursor c) {
			super(UsageActivity.this, c);
		}

		@Override
		public void bindView(View row, Context ctxt, Cursor c) {
			UsageHolder holder = (UsageHolder) row.getTag();
			holder.populateFrom(c, helper);
		}

		@Override
		public View newView(Context ctxt, Cursor c, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View listItem = inflater.inflate(R.layout.usage_list_item, parent, false);
			UsageHolder holder = new UsageHolder(listItem);
			listItem.setTag(holder);
			return listItem;
		}
	}

}
