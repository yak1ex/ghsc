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

// TODO: Account settings
// TODO: Design e.g. background
// TODO: Keep values for rotation etc.

public class MainActivity extends Activity implements Handler.Callback {

	class Status
	{
		public boolean success;
		public String error;
		public int days;
		public boolean done;
		public int left_hour, left_min, left_sec;
	}

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
		update();
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
		RequestRunnable(Handler handler) {
			mHandler = handler;
		}
		public void run() {
			Message m = new Message();
			m.obj = scrape();
			mHandler.sendMessage(m);
		}
		private Status scrape() {
			Status ret = new Status();

			try {
				Document d = Jsoup.connect("https://github.com/yak1ex/").get();
				String s = d.getElementsByAttributeValueContaining("class", "contrib-streak-current").text();
				Pattern p = Pattern.compile("(\\d+) days (\\w+) (\\d+) - (\\w+) (\\d+) Current Streak");
				Matcher m = p.matcher(s);
				m.find();
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

	public boolean handleMessage(Message msg) {
		Status s = (Status)msg.obj;
		TextView view = (TextView)findViewById(R.id.textView1);
		if(s.success) {
			TextView viewDays = (TextView)findViewById(R.id.textViewDays);
			viewDays.setText(Integer.toString(s.days));
			TextView viewBy = (TextView)findViewById(R.id.textViewBy);
			if(s.done) {
				viewBy.setText(getResources().getText(R.string.done));
			} else {
				viewBy.setText(getResources().getString(R.string.hms, s.left_hour, s.left_min, s.left_sec));
			}
			view.setText(getResources().getText(R.string.tap_to_update));
		} else {
			view.setText(s.error);
		}
		return true;
	}

	public void update() {
		TextView view = (TextView)findViewById(R.id.textView1);
		view.setText(getResources().getText(R.string.updating));
		new Thread(new RequestRunnable(new Handler(this))).start();
	}

}
