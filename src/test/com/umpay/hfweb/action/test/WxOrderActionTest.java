package com.umpay.hfweb.action.test;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.umpay.hfweb.action.wx.WxOrderAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.Encryptor;
import com.umpay.hfweb.util.SessionThreadLocal;

public class WxOrderActionTest {
	private static Logger logger = Logger.getLogger(WxOrderActionTest.class);
	private WxOrderAction action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    action = (WxOrderAction)ctx.getBean("wxOrderAction");
	}
	
		@Test 
		public void handleRequestInternalTest_case(){
			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			request.setMethod("POST");
			
			Map<String,String> map = new HashMap<String,String>();
			map.put("merId", "9996");
			map.put("goodsId", "100");
			map.put("orderId","2012070310016");
			map.put("merDate","20120711");
			map.put("amount", "1000");//TODO
			map.put("merPriv","xx商户私有信息");
			map.put("expand","xx商户扩展信息");
			map.put("version","1.0");
			map.put("IMEI", "1232145");
			map.put("sign","sign99961002012030710001201203201000sign");
			SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
			map.put(DataDict.REQ_MER_RPID, "W999612345678901");
			String jstr = JSONObject.fromObject(map).toString();
			Encryptor ep = new Encryptor("umpay123");
			byte[] by  = null;
			try {
				by = ep.encyptString(jstr);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			System.out.println("加密数据为:"+jstr);
			System.out.println("加密的数组长度为:"+by.length);
			System.out.println("加密后的数据为:"+by.toString());
			request.setCharacterEncoding("UTF-8");
			request.setContent(by);
			try {
				ModelAndView modelAndView = action.handleRequest(request, response);
				//Assert.assertEquals(modelAndView.getModelMap().get(DataDict.RET_CODE),"0000");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
