package com.umpay.hfweb.action.wx;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;

import com.umpay.api.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  WxVerifyRequestAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  获取验证码action
 * @see        :                        
 * ************************************************/   
public class WxVerifyRequestAction extends WxOrderBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		// 1-检查请求参数
		logInfo("检验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}
		//2-获取输入参数
		String merId = requestMsg.getStr(DataDict.MER_REQ_MERID);
		String goodsId = requestMsg.getStr(DataDict.MER_REQ_GOODSID);
		String mobileId = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		String orderDate = requestMsg.getStr(DataDict.MER_REQ_MERDATE);
		long amount = Long.parseLong(requestMsg.getStr(DataDict.MER_REQ_AMOUNT));
		
		//3-商户下单时间校验
		if(!DateUtil.verifyOrderDate(orderDate)){
			respMap.setRetCode("1310");
			logInfo("请求日期超出当前日期前后一天！ merDate=%s", orderDate);
			responseEorr(requestMsg,respMap);
			return;
		}
		
		//4-商户验证
		String permitMers = ObjectUtil.trim(messageService.getSystemParam("wx.verifypay.mer"));
		if(!permitMers.contains(merId) && !permitMers.contains("ALL")){
			logInfo("商户%s未开通验证码支付,开通商户：%s",merId,permitMers);
			respMap.setRetCode("1320");
			responseEorr(requestMsg,respMap);
			return;
		}
		
		//5-交易鉴权
		MpspMessage checkTradeResp = restService.checkTrade(mobileId, merId, goodsId);
		if(!checkTradeResp.isRetCode0000()){
			logInfo("checkTrade Result Failed[RetCode]:%s:%s", checkTradeResp.getRetCode(), "交易鉴权失败");
			respMap.setRetCode(checkTradeResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("checkTrade Result Success[RetCode]0000:交易鉴权通过");
		
		//6-价格校验
		String priceMode = checkTradeResp.getStr(HFBusiDict.PRICEMODE);
		if("0".equals(priceMode)){
			logInfo("定价商品,校验金额开始");
			//如果价格模式是固定价格则获取商品银行信息
			MpspMessage goodsBankResp = restService.getGoodsBank(merId, goodsId);
			if(!goodsBankResp.isRetCode0000()){
				logInfo("queryMerGoodsInfo Result Failed[RetCode]:%s:%s", goodsBankResp.getRetCode(), "获取商品银行失败");
				respMap.setRetCode(goodsBankResp.getRetCode());
				responseEorr(requestMsg,respMap);
				return;
			}
			logInfo("获取商品银行成功");
			List<Map<String, Object>> rs = (List<Map<String, Object>>) goodsBankResp.get(HFBusiDict.GOODSBANKS);
			Map<String,Object> map = rs.get(0);
			long price = Long.valueOf(map.get(HFBusiDict.AMOUNT).toString());//制定价格
			if(amount!=price){
				logInfo("定价商品，金额不符，制定价格为：%s,输入价格为:%s",price,amount);
				respMap.setRetCode("86011008");
				responseEorr(requestMsg,respMap);
				return;
			}
			logInfo("金额校验通过");
		}
		//7-获取验证码
		MpspMessage respMessage = restService.getWxOrderVerifyCode(requestMsg);
		if(!respMessage.isRetCode0000()){
			logInfo("获取动态验证码失败");
			respMap.setRetCode(respMessage.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		
		//8-给用户手机下发动态验证码	
		String verifyCode = respMessage.getStr(HFBusiDict.RANDOMKEY);
		String bankId= StringUtil.trim(requestMsg.getStr(HFBusiDict.BANKID));
//		String smsContent=messageService.getSystemParam(bankId+".WxVerify.SmsContent.Msg");
//		if("".equals(smsContent)){
//	         smsContent=messageService.getSystemParam("WxVerify.SmsContent.Msg");
//		}
		String goodsName = ObjectUtil.trim(checkTradeResp.get(HFBusiDict.GOODSNAME));
//		smsContent=MessageFormat.format(smsContent, verifyCode,goodsName,amount/100);
//		smsService.pushSms(merId, mobileId, smsContent);
		Map<String, Object> smsMap= new HashMap<String,Object>();
		smsMap.put(HFBusiDict.GOODSNAME, goodsName);
		smsMap.put(HFBusiDict.MERID, merId);
		smsMap.put(HFBusiDict.GOODSID, goodsId);
		smsMap.put(HFBusiDict.AMOUNT, requestMsg.getStr(DataDict.MER_REQ_AMOUNT));
		smsMap.put(HFBusiDict.BANKID, bankId);
		smsMap.put(HFBusiDict.VERIFYCODE, verifyCode);
		smsMap.put(HFBusiDict.SERVTYPE, "2");
		smsMap.put(HFBusiDict.MOBILEID, mobileId);
		smsService.pushRandomKeySms(smsMap);
		logInfo("下发动态验证码成功【验证码:%s】", verifyCode);
		responseSuccess(respMap);
	}
	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg =  getRetMessage(respMap);
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

	private void responseSuccess(ResponseMsg respMap) {
		respMap.setRetCode0000();
		String retMsg =  getRetMessage(respMap);
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg", retMsg);
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据:%s",jsonStr);
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
		
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_VERIFY_XIAFA;
	}

}
