package com.umpay.hfweb.service.impl.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import net.sf.ehcache.Element;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bs.mpsp.util.DateTimeUtil;
import com.bs2.core.ext.Service4QObj;
import com.bs2.mpsp.XmlMobile;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.CommonEcc;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.service.SmsService;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;

public class SmsServiceImplTest {

	private SmsService smsService = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    smsService = (SmsService)ctx.getBean("smsService");
	}
	//@Test
	public void sendUserOrderLtdSms_ok1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678911");
		String merId = "9996";
		String mobileId = "15010107839";
		String smsContent = "测试";
		Service4QObj smsQueue = (Service4QObj)ctx.getBean("smsQueue");
		//使用队列需要先调用start方法
		smsQueue.start();//web 环境中在web容器启动时调用
		boolean isok = smsService.sendUserOrderLtdSms(mobileId);
		Assert.assertTrue(isok);
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//@Test
	public void pushSms_ok1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
		String merId = "9996";
		String mobileId = "15010107839";
		String smsContent = "测试";
		Service4QObj smsQueue = (Service4QObj)ctx.getBean("smsQueue");
		//使用队列需要先调用start方法
		smsQueue.start();//web 环境中在web容器启动时调用
		boolean isok = smsService.pushSms(merId, mobileId, smsContent);
		Assert.assertTrue(isok);
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//@Test
	public void pushPaySms_ok1() throws InterruptedException{
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678905");
		String merId = "9996";
		String mobileId = "15110259623";
		String smsContent = "测试";
		Service4QObj smsQueue = (Service4QObj)ctx.getBean("smsQueue");
		//使用队列需要先调用start方法
		smsQueue.start();//web 环境中在web容器启动时调用
		Map<String, String> smsMap = new HashMap<String, String>();
		smsMap.put(HFBusiDict.RPID, "w1212121212");
		smsMap.put(HFBusiDict.CALLED, "10658008");//长号码
		smsMap.put(HFBusiDict.CALLING, "15110259623");
		smsMap.put(HFBusiDict.GOODSNAME, "商品大名哇哈哈");
		smsMap.put(HFBusiDict.BANKID, "XE01000");
		smsMap.put(HFBusiDict.GOODSID, "998901");
		smsMap.put(HFBusiDict.MERID, "9989");
		smsMap.put(HFBusiDict.SERVTYPE, "3");
		smsMap.put(HFBusiDict.SERVMONTH, "1");
		smsMap.put(HFBusiDict.AMOUNT, "1000");
		smsMap.put(HFBusiDict.VERIFYCODE, "8");
		smsMap.put("ISNOTIFY", "TRUE");
		long dt = DateTimeUtil.currentDateTime();
		smsMap.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		smsMap.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		smsMap.put(XmlMobile.FUNCODE, "HFQRZF");
		smsMap.put(HFBusiDict.PORDERID, "8998901");
		smsMap.put("ISTEST", "TRUE");
//		if(ObjectUtil.isNotEmpty(smsParam.getServMonth())){
//			smsMap.put(HFBusiDict.SERVMONTH, smsParam.getServMonth());
//		}
		boolean isok = smsService.pushPaySms(smsMap);
		Thread.sleep(1000000);
		Assert.assertTrue(isok);
	}
	@Test
	public void alarm_Test(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678904");
		String merId = "9996";
		String mobileId = "15010107839";
		String smsContent = "测试";
		Service4QObj smsQueue = (Service4QObj)ctx.getBean("smsQueue");
		//使用队列需要先调用start方法
		smsQueue.start();//web 环境中在web容器启动时调
				
		try {
			Map<String,Object> out = new HashMap<String,Object>();
			out.put(DataDict.RET_CODE, "9998");
			out.put(DataDict.FUNCODE, "HTXD");
			smsService.alarm(out);
			Thread.sleep(2000);
			smsService.alarm(out);
//			smsService.alarm(out);
//			smsService.alarm(out);
		
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
