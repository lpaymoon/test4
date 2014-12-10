package com.umpay.hfweb.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.bs2.core.ext.Service4QObj;

public class AlarmInitServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	public static Logger logger = Logger.getLogger(AlarmInitServlet.class);

	private Service4QObj alarmQueue = null;	 

	public void init() throws ServletException {

		WebApplicationContext context = WebApplicationContextUtils
				.getWebApplicationContext(this.getServletContext());
		try {		 
			alarmQueue = (Service4QObj) context.getBean("alarmQueue");
			alarmQueue.start();		 
			logger.info("alarmQueue Initialized ..... Succ ");
		} catch (Exception e) {
			logger.error("alarmQueue Initialized ..... Fail ",e);
			System.exit(0);
		}
	}
}



