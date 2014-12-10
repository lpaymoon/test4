package com.umpay.hfweb.action.param;

public class SmsParam {

	private String smsFrom;
	
	private String smsSub;
	
	private String smsPswd;
	
	private int sendCount;
	
	private String servType;
	
	private String servMonth;

	public String getSmsFrom() {
		return smsFrom;
	}

	public void setSmsFrom(String smsFrom) {
		this.smsFrom = smsFrom;
	}

	public String getSmsSub() {
		return smsSub;
	}

	public void setSmsSub(String smsSub) {
		this.smsSub = smsSub;
	}

	public String getSmsPswd() {
		return smsPswd;
	}

	public void setSmsPswd(String smsPswd) {
		this.smsPswd = smsPswd;
	}

	public int getSendCount() {
		return sendCount;
	}

	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}

	public String getServType() {
		return servType;
	}

	public void setServType(String servType) {
		this.servType = servType;
	}

	public String getServMonth() {
		return servMonth;
	}

	public void setServMonth(String servMonth) {
		this.servMonth = servMonth;
	}

}
