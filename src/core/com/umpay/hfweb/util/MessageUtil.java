package com.umpay.hfweb.util;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

public class MessageUtil {
	public static Logger log = Logger.getLogger(MessageUtil.class);
	/**
	 * 获得配置信息
	 * 
	 * @param key
	 * @return
	 */
	public static String getLocalProperty(MessageSource source,String key) {
		String rtn = "";
		try{
			rtn = ObjectUtil.trim(source.getMessage(key, null,
					Locale.CHINA));
		}catch(NoSuchMessageException e){
			rtn = "";
			log.debug(e);
		}
		
		log.debug("getLocalMsg() key[" + key + "] localMsg[" + rtn + "]");
		return rtn;
	}
}
