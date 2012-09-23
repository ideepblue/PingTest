package com.pingtest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

	private AlarmManager alarmManager;
	private LocationManager locationManager;
	private TelephonyManager telephonyManager;

	private PhoneStateListener phoneStateListener;

	private PingRecevier pingReceiver;

	private CellInfo cellInfo;
	private PingInfo pingInfo;

	private int pingCount;

	private static int TIME_REPEAT_PING = 60 * 1000;
	private static int LOCATION_UPDATE_MIN_TIME = 5 * 1000;
	private static int LOCATION_UPDATE_MIN_DISTANCE = 10;
	private static String SERVER_ADDRESS = "www.baidu.com";

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
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		MainActivity.this.stop();
		super.onDestroy();
	}

	private void initManager() {
		alarmManager = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
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
					// SET
					cellInfo.signalStrengthsGSM = signalStrength
							.getGsmSignalStrength() * 2 - 113;
					cellInfo.signalStrengthsCDMA = -1;
					cellInfo.signalStrengthsEVDO = -1;
				} else {
					str += "CdmaDbm = " + signalStrength.getCdmaDbm()
							+ ", EvdoDbm = " + signalStrength.getEvdoDbm();
					// SET
					cellInfo.signalStrengthsGSM = -1;
					cellInfo.signalStrengthsCDMA = signalStrength.getCdmaDbm();
					cellInfo.signalStrengthsEVDO = signalStrength.getEvdoDbm();
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
				// SET
				cellInfo.dataState = r;
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
				// SET
				cellInfo.networkType = r;
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
				// SET
				cellInfo.serviceState = r;
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
				MainActivity.this.start();
			}
		});

		textViewEnd = (TextView) findViewById(R.id.end);
		textViewEnd.setText("END");
		textViewEnd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.this.stop();
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
		pingCount = 0;
		cellInfo = new CellInfo();
		pingInfo = new PingInfo();

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
		// SET
		cellInfo.phoneType = str;
		cellInfo.deviceId = telephonyManager.getDeviceId();
		cellInfo.line1Number = telephonyManager.getLine1Number();
		cellInfo.simSerialNumber = telephonyManager.getSimSerialNumber();
		cellInfo.networkOperator = telephonyManager.getNetworkOperator();

	}

	private void start() {
		Intent intent;
		PendingIntent pendingIntent;

		// 开启 Ping 定时器
		intent = new Intent(PingtestActions.ACTION_PING);
		pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
				intent, 0);
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + 2000, TIME_REPEAT_PING, pendingIntent);

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
		intent = new Intent(PingtestActions.ACTION_PING);
		pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
				intent, 0);
		alarmManager.cancel(pendingIntent);

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

	private class PingAsyncTask extends AsyncTask<Void, Void, Void> {

		String strResult;
		int result;
		CellInfo cellInfoLock;
		PingInfo pingInfoLock;

		protected void onPreExecute() {
			cellInfoLock = cellInfo.lock();
			pingInfo.timestamp = System.currentTimeMillis();
		}

		protected Void doInBackground(Void... params) {

			Runtime run = Runtime.getRuntime();
			Process proc = null;
			try {
				String str = "ping -c 10 -i 1 -W 1 " + SERVER_ADDRESS;
				System.out.println(str);
				proc = run.exec(str);
				InputStreamReader ir = new InputStreamReader(
						proc.getInputStream());
				LineNumberReader input = new LineNumberReader(ir);
				String line;
				strResult = new String();

				boolean startRecord = false;
				while ((line = input.readLine()) != null) {
					if (startRecord) {
						strResult += line + "\n";
					}
					if (line.startsWith("---")) {
						startRecord = true;
					}
				}

				result = proc.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally { // proc.destroy();

			}
			return null;
		}

		protected void onPostExecute(Void params) {
			if (result == 0) {
				// 10 packets transmitted, 10 received, 0% packet loss, time
				// 9008ms
				// rtt min/avg/max/mdev = 49.082/58.336/63.711/4.395 ms

				Log.v("Ping", "ok");
				Log.v("Ping", strResult);

				pingInfo.result = "ok";

				String[] lines = strResult.split("\n");
				String[] line1 = lines[0].split(", ");
				pingInfo.packetsTransmitted = line1[0].split(" ")[0];
				pingInfo.packetsReceived = line1[1].split(" ")[0];
				pingInfo.packetLoss = line1[2].split(" ")[0];
				pingInfo.pingTime = line1[3].split(" ")[1];

				String[] line2 = lines[1].substring(23, lines[1].length() - 2)
						.split("/");

				pingInfo.min = line2[0];
				pingInfo.avg = line2[1];
				pingInfo.max = line2[2];
				pingInfo.mdev = line2[3];

			} else {
				Log.v("Ping", "failed");
				pingInfo.result = "failed";
			}
			textViewPing.setText(strResult);
			
			pingInfoLock = pingInfo.lock();

			Log.i("R", pingInfoLock.avg);
			Log.i("R", pingInfoLock.max);
			Log.i("R", pingInfoLock.mdev);
			Log.i("R", pingInfoLock.min);
			Log.i("R", pingInfoLock.packetLoss);
			Log.i("R", pingInfoLock.packetsReceived);
			Log.i("R", pingInfoLock.packetsTransmitted);
			Log.i("R", pingInfoLock.pingTime);
			Log.i("R", pingInfoLock.result);
			Log.i("R", pingInfoLock.timestamp + "");
			
			Log.i("R", cellInfoLock.phoneType);
			Log.i("R", cellInfoLock.deviceId);
			Log.i("R", cellInfoLock.line1Number);
			Log.i("R", cellInfoLock.networkOperator);
			Log.i("R", cellInfoLock.simSerialNumber);
			Log.i("R", cellInfoLock.dataState);
			Log.i("R", cellInfoLock.networkType);
			Log.i("R", cellInfoLock.serviceState);
			Log.i("R", cellInfoLock.signalStrengthsGSM+"");
			Log.i("R", cellInfoLock.signalStrengthsCDMA+"");
			Log.i("R", cellInfoLock.signalStrengthsEVDO+"");
			Log.i("R", cellInfoLock.gpsTimestamp+"");
			Log.i("R", cellInfoLock.gpsLatitude);
			Log.i("R", cellInfoLock.gpsLongitude);
			Log.i("R", cellInfoLock.gpsAccuracy);
			Log.i("R", cellInfoLock.agpsTimestamp+"");
			Log.i("R", cellInfoLock.agpsLatitude);
			Log.i("R", cellInfoLock.agpsLongitude);
			Log.i("R", cellInfoLock.agpsAccuracy);
		}
	}

	private class PingRecevier extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			DecimalFormat df7 = new DecimalFormat("0.0000000");
			DecimalFormat df0 = new DecimalFormat("0");
			if (intent.getAction().equals(PingtestActions.ACTION_PING)) {

				Log.v("Receiver", "Ping");
				pingCount++;
				textViewPause.setText("TIME\n" + calendar.getTime().getHours()
						+ ":" + calendar.getTime().getMinutes() + ":"
						+ calendar.getTime().getSeconds() + "\nPingCount = "
						+ pingCount);

				Location location;

				location = locationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				cellInfo.agpsTimestamp = location.getTime();
				cellInfo.agpsLatitude = df7.format(location.getLatitude());
				cellInfo.agpsLongitude = df7.format(location.getLongitude());
				cellInfo.agpsAccuracy = df0.format(location.getAccuracy());

				location = locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				cellInfo.gpsTimestamp = location.getTime();
				cellInfo.gpsLatitude = df7.format(location.getLatitude());
				cellInfo.gpsLongitude = df7.format(location.getLongitude());
				cellInfo.gpsAccuracy = df0.format(location.getAccuracy());

				PingAsyncTask task = new PingAsyncTask();
				task.execute();

			} else if (intent.getAction().equals(
					PingtestActions.ACTION_AGPS_UPDATE)) {

				Log.v("Receiver", "AgpsUpdate");

				Location location = locationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (location != null) {
					calendar.setTimeInMillis(location.getTime());
					textViewAgps.setText("AGPS" + "\nLatitude = "
							+ df7.format(location.getLatitude()) + "\nLongitude = "
							+ df7.format(location.getLongitude()) + "\nAccuracy = "
							+ df0.format(location.getAccuracy()) + "\nTime = "
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
							+ df7.format(location.getLatitude()) + "\nLongitude = "
							+ df7.format(location.getLongitude()) + "\nAccuracy = "
							+ df0.format(location.getAccuracy()) + "\nTime = "
							+ calendar.getTime().getHours() + ":"
							+ calendar.getTime().getMinutes() + ":"
							+ calendar.getTime().getSeconds());
				}
			}
		}

	}
}
