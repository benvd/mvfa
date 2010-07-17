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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private Preference autoCreditPreference;
	private Preference autoUsagePreference;
	private Preference autoTopupsPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		autoCreditPreference = getPreferenceScreen().findPreference("auto_credit");
		autoUsagePreference = getPreferenceScreen().findPreference("auto_usage");
		autoTopupsPreference = getPreferenceScreen().findPreference("auto_topups");
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("auto_credit")) {
			autoCreditPreference
					.setSummary(sharedPreferences.getBoolean(key, false) ? getString(R.string.settings_auto_credit_enabled)
							: getString(R.string.settings_auto_credit_disabled));
		} else if (key.equals("auto_usage")) {
			sharedPreferences.getBoolean(key, false);
			autoUsagePreference
					.setSummary(sharedPreferences.getBoolean(key, false) ? getString(R.string.settings_auto_usage_enabled)
							: getString(R.string.settings_auto_usage_disabled));
		} else if (key.equals("auto_topups")) {
			sharedPreferences.getBoolean(key, false);
			autoTopupsPreference
					.setSummary(sharedPreferences.getBoolean(key, false) ? getString(R.string.settings_auto_topups_enabled)
							: getString(R.string.settings_auto_topups_disabled));
		} else {
		}
	}

}
