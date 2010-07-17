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
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import be.benvd.mvforandroid.data.DatabaseHelper;

public class HistoryActivity extends Activity {

	public DatabaseHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.history);

		helper = new DatabaseHelper(this);

		ListView historyList = (ListView) findViewById(R.id.history_list);
		// historyList.setAdapter();
	}

	static class HistoryHolder {
		private TextView contact = null;

		HistoryHolder(View listItem) {
			contact = (TextView) listItem.findViewById(R.id.contact);
		}

		void populateFrom(Cursor c, DatabaseHelper helper) {
			contact.setText(helper.history.getContact(c));
		}
	}

	class HistoryAdapter extends CursorAdapter {
		HistoryAdapter(Cursor c) {
			super(HistoryActivity.this, c);
		}

		@Override
		public void bindView(View row, Context ctxt, Cursor c) {
			HistoryHolder holder = (HistoryHolder) row.getTag();
			holder.populateFrom(c, helper);
		}

		@Override
		public View newView(Context ctxt, Cursor c, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View listItem = inflater.inflate(R.layout.history_list_item, parent, false);
			HistoryHolder holder = new HistoryHolder(listItem);
			listItem.setTag(holder);
			return (listItem);
		}
	}

}
