package cx.myhome.yak.ghsc;

import java.util.TimeZone;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		updateSummary(null);
		CharSequence timezone_key = getText(R.string.timezone_key);
		ListPreference timezonePref = (ListPreference)getPreferenceScreen().findPreference(timezone_key);
		String[] timezone = TimeZone.getAvailableIDs();
		timezonePref.setEntries(timezone);
		timezonePref.setEntryValues(timezone);
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		updateSummary(key);
	}

	private void updateSummary(String key) {
		String account_key = (String)getText(R.string.account_key);
		String timezone_key = (String)getText(R.string.timezone_key);
		if(key == null || key.equals(account_key)) {
			Preference accountPref = getPreferenceScreen().findPreference(account_key);
			accountPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(account_key, ""));
		}
		if(key == null || key.equals(timezone_key)) {
			Preference timezonePref = getPreferenceScreen().findPreference(timezone_key);
			timezonePref.setSummary(getPreferenceScreen().getSharedPreferences().getString(timezone_key, ""));
		}
	}

	static String getAccount(Context context) {
		String key = (String)context.getText(R.string.account_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
	}

	static String getTimezone(Context context) {
		String key = (String)context.getText(R.string.timezone_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
	}
}