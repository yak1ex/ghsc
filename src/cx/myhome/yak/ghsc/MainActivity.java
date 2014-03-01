package cx.myhome.yak.ghsc;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Menu;
import android.widget.TextView;

// TODO: Design e.g. background

public class MainActivity extends Activity implements Handler.Callback {

	static final String BUNDLE_KEY_SUCCESS = "success";
	static final String BUNDLE_KEY_DAYS = "days";
	static final String BUNDLE_KEY_DONE = "done";
	static final String BUNDLE_KEY_LEFT_HOUR = "hour";
	static final String BUNDLE_KEY_LEFT_MIN = "min";
	static final String BUNDLE_KEY_LEFT_SEC = "sec";
	class Status
	{
		public boolean success;
		public String error;
		public int days;
		public boolean done;
		public int left_hour, left_min, left_sec;
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
			if(!mStatus.done){
				outState.putInt(BUNDLE_KEY_LEFT_HOUR, mStatus.left_hour);
				outState.putInt(BUNDLE_KEY_LEFT_MIN, mStatus.left_min);
				outState.putInt(BUNDLE_KEY_LEFT_SEC, mStatus.left_sec);
			}
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mStatus.success = savedInstanceState.getBoolean(BUNDLE_KEY_SUCCESS);
		if(mStatus.success) {
			mStatus.days = savedInstanceState.getInt(BUNDLE_KEY_DAYS);
			mStatus.done = savedInstanceState.getBoolean(BUNDLE_KEY_DONE);
			if(!mStatus.done){
				mStatus.left_hour = savedInstanceState.getInt(BUNDLE_KEY_LEFT_HOUR);
				mStatus.left_min = savedInstanceState.getInt(BUNDLE_KEY_LEFT_MIN);
				mStatus.left_sec = savedInstanceState.getInt(BUNDLE_KEY_LEFT_SEC);
			}
			updateView();
		} else {
			update();
		}
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
		RequestRunnable(Handler handler, String account) {
			mHandler = handler;
			mAccount = account;
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
// FIXME: Handle 404, probably wrong account name
// FIXME: Handle network error
				String s = d.getElementsByAttributeValueContaining("class", "contrib-streak-current").text();
				Pattern p = Pattern.compile("(\\d+) days (\\w+) (\\d+) - (\\w+) (\\d+) Current Streak");
				Matcher m = p.matcher(s);
				m.find();
// FIXME: Check if matched or not
// FIXME: Handle 0 day
//     0 days Rock - Hard Place Current Streak

				ret.days = Integer.parseInt(m.group(1));

				TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
				Calendar c = Calendar.getInstance(tz);
				Calendar c2 = (Calendar)c.clone();
				c2.set(Calendar.HOUR_OF_DAY, 0);
				c2.set(Calendar.MINUTE, 0);
				c2.set(Calendar.SECOND, 0);

				Calendar c3 = (Calendar)c2.clone();
				c3.set(Calendar.DATE, Integer.parseInt(m.group(5)));
				c3.set(Calendar.MONTH,  monthName.get(m.group(4)));
				ret.done = c2.equals(c3);

				c2.add(Calendar.DATE, 1);
				long diff = c2.getTimeInMillis() - c.getTimeInMillis();
				ret.left_sec = (int)((diff / 1000) % 60);
				ret.left_min = (int)((diff / 1000 / 60) % 60);
				ret.left_hour = (int)(diff / 1000 / 60 / 60);

				ret.success = true;
			} catch (Exception e) {
				ret.error = e.toString();
				ret.success = false;
			}
			return ret;
		}
	}

	private void updateView()
	{
		TextView viewDays = (TextView)findViewById(R.id.textViewDays);
		viewDays.setText(Integer.toString(mStatus.days));
		TextView viewBy = (TextView)findViewById(R.id.textViewBy);
		if(mStatus.done) {
			viewBy.setText(getResources().getText(R.string.done));
		} else {
			viewBy.setText(getResources().getString(R.string.hms, mStatus.left_hour, mStatus.left_min, mStatus.left_sec));
		}
		TextView view = (TextView)findViewById(R.id.textView1);
		view.setText(getResources().getText(R.string.tap_to_update));
	}

	public boolean handleMessage(Message msg) {
		Status s = (Status)msg.obj;
		if(s.success) {
			mStatus = s;
			updateView();
		} else {
			TextView view = (TextView)findViewById(R.id.textView1);
			view.setText(s.error);
		}
		return true;
	}

	public void update() {
		TextView view = (TextView)findViewById(R.id.textView1);
		view.setText(getResources().getText(R.string.updating));
		new Thread(new RequestRunnable(new Handler(this), SettingsActivity.getAccount(this))).start();
	}

}
