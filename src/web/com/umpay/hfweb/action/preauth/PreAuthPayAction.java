package com.umpay.hfweb.action.preauth;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.action.command.DirectOrderCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.ObjectUtil;
/**
 * ******************  类说明  *********************
 * class       :  PreAuthPayAction
 * @author     :  xuwei 
 * description :  预授权商户请求扣款接口 3.0
 * @see        :  
 * @version    :  1.0
 * ***********************************************
 */
public class PreAuthPayAction extends DirectBaseAction{

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_PREAUTH_HFZF;
	}
	@Override
	public void processBussiness(RequestMsg requestMsg, ResponseMsg responseMsg){
		//1-1 请求数据校验
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);

		if(!checkParamResp.isRetCode0000()){
			responseMsg.setRetCode(checkParamResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),(String)checkParamResp.get("message"));
			return;
		}
		
		//2-商户验签
		MpspMessage checkSignResp = restService.checkSign(requestMsg.get(DataDict.MER_REQ_MERID).toString(), getPlainText(requestMsg), requestMsg.get(DataDict.MER_REQ_SIGN).toString());
		if(!checkSignResp.isRetCode0000()){
			responseMsg.setRetCode(checkSignResp.getRetCode());
			responseMsg.setRetCodeBussi(checkSignResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("SignCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"验证商户签名失败");
			return;
		}
		logInfo("SignCheck Result Success[RetCode]0000:商户签名验证通过");

		//6-调用异步扣款接口
		MpspMessage payResp = null;
	
		payResp = tradeService.preAuthPay(requestMsg);
		if(!payResp.isRetCode0000()){
			responseMsg.setRetCode(payResp.getRetCode());
			responseMsg.setRetCodeBussi(payResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("OrderSave Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"支付失败");
			return;
		}
		logInfo("OrderSave Result Success[RetCode]:0000:支付成功");
		//7-组装成功响应
		responseSuccess2Mer(requestMsg,responseMsg);
	}
	
	
	protected  DirectOrderCmd populateCommand(RequestMsg requestMsg){
		return new DirectOrderCmd(requestMsg.getWrappedMap());
	}
	
	protected void responseSuccess2Mer(RequestMsg requestMsg,final ResponseMsg responseMsg){
		String retCode = DataDict.SUCCESS_RET_CODE;
		responseMsg.setRetCode0000();
		
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String merDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERDATE));
		String version = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_VERSION));
		StringBuffer sb = new StringBuffer();
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append(orderId).append("|");
		sb.append(merDate).append("|");
		sb.append(retCode).append("|");
		sb.append("扣款成功").append("|");
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
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String merDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERDATE));
		StringBuffer sb = new StringBuffer();
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append(orderId).append("|");
		sb.append(merDate).append("|");
		sb.append(responseMsg.getRetCode()).append("|");
		sb.append(getRetMessage(responseMsg)).append("||");
		responseMsg.setDirectResMsg(sb.toString());
	}
	
	protected String getPlainText(RequestMsg requestMsg){
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//商品号
		String goodsId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_GOODSID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		//订单号
		String orderId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_ORDERID));
		//商户日期
		String merDate = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERDATE));
		//交易状态
		String transState = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_TRANSSTATE));
		//商户扩展信息
		String expand = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_EXPAND));

		//版本号
		String version = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_VERSION));
		String content = "merId="+merId+"&goodsId="+goodsId+"&mobileId="+mobileId+"&orderId="+orderId+"&merDate="+merDate
		              +"&transState="+transState+"&expand="+expand+"&version=3.0";
		return content;
	}
}
