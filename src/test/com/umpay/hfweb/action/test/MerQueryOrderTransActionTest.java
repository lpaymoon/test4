package com.umpay.hfweb.action.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import com.umpay.hfweb.action.mer.MerQueryOrderTransAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;

public class MerQueryOrderTransActionTest {
	private MerQueryOrderTransAction action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    System.out.println(ctx.getBean("merQueryOrderAction"));
	    action = (MerQueryOrderTransAction)ctx.getBean("merQueryOrderTransAction");
	}
	//@Test
	public void testProcessBussiness(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
		RequestMsg requestMsg = new RequestMsg();
		ResponseMsg responseMsg = new ResponseMsg();
		requestMsg.put("merId","9996");
		requestMsg.put("goodsId","398996");
		requestMsg.put("mobileId","15010107839");
	    requestMsg.put("version","3.0");
	    requestMsg.put("merDate","20111102");
	    requestMsg.put("orderId","321T001");
		requestMsg.put("sign","hxHy7e6FR4yRCVA+E4ZXWCh7fZzmMFvfNpr7wDTrMnQhwEVKacf1SALUSb5pcNRRfVIyNLT1/SRsWijK+Iin8fzHH3aD53Al21Dr/uRJWiKkiwzP1Pr4gAoH18wi9sRqzB3KzEQsvFBLIpmP7TMr9f/pd8ls/K9cXc5mb4FRfRo=");
		action.processBussiness(requestMsg, responseMsg);
		Assert.assertTrue(responseMsg.isRetCode0000());
	}
	
	//@Test
	public void testProcessBussiness_ok1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
		RequestMsg requestMsg = new RequestMsg();
		ResponseMsg responseMsg = new ResponseMsg();
		requestMsg.put("merId","9996");
		requestMsg.put("goodsId","100");
		requestMsg.put("mobileId","15010107839");
	    requestMsg.put("version","3.0");
	    requestMsg.put("merDate","20111121");
	    requestMsg.put("orderId","644262");
		requestMsg.put("sign","eHDvM4hJ9/MV5GKhetU50lXTl78yVrAeeJfDSqLsTDQnOFZ0zNkU9vsdXf8FojaRs0OiTtx7b0KkAlR7kKceBF13l21asM6VLOUUXB/7bedv68agpv4N6LGNBesEdqMSbxq1dkg4NXvE1xctVOhVzdMGgXntO+27+nlylPNbs/M=");
		action.processBussiness(requestMsg, responseMsg);
		System.out.println("resMsg: "+responseMsg.getDirectResMsg());
		Assert.assertTrue(responseMsg.isRetCode0000());

	}
	@Test
	public void MerQueryOrderActionTest(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setMethod("POST");
		request.addParameter("merId", "9996");
		request.addParameter("goodsId", "100");
		request.addParameter("mobileId", "13546091234");
		request.addParameter("version", "3.0");
		request.addParameter("merDate", "20120924");
		request.addParameter("orderId","763111");
		String temp = this.getPlainText(request);
		System.out.println(temp);
		request.addParameter("sign","j7oNEKbTM6eJjhfOjceZrq2McsiuBcGGdU1T8VII5pXpzl7elzFATRE5fTK1r6bwI4GnshINZUyMx5RhfryrSTS0zLl9ZOH/QT9KW3HJb359E0MVAVcgm+ht+NPoLAdWB4Klh8emx5s332RPsk5Fcv+07FrqlGRwRahwiUYIL5E=");	
		
		try {
			ModelAndView modelAndView = action.handleRequest(request,response);
			//Assert.assertEquals(modelAndView.getModelMap().get(DataDict.RET_CODE),"0000");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	protected String getPlainText(MockHttpServletRequest requestMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.getParameter(DataDict.MER_REQ_MERID));
		//商品号
		String goodsId = ObjectUtil.trim(requestMsg.getParameter(DataDict.MER_REQ_GOODSID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.getParameter(DataDict.MER_REQ_MOBILEID));
		//版本号
		String version = ObjectUtil.trim(requestMsg.getParameter(DataDict.MER_REQ_VERSION));
		//商户日期
		String merDate = ObjectUtil.trim(requestMsg.getParameter(DataDict.MER_REQ_MERDATE));
		//订单号
		String orderId = ObjectUtil.trim(requestMsg.getParameter(DataDict.MER_REQ_ORDERID));

		StringBuffer buffer = new StringBuffer();
		buffer.append("merId=").append(merId);
		buffer.append("&goodsId=").append(goodsId);
		buffer.append("&orderId=").append(orderId);
		buffer.append("&merDate=").append(merDate);
		buffer.append("&mobileId=").append(mobileId);
		buffer.append("&version=").append(version);
		
		return buffer.toString();
	}
}
