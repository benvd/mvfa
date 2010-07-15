package be.benvd.mvforandroid;

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

	private Intent broadcast = new Intent(UPDATE);
	private IBinder binder;
	private AlarmManager alarm = null;
	private PendingIntent pi = null;
	private SharedPreferences prefs;
	private int counter = 0;

	public class LocalBinder extends Binder {
		MVDataService getService() {
			return MVDataService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
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

		alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(this, OnAlarmReceiver.class);
		pi = PendingIntent.getBroadcast(this, 0, i, 0);

		setAlarm(5000);
	}

	private void setAlarm(long period) {
		alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock
				.elapsedRealtime()
				+ period, pi);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		if (intent.getAction().equals(UPDATE_ACTION)) {
			Log.i("MVDataService", "Doing wakeful work");
			setAlarm(Long
					.parseLong(prefs.getString("update_frequency", "5000")));
			sendBroadcast(broadcast);
		}
	}

	public int getData() {
		return counter++;
	}

}
