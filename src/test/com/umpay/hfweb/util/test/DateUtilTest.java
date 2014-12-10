package com.umpay.hfweb.util.test;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.umpay.hfweb.util.DateUtil;

public class DateUtilTest {

	//@Test
	public void testGetDateBefore(){
		Date date = DateUtil.getDateBefore(new Date());
		System.out.println(date);
	}
	
	public void testGetDateyyyMMdd(){
		System.out.println(DateUtil.getDateyyyMMdd(DateUtil.getDateBefore(new Date())));
	}
	
	public void testParseDateyyyyMMdd(){
		//DateUtil.getDateyyyMMdd();
		System.out.println(DateUtil.parseDateyyyyMMdd("20110231"));
		System.out.println(System.nanoTime());
		System.out.println(System.currentTimeMillis());
	}

	public void verifyOrderDate_ok1(){
		System.out.println(DateUtil.verifyOrderDate("20111201"));
	}
	@Test
	public void test(){
		String orderTime = "2011-01-11 21:33:15.993889";
		try {
			SimpleDateFormat dateFormat1 = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat dateFormat2 = new SimpleDateFormat ("yyyyMMddHHmmss");
			Date orderTimeDate = dateFormat1.parse(orderTime);
			orderTime = dateFormat2.format(orderTimeDate);
		} catch (Exception e) {
			//logInfo("定购时间解析失败");
			e.printStackTrace();
		}
		System.out.println(orderTime);
	}
	
	
	
}
