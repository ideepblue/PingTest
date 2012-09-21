package com.pingtest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Calendar;

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
	private TextView textViewSignalStrengths;
	private TextView textViewPhoneType;
	private TextView textViewNetworkType;
	private TextView textViewDataActivity;
	private TextView textViewLocationStatus;
	private TextView textViewGps;
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
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_DATA_ACTIVITY);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, 10, locationListener);
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

		textViewSignalStrengths = (TextView) findViewById(R.id.signalStrengths);
		textViewPhoneType = (TextView) findViewById(R.id.phoneType);
		textViewNetworkType = (TextView) findViewById(R.id.networkType);
		textViewDataActivity = (TextView) findViewById(R.id.dataActivity);
		textViewLocationStatus = (TextView) findViewById(R.id.locationStatus);
		textViewGps = (TextView) findViewById(R.id.gps);
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

		String str = new String();
		int phoneType = telephonyManager.getPhoneType();
		switch (phoneType) {
		case TelephonyManager.PHONE_TYPE_NONE:
			str = "PHONE_TYPE_NONE";
			break;
		case TelephonyManager.PHONE_TYPE_CDMA:
			str = "PHONE_TYPE_CDMA";
			break;
		case TelephonyManager.PHONE_TYPE_GSM:
			str = "PHONE_TYPE_GSM";
			break;
		/*
		 * case TelephonyManager.PHONE_TYPE_SIP: str = "PHONE_TYPE_SIP"; break;
		 */
		}
		textViewPhoneType.setText(str);

		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);
				String str = new String();
				if (signalStrength.isGsm()) {
					str += "isGSM\n";
					str += "GsmAsu = " + signalStrength.getGsmSignalStrength()
							+ "\n" + "GsmDbm = "
							+ (signalStrength.getGsmSignalStrength() * 2 - 113);
				} else {
					str += "CdmaDbm = " + signalStrength.getCdmaDbm() + "\n"
							+ "EvdoDbm = " + signalStrength.getEvdoDbm();
				}
				textViewSignalStrengths.setText(str);
			}

			@Override
			public void onDataActivity(int direction) {
				super.onDataActivity(direction);
				String str = new String();
				switch (direction) {
				case TelephonyManager.DATA_ACTIVITY_DORMANT:
					str += "DATA_ACTIVITY_DORMANT\n";
					break;
				case TelephonyManager.DATA_ACTIVITY_IN:
					str += "DATA_ACTIVITY_IN\n";
					break;
				case TelephonyManager.DATA_ACTIVITY_INOUT:
					str += "DATA_ACTIVITY_INOUT\n";
					break;
				case TelephonyManager.DATA_ACTIVITY_NONE:
					str += "DATA_ACTIVITY_NONE\n";
					break;
				case TelephonyManager.DATA_ACTIVITY_OUT:
					str += "DATA_ACTIVITY_OUT\n";
					break;
				}
				textViewDataActivity.setText(str);
			}

			@Override
			public void onDataConnectionStateChanged(int state, int networkType) {
				super.onDataConnectionStateChanged(state, networkType);
				String str = new String();
				switch (state) {
				case TelephonyManager.DATA_CONNECTED:
					str += "DATA_CONNECTED\n";
					break;
				case TelephonyManager.DATA_CONNECTING:
					str += "DATA_CONNECTING\n";
					break;
				case TelephonyManager.DATA_DISCONNECTED:
					str += "DATA_DISCONNECTED\n";
					break;
				case TelephonyManager.DATA_SUSPENDED:
					str += "DATA_SUSPENDED\n";
					break;
				}
				switch (networkType) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					/*
					 * str += "NETWORK_TYPE_1xRTT\n" break;
					 */
				case TelephonyManager.NETWORK_TYPE_CDMA:
					str += "NETWORK_TYPE_CDMA\n";
					break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					str += "NETWORK_TYPE_EDGE\n";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_EHRPD: str +=
				 * "NETWORK_TYPE_EHRPD\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					str += "NETWORK_TYPE_EVDO_0\n";
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					str += "NETWORK_TYPE_EVDO_A\n";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_EVDO_B: str +=
				 * "NETWORK_TYPE_EVDO_B\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_GPRS:
					str += "NETWORK_TYPE_GPRS\n";
					break;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					str += "NETWORK_TYPE_HSDPA\n";
					break;
				case TelephonyManager.NETWORK_TYPE_HSPA:
					str += "NETWORK_TYPE_HSPA\n";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_HSPAP: str +=
				 * "NETWORK_TYPE_HSPAP\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					str += "NETWORK_TYPE_HSUPA\n";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_IDEN: str +=
				 * "NETWORK_TYPE_IDEN\n"; break;
				 */
				/*
				 * case TelephonyManager.NETWORK_TYPE_LTE: str +=
				 * "NETWORK_TYPE_LTE\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_UMTS:
					str += "NETWORK_TYPE_UMTS\n";
					break;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
					str += "NETWORK_TYPE_UNKNOWN\n";
					break;
				}
				textViewNetworkType.setText(str);
			}
		};
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_DATA_ACTIVITY);
	}

	private void initLocationListener() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {

				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				textViewGps.setText(
						"Latitude = " + location.getLatitude() + 
						"\nLongitude = "
						+ location.getLongitude() + "\nTime = "
						+ calendar.getTime().getHours() + ":"
						+ calendar.getTime().getMinutes() + ":"
						+ calendar.getTime().getSeconds());
			}

			@Override
			public void onProviderDisabled(String provider) {

				textViewLocationStatus.setText("Disabled");
			}

			@Override
			public void onProviderEnabled(String provider) {

				textViewLocationStatus.setText("Enabled");
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				switch (status) {
				case LocationProvider.OUT_OF_SERVICE:
					textViewLocationStatus.setText("OUT_OF_SERVICE");
					break;
				case LocationProvider.AVAILABLE:
					textViewLocationStatus.setText("AVAILABLE");
					break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					textViewLocationStatus.setText("TEMPORARILY_UNAVAILABLE");
					break;
				default:
					textViewLocationStatus.setText("DEFAULT");
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
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, 10, locationListener);
	}
}
