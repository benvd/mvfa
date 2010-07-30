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

import my.android.app.TabActivity;
import my.android.widget.TabHost;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import be.benvd.mvforandroid.data.OnAlarmReceiver;

public class MainActivity extends TabActivity {

	public static final String TAG = "MVFA";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Progress in subactivities: need to call the progress methods of the MainActivity.
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);

		setupTabHost();

		Intent i = new Intent(this, OnAlarmReceiver.class);
		PendingIntent wakefulWorkIntent = PendingIntent.getBroadcast(this, 0, i, 0);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5000, wakefulWorkIntent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void setupTabHost() {
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		View tabView;

		intent = new Intent().setClass(this, CreditActivity.class);
		tabView = getTabView(R.string.credit);
		spec = tabHost.newTabSpec("credit").setIndicator(tabView).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, UsageActivity.class);
		tabView = getTabView(R.string.usage);
		spec = tabHost.newTabSpec("usage").setIndicator(tabView).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, TopupsActivity.class);
		tabView = getTabView(R.string.topups);
		spec = tabHost.newTabSpec("topups").setIndicator(tabView).setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTabByTag("topups");
	}

	private View getTabView(int stringId) {
		View view = getLayoutInflater().inflate(R.layout.tab_view, null, false);
		TextView text = (TextView) view.findViewById(R.id.tab_text);
		text.setText(stringId);
		return view;
	}

}
