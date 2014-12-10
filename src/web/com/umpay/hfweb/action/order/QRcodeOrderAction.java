package com.umpay.hfweb.action.order;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import com.bs.mpsp.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  WxQRcodeOrderAction
 * @author     :  xuwei
 * @version    :  1.0  
 * description :  无线二维码下单
 * @see        :                        
 * ************************************************/   
public class QRcodeOrderAction extends DirectBaseAction {
	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		
	//	merId,goodsId,mobileId,orderId,merDate,amount,amtType,bankType,retUrl,[notifyUrl],[merPriv],[expand],bankId,version,sign
		requestMsg.setFunCode(getFunCode());
		PageOrderCmd cmd = new PageOrderCmd(requestMsg.getWrappedMap());
		// 1-检查请求参数
		logInfo("检验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr2Mer(requestMsg,respMap);
			return;
		}
		logInfo("参数校验通过");
	
//		long goodsPrice = Long.valueOf(requestMsg.getStr(DataDict.MER_REQ_AMOUNT));
		String merDate = requestMsg.getStr(DataDict.MER_REQ_MERDATE);
		String merId= requestMsg.getStr(DataDict.MER_REQ_MERID);
		//1-2 校验访问权限
		if(!merAuthService.canAccess(getFunCode(), merId)){
			//商户未开通此项支付服务
			respMap.setRetCode("1128");
			logInfo("MerAuthCheck merId["+merId+"] Result Failed[RetCode]:1128:商户访问权限校验未通过");
			responseEorr2Mer(requestMsg,respMap);
			return;
		}
	    logInfo("商户访问权限校验成功");
	    
		// 特殊访问权限校验,IP校验
	    String  isCheckIp= messageService.getSystemParam("IsCheckIP.QRCODE","false");
		requestMsg.put(DataDict.NET_CLIENTIP+"#FLAG", isCheckIp);
		MpspMessage checkIPResp = merAuthService.accessCheck(getFunCode(),"QRCODE",requestMsg);
		if(!checkIPResp.isRetCode0000()){
			//特殊访问权限校验未通过
			respMap.setRetCode(checkIPResp.getRetCode());
			logInfo("MerAuthCheck special Result Failed[RetCode]:%s:访问IP校验未通过",checkIPResp.getRetCode());
			responseEorr2Mer(requestMsg,respMap);
			return ;
		}
		logInfo("MerAuthCheck special Result Success[RetCode]:0000:访问IP校验已通过");
				
		// 2-下单时间校验
		if(!DateUtil.verifyOrderDate(merDate)){
			respMap.setRetCode("1310");
			logInfo("请求日期超出当前日期前后一天！ merDate=%s", merDate);
			responseEorr2Mer(requestMsg,respMap);
			return;
		}
	
