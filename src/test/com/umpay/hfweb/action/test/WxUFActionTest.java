package com.umpay.hfweb.action.test;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.umpay.hfweb.action.wx.WxUFAction;
import com.umpay.hfweb.action.wx.WxUpdateVersionAction;
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
public class WxUFActionTest {
	private static Logger logger = Logger.getLogger(DirectPayAction4Test.class);
	private WxUFAction action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    action = (WxUFAction)ctx.getBean("wxUfAction");
	}
	
		@Test
		public void handleRequestInternalTest_case(){
			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			request.setMethod("POST");
			
			Map<String,String> map = new HashMap<String,String>();
			map.put("goodsId", "100");
			map.put("mobileId", "18810528823");
			map.put("goodsInf","test goods");
			map.put("orderId","T000001");
			map.put("merDate","20121029");
			map.put("amount","100");
			map.put("merPriv","test");
			map.put("merId", "9996");
			SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
			
		
			Encryptor ep = new Encryptor("umpay123");
			
			
			//map.put(DataDict.REQ_MER_RPID, "W999612345678901");
			String jstr = JSONObject.fromObject(map).toString();
			
			byte[] bt = null;
			try {
				bt = ep.encyptString(jstr);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			System.out.println("加密前的数据:"+jstr);
			System.out.println("加密后的数据:"+bt);
			request.setContent(bt);
			try {
				ModelAndView modelAndView = action.handleRequest(request, response);
				//Assert.assertEquals(modelAndView.getModelMap().get(DataDict.RET_CODE),"0000");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
