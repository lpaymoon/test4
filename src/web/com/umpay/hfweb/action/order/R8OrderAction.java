package com.umpay.hfweb.action.order;

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
 * class       :  R8OrderAction
 * @author     :  panxingwu 
 * description :  无线商户R8商户下单接口(目前只给重庆使用)
 * @version    :  1.0
 * ***********************************************
 */
public class R8OrderAction extends DirectBaseAction{

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_R8XD;
	}
	@Override
	public void processBussiness(RequestMsg requestMsg, ResponseMsg responseMsg){
		//1-1 请求数据校验
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		//组装请求对象
		DirectOrderCmd cmd = this.populateCommand(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			responseMsg.setRetCode(checkParamResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),(String)checkParamResp.get("message"));
			return;
		}
		logInfo("ParamCheck Result Success[RetCode]:0000:请求参数校验通过");
		//1-2 校验访问权限
		if(!merAuthService.canAccess(getFunCode(), cmd.getMerId())){
			//商户未开通此项支付服务
			responseMsg.setRetCode("1128");
			logInfo("MerAuthCheck merId["+cmd.getMerId()+"] Result Failed[RetCode]:1128:商户访问权限校验未通过");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		logInfo("MerAuthCheck Result Success[RetCode]:0000:商户访问权限校验已通过");
		//1-3 商户下单时间校验
		if(!DateUtil.verifyOrderDate(cmd.getMerDate())){
			responseMsg.setRetCode("1310");
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("VerifyOrderDate Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"商户下单时间校验未通过");
			return;
		}
		logInfo("VerifyOrderDate Result Success[RetCode]:0000:商户下单时间校验通过");
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
		//3-交易鉴权
		MpspMessage checkTradeResp = restService.checkTrade(cmd.getMobileId(), cmd.getMerId(), cmd.getGoodsId());
		if(!checkTradeResp.isRetCode0000()){
			responseMsg.setRetCode(checkTradeResp.getRetCode());
			responseMsg.setRetCodeBussi(checkTradeResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("TradeCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"交易鉴权失败");
			return;
		}
		logInfo("TradeCheck Result Success[RetCode]0000:交易鉴权通过");
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		responseMsg.put(HFBusiDict.PROVCODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.PROVCODE)));
		responseMsg.put(HFBusiDict.AREACODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.AREACODE)));
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		//4-确认可支付银行
		String bankId = (String)checkTradeResp.get(HFBusiDict.BANKID);
		if(ObjectUtil.isEmpty(bankId)){
			//无可支付银行的返回码 <您暂时不能使用支付服务>
			logInfo("BankCheck Result Failed[RetCode]:%s:%s","1303","支付银行校验未通过");
			responseMsg.setRetCode("1303");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		//TODO begin 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-13
		responseMsg.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-13
		logInfo("BankCheck Result Success[RetCode]:0000:可支付银行确认通过");
		//5-交易屏蔽模板校验
		MpspMessage transaclResp = restService.transacl(checkTradeResp, cmd);
		if(!transaclResp.isRetCode0000()){
			responseMsg.setRetCode(transaclResp.getRetCode());
			responseMsg.setRetCodeBussi(transaclResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("Transacl Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"交易屏蔽模板校验失败");
			return;
		}
		logInfo("Transacl Result Success[RetCode]:0000:交易屏蔽模板通过");
		//6-下单	
		cmd.setBusinessType(DataDict.BUSI_R8);
		MpspMessage orderResp = tradeService.saveOrder(bankId,checkTradeResp, cmd);
		if(!orderResp.isRetCode0000()){
			responseMsg.setRetCode(orderResp.getRetCode());
			responseMsg.setRetCodeBussi(orderResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("OrderSave Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"下单失败");
			return;
		}
		logInfo("OrderSave Result Success[RetCode]:0000:下单成功");
		//7-组装成功响应
		responseSuccess2Mer(requestMsg,responseMsg,orderResp);
	}
	
	
	protected  DirectOrderCmd populateCommand(RequestMsg requestMsg){
		return new DirectOrderCmd(requestMsg.getWrappedMap());
	}
	
	protected void responseSuccess2Mer(RequestMsg requestMsg,final ResponseMsg responseMsg, MpspMessage orderResp){
		String retCode = DataDict.SUCCESS_RET_CODE;
		responseMsg.setRetCode0000();
		
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String merDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERDATE));
		String version = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_VERSION));
		String verifyCode = ObjectUtil.trim(orderResp.getStr(HFBusiDict.VERIFYCODE));
		String porderid = ObjectUtil.trim(orderResp.getStr(HFBusiDict.PORDERID));
		
		StringBuffer sb = new StringBuffer();
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append(orderId).append("|");
		sb.append(merDate).append("|");
		sb.append(porderid).append("|");
		sb.append(verifyCode).append("|");
		sb.append(version).append("|");
		sb.append(retCode).append("|");
		sb.append("下订单成功");
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
		String version = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_VERSION));
		StringBuffer sb = new StringBuffer();
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append(orderId).append("|");
		sb.append(merDate).append("|");
		sb.append(version).append("|");
		sb.append(responseMsg.getRetCode()).append("|");
		sb.append(getRetMessage(responseMsg)).append("|");
		logInfo("Info2Mer Plain Text:%s",sb.toString());
		String sign = super.platSign(sb.toString());
		logInfo("Info2Mer Signed Text:%s",sign);
		sb.append("|").append(sign);
		responseMsg.setDirectResMsg(sb.toString());
	}
}
