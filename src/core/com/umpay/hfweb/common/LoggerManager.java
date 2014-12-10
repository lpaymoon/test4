/**
 * 创建人：杨文荣
 * 创建日期：2011-10-24 20:04
 * 说明：
 */
package com.umpay.hfweb.common;

import org.apache.log4j.Logger;

public class LoggerManager {

	/**简要日志*/
	private static final Logger mpspLog = Logger.getLogger("uniformMpspLog");
	
	/**获取简要日志*/
	public static Logger getMpspLogger(){
		return mpspLog;
	}
	
	
}
