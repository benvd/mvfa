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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import be.benvd.mvforandroid.data.DatabaseHelper;
import be.benvd.mvforandroid.data.MVDataService;

import com.commonsware.cwac.merge.MergeAdapter;

public class UsageActivity extends Activity {

	public DatabaseHelper helper;

	// TODO Store this in the preferences (but not through SettingsActivity -- just remember it for later).
	private boolean ascending = true;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateView();
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
		MergeAdapter adapter = new MergeAdapter();

		Cursor c = helper.usage.getDates(false, ascending);
		ArrayList<Long> daysProcessed = new ArrayList<Long>();
		long timestamp, timestampEnd;
		while (c.moveToNext()) {
			timestamp = c.getLong(0);
			timestamp -= (timestamp % 86400000); // Timestamp without h/m/s, i.e. the beginning of the same day

			// Check if this day was already processed
			if (!daysProcessed.contains(new Long(timestamp))) {
				daysProcessed.add(new Long(timestamp));
				Log.d("MVFA", "" + timestamp);

				timestampEnd = timestamp + 86400000; // Begin of the day + one day in millis = end of the day

				Cursor usageByDay = helper.usage.getBetween(false, timestamp, timestampEnd, ascending);
				View separator = getLayoutInflater().inflate(R.layout.credit_separator, null, false);
				TextView text = (TextView) separator.findViewById(R.id.separator_text);
				text.setText(formatDate(timestamp));
				adapter.addView(separator);
				adapter.addAdapter(new UsageSectionAdapter(usageByDay));
			}
		}
		usageList.setAdapter(adapter);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.usage_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem ascDesc = menu.findItem(R.id.change_asc_desc);
		if (ascending) {
			ascDesc.setIcon(R.drawable.menu_sort_descending);
			ascDesc.setTitle(R.string.descending);
		} else {
			ascDesc.setIcon(R.drawable.menu_sort_ascending);
			ascDesc.setTitle(R.string.ascending);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.change_asc_desc: {
				ascending = !ascending;
				updateView();
				return true;
			}
			case R.id.settings: {
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			}
		}

