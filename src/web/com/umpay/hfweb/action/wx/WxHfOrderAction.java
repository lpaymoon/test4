package com.umpay.hfweb.action.wx;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  WxHfOrderAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  无线客户端下话费订单（t_order_0/1/2/3/4/5/6）
 * @see        :                        
 * ************************************************/   
public class WxHfOrderAction extends WxOrderBaseAction {

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
		MpspMessage orderSaveResp = tradeService.wxHfOrderSave(requestMsg);
		if(!orderSaveResp.isRetCode0000()){
			logInfo("R4hfOrderSave Result Failed[RetCode]:%s:%s", orderSaveResp.getRetCode(), "客户端向平台下单失败!");
			respMap.setRetCode(orderSaveResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("客户端向平台下单成功!");
		responseSuccess(orderSaveResp,respMap);
	}

	private void responseSuccess(MpspMessage message,ResponseMsg respMap){
		respMap.setRetCode0000();
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg", message.getStr(HFBusiDict.RETMSG));
		map.put("mtNum", message.getStr(HFBusiDict.MTNUM));
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据为:%s", jsonStr);
			data = encryptor.encyptString(jsonStr);
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
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);

	}
	
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_WXHFXD;
	}

}
