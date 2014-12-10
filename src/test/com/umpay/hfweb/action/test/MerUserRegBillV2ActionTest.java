package com.umpay.hfweb.action.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import com.umpay.hfweb.action.mer.bill.MerDayTradeBillV2Action;
import com.umpay.hfweb.action.mer.bill.MerUserRegBillV2Action;

public class MerUserRegBillV2ActionTest {
	private static Logger logger = Logger.getLogger(MerUserRegBillV2ActionTest.class);
	private MerUserRegBillV2Action meruserregbillv2action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    System.out.println(ctx.getBean("merUserRegBillV2Action"));
	    meruserregbillv2action = (MerUserRegBillV2Action)ctx.getBean("merUserRegBillV2Action");
	}
	@Test
	public void handleRequestInternalTest(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setMethod("POST");
		request.addParameter("SPID", "9996");
		request.addParameter("TAG", "12");
		request.addParameter("RDPWD", "123456");
		request.addParameter("SIGN","i+d8v67dYr9lS4a+/PMNHOx4Ug6soyEurqD96jV8Ss5BDWhFU2ljBJ9u4LAIdKvAbXA093A2RGvmhmBs+X0GVhZh8OQr5OEFK1Y4PdFnDcX/0WP9H3L5GSkt7dNQyQ2uxMh1WUu/zQTQ3bK/eGtZyg/deInaynUm/ipQqGDTbKc=");

	try {
			ModelAndView modelAndView = meruserregbillv2action.handleRequest(request,response); 
			byte[] responsePDFValues = response.getContentAsByteArray();
			StringReader reader = new StringReader(new String(responsePDFValues));
			LineNumberReader lineNumReader = new LineNumberReader(reader);
	        String sb=new String();
			String line=lineNumReader.readLine();
			while(line!=null){
				 sb+=line;
				line=lineNumReader.readLine();
	        }
           String x=loadStringFromFile();
           String sb1=sb.toString();
           String x1=x.toString();
           if(sb.equals(x)){
        	   logger.info("获得的文件与本地保持一致！");
           }else{
        	   logger.info("获取文件不相同！");
           }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private String loadStringFromFile() throws IOException {
		File file = new File("D:/duizhang/mer3/check/user.9996.201111.txt"); 
	
		BufferedReader reader = null; 
	    reader = new BufferedReader(new FileReader(file)); 
		String tempString = new String(); 
		String temp=reader.readLine();
		while (temp!= null){ 
		tempString+=temp;
		temp=reader.readLine();
		}
		return tempString;
		}
}