		return false;
	}

	private static DecimalFormat currencyFormat = new DecimalFormat("#.##");
	private static SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
	private static DecimalFormat dataFormat = new DecimalFormat("#.##");

	private static String formatCurrency(double amount) {
		return currencyFormat.format(amount) + "€";

	}

	private static String formatTime(long timestamp) {
		return formatTime.format(new Date(timestamp));
	}

	private static String formatBytes(Context c, long bytes) {
		if (bytes < 1048576) {
			return dataFormat.format((double) bytes / 1024) + " " + c.getString(R.string.kilobytes);
		} else {
			return dataFormat.format((double) bytes / 1048576) + " " + c.getString(R.string.megabytes);
		}
	}

	private static String formatDuration(long duration) {
		int hours = (int) (duration / 3600);
		int minutes = (int) ((duration / 60) - (hours * 60));
		int seconds = (int) (duration % 60);
		String result = "";
		if (hours != 0)
			result += String.format("%02d:", hours);
		result += String.format("%02d:%02d", minutes, seconds);
		return result;
	}

	private SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");

	private String formatDate(long timestamp) {
		return formatDate.format(new Date(timestamp));
	}

	static class UsageHolder {
		private ImageView logo = null;
		private TextView title = null, cost = null, date = null, duration = null;

		UsageHolder(View listItem) {
			logo = (ImageView) listItem.findViewById(R.id.usage_logo);
			title = (TextView) listItem.findViewById(R.id.usage_title);
			cost = (TextView) listItem.findViewById(R.id.usage_cost);
			date = (TextView) listItem.findViewById(R.id.usage_date);
			duration = (TextView) listItem.findViewById(R.id.usage_duration);
		}

		void populateFrom(Cursor c, DatabaseHelper helper) {
			title.setText(helper.usage.getContact(c));
			cost.setText(formatCurrency(helper.usage.getCost(c)));
			date.setText(formatTime(helper.usage.getTimestamp(c)));
			duration.setText("xx");

			switch (helper.usage.getType(c)) {
				case DatabaseHelper.Usage.TYPE_DATA: {
					logo.setImageResource(R.drawable.credit_data);
					duration.setText(formatBytes(duration.getContext(), helper.usage.getduration(c)));
					break;
				}
				case DatabaseHelper.Usage.TYPE_MMS: {
					logo.setImageResource(R.drawable.credit_sms);
					duration.setText("");
					break;
				}
				case DatabaseHelper.Usage.TYPE_SMS: {
					logo.setImageResource(R.drawable.credit_sms);
					duration.setText("");
					break;
				}
				case DatabaseHelper.Usage.TYPE_VOICE: {
					if (helper.usage.isIncoming(c))
						logo.setImageResource(R.drawable.call_incoming);
					else
						logo.setImageResource(R.drawable.call_outgoing);
					duration.setText(formatDuration(helper.usage.getduration(c)));
					break;
				}
			}
		}
	}

	class UsageSectionAdapter extends CursorAdapter {
		UsageSectionAdapter(Cursor c) {
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
			return "[{\"is_data\": false, \"start_timestamp\": \"2010-07-24 11:14:55\", \"balance\": \"20.360000\", \"duration_call\": 41, \"to\": \"0498441877\", \"is_sms\": false, \"timestamp\": 1279962895, \"price\": \"0.240000\", \"duration_connection\": 29, \"duration_human\": \"0:29\", \"is_incoming\": false, \"is_voice\": true, \"is_mms\": false, \"end_timestamp\": \"2010-07-24 11:15:36\"}, {\"is_data\": true, \"start_timestamp\": \"2010-07-24 01:24:12\", \"balance\": \"20.600000\", \"duration_call\": 28312, \"to\": \"web.be\", \"is_sms\": false, \"timestamp\": 1279927452, \"price\": \"0.000000\", \"duration_connection\": 661466, \"duration_human\": \"183:44:26\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-24 09:16:04\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-23 17:44:33\", \"balance\": \"20.600000\", \"duration_call\": 1, \"to\": \"0497416434\", \"is_sms\": true, \"timestamp\": 1279899873, \"price\": \"0.000000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-23 17:44:34\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-23 17:44:16\", \"balance\": \"0.000000\", \"duration_call\": 1, \"to\": \"0497416434\", \"is_sms\": true, \"timestamp\": 1279899856, \"price\": \"0.000000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": true, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-23 17:44:17\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-23 13:21:27\", \"balance\": \"5.600000\", \"duration_call\": 91, \"to\": \"0066886247284\", \"is_sms\": false, \"timestamp\": 1279884087, \"price\": \"2.000000\", \"duration_connection\": 76, \"duration_human\": \"1:16\", \"is_incoming\": false, \"is_voice\": true, \"is_mms\": false, \"end_timestamp\": \"2010-07-23 13:22:58\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-23 13:20:13\", \"balance\": \"0.000000\", \"duration_call\": 37, \"to\": \"1933\", \"is_sms\": false, \"timestamp\": 1279884013, \"price\": \"0.000000\", \"duration_connection\": 35, \"duration_human\": \"0:35\", \"is_incoming\": false, \"is_voice\": true, \"is_mms\": false, \"end_timestamp\": \"2010-07-23 13:20:50\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-23 12:57:55\", \"balance\": \"0.000000\", \"duration_call\": 1, \"to\": \"0486191933\", \"is_sms\": true, \"timestamp\": 1279882675, \"price\": \"0.000000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": true, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-23 12:57:56\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-23 12:56:56\", \"balance\": \"7.600000\", \"duration_call\": 57, \"to\": \"9997\", \"is_sms\": false, \"timestamp\": 1279882616, \"price\": \"0.000000\", \"duration_connection\": 57, \"duration_human\": \"0:57\", \"is_incoming\": false, \"is_voice\": true, \"is_mms\": false, \"end_timestamp\": \"2010-07-23 12:57:53\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-23 12:56:56\", \"balance\": \"0.000000\", \"duration_call\": 64, \"to\": \"66886247284\", \"is_sms\": false, \"timestamp\": 1279882616, \"price\": \"0.000000\", \"duration_connection\": 57, \"duration_human\": \"0:57\", \"is_incoming\": true, \"is_voice\": true, \"is_mms\": false, \"end_timestamp\": \"2010-07-23 12:58:00\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-23 11:32:26\", \"balance\": \"7.600000\", \"duration_call\": 1, \"to\": \"0498441877\", \"is_sms\": true, \"timestamp\": 1279877546, \"price\": \"0.100000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-23 11:32:27\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-23 11:31:25\", \"balance\": \"0.000000\", \"duration_call\": 1, \"to\": \"0498441877\", \"is_sms\": true, \"timestamp\": 1279877485, \"price\": \"0.000000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": true, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-23 11:31:26\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-23 11:28:39\", \"balance\": \"7.700000\", \"duration_call\": 2, \"to\": \"0498441877\", \"is_sms\": true, \"timestamp\": 1279877319, \"price\": \"0.100000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-23 11:28:41\"}, {\"is_data\": true, \"start_timestamp\": \"2010-07-22 23:06:49\", \"balance\": \"20.600000\", \"duration_call\": 94568, \"to\": \"web.be\", \"is_sms\": false, \"timestamp\": 1279832809, \"price\": \"0.000000\", \"duration_connection\": 9429888, \"duration_human\": \"2619:24:48\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-24 01:22:57\"}, {\"is_data\": true, \"start_timestamp\": \"2010-07-22 22:55:37\", \"balance\": \"7.800000\", \"duration_call\": 26, \"to\": \"web.be\", \"is_sms\": false, \"timestamp\": 1279832137, \"price\": \"0.000000\", \"duration_connection\": 0, \"duration_human\": \"0:00\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-22 22:56:03\"}, {\"is_data\": true, \"start_timestamp\": \"2010-07-22 20:35:46\", \"balance\": \"7.800000\", \"duration_call\": 7366, \"to\": \"web.be\", \"is_sms\": false, \"timestamp\": 1279823746, \"price\": \"0.000000\", \"duration_connection\": 841084, \"duration_human\": \"233:38:04\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-22 22:38:32\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-22 19:40:38\", \"balance\": \"7.800000\", \"duration_call\": 2, \"to\": \"0497416434\", \"is_sms\": true, \"timestamp\": 1279820438, \"price\": \"0.100000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-22 19:40:40\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-22 19:00:02\", \"balance\": \"7.900000\", \"duration_call\": 1, \"to\": \"0498441877\", \"is_sms\": true, \"timestamp\": 1279818002, \"price\": \"0.100000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-22 19:00:03\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-22 18:59:35\", \"balance\": \"0.000000\", \"duration_call\": 1, \"to\": \"0498441877\", \"is_sms\": true, \"timestamp\": 1279817975, \"price\": \"0.000000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": true, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-22 18:59:36\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-22 18:53:27\", \"balance\": \"0.000000\", \"duration_call\": 1, \"to\": \"0497416434\", \"is_sms\": true, \"timestamp\": 1279817607, \"price\": \"0.000000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": true, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-22 18:53:28\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-22 17:57:55\", \"balance\": \"0.000000\", \"duration_call\": 1, \"to\": \"0497416434\", \"is_sms\": true, \"timestamp\": 1279814275, \"price\": \"0.000000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": true, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-22 17:57:56\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-22 17:56:30\", \"balance\": \"8.000000\", \"duration_call\": 1, \"to\": \"0497416434\", \"is_sms\": true, \"timestamp\": 1279814190, \"price\": \"0.100000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-22 17:56:31\"}, {\"is_data\": true, \"start_timestamp\": \"2010-07-21 23:26:53\", \"balance\": \"7.800000\", \"duration_call\": 75444, \"to\": \"web.be\", \"is_sms\": false, \"timestamp\": 1279747613, \"price\": \"0.000000\", \"duration_connection\": 22254141, \"duration_human\": \"6181:42:21\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-22 20:24:17\"}, {\"is_data\": true, \"start_timestamp\": \"2010-07-21 23:09:44\", \"balance\": \"8.100000\", \"duration_call\": 490, \"to\": \"web.be\", \"is_sms\": false, \"timestamp\": 1279746584, \"price\": \"0.000000\", \"duration_connection\": 94942, \"duration_human\": \"26:22:22\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-21 23:17:54\"}, {\"is_data\": true, \"start_timestamp\": \"2010-07-21 00:57:19\", \"balance\": \"8.100000\", \"duration_call\": 79881, \"to\": \"web.be\", \"is_sms\": false, \"timestamp\": 1279666639, \"price\": \"0.000000\", \"duration_connection\": 5368697, \"duration_human\": \"1491:18:17\", \"is_incoming\": false, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-21 23:08:40\"}, {\"is_data\": false, \"start_timestamp\": \"2010-07-20 21:02:20\", \"balance\": \"0.000000\", \"duration_call\": 1, \"to\": \"0486739305\", \"is_sms\": true, \"timestamp\": 1279652540, \"price\": \"0.000000\", \"duration_connection\": 1, \"duration_human\": \"0:01\", \"is_incoming\": true, \"is_voice\": false, \"is_mms\": false, \"end_timestamp\": \"2010-07-20 21:02:21\"}]";
		}

		@Override
		protected void onPostExecute(Void result) {
			updateView();
			setProgressBarIndeterminateVisibility(false);
		}

	}

}
