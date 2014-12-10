package com.umpay.hfweb.action.qudao;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;

/** 
* @ClassName	: ChnlQueryOrderAction 
* @Description	: 渠道查询订单
* @author		： panxingwu
* @date			： 2013-5-7 上午10:36:19 
*/
public class ChnlQueryOrderAction  extends DirectBaseAction{

	@Override
	protected void processBussiness(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		//1 请求数据校验
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		logInfo("请求参数为:%s", requestMsg.getWrappedMap());
		if(!checkParamResp.isRetCode0000()){
			responseMsg.setRetCode(checkParamResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),(String)checkParamResp.get("message"));
			return;
		}
		logInfo("参数校验通过");
		
		//2 渠道验签
		logInfo("掉用资源层进行渠道验签");
		String chnlId = requestMsg.getStr(DataDict.MER_REQ_CHNLID);//渠道号
		String chnlDate = requestMsg.getStr(DataDict.MER_REQ_CHNLDATE);//渠道日期
		String chnlOrderid = requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID);//渠道订单号
		String merId =  requestMsg.getStr(DataDict.MER_REQ_MERID);
		String signstr = requestMsg.getStr(DataDict.MER_REQ_SIGN);
		String version = requestMsg.getStr(DataDict.MER_REQ_VERSION);
		String unsignstr = "chnlId="+chnlId+"&chnlOrderId="+chnlOrderid+"&chnlDate="+chnlDate+"&merId="+merId+"&version="+version;
		logInfo("请求资源层验签，签名原串为："+unsignstr);
		MpspMessage checkSignResp = restService.checkChnlSign(chnlId,signstr,unsignstr);
		if(!checkSignResp.isRetCode0000()){
			responseMsg.setRetCode(checkSignResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("QDOrderCreate Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"渠道验签失败");
			return;
		}
		logInfo("渠道验签通过");
		
		//3 调用资源层查询渠道订单信息
		logInfo("调用rest查询渠道订单");
		MpspMessage queryRs = restService.queryQDOrder(requestMsg);
		if(!queryRs.isRetCode0000()){
			responseMsg.setRetCode(queryRs.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("QDOrderCreate Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"渠道验签失败");
			return;
		}
		logInfo("查询订单成功");
		responseSuccess2Mer(requestMsg,queryRs,responseMsg);
	}

	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		logInfo("渠道查询订单失败");
		String chnlOrderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID));
		String chnlDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLDATE));
		String chnlid = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLID));
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String version = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_VERSION));
		StringBuffer sb = new StringBuffer();
		sb.append(chnlid).append("|");
		sb.append(chnlOrderId).append("|");
		sb.append(chnlDate).append("|");
		sb.append(merId).append("|");
		sb.append(version).append("|");
		sb.append(responseMsg.getRetCode()).append("|");
		sb.append(getRetMessage(responseMsg));
		responseMsg.setDirectResMsg(sb.toString());
	}

	protected void responseSuccess2Mer(RequestMsg requestMsg,MpspMessage mpsp,ResponseMsg responseMsg){
		String retCode = DataDict.SUCCESS_RET_CODE;
		responseMsg.setRetCode0000();
		logInfo("渠道查询订单成功");
		String chnlId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLID));//渠道号
		String chnlDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLDATE));//渠道日期
		String chnlOrderid = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID));//渠道订单号
		String mobileid = ObjectUtil.trim(mpsp.getStr(HFBusiDict.MOBILEID));//用户手机号
		String merid = ObjectUtil.trim(mpsp.getStr(HFBusiDict.MERID));
		String goodsid = ObjectUtil.trim(mpsp.getStr(HFBusiDict.GOODSID));
		String version = ObjectUtil.trim(mpsp.getStr(HFBusiDict.VERSION));
		String orderstate = ObjectUtil.trim(mpsp.getStr(HFBusiDict.ORDERSTATE));
		String amount = ObjectUtil.trim(mpsp.getStr(HFBusiDict.AMOUNT));
		
		StringBuffer sb = new StringBuffer();
		sb.append(chnlId).append("|");
		sb.append(chnlOrderid).append("|");
		sb.append(chnlDate).append("|");
		sb.append(merid).append("|");
		sb.append(goodsid).append("|");
		sb.append(mobileid).append("|");
		sb.append(amount).append("|");
		sb.append(orderstate).append("|");
		sb.append(retCode).append("|");
		sb.append("查询订单成功").append("|");
		sb.append(version);
		logInfo("Info2Mer Plain Text:%s",sb.toString());
		String sign = super.platSign(sb.toString());
		logInfo("Info2Mer Signed Text:%s",sign);
		sb.append("|").append(ObjectUtil.trim(sign));
		responseMsg.setDirectResMsg(sb.toString());
	}
	
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_MER_REQ_QDDDCX;
	}

}
