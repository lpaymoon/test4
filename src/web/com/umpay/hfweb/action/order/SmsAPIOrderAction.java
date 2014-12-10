package com.umpay.hfweb.action.order;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
 * class       :  SmsAPIOrderAction
 * @author     :  chenwei 
 * description :  短信api下单
 * @see        :  2014-04-01
 * @version    :  1.0
 * ***********************************************
 */
public class SmsAPIOrderAction extends DirectBaseAction{

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_DXAPIXD;
	}
	@Override
	public void processBussiness(RequestMsg requestMsg, ResponseMsg responseMsg){
		//1 请求数据校验
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

		//2 校验访问权限
		if(!merAuthService.canAccess(getFunCode(), cmd.getMerId())){
			//商户未开通此项支付服务
			responseMsg.setRetCode("1128");
			logInfo("MerAuthCheck merId["+cmd.getMerId()+"] Result Failed[RetCode]:1128:商户访问权限校验未通过");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		logInfo("MerAuthCheck Result Success[RetCode]:0000:商户访问权限校验已通过");

		//3  商户下单时间校验
		if(!DateUtil.verifyOrderDate(cmd.getMerDate())){
			responseMsg.setRetCode("1310");
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("VerifyOrderDate Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"商户下单时间校验未通过");
			return;
		}
		logInfo("VerifyOrderDate Result Success[RetCode]:0000:商户下单时间校验通过");

		//4 商户验签
		MpspMessage checkSignResp = restService.checkSign(cmd.getMerId(), cmd.getPlainText(), cmd.getSign());
		if(!checkSignResp.isRetCode0000()){
			responseMsg.setRetCode(checkSignResp.getRetCode());
			responseMsg.setRetCodeBussi(checkSignResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("SignCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"验证商户签名失败");
			return;
		}
		logInfo("SignCheck Result Success[RetCode]0000:商户签名验证通过");

		//5 交易鉴权
		MpspMessage checkTradeResp = restService.queryMerGoodsInfo(cmd.getMerId(), cmd.getGoodsId());
		if(!checkTradeResp.isRetCode0000()){
			responseMsg.setRetCode(checkTradeResp.getRetCode());
			responseMsg.setRetCodeBussi(checkTradeResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("TradeCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"交易鉴权失败");
			return;
		}
		logInfo("TradeCheck Result Success[RetCode]0000:交易鉴权通过");
		requestMsg.put(HFBusiDict.EXPIRETIME, checkTradeResp.get(HFBusiDict.EXPIRETIME));

		//6 定价金额校验
		String pricemode = checkTradeResp.getStr(HFBusiDict.PRICEMODE);
		if("0".equals(pricemode)){
			logInfo("定价商品,校验金额开始");
			//如果价格模式是固定价格则获取商品银行信息
			String merid = checkTradeResp.getStr(HFBusiDict.MERID);
			String goodsid = checkTradeResp.getStr(HFBusiDict.GOODSID);
			MpspMessage goodsBankResp = restService.getGoodsBank(merid, goodsid);
			if(!goodsBankResp.isRetCode0000()){
				logInfo("queryMerGoodsInfo Result Failed[RetCode]:%s:%s", goodsBankResp.getRetCode(), "获取商品银行失败");
				responseMsg.setRetCode(goodsBankResp.getRetCode());
				responseEorr2Mer(requestMsg,responseMsg);
				return;
			}
			logInfo("获取商品银行成功");
			List<Map<String, Object>> rs = (List<Map<String, Object>>) goodsBankResp.get(HFBusiDict.GOODSBANKS);
			Map<String,Object> map = rs.get(0);
			long amount = Long.valueOf(requestMsg.getStr(DataDict.MER_REQ_AMOUNT));
			long price = Long.valueOf(map.get(HFBusiDict.AMOUNT).toString());//制定价格
			if(price!=amount){
				logInfo("定价商品，金额不符，制定价格为：%s,输入价格为:%s",price,amount);
				responseMsg.setRetCode("1404");
				responseEorr2Mer(requestMsg,responseMsg);
				return;
			}
			logInfo("金额校验通过");
		}

		//7 保存无线订单表
		String verifyCode = String.valueOf((int)(Math.random()*9000+1000));
		requestMsg.put(HFBusiDict.VERIFYCODE, verifyCode);
		requestMsg.put(HFBusiDict.BUSINESSTYPE, DataDict.BUSI_DXAPI);//短信api下单(0106) chenwei 20140327
		MpspMessage createWxOrderResp = restService.createWxOrder(requestMsg);
		if(!createWxOrderResp.isRetCode0000()){
			responseMsg.setRetCode(createWxOrderResp.getRetCode());
			responseMsg.setRetCodeBussi(createWxOrderResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("SMSAPIOrderSave Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"保存短信API订单失败");
			return;
		}
		logInfo("SMSAPIOrderSave Result Success[RetCode]:0000:下单成功");
		requestMsg.put(HFBusiDict.PORDERID,createWxOrderResp.get(HFBusiDict.PORDERID));

		//8 生成短信回复指令，保存指令、上行端口号、指令生成时间，并保存到数据库中
		//判断是否为测试系统
		boolean test = checkIsTest();
		String smsFrom = "";
		String smsPrex = messageService.getSystemParam("smsPrex");//10658008
		if (test){
			smsFrom += "4";
		}

		String smsCon = cmd.getGoodsId() + "_" + cmd.getOrderId() + "_" + cmd.getMerDate() + "_" + verifyCode;
		String porderid = (String) createWxOrderResp.get(HFBusiDict.PORDERID);
		String smsCalled = smsPrex + smsFrom + "14" + porderid.substring(1);
		String smsMadeTime = DateUtil.getDate(new Date(), "yyyyMMddHHmmss");
		
		logInfo("订单短信指令为：" + smsCon + ";订单端口号为：" + smsCalled + ";订单短信生成时间为：" + smsMadeTime);

		//9 组装成功响应
		responseSuccess2Mer(requestMsg,responseMsg,smsCon,smsCalled,smsMadeTime);
	}
	
	
	protected  DirectOrderCmd populateCommand(RequestMsg requestMsg){
		return new DirectOrderCmd(requestMsg.getWrappedMap());
	}
	
	protected void responseSuccess2Mer(RequestMsg requestMsg,final ResponseMsg responseMsg, String smsCon, String smsCalled, String smsMadeTime){
		String retCode = DataDict.SUCCESS_RET_CODE;
		responseMsg.setRetCode0000();
		
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String merDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERDATE));

		StringBuffer sb = new StringBuffer();
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append(orderId).append("|");
		sb.append(merDate).append("|");
		sb.append(retCode).append("|");
		sb.append("短信API下订单成功").append("|");
		sb.append(smsCon).append("|");
		sb.append(smsCalled).append("|");
		sb.append(smsMadeTime);
		logInfo("Info2Mer(SMSAPI) Plain Text:%s",sb.toString());
		String sign = super.platSign(sb.toString());
		logInfo("Info2Mer(SMSAPI) Signed Text:%s",sign);
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
		sb.append(getRetMessage(responseMsg)).append("|||");
		responseMsg.setDirectResMsg(sb.toString());
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