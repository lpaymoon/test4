package com.umpay.hfweb.action.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;


import com.umpay.hfweb.action.mer.MerQueryUserAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.SessionThreadLocal;

public class MerQueryUserActionTest {
	private MerQueryUserAction action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    System.out.println(ctx.getBean("merQueryUserAction"));
	    action = (MerQueryUserAction)ctx.getBean("merQueryUserAction");
	}
	//@Test
	public void testProcessBussiness(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
		RequestMsg requestMsg = new RequestMsg();
		ResponseMsg responseMsg = new ResponseMsg();
		requestMsg.put("merId","9996");
		requestMsg.put("goodsId","999601");
		requestMsg.put("mobileId","13581946206");
	    requestMsg.put("version","3.0");
		requestMsg.put("sign","MvyNr7rQxOrbG5E5+kN5g2+N7GVOHIENBlKDPrACgiAO9/EADkHvSlWXvQZJJ6FTBEUxbH+1Yus5k6gQa93fLoZdhJTjcRHHnRpwc+S1yH5ZWcivG35IK7NCAHJ1sZFKXAnjK8O3nD9F0YhvNUCnDb1Ol3Tci83jSJ1tahH6+ys=");
		action.processBussiness(requestMsg, responseMsg);
		Assert.assertTrue(responseMsg.isRetCode0000());
	}
	@Test
	public void MerQueryUserActionTest(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setMethod("POST");
		request.addParameter("merId", "9996");
		request.addParameter("goodsId", "999601");
		request.addParameter("mobileId", "13581946206");
		request.addParameter("version", "3.0");
		request.addParameter("sign", "NsIR+G/81SlDypjG4sflERlq1a9K+5yP0NvaZBWCFtjcWgSq4FbWUcVuF0fwaEg9YHWslFBjdqsv2YJRoYwQw2ZizZRr5kay4z2DZJYBxczMrBCVYCn3jr/RZ83/1JfVVzhstg27mzSImDSAELRMq0TkjPfRpUingE+FWDc1ngs=");
		
		
		
		
		try {
			ModelAndView modelAndView = action.handleRequest(request,response);
			//Assert.assertEquals(modelAndView.getModelMap().get(DataDict.RET_CODE),"0000");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
