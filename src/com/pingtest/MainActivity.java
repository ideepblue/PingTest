package com.pingtest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView textViewStart;
	private TextView textViewSignalStrengths;
	private TextView textViewPhoneType;
	private TextView textViewServiceState;
	private TextView textViewNetworkType;
	private TextView textViewDataActivity;
	private TextView textViewGpsStatus;
	private TextView textViewAgpsStatus;
	private TextView textViewGps;
	private TextView textViewAgps;
	private TextView textViewCell;
	private TextView textViewGoogleLocation;
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
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
						| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
						| PhoneStateListener.LISTEN_DATA_ACTIVITY
						| PhoneStateListener.LISTEN_CELL_LOCATION
						| PhoneStateListener.LISTEN_SERVICE_STATE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				3000, 10, locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 3000, 10, locationListener);
	}

	private void initUI() {
		textViewStart = (TextView) findViewById(R.id.start);
		textViewStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AsyncTask<Void, Void, Void>() {
					String strResult;
					int result;

					@Override
					protected Void doInBackground(Void... params) {
						Runtime run = Runtime.getRuntime();
						Process proc = null;
						try {
							String str = "ping -c 30 -i 0.2 -W 1 www.baidu.com";
							System.out.println(str);
							proc = run.exec(str);
							InputStreamReader ir = new InputStreamReader(proc
									.getInputStream());
							LineNumberReader input = new LineNumberReader(ir);
							String line;
							strResult = new String();

							boolean startRecord = false;
							while ((line = input.readLine()) != null) {
								Log.v("AA", line);
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
						} finally {
							// proc.destroy();
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void params) {
						if (result == 0) {
							Toast.makeText(MainActivity.this, "ping连接成功",
									Toast.LENGTH_SHORT).show();
							Log.v("AA", "ok");

						} else {
							Toast.makeText(MainActivity.this, "ping测试失败",
									Toast.LENGTH_SHORT).show();
							Log.v("AA", "failed");
						}
						textViewPing.setText(strResult);

					}

				}.execute();

			}
		});

		textViewSignalStrengths = (TextView) findViewById(R.id.signalStrengths);
		textViewPhoneType = (TextView) findViewById(R.id.phoneType);
		textViewServiceState = (TextView) findViewById(R.id.serviceState);
		textViewNetworkType = (TextView) findViewById(R.id.networkType);
		textViewDataActivity = (TextView) findViewById(R.id.dataActivity);
		textViewGpsStatus = (TextView) findViewById(R.id.gpsStatus);
		textViewAgpsStatus = (TextView) findViewById(R.id.agpsStatus);
		textViewGoogleLocation = (TextView) findViewById(R.id.googleLocation);
		textViewGps = (TextView) findViewById(R.id.gps);
		textViewAgps = (TextView) findViewById(R.id.agps);
		textViewCell = (TextView) findViewById(R.id.cell);
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
		textViewPhoneType.setText(MainActivity.this
				.getString(R.string.phone_type) + str);

		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				// super.onSignalStrengthsChanged(signalStrength);
				Log.v("BB", "EE");
				String str = new String();
				if (signalStrength.isGsm()) {
					str += "GsmAsu = " + signalStrength.getGsmSignalStrength()
							+ ", GsmDbm = "
							+ (signalStrength.getGsmSignalStrength() * 2 - 113);
				} else {
					str += "CdmaDbm = " + signalStrength.getCdmaDbm()
							+ ", EvdoDbm = " + signalStrength.getEvdoDbm();
				}
				textViewSignalStrengths.setText(MainActivity.this
						.getString(R.string.signal_strengths) + str);
			}

			@Override
			public void onDataActivity(int direction) {
				// super.onDataActivity(direction);
				Log.v("BB", "DD");
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
				textViewDataActivity.setText(MainActivity.this
						.getString(R.string.data_activity) + str);
			}

			@Override
			public void onDataConnectionStateChanged(int state, int networkType) {
				// super.onDataConnectionStateChanged(state, networkType);
				Log.v("BB", "CC");
				String str = new String();
				str += MainActivity.this.getString(R.string.data_state);
				switch (state) {
				case TelephonyManager.DATA_CONNECTED:
					str += "DATA_CONNECTED";
					break;
				case TelephonyManager.DATA_CONNECTING:
					str += "DATA_CONNECTING";
					break;
				case TelephonyManager.DATA_DISCONNECTED:
					str += "DATA_DISCONNECTED";
					break;
				case TelephonyManager.DATA_SUSPENDED:
					str += "DATA_SUSPENDED";
					break;
				}
				str += "\n"
						+ MainActivity.this.getString(R.string.network_type);
				switch (networkType) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					str += "NETWORK_TYPE_1xRTT";
					break;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					str += "NETWORK_TYPE_CDMA";
					break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					str += "NETWORK_TYPE_EDGE";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_EHRPD: str +=
				 * "NETWORK_TYPE_EHRPD\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					str += "NETWORK_TYPE_EVDO_0";
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					str += "NETWORK_TYPE_EVDO_A";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_EVDO_B: str +=
				 * "NETWORK_TYPE_EVDO_B\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_GPRS:
					str += "NETWORK_TYPE_GPRS";
					break;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					str += "NETWORK_TYPE_HSDPA";
					break;
				case TelephonyManager.NETWORK_TYPE_HSPA:
					str += "NETWORK_TYPE_HSPA";
					break;
				/*
				 * case TelephonyManager.NETWORK_TYPE_HSPAP: str +=
				 * "NETWORK_TYPE_HSPAP\n"; break;
				 */
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					str += "NETWORK_TYPE_HSUPA";
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
					str += "NETWORK_TYPE_UMTS";
					break;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
					str += "NETWORK_TYPE_UNKNOWN";
					break;
				}
				textViewNetworkType.setText(str);
			}

			@Override
			public void onServiceStateChanged(ServiceState serviceState) {
				Log.v("BB", "BB");
				int state = serviceState.getState();
				String str = new String();
				str = MainActivity.this.getString(R.string.service_state);
				switch (state) {
				case ServiceState.STATE_EMERGENCY_ONLY:
					str += "STATE_EMERGENCY_ONLY";
					break;
				case ServiceState.STATE_IN_SERVICE:
					str += "STATE_IN_SERVICE";
					break;
				case ServiceState.STATE_OUT_OF_SERVICE:
					str += "STATE_OUT_OF_SERVICE";
					break;
				case ServiceState.STATE_POWER_OFF:
					str += "STATE_POWER_OFF";
					break;
				}
				textViewServiceState.setText(str);
			}

			@Override
			public void onCellLocationChanged(CellLocation location) {
				int phoneType = telephonyManager.getPhoneType();
				GoogleLocationAsyncTask task = new GoogleLocationAsyncTask();
				if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
					// GSM
					GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
					textViewCell.setText("Lac = " + gsmCellLocation.getLac()
							+ "\nCid = " + gsmCellLocation.getCid() + "\nNO = "
							+ telephonyManager.getNetworkOperator());

					task.execute(0, gsmCellLocation.getCid(),
							gsmCellLocation.getLac());

				} else {
					// CDMA
					CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
					textViewCell.setText("Lac = "
							+ cdmaCellLocation.getNetworkId() + "\nCid = "
							+ cdmaCellLocation.getBaseStationId() + "\nMNC = "
							+ cdmaCellLocation.getSystemId() + "\nNO = "
							+ telephonyManager.getNetworkOperator());

					task.execute(1, cdmaCellLocation.getBaseStationId(),
							cdmaCellLocation.getNetworkId(),
							cdmaCellLocation.getSystemId());

				}
			}

		};

		// ServiceState serviceState = new ServiceState();
		// phoneStateListener.onServiceStateChanged(serviceState);

		// phoneStateListener.onCellLocationChanged(telephonyManager
		// .getCellLocation());
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
						| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
						| PhoneStateListener.LISTEN_DATA_ACTIVITY
						| PhoneStateListener.LISTEN_CELL_LOCATION
						| PhoneStateListener.LISTEN_SERVICE_STATE);
	}

	private void initLocationListener() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {

				if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
					// GPS
					Log.v("CC", "BB");
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(location.getTime());
					textViewGps.setText("GPS" + "\nLatitude = "
							+ location.getLatitude() + "\nLongitude = "
							+ location.getLongitude() + "\nAccuracy = "
							+ location.getAccuracy() + "\nTime = "
							+ calendar.getTime().getHours() + ":"
							+ calendar.getTime().getMinutes() + ":"
							+ calendar.getTime().getSeconds());
				} else {
					// AGPS
					Log.v("CC", "DD");
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(location.getTime());
					textViewAgps.setText("AGPS" + "\nLatitude = "
							+ location.getLatitude() + "\nLongitude = "
							+ location.getLongitude() + "\nAccuracy = "
							+ location.getAccuracy() + "\nTime = "
							+ calendar.getTime().getHours() + ":"
							+ calendar.getTime().getMinutes() + ":"
							+ calendar.getTime().getSeconds());
				}
			}

			@Override
			public void onProviderDisabled(String provider) {
				if (provider.equals(LocationManager.GPS_PROVIDER)) {
					// GPS
					textViewGpsStatus.setText(MainActivity.this
							.getString(R.string.gps_status) + "Disabled");
				} else {
					// AGPS
					textViewAgpsStatus.setText(MainActivity.this
							.getString(R.string.agps_status) + "Disabled");
				}
			}

			@Override
			public void onProviderEnabled(String provider) {

				if (provider.equals(LocationManager.GPS_PROVIDER)) {
					// GPS
					textViewGpsStatus.setText(MainActivity.this
							.getString(R.string.gps_status) + "Enabled");
				} else {
					// AGPS
					textViewAgpsStatus.setText(MainActivity.this
							.getString(R.string.agps_status) + "Enabled");
				}
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				Log.v("CC", "AA");
				String str = new String();
				switch (status) {
				case LocationProvider.OUT_OF_SERVICE:
					str += "OUT_OF_SERVICE";
					break;
				case LocationProvider.AVAILABLE:
					str += "AVAILABLE";
					break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					str += "TEMPORARILY_UNAVAILABLE";
					break;
				default:
					str += "DEFAULT";
					break;
				}
				if (provider.equals(LocationManager.GPS_PROVIDER)) {
					// GPS
					textViewGpsStatus.setText(MainActivity.this
							.getString(R.string.gps_status) + str);
				} else {
					// AGPS
					textViewAgpsStatus.setText(MainActivity.this
							.getString(R.string.agps_status) + str);
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
		location = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		// 第一次获得设备的位置
		if (location != null) {
			Log.v("AA",
					"l1 = " + location.getLatitude() + ", L = "
							+ location.getLongitude());
			locationListener.onLocationChanged(location);
		}
		// 重要函数，监听数据测试
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				3000, 10, locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 3000, 10, locationListener);
	}

	private class GoogleLocationAsyncTask extends
			AsyncTask<Integer, Void, Void> {
		HttpClient httpClient;
		HttpPost httpPost;
		String result = "";
		int code;

		@Override
		protected void onPreExecute() {
			HttpParams httpParams = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
			HttpConnectionParams.setSoTimeout(httpParams, 5000);

			httpClient = new DefaultHttpClient(httpParams);
			httpPost = new HttpPost("http://www.google.com/loc/json");

		}

		@Override
		protected Void doInBackground(Integer... params) {

			int type = params[0];
			JSONObject holder = new JSONObject();
			try {
				if (type == 0) {
					// GSM
					holder.put("version", "1.1.0");
					// holder.put("radio_type", "gsm");
					holder.put("request_address", true);
					holder.put("address_language", "zh_CN");

					JSONObject current_data;

					JSONArray array = new JSONArray();

					current_data = new JSONObject();
					current_data.put("cell_id", params[1]);
					current_data.put("location_area_code", params[2]);
					array.put(current_data);

					holder.put("cell_towers", array);

				} else {
					// CMDA
					holder.put("version", "1.1.0");
					holder.put("radio_type", "cdma");
					holder.put("request_address", true);
					holder.put("address_language", "zh_CN");

					JSONObject current_data;

					JSONArray array = new JSONArray();

					current_data = new JSONObject();
					current_data.put("cell_id", params[1]);
					current_data.put("location_area_code", params[2]);
					current_data.put("mobile_network_code", params[3]);
					array.put(current_data);

					holder.put("cell_towers", array);
				}

				httpPost.setEntity(new StringEntity(holder.toString()));

				HttpResponse response = httpClient.execute(httpPost);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
						&& response.getEntity() != null) {
					code = HttpStatus.SC_OK;
					result = EntityUtils.toString(response.getEntity(), "UTF8");
				} else {
					code = response.getStatusLine().getStatusCode();
					result = "";
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void params) {
			if (code == HttpStatus.SC_OK) {
				String str = new String();
				try {
					JSONObject json = new JSONObject(result);
					if (json.length() == 0) {
						str = "GoogleReturnNull";
					} else {
						JSONObject location = json.getJSONObject("location");
						JSONObject address = location.getJSONObject("address");
						str += "GoogleReturn:" + "\nLatitude = "
								+ location.getString("latitude")
								+ "\nLongitude = "
								+ location.getString("longitude")
								+ "\nAccuracy = "
								+ location.getString("accuracy") + "\n"
								+ address.getString("country")
								+ address.getString("region")
								+ address.getString("city")
								+ address.getString("street");
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				textViewGoogleLocation.setText(str);
			} else {
				textViewGoogleLocation.setText("code = " + code);
			}
		}
	}
}