		//3-商户验签
	    String sign=requestMsg.getStr(DataDict.MER_REQ_SIGN);
		MpspMessage checkSignResp = restService.checkSign(merId, cmd.getPlainText(), sign);
		if(!checkSignResp.isRetCode0000()){
			logInfo("SignCheck Result Failed[RetCode]:%s:%s", checkSignResp.getRetCode(), "验证商户签名失败");
			respMap.put(DataDict.RET_CODE, checkSignResp.getRetCode());
			respMap.put(DataDict.RET_CODE_BUSSI, checkSignResp.getRetCodeBussi());
			respMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(checkSignResp.getRetCode()));
			responseEorr2Mer(requestMsg,respMap);
			return;
		}
		logInfo("SignCheck Result Success[RetCode]0000:商户签名验证通过");
		String mobileId=requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		String goodsId=requestMsg.getStr(DataDict.MER_REQ_GOODSID);
		//4-交易鉴权
		MpspMessage checkTradeResp = restService.checkTrade(mobileId, merId, goodsId);
		if(!checkTradeResp.isRetCode0000()){
			respMap.setRetCode(checkTradeResp.getRetCode());
			respMap.setRetCodeBussi(checkTradeResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,respMap);
			logInfo("TradeCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),"交易鉴权失败");
			return;
		}
		logInfo("TradeCheck Result Success[RetCode]0000:交易鉴权通过");
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		respMap.put(HFBusiDict.PROVCODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.PROVCODE)));
		respMap.put(HFBusiDict.AREACODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.AREACODE)));
		respMap.put(HFBusiDict.GOODSNAME,ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.GOODSNAME)));
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		//4-确认可支付银行
		String bankId = (String)checkTradeResp.get(HFBusiDict.BANKID);
		if(ObjectUtil.isEmpty(bankId)){
			//无可支付银行的返回码 <您暂时不能使用支付服务>
			logInfo("BankCheck Result Failed[RetCode]:%s:%s","1303","支付银行校验未通过");
			respMap.setRetCode("1303");
			responseEorr2Mer(requestMsg,respMap);
			return;
		}
		//TODO begin 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-13
		respMap.put(HFBusiDict.BANKID, bankId);
		logInfo("BankCheck Result Success[RetCode]:0000:可支付银行确认通过");
		//5-交易屏蔽模板校验
		MpspMessage transaclResp = restService.transacl(checkTradeResp, cmd);
		if(!transaclResp.isRetCode0000()){
			respMap.setRetCode(transaclResp.getRetCode());
			respMap.setRetCodeBussi(transaclResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,respMap);
			logInfo("Transacl Result Failed[RetCode]:%s:%s",respMap.getRetCode(),"交易屏蔽模板校验失败");
			return;
		}
		logInfo("Transacl Result Success[RetCode]:0000:交易屏蔽模板通过");

		
		//6-保存订单表
		cmd.setBusinessType(DataDict.BUSI_QRCODE);//支付类型
		MpspMessage createOrderResp = tradeService.saveOrder(bankId, checkTradeResp, cmd);
		if(!createOrderResp.isRetCode0000()){
			logInfo("createOrder Result Failed[RetCode]:%s:%s", createOrderResp.getRetCode(), "保存订单失败");
			if(createOrderResp.getRetCode().equals("1304")){//判断订单是否可继续支付
				//查询订单信息
				String orderid=ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
				MpspMessage QueryOrderResp=restService.queryMerOrder(merId, merDate, orderid);				
				if(QueryOrderResp.isRetCode0000()){//订单已存在
					logInfo("QueryOrderResp Result Success[RetCode]:0000:查询订单成功");
					respMap.setRetCode(createOrderResp.getRetCode());
					responseOrderError(QueryOrderResp,requestMsg,respMap);
					return;
				}
			}
			respMap.setRetCode(createOrderResp.getRetCode());
			responseEorr2Mer(requestMsg,respMap);
			return;
		}
		logInfo("createOrder Result Success[RetCode]:0000:下单成功");
		requestMsg.put(HFBusiDict.PORDERID,createOrderResp.get(HFBusiDict.PORDERID));
		requestMsg.put(HFBusiDict.VERIFYCODE,createOrderResp.get(HFBusiDict.VERIFYCODE));
		responseSuccess(createOrderResp,requestMsg,respMap);
		
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_QRCODE;
	}
	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg, ResponseMsg respMap) {
//		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
//		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String goodsname = ObjectUtil.trim(respMap.getStr(HFBusiDict.GOODSNAME));
		String bankid = ObjectUtil.trim(respMap.getStr(HFBusiDict.BANKID));
		String expiretime = ObjectUtil.trim(respMap.getStr(HFBusiDict.EXPIRETIME));
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg =messageService.getMessageDetail(retCode);
		if(retMsg==null||retMsg.equals(""))
		{
			retMsg =  getRetMessage(respMap);
		}
//		String detailMsg=messageService.getMessageDetail(respMap.getRetCode());
//		messageService.getMessageDetail(DataDict.SYSTEM_ERROR_CODE);
		Map <String,String> map = new HashMap<String,String>();
		map.put(DataDict.MER_REQ_ORDERID, orderId);
		map.put(HFBusiDict.GOODSNAME, goodsname);
		map.put(HFBusiDict.RETCODE, retCode);
		map.put(HFBusiDict.RETMSG, retMsg);
		
		StringBuffer sb = new StringBuffer();
		sb.append(goodsname).append("|");
		sb.append(orderId).append("|");
		sb.append(expiretime).append("|");
		sb.append(bankid).append("|||");
		sb.append(retCode).append("|");
		sb.append(retMsg);
		respMap.setDirectResMsg(sb.toString());
	}
	private void responseSuccess(MpspMessage mpspMessage,RequestMsg requestMsg, ResponseMsg respMap){
		respMap.setRetCode0000();
		String veryCode = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.VERIFYCODE));
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		String porderId= ObjectUtil.trim(requestMsg.getStr(HFBusiDict.PORDERID));
		String servtype=ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.SERVTYPE));
		String goodsId=ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.GOODSID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String expiretime=ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.EXPIRETIME));
		String bankid=ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.BANKID));
		String goodsname = ObjectUtil.trim(respMap.getStr(HFBusiDict.GOODSNAME));
		Map <String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.GOODSNAME, goodsname);
		map.put("orderId", orderId);
		map.put("expiretime", expiretime);
		map.put("bankid", bankid);
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg", mpspMessage.getStr(HFBusiDict.RETMSG));
	
		//判断是否为测试系统
		boolean test = checkIsTest();
		String smsFrom = "";
		String smsPrex = messageService.getSystemParam("smsPrex");
		if (test){
			smsFrom += "4";
		}
	
		String merIds = messageService.getSystemParam("special.called.mer.list","");
		if(merIds.contains(merId)||merIds.contains("ALL")){
			porderId="1"+porderId;//新的短信子号为1+9+merid(4)+seq(5)+tableName(1)
		}else{
			if(servtype.equals("3")){
				porderId="8"+goodsId;
			}else{
				porderId="2"+porderId;
			}
		}
		String mo1Called = smsPrex+smsFrom+porderId;
		map.put("moCalled", mo1Called);
		map.put("moMsg", veryCode);
//		String jsonStr = JSONObject.fromObject(map).toString();
//		//加密
//		byte[] data=null;
//		try {
//			logInfo("返回客户端的数据:%s",jsonStr);
////			data = encryptor.encyptString(jsonStr);
//			data = jsonStr.getBytes("UTF-8");
//		} catch (Exception e) {
//			logInfo("加密返回信息出现异常:%s", e);
//		}
		StringBuffer sb = new StringBuffer();
		sb.append(goodsname).append("|");
		sb.append(orderId).append("|");
		sb.append(expiretime).append("|");
		sb.append(bankid).append("|");
		sb.append(mo1Called).append("|");
		sb.append(veryCode).append("|");
		sb.append(DataDict.SUCCESS_RET_CODE).append("|");
		sb.append(mpspMessage.getStr(HFBusiDict.RETMSG));
		respMap.setDirectResMsg(sb.toString());
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
   
	
	/**
	 * 订单已存在时，返回订单信息和用户上行长号，短信内容
	 * @param mpspMessage
	 * @param requestMsg
	 * @param respMap
	 */
	private void responseOrderError(MpspMessage mpspMessage,RequestMsg requestMsg, ResponseMsg respMap){
		String veryCode = ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.VERIFYCODE));
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		String porderId= ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.PORDERID));
		String servtype=ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.SERVTYPE));
		String goodsId=ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.GOODSID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String expiretime=ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.EXPIRETIME));
		String bankid=ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.BANKID));
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String goodsname = ObjectUtil.trim(respMap.getStr(HFBusiDict.GOODSNAME));
		
		Map <String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.GOODSNAME, goodsname);
		map.put("orderId", orderId);
		map.put("expiretime", expiretime);
		map.put("bankid", bankid);
		map.put("retCode", retCode);
		map.put("retMsg", mpspMessage.getStr(HFBusiDict.RETMSG));
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();
		long expire=Timestamp.valueOf(expiretime).getTime();
		if(expire>=now){//订单未过期
		    int orderState=Integer.parseInt(mpspMessage.getStr(HFBusiDict.ORDERSTATE));
			    if(orderState==0||orderState==3)
			    {
					//判断是否为测试系统
					boolean test = checkIsTest();
					String smsFrom = "";
					String smsPrex = messageService.getSystemParam("smsPrex");
					if (test){
						smsFrom += "4";
					}
				
					String merIds = messageService.getSystemParam("special.called.mer.list","");
					if(merIds.contains(merId)||merIds.contains("ALL")){
						porderId="1"+porderId;//新的短信子号为1+9+merid(4)+seq(5)+tableName(1)
					}else{
						if(servtype.equals("3")){
							porderId="8"+goodsId;
						}else{
							porderId="2"+porderId;
						}
					}
					String mo1Called = smsPrex+smsFrom+porderId;
					map.put("moCalled", mo1Called);
					map.put("moMsg", veryCode);
					map.put("retCode", "0000");
					respMap.setRetCode0000();
			    
			    }else if(orderState==2){
			    	map.put("retMsg","订单已支付成功");
			    }else if(orderState==1){
			    	map.put("retMsg","订单支付中，请等待支付结果短信");
			    }
		
		}else{
			map.put("retMsg","订单已过期");
		}
//		String jsonStr = JSONObject.fromObject(map).toString();
//		//加密
//		byte[] data=null;
//		try {
//			logInfo("返回客户端的数据:%s",jsonStr);
////			data = encryptor.encyptString(jsonStr);
//			data = jsonStr.getBytes("UTF-8");
//		} catch (Exception e) {
//			logInfo("加密返回信息出现异常:%s", e);
//		}
		StringBuffer sb = new StringBuffer();
		sb.append(goodsname).append("|");
		sb.append(orderId).append("|");
		sb.append(expiretime).append("|");
		sb.append(bankid).append("|");
		sb.append(ObjectUtil.trim(map.get("moCalled"))).append("|");
		sb.append(ObjectUtil.trim(map.get("moMsg"))).append("|");
		sb.append(ObjectUtil.trim(map.get("retCode"))).append("|");
		sb.append(ObjectUtil.trim(map.get("retMsg")));
		respMap.setDirectResMsg(sb.toString());;
	}

	
}
