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

	public static final String UPDATE = "be.benvd.mvforandroid.data.Updated";
	public static final String UPDATE_ACTION = "be.benvd.mvforandroid.data.Update";

	private final String URL_HISTORY = "https://mobilevikings.com/api/2.0/basic/usage.json";
	private final String URL_CREDIT = "https://mobilevikings.com/api/2.0/basic/sim_balance.json";
	private final String URL_TOPUPS = "https://mobilevikings.com/api/2.0/basic/top_up_history.json";

	private Intent broadcast = new Intent(UPDATE);
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

	private void scheduleNextUpdate() {
		alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
				+ Long.parseLong(prefs.getString("update_frequency", "60000")), wakefulWorkIntent);
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
			Log.i("MVFA", "Doing wakeful work");

			// TODO Have preferences to decide which of the url's should be updated.

			try {
				// TODO Fake response, to spare MV servers (and avoid throttling).
				String response = getResponse(URL_CREDIT);
				helper.credit.update(new JSONObject(response));
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Log.e("MVFA", "Exception in doWakefulWork", e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("MVFA", "Exception in doWakefulWork", e);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e("MVFA", "Exception in doWakefulWork", e);
			}

			// TODO Should something go wrong during the update, reschedule after a short while.

			scheduleNextUpdate();
			sendBroadcast(broadcast);
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

}
