package com.umpay.hfweb.action.wx;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.bs.mpsp.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;

public class WxHfQueryPlatOrderAction extends WxOrderBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		//检查请求参数
		logInfo("校验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}
		//查询订单信息
		logInfo("查询临时订单信息...");
		MpspMessage queryOrderResp = restService.queryWXPlatOrder(requestMsg.getStr(DataDict.MER_REQ_PLATORDERID));
		if(!queryOrderResp.isRetCode0000()){
			logInfo("WxPlatOrderAction Result Failed[RetCode]:%s:%s", queryOrderResp.getRetCode(), "查询平台订单失败");
			respMap.setRetCode(queryOrderResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("WxPlatOrderAction Result Success[RetCode]:0000:查询平台订单成功");
		responseSuccess(queryOrderResp,requestMsg,respMap);
		
	}

	private void responseSuccess(MpspMessage queryOrderResp,
			RequestMsg requestMsg, ResponseMsg respMap) {
		respMap.setRetCode0000();
		String orderState  = ObjectUtil.trim(queryOrderResp.getStr(HFBusiDict.ORDERSTATE));
		String orderId  = ObjectUtil.trim(queryOrderResp.getStr(HFBusiDict.ORDERID));
		String orderDate  = ObjectUtil.trim(queryOrderResp.getStr(HFBusiDict.ORDERDATE));
		String sign = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_SIGN));
		Map <String,String> map = new HashMap<String,String>();
		
		map.put(HFBusiDict.RETCODE, DataDict.SUCCESS_RET_CODE);
		map.put(HFBusiDict.RETMSG, queryOrderResp.getStr(HFBusiDict.RETMSG));
		//根据商户的错误返回对应的错误码与错误信息
		if("1".equals(StringUtil.trim(orderState))){//平台下单失败
			map.put(HFBusiDict.RETCODE, "1304");
			map.put(HFBusiDict.RETMSG, messageService.getMessage("1304"));
		}else if("2".equals(StringUtil.trim(orderState))){//下单成功
			map.put(HFBusiDict.ORDERID, orderId);
			map.put(HFBusiDict.ORDERDATE, orderDate);
			map.put(HFBusiDict.RETCODE, DataDict.SUCCESS_RET_CODE);
			map.put(HFBusiDict.RETMSG, "下单成功");
		}else if("3".equals(StringUtil.trim(orderState))){//商户下单失败
			map.put(HFBusiDict.RETCODE, ObjectUtil.trim(queryOrderResp.getStr("retcode")));
			map.put(HFBusiDict.RETMSG, messageService.getMessage("8601"+ObjectUtil.trim(queryOrderResp.getStr("retcode"))));
		}else if(!"".equals(StringUtil.trim(orderState))){//STATE 状态错误
			map.put(HFBusiDict.RETCODE, "1304");
			map.put(HFBusiDict.RETMSG, messageService.getMessage("1304"));
		}
		//map.put(DataDict.MER_REQ_VERSION, "1.0");
		//map.put("orderState", orderState);
		//map.put(DataDict.MER_REQ_SIGN, sign);
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据:%s", jsonStr);
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息的时候出现异常:", e);
		}
		respMap.setDirectByteMsg(data);
	}

	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		String retCode = respMap.getStr(DataDict.RET_CODE);
		String retMsg = getRetMessage(respMap);
		//String sign = requestMsg.getStr(DataDict.MER_REQ_SIGN);
		
		Map<String,String> map = new HashMap<String,String>();
		map.put(DataDict.RET_CODE, retCode);
		map.put(DataDict.RET_MSG, retMsg);
		//map.put(DataDict.MER_REQ_VERSION, "1.0");
		//map.put("orderState", "");
		//map.put(DataDict.MER_REQ_SIGN, sign);
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据:%s", jsonStr);
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息的时候出现异常:", e);
		}
		respMap.setDirectByteMsg(data);
	}

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_PLATCX;
	}

	
}
