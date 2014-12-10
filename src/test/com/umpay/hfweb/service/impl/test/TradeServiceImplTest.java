package com.umpay.hfweb.service.impl.test;

import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sun.jmx.snmp.Timestamp;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.command.DirectOrderCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.service.TradeService;
import com.umpay.hfweb.util.SessionThreadLocal;


public class TradeServiceImplTest {

	private static Logger logger = Logger.getLogger(TradeServiceImplTest.class);
	private TradeService trade = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    trade = (TradeService)ctx.getBean("tradeService");
	}
	
	@Test
	public void saveOrder_ok1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		String bankId = "XE010000";
		String priceMode = "1";
		String servType = "2";
		String servMonth = "3";
		String goodsName = "商品名称大名鼎鼎";
		MpspMessage message = new MpspMessage();
		message.put(HFBusiDict.PRICEMODE, priceMode);
		message.put(HFBusiDict.SERVTYPE, servType);
		message.put(HFBusiDict.SERVMONTH, servMonth);
		message.put(HFBusiDict.GOODSNAME, goodsName);
		long expireTime = 10000;
		message.put(HFBusiDict.EXPIRETIME, expireTime);
		RequestMsg reqMap = new RequestMsg();
		reqMap.put("merId", "9996");
		reqMap.put("goodsId", "100");
		reqMap.put("mobileId", "15010107839");
		reqMap.put("orderId","321T00009");
		reqMap.put("merDate","20111102");
		reqMap.put("amount", "100");//TODO
		reqMap.put("amtType","02");
		reqMap.put("bankType","3");
		reqMap.put("notifyUrl","http://localhost:8080/web");
		reqMap.put("merPriv","");
		reqMap.put("expand","");
		reqMap.put("version","3.0");
		reqMap.put("sign","UdbiPSnwNLDhO4c4hpjTi0TTBOsp9/z9u6lJFJmR+/aekrqI/7yZiuATcbwB0ktoW+UbMA4/iAiI4LXAbJ8e+HpBtX2VL84IML0S2RIN0hgHdgkDUV9TvufNDnjOd0nvTydpGfQuHTqH39wGvXMPM3bpoUlPNmoLsGjp7s4YjZ4=");

		DirectOrderCmd cmd = new DirectOrderCmd(reqMap.getWrappedMap());
		MpspMessage ret = trade.saveOrder(bankId,message,cmd);
		logger.info("saveOrder_ok1=====retCode: "+ret.getRetCode());
		logger.info("saveOrder_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.isRetCode0000());
	}
	
	//@Test
	public void saveOrder_error1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		String bankId = "XE010000";
		String priceMode = "0";
		String servType = "2";
		String servMonth = "3";
		String goodsName = "满意一百分";
		MpspMessage message = new MpspMessage();
		message.put(HFBusiDict.PRICEMODE, priceMode);
		message.put(HFBusiDict.SERVTYPE, servType);
		message.put(HFBusiDict.SERVMONTH, servMonth);
		message.put(HFBusiDict.GOODSNAME, goodsName);
		long expireTime = 100000;
		message.put(HFBusiDict.EXPIRETIME, new Timestamp(expireTime).toString());

		RequestMsg reqMap = new RequestMsg();
		reqMap.put("merId", "9996");
		reqMap.put("goodsId", "100");
		reqMap.put("mobileId", "15010107839");
		reqMap.put("orderId","321T001");
		reqMap.put("merDate","20111102");
		reqMap.put("amount", "100");//TODO
		reqMap.put("amtType","02");
		reqMap.put("bankType","3");
		reqMap.put("notifyUrl","http://localhost:8080/web");
		reqMap.put("merPriv","");
		reqMap.put("expand","");
		reqMap.put("version","3.0");
		reqMap.put("sign","UdbiPSnwNLDhO4c4hpjTi0TTBOsp9/z9u6lJFJmR+/aekrqI/7yZiuATcbwB0ktoW+UbMA4/iAiI4LXAbJ8e+HpBtX2VL84IML0S2RIN0hgHdgkDUV9TvufNDnjOd0nvTydpGfQuHTqH39wGvXMPM3bpoUlPNmoLsGjp7s4YjZ4=");

		DirectOrderCmd cmd = new DirectOrderCmd(reqMap.getWrappedMap());
		MpspMessage ret = trade.saveOrder(bankId,message,cmd);
		logger.info("saveOrder_ok1=====retCode: "+ret.getRetCode());
		logger.info("saveOrder_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.getRetCode().equals("1166"));
	}
	
	//@Test
	public void queryBalance_error1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = trade.queryBalance("9996","15010107839");
		logger.info("queryBalance_ok1=====retCode: "+ret.getRetCode());
		logger.info("queryBalance_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.getRetCode().equals("1166"));
	}
	
	//@Test
	public void queryBalance_ok1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = trade.queryBalance("9996","15010107839");
		logger.info("queryBalance_ok1=====retCode: "+ret.getRetCode());
		logger.info("queryBalance_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.isRetCode0000());
	}
	
}
