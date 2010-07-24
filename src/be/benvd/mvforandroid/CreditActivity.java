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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import be.benvd.mvforandroid.data.DatabaseHelper;
import be.benvd.mvforandroid.data.FormatUtil;
import be.benvd.mvforandroid.data.MVDataService;

import com.commonsware.cwac.sacklist.SackOfViewsAdapter;

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
		ListView list = (ListView) findViewById(R.id.credit_list);
		list.setAdapter(new CreditAdapter(4));
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
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.e("MVFA", "Exception in getCreditResponse", e);
			}
			return "{\"valid_until\": \"2010-08-16 13:24:00\", \"data\": 753741824, \"sms\": 869, \"credits\": \"6.59\", \"price_plan\": \"Classic\", \"is_expired\": false}";
		}

		@Override
		protected void onPostExecute(Void result) {
			updateView();
			setProgressBarIndeterminateVisibility(false);
		}

	}

	class CreditAdapter extends SackOfViewsAdapter {

		public CreditAdapter(int count) {
			super(count);
		}

		@Override
		protected View newView(int position, ViewGroup parent) {
			switch (position) {
				case 0: {
					double remainingCredit = helper.credit.getRemainingCredit();
					View view = getLayoutInflater().inflate(R.layout.credit_credit, parent, false);
					TextView text = (TextView) view.findViewById(R.id.credit_text);
					text.setText(FormatUtil.formatCurrency(remainingCredit) + " " + getString(R.string.remaining));

					float ratio = ((float) remainingCredit / helper.credit.getPricePlan());
					view.setBackgroundDrawable(getProgressBackground(ratio));

					return view;
				}
				case 1: {
					int remainingSms = helper.credit.getRemainingSms();
					View view = getLayoutInflater().inflate(R.layout.credit_sms, parent, false);
					TextView text = (TextView) view.findViewById(R.id.sms_text);
					text.setText(remainingSms + " " + getString(R.string.sms_remaining));

					float ratio = ((float) remainingSms / 1000);
					view.setBackgroundDrawable(getProgressBackground(ratio));

					return view;
				}
				case 2: {
					int remainingBytes = helper.credit.getRemainingData();
					View view = getLayoutInflater().inflate(R.layout.credit_data, parent, false);
					TextView text = (TextView) view.findViewById(R.id.data_text);
					text.setText((remainingBytes / 1048576) + " " + getString(R.string.megabytes_remaining));

					float ratio = ((float) remainingBytes / 1073741824);
					view.setBackgroundDrawable(getProgressBackground(ratio));

					return view;
				}
				case 3: {
					View view = getLayoutInflater().inflate(R.layout.credit_valid, parent, false);
					TextView text = (TextView) view.findViewById(R.id.valid_text);
					text.setText(getString(R.string.valid_until) + " "
							+ FormatUtil.formatValidUntilDate(helper.credit.getValidUntil()));
					return view;
				}
			}
			return null;
		}

		private BitmapDrawable getProgressBackground(double ratio) {
			// Setup bitmap and corresponding canvas
			int width = getWindow().getWindowManager().getDefaultDisplay().getWidth();
			Bitmap result = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas();
			c.setBitmap(result);

			// Draw background
			c.drawColor(0xffeeeeee);

			// Draw progress rectangle
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(Color.LTGRAY);
			c.drawRect(0, 0, (float) (ratio * width), 1, paint);

			return new BitmapDrawable(result);
		}

	}

}
