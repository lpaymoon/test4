package com.umpay.hfweb.action.order;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  ChnlCodeOrderAction
 * @author     :  xuwei
 * @version    :  1.0  
 * description :  渠道验证码下单
 * @see        :                        
 * ************************************************/   
public class ChnlVerifyOrderAction extends DirectBaseAction{

	@Override
	protected void processBussiness(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		//1 请求数据校验
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			responseMsg.setRetCode(checkParamResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),(String)checkParamResp.get("message"));
			return;
		}
		logInfo("ParamCheck Result Success[RetCode]:0000:请求参数校验通过");
	
		String merid = requestMsg.getStr(DataDict.MER_REQ_MERID);
		//2 校验访问权限
		if(!merAuthService.canAccess("QDYZM", merid)){
			//商户未开通此项支付服务
			responseMsg.setRetCode("1128");
			logInfo("MerAuthCheck merId["+merid+"] Result Failed[RetCode]:1128:商户访问权限校验未通过");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		logInfo("MerAuthCheck Result Success[RetCode]:0000:商户访问权限校验已通过");
		//1渠道验签
		String chnlId = requestMsg.getStr(DataDict.MER_REQ_CHNLID);//渠道号
		String chnlDate = requestMsg.getStr(DataDict.MER_REQ_CHNLDATE);//渠道日期
		String chnlOrderid = requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID);//渠道订单号
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);//用户手机号
		String goodsid = requestMsg.getStr(DataDict.MER_REQ_GOODSID);
		String version = requestMsg.getStr(DataDict.MER_REQ_VERSION);
		String amount = requestMsg.getStr(DataDict.MER_REQ_AMOUNT);
		String chnlPriv = requestMsg.getStr(DataDict.MER_REQ_CHNLPRIV);
		String expand = requestMsg.getStr(DataDict.MER_REQ_EXPAND);
		String signstr = requestMsg.getStr(DataDict.MER_REQ_SIGN);
		String goodsInf = requestMsg.getStr(DataDict.MER_REQ_GOODSINF);
		String unsignstr = "chnlId="+chnlId+"&chnlOrderId="+chnlOrderid+"&chnlDate="+chnlDate+"&merId="+merid+"&goodsId="+goodsid+"&goodsInf="+goodsInf+"&amount="+amount+"&mobileId="+mobileid+"&chnlPriv="+chnlPriv+"&expand="+expand+"&version="+version;
		MpspMessage checkSignResp = restService.checkChnlSign(chnlId,signstr,unsignstr);
		if(!checkSignResp.isRetCode0000()){
			responseMsg.setRetCode(checkSignResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("QDOrderCreate Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"渠道验签失败");
			return;
		}
		logInfo("渠道验签通过");
		requestMsg.put(HFBusiDict.BUSINESSTYPE, DataDict.BUSI_QD_YZMZF);//业务区分：渠道验证码下单，需增加新的类型
		MpspMessage payResp = tradeService.qdVerifyOrder(requestMsg);
		if(!payResp.isRetCode0000()){
			responseMsg.setRetCode(payResp.getRetCode());
			String orderid = payResp.getStr(HFBusiDict.ORDERID);
			String orderdate = payResp.getStr(HFBusiDict.ORDERDATE);
			requestMsg.put(HFBusiDict.ORDERID, orderid);
			requestMsg.put(HFBusiDict.ORDERDATE, orderdate);
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("QDOrderCreate Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"向业务层下单失败");
			return;
		}
		logInfo("渠道验证码下单成功");
		responseSuccess2Mer(requestMsg,responseMsg,payResp);
	}

	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,ResponseMsg responseMsg) {
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		String mobileId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MOBILEID));
		String chnlOrderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID));
		String chnlDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLDATE));
		String chnlid = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLID));
		String orderId= ObjectUtil.trim(requestMsg.getStr(HFBusiDict.ORDERID));
		String orderdate = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.ORDERDATE));
		StringBuffer sb = new StringBuffer();
		sb.append(chnlid).append("|");
		sb.append(chnlOrderId).append("|");
		sb.append(chnlDate).append("|");
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append(orderId).append("|");//panxingwu 20130708 add
		sb.append(orderdate).append("|");//panxingwu 20130708 add
		sb.append(mobileId).append("|");
		sb.append(responseMsg.getRetCode()).append("|");
		sb.append(getRetMessage(responseMsg)).append("|");
		sb.append("3.0");//panxingwu 20130708 add
		logInfo("Info2Mer Plain Text:%s",sb.toString());
		String sign = super.platSign(sb.toString());
		logInfo("Info2Mer Signed Text:%s",sign);
		sb.append("|").append(sign);//panxingwu 20130708 add
		responseMsg.put(HFBusiDict.ORDERID, orderId);
		responseMsg.put(HFBusiDict.ORDERDATE, orderdate);
		responseMsg.setDirectResMsg(sb.toString());
		
	}
	private void logInfo(Object object) {
		// TODO Auto-generated method stub
		
	}

	protected void responseSuccess2Mer(RequestMsg requestMsg,final ResponseMsg responseMsg,MpspMessage mpsp){
		String retCode = DataDict.SUCCESS_RET_CODE;
		responseMsg.setRetCode0000();
		
		String chnlId = requestMsg.getStr(DataDict.MER_REQ_CHNLID);//渠道号
		String chnlDate = requestMsg.getStr(DataDict.MER_REQ_CHNLDATE);//渠道日期
		String chnlOrderid = requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID);//渠道订单号
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);//用户手机号
		String merid = requestMsg.getStr(DataDict.MER_REQ_MERID);
		String goodsid = requestMsg.getStr(DataDict.MER_REQ_GOODSID);
		String version = requestMsg.getStr(DataDict.MER_REQ_VERSION);
		String orderid = mpsp.getStr(HFBusiDict.ORDERID);
		String orderdate = mpsp.getStr(HFBusiDict.ORDERDATE);
		
		StringBuffer sb = new StringBuffer();
		sb.append(chnlId).append("|");
		sb.append(chnlOrderid).append("|");
		sb.append(chnlDate).append("|");
		sb.append(merid).append("|");
		sb.append(goodsid).append("|");
		sb.append(orderid).append("|");
		sb.append(orderdate).append("|");
		sb.append(mobileid).append("|");
		sb.append(retCode).append("|");
		sb.append("下单成功").append("|");
		sb.append(version);
		logInfo("Info2Mer Plain Text:%s",sb.toString());
		String sign = super.platSign(sb.toString());
		logInfo("Info2Mer Signed Text:%s",sign);
		sb.append("|").append(sign);
		responseMsg.put(HFBusiDict.ORDERID, orderid);
		responseMsg.put(HFBusiDict.ORDERDATE, orderdate);
		responseMsg.setDirectResMsg(sb.toString());
	}
	@Override
	protected String getFunCode() {
		 return DataDict.FUNCODE_QD_VERIFY_XD;
	}

}
