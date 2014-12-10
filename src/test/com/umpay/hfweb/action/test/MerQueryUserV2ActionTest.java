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
import com.umpay.hfweb.action.mer.MerQueryUserV2Action;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.SessionThreadLocal;

public class MerQueryUserV2ActionTest {
	private MerQueryUserV2Action action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    System.out.println(ctx.getBean("merQueryUserV2Action"));
	    action = (MerQueryUserV2Action)ctx.getBean("merQueryUserV2Action");
	}
//	@Test
//	public void testProcessBussiness(){
//		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
//		RequestMsg requestMsg = new RequestMsg();
//		ResponseMsg responseMsg = new ResponseMsg();
//		requestMsg.put("SPID","9996");
//		requestMsg.put("GOODSID","999602");
//		requestMsg.put("MOBILEID","15710624951");
//	    requestMsg.put("RDPWD","123456");
//		requestMsg.put("SIGN","aGhH+pNVtzdWqdps9A25YI2JgpIBu1iGCiZbazTGHtMLaaDvjh9C15DQxsR6O2R/i4JI57NU+OLKl98FqVq+5Kt9qs1GBMTezr4crx4MPwk2GM+h3QJaq/UKS7ESiH+lM7fJg7/av5Yfyubv+zNjm99Lk+VLNFYSqfg20e9FBgM= ");
//		action.processBussiness(requestMsg, responseMsg);
//		Assert.assertTrue(responseMsg.isRetCode0000());
//	}
	@Test
	public void MerQueryUserV2ActionTest(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setMethod("POST");
		request.addParameter("SPID", "9996");
		request.addParameter("GOODSID", "999602");
		request.addParameter("MOBILEID", "15710624951");
		request.addParameter("RDPWD", "123456");
		request.addParameter("SIGN", "aGhH+pNVtzdWqdps9A25YI2JgpIBu1iGCiZbazTGHtMLaaDvjh9C15DQxsR6O2R/i4JI57NU+OLKl98FqVq+5Kt9qs1GBMTezr4crx4MPwk2GM+h3QJaq/UKS7ESiH+lM7fJg7/av5Yfyubv+zNjm99Lk+VLNFYSqfg20e9FBgM= ");
		
		
		
		try {
			ModelAndView modelAndView = action.handleRequest(request,response);
			//Assert.assertEquals(modelAndView.getModelMap().get(DataDict.RET_CODE),"0000");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
