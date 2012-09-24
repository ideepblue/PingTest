package com.pingtest;

import org.json.JSONException;
import org.json.JSONObject;

public class CellInfo extends JSONObject {
	public static String OK = "ok";
	public static String FAILED = "failed";
	
	public long id;
	public CellInfo() {
		super();
		
		this.id = 0;
		
		try {
			// 通常固定值
			this.put("phoneType", "bad");
			this.put("deviceId", "bad");
			this.put("line1Number", "bad");
			this.put("networkOperator", "bad");
			this.put("simSerialNumber", "bad");
			// 变化值
			this.put("dataState", "bad");
			this.put("networkType", "bad");
			this.put("serviceState", "bad");
			this.put("signalStrengthsGSM", -2);
			this.put("signalStrengthsCDMA", -2);
			this.put("signalStrengthsEVDO", -2);
			this.put("gpsTimestamp", 0);
			this.put("gpsLatitude", "bad");
			this.put("gpsLongitude", "bad");
			this.put("gpsAccuracy", "bad");
			this.put("agpsTimestamp", 0);
			this.put("agpsLatitude", "bad");
			this.put("agpsLongitude", "bad");
			this.put("agpsAccuracy", "bad");
			//PING
			this.put("result", "bad");
			this.put("timestamp", 0);
			this.put("packetsTransmitted", "bad");
			this.put("packetsReceived", "bad");
			this.put("packetLoss", "bad");
			this.put("pingTime", "bad");
			this.put("min", "bad");
			this.put("max", "bad");
			this.put("avg", "bad");
			this.put("mdev", "bad");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public CellInfo lock() {
		CellInfo cell;
		try {
			cell = new CellInfo(this.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			cell = new CellInfo();
		}
		return cell;
	}
	
	public CellInfo(String content) throws JSONException {
		super(content);
	}
}
