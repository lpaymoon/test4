package com.umpay.hfweb.action.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import com.bs2.core.ext.Service4QObj;
import com.umpay.hfweb.action.mer.MerPushSmsV2Action;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.SessionThreadLocal;

public class MerPushSmsV2ActionTest {
	private MerPushSmsV2Action action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    System.out.println(ctx.getBean("merPushSmsV2Action"));
	    action = (MerPushSmsV2Action)ctx.getBean("merPushSmsV2Action");
		Service4QObj smsQueue = (Service4QObj)ctx.getBean("smsQueue");
		//使用队列需要先调用start方法
		smsQueue.start();//web 环境中在web容器启动时调
	}
	//@Test
	public void specialCheck(){
		RequestMsg requestMsg = new RequestMsg();
		requestMsg.put(DataDict.MER_REQ_FUNCODE_V2, "8820");
		requestMsg.put(DataDict.MER_REQ_MERID_V2, "5502");
		String remark = "非常抱歉，金山通行证12345已经存在，但没有与同名手机绑定，您无法使用本服务！";
		requestMsg.put(DataDict.MER_REQ_REMARK2_V2, remark);

		MpspMessage mpspMsg = action.specialCheck(requestMsg);
		System.out.println("retCode:"+mpspMsg.getRetCode());
		Assert.assertTrue(mpspMsg.isRetCode0000());
	}
	//@Test
	public void patternTest(){
		//String pattern = "(非常抱歉，金山通行证){1}.*(已经存在，但没有与同名手机绑定，您无法使用本服务！){1}";
		String pattern = "(非常抱歉，金山通行证){1}.*已经存在，但没有与同名手机绑定，您无法使用本服务！{1}.*";
		//(您的游戏账号){1}.*(本月已达到手机业务充值上限，请更换其他账号再试。){1}
		Pattern p = Pattern.compile(pattern);
		//String remark = "(非常抱歉，金山通行证)123456(已经存在，但没有与同名手机绑定，您无法使用本服务！)123456";
		//String remark = "非常抱歉，金山通行证123已经存在，但没有与同名手机绑定，您无法使用本服务！123";
		String remark = "非常抱歉，金山通行证12345已经存在，但没有与同名手机绑定，您无法使用本服务！1234567890服务";
		Matcher m = p.matcher(remark);
		boolean check = m.matches();
		System.out.println("check:"+check);
		Assert.assertTrue(check);
	}
	//@Test
	public void patternTest_ok2(){
		//String pattern = "(非常抱歉，金山通行证){1}.*(已经存在，但没有与同名手机绑定，您无法使用本服务！){1}";
		String pattern = "(您的游戏账号){1}.*(本月已达到手机业务充值上限，请更换其他账号再试。){1}";
		//
		Pattern p = Pattern.compile(pattern);
		//String remark = "(非常抱歉，金山通行证)123456(已经存在，但没有与同名手机绑定，您无法使用本服务！)123456";
		//String remark = "非常抱歉，金山通行证123已经存在，但没有与同名手机绑定，您无法使用本服务！123";
		String remark = "您的游戏账号123本月已达到手机业务充值上限，请更换其他账号再试。123";
		Matcher m = p.matcher(remark);
		boolean check = m.matches();
		System.out.println("check:"+check);
		Assert.assertTrue(check);
	}
	//
	//@Test
	public void patternTest_ok3(){
		//String pattern = "(非常抱歉，金山通行证){1}.*(已经存在，但没有与同名手机绑定，您无法使用本服务！){1}";
		String pattern = "(您订购的){1}.*(激活码为){1}.*(感谢您的购买!客服电话:){1}.{10,12}";
		//
		Pattern p = Pattern.compile(pattern);
		//String remark = "(非常抱歉，金山通行证)123456(已经存在，但没有与同名手机绑定，您无法使用本服务！)123456";
		//String remark = "非常抱歉，金山通行证123已经存在，但没有与同名手机绑定，您无法使用本服务！123";
		String remark = "您订购的a1s2激活码为123456感谢您的购买!客服电话:010-12345678";
		Matcher m = p.matcher(remark);
		boolean check = m.matches();
		System.out.println("check:"+check);
		Assert.assertTrue(check);
	}
	//
	//@Test
	public void patternTest_ok4(){
		//String pattern = "(非常抱歉，金山通行证){1}.*(已经存在，但没有与同名手机绑定，您无法使用本服务！){1}";
		String pattern = "(您已成功购买).*(激活码。现可以享受){1}.*(特别优惠：加20元多得一套送给Ta。回复){1}.*(立即购买。客服){1}.{11,12}(，本条免费){1}.{0}";
		//
		Pattern p = Pattern.compile(pattern);
		//String remark = "(非常抱歉，金山通行证)123456(已经存在，但没有与同名手机绑定，您无法使用本服务！)123456";
		//String remark = "非常抱歉，金山通行证123已经存在，但没有与同名手机绑定，您无法使用本服务！123";
		String remark = "您已成功购买a11激活码。现可以享受a11特别优惠：加20元多得一套送给Ta。回复a11立即购买。客服01087654321，本条免费";
		Matcher m = p.matcher(remark);
		boolean check = m.matches();
		System.out.println("check:"+check);
		Assert.assertTrue(check);
	}
	//@Test
	public void testProcessBussiness(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
		RequestMsg requestMsg = new RequestMsg();
		ResponseMsg responseMsg = new ResponseMsg();
		requestMsg.put("FUNCODE","8820");
		requestMsg.put("SPID","5502");
		requestMsg.put("ORDERID","321T001");
		requestMsg.put("AMOUNT","100");
		requestMsg.put("DATETIME","20111116");
		requestMsg.put("MOBILEID","15010107839");
		requestMsg.put("RDPWD","123");
		requestMsg.put("REMARK","非常抱歉，金山通行证12345已经存在，但没有与同名手机绑定，您无法使用本服务！");
		requestMsg.put("REMARK2","aa");
		requestMsg.put("URL","");
		requestMsg.put("SIGN","omUhUc/Ciji5Yurc7jXnjZMnimRIOA6jOwwmx3uHalQ2mMb7iYy3eSrkNn8Rjoujdq9A2QJvjz9J2HnUpyawjavQXC56S672p1cexZY1DmQzkBamuxeztfhrbwqIHPRGRjjZYur9Crfot/2WTlZPiLOJ8ttObmQUlPWzaQPdNDY=");
		action.processBussiness(requestMsg, responseMsg);
		Assert.assertTrue(responseMsg.isRetCode0000());
	}
	
	@Test
	public void MerPushSmsV2(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setMethod("POST");
		request.addParameter("FUNCODE", "8820");
		request.addParameter("SPID", "5502");
		request.addParameter("ORDERID", "321T001");
		request.addParameter("AMOUNT", "100");
		request.addParameter("DATETIME", "20111116");
		request.addParameter("MOBILEID", "15010107839");
		request.addParameter("RDPWD", "123");
		request.addParameter("REMARK", "非常抱歉，金山通行证12345已经存在，但没有与同名手机绑定，您无法使用本服务！");
		request.addParameter("REMARK2", "aa");
		request.addParameter("URL", "");
		request.addParameter("SIGN", "omUhUc/Ciji5Yurc7jXnjZMnimRIOA6jOwwmx3uHalQ2mMb7iYy3eSrkNn8Rjoujdq9A2QJvjz9J2HnUpyawjavQXC56S672p1cexZY1DmQzkBamuxeztfhrbwqIHPRGRjjZYur9Crfot/2WTlZPiLOJ8ttObmQUlPWzaQPdNDY=");
		
		
		try {
			ModelAndView modelAndView = action.handleRequest(request,response);
			//Assert.assertEquals(modelAndView.getModelMap().get(DataDict.RET_CODE),"0000");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}
