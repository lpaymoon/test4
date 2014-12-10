package com.umpay.hfweb.util;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;


/**
 * ****************** 类说明 *********************
 * class       :  ApplicationProperties
 * @author     :  David
 * @version    :  1.0  
 * description :  properties holder
 * @see        :                        
 * ***********************************************
 */

public class ApplicationProperties {
	private static Logger log = Logger.getLogger(ApplicationProperties.class);

	public  MessageSource messageSource;

	public ApplicationProperties(MessageSource messageSource) {
		
		this.messageSource = messageSource;
	}

	public  String getProproperty(String key) {
		return getMessage(key, null);
	}

	public  String getMessage(String key, String[] args) {
		if (messageSource == null) {
			log.error("messageSource in ApplicationProperties is null");
			throw new RuntimeException("messageSource in ApplicationProperties is null");
		}
		String rtn = null;
		try {
			rtn = trim(messageSource.getMessage(key, args, null));
		} catch (Exception nsme) {
			log.warn("getLocalMsg() key[" + key + "] Error:" + nsme.toString());
		}
		return rtn;

	}
	
	public  String trim(String obj){
    	return obj!=null? obj.trim():"";
    }
}