package com.umpay.hfweb.service.impl.test;

import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bs.mpsp.util.FileUtil;
import com.bs.mpsp.util.SignUtil;
import com.bs.utils.Base64;
import com.umpay.hfweb.action.command.DirectOrderCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.service.RestService;
import com.umpay.hfweb.util.SessionThreadLocal;


public class RestServiceImplTest {

	private static Logger logger = Logger.getLogger(RestServiceImplTest.class);
	private RestService rest = null;
	private ApplicationContext ctx = null;
	@Before
	public void init(){
		String[] locations = {"ctx/ctx-common.xml", "ctx/ctx-business.xml", "ctx/url-mapping.xml"};
	    ctx = new ClassPathXmlApplicationContext(locations);
	    rest = (RestService)ctx.getBean("restService");
	}
//	@Test
//	public void queryOrderByMobileid_ok1(){//TODO
//		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
//		MpspMessage ret = rest.queryOrderByMobileId("15110259623","900056051");
//		logger.info("queryOrderByMobileid_error1=====retCode: "+ret.getRetCode());
//		logger.info("queryOrderByMobileid_error1=====retCodeBussi: "+ret.getRetCodeBussi());
//		Assert.assertTrue(ret.getRetCode().equals("0000"));
//	}
	//@Test
//	public void queryOrderByMobileid_error1(){
//		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
//		MpspMessage ret = rest.queryOrderByMobileId("15010107839","001");
//		logger.info("queryOrderByMobileid_error1=====retCode: "+ret.getRetCode());
//		logger.info("queryOrderByMobileid_error1=====retCodeBussi: "+ret.getRetCodeBussi());
//		Assert.assertTrue(ret.getRetCode().equals("1162"));
//	}
	//@Test
	public void queryMerOrder_ok1(){ //TODO
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = rest.queryMerOrder("9111", "20111111", "398996" );
		logger.info("queryMerOrder_ok1=====retCode: "+ret.getRetCode());
		logger.info("queryMerOrder_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.getRetCode().equals("1139"));
	}
	//@Test
	public void queryMerOrder_error1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = rest.queryMerOrder("9111", "20111110", "398996" );
		logger.info("queryMerOrder_ok1=====retCode: "+ret.getRetCode());
		logger.info("queryMerOrder_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.getRetCode().equals("1139"));
	}
	//@Test
	public void queryMerInfo_ok1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = rest.queryMerInfo("3059");
		logger.info("queryMerInfo_ok1=====retCode: "+ret.getRetCode());
		logger.info("queryMerInfo_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.isRetCode0000());
	}
	
	public void queryMerInfo_ok2(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = rest.queryMerInfo("9996");
	
		logger.info("queryMerInfo_ok1=====retCode: "+ret.getRetCode());
		logger.info("queryMerInfo_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.isRetCode0000());
	}
	//@Test
	public void queryMerInfo_error1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = rest.queryMerInfo("1234");
		logger.info("queryMerInfo_error1=====retCode: "+ret.getRetCode());
		logger.info("queryMerInfo_error1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.getRetCode().equals("1134"));
	}
	//@Test
	public void cancelMonthUserState_ok1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678902");
		MpspMessage ret = rest.cancelMonthUserState("3059","15910709214","305905");
		logger.info("cancelMonthUserState_ok1=====retCode: "+ret.getRetCode());
		logger.info("cancelMonthUserState_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.isRetCode0000());
	}
	
	//@Test
	public void queryMonthUserState_ok1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = rest.queryMonthUserState("9996","15710624951","999602");
		logger.info("queryMonthUserState_ok1=====retCode: "+ret.getRetCode());
		logger.info("queryMonthUserState_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.isRetCode0000());
	}
	
	//@Test
	public void queryMonthUserState_error1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = rest.queryMonthUserState("9996","15010107839","100");
		logger.info("queryMonthUserState_error1=====retCode: "+ret.getRetCode());
		logger.info("queryMonthUserState_error1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.getRetCode().equals("1201"));
	}
	//@Test
	public void checktransacl_ok1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage message=new MpspMessage();
		RequestMsg reqMap = new RequestMsg();
		reqMap.put("merId", "9996");
		reqMap.put("goodsId", "100");
	    reqMap.put("mobileId", "15010107839");
	    reqMap.put("bankId", "XE010000");
		DirectOrderCmd cmd = new DirectOrderCmd(reqMap.getWrappedMap());
		message.put("bankId", "XE010000");
		message.put("iscontrol", "");
		message.put("areacode", "");
		message.put("nettype", "");
		message.put("cardtype", "");
		message.put("grade","");
	
		MpspMessage ret=rest.transacl(message, cmd);
		logger.info("checktransacl_ok1=====retCode: "+ret.getRetCode());
		logger.info("checktransacl_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		
	}
	//@Test
	public void getMobileSeg_error1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = rest.getMobileSeg("19010107839");
		logger.info("getMobileSeg_error1=====retCode: "+ret.getRetCode());
		logger.info("getMobileSeg_error1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.getRetCode().equals("1132"));
	}
	//@Test
	public void getMobileSeg_ok1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = rest.getMobileSeg("15010107839");
		logger.info("getMobileSeg_ok1=====retCode: "+ret.getRetCode());
		logger.info("getMobileSeg_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.isRetCode0000());
	}
	
	
	//@Test
	public void checkSign_ok1(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		RequestMsg reqMap = new RequestMsg();
		reqMap.put("merId", "9996");
		reqMap.put("goodsId", "100");
		reqMap.put("goodsInf", "goods Inf");//TODO
		reqMap.put("mobileId", "15010107839");
		reqMap.put("orderId","321T001");
		reqMap.put("merDate","20111101");
		reqMap.put("amount", "100");//TODO
		reqMap.put("amtType","02");
		reqMap.put("bankType","3");
		reqMap.put("notifyUrl","http://localhost:8080/web");
		reqMap.put("merPriv","");
		reqMap.put("expand","");
		reqMap.put("version","3.0");
		reqMap.put("sign","ABC");
		DirectOrderCmd cmd = new DirectOrderCmd(reqMap.getWrappedMap());
		
		String plainText = cmd.getPlainText();
		String signedText = platSign(plainText);
		MpspMessage ret = rest.checkSign("9996", plainText, signedText);
		logger.info("checkSign_ok1=====retCode: "+ret.getRetCode());
		logger.info("checkSign_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.isRetCode0000());
	}
	
	//@Test
	public void checkSign2_ok2(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		RequestMsg reqMap = new RequestMsg();
		reqMap.put("merId", "9996");
		reqMap.put("goodsId", "100");
		reqMap.put("mobileId", "15010107839");
		reqMap.put("merDate", "20111102");
		reqMap.put("version","3.0");
		reqMap.put("orderId","1234");
		reqMap.put("sign","ABC");
		//DirectOrderCmd cmd = new DirectOrderCmd(reqMap.getWrappedMap());
		
		//String plainText = cmd.getPlainText();
		String paramNew = "";
		paramNew += "merId="+"9996";
		paramNew += "&goodsId="+"100";
		paramNew += "&orderId="+"1234";
		paramNew += "&merDate="+"20111102";
	    paramNew += "&mobileId=" + "15010107839";
	    paramNew += "&version=" + "3.0";
		
		String signedText = platSign(paramNew);
		logger.info("生成的签名为=========================》"+signedText);
		
		MpspMessage ret = rest.checkSign("9996", paramNew, signedText);
		logger.info("checkSign_ok1=====retCode: "+ret.getRetCode());
		logger.info("checkSign_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.isRetCode0000());
	}
	//@Test
	public void checkSign3_ok3(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		RequestMsg reqMap = new RequestMsg();
		reqMap.put("merId", "3059");
		reqMap.put("goodsId", "305905");
		reqMap.put("mobileId", "15910709214");
	    reqMap.put("version", "3.0");
		reqMap.put("sign","ABC");
		//DirectOrderCmd cmd = new DirectOrderCmd(reqMap.getWrappedMap());
		
		//String plainText = cmd.getPlainText();
		String paramNew = "";
		paramNew += "merId="+"3059";
		paramNew += "&goodsId="+"305905";
		paramNew += "&mobileId=" + "15910709214";
		paramNew += "&version=" + "3.0";
		String signedText = platSign(paramNew);
		logger.info("生成的签名为=========================》"+signedText);
		
		MpspMessage ret = rest.checkSign("3059", paramNew, signedText);
		logger.info("checkSign_ok1=====retCode: "+ret.getRetCode());
		logger.info("checkSign_ok1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.isRetCode0000());
	}
	
	public String platSign(String content){
		return platSign(content,"d:/cert/testMer.key.p8");
	}
	
	public String platSign(String content,String keyFile){
		//D:/mobile/workspace/hfWebBusi/src/test/testMer.key.p8
		//String keyFile = "E:/umpaymobile/workspace/hfWebBusi/src/test/testMer.key.p8";
		byte[] key = FileUtil.getFileContent(keyFile);
		PrivateKey pk = SignUtil.genPrivateKey(key);
		byte[] signData = SignUtil.sign(pk, content.getBytes());
		String sign = Base64.encode(signData);
		return sign;

	}
	//@Test
	public void queryMerGoodsInfo(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = rest.queryMerGoodsInfo("9996", "100");
		logger.info("queryMonthUserState_error1=====retCode: "+ret.getRetCode());
		logger.info("queryMonthUserState_error1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.getRetCode().equals("0000"));
	}
	//@Test
	public void checkTrade(){
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, "W999612345678901");
		MpspMessage ret = rest.checkTrade("15110259623","9996", "100");
		logger.info("queryMonthUserState_error1=====retCode: "+ret.getRetCode());
		logger.info("queryMonthUserState_error1=====retCodeBussi: "+ret.getRetCodeBussi());
		Assert.assertTrue(ret.getRetCode().equals("0000"));
	}
	//@Test
	public void testVerify(){
		String plain = "merId=9996&goodsid=100&mobileId=15010107839&orderId=321T001&merDate=20111101&amount=20111101&amtType=02&bankType=3&notifyUrl=http://localhost:8080/web&merPriv=&expand=&version=3.0";
		String sign = "AoGBHmzb0Byb8BY2/xYJYU+IZswX1N6YvUJzaaLyihEfungbPpHX50+ptj7DtfGGXYiLxFIQuCFWp3JHEF2wuXmFkLL4tX/RkcTmprhrHXk83zkiMw7wUphwSladzw+emK6R2SWz3C4+slXO5XXFaba/f85PbcBYHrEFnzD1kAo=";
		try {
			verify(plain,sign);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void verify(String plain,String sign) throws UnsupportedEncodingException{
		String certFile = "D:/mobile/workspace/hfWebBusi/src/config/testMer.cert.crt";
		byte[] spCert = FileUtil.getFileContent(certFile);
		X509Certificate x509cert = SignUtil.genCertificate(spCert);
		logger.info(sign);
		byte[] bytesign = sign.getBytes();
		byte[] bSign = Base64.decode(bytesign);
		boolean result = SignUtil.verify(x509cert, plain.getBytes("gbk"),
				bSign);
		if (!result) {
			result = SignUtil.verify(x509cert, plain.getBytes("utf-8"),
					bSign);
		}
		if (result) {
			logger.info("ok");
		} else {
			logger.info("no");
		}

	}
	
}
