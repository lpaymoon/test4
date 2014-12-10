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
* @ClassName	: WxQueryAmtAction 
* @Description	: 查询用户余额和可用限额
* @author		： panxingwu
* @date			： 2013-5-10 上午9:21:59 
*/
public class WxQueryUserInfAction extends WxOrderBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		// 检查请求参数
		logInfo("检验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}
		MpspMessage querInf = tradeService.wxQueryUserInf(requestMsg);
		if(!querInf.isRetCode0000()){
			logInfo("QueryUserinf Result Failed[RetCode]:%s:%s", querInf.getRetCode(), "查询用户信息失败");
			respMap.setRetCode(querInf.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("查询用户信息成功");
		responseSuccess(querInf,respMap);
	}

	private void responseSuccess(MpspMessage message,ResponseMsg respMap){
		String amount  =  ObjectUtil.trim(message.getStr(HFBusiDict.BALANCE));
		String daypayltd =  ObjectUtil.trim(message.getStr(HFBusiDict.DAYPAYLTD));//日可用额度
		String monthpayltd = ObjectUtil.trim(message.getStr(HFBusiDict.MONTHPAYLTD));//月可用额度
		if(Long.parseLong(daypayltd)>Long.parseLong(monthpayltd)){
			daypayltd=monthpayltd;//20130614 panxingwu add
		}
		String cardType =  ObjectUtil.trim(message.getStr(HFBusiDict.CARDTYPE));
		if("".equals(amount)){
			amount="-1";
		}else{
			long amt = Long.parseLong(amount);
			if(amt<0) amount="-1";
		}
		if("2".equals(cardType)||"0".equals(cardType)){
			amount="-1";
		}
		
		respMap.setRetCode0000();
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg", "查询成功!");
		map.put("amount", amount);//余额
		map.put("daypayltd",daypayltd);
		map.put("monthpayltd",monthpayltd);
		map.put("cardType", message.getStr(HFBusiDict.CARDTYPE));
		map.put("provcode", message.getStr(HFBusiDict.PROVCODE));
		map.put("areacode", message.getStr(HFBusiDict.AREACODE));
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
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg = getRetMessage(respMap);
		Map <String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.RETCODE, retCode);
		map.put(HFBusiDict.RETMSG, retMsg);
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
		return DataDict.FUNCODE_WX_YHCX;
	}

	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request, false);
	}
}
