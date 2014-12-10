package com.umpay.hfweb.action.wx;

import java.util.HashMap;
import java.util.Map;

import com.umpay.api.paygate.v40.Mer2Plat_v40;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;


/** ******************  类说明  *********************
 * class       :  WxUFNotifyAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  话付宝银行卡支付，支付结果通知
 * 				  （1.接收U付的支付结果通知
 * 				  2.调用trade的商户结果通知接口
 *                3.处理结果响应给U付）
 * @see        :                        
 * ************************************************/   
public class WxUFNotifyAction extends DirectBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg,ResponseMsg responseMsg) {
		//1-1 请求数据校验
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			responseMsg.setRetCode(checkParamResp.getRetCode());
			responseMsg.put("retMsg", "参数校验异常");
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),(String)checkParamResp.get("message"));
			return;
		}
		logInfo("ParamCheck Result Success[RetCode]:0000:请求参数校验通过");
		
		String payretcode = requestMsg.getStr("error_code");
		String merid = requestMsg.getStr("mer_id");
		String orderdid = requestMsg.getStr("order_id");
		String orderdate = requestMsg.getStr("mer_date");
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String orderstate = "2";
		if(!"0000".equals(payretcode)){//支付失败
			orderstate="3";
		}
		Map<String,String> reqMap = new HashMap<String,String>();
		reqMap.put(HFBusiDict.MERID, merid);
		reqMap.put(HFBusiDict.ORDERID, orderdid);
		reqMap.put(HFBusiDict.ORDERDATE, orderdate);
		reqMap.put(HFBusiDict.ORDERSTATE,orderstate);
		reqMap.put(HFBusiDict.RPID, rpid);
		restService.updateOrder(reqMap);//修改订单状态
		//通知商户
		MpspMessage orderSaveResp = tradeService.wxUFNotifyService(requestMsg);
		responseMsg.putAll(orderSaveResp);
		String retCode = orderSaveResp.getStr(HFBusiDict.RETCODE);
		if(!"0000".equals(retCode)){
			responseMsg.setRetCode(retCode);
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		logInfo("通知商户成功!");
		responseSuccess(responseMsg,requestMsg);
		
	}

	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,ResponseMsg responseMsg) {
		Map<String,String> map = new HashMap<String,String>();
		String mer_id = ObjectUtil.trim(requestMsg.getStr("mer_id"));
		String sign_type = ObjectUtil.trim(requestMsg.getStr("sign_type"));
		String version = ObjectUtil.trim(requestMsg.getStr("version"));
		String order_id = ObjectUtil.trim(requestMsg.getStr("order_id")); 
		String mer_date = ObjectUtil.trim(requestMsg.getStr("mer_date")); 
		map.put("mer_id", mer_id);
		map.put("sign_type", sign_type);
		map.put("version", version);
		map.put("order_id", order_id);
		map.put("mer_date", mer_date);
		map.put("ret_code", responseMsg.getStr(HFBusiDict.RETCODE));
		map.put("ret_msg", responseMsg.getStr(HFBusiDict.RETMSG));
	    String str = Mer2Plat_v40.merNotifyResData(map);
		responseMsg.setDirectResMsg(str);
	}
	
	private void  responseSuccess(ResponseMsg responseMsg,RequestMsg requestMsg){
		responseMsg.setRetCode0000();
		Map<String,String> map = new HashMap<String,String>();
		String mer_id = ObjectUtil.trim(requestMsg.getStr("mer_id"));
		String sign_type = ObjectUtil.trim(requestMsg.getStr("sign_type"));
		String version = ObjectUtil.trim(requestMsg.getStr("version"));
		String order_id = ObjectUtil.trim(requestMsg.getStr("order_id")); 
		String mer_date = ObjectUtil.trim(requestMsg.getStr("mer_date")); 
		map.put("mer_id", mer_id);
		map.put("sign_type", sign_type);
		map.put("version", version);
		map.put("order_id", order_id);
		map.put("mer_date", mer_date);
		map.put("ret_code", responseMsg.getStr(HFBusiDict.RETCODE));
	    map.put("ret_msg", "商户结果通知成功");
	    String str = Mer2Plat_v40.merNotifyResData(map);//组装响应数据（生成U付平台需要的格式以及加上签名）
		responseMsg.setDirectResMsg(str);
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WXUFTZ;
	}
}
