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
 * class       :  WxSmsAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  无线客户端请求平台重新发送确认支付短信
 * @see        :                        
 * ************************************************/   
public class WxSmsAction extends WxOrderBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		//1-检查请求参数
		logInfo("检验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}
		//2-获取订单信息
		String orderdate = requestMsg.getStr(DataDict.MER_REQ_MERDATE);
		String orderid = requestMsg.getStr(DataDict.MER_REQ_ORDERID);
		String merid = requestMsg.getStr(DataDict.MER_REQ_MERID);
		MpspMessage orderInfoResp = restService.queryMerOrder(merid,orderdate, orderid);
		if(!orderInfoResp.isRetCode0000()){
			logInfo("queryOrderInfo Result Failed[RetCode]:%s:%s", orderInfoResp.getRetCode(), "查询订单失败!");
			respMap.setRetCode(orderInfoResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		String orderstate = orderInfoResp.getStr(HFBusiDict.ORDERSTATE);
		if (orderstate.equals("2")) {
			logInfo("已经支付成功，不再重复下发确认短信！");
			respMap.setRetCode("1318");
			responseEorr(requestMsg,respMap);
			return;
		}
		String goodsid = orderInfoResp.getStr(HFBusiDict.GOODSID);
		//3-获取商品信息
		MpspMessage goodsInfoResp = restService.queryMerGoodsInfo(merid, goodsid);
		if(!goodsInfoResp.isRetCode0000()){
			logInfo("queryOrderInfo Result Failed[RetCode]:%s:%s", goodsInfoResp.getRetCode(), "查询商品信息失败!");
			respMap.setRetCode(goodsInfoResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		//4-组装和重发确认短信
		String goodsname = goodsInfoResp.getStr(HFBusiDict.GOODSNAME);
		String servtype = goodsInfoResp.getStr(HFBusiDict.SERVTYPE);
		String servMonth = goodsInfoResp.getStr(HFBusiDict.SERVMONTH);
		String amount = orderInfoResp.getStr(HFBusiDict.AMOUNT);
		String porderid = orderInfoResp.getStr(HFBusiDict.PORDERID);
		String verifycode = orderInfoResp.getStr(HFBusiDict.VERIFYCODE);
		String mobileid = orderInfoResp.getStr(HFBusiDict.MOBILEID);
		Map<String,String> smsMap = new HashMap<String,String>();
		if(servMonth!=null&&!servMonth.equals("")){
			if(servMonth.equals("1"))
				smsMap.put(HFBusiDict.SERVMONTH,"");
			else
				smsMap.put(HFBusiDict.SERVMONTH,servMonth);//服务月份
		}
		String mers = messageService.getSystemParam("special.called.mer.list","");
		if(mers.toUpperCase().contains("ALL") || mers.contains(merid)){
			porderid="1"+porderid;//新的短信子号为1+9+merid(4)+seq(5)+tableName(1)
		}else{
			if(servtype.equals("3")){
				porderid="8"+goodsid;
			}else{
				porderid="2"+porderid;
			}
		}
		smsMap.put(HFBusiDict.CALLED, messageService.getSystemParam("smsPrex"));//10658008
		smsMap.put(HFBusiDict.CALLING, mobileid);
		smsMap.put(HFBusiDict.VERIFYCODE, verifycode);
		smsMap.put(HFBusiDict.PORDERID, porderid);
		smsMap.put(HFBusiDict.GOODSNAME, goodsname);
		smsMap.put(HFBusiDict.AMOUNT, amount);
		smsMap.put(HFBusiDict.SERVTYPE, servtype);
		smsMap.put(HFBusiDict.BANKID, (String)orderInfoResp.get(HFBusiDict.BANKID));
		smsMap.put(HFBusiDict.GOODSID, (String)orderInfoResp.get(HFBusiDict.GOODSID));
		smsMap.put(HFBusiDict.MERID, (String)orderInfoResp.get(HFBusiDict.MERID));
		smsService.pushPaySms(smsMap);
		logInfo("客户端请求重新发送确认支付短信成功!");
		responseSuccess(goodsInfoResp,respMap);
	}

	private void responseSuccess(MpspMessage message,ResponseMsg respMap){
		respMap.setRetCode0000();
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg", message.getStr(HFBusiDict.RETMSG));
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
		return DataDict.FUNCODE_WX_DXCF;
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
}
