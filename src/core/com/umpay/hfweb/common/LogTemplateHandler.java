/**
 * filename   :   LogTemplateHandler.java
 * owner      :   zhaowei
 * copyright  :   UMPAY
 * description:
 * modified   :   2009-7-6
 */
package com.umpay.hfweb.common;

import java.util.Map;
import java.util.StringTokenizer;

import com.umpay.hfweb.util.ObjectUtil;


/*
 * class       :  LogTemplateHandler.java
 * @author     :  zhaowei
 * @version    :  1.0  
 * description :  日志模板生产类
 * @see        :                        
 */
public class LogTemplateHandler {
	private String temp;

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String createLog(Map mp) {
		StringTokenizer token = new StringTokenizer(temp, ",");
		StringBuilder buffer = new StringBuilder();
		while (token.hasMoreTokens()) {
			try {
				String str = ObjectUtil.trim(token.nextToken());
				buffer.append(ObjectUtil.trim((String)mp.get(str))).append(",");
			} catch (Exception e) {
				e.printStackTrace();
				buffer.append("").append(",");
			}
		}
		return buffer.toString();
	}
}
