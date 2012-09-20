package com.pingtest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView textViewStart;
	private TextView textViewGsmDbm;
	private TextView textViewGsmAsu;
	private TextView textViewLatitude;
	private TextView textViewLongitude;
	private TextView textViewPing;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private TelephonyManager telephonyManager;
	private PhoneStateListener phoneStateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initUI();
		initPhoneListener();
		initLocationListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_NONE);
		locationManager.removeUpdates(locationListener);
	}

	@Override
	protected void onResume() {

		super.onResume();
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000,
				10, locationListener);
	}

	private void initUI() {
		textViewStart = (TextView) findViewById(R.id.start);
		textViewStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Runtime run = Runtime.getRuntime();
				Process proc = null;
				try {
					String str = "ping -c 5 -i 0.2 -W 1 www.baidu.com";
					System.out.println(str);
					proc = run.exec(str);
					InputStreamReader ir = new InputStreamReader(proc
							.getInputStream());
					LineNumberReader input = new LineNumberReader(ir);
					String line;
					String strResult;
					strResult = new String();

					int result = proc.waitFor();
					if (result == 0) {
						Toast.makeText(MainActivity.this, "ping连接成功",
								Toast.LENGTH_SHORT).show();
						while ((line = input.readLine()) != null) {
							Log.v("AA", line);
							strResult += line;
						}

					} else {
						Toast.makeText(MainActivity.this, "ping测试失败",
								Toast.LENGTH_SHORT).show();
						Log.v("AA", "failed");
						while ((line = input.readLine()) != null) {
							Log.v("AA", line);
							strResult += line;
						}
					}
					textViewPing.setText(strResult);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					// proc.destroy();
				}
			}
		});

		textViewGsmAsu = (TextView) findViewById(R.id.gsmAsu);
		textViewGsmDbm = (TextView) findViewById(R.id.gsmDbm);
		textViewLatitude = (TextView) findViewById(R.id.latitude);
		textViewLongitude = (TextView) findViewById(R.id.longitude);
		textViewPing = (TextView) findViewById(R.id.ping);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void initPhoneListener() {
		telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);
				textViewGsmAsu.setText("GSM_ASU = "
						+ signalStrength.getGsmSignalStrength());
				textViewGsmDbm.setText("GSM_dBm = "
						+ (signalStrength.getGsmSignalStrength() * 2 - 113));
			}
		};
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	private void initLocationListener() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {

				textViewLatitude
						.setText("Latitude = " + location.getLatitude());
				textViewLongitude.setText("Longitude = "
						+ location.getLongitude());
			}

			@Override
			public void onProviderDisabled(String provider) {

				textViewLongitude.setText("Disabled");
			}

			@Override
			public void onProviderEnabled(String provider) {

				textViewLongitude.setText("Enabled");
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				switch (status) {
				case LocationProvider.OUT_OF_SERVICE:
					textViewLatitude.setText("OUT_OF_SERVICE");
					break;
				case LocationProvider.AVAILABLE:
					textViewLatitude.setText("AVAILABLE");
					break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					textViewLatitude.setText("TEMPORARILY_UNAVAILABLE");
					break;
				default:
					textViewLatitude.setText("DEFAULT");
					break;
				}

			}

		};
		Location location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		// 第一次获得设备的位置
		if (location != null) {
			Log.v("AA",
					"l1 = " + location.getLatitude() + ", L = "
							+ location.getLongitude());
			locationListener.onLocationChanged(location);
		}

		// 重要函数，监听数据测试
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000,
				10, locationListener);
	}
}
