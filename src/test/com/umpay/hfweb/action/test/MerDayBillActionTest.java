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

import com.umpay.hfweb.action.mer.bill.MerDayBillAction;

public class MerDayBillActionTest {
	private static Logger logger = Logger.getLogger(MerDayBillActionTest.class);
	private MerDayBillAction merdaybillaction = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    System.out.println(ctx.getBean("merDayBillAction"));
	    merdaybillaction = (MerDayBillAction)ctx.getBean("merDayBillAction");
	 
	}
	@Test
	public void handleRequestInternalTest(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setMethod("POST");
		request.addParameter("merId", "9996");
		request.addParameter("payDate", "20111102");
		request.addParameter("version","3.0");
		request.addParameter("sign","lmtKVFI4iTtDJpQF6Su9k1l46Sa4yshd7E7dfCfPWc8S+rEbirSwtdA3BNQK3ejC0OvuMHyEyqnpuN1lgDTkmJA7NigFh7+Nsn3j+HAuG00L9ZdZ4KLsmd93HIEOt37+v+nBKdbl9CKyQeTqdBGXeq7pA2QrnjvhD6TXdpSL+Tc=");

	try {
			ModelAndView modelAndView = merdaybillaction.handleRequest(request,response); 
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
		File file = new File("D:/duizhang/mer3/check/9996.20111102.txt"); 
	
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
