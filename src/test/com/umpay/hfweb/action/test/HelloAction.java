package com.umpay.hfweb.action.test;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.umpay.hfweb.action.base.BaseAbstractAction;

public class HelloAction extends BaseAbstractAction{

	private String helloView;
	@Override
	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap) {
		
		String id = (String)request.getParameter("id");
		modelMap.put("msg", "你好哈,我从参数中得到ID："+id);
		request.setAttribute("attr", "好啊");
		test();
		return helloView;
	}
	public void setHelloView(String helloView) {
		this.helloView = helloView;
	}
	public void test(){
		System.out.println("hello");
	}
	@Override
	protected String getFunCode() {
		return "";
	}
	
}
