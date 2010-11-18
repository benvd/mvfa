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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public static final String OPEN_APP = "0";
	public static final String UPDATE_DATA = "1";
	public static final String WIDGET_ACTION = "widget_action";

	private Preference autoCreditPreference;
	private Preference autoUsagePreference;
	private Preference autoTopupsPreference;
	private Preference updateFrequencyPreference;
	private Preference widgetActionPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		autoCreditPreference = getPreferenceScreen().findPreference("auto_credit");
		autoUsagePreference = getPreferenceScreen().findPreference("auto_usage");
		autoTopupsPreference = getPreferenceScreen().findPreference("auto_topups");
		updateCreditPreference(getPreferenceScreen().getSharedPreferences());
		updateUsagePreference(getPreferenceScreen().getSharedPreferences());
		updateTopupsPreference(getPreferenceScreen().getSharedPreferences());

		updateFrequencyPreference = getPreferenceScreen().findPreference("update_frequency");
		updateFrequencyPreference();

		widgetActionPreference = getPreferenceScreen().findPreference(WIDGET_ACTION);
		updateWidgetActionPreference();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (!(prefs.getBoolean("auto_credit", false) || prefs.getBoolean("auto_usage", false) || prefs.getBoolean(
				"auto_topups", false))) {
			Intent intent = new Intent(this, MVDataService.class);
			intent.setAction(MVDataService.STOP_SERVICE);
			WakefulIntentService.sendWakefulWork(this, intent);
		}

		if (key.equals("auto_credit")) {
			updateCreditPreference(prefs);
		} else if (key.equals("auto_usage")) {
			updateUsagePreference(prefs);
		} else if (key.equals("auto_topups")) {
			updateTopupsPreference(prefs);
		} else if (key.equals("update_frequency")) {
			updateFrequencyPreference();
			rescheduleService();
		} else if (key.equals(WIDGET_ACTION)) {
			updateWidgetActionPreference();
		}
	}

	private void updateCreditPreference(SharedPreferences sharedPreferences) {
		boolean autoCredit = sharedPreferences.getBoolean("auto_credit", false);
		autoCreditPreference.setSummary(autoCredit ? getString(R.string.settings_auto_credit_enabled)
				: getString(R.string.settings_auto_credit_disabled));
	}

	private void updateUsagePreference(SharedPreferences sharedPreferences) {
		boolean autoUsage = sharedPreferences.getBoolean("auto_usage", false);
		autoUsagePreference.setSummary(autoUsage ? getString(R.string.settings_auto_usage_enabled)
				: getString(R.string.settings_auto_usage_disabled));
	}

	private void updateTopupsPreference(SharedPreferences sharedPreferences) {
		boolean autoTopups = sharedPreferences.getBoolean("auto_topups", false);
		autoTopupsPreference.setSummary(autoTopups ? getString(R.string.settings_auto_topups_enabled)
				: getString(R.string.settings_auto_topups_disabled));
	}

	private void updateFrequencyPreference() {
		updateFrequencyPreference.setSummary(getString(R.string.settings_frequency,
				((ListPreference) updateFrequencyPreference).getEntry()));
	}

	private void rescheduleService() {
		Intent stop = new Intent(this, MVDataService.class);
		stop.setAction(MVDataService.STOP_SERVICE);
		WakefulIntentService.sendWakefulWork(this, stop);

		Intent start = new Intent(this, MVDataService.class);
		start.setAction(MVDataService.SCHEDULE_SERVICE);
		WakefulIntentService.sendWakefulWork(this, start);
	}

	private void updateWidgetActionPreference() {
		widgetActionPreference.setSummary(getString(R.string.settings_widget_action,
				((ListPreference) widgetActionPreference).getEntry()));
	}

}
