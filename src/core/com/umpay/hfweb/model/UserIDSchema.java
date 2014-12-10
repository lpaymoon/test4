package com.umpay.hfweb.model;

import java.io.Serializable;

public class UserIDSchema implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2402719937466004872L;
	
	private String ID; // 用户标识
	
	private String Type; // 用户标识类型 mobile：手机号;   UCID：用户证书标识;   BUID：用户绑定帐号


	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	
}
