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

import com.umpay.hfweb.action.wx.WxUpdateVersionAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.Encryptor;
import com.umpay.hfweb.util.SessionThreadLocal;

public class WxVersionUpdateActionTest {
	private static Logger logger = Logger.getLogger(DirectPayAction4Test.class);
	private WxUpdateVersionAction action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    action = (WxUpdateVersionAction)ctx.getBean("wxUpdateVersionAction");
	}
	
		@Test
		public void handleRequestInternalTest_case(){
			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			request.setMethod("POST");
			
			Map<String,String> map = new HashMap<String,String>();
			map.put("type", "0");
			map.put("clientName","RemoteBilling");
			map.put("versionCode","1");
			SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
			
		
			Encryptor ep = new Encryptor("umpay123");
			
			
			//map.put(DataDict.REQ_MER_RPID, "W999612345678901");
			String jstr = JSONObject.fromObject(map).toString();
			
			byte[] bt = null;
			try {
				bt = ep.encyptString(jstr);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			System.out.println("加密前的数据:"+jstr);
			System.out.println("加密后的数据:"+bt);
			request.setContent(bt);
			try {
				ModelAndView modelAndView = action.handleRequest(request, response);
				//Assert.assertEquals(modelAndView.getModelMap().get(DataDict.RET_CODE),"0000");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
