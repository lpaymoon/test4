package com.umpay.hfweb.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.bs.mpsp.util.DateTimeUtil;
import com.umpay.hfbusi.HFBusiDict;

public class DateUtil {

//	private static final SimpleDateFormat FM_yyyyMMdd = new SimpleDateFormat ("yyyyMMdd");
//	static{
//		FM_yyyyMMdd.setLenient(false);
//	}

	/**
	 * ********************************************
	 * method name   : getDateBefore 
	 * description   : 获取前一天
	 * @return       : Date
	 * modified      : yangwr ,  Nov 2, 2011  5:51:15 PM
	 * @see          : 
	 * *******************************************
	 */
	public static Date getDateBefore(Date date){
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.add(Calendar.DATE,-1);
      return calendar.getTime();
	}
	
	public static String getDateyyyMMdd(Date date){
	  SimpleDateFormat formator = new SimpleDateFormat ("yyyyMMdd");
	  formator.setLenient(false);
      String dateStr = formator.format(date);
      return dateStr;
	}
	
	public static String getDate(Date date, String format){
		  SimpleDateFormat formator = new SimpleDateFormat(format);
		  formator.setLenient(false);
	      String dateStr = formator.format(date);
	      return dateStr;
		}
	
	public static Date parseDateyyyyMMdd(String str){
		try {
			SimpleDateFormat formator = new SimpleDateFormat ("yyyyMMdd");
			formator.setLenient(false);
			return formator.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Date calcDay(int num){
	      Calendar calendar = Calendar.getInstance();
	      calendar.setTime(new Date());
	      calendar.add(Calendar.DATE,num);
	      return calendar.getTime();
	}
	/**
	 * ********************************************
	 * method name   : verifyOrderDate 
	 * description   : 下单时间校验(前后一天)
	 * @return       : boolean
	 * @param        : @param strDate
	 * *******************************************
	 */
	public static boolean verifyOrderDate(String strDate){
		SimpleDateFormat formator = new SimpleDateFormat ("yyyyMMdd");
		formator.setLenient(false);
		Date paramDate = null;
		try {
			paramDate = formator.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(paramDate == null){
			return false;
		}
		String yestoday = formator.format(calcDay(-1));
		String tommorow = formator.format(calcDay(1));
		if(strDate.compareTo(yestoday)>=0 && strDate.compareTo(tommorow) <=0){
			return true;
		}
		return false;
	}
	
	/**
	 * ********************************************
	 * method name   : verifyOrderDate 
	 * description   : 下单时间校验(同一天)
	 * @return       : boolean
	 * @param        : @param strDate
	 * *******************************************
	 */
	public static boolean verifyOrderDateStrict(String strDate){
		SimpleDateFormat formator = new SimpleDateFormat ("yyyyMMdd");
		formator.setLenient(false);
		Date paramDate = null;
		try {
			paramDate = formator.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(paramDate == null){
			return false;
		}
		String sysDate = DateTimeUtil.getDateString(DateTimeUtil.currentDateTime());
		if(sysDate.equals(strDate)){
			return true;
		}
		return false;
	} 
}
