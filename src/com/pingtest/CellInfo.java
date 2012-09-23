package com.pingtest;

public class CellInfo {

	// 通常固定值
	public String phoneType;
	public String deviceId;
	public String line1Number;
	public String networkOperator;
	public String simSerialNumber;
	
	// 变化值
	public String dataState;
	public String networkType;
	public String serviceState;
	public int signalStrengthsGSM;
	public int signalStrengthsCDMA;
	public int signalStrengthsEVDO;
	public long gpsTimestamp;
	public String gpsLatitude;
	public String gpsLongitude;
	String gpsAccuracy;
	long agpsTimestamp;
	public String agpsLatitude;
	public String agpsLongitude;
	public String agpsAccuracy;
	public CellInfo() {
		super();
		this.phoneType = "bad";
		this.deviceId = "bad";
		this.line1Number = "bad";
		this.networkOperator = "bad";
		this.simSerialNumber = "bad";
		this.dataState = "bad";
		this.networkType = "bad";
		this.serviceState = "bad";
		this.signalStrengthsGSM = -2;
		this.signalStrengthsCDMA = -2;
		this.signalStrengthsEVDO = -2;
		this.gpsTimestamp = 0;
		this.gpsLatitude = "bad";
		this.gpsLongitude = "bad";
		this.gpsAccuracy = "bad";
		this.agpsTimestamp = 0;
		this.agpsLatitude = "bad";
		this.agpsLongitude = "bad";
		this.agpsAccuracy = "bad";
	}
	
	public CellInfo lock() {
		CellInfo lock = new CellInfo();
		lock.phoneType = phoneType;
		lock.deviceId = deviceId;
		lock.line1Number = line1Number;
		lock.networkOperator = networkOperator;
		lock.simSerialNumber = simSerialNumber;
		lock.dataState = dataState;
		lock.networkType = networkType;
		lock.serviceState = serviceState;
		lock.signalStrengthsGSM = signalStrengthsGSM;
		lock.signalStrengthsCDMA = signalStrengthsCDMA;
		lock.signalStrengthsEVDO = signalStrengthsEVDO;
		lock.gpsTimestamp = gpsTimestamp;
		lock.gpsLatitude = gpsLatitude;
		lock.gpsLongitude = gpsLongitude;
		lock.gpsAccuracy = gpsAccuracy;
		lock.agpsTimestamp = agpsTimestamp;
		lock.agpsLatitude = agpsLatitude;
		lock.agpsLongitude = agpsLongitude;
		lock.agpsAccuracy = agpsAccuracy;
		return lock;
	}
}
