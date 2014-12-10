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
import com.umpay.hfweb.action.wx.WxUserAction;

public class WxUserActionTest {
	private static Logger logger = Logger.getLogger(DirectPayAction4Test.class);
	private WxUserAction action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    action = (WxUserAction)ctx.getBean("wxUserAction");
	}
	
		@Test 
		public void handleRequestInternalTest_case(){
			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			request.setMethod("POST");
			
			Map<String,String> map = new HashMap<String,String>();
			map.put("IMEI", "100000000000002");
			map.put("platType", "1");
			map.put("clientversion","2.0.1");
			map.put("mobileos","2.3.5");
			map.put("model", "HTC G14");
			String jstr = JSONObject.fromObject(map).toString();
			byte[] by  = null;
			try {
				by = jstr.getBytes("UTF-8");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			request.setContent(by);
			try {
				ModelAndView modelAndView = action.handleRequest(request, response);
				//Assert.assertEquals(modelAndView.getModelMap().get(DataDict.RET_CODE),"0000");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
