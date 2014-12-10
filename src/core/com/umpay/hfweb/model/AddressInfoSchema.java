package com.umpay.hfweb.model;

import java.io.Serializable;

public class AddressInfoSchema implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -565583350110391878L;

	private String DeviceType;
	
	private String DeviceID;

	public String getDeviceType() {
		return DeviceType;
	}

	public void setDeviceType(String deviceType) {
		DeviceType = deviceType;
	}

	public String getDeviceID() {
		return DeviceID;
	}

	public void setDeviceID(String deviceID) {
		DeviceID = deviceID;
	}
	
	
}
