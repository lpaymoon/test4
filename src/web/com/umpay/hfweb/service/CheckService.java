package com.umpay.hfweb.service;

import com.umpay.hfweb.model.MpspMessage;

public interface CheckService {

	/**
	 * ********************************************
	 * method name   : doCheck 
	 * description   : 校验请求信息 校验依据为参数funcode,以及配置文件validate.properties
	 * @return       : MpspMessage 响应消息
	 * @param        : paraMessage 请求参数消息
	 * modified      : yangwr ,  Nov 2, 2011  11:50:22 AM
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage doCheck(MpspMessage paraMessage);
}
