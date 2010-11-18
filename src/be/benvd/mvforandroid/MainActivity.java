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
import my.android.widget.TabHost.OnTabChangeListener;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import be.benvd.mvforandroid.data.MVDataService;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class MainActivity extends TabActivity {

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
		Builder b = new Builder(this);
		b.setTitle(getString(R.string.login_title));
		View view = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.login_window, null);

		final EditText username = (EditText) view.findViewById(R.id.username);
		final EditText password = (EditText) view.findViewById(R.id.password);

		b.setView(view);
		b.setPositiveButton(getString(android.R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveCredentials(username.getText().toString(), password.getText().toString());

				Intent i = new Intent(MainActivity.this, MVDataService.class);
				i.setAction(MVDataService.SCHEDULE_SERVICE);
				WakefulIntentService.sendWakefulWork(MainActivity.this, i);

				prefs.edit().putBoolean(FIRST_TIME, false).commit();

				// Should have credentials now, let's force a refresh of the credits overview. We can't just broadcast
				// to the MVDataService, since we want visual confirmation in CreditsActivity that the update is in
				// progress, so we need to go through it.
				sendBroadcast(new Intent(CreditActivity.ACTION_REFRESH));
			}

			private void saveCredentials(String username, String password) {
				prefs.edit().putString("username", username).putString("password", password).commit();
			}
		});
		b.setCancelable(false);
		b.create().show();
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
