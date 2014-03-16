package cx.myhome.yak.ghsc;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
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

	private String getOffsetDesc(int offset)
	{
		int absOffset = Math.abs(offset);
		String sign = offset >= 0 ? "+" : "-";
		return String.format("GMT%s%d:%02d", sign, absOffset / 3600000, absOffset / 60000 % 60);
	}

	private String getTimezoneDesc(String timezoneID, String delimiter)
	{
		TimeZone tz = TimeZone.getTimeZone(timezoneID);
		Date current = new Date();
		int rawOffset = tz.getRawOffset();
		if(tz.inDaylightTime(current)) {
			return String.format("%s%s%s (DST)%s(ST: %s)", timezoneID, delimiter, getOffsetDesc(rawOffset + tz.getDSTSavings()), delimiter, getOffsetDesc(rawOffset));
		} else {
			return String.format("%s%s%s", timezoneID, delimiter, getOffsetDesc(rawOffset));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		CharSequence timezone_key = getText(R.string.timezone_key);
		ListPreference timezonePref = (ListPreference)getPreferenceScreen().findPreference(timezone_key);
		String[] timezone = getResources().getStringArray(R.array.timezone_ids);
// Sort by effective offsets
		TimeZone[] tz = new TimeZone[timezone.length];
		for(int i = 0; i < timezone.length; ++i) {
			tz[i] = TimeZone.getTimeZone(timezone[i]);
		}
		final Date d = new Date();
		Arrays.sort(tz, new Comparator<TimeZone>() {
			public int compare(TimeZone a, TimeZone b) {
				long a_ = a.getOffset(d.getTime());
				long b_ = b.getOffset(d.getTime());
				return a_ < b_ ? -1 : a_ > b_ ? 1 : 0;
			}
		});
// Back to string array
		for(int i = 0; i < timezone.length; ++i) {
			timezone[i] = tz[i].getID();
		}
		timezonePref.setEntryValues(timezone);
		String[] timezoneDesc = new String[timezone.length];
		for(int i = 0; i < timezone.length; ++i) {
			timezoneDesc[i] = getTimezoneDesc(timezone[i], "\n");
		}
		timezonePref.setEntries(timezoneDesc);
		// NOTE: I'm not sure the reason but setDefaultValue() seems to have no effect here
		if(getTimezone(this).equals("")) {
			timezonePref.setValue(TimeZone.getDefault().getID());
		}
		updateSummary(null);
		setResult(RESULT_OK);
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
		setResult(RESULT_FIRST_USER);
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
			String id = getPreferenceScreen().getSharedPreferences().getString(timezone_key, "");
			timezonePref.setSummary(getTimezoneDesc(id, " "));
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