package com.umpay.hfweb.action.test;


import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.umpay.hfweb.action.wx.WxUFNotifyAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.Encryptor;
import com.umpay.hfweb.util.SessionThreadLocal;


/** ******************  类说明  *********************
 * class       :  WxUFActionTest
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  话付宝U付测试接口
 * @see        :                        
 * ************************************************/   
public class WxUFNotifyActionTest {
	private static Logger logger = Logger.getLogger(DirectPayAction4Test.class);
	private WxUFNotifyAction action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    action = (WxUFNotifyAction)ctx.getBean("wxUFNotifyAction");
	}
	@Test
	public void handleRequestInternalTest_case(){
			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			request.setMethod("POST");
			request.addParameter("mer_id", "9996");
			request.addParameter("goods_id", "100");
			request.addParameter("amount", "1000");
			request.addParameter("mer_date", "20121026");
			request.addParameter("order_id", "12345678");
			request.addParameter("version","4.0");
			request.addParameter("sign_type","RSA");
			request.addParameter("mer_priv", "123");
			request.addParameter("error_code", "0000");
			request.addParameter("settle_date", "20121030");
			request.addParameter("media_id","18810528823");
			request.addParameter("pay_seq", "XE010000");
			try {
				ModelAndView modelAndView = action.handleRequest(request, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
