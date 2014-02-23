package cx.myhome.yak.ghsc;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;

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

			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet g = new HttpGet("https://github.com/yak1ex");
			HttpResponse res = null;
			try {
				res = client.execute(g);
			} catch (Exception e) {
				view.setText(e.toString());
				e.printStackTrace();
				return;
			}
			view.setText(res.getStatusLine().toString());
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder b = dbf.newDocumentBuilder();
				Document d = b.parse(res.getEntity().getContent());

				XPathFactory xf = XPathFactory.newInstance();
				XPath xp = xf.newXPath();

				view.setText(xp.evaluate("//div[contains(@class, 'contrib-streak-current')]/span/text()", d));
			} catch (Exception e) {
				view.setText(e.toString());
				e.printStackTrace();
				return;
			}
			//view.setText(res.getEntity().getContentType().getValue());
		}
	}

}
