package com.umpay.hfweb.model;

import java.io.Serializable;

public class Advertisement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4797972177125615949L;
	private String adName;
	private String adUrl;
	public String getAdName() {
		return adName;
	}
	public void setAdName(String adName) {
		this.adName = adName;
	}
	public String getAdUrl() {
		return adUrl;
	}
	public void setAdUrl(String adUrl) {
		this.adUrl = adUrl;
	}
}
