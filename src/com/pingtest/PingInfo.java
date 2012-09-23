package com.pingtest;

public class PingInfo {
	public String result;
	public long timestamp;
	public String packetsTransmitted;
	public String packetsReceived;
	public String packetLoss;
	public String pingTime;
	public String min;
	public String max;
	public String avg;
	public String mdev;
	public PingInfo() {
		this.result = "bad";
		this.timestamp = 0;
		this.packetsTransmitted = "bad";
		this.packetsReceived = "bad";
		this.packetLoss = "bad";
		this.pingTime = "bad";
		this.min = "bad";
		this.max = "bad";
		this.avg = "bad";
		this.mdev = "bad";
	}
	
	
	public PingInfo lock() {
		PingInfo lock = new PingInfo();
		lock.result = result;
		lock.timestamp = timestamp;
		lock.packetsTransmitted = packetsTransmitted;
		lock.packetsReceived = packetsReceived;
		lock.packetLoss = packetLoss;
		lock.pingTime = pingTime;
		lock.min = min;
		lock.max = max;
		lock.avg = avg;
		lock.mdev = mdev;
		return lock;
	}
}
