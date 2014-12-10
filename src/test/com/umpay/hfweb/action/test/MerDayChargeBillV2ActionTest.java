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

import com.umpay.hfweb.action.mer.bill.MerDayChargeBillV2Action;

public class MerDayChargeBillV2ActionTest {
	private static Logger logger = Logger.getLogger(MerDayChargeBillV2ActionTest.class);
	private MerDayChargeBillV2Action merdaychargebillv2action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    System.out.println(ctx.getBean("merDayChargeBillV2Action"));
	    merdaychargebillv2action = (MerDayChargeBillV2Action)ctx.getBean("merDayChargeBillV2Action");
	}
	@Test
	public void handleRequestInternalTest(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setMethod("POST");
		request.addParameter("SPID", "9996");
		request.addParameter("REQDATE", "20111102");
		request.addParameter("RDPWD", "123456");
		request.addParameter("SIGN","l8bCV7MC+myXpmmnCLilYNT/y/BO94Dvcln1kFoXwy/NqwE/xzdrNp+IxO7fwhaUTSGhcyAHC/WAHqWTm/bm6ZWPIPY4StQb95id3BbKBo4AHvKgvtg2hXvclveOeGhbeHRAWDxzw55J+yNUG2kDTeNNBPbrD4pRJBq5xRK2NJQ=");

	try {
			ModelAndView modelAndView = merdaychargebillv2action.handleRequest(request,response); 
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
		File file = new File("D:/duizhang/mer3/check/9996.20111102success.txt"); 
	
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
	

