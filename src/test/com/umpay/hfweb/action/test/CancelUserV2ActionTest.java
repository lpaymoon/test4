package com.umpay.hfweb.action.test;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.umpay.hfweb.action.mer.MerCancelUserV2Action;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;

public class CancelUserV2ActionTest {
	private static Logger logger = Logger.getLogger(CancelUserV2ActionTest.class);
	private MerCancelUserV2Action action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	   // System.out.println(ctx.getBean("directPayAction"));
	    action = (MerCancelUserV2Action)ctx.getBean("cancelUserV2Action");
	}
	
	@Test
	public void testProcessBussiness(){
		RequestMsg requestMsg = new RequestMsg();
		ResponseMsg responseMsg = new ResponseMsg();
		action.processBussiness(requestMsg, responseMsg);
		Assert.assertTrue(responseMsg.isRetCode0000());
	}
}
