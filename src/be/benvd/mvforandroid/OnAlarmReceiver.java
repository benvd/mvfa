package be.benvd.mvforandroid;

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class OnAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, MVDataService.class);
		i.setAction(MVDataService.UPDATE_ACTION);
		WakefulIntentService.sendWakefulWork(context, i);
	}

}
