package com.umpay.hfweb.util.test;

import org.junit.Test;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.SessionThreadLocal;

public class LogInfoTest {

	
	public  static String logInfo(String message,String... args){
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String funCode = SessionThreadLocal.getSessionValue(DataDict.FUNCODE);
		return String.format("%s,%s,%s",funCode,rpid,String.format(message,args));
	}
	@Test
	public void testLogInfo(){
		long begin = System.currentTimeMillis();
		logInfo("dddddxxxxxx");
		long time1 = System.currentTimeMillis();
		System.out.println(time1-begin);
		//System.out.println(logInfo("dddddxx%sxxx"));
		long time2 = System.currentTimeMillis();
		System.out.println(logInfo("dddddxx%sxx%sx","abc%s","abc"));
		long time3 = System.currentTimeMillis();
		System.out.println(time3-time2);
	}
}
