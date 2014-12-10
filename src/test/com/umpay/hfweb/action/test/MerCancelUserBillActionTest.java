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

import com.umpay.hfweb.action.mer.bill.MerCancelUserBillAction;


public class MerCancelUserBillActionTest {
	private static Logger logger = Logger.getLogger(MerCancelUserBillActionTest.class);
	private MerCancelUserBillAction mercanceluserbillaction = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    System.out.println(ctx.getBean("merCancelUserBillAction"));
	    mercanceluserbillaction = (MerCancelUserBillAction)ctx.getBean("merCancelUserBillAction");
	 
	}
	@Test
	public void handleRequestInternalTest(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setMethod("POST");
		request.addParameter("merId", "9996");
		request.addParameter("cancelDate","20111114");
        request.addParameter("version","3.0");
		request.addParameter("sign","ljP810wlbxtw1Jw0qkUQQCFIdlG3VXjvNrFZzjfPkm0QoY4iVTnqqzkWWq2ONQieSyTAo9UpTL8rc30/mLVn0lHhS+g6c2rVE8FyOFBUzpopQ0cIJainTV8b2NFgb8g7R2+Wohwpjs2iHVjtyRmtejbXyY3VlmUlWM9VPYFz+tE=");

	try {
			ModelAndView modelAndView = mercanceluserbillaction.handleRequest(request,response); 
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
		File file = new File("D:/duizhang/mer3/check/9996.20111114.txt"); 
	
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
