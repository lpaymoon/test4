package com.umpay.hfweb.action.test;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import com.umpay.hfweb.action.order.PageOrderAction;
import com.umpay.hfweb.action.order.PageReqNoMblAction;
import com.umpay.hfweb.action.order.PageRequestAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.CaptchaServiceSingleton;

public class PageRequestActionTest {

	private static Logger logger = Logger.getLogger(PageRequestActionTest.class);
	//商户请求页面
	private PageRequestAction actionRequest = null;
	//订单保存
	private PageOrderAction actionOrder = null;
	//带验证码-输入手机号页面
	private PageReqNoMblAction action4MobileInput = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	   // System.out.println(ctx.getBean("directPayAction"));
	    actionRequest = (PageRequestAction)ctx.getBean("pageRequestAction");
	    actionOrder = (PageOrderAction)ctx.getBean("pageOrderAction");
	    action4MobileInput = (PageReqNoMblAction)ctx.getBean("pageReqNoMblAction");
	}
	//带手机号下单流程
	//@Test
	public void handleRequestInternalTest_case1(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setMethod("POST");
		//request.addParameter("viewDetails", "true");
		//ModelAndView modelAndView = myController.handleRequest(request, response);
		//assertEquals("Incorrect view name", detailsViewName,modelAndView.getViewName()); 
		
		request.addParameter("merId", "9996");
		request.addParameter("goodsId", "100");
		request.addParameter("goodsInf", "好东西");
		
		request.addParameter("mobileId", "15010107839");
		request.addParameter("orderId","644262");
		request.addParameter("merDate","20111121");
		request.addParameter("amount", "1000");
		request.addParameter("amtType","02");
		request.addParameter("bankType","3");
		request.addParameter("gateId","");
		request.addParameter("retUrl","http://localhost:8080/web/index.jsp");
		request.addParameter("notifyUrl","http://localhost:8080/web/index.jsp");
		request.addParameter("merPriv","");
		request.addParameter("expand","");
		request.addParameter("version","3.0");
		request.addParameter("sign","M5z678tmni9+1+kvaI3vf3+Dwa7uICWFITGLdumeW2OvN9LRqg+jbqvOKMKKV06ooY0IgQhmr/VOTUsyVMuizgxnnx+5yqpYLKVFjhLxKCmoU9GnNop87NPNRf5kVyTfR9Oz/db2uBg46Be0U5l+W+CUEYQTimoELk1tKrpVhDc=");

		try {
			ModelAndView modelAndView = actionRequest.handleRequest(request, response);
			Assert.assertEquals(modelAndView.getViewName(), "order/web_xe_confirmpay");
			//session.
			MockHttpServletRequest requestOrder = new MockHttpServletRequest();
			MockHttpServletResponse responseOrder = new MockHttpServletResponse();
			requestOrder.setMethod("POST");
			requestOrder.setSession(session);
			ModelAndView mvOrder = actionOrder.handleRequest(requestOrder, responseOrder);
			Assert.assertEquals(mvOrder.getModelMap().get(DataDict.RET_CODE),"0000");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//不带手机号下单流程
	@Test
	public void handleRequestInternalTest_case2(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setMethod("POST");
		//request.addParameter("viewDetails", "true");
		//ModelAndView modelAndView = myController.handleRequest(request, response);
		//assertEquals("Incorrect view name", detailsViewName,modelAndView.getViewName()); 
		
		request.addParameter("merId", "9996");
		request.addParameter("goodsId", "100");
		request.addParameter("goodsInf", "好东西");
		
		//request.addParameter("mobileId", "15010107839");
		request.addParameter("orderId","502616");
		request.addParameter("merDate","20111121");
		request.addParameter("amount", "1000");
		request.addParameter("amtType","02");
		request.addParameter("bankType","3");
		request.addParameter("gateId","");
		request.addParameter("retUrl","http://localhost:8080/web/index.jsp");
		request.addParameter("notifyUrl","http://localhost:8080/web/index.jsp");
		request.addParameter("merPriv","");
		request.addParameter("expand","");
		request.addParameter("version","3.0");
		request.addParameter("sign","cdYr9Zw3Cw/U7z6Ki2oHIKI578xyrW2VUrFlgS2Q83tmDMXGCV5k3vdXZt6XLX4lP9GHjofk0atETJFCz5BGQ5oTfNCWkpmfO2CmtcwoFMJCaze3S6veYPosPkw/G1uaj6ew9F3hSf+mjqxkSQ/k272bpmZq1WdwcEiGrnpeG9k=");

		try {
			ModelAndView modelAndView = actionRequest.handleRequest(request, response);
			Assert.assertEquals(modelAndView.getViewName(), "order/web_confirmPayNoMbl");
			//session.
			MockHttpServletRequest action4MobileInputReuest = new MockHttpServletRequest();
			MockHttpServletResponse action4MobileInputResp = new MockHttpServletResponse();
			action4MobileInputReuest.setMethod("POST");
			action4MobileInputReuest.setSession(session);
			
			CaptchaServiceSingleton.getInstance().writeCaptchaImage(action4MobileInputReuest, action4MobileInputResp);
			action4MobileInputReuest.addParameter("mobileId", "15010107839");
			action4MobileInputReuest.addParameter("j_captcha_response", "1234");
			ModelAndView mvOrder = action4MobileInput.handleRequest(action4MobileInputReuest, action4MobileInputResp);
			Assert.assertEquals(mvOrder.getModelMap().get(DataDict.RET_CODE),"0000");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
