package cx.myhome.yak.ghsc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		updateSummary(null);
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
		if(key == null || key.equals(account_key)) {
			Preference accountPref = getPreferenceScreen().findPreference(account_key);
			accountPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(account_key, ""));
		}
	}

	static String getAccount(Context context) {
		String key = (String)context.getText(R.string.account_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
	}
}