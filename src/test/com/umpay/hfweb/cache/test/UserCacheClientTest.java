package com.umpay.hfweb.cache.test;

//import org.junit.Before;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.UserCacheClient;

public class UserCacheClientTest {

	private ApplicationContext ctx;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	}
	@Test
	public void testCheckOrderLtd(){
		UserCacheClient ucc = (UserCacheClient)AbstractCacheFactory.getInstance().getUserCacheClient();
		String retCode = ucc.checkUserOrdersLtd("15010107839", "1");
		System.out.println(retCode);
		retCode = ucc.checkUserOrdersLtd("15010107839", "1");
		System.out.println(retCode);
		Assert.assertEquals(retCode, "0000");
	}

}
