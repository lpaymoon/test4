package com.umpay.hfweb.service.impl.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.umpay.hfweb.service.MerAuthService;

public class MerAuthServiceImplTest {

	private MerAuthService auth = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    auth = (MerAuthService)ctx.getBean("merAuthService");
	}
	@Test
	public void canAccess_ok1(){
		Assert.assertTrue(auth.canAccess("HTXD", "9995"));
	}
	
}
