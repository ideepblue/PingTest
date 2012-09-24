package com.pingtest;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView textViewStart;
	private TextView textViewEnd;
	private TextView textViewSignalStrengths;
	private TextView textViewPhoneType;
	private TextView textViewServiceState;
	private TextView textViewNetworkType;
	private TextView textViewGps;
	private TextView textViewAgps;
	private TextView textViewPing;
	private TextView textViewPause;

	// private AlarmManager alarmManager;
	private LocationManager locationManager;
	private TelephonyManager telephonyManager;

	private PhoneStateListener phoneStateListener;

	private PingRecevier pingReceiver;

	// private CellInfo cellInfo;

	private DatabaseOperator dbo;

	private int pingHistroyCount;

	// private static int TIME_REPEAT_PING = 60 * 1000;
	private static int LOCATION_UPDATE_MIN_TIME = 5 * 1000;
	private static int LOCATION_UPDATE_MIN_DISTANCE = 10;

	// private static String SERVER_ADDRESS = "jia.bit.edu.cn";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initManager();
		initReceiver();
		initPhoneStateListener();
		initUI();
		initData();
	}

	@Override
	protected void onPause() {
		MainActivity.this.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// Service Running Check
		ActivityManager activityManager = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);

		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		boolean isRunning = false;
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(
					"com.pingtest.PingService") == true) {
				textViewPing.setText(this.getString(R.string.service_running));
				textViewPing.setTextColor(this.getResources().getColor(
						R.color.text_blue));
				isRunning = true;
			}
		}
		if (!isRunning) {
			textViewPing.setText(this.getString(R.string.service_stopped));
			textViewPing.setTextColor(this.getResources().getColor(
					R.color.text_red));
		}

		// PingHistoryCount
		pingHistroyCount = dbo.queryCount();
		Log.v("PingHistoryCount", pingHistroyCount + "");

		// START
		MainActivity.this.start();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// MainActivity.this.stop();
		this.unregisterReceiver(pingReceiver);
		super.onDestroy();
	}

	private void initManager() {
		// alarmManager = (AlarmManager) this
		// .getSystemService(Context.ALARM_SERVICE);
		telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
	}

	private void initReceiver() {
		pingReceiver = new PingRecevier();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PingtestActions.ACTION_PING);
		intentFilter.addAction(PingtestActions.ACTION_AGPS_UPDATE);
		intentFilter.addAction(PingtestActions.ACTION_GPS_UPDATE);
		this.registerReceiver(pingReceiver, intentFilter);
	}

	private void initPhoneStateListener() {

		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				Log.v("PhoneStateListener", "SignalStrengthsChanged");
				String str = new String();
				if (signalStrength.isGsm()) {
					str += "GsmDbm = "
							+ (signalStrength.getGsmSignalStrength() * 2 - 113);

				} else {
					str += "CdmaDbm = " + signalStrength.getCdmaDbm()
							+ ", EvdoDbm = " + signalStrength.getEvdoDbm();
				}
				textViewSignalStrengths.setText(MainActivity.this
						.getString(R.string.signal_strengths) + str);
			}

			@Override
			public void onDataConnectionStateChanged(int state, int networkType) {
				Log.v("PhoneStateListener", "DataConnectionStateChanged");
				String str = new String();
				String r = new String();

				str += MainActivity.this.getString(R.string.data_state);
				switch (state) {
				case TelephonyManager.DATA_CONNECTED:
					r = "DATA_CONNECTED";
					break;
				case TelephonyManager.DATA_CONNECTING:
					r = "DATA_CONNECTING";
					break;
				case TelephonyManager.DATA_DISCONNECTED:
					r = "DATA_DISCONNECTED";
					break;
				case TelephonyManager.DATA_SUSPENDED:
					r = "DATA_SUSPENDED";
					break;
				}
				str += r + "\n"
						+ MainActivity.this.getString(R.string.network_type);
				switch (networkType) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					r = "NETWORK_TYPE_1xRTT";
					break;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					r = "NETWORK_TYPE_CDMA";
					break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					r = "NETWORK_TYPE_EDGE";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_EHRPD: str +=
				 * "NETWORK_TYPE_EHRPD\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					r = "NETWORK_TYPE_EVDO_0";
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					r = "NETWORK_TYPE_EVDO_A";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_EVDO_B: str +=
				 * "NETWORK_TYPE_EVDO_B\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_GPRS:
					r = "NETWORK_TYPE_GPRS";
					break;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					r = "NETWORK_TYPE_HSDPA";
					break;
				case TelephonyManager.NETWORK_TYPE_HSPA:
					r = "NETWORK_TYPE_HSPA";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_HSPAP: str +=
				 * "NETWORK_TYPE_HSPAP\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					r = "NETWORK_TYPE_HSUPA";
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
					r = "NETWORK_TYPE_UMTS";
					break;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
					r = "NETWORK_TYPE_UNKNOWN";
					break;
				}
				str += r;
				textViewNetworkType.setText(str);
			}

			@Override
			public void onServiceStateChanged(ServiceState serviceState) {
				Log.v("PhoneStateListener", "ServiceStateChanged");
				int state = serviceState.getState();
				String str = new String();
				String r = new String();
				str = MainActivity.this.getString(R.string.service_state);
				switch (state) {
				case ServiceState.STATE_EMERGENCY_ONLY:
					r = "STATE_EMERGENCY_ONLY";
					break;
				case ServiceState.STATE_IN_SERVICE:
					r = "STATE_IN_SERVICE";
					break;
				case ServiceState.STATE_OUT_OF_SERVICE:
					r = "STATE_OUT_OF_SERVICE";
					break;
				case ServiceState.STATE_POWER_OFF:
					r = "STATE_POWER_OFF";
					break;
				}
				str += r;
				textViewServiceState.setText(str);
			}
		};
	}

	private void initUI() {
		textViewStart = (TextView) findViewById(R.id.start);
		textViewStart.setText("START");
		textViewStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "START PINGTEST",
						Toast.LENGTH_SHORT).show();
				MainActivity.this.startService(new Intent(MainActivity.this,
						PingService.class));
				textViewPing.setText(MainActivity.this
						.getString(R.string.service_running));
				textViewPing.setTextColor(MainActivity.this.getResources()
						.getColor(R.color.text_blue));
				// MainActivity.this.start();
			}
		});

		textViewEnd = (TextView) findViewById(R.id.end);
		textViewEnd.setText("END");
		textViewEnd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "STOP PINGTEST",
						Toast.LENGTH_SHORT).show();
				MainActivity.this.stopService(new Intent(MainActivity.this,
						PingService.class));
				textViewPing.setText(MainActivity.this
						.getString(R.string.service_stopped));
				textViewPing.setTextColor(MainActivity.this.getResources()
						.getColor(R.color.text_red));
				// MainActivity.this.stop();
			}
		});

		textViewSignalStrengths = (TextView) findViewById(R.id.signalStrengths);
		textViewPhoneType = (TextView) findViewById(R.id.phoneType);
		textViewServiceState = (TextView) findViewById(R.id.serviceState);
		textViewNetworkType = (TextView) findViewById(R.id.networkType);
		textViewGps = (TextView) findViewById(R.id.gps);
		textViewAgps = (TextView) findViewById(R.id.agps);
		textViewPing = (TextView) findViewById(R.id.ping);
		textViewPause = (TextView) findViewById(R.id.pause);
	}

	private void initData() {
		dbo = new DatabaseOperator(this);
		pingHistroyCount = 0;

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
		textViewPhoneType.setText(MainActivity.this
				.getString(R.string.phone_type) + str);
	}

	private void start() {

		Intent intent;
		PendingIntent pendingIntent;

		// 开启 Ping 定时器
		// intent = new Intent(PingtestActions.ACTION_PING);
		// pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
		// intent, 0);
		// alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
		// SystemClock.elapsedRealtime() + 2000, TIME_REPEAT_PING,
		// pendingIntent);

		// 打开AGPS监听
		intent = new Intent(PingtestActions.ACTION_AGPS_UPDATE);
		pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
				intent, 0);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_MIN_TIME,
				LOCATION_UPDATE_MIN_DISTANCE, pendingIntent);

		// 打开GPS监听
		intent = new Intent(PingtestActions.ACTION_GPS_UPDATE);
		pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
				intent, 0);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE,
				pendingIntent);

		// 打开Telephony监听
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
						| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
						| PhoneStateListener.LISTEN_SERVICE_STATE);
	}

	private void stop() {

		Intent intent;
		PendingIntent pendingIntent;

		// 关闭 Ping 定时器

		// intent = new Intent(PingtestActions.ACTION_PING);
		// pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
		// intent, 0);
		// alarmManager.cancel(pendingIntent);

		// 关闭AGPS监听
		intent = new Intent(PingtestActions.ACTION_AGPS_UPDATE);
		pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
				intent, 0);
		locationManager.removeUpdates(pendingIntent);

		// 关闭GPS监听
		intent = new Intent(PingtestActions.ACTION_GPS_UPDATE);
		pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
				intent, 0);
		locationManager.removeUpdates(pendingIntent);

		// 关闭Telephony监听
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_NONE);
	}

	// private class PingAsyncTask extends AsyncTask<Void, Void, Void> {
	//
	// String strResult;
	// int result;
	// String cellInfoLock;
	// String pingInfoLock;
	//
	// protected void onPreExecute() {
	// cellInfoLock = cellInfo.toString();
	// try {
	// cellInfo.put("timestamp", System.currentTimeMillis());
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// protected Void doInBackground(Void... params) {
	//
	// Runtime run = Runtime.getRuntime();
	// Process proc = null;
	// try {
	// String str = "ping -c 10 -i 1 -W 1 " + SERVER_ADDRESS;
	// System.out.println(str);
	// proc = run.exec(str);
	// InputStreamReader ir = new InputStreamReader(
	// proc.getInputStream());
	// LineNumberReader input = new LineNumberReader(ir);
	// String line;
	// strResult = new String();
	//
	// boolean startRecord = false;
	// while ((line = input.readLine()) != null) {
	// if (startRecord) {
	// strResult += line + "\n";
	// }
	// if (line.startsWith("---")) {
	// startRecord = true;
	// }
	// }
	//
	// result = proc.waitFor();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// } finally { // proc.destroy();
	//
	// }
	// return null;
	// }
	//
	// protected void onPostExecute(Void params) {
	// try {
	// if (result == 0) {
	// // 10 packets transmitted, 10 received, 0% packet loss, time
	// // 9008ms
	// // rtt min/avg/max/mdev = 49.082/58.336/63.711/4.395 ms
	//
	// Log.v("Ping", "ok");
	// Log.v("Ping", strResult);
	//
	// cellInfo.put("result", "ok");
	//
	// String[] lines = strResult.split("\n");
	// String[] line1 = lines[0].split(", ");
	// cellInfo.put("packetsTransmitted", line1[0].split(" ")[0]);
	// cellInfo.put("packetsReceived", line1[1].split(" ")[0]);
	// cellInfo.put("packetLoss", line1[2].split(" ")[0]);
	// cellInfo.put("pingTime", line1[3].split(" ")[1]);
	//
	// String[] line2 = lines[1].substring(23,
	// lines[1].length() - 2).split("/");
	//
	// cellInfo.put("min", line2[0]);
	// cellInfo.put("avg", line2[1]);
	// cellInfo.put("max", line2[2]);
	// cellInfo.put("mdev", line2[3]);
	//
	// } else {
	// Log.v("Ping", "failed");
	// cellInfo.put("result", "failed");
	// }
	//
	// textViewPing.setText(strResult);
	//
	// Iterator iterator = cellInfo.keys();
	// while (iterator.hasNext()) {
	// String key = (String) iterator.next();
	//
	// Log.i("R", key + " = " + cellInfo.getString(key));
	// }
	// Log.i("R", cellInfoLock);
	//
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	//
	// }
	// }

	private class PingRecevier extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			DecimalFormat df7 = new DecimalFormat("0.0000000");
			DecimalFormat df0 = new DecimalFormat("0");
			if (intent.getAction().equals(PingtestActions.ACTION_PING)) {

				Log.v("Receiver", "Ping");
				pingHistroyCount++;
				textViewPause.setText("TIME\n"
						+ (calendar.getTime().getYear() + 1900) + "-"
						+ (calendar.getTime().getMonth() + 1) + "-"
						+ calendar.getTime().getDate() + " "
						+ calendar.getTime().getHours() + ":"
						+ calendar.getTime().getMinutes() + ":"
						+ calendar.getTime().getSeconds()
						+ "\nPingHistoryCount = " + pingHistroyCount);
				// PingAsyncTask task = new PingAsyncTask();
				// task.execute();

			} else if (intent.getAction().equals(
					PingtestActions.ACTION_AGPS_UPDATE)) {

				Log.v("Receiver", "AgpsUpdate");

				Location location = locationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (location != null) {
					calendar.setTimeInMillis(location.getTime());
					textViewAgps.setText("AGPS" + "\nLatitude = "
							+ df7.format(location.getLatitude())
							+ "\nLongitude = "
							+ df7.format(location.getLongitude())
							+ "\nAccuracy = "
							+ df0.format(location.getAccuracy()) + "\nT "
							+ (calendar.getTime().getYear() + 1900) + "-"
							+ (calendar.getTime().getMonth() + 1) + "-"
							+ calendar.getTime().getDate() + " "
							+ calendar.getTime().getHours() + ":"
							+ calendar.getTime().getMinutes() + ":"
							+ calendar.getTime().getSeconds());
				}
			} else if (intent.getAction().equals(
					PingtestActions.ACTION_GPS_UPDATE)) {

				Log.v("Receiver", "GpsUpdate");

				Location location = locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location != null) {
					calendar.setTimeInMillis(location.getTime());
					textViewGps.setText("GPS" + "\nLatitude = "
							+ df7.format(location.getLatitude())
							+ "\nLongitude = "
							+ df7.format(location.getLongitude())
							+ "\nAccuracy = "
							+ df0.format(location.getAccuracy()) + "\nT "
							+ (calendar.getTime().getYear() + 1900) + "-"
							+ (calendar.getTime().getMonth() + 1) + "-"
							+ calendar.getTime().getDate() + " "
							+ calendar.getTime().getHours() + ":"
							+ calendar.getTime().getMinutes() + ":"
							+ calendar.getTime().getSeconds());
				}
			}
		}

	}
}
