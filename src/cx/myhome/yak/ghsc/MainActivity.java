package cx.myhome.yak.ghsc;

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

public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.button1) {
			TextView view = (TextView)findViewById(R.id.textView1);
			view.setText("Test");

			try {
				Document d = Jsoup.connect("https://github.com/yak1ex/").get();
				view.setText(d.getElementsByAttributeValueContaining("class", "contrib-streak-current").text());
			} catch (Exception e) {
				view.setText(e.toString());
				e.printStackTrace();
				return;
			}
			//view.setText(res.getEntity().getContentType().getValue());
		}
	}

}
