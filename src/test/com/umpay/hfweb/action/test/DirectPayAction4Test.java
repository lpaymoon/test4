package com.umpay.hfweb.action.test;

import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.bs.mpsp.util.FileUtil;
import com.bs.mpsp.util.SignUtil;
import com.bs.utils.Base64;
import com.umpay.hfweb.action.order.DirectPayAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.SessionThreadLocal;


public class DirectPayAction4Test {

	private static Logger logger = Logger.getLogger(DirectPayAction4Test.class);
//	private DirectPayActionTest dpAction = null;
	private DirectPayAction action = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    action = (DirectPayAction)ctx.getBean("directPayAction");
	}
	//带手机号下单流程
	@Test
	public void handleRequestInternalTest_case1(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setMethod("POST");
		
		request.addParameter("merId", "9996");
		request.addParameter("goodsId", "100");
		request.addParameter("mobileId", "13426399070");
		request.addParameter("orderId","577631");
		request.addParameter("merDate","20111201");
		request.addParameter("amount", "1000");//TODO
		request.addParameter("amtType","02");
		request.addParameter("bankType","3");
		request.addParameter("notifyUrl","http://10.10.41.189:9080/demo1/paymentNotify3.jsp");
		request.addParameter("merPriv","");
		request.addParameter("expand","");
		request.addParameter("version","3.0");
		request.addParameter("sign","a/KZ2yzqX9YHdMTOxJfx4ivqY9ane+4N7mUbjwReTKERnH/xpdz7OOrLic5V3SiKBw6/BC+TPQhDamEfhFWqsK0dn3BxjhaAD2gEnBJypwmDSxRlXJatMQP8dUem3hzhzYEQ/6C48MWcQkL3903tqr4iv6KMLmiYJo9elTlLw0g=");
		try {
			ModelAndView modelAndView = action.handleRequest(request, response);
			//Assert.assertEquals(modelAndView.getModelMap().get(DataDict.RET_CODE),"0000");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//@Test
	public void processBussinessTest_ok(){
		//rest.checkSign(payType, cmd);
		Assert.assertNotNull(action);
		RequestMsg reqMap = new RequestMsg();
		reqMap.put("merId", "9996");
		reqMap.put("goodsId", "100");
		reqMap.put("mobileId", "13426399070");
		reqMap.put("orderId","577631");
		reqMap.put("merDate","20111121");
		reqMap.put("amount", "1000");//TODO
		reqMap.put("amtType","02");
		reqMap.put("bankType","3");
		reqMap.put("notifyUrl","http://10.10.41.189:9080/demo1/paymentNotify3.jsp");
		reqMap.put("merPriv","");
		reqMap.put("expand","");
		reqMap.put("version","3.0");
		reqMap.put("sign","a/KZ2yzqX9YHdMTOxJfx4ivqY9ane+4N7mUbjwReTKERnH/xpdz7OOrLic5V3SiKBw6/BC+TPQhDamEfhFWqsK0dn3BxjhaAD2gEnBJypwmDSxRlXJatMQP8dUem3hzhzYEQ/6C48MWcQkL3903tqr4iv6KMLmiYJo9elTlLw0g=");

		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID,"W999612345678901");
		reqMap.put(DataDict.REQ_MER_RPID, "W999612345678901");
		Map<String, Object> map = new HashMap<String, Object>();
		ResponseMsg respMsg = new ResponseMsg(map);
		reqMap.put(DataDict.FUNCODE, "HTXD");
		action.processBussiness(reqMap, respMsg);
		logger.info("===返回数据为=====respMsg="+respMsg.getDirectResMsg());
		//Assert.assertEquals(retCode, "0000");
		//String unSign = cmd.getUnSignStr("directPay", cmd.getExpand());
		//String unSign = "";
	}
	
	public String platSign(String content){
		String keyFile = "D:/work_hf/hfWebBusi/src/test/testMer.key.p8";
		byte[] key = FileUtil.getFileContent(keyFile);
		PrivateKey pk = SignUtil.genPrivateKey(key);
		byte[] signData = SignUtil.sign(pk, content.getBytes());
		String sign = Base64.encode(signData);
		return sign;

	}
	
	//@Test
	public void testVerify(){
//		String plain = "merId=9996&goodsid=100&mobileId=15110259623&orderId=123456&merDate=20111115&amount=1000&amtType=02&bankType=3&gateId=XE01000&retUrl=http://localhost:8080/web/index.jsp&notifyUrl=http://localhost:8080/web/index.jsp&merPriv=&expand=&version=3.0";
		
		String plain="merId=9996&goodsId=100&goodsInf=小话费大商品&mobileId=15110259623&orderId=123456&merDate=20111115&amount=1000&amtType=02&bankType=3&gateId=XE01000&retUrl=http://localhost:8080/web/index.jsp&notifyUrl=http://localhost:8080/web/index.jsp&merPriv=&expand=&version=3.0";
		System.out.println(platSign(plain));
//		String sign = "AoGBHmzb0Byb8BY2/xYJYU+IZswX1N6YvUJzaaLyihEfungbPpHX50+ptj7DtfGGXYiLxFIQuCFWp3JHEF2wuXmFkLL4tX/RkcTmprhrHXk83zkiMw7wUphwSladzw+emK6R2SWz3C4+slXO5XXFaba/f85PbcBYHrEFnzD1kAo=";
		String sign = "SI+pplO2oY0tB34IKJ97gc8j+J5McGribsSEMJc2qO/uHPEMBwNLtA7Wa6MWzAXFQ/cxM2/3cW8lR8EzoKbquGaZySsqJhDUbZ9hlvmCyeR86w2NbzAKEza/pmiIU3pZQUIpYCadSjWNlI0BVDAAdcdi4XMuB8Upu9ptzHASCIA=";
		try {
			verify(plain,sign);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void verify(String plain,String sign) throws UnsupportedEncodingException{
		String certFile = "D:/work_hf/hfWebBusi/src/test/testMer.cert.crt";
		byte[] spCert = FileUtil.getFileContent(certFile);
		X509Certificate x509cert = SignUtil.genCertificate(spCert);
		logger.info(sign);
		byte[] bytesign = sign.getBytes();
		byte[] bSign = Base64.decode(bytesign);
		boolean result = SignUtil.verify(x509cert, plain.getBytes("gbk"),
				bSign);
		if (!result) {
//			logger.info("rpid[" + webInfo.getRpid() + "] merId["
//					+ webInfo.getMerId() + "] mobileId["
//					+ webInfo.getMobileId() + "] orderId["
//					+ webInfo.getOrderId() + "] --- 使用gbk验签失败，尝试使用utf-8验签");
			result = SignUtil.verify(x509cert, plain.getBytes("utf-8"),
					bSign);
		}
		if (result) {
//			logger.info("rpid[" + webInfo.getRpid() + "] merId["
//					+ webInfo.getMerId() + "] unSign[" + unSign + "] sign["
//					+ sign + "] --- 验证商户签名 OK");
			logger.info("ok");
		} else {
//			logger.info("rpid[" + webInfo.getRpid() + "] merId["
//					+ webInfo.getMerId() + "] mobileId["
//					+ webInfo.getMobileId() + "] orderId["
//					+ webInfo.getOrderId() + "] ---验证商户签名失败");
//			throw new Exception("1143");
			logger.info("no");
		}

	}
	
}
