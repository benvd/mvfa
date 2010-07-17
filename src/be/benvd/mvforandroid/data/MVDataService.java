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

package be.benvd.mvforandroid.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class MVDataService extends WakefulIntentService {

	private final String URL_USAGE = "https://mobilevikings.com/api/2.0/basic/usage.json";
	private final String URL_CREDIT = "https://mobilevikings.com/api/2.0/basic/sim_balance.json";
	private final String URL_TOPUPS = "https://mobilevikings.com/api/2.0/basic/top_up_history.json";

	private static final long RETRY_TIMEOUT = 30000;

	public static final String UPDATE_ACTION = "be.benvd.mvforandroid.data.Update";
	public static final String CREDIT_UPDATED = "be.benvd.mvforandroid.data.CreditUpdated";
	public static final String USAGE_UPDATED = "be.benvd.mvforandroid.data.UsageUpdated";
	public static final String TOPUPS_UPDATED = "be.benvd.mvforandroid.data.TopupsUpdated";

	private Intent creditBroadcast = new Intent(CREDIT_UPDATED);
	private Intent usageBroadcast = new Intent(USAGE_UPDATED);
	private Intent topupsBroadcast = new Intent(TOPUPS_UPDATED);

	private IBinder binder;
	private AlarmManager alarm = null;
	private PendingIntent wakefulWorkIntent = null;
	private SharedPreferences prefs;
	private DatabaseHelper helper;

	public class LocalBinder extends Binder {
		public MVDataService getService() {
			return MVDataService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	public MVDataService() {
		super("MVDataService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		binder = new LocalBinder();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		helper = new DatabaseHelper(this);
		alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(this, OnAlarmReceiver.class);
		wakefulWorkIntent = PendingIntent.getBroadcast(this, 0, i, 0);
		scheduleNextUpdate();
	}

	/**
	 * Schedules the next execution of doWakefulWork, using the frequency specified in the Preferences.
	 */
	private void scheduleNextUpdate() {
		long delay = Long.parseLong(prefs.getString("update_frequency", "86400000"));
		alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, wakefulWorkIntent);
		Log.d("MVDataService", "Scheduled update in " + delay + "ms.");
	}

	/**
	 * Schedules the next execution of doWakefulWork using RETRY_TIMEOUT.
	 */
	private void retry() {
		alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + RETRY_TIMEOUT,
				wakefulWorkIntent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Does the actual work.
	 */
	@Override
	protected void doWakefulWork(Intent intent) {
		if (intent.getAction().equals(UPDATE_ACTION)) {
			try {
				updateCredit();
				updateUsage();
				updateTopups();
			} catch (ClientProtocolException e) {
				Log.e("MVDataService", "Exception in doWakefulWork", e);
				retry();
			} catch (IOException e) {
				Log.e("MVDataService", "Exception in doWakefulWork", e);
				retry();
			} catch (JSONException e) {
				Log.e("MVDataService", "Exception in doWakefulWork", e);
				retry();
			} finally {
				helper.close();
			}

			scheduleNextUpdate();
		}
	}

	private void updateCredit() throws ClientProtocolException, IOException, JSONException {
		if (prefs.getBoolean("auto_credit", false)) {
			String response = getTestResponse(URL_CREDIT);
			helper.credit.update(new JSONObject(response));
			sendBroadcast(creditBroadcast);
			Log.i("MVDataService", "Updated credit");
		}
	}

	private void updateUsage() throws ClientProtocolException, IOException, JSONException {
		if (prefs.getBoolean("auto_usage", false)) {
			String response = getTestResponse(URL_USAGE);
			helper.usage.update(new JSONArray(response), false);
			sendBroadcast(usageBroadcast);
			Log.i("MVDataService", "Updated usage");
		}
	}

	private void updateTopups() throws ClientProtocolException, IOException, JSONException {
		if (prefs.getBoolean("auto_topups", false)) {
			String response = getTestResponse(URL_TOPUPS);
			helper.topups.update(new JSONArray(response), false);
			sendBroadcast(topupsBroadcast);
			Log.i("MVDataService", "Updated topups");
		}
	}

	/**
	 * Returns the GET response of the given url.
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * 
	 * @return The response of the given URL. If no response was found, null is returned.
	 */
	private String getResponse(String url) throws ClientProtocolException, IOException {
		String username = prefs.getString("username", null);
		String password = prefs.getString("password", null);

		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1),
				new UsernamePasswordCredentials(username + ":" + password));
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpclient.execute(httpget);

		if (response.getEntity() != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuilder sb = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();

			return sb.toString();
		}

		return null;
	}

	/**
	 * Returns fake data.
	 */
	private String getTestResponse(String url) throws ClientProtocolException, IOException {
		if (url.equals(URL_CREDIT)) {
			return "{\"valid_until\": \"2010-07-21 21:38:00\", \"sms\": " + getRandom(1000) + ", \"data\": "
					+ getRandom(1073741824) + ", \"is_expired\": false, \"credits\": \"" + getRandom(40) + ".37\"}";
		} else if (url.equals(URL_USAGE)) {
			return "[    {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:25:08\",         \"balance\": \"8.100000\",         \"duration_call\": 22,         \"to\": \"0498441877\",         \"is_sms\": false,         \"timestamp\": 1279329908,         \"price\": \"0.240000\",         \"duration_connection\": 11,         \"duration_human\": \"0:11\",         \"is_incoming\": false,         \"is_voice\": true,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:25:30\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:09:58\",         \"balance\": \"8.340000\",         \"duration_call\": 2,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328998,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:10:00\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:05:03\",         \"balance\": \"8.340000\",         \"duration_call\": 1,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328703,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:05:04\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:04:41\",         \"balance\": \"0.000000\",         \"duration_call\": 1,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328681,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": true,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:04:42\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:04:04\",         \"balance\": \"8.340000\",         \"duration_call\": 2,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328644,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:04:06\"    }]";
		} else if (url.equals(URL_TOPUPS)) {
			return "[    {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2010-06-21 21:38:33\",         \"method\": \"Ogone\",         \"payment_received_on\": \"2010-06-21 21:38:32\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2010-05-01 11:28:36\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2010-05-01 11:24:01\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2010-03-17 19:07:46\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2010-03-17 19:07:41\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2010-02-15 12:42:52\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2010-02-15 12:42:40\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2010-01-07 14:49:00\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2010-01-07 14:48:51\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2009-12-04 00:45:43\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2009-12-04 00:45:30\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2009-10-28 10:23:30\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2009-10-28 10:23:17\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2009-09-28 08:03:00\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2009-09-28 08:03:00\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2009-08-27 22:35:39\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2009-08-27 22:35:39\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2009-07-27 13:17:45\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2009-07-27 13:17:45\"    }]";
		} else {
			return "";
		}
	}

	private int getRandom(int to) {
		return (int) Math.floor(Math.random() * to);
	}

}
