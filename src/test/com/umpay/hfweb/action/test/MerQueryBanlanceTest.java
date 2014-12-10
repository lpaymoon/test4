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

import com.umpay.hfweb.action.mer.MerPushSmsV2Action;
import com.umpay.hfweb.action.mer.MerQueryBalanceAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.SessionThreadLocal;

public class MerQueryBanlanceTest {
	private MerQueryBalanceAction action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    System.out.println(ctx.getBean("merQueryBalanceAction"));
	    action = (MerQueryBalanceAction)ctx.getBean("merQueryBalanceAction");
	}
	@Test
	public void testProcessBussiness(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
		RequestMsg requestMsg = new RequestMsg();
		ResponseMsg responseMsg = new ResponseMsg();
		requestMsg.put("merId","9996");
		requestMsg.put("merDate","20111117");
		requestMsg.put("mobileId","13559012342");
	    requestMsg.put("version","3.0");
		requestMsg.put("sign","F2A90+uNjMJ8XOjSQUz/jsFjUX2EQWezjXL15ppx7RQcgE50yOo9iFoik8KuGdwbofepUe5+4+DwJYju8Q3NxPYQRw0QLpuLW4mr7Bjh/SWdy0OPEobWf8QemNzkeC7ANj8Jql48HjV0O/rBlav38VvtIevm/N9Xd2DfDK+7V4o= ");
		action.processBussiness(requestMsg, responseMsg);
		Assert.assertTrue(responseMsg.isRetCode0000());
	}
	@Test
	public void MerQueryBanlanceTest(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setMethod("POST");
		request.addParameter("merId", "9996");
		request.addParameter("merDate", "20111117");
		request.addParameter("mobileId", "13559012342");
		request.addParameter("version", "3.0");
		request.addParameter("sign", "F2A90+uNjMJ8XOjSQUz/jsFjUX2EQWezjXL15ppx7RQcgE50yOo9iFoik8KuGdwbofepUe5+4+DwJYju8Q3NxPYQRw0QLpuLW4mr7Bjh/SWdy0OPEobWf8QemNzkeC7ANj8Jql48HjV0O/rBlav38VvtIevm/N9Xd2DfDK+7V4o=");
		try {
			ModelAndView modelAndView = action.handleRequest(request,response);
			Assert.assertEquals(modelAndView.getModelMap().get(DataDict.RET_CODE),"0000");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}
