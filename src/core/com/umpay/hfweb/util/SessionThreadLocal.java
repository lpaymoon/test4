package com.umpay.hfweb.util;

import java.util.HashMap;
import java.util.Map;

/**
 * ******************  类说明  *********************
 * class       :  SessionThreadLocal
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  单次web请求相关的信息存储                      
 * ***********************************************
 */
public class SessionThreadLocal {

	private static final ThreadLocal<Map<String,String>> sessionIdLocal = new ThreadLocal<Map<String,String>>();
	
	public static void setSessionMap(Map<String,String> map){
		sessionIdLocal.set(map);
	}
	
	public static Map<String,String> getSessionMap(){
		Map<String,String> map = sessionIdLocal.get();
		if(null==map){
			map = new HashMap<String,String>();
			setSessionMap(map);
		}
		return map;
	}
	public static void setSessionValue(String key,String value){
		Map<String,String> map = getSessionMap();
		map.put(key, value);
		
	}
	public static void putAll(Map<String,String> map){
		Map<String,String> mapTemp = getSessionMap();
		mapTemp.putAll(map);
	}
	public static String getSessionValue(String key){
		Map<String,String> map = getSessionMap();
		return map.get(key);
	}
}
