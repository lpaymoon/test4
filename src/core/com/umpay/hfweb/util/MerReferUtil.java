package com.umpay.hfweb.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.umpay.hfweb.action.command.PageOrderCmd;

public class MerReferUtil {
	
	public static final String REST_RTN_REFER_KEY = "merrefer";
	
	// alarmtype：1代表有手机号请求的url   2代表无手机号请求的url   3代表爬虫关键字   
	public static final String ALARM_URL_HAS_MOBILE = "1";
	public static final String ALARM_URL_NO_MOBILE = "2";
	public static final String ALARM_KEY = "3";
	
	// URL验证结果   1合法   0 非法
	public static final String REFER_VALIDATED = "1";
	public static final String REFER_INVALIDATED = "0";
	
	/**
	 * 从资源层返回的Map中抽取refer集合
	 * @return
	 */
	public synchronized static List<String> getReferListFromMap(List<Map<String, Object>> list){
		List<String> rtnList = new LinkedList<String>();
		
		for(Map<String, Object> map : list){
			Set<String> keySet = map.keySet();
			Iterator<String> it = keySet.iterator();
			while(it.hasNext()){
				String key = it.next();
				// 将refer字段值取出
				if("refer".equalsIgnoreCase(key)){
					String o = (String) map.get(key);
					rtnList.add(o);
				}
			}
		}
		
		return rtnList;
	}
	
	/**
	 * 过滤url
	 * @param rfer ： 当前请求url
	 * @param cacheReferList ： 报备的url
	 * @return 0 ：非法　　　1：正常
	 */
	public synchronized static String filterTheRefer(String refer, List<String> cacheReferList){
		String rtnCode = REFER_INVALIDATED;
		for(String cacheRefer : cacheReferList){
			if(refer.toLowerCase().contains(cacheRefer.toLowerCase())){
				rtnCode = REFER_VALIDATED;
				break;
			}
		}
		return rtnCode;
	}
	
	public synchronized static String logFilterRefer(PageOrderCmd cmd, String refer, String rtnCode, String funCode, String alarmType){
		Logger referLog = Logger.getLogger("HFMERREFER");
		String logInfo = funCode + "," + rtnCode + "," + cmd.getMerId() + "," + cmd.getGoodsId() + "," + cmd.getGoodsName() + "," + cmd.getOrderId() + "," + ObjectUtil.trim(cmd.getMobileId()) + "," + alarmType + "," + refer;
		referLog.info(logInfo);
		return logInfo;
	}
}
