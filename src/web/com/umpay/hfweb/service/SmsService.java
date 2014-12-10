package com.umpay.hfweb.service;

import java.util.Map;

import com.umpay.hfweb.action.param.SmsParam;

public interface SmsService {
	/**
	 * ********************************************
	 * method name   : pushSms 
	 * description   : 下发常规短信
	 * @return       : void
	 * @param        : @param merId
	 * @param        : @param mobileId
	 * @param        : @param smsContent
	 * modified      : yangwr ,  Nov 6, 2011  3:58:54 PM
	 * @see          : 
	 * *******************************************
	 */
	public boolean pushSms(String merId,String mobileId,String smsContent);
	/**
	 * ********************************************
	 * method name   : pushPaySms 
	 * description   : 重发确认支付短信
	 * @return       : boolean
	 * @param        : @param smsMap
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 8, 2011 8:39:22 PM
	 * @see          : 
	 * *******************************************
	 */
	public boolean pushPaySms(Map<String, String> smsMap);
	/**
	 * ********************************************
	 * method name   : sendUserOrderLtdSms 
	 * description   : 发送用户第一次达到下单频率阀值提醒短信
	 * @return       : boolean
	 * @param        : @param mobileId 手机号
	 * @see          : 
	 * *******************************************
	 */
	public boolean sendUserOrderLtdSms(String mobileId);
	/**
	 * ********************************************
	 * method name   : genSmsInfo 
	 * description   : 组装发送短信相关内容
	 * @return       : SmsParam
	 * @param        : @param servType
	 * @param        : @param servMonth
	 * @param        : @param goodsId
	 * @param        : @param porderId
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 22, 2011 11:17:05 AM
	 * @see          : 
	 * *******************************************
	 */
	public SmsParam genSmsInfo(String servType, String servMonth, String goodsId, String porderId);
	/**
	 * ********************************************
	 * method name   : genSmsInfo 
	 * description   : 组装发送短信相关内容(重构迁移相关变更,特殊商户发送19开头的短信子号)
	 * @return       : SmsParam
	 * @param        : @param servType
	 * @param        : @param servMonth
	 * @param        : @param goodsId
	 * @param        : @param porderId
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 22, 2011 11:17:05 AM
	 * @see          : 
	 * *******************************************
	 */
	public SmsParam genSmsInfoSpecial(String servType, String servMonth, String goodsId, String porderId,String merId);
	
	/**
	 * ********************************************
	 * method name   : alarm 
	 * description   : 报警功能
	 * @return       : void
	 * @param        : @param out        : 
	 * *******************************************
	 */
	public void alarm(Map<String,Object> out);
	/**
	 * ********************************************
	 * method name   : pushCaptchaSms 
	 * description   : 下发验证码短信
	 * @return       : boolean
	 * @param        : @param smsMap
	 * @param        : @return
	 * modified      : LiZhen ,  2013-5-27  上午11:53:40
	 * @see          : 
	 * *******************************************
	 */
	public boolean pushCaptchaSms(Map<String,String> smsMap);
	/**
	 * ********************************************
	 * method name   : pushCaptchaSms 
	 * description   : 下发支付成功短信
	 * @return       : boolean
	 * @param        : @param smsMap
	 * @param        : @return
	 * modified      : LiZhen ,  2013-5-27  上午11:53:40
	 * @see          : 
	 * *******************************************
	 */
	public boolean pushPayOkSms(Map<String,Object> smsMap);
	
	/**
	 * ********************************************
	 * method name   : pushRandomKeySms 
	 * description   : 下发无线验证码短信
	 * @return       : boolean
	 * @param        : @param smsMap
	 * @param        : @return
	 * modified      : LiZhen ,  2014-10-22 上午11:53:40
	 * @see          : 
	 * *******************************************
	 */
	public boolean pushRandomKeySms(Map<String,Object> smsMap);
}
