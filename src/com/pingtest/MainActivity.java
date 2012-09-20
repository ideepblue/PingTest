package com.pingtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView start;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initUI();
	}

	private void initUI() {
		start = (TextView) findViewById(R.id.start);
		start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Runtime run = Runtime.getRuntime();
				Process proc = null;
				try {
					String str = "ping -c 5 -i 0.2 -W 1 10.0.0.1";
					System.out.println(str);
					proc = run.exec(str);
					InputStreamReader ir = new InputStreamReader(proc
							.getInputStream());
					LineNumberReader input = new LineNumberReader(ir);
					String line;

					int result = proc.waitFor();
					if (result == 0) {
						Toast.makeText(MainActivity.this, "ping连接成功",
								Toast.LENGTH_SHORT).show();
						while ((line = input.readLine()) != null) {
							Log.v("AA", line);
						}

					} else {
						Toast.makeText(MainActivity.this, "ping测试失败",
								Toast.LENGTH_SHORT).show();
						Log.v("AA", "failed");
						while ((line = input.readLine()) != null) {
							Log.v("AA", line);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					// proc.destroy();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
