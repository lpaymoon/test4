package com.umpay.hfweb.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.bs2.core.ext.Service4QObj;

public class SmsInitServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	public static Logger logger = Logger.getLogger(SmsInitServlet.class);

	private Service4QObj smsQueue = null;	 

	public void init() throws ServletException {

		WebApplicationContext context = WebApplicationContextUtils
				.getWebApplicationContext(this.getServletContext());
		try {		 
			smsQueue = (Service4QObj) context.getBean("smsQueue");
			smsQueue.start();		 
		} catch (Exception e) {
			logger.error("Initialized ..... Fail ",e);
			System.exit(0);
		}
	}
}



