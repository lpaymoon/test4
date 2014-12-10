package com.umpay.hfweb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.bs2.inf.Datalet2Inf;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.common.HttpClientControler;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.loadstrategy.base.LoadStrategyInf;
 

public class AlarmInfoSender implements Datalet2Inf {

	private static Logger log = Logger.getLogger(AlarmInfoSender.class);

	private static LoadStrategyInf loadStrategy;

	static{
		loadStrategy = (LoadStrategyInf)SpringContextUtil.getBean("loadStrategy_alarm");//初始化短信解析负载组件
	}
    
	public void onData(Object smsMapInfo) {
		Map<String,String> paramMap = (Map<String,String>)smsMapInfo;
		List<Map> list = new ArrayList<Map>();
		list.add(paramMap);
		log.info("***执行队列中的发送报警任务****");
		
		String retCode = "";
		String url = getSrvPath();
		try {
			MpspMessage respMsg = postHttpRest("http://" + url + "/hfalarm",
					list);
			retCode = respMsg.getRetCode();
			if (respMsg.isRetCode0000()) {
				log.info("报警信息发送成功:[rpid]" + paramMap.get(HFBusiDict.RPID));
			} else {
				log.info("报警信息发送失败:[rpid]" + paramMap.get(HFBusiDict.RPID)
						+ " ], retcode[ " + respMsg.getRetCode()
						+ "] retcodebusi[ " + respMsg.getRetCodeBussi() + " ]");
			}
		} finally {
			finish(url, retCode);
		}
		
	}
	
	private  MpspMessage postHttpRest(String urlstr, List paramMap){
		MpspMessage respMessage = new MpspMessage();
		PostMethod post = new PostMethod(urlstr);
		HttpClientControler httpCtrl = (HttpClientControler)SpringContextUtil.getBean("clientCtrl4Alarm");
		Map<String,String> retMap = (Map<String,String>)httpCtrl.getHttpResPost_Xstream(paramMap,post);
		if(retMap == null){
			ObjectUtil.logInfo(log,"报警中心未返回信息");
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
	private String getSrvPath(){
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
