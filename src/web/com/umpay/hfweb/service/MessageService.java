package com.umpay.hfweb.service;

public interface MessageService {
	public String getSystemParam(String key);
	public String getSystemParam(String key,String defaultValue);
	public String getMessage(String key);
	public String getMessageDetail(String key);
}
