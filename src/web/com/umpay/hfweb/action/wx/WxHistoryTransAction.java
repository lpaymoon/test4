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
 * description :  无线交易记录查询
 * @see        :                        
 * ************************************************/   
public class WxHistoryTransAction extends WxOrderBaseAction {

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
		//3.查询交易记录
		MpspMessage transMap = restService.getHistoryTrans(requestMsg);
		String responseCode = transMap.getStr(HFBusiDict.RETCODE);
		if(!"0000".equals(responseCode)){
			respMap.setRetCode(retCode);
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("查询交易记录成功！");
		intSendedCount++;
		cacheInfo.put(Sended_SMS_Count, intSendedCount);
		responseSuccess(respMap,transMap);
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
		respMap.setRetCode0000();
		List<Map<String, Object>> list = (List<Map<String, Object>>) message.get("transdata");
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> map = list.get(i);
			String goodsname = (String)map.get(HFBusiDict.GOODSNAME);
			list.get(i).remove(HFBusiDict.GOODSNAME);
			list.get(i).put("goodsName",goodsname);
			if(goodsname.indexOf("|")!=-1){//现在商品名称去的是交易表里的mercustid（格式是：商品号|商品名称）
				list.get(i).put("goodsName", goodsname.substring(goodsname.indexOf("|")+1));
			}
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("retCode", respMap.getRetCode());
		map.put("retMsg",getRetMessage(respMap));
		map.put("transdata",message.get("transdata"));
		map.put("umpayCusPhone", messageService.getSystemParam("UMPCusPhone"));
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
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_HISTORYTRANS;
	}
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request, false);
	}
}
