package com.umpay.hfweb.action.order;


import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectBaseAction;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  WxVerifyPayAction
 * @author     :  xuwei
 * @version    :  1.0  
 * description :  话付宝验证码支付（校验验证码，完成支付）
 * @see        :                        
 * ************************************************/   
public class ChnlVerifyPayAction extends DirectBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		// 1-检查请求参数
		logInfo("检验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr2Mer(requestMsg,respMap);
			return;
		}
		String merId=ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		//2 校验访问权限
		if(!merAuthService.canAccess("QDYZM", merId)){
			//商户未开通此项支付服务
			respMap.setRetCode("1128");
			logInfo("MerAuthCheck merId["+merId+"] Result Failed[RetCode]:1128:商户访问权限校验未通过");
			responseEorr2Mer(requestMsg,respMap);
			return;
		}
		logInfo("MerAuthCheck Result Success[RetCode]:0000:商户访问权限校验已通过");
		String merDate=ObjectUtil.trim(requestMsg.getStr("orderdate"));
		String orderid=ObjectUtil.trim(requestMsg.getStr(HFBusiDict.ORDERID));
		String verifycode=ObjectUtil.trim(requestMsg.getStr(HFBusiDict.VERIFYCODE));
		requestMsg.put(DataDict.MER_REQ_ORDERID, orderid);
		requestMsg.put(DataDict.MER_REQ_VERIFYCODE, verifycode);
		requestMsg.put(DataDict.MER_REQ_MERDATE, merDate);
		//3-验证码校验
		MpspMessage respMessage = restService.checkVerifyCode(requestMsg);
		if(!respMessage.isRetCode0000()){
			logInfo("验证码校验失败,retCode=%s", respMessage.getRetCode());
			respMap.setRetCode(respMessage.getRetCode());
			responseEorr2Mer(requestMsg, respMap);
			return;
		}
		//4-渠道验签
		String chnlId = requestMsg.getStr(DataDict.MER_REQ_CHNLID);//渠道号
		String chnlDate = requestMsg.getStr(DataDict.MER_REQ_CHNLDATE);//渠道日期
		String chnlOrderid = requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID);//渠道订单号
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);//用户手机号
		String goodsid = requestMsg.getStr(DataDict.MER_REQ_GOODSID);
		String channelpriv = requestMsg.getStr(HFBusiDict.CHANNELPRIV);
		String expand = requestMsg.getStr(DataDict.MER_REQ_EXPAND);
		String signstr = requestMsg.getStr(DataDict.MER_REQ_SIGN);
//		String orderid=ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String merid=ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
//		String verifycode=ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_VERIFYCODE));
		String unsignstr = "chnlId="+chnlId+"&chnlOrderId="+chnlOrderid+"&chnlDate="+chnlDate+"&merId="+merid+"&goodsId="+goodsid+"&orderid="+orderid+"&orderdate="+merDate+"&verifycode="+verifycode+"&mobileId="+mobileid+"&channelpriv="+channelpriv+"&expand="+expand;
		MpspMessage checkSignResp = restService.checkChnlSign(chnlId,signstr,unsignstr);
		if(!checkSignResp.isRetCode0000()){
			respMap.setRetCode(checkSignResp.getRetCode());
			responseEorr2Mer(requestMsg,respMap);
			logInfo("QDOrderCreate Result Failed[RetCode]:%s:%s",respMap.getRetCode(),"渠道验签失败");
			return;
		}
		logInfo("渠道验签通过");
		//5-查询订单信息，获取订单验证码和porderid
		MpspMessage QueryOrderResp=restService.queryMerOrder(merId, merDate, orderid);				
		if(!QueryOrderResp.isRetCode0000()){//订单不存在
			logInfo("QueryOrderResp Result Success[RetCode]:0000:查询订单失败");
		    respMap.setRetCode(QueryOrderResp.getRetCode());
		    responseEorr2Mer(requestMsg, respMap);
			return;
		}
		//6-支付
		requestMsg.put(HFBusiDict.BUSINESSTYPE, DataDict.BUSI_QD_YZMZF);//业务区分：渠道验证码下单，需增加新的类型
		
		MpspMessage payResp = tradeService.qdVerifyPay(requestMsg,QueryOrderResp);
		if(!payResp.isRetCode0000()&&!payResp.getRetCode().equals("86011571")){
			logInfo("支付失败[RetCode]:%s",payResp.getRetCode());
			respMap.setRetCode(payResp.getRetCode());
			respMap.put(HFBusiDict.RETMSG, payResp.get(HFBusiDict.RETMSG));
			responseEorr2Mer(requestMsg, respMap);
			return;
		}
		// 3-下发支付成功通知短信
		smsService.pushPayOkSms(payResp.getWrappedMap());
		merId=payResp.getStr(HFBusiDict.MERID);	
		String mobileId=payResp.getStr(HFBusiDict.MOBILEID);
		String send_promotion=ObjectUtil.trim(payResp.getStr("send_mt_promotion"));
		if(send_promotion.equals("true")){ //下发限额提示短信
			String smsContent=payResp.getStr("promotion_msg");
			 smsService.pushSms(merId, mobileId, smsContent);
		}
		
		logInfo("支付成功[RetCode]:0000");
		responseSuccess2Mer(requestMsg,respMap);
	}


	protected void responseSuccess2Mer(RequestMsg requestMsg,final ResponseMsg responseMsg){
		String retCode = DataDict.SUCCESS_RET_CODE;
		responseMsg.setRetCode0000();
		
		String chnlOrderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID));
		String chnlDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLDATE));
		String chnlid = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLID));
		String orderId= ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String orderdate = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.ORDERDATE));


		StringBuffer sb = new StringBuffer();
		sb.append(chnlid).append("|");
		sb.append(chnlOrderId).append("|");
		sb.append(chnlDate).append("|");
		sb.append(orderId).append("|");
		sb.append(orderdate).append("|");
		sb.append(retCode).append("|");
		sb.append("交易成功");
	
		logInfo("Info2Mer Plain Text:%s",sb.toString());
		String sign = super.platSign(sb.toString());
		logInfo("Info2Mer Signed Text:%s",sign);
		sb.append("|").append(sign);
		
		responseMsg.setDirectResMsg(sb.toString());
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_QD_VERIFY_ZF;
	}
	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		// TODO Auto-generated method stub
		String chnlOrderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID));
		String chnlDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLDATE));
		String chnlid = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLID));
		String orderId= ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String orderdate = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.ORDERDATE));
		StringBuffer sb = new StringBuffer();
		sb.append(chnlid).append("|");
		sb.append(chnlOrderId).append("|");
		sb.append(chnlDate).append("|");
		sb.append(orderId).append("|");
		sb.append(orderdate).append("|");
		sb.append(responseMsg.getRetCode()).append("|");
		sb.append(getRetMessage(responseMsg)).append("||");
		responseMsg.setDirectResMsg(sb.toString());
	}
}
