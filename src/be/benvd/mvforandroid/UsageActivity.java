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

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
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
		updateView();
	}

	public void updateView() {
		Button updateButton = (Button) findViewById(R.id.update_button);
		updateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setProgressBarIndeterminateVisibility(true);
				new UpdateUsageTask().execute();
			}
		});

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

	// TODO handle rotation

	public class UpdateUsageTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String response = getCreditResponse();
				helper.usage.update(new JSONArray(response), false);
			} catch (JSONException e) {
				Log.e("MVFA", "Exception in doInBackground", e);
			}
			return null;
		}

		private String getCreditResponse() {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Log.e("MVFA", "Exception in getCreditResponse", e);
			}
			return "[    {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    }, {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    }, {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    }, {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    }, {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    }, {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    }, {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    }, {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    }, {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    }, {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    }, {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:25:08\",         \"balance\": \"8.100000\",         \"duration_call\": 22,         \"to\": \"0498441877\",         \"is_sms\": false,         \"timestamp\": 1279329908,         \"price\": \"0.240000\",         \"duration_connection\": 11,         \"duration_human\": \"0:11\",         \"is_incoming\": false,         \"is_voice\": true,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:25:30\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:09:58\",         \"balance\": \"8.340000\",         \"duration_call\": 2,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328998,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:10:00\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:05:03\",         \"balance\": \"8.340000\",         \"duration_call\": 1,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328703,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:05:04\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:04:41\",         \"balance\": \"0.000000\",         \"duration_call\": 1,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328681,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": true,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:04:42\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:04:04\",         \"balance\": \"8.340000\",         \"duration_call\": 2,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328644,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:04:06\"    }]";
		}

		@Override
		protected void onPostExecute(Void result) {
			updateView();
			setProgressBarIndeterminateVisibility(false);
		}

	}

}
