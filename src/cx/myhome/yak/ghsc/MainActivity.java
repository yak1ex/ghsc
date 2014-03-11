package cx.myhome.yak.ghsc;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

// TODO: Design e.g. background

public class MainActivity extends Activity implements Handler.Callback {

	static final String BUNDLE_KEY_SUCCESS = "success";
	static final String BUNDLE_KEY_DAYS = "days";
	static final String BUNDLE_KEY_DONE = "done";
	static final String BUNDLE_KEY_LEFT_HOUR = "hour";
	static final String BUNDLE_KEY_LEFT_MIN = "min";
	static final String BUNDLE_KEY_LEFT_SEC = "sec";
	static final String BUNDLE_KEY_UPDATED = "updated";
	enum Error { NO_ERROR, NETWORK_ERROR, ACCOUNT_ERROR, UNKNOWN_ERROR };
	class Status
	{
		public boolean success;
		public String error;
		public Error error_kind;
		public int days;
		public boolean done;
		public int left_hour, left_min, left_sec;
		public long updated;
	}

	Status mStatus;
	Map<String, Integer> monthName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		monthName = new HashMap<String, Integer>();
		monthName.put("January", 0);
		monthName.put("February", 1);
		monthName.put("March", 2);
		monthName.put("April", 3);
		monthName.put("May", 4);
		monthName.put("June", 5);
		monthName.put("July", 6);
		monthName.put("August", 7);
		monthName.put("September", 8);
		monthName.put("October", 9);
		monthName.put("November", 10);
		monthName.put("December", 11);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mStatus = new Status();
		if(savedInstanceState == null) {
			update();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(BUNDLE_KEY_SUCCESS, mStatus.success);
		if(mStatus.success) {
			outState.putInt(BUNDLE_KEY_DAYS, mStatus.days);
			outState.putBoolean(BUNDLE_KEY_DONE, mStatus.done);
			outState.putInt(BUNDLE_KEY_LEFT_HOUR, mStatus.left_hour);
			outState.putInt(BUNDLE_KEY_LEFT_MIN, mStatus.left_min);
			outState.putInt(BUNDLE_KEY_LEFT_SEC, mStatus.left_sec);
			outState.putLong(BUNDLE_KEY_UPDATED, mStatus.updated);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mStatus.success = savedInstanceState.getBoolean(BUNDLE_KEY_SUCCESS);
		if(mStatus.success) {
			mStatus.days = savedInstanceState.getInt(BUNDLE_KEY_DAYS);
			mStatus.done = savedInstanceState.getBoolean(BUNDLE_KEY_DONE);
			mStatus.left_hour = savedInstanceState.getInt(BUNDLE_KEY_LEFT_HOUR);
			mStatus.left_min = savedInstanceState.getInt(BUNDLE_KEY_LEFT_MIN);
			mStatus.left_sec = savedInstanceState.getInt(BUNDLE_KEY_LEFT_SEC);
			mStatus.updated = savedInstanceState.getLong(BUNDLE_KEY_UPDATED);
			updateView();
		} else {
			update();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		updateTime();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			update();
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		return false;
	}

	class RequestRunnable implements Runnable
	{
		private Handler mHandler;
		private String mAccount;
		private String mTimezone;
		RequestRunnable(Handler handler, String account, String timezone) {
			mHandler = handler;
			mAccount = account;
			mTimezone = timezone;
		}
		public void run() {
			Message m = new Message();
			m.obj = scrape();
			mHandler.sendMessage(m);
		}
		private Status scrape() {
			Status ret = new Status();

			try {
				Document d = Jsoup.connect("https://github.com/" + mAccount).get();
				String s = d.getElementsByAttributeValueContaining("class", "contrib-streak-current").text();
				Pattern p = Pattern.compile("(\\d+) days (\\w+) (\\d+) - (\\w+) (\\d+) Current Streak");
				Matcher m = p.matcher(s);
				boolean matches = m.find(); 
				if(!matches && !s.equals("0 days Rock - Hard Place Current Streak")) {
					ret.error = "Unknown string gotten: " + s;
					ret.error_kind = Error.UNKNOWN_ERROR;
					ret.success = false;
					return ret;
				}

				TimeZone tz = TimeZone.getTimeZone(mTimezone);
				Calendar c = Calendar.getInstance(tz);
				ret.updated = c.getTimeInMillis();
				Calendar c2 = (Calendar)c.clone();
				c2.set(Calendar.HOUR_OF_DAY, 0);
				c2.set(Calendar.MINUTE, 0);
				c2.set(Calendar.SECOND, 0);

				if(matches) {
					ret.days = Integer.parseInt(m.group(1));
					Calendar c3 = (Calendar)c2.clone();
					c3.set(Calendar.DATE, Integer.parseInt(m.group(5)));
					c3.set(Calendar.MONTH,  monthName.get(m.group(4)));
					ret.done = c2.equals(c3);
				}

				c2.add(Calendar.DATE, 1);
				long diff = c2.getTimeInMillis() - c.getTimeInMillis();
				ret.left_sec = (int)((diff / 1000) % 60);
				ret.left_min = (int)((diff / 1000 / 60) % 60);
				ret.left_hour = (int)(diff / 1000 / 60 / 60);

				ret.success = true;
			} catch(HttpStatusException e) {
				ret.error = e.toString();
				ret.error_kind = Error.ACCOUNT_ERROR;
				ret.success = false;
			} catch(IOException e) {
				ret.error = e.toString();
				ret.error_kind = Error.NETWORK_ERROR;
				ret.success = false;
			} catch (Exception e) {
				ret.error = e.toString();
				ret.error_kind = Error.UNKNOWN_ERROR;
				ret.success = false;
			}
			return ret;
		}
	}

	private void updateView()
	{
		TextView viewDays = (TextView)findViewById(R.id.textViewDays);
		viewDays.setText(Integer.toString(mStatus.days));

		TextView viewLabelToKeep = (TextView)findViewById(R.id.textViewLabelToKeep);
		TextView viewBy = (TextView)findViewById(R.id.textViewBy);
		TextView viewLabelWillStart = (TextView)findViewById(R.id.textViewLabelWillStart);
		TextView viewWillStart = (TextView)findViewById(R.id.textViewWillStart);
		if(mStatus.done) {
			viewLabelToKeep.setText(getResources().getText(R.string.label_to_keep_done));
			viewBy.setText(getResources().getText(R.string.done));
			viewLabelWillStart.setText(getResources().getText(R.string.label_will_start));
			viewWillStart.setText(getResources().getString(R.string.hms, mStatus.left_hour, mStatus.left_min, mStatus.left_sec));
		} else {
			viewLabelToKeep.setText(getResources().getText(R.string.label_to_keep));
			viewBy.setText(getResources().getString(R.string.hms, mStatus.left_hour, mStatus.left_min, mStatus.left_sec));
			viewLabelWillStart.setText("");
			viewWillStart.setText("");
		}
	}

	private void updateTime()
	{
		TextView viewLastUpdate = (TextView)findViewById(R.id.textViewLastUpdate);
		if(!mStatus.success) {
			viewLastUpdate.setText(getResources().getText(R.string.last_update));
			return;
		}
		String lastUpdate;
		TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
		long diff = Calendar.getInstance(tz).getTimeInMillis() - mStatus.updated;
		if(diff / 1000 >= 24 * 3600) {
			 lastUpdate = (String)getResources().getString(R.string.last_update_days, diff / 1000 / 24 / 3600);
		} else if(diff / 1000 >= 3600) {
			 lastUpdate = (String)getResources().getString(R.string.last_update_hours, diff / 1000 / 3600);
		} else if(diff / 1000 >= 60) {
			 lastUpdate = (String)getResources().getString(R.string.last_update_minutes, diff / 1000 / 60);
		} else {
			 lastUpdate = (String)getResources().getString(R.string.last_update_seconds, diff / 1000);
		}
		viewLastUpdate.setText(lastUpdate);
	}

	public boolean handleMessage(Message msg) {
		Status s = (Status)msg.obj;
		if(s.success) {
			mStatus = s;
			updateView();
			updateTime();
			Toast.makeText(this, R.string.updated, Toast.LENGTH_LONG).show();
		} else {
			switch(s.error_kind) {
			case NETWORK_ERROR:
				Toast.makeText(this, "Failed to access GitHub", Toast.LENGTH_LONG).show();
				break;
			case ACCOUNT_ERROR:
				Toast.makeText(this, "Account name not found", Toast.LENGTH_LONG).show();
				break;
			case UNKNOWN_ERROR:
				Toast.makeText(this, "Unknown error occurred", Toast.LENGTH_LONG).show();
				break;
			case NO_ERROR: // not reached
				break;
			}
			Log.d("MainActivity", s.error);
		}
		return true;
	}

	public void update() {
		String account = SettingsActivity.getAccount(this);
		String timezone = SettingsActivity.getTimezone(this);
		if(account.equals("") || timezone.equals("")) {
			Toast.makeText(this, R.string.need_account, Toast.LENGTH_SHORT).show();
			startActivity(new Intent(this, SettingsActivity.class));
		} else {
			Toast.makeText(this, R.string.updating, Toast.LENGTH_SHORT).show();
			new Thread(new RequestRunnable(new Handler(this), account, timezone)).start();
		}
	}

}
