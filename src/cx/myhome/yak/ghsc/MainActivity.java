package cx.myhome.yak.ghsc;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

// TODO: Account settings
// TODO: Design e.g. background
// TODO: Keep values for rotation etc.

public class MainActivity extends Activity implements OnClickListener {

	class Status
	{
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

		Button button = (Button)findViewById(R.id.button1);
		button.setOnClickListener(this);

// FIXME: Move accessing network to another thread
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	Status scrape() throws IOException {
		Status ret = new Status();

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
		return ret;
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.button1) {
			TextView view = (TextView)findViewById(R.id.textView1);
			try {
// TODO: Set on init
				Status s = scrape();
				TextView viewDays = (TextView)findViewById(R.id.textViewDays);
				viewDays.setText(Integer.toString(s.days));
				TextView viewBy = (TextView)findViewById(R.id.textViewBy);
				if(s.done) {
					viewBy.setText("Already done!!");
				} else {
					viewBy.setText(String.format("%dh%dm%ds", s.left_hour, s.left_min, s.left_sec));
				}
			} catch (Exception e) {
				view.setText(e.toString());
				e.printStackTrace();
				return;
			}
		}
	}

}
