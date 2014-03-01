package cx.myhome.yak.ghsc;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	static String getAccount(Context context) {
		String key = (String)context.getText(R.string.account_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
	}
}