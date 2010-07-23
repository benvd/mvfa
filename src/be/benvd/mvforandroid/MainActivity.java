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
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import be.benvd.mvforandroid.data.MVDataService;

public class MainActivity extends TabActivity {

	private MVDataService dataService;

	/**
	 * Called when the MVDataService is bound to / unbound from the Activity.
	 */
	private ServiceConnection onService = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
			Log.d("MVFA", "Service bound to activity.");
			dataService = ((MVDataService.LocalBinder) rawBinder).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d("MVFA", "Service disconnected.");
			dataService = null;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Progress in subactivities: need to call the progress methods of the MainActivity.
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);

		// Binding the Activity to our MVDataService
		bindService(new Intent(this, MVDataService.class), onService, BIND_AUTO_CREATE);

		setupTabHost();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Unbind the Service
		unbindService(onService);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.main_menu, menu);
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

		tabHost.setCurrentTabByTag("topups");
	}

	private View getTabView(int stringId) {
		View view = getLayoutInflater().inflate(R.layout.tab_view, null, false);
		TextView text = (TextView) view.findViewById(R.id.tab_text);
		text.setText(stringId);
		return view;
	}

}
