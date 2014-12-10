package com.umpay.hfweb.model;

import java.io.Serializable;

public class SIAP implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4899335982258841882L;
	
	private SIAPHeader SIAPHeader;
	private SIAPBody SIAPBody;
	public SIAPHeader getSIAPHeader() {
		return SIAPHeader;
	}
	public void setSIAPHeader(SIAPHeader sIAPHeader) {
		SIAPHeader = sIAPHeader;
	}
	public SIAPBody getSIAPBody() {
		return SIAPBody;
	}
	public void setSIAPBody(SIAPBody sIAPBody) {
		SIAPBody = sIAPBody;
	}
	
	

	
}
