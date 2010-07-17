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

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import be.benvd.mvforandroid.data.DatabaseHelper;
import be.benvd.mvforandroid.data.MVDataService;

public class CreditActivity extends Activity {

	private DatabaseHelper helper;

	/**
	 * Callback for the MVDataService.
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateView();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.credit);

		helper = new DatabaseHelper(this);

		updateView();

		Button updateButton = (Button) findViewById(R.id.update_button);
		updateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setProgressBarIndeterminateVisibility(true);
				new UpdateCreditTask().execute();
			}
		});
	}

	private void updateView() {
		TextView creditText = (TextView) findViewById(R.id.credit_text);
		creditText.setText(helper.credit.getRemainingCredit() + " EUR remaining");

		TextView smsText = (TextView) findViewById(R.id.sms_text);
		smsText.setText(helper.credit.getRemainingSms() + " SMS remaining");

		TextView dataText = (TextView) findViewById(R.id.data_text);
		dataText.setText(helper.credit.getRemainingData() + " bytes remaining");

		TextView validText = (TextView) findViewById(R.id.valid_text);
		validText.setText("Valid until " + helper.credit.getValidUntil());
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(MVDataService.CREDIT_UPDATED));
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

	// TODO handle rotation

	public class UpdateCreditTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String response = getCreditResponse();
				helper.credit.update(new JSONObject(response));
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
			return "{\"valid_until\": \"2010-07-21 21:38:00\", \"sms\": " + getRandom(1000) + ", \"data\": "
					+ getRandom(1073741824) + ", \"is_expired\": false, \"credits\": \"" + getRandom(40) + ".37\"}";
		}

		private int getRandom(int to) {
			return (int) Math.floor(Math.random() * to);
		}

		@Override
		protected void onPostExecute(Void result) {
			updateView();
			setProgressBarIndeterminateVisibility(false);
		}

	}

}
