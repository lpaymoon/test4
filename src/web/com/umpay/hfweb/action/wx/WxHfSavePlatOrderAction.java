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

public class WxHfSavePlatOrderAction extends WxOrderBaseAction {

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
		logInfo("开始下平台临时订单...");
		MpspMessage queryOrderResp = restService.saveWXPlatOrder(requestMsg);
		if(!queryOrderResp.isRetCode0000()){
			logInfo("WxHfSavePlatOrderAction Result Failed[RetCode]:%s:%s", queryOrderResp.getRetCode(), "无线平台临时订单失败");
			respMap.setRetCode(queryOrderResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("WxHfSavePlatOrderAction Result Success[RetCode]:0000:平台临时订单成功");
		responseSuccess(queryOrderResp,requestMsg,respMap);
		
	}

	private void responseSuccess(MpspMessage queryOrderResp,
			RequestMsg requestMsg, ResponseMsg respMap) {
		respMap.setRetCode0000();
		//String orderState  = ObjectUtil.trim(queryOrderResp.getStr(HFBusiDict.ORDERSTATE));
		//String sign = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_SIGN));
		Map <String,String> map = new HashMap<String,String>();
		
		map.put(HFBusiDict.RETCODE, DataDict.SUCCESS_RET_CODE);
		map.put(HFBusiDict.RETMSG, queryOrderResp.getStr(HFBusiDict.RETMSG));
//		calledMO1	(106580081016+MERID)
//		calledMO2	(1065800819+PRODERID)
//		msgConMO1	platOrderId+配置项
//		msgConMO2	(固定 8 ，MO2短信内容) 

		String merId = ObjectUtil.trim(queryOrderResp.getStr(HFBusiDict.MERID));
		String pOrderId = ObjectUtil.trim(queryOrderResp.getStr(HFBusiDict.PORDERID));
		String platOrderId = ObjectUtil.trim(queryOrderResp.getStr(HFBusiDict.PLATORDERID));
		String goodsdesc = ObjectUtil.trim(queryOrderResp.getStr(HFBusiDict.GOODSDESC));

		//判断是否为测试系统
		boolean test = checkIsTest();
//		String smsFrom = "";
//		String smsPrex = messageService.getSystemParam("smsPrex");
		if (test){
			map.put("mo2Called", "1065800842" + pOrderId);
			map.put("mo1Called", "1065800841016" + merId);
		}else{
			map.put("mo2Called", "106580082" + pOrderId);
			map.put("mo1Called", "106580081016" + merId);
		}
		
		map.put("mo1Msg", platOrderId + "#" + goodsdesc);
		map.put("mo2Msg", "8");
		map.put("platOrderId", platOrderId);
		
		//map.put(DataDict.MER_REQ_VERSION, "1.0");
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
		return DataDict.FUNCODE_WX_PLATXD;
	}

	/**
     * 校验系统类型
     * @return
     */
    private boolean checkIsTest(){
    	boolean rnt = false;
		// 0:生产系统 其他：测试系统
		String type = messageService.getSystemParam("SystemType");
		if (!type.equals("0")) {
			rnt = true;
		}
		return rnt;
    }
}
