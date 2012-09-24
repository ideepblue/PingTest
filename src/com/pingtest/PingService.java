package com.pingtest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Iterator;

import org.json.JSONException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class PingService extends Service {

	private AlarmManager alarmManager;
	private LocationManager locationManager;
	private TelephonyManager telephonyManager;

	private PhoneStateListener phoneStateListener;

	private PingRecevier pingReceiver;

	private CellInfo cellInfo;
	
	private DatabaseOperator dbo;

	private static int TIME_REPEAT_PING = 60 * 1000;
	private static int LOCATION_UPDATE_MIN_TIME = 5 * 1000;
	private static int LOCATION_UPDATE_MIN_DISTANCE = 10;
	private static String SERVER_ADDRESS = "jia.bit.edu.cn";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		initManager();
		initReceiver();
		initPhoneStateListener();
		initData();
		Log.i("PingService", "startPingService");
		this.start();
	}

	@Override
	public void onDestroy() {
		PingService.this.stop();
		this.unregisterReceiver(pingReceiver);
		Log.i("PingService", "stopPingService");
		this.stop();
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
				if (signalStrength.isGsm()) {
					// SET
					try {
						cellInfo.put("signalStrengthsGSM",
								signalStrength.getGsmSignalStrength() * 2 - 113);
						cellInfo.put("signalStrengthsCDMA", -1);
						cellInfo.put("signalStrengthsEVDO", -1);
					} catch (JSONException e) {
						e.printStackTrace();
					}

				} else {
					// SET
					try {
						cellInfo.put("signalStrengthsGSM", -1);
						cellInfo.put("signalStrengthsCDMA",
								signalStrength.getCdmaDbm());
						cellInfo.put("signalStrengthsEVDO",
								signalStrength.getEvdoDbm());
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onDataConnectionStateChanged(int state, int networkType) {
				Log.v("PhoneStateListener", "DataConnectionStateChanged");
				String str = new String();

				switch (state) {
				case TelephonyManager.DATA_CONNECTED:
					str = "DATA_CONNECTED";
					break;
				case TelephonyManager.DATA_CONNECTING:
					str = "DATA_CONNECTING";
					break;
				case TelephonyManager.DATA_DISCONNECTED:
					str = "DATA_DISCONNECTED";
					break;
				case TelephonyManager.DATA_SUSPENDED:
					str = "DATA_SUSPENDED";
					break;
				}
				// SET
				try {
					cellInfo.put("dataState", str);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				switch (networkType) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					str = "NETWORK_TYPE_1xRTT";
					break;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					str = "NETWORK_TYPE_CDMA";
					break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					str = "NETWORK_TYPE_EDGE";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_EHRPD: str +=
				 * "NETWORK_TYPE_EHRPD\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					str = "NETWORK_TYPE_EVDO_0";
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					str = "NETWORK_TYPE_EVDO_A";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_EVDO_B: str +=
				 * "NETWORK_TYPE_EVDO_B\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_GPRS:
					str = "NETWORK_TYPE_GPRS";
					break;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					str = "NETWORK_TYPE_HSDPA";
					break;
				case TelephonyManager.NETWORK_TYPE_HSPA:
					str = "NETWORK_TYPE_HSPA";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_HSPAP: str +=
				 * "NETWORK_TYPE_HSPAP\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					str = "NETWORK_TYPE_HSUPA";
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
					str = "NETWORK_TYPE_UMTS";
					break;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
					str = "NETWORK_TYPE_UNKNOWN";
					break;
				}

				// SET
				try {
					cellInfo.put("networkType", str);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onServiceStateChanged(ServiceState serviceState) {
				Log.v("PhoneStateListener", "ServiceStateChanged");
				int state = serviceState.getState();
				String str = new String();

				switch (state) {
				case ServiceState.STATE_EMERGENCY_ONLY:
					str = "STATE_EMERGENCY_ONLY";
					break;
				case ServiceState.STATE_IN_SERVICE:
					str = "STATE_IN_SERVICE";
					break;
				case ServiceState.STATE_OUT_OF_SERVICE:
					str = "STATE_OUT_OF_SERVICE";
					break;
				case ServiceState.STATE_POWER_OFF:
					str = "STATE_POWER_OFF";
					break;
				}
				// SET
				try {
					cellInfo.put("serviceState", str);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}

	private void initData() {
		cellInfo = new CellInfo();
		dbo = new DatabaseOperator(this);

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
		// SET
		try {
			cellInfo.put("phoneType", str);
			cellInfo.put("deviceId", telephonyManager.getDeviceId());
			cellInfo.put("line1Number", telephonyManager.getLine1Number());
			cellInfo.put("simSerialNumber",
					telephonyManager.getSimSerialNumber());
			cellInfo.put("networkOperator",
					telephonyManager.getNetworkOperator());
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void start() {
		Toast.makeText(this, "START PINGTEST", Toast.LENGTH_SHORT).show();
		Intent intent;
		PendingIntent pendingIntent;

		// 开启 Ping 定时器
		intent = new Intent(PingtestActions.ACTION_PING);
		pendingIntent = PendingIntent.getBroadcast(PingService.this, 0,
				intent, 0);
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + 2000, TIME_REPEAT_PING,
				pendingIntent);

		// 打开AGPS监听
		intent = new Intent(PingtestActions.ACTION_AGPS_UPDATE);
		pendingIntent = PendingIntent.getBroadcast(PingService.this, 0,
				intent, 0);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_MIN_TIME,
				LOCATION_UPDATE_MIN_DISTANCE, pendingIntent);

		// 打开GPS监听
		intent = new Intent(PingtestActions.ACTION_GPS_UPDATE);
		pendingIntent = PendingIntent.getBroadcast(PingService.this, 0,
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
		Toast.makeText(this, "STOP PINGTEST", Toast.LENGTH_SHORT).show();
		Intent intent;
		PendingIntent pendingIntent;

		// 关闭 Ping 定时器
		intent = new Intent(PingtestActions.ACTION_PING);
		pendingIntent = PendingIntent.getBroadcast(PingService.this, 0,
				intent, 0);
		alarmManager.cancel(pendingIntent);

		// 关闭AGPS监听
		intent = new Intent(PingtestActions.ACTION_AGPS_UPDATE);
		pendingIntent = PendingIntent.getBroadcast(PingService.this, 0,
				intent, 0);
		locationManager.removeUpdates(pendingIntent);

		// 关闭GPS监听
		intent = new Intent(PingtestActions.ACTION_GPS_UPDATE);
		pendingIntent = PendingIntent.getBroadcast(PingService.this, 0,
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

		protected void onPreExecute() {
			cellInfoLock = cellInfo.lock();
			try {
				cellInfoLock.put("timestamp", System.currentTimeMillis());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			PingService.this.sendBroadcast(new Intent(PingtestActions.ACTION_STOP_UPLOAD));
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
			try {
				if (result == 0) {
					// 10 packets transmitted, 10 received, 0% packet loss, time
					// 9008ms
					// rtt min/avg/max/mdev = 49.082/58.336/63.711/4.395 ms

					Log.v("Ping", "ok");
					Log.v("Ping", strResult);

					cellInfoLock.put("result", "ok");

					String[] lines = strResult.split("\n");
					String[] line1 = lines[0].split(", ");
					cellInfoLock.put("packetsTransmitted", line1[0].split(" ")[0]);
					cellInfoLock.put("packetsReceived", line1[1].split(" ")[0]);
					cellInfoLock.put("packetLoss", line1[2].split(" ")[0]);
					cellInfoLock.put("pingTime", line1[3].split(" ")[1]);

					String[] line2 = lines[1].substring(23,
							lines[1].length() - 2).split("/");

					cellInfoLock.put("min", line2[0]);
					cellInfoLock.put("avg", line2[1]);
					cellInfoLock.put("max", line2[2]);
					cellInfoLock.put("mdev", line2[3]);

				} else {
					Log.v("Ping", "failed");
					cellInfoLock.put("result", "failed");
				}

				Iterator iterator = cellInfoLock.keys();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();

					Log.i("R", key + " = " + cellInfoLock.getString(key));
				}
				dbo.insertCellInfo(cellInfoLock);

			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			PingService.this.sendBroadcast(new Intent(PingtestActions.ACTION_START_UPLOAD));

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

				// 检测GPS状态
				boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
				boolean agpsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
				if ((! gpsEnabled) && (! agpsEnabled)) {
					Toast.makeText(PingService.this, PingService.this.getText(R.string.enable_all_gps),
							Toast.LENGTH_LONG).show();
				} else if (! gpsEnabled) {
					Toast.makeText(PingService.this, PingService.this.getText(R.string.enable_gps),
							Toast.LENGTH_LONG).show();
				} else if (! agpsEnabled) {
					Toast.makeText(PingService.this, PingService.this.getText(R.string.enable_agps),
							Toast.LENGTH_LONG).show();
				}
				
				Log.v("Receiver", "Ping");
				try {
					Location location;

					location = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						cellInfo.put("agpsTimestamp", location.getTime());

						cellInfo.put("agpsLatitude",
								df7.format(location.getLatitude()));

						cellInfo.put("agpsLongitude",
								df7.format(location.getLongitude()));
						cellInfo.put("agpsAccuracy",
								df0.format(location.getAccuracy()));
					} else {
						cellInfo.put("agpsTimestamp", -1);

						cellInfo.put("agpsLatitude", -1);

						cellInfo.put("agpsLongitude", -1);
						cellInfo.put("agpsAccuracy", -1);
					}

					location = locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if (location != null) {
						cellInfo.put("gpsTimestamp", location.getTime());
						cellInfo.put("gpsLatitude",
								df7.format(location.getLatitude()));
						cellInfo.put("gpsLongitude",
								df7.format(location.getLongitude()));
						cellInfo.put("gpsAccuracy",
								df0.format(location.getAccuracy()));
					} else {
						cellInfo.put("agpsTimestamp", -1);

						cellInfo.put("agpsLatitude", -1);

						cellInfo.put("agpsLongitude", -1);
						cellInfo.put("agpsAccuracy", -1);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				PingAsyncTask task = new PingAsyncTask();
				task.execute();

			} else if (intent.getAction().equals(
					PingtestActions.ACTION_AGPS_UPDATE)) {

				Log.v("Receiver", "AgpsUpdate");

			} else if (intent.getAction().equals(
					PingtestActions.ACTION_GPS_UPDATE)) {

				Log.v("Receiver", "GpsUpdate");

			}
		}

	}

}
