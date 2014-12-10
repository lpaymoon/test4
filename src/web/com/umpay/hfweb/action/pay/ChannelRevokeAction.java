package com.umpay.hfweb.action.pay;
import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.action.command.ChannelRevokeCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.ObjectUtil;
/**
 * ******************  类说明  *********************
 * class       :  ChannelRevokeAction
 * @author     :  yangwr 
 * description :  商户直接冲正
 * @see        :  
 * @version    :  1.0
 * ***********************************************
 */
public class ChannelRevokeAction extends DirectBaseAction{

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_QDHFCZ;
	}
	@Override
	public void processBussiness(RequestMsg requestMsg, ResponseMsg responseMsg){
		//1-1 请求数据校验
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		//组装请求对象
		ChannelRevokeCmd cmd = this.populateCommand(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			responseMsg.setRetCode(checkParamResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),(String)checkParamResp.get("message"));
			return;
		}
		logInfo("ParamCheck Result Success[RetCode]:0000:请求参数校验通过");
		//1-2 商户交易时间校验
		//渠道支付接口中没有商户日期，为保证账期一致，商户日期不允许跨天
//		if(!DateUtil.verifyOrderDateStrict(cmd.getMerDate())){
//			responseMsg.setRetCode("1310");
//			responseEorr2Mer(requestMsg,responseMsg);
//			logInfo("VerifyOrderDate Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"商户交易时间校验未通过");
//			return;
//		}
		logInfo("VerifyOrderDate Result Success[RetCode]:0000:商户交易时间校验通过");
		//1-3 校验访问权限
		if(!merAuthService.canAccess(getFunCode(), cmd.getMerId())){
			//商户未开通此项支付服务
			responseMsg.setRetCode("1128");
			logInfo("MerAuthCheck Result Failed[RetCode]:1128:商户访问权限校验未通过");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		logInfo("MerAuthCheck Result Success[RetCode]:0000:商户访问权限校验已通过");
		//1-4 特殊访问权限校验,IP校验等
		requestMsg.put(DataDict.NET_CLIENTIP+"#FLAG", "true");
		MpspMessage checkIPResp = merAuthService.accessCheck(getFunCode(), cmd.getMerId(),requestMsg);
		if(!checkIPResp.isRetCode0000()){
			//特殊访问权限校验未通过
			responseMsg.setRetCode(checkIPResp.getRetCode());
			logInfo("MerAuthCheck special Result Failed[RetCode]:%s:商户访问特殊校验未通过",checkIPResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}	
		logInfo("MerAuthCheck special Result Success[RetCode]:0000:商户访问特殊校验已通过");
		//2-商户验签
		MpspMessage checkSignResp = restService.checkSign(cmd.getMerId(), cmd.getPlainText(), cmd.getSign());
		if(!checkSignResp.isRetCode0000()){
			responseMsg.setRetCode(checkSignResp.getRetCode());
			responseMsg.setRetCodeBussi(checkSignResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("SignCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"验证商户签名失败");
			return;
		}
		logInfo("SignCheck Result Success[RetCode]0000:商户签名验证通过");
		//3-交易鉴权 省略
		//4-确认可支付银行  省略
		//5-交易屏蔽模板校验  省略
		//6-冲正
		MpspMessage tradeResp = tradeService.channelRevoke(cmd);
		if(!tradeResp.isRetCode0000()){
			responseMsg.setRetCode(tradeResp.getRetCode());
			responseMsg.setRetCodeBussi(tradeResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("QDZF Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"冲正失败");
			return;
		}
		logInfo("QDCZ Result Success[RetCode]:0000:冲正成功");
		//7-组装成功响应
		responseSuccess2Mer(requestMsg,responseMsg);
	}
	
	
	protected  ChannelRevokeCmd populateCommand(RequestMsg requestMsg){
		return new ChannelRevokeCmd(requestMsg.getWrappedMap());
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
		sb.append("冲正成功").append("|");
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
}

