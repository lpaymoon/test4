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

import com.umpay.hfweb.action.wx.WxQueryOrderAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.Encryptor;
import com.umpay.hfweb.util.SessionThreadLocal;

public class WxQueryOrderActionTest { 
	private static Logger logger = Logger.getLogger(DirectPayAction4Test.class);
	private WxQueryOrderAction action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    action = (WxQueryOrderAction)ctx.getBean("wxQueryOrderAction");
	}
	
		@Test
		public void handleRequestInternalTest_case(){
			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			request.setMethod("POST");
			
			Map<String,String> map = new HashMap<String,String>();
			map.put("merId", "9989");
			map.put("orderId","604295");
			map.put("merDate","20120220");
			map.put("version","1.0");
			map.put("sign","a/KZ2yzqX9YHdMTOxJfx4ivqY9ane+4N7mUbjwReTKERnH/xpdz7OOrLic5V3SiKBw6/BC+TPQhDamEfhFWqsK0dn3BxjhaAD2gEnBJypwmDSxRlXJatMQP8dUem3hzhzYEQ/6C48MWcQkL3903tqr4iv6KMLmiYJo9elTlLw0g=");
			SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
			map.put(DataDict.REQ_MER_RPID, "W999612345678901");
			String jstr = JSONObject.fromObject(map).toString();
			Encryptor encryptor = new Encryptor("umpay123");
			byte[] encBytes=null;
			try {
				encBytes = encryptor.encyptString(jstr);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String enString = new String(encBytes);
			System.out.println("加密后的数据为:"+enString);
			byte [] content = enString.getBytes();
			System.out.println("字节数组的长度:"+encBytes.length);
			request.setContent(encBytes);
			try {
				ModelAndView modelAndView = action.handleRequest(request, response);
				//Assert.assertEquals(modelAndView.getModelMap().get(DataDict.RET_CODE),"0000");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
