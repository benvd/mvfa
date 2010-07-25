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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

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

		updateView();
	}

	private void updateView() {
		Button updateButton = (Button) findViewById(R.id.update_button);
		updateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setProgressBarIndeterminateVisibility(true);
				new UpdateTopupsTask().execute();
			}
		});

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

	private static SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");

	static class TopupsHolder {
		private TextView amount = null, method = null, date = null;

		TopupsHolder(View listItem) {
			amount = (TextView) listItem.findViewById(R.id.topup_amount);
			method = (TextView) listItem.findViewById(R.id.topup_method);
			date = (TextView) listItem.findViewById(R.id.topup_date);
		}

		public void populateFrom(Cursor c, DatabaseHelper helper) {
			amount.setText((int) helper.topups.getAmount(c) + "€");
			method.setText(helper.topups.getMethod(c));
			date.setText(formatDate.format(new Date(helper.topups.getExecutedOn(c))));
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

	// TODO handle rotation

	public class UpdateTopupsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String response = getTopupsResponse();
				helper.topups.update(new JSONArray(response), false);
			} catch (JSONException e) {
				Log.e("MVFA", "Exception in doInBackground", e);
			}
			return null;
		}

		private String getTopupsResponse() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Log.e("MVFA", "Exception in getTopupsResponse", e);
			}
			return "[{\"status\": \"Top-up done\", \"amount\": \""
					+ new Random().nextInt(40)
					+ ".00\", \"amount_ex_vat\": \"12.40\", \"executed_on\": \"2010-07-23 13:24:26\", \"method\": \"150 Viking Points\", \"payment_received_on\": \"2010-07-23 13:24:26\"}, {\"status\": \"Top-up done\", \"amount\": \"15.00\", \"amount_ex_vat\": \"12.40\", \"executed_on\": \"2010-06-21 21:38:33\", \"method\": \"Ogone\", \"payment_received_on\": \"2010-06-21 21:38:32\"}, {\"status\": \"Top-up done\", \"amount\": \"15.00\", \"amount_ex_vat\": \"12.40\", \"executed_on\": \"2010-05-01 11:28:36\", \"method\": \"PayPal\", \"payment_received_on\": \"2010-05-01 11:24:01\"}, {\"status\": \"Top-up done\", \"amount\": \"15.00\", \"amount_ex_vat\": \"12.40\", \"executed_on\": \"2010-03-17 19:07:46\", \"method\": \"PayPal\", \"payment_received_on\": \"2010-03-17 19:07:41\"}, {\"status\": \"Top-up done\", \"amount\": \"15.00\", \"amount_ex_vat\": \"12.40\", \"executed_on\": \"2010-02-15 12:42:52\", \"method\": \"PayPal\", \"payment_received_on\": \"2010-02-15 12:42:40\"}, {\"status\": \"Top-up done\", \"amount\": \"15.00\", \"amount_ex_vat\": \"12.40\", \"executed_on\": \"2010-01-07 14:49:00\", \"method\": \"PayPal\", \"payment_received_on\": \"2010-01-07 14:48:51\"}, {\"status\": \"Top-up done\", \"amount\": \"15.00\", \"amount_ex_vat\": \"12.40\", \"executed_on\": \"2009-12-04 00:45:43\", \"method\": \"PayPal\", \"payment_received_on\": \"2009-12-04 00:45:30\"}, {\"status\": \"Top-up done\", \"amount\": \"15.00\", \"amount_ex_vat\": \"12.40\", \"executed_on\": \"2009-10-28 10:23:30\", \"method\": \"PayPal\", \"payment_received_on\": \"2009-10-28 10:23:17\"}, {\"status\": \"Top-up done\", \"amount\": \"15.00\", \"amount_ex_vat\": \"12.40\", \"executed_on\": \"2009-09-28 08:03:00\", \"method\": \"PayPal\", \"payment_received_on\": \"2009-09-28 08:03:00\"}, {\"status\": \"Top-up done\", \"amount\": \"15.00\", \"amount_ex_vat\": \"12.40\", \"executed_on\": \"2009-08-27 22:35:39\", \"method\": \"PayPal\", \"payment_received_on\": \"2009-08-27 22:35:39\"}]";
		}

		@Override
		protected void onPostExecute(Void result) {
			model.requery();
			setProgressBarIndeterminateVisibility(false);
		}

	}

}
