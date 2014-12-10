package com.umpay.hfweb.action.wx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Element;
import net.sf.json.JSONObject;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.CommonEcc;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;


/** ******************  类说明  *********************
 * class       :  WxHistoryTransAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  动态密码验证
 * @see        :                        
 * ************************************************/   
public class WxRandomKeyVerifyAction extends WxOrderBaseAction {

	private static final String Sended_SMS_Count= "intSendedCount";
	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		//1.检查请求参数
		logInfo("校验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}
		//2.做缓存，用于限制获取动态码的次数
		CommonEcc ecc = (CommonEcc)AbstractCacheFactory.getInstance().getCacheClient("historyTrans");
		String key = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		Element cache = ecc.getElementByDefaultMap(key,Sended_SMS_Count,new Integer(0));
		Map<String,Object> cacheInfo = (Map<String,Object>)cache.getValue();
		Integer intSendedCount = (Integer)cacheInfo.get(Sended_SMS_Count);
		Integer maxNum = Integer.parseInt(messageService.getSystemParam("WXTRANSGETNUM.MAX"));
		if(intSendedCount>=maxNum){
			logInfo("超过一小时内查询交易记录的次数限制！");
			respMap.setRetCode("86011402");
			responseEorr(requestMsg,respMap);
			return;
		}
		//2.动态密码验证
		MpspMessage resultMap = restService.checkRandomKey(requestMsg);
		String retCode = resultMap.getStr(HFBusiDict.RETCODE);
		if(!"0000".equals(retCode)){
			respMap.setRetCode(retCode);
			responseEorr(requestMsg,respMap);
			return;
		}
		intSendedCount++;
		cacheInfo.put(Sended_SMS_Count, intSendedCount);
		responseSuccess(respMap,resultMap);
	}
	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", respMap.getRetCode());
		map.put("retMsg",getRetMessage(respMap));
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据为:%s", jsonStr);
			data = jsonStr.getBytes("UTF-8");
		} catch (Exception e) {
			logInfo("加密返回信息时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
	}
	private void responseSuccess(ResponseMsg respMap,MpspMessage message){		
		Map <String,String> map = new HashMap<String,String>();
		respMap.setRetCode0000();
		map.put("retCode", respMap.getRetCode());
		map.put("retMsg",getRetMessage(respMap));
		String jsonStr = JSONObject.fromObject(map).toString();
		byte[] data=null;
		try {
			logInfo("返回客户端的数据为:%s", jsonStr);
			data = jsonStr.getBytes("UTF-8");
		} catch (Exception e) {
			logInfo("加密返回信息时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_RANDOMVERIFY;
	}
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request, false);
	}
}
