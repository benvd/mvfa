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

import be.benvd.mvforandroid.data.MVDataService;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import my.android.app.TabActivity;
import my.android.widget.TabHost;
import my.android.widget.TabHost.OnTabChangeListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends TabActivity {

	public static final String TAG = "MVFA";
	private static final String FIRST_TIME = "be.benvd.mvforandroid.FirstTime";
	private SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean(FIRST_TIME, true))
			firstTimeInit();

		setupTabHost();
	}

	private void firstTimeInit() {
		// FIXME: This takes care of starting the service after app installation. We still need to add an OnBootReceiver
		// to start the service after a reboot.
		Intent i = new Intent(this, MVDataService.class);
		i.setAction(MVDataService.SCHEDULE_SERVICE);
		WakefulIntentService.sendWakefulWork(this, i);

		prefs.edit().putBoolean(FIRST_TIME, false).commit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
		}

		return false;
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

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				MainActivity.this.setProgressBarIndeterminateVisibility(false);
			}
		});
	}

	private View getTabView(int stringId) {
		View view = getLayoutInflater().inflate(R.layout.tab_view, null, false);
		TextView text = (TextView) view.findViewById(R.id.tab_text);
		text.setText(stringId);
		return view;
	}

}
