package com.umpay.hfweb.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * ******************  类说明  *********************
 * class       :  RequestRemoteAddrFilter
 * @author     :  孙善峰
 * @version    :  1.0  
 * description :  配合nginx替换apache的应用
 * @see        :                        
 * ***********************************************
 */

public class RequestRemoteAddrFilter implements Filter {

	private FilterConfig config = null;
	
	private String IS_NGINX = "IS_NGINX";
	
	private String NGINX_IP = "NGINX_IP";
	
	public static Logger logger = Logger.getLogger(RequestRemoteAddrFilter.class);
	/*
	 * ********************************************
	 * method name   : destroy 
	 * modified      : 孙善峰 ,  2009-12-10
	 * @see          : @see javax.servlet.Filter#destroy()
	 * *******************************************
	 */
	public void destroy() {
		config = null;
	}

	/*
	 * ********************************************
	 * method name   : doFilter 
	 * modified      : 孙善峰 ,  2009-12-10
	 * @see          : @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 * *******************************************
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req=(HttpServletRequest)request;
		
		logger.debug("转换前的客户端IP地址为："+req.getRemoteAddr());
		
		String nginx_value = config.getInitParameter(IS_NGINX);
		String nginx_head = config.getInitParameter(NGINX_IP);
		if(nginx_value!=null&&nginx_value.trim().equals("true")){
			/**
			 * 包装请求信息
			 */
		    req = new RemoteWrapperRequest(req,nginx_head);
		}
		
		logger.debug("转换后的客户端IP地址为："+req.getRemoteAddr());
		
		chain.doFilter(req, response);
	}

	/*
	 * ********************************************
	 * method name   : init 
	 * modified      : 孙善峰 ,  2009-12-10
	 * @see          : @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 * *******************************************
	 */
	public void init(FilterConfig config) throws ServletException {
		this.config=config;
	}

}