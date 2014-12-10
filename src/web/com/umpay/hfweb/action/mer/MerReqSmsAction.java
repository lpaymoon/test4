package com.umpay.hfweb.action.mer;

import java.util.HashMap;
import java.util.Map;


import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;

/** 
* @ClassName  : MerReqSmsAction 
* @author     : panxingwu
* @date       : 2013-4-10 上午11:39:03 
* @Description: 商户请求平台重发二次确认支付短信
*/
public class MerReqSmsAction extends DirectBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		//1 请求数据校验
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		//组装请求对象
		if(!checkParamResp.isRetCode0000()){
			responseMsg.setRetCode(checkParamResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),(String)checkParamResp.get("message"));
			return;
		}
		logInfo("ParamCheck Result Success[RetCode]:0000:请求参数校验通过");
		
		String merid = requestMsg.getStr(DataDict.MER_REQ_MERID);
		String orderid = requestMsg.getStr(DataDict.MER_REQ_ORDERID);
		String orderDate = requestMsg.getStr(DataDict.MER_REQ_ORDERDATE);
		String sign = requestMsg.getStr(DataDict.MER_REQ_SIGN);
		String version = requestMsg.getStr(DataDict.MER_REQ_VERSION);
		String unSign = "merId="+merid+"&orderId="+orderid+"&orderDate="+orderDate+"&version="+version;
		
		//2 验证商户是否可用此功能
		if(!merAuthService.canAccess(getFunCode(),merid)){
			//商户未开通此项支付服务
			responseMsg.setRetCode("1128");
			logInfo("MerAuthCheck Result Failed[RetCode]:1128:商户访问权限校验未通过");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		logInfo("MerAuthCheck Result Success[RetCode]:0000:商户访问权限校验已通过");
		
		//3 IP验证
		requestMsg.put(DataDict.NET_CLIENTIP+"#FLAG", "true");
		MpspMessage checkIPResp = merAuthService.accessCheck(getFunCode(), merid,requestMsg);
		if(!checkIPResp.isRetCode0000()){
			//特殊访问权限校验未通过
			responseMsg.setRetCode(checkIPResp.getRetCode());
			logInfo("MerAuthCheck special Result Failed[RetCode]:%s:商户访问特殊校验未通过",checkIPResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}	
		logInfo("MerAuthCheck special Result Success[RetCode]:0000:商户访问特殊校验已通过");
		
		//4 商户验签
		MpspMessage checkSignResp = restService.checkSign(merid, unSign, sign);
		if(!checkSignResp.isRetCode0000()){
			responseMsg.setRetCode(checkSignResp.getRetCode());
			responseMsg.setRetCodeBussi(checkSignResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("SignCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"验证商户签名失败");
			return;
		}
		logInfo("SignCheck Result Success[RetCode]0000:商户签名验证通过");
		
		//5 查询订单
		MpspMessage orderInfoResp = restService.queryMerOrder(merid,orderDate,orderid);
		if(!orderInfoResp.isRetCode0000()){
			logInfo("queryOrderInfo Result Failed[RetCode]:%s:%s", orderInfoResp.getRetCode(), "查询订单失败!");
			responseMsg.setRetCode(orderInfoResp.getStr("retCode"));
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		String mobileid = orderInfoResp.getStr(HFBusiDict.MOBILEID);
		String orderstate = orderInfoResp.getStr(HFBusiDict.ORDERSTATE);
		if (orderstate.equals("2")){
			responseMsg.setRetCode("1318");
			logInfo("已经支付成功，不再重复下发确认短信！");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		if (orderstate.equals("1")){
			responseMsg.setRetCode("1319");
			logInfo("订单正在支付中！");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		//6 频率控制
		String checkRateResp = merAuthService.checkChannelPayRate(orderid);
		if(!DataDict.SUCCESS_RET_CODE.equals(checkRateResp)){
			//支付频率控制未通过
			responseMsg.setRetCode(checkRateResp);
			logInfo("PayRate Check Result Failed[RetCode]:%s:支付频率控制未通过",checkRateResp);
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}	
		logInfo("PayRate Check Result Success[RetCode]:0000:支付频率控制已通过");
		
		//7 获取商品信息
		String goodsid = orderInfoResp.getStr(HFBusiDict.GOODSID);
		MpspMessage goodsInfoResp = restService.queryMerGoodsInfo(merid, goodsid);
		if(!goodsInfoResp.isRetCode0000()){
			logInfo("queryOrderInfo Result Failed[RetCode]:%s:%s", goodsInfoResp.getRetCode(), "查询商品信息失败!");
			responseMsg.setRetCode(goodsInfoResp.getStr("retCode"));
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		requestMsg.put(HFBusiDict.GOODSID, goodsid);
		//8 发送确认支付短信
		String goodsname = goodsInfoResp.getStr(HFBusiDict.GOODSNAME);
		String servtype = goodsInfoResp.getStr(HFBusiDict.SERVTYPE);
		String servMonth = goodsInfoResp.getStr(HFBusiDict.SERVMONTH);
		String amount = orderInfoResp.getStr(HFBusiDict.AMOUNT);
		String porderid = orderInfoResp.getStr(HFBusiDict.PORDERID);
		String verifycode = orderInfoResp.getStr(HFBusiDict.VERIFYCODE);
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
		
		//9流程返回
		responseSuccess2Mer(requestMsg,responseMsg);

	}

	private void responseSuccess2Mer(RequestMsg requestMsg,ResponseMsg responseMsg) {
		String retCode = DataDict.SUCCESS_RET_CODE;
		responseMsg.setRetCode0000();
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.GOODSID));
		String orderDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERDATE));
		String version = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_VERSION));

		StringBuffer sb = new StringBuffer();
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append(orderId).append("|");
		sb.append(orderDate).append("|");
		sb.append(retCode).append("|");
		sb.append("支付成功").append("|");
		sb.append(version);
		logInfo("Info2Mer Plain Text:%s",sb.toString());
		String sign = super.platSign(sb.toString());
		logInfo("Info2Mer Signed Text:%s",sign);
		sb.append("|").append(sign);
		responseMsg.setDirectResMsg(sb.toString());
		
	}

	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,ResponseMsg responseMsg) {

		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.GOODSID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String orderDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERDATE));
		String version = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_VERSION));
		StringBuffer sb = new StringBuffer();
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append(orderId).append("|");
		sb.append(orderDate).append("|");
		sb.append(responseMsg.getRetCode()).append("|");
		sb.append(getRetMessage(responseMsg)).append("|");
		sb.append(version);
		logInfo("Info2Mer Plain Text:%s",sb.toString());
		String sign = super.platSign(sb.toString());
		logInfo("Info2Mer Signed Text:%s",sign);
		sb.append("|").append(sign);
		responseMsg.setDirectResMsg(sb.toString());

	}

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_MER_REQ_SMS_MO2;
	}
}
