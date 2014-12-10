package com.umpay.hfweb.action.wx;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;

/** 
* @ClassName	: WxUserReplyAction 
* @Description	: 用户反馈接口，用以记录用户反馈信息
* @author		： panxingwu
* @date			： 2013-5-6 下午4:04:20 
*/
public class WxUserReplyAction extends WxOrderBaseAction{

	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		// 1-检查请求参数
		logInfo("检验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("参数校验通过,调用资源层保存反馈信息");
		
		//2-调用资源层进行信息保存
		MpspMessage resultMap= null;
		try{
			resultMap= restService.addWxUserReplyInf(requestMsg);
		}catch(Exception e){
			logInfo("保存用户反馈信息失败："+e.getMessage());
			respMap.setRetCode("9999");
			responseEorr(requestMsg,respMap);
			return;
		}
		if(!resultMap.isRetCode0000()){
			logInfo("保存用户反馈信息失败");
			respMap.setRetCode(resultMap.getStr(HFBusiDict.RETCODE));
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("保存用户反馈信息成功");
		responseSuccess(respMap,resultMap);
	}

	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		Map <String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.RETCODE, retCode);
		map.put(HFBusiDict.RETMSG, "保存用户反馈信息失败!");
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
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("retCode", respMap.getRetCode());
		map.put("retMsg","保存用户反馈信息成功");
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
		return DataDict.FUNCODE_WX_YHFK;
	}
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request, false);
	}
}
