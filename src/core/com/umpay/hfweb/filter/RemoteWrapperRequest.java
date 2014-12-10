package com.umpay.hfweb.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * ******************  类说明  *********************
 * class       :  RemoteWrapperRequest
 * @author     :  孙善峰
 * @version    :  1.0  
 * description :  
 * @see        :                        
 * ***********************************************
 */

public class RemoteWrapperRequest extends HttpServletRequestWrapper {

	private String HEAD_PARA="X-Real-IP";
	
	public RemoteWrapperRequest(HttpServletRequest request) {
		super(request);
		// TODO Auto-generated constructor stub
	}
	
	public RemoteWrapperRequest(HttpServletRequest request,String HEAD_PARA) {
		super(request);
		if(HEAD_PARA!=null&&!HEAD_PARA.trim().equals(""))
			this.HEAD_PARA=HEAD_PARA;
		// TODO Auto-generated constructor stub
	}

	/*
	 * ********************************************
	 * method name   : getRemoteAddr 
	 * modified      : 孙善峰 ,  2009-12-10
	 * @see          : @see javax.servlet.ServletRequestWrapper#getRemoteAddr()
	 * *******************************************
	 */
	public String getRemoteAddr(){
		String address=this.getHeader(HEAD_PARA);
		if(address==null||address.trim().equals(""))
			address=super.getRemoteAddr();
		return address;
	}
	
}