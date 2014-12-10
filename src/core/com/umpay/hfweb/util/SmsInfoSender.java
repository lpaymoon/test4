package com.umpay.hfweb.util;

import java.util.Map;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.bs2.inf.Datalet2Inf;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.common.HttpClientControler;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.loadstrategy.base.LoadStrategyInf;
 

public class SmsInfoSender implements Datalet2Inf {

	private static Logger log = Logger.getLogger(SmsInfoSender.class);

	private static LoadStrategyInf loadStrategy;

	static{
		loadStrategy = (LoadStrategyInf)SpringContextUtil.getBean("loadStrategy_sms");//初始化短信解析负载组件
	}
    
	public void onData(Object smsMapInfo) {
		Map<String,String> paramMap = (Map<String,String>)smsMapInfo;
		log.info("***执行队列中的发送短信任务****");
		
		/*
		 * 20140304 liujilong 修改短信接口为策略负载模式
		 */
		String retCode = "";
		String url = getSmsSrvPath();
		try {
			MpspMessage respMsg = postHttpRest("http://" + url + "/hfdownsms",
					paramMap);
			retCode = respMsg.getRetCode();
			if (respMsg.isRetCode0000()) {
				log.info("短信发送成功:[rpid]" + paramMap.get(HFBusiDict.RPID) + ","
						+ paramMap.get(HFBusiDict.CALLING));
			} else {
				log.info("短信发送失败:[rpid]" + paramMap.get(HFBusiDict.RPID)
						+ "calling[ " + paramMap.get(HFBusiDict.CALLING)
						+ " ],called [" + paramMap.get(HFBusiDict.CALLED)
						+ " ], retcode[ " + respMsg.getRetCode()
						+ "] retcodebusi[ " + respMsg.getRetCodeBussi() + " ]");
			}
		} finally {
			finish(url, retCode);
		}
		
	}
	
	private  MpspMessage postHttpRest(String urlstr, Map<String,String> paramMap){
		paramMap.put("x-accept-charset", "UTF-8");
		MpspMessage respMessage = new MpspMessage();
		PostMethod post = new PostMethod(urlstr);
		HttpClientControler httpCtrl = (HttpClientControler)SpringContextUtil.getBean("clientCtrl4Sms");
		Map<String,String> retMap = (Map<String,String>)httpCtrl.getHttpResPost_Xstream(paramMap,post);
		if(retMap == null){
			ObjectUtil.logInfo(log,"短信解析未返回信息");
			respMessage.setRetCode(DataDict.SYSTEM_ERROR_CODE);
			respMessage.setRetCodeBussi(DataDict.SYSTEM_ERROR_SMS_NULL);
		}else{
			respMessage.getWrappedMap().putAll(retMap);
			respMessage.setRetCode(ObjectUtil.trim(retMap.get(HFBusiDict.RETCODE)));
			respMessage.setRetCodeBussi(ObjectUtil.trim(retMap.get(HFBusiDict.RETCODE)));
		}
		return respMessage;
	}
	
	/** *****************  方法说明  *****************
	 * method name   :  getSmsSrvPath
	 * @param		 :  @return
	 * @return		 :  String
	 * @author       :  LiuJiLong 2014-3-4 上午10:54:48
	 * description   :  策略负载获得URL
	 * @see          :  
	 * ***********************************************/
	private String getSmsSrvPath(){
		return loadStrategy.lookup();
	}
	
	/** *****************  方法说明  *****************
	 * method name   :  finish
	 * @param		 :  @param url
	 * @param		 :  @param retCode
	 * @param		 :  @param responseMsgGlobal
	 * @return		 :  void
	 * @author       :  LiuJiLong 2014-3-4 上午10:48:12
	 * description   :  策略负载反馈
	 * @see          :  
	 * ***********************************************/
	private void finish(String url, String retCode) {
		loadStrategy.finish(url, retCode);
	}
}
