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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
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
			case R.id.about:
				showAboutDialog();
				return true;
		}

		return false;
	}

	private void showAboutDialog() {
		Builder builder = new Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(getString(R.string.about));
		builder.setMessage(getAboutMessage());
		builder.setPositiveButton(getString(android.R.string.ok), null);
		AlertDialog dialog = builder.create();
		dialog.show();
		allowClickableLinks(dialog);
	}

	private CharSequence getAboutMessage() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getString(R.string.app_name));
		stringBuilder.append(" ");
		stringBuilder.append(getVersionName());
		stringBuilder.append("\n\n");
		stringBuilder.append("http://github.com/benvd/mvfa\nhttp://benvd.be/mvfa\n\n");
		stringBuilder.append("@benvandaele\nvandaeleben@gmail.com");

		SpannableStringBuilder message = new SpannableStringBuilder(stringBuilder.toString());
		Linkify.addLinks(message, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);

		int twitterStart = stringBuilder.toString().indexOf("@benvandaele");
		int twitterEnd = twitterStart + "@benvandaele".length();
		message.setSpan(new URLSpan("http://twitter.com/benvandaele"), twitterStart, twitterEnd,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return message;
	}

	private String getVersionName() {
		try {
			ComponentName componentName = new ComponentName(this, MainActivity.class);
			PackageInfo info = getPackageManager().getPackageInfo(componentName.getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			// Won't happen, versionName is present in the manifest!
			return "";
		}
	}

	private void allowClickableLinks(AlertDialog dialog) {
		TextView message = (TextView) dialog.findViewById(android.R.id.message);
		message.setMovementMethod(LinkMovementMethod.getInstance());
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
