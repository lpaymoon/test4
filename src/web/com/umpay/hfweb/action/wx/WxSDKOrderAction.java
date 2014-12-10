package com.umpay.hfweb.action.wx;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import com.umpay.api.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.CommonUtil;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.RSAUtils;
import com.umpay.hfweb.util.SessionThreadLocal;


/** ******************  类说明  *********************
 * class       :  WxSDKOrderAction
 * @author     :  xuwei
 * @version    :  1.0  
 * description :  12580 SDK客户端接入商户下单
 * @see        :                        
 * ************************************************/   
public class WxSDKOrderAction extends WxOrderBaseAction {
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
		logInfo("参数校验通过");
		String merId = requestMsg.get(DataDict.MER_REQ_MERID).toString();	
		String goodsId=requestMsg.get(DataDict.MER_REQ_GOODSID).toString();
		//1-2 校验访问权限
		if(!merAuthService.canAccess(getFunCode(), merId)){
			//商户未开通此项支付服务
			respMap.setRetCode("1128");
			logInfo("MerAuthCheck merId["+merId+"] Result Failed[RetCode]:1128:商户访问权限校验未通过");
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("商户访问权限校验成功");
	
		String IMEI = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.IMEI));//手机串号
		String IMSI = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.IMSI));//SIM 卡识别码
		long goodsPrice = Long.valueOf(requestMsg.getStr(DataDict.MER_REQ_AMOUNT));
		String merDate = requestMsg.getStr(DataDict.MER_REQ_MERDATE);
					
		// 1-4-下单时间校验
		if(!DateUtil.verifyOrderDate(merDate)){
			respMap.setRetCode("1310");
			logInfo("请求日期超出当前日期前后一天！ merDate=%s", merDate);
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("下单时间校验通过");
		//2-商户验签
		String merSignCheck = messageService.getSystemParam("merSignCheck");
		if("1".equals(merSignCheck)){
			logInfo("商户验签开始...");
			String merUnSign=getUnsignStr(requestMsg);
			String merSign=ObjectUtil.trim(requestMsg.getStr("merSign"));
			MpspMessage checkMerSignResp = restService.checkSign(merId, merUnSign, merSign);
			if(!checkMerSignResp.isRetCode0000()){
				respMap.setRetCode(checkMerSignResp.getRetCode());
				respMap.setRetCodeBussi(checkMerSignResp.getRetCodeBussi());
				responseEorr(requestMsg,respMap);
				logInfo("SignCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),"验证商户签名失败");
				return;
			}
			logInfo("验证商户签名成功");
		}
		
		// 1-4-1 验证客户端身份
		String sdkSign = ObjectUtil.trim(requestMsg.getStr("sdkSign"));//手机SIM 识别码
		String sdkSignStr = messageService.getSystemParam("sdkSign");
		logInfo("验证客户端身份...");
	    if(!sdkSign.equals(sdkSignStr)){
	        	logInfo("客户端身份验证不通过");
	        	respMap.setRetCode("1322");
				responseEorr(requestMsg,respMap);
				return;
	     }
	    logInfo("客户端身份验证通过");
	  //客户端签名验证
	    String sign = ObjectUtil.trim(requestMsg.getStr("sign"));//客户端签名
		try {
			String isCheck = messageService.getSystemParam("isSdkCheck");
			if("1".equals(isCheck)){//只有开通了验签功能才走验签步骤
		        String unSignStr = getSdksignStr(requestMsg);
		        logInfo("原串:%s,签名串:%s",unSignStr,sign);
		        if(!unSignStr.equals(sign)){
		        	logInfo("请求数据非法，验签失败");
		        	respMap.setRetCode("1321");
					responseEorr(requestMsg,respMap);
					return;
		        }
		        logInfo("代码签名验证通过");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logInfo("异常:%s",e.getMessage());
			respMap.setRetCode("1323");
			responseEorr(requestMsg,respMap);
			return;
		}
	    

		//1-5-交易鉴权
		MpspMessage checkMerGoodsResp = restService.queryMerGoodsInfo(requestMsg.get(DataDict.MER_REQ_MERID).toString(),requestMsg.get(DataDict.MER_REQ_GOODSID).toString());
		if(!checkMerGoodsResp.isRetCode0000()){
			logInfo("queryMerGoodsInfo Result Failed[RetCode]:%s:%s", checkMerGoodsResp.getRetCode(), "交易鉴权失败[获取商户商品信息失败]");
			respMap.setRetCode(checkMerGoodsResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("交易鉴权成功");
		
		//1-7商品金额校验
		String pricemode = checkMerGoodsResp.getStr(HFBusiDict.PRICEMODE);
		if("0".equals(pricemode)){
			logInfo("定价商品,校验金额开始");
			//如果价格模式是固定价格则获取商品银行信息
			String merid = checkMerGoodsResp.getStr(HFBusiDict.MERID);
			String goodsid = checkMerGoodsResp.getStr(HFBusiDict.GOODSID);
			MpspMessage goodsBankResp = restService.getGoodsBank(merid, goodsid);
			if(!goodsBankResp.isRetCode0000()){
				logInfo("queryMerGoodsInfo Result Failed[RetCode]:%s:%s", goodsBankResp.getRetCode(), "获取商品银行失败");
				respMap.setRetCode(goodsBankResp.getRetCode());
				responseEorr(requestMsg,respMap);
				return;
			}
			logInfo("获取商品银行成功");
			List<Map<String, Object>> rs = (List<Map<String, Object>>) goodsBankResp.get(HFBusiDict.GOODSBANKS);
			Map<String,Object> map = new HashMap<String, Object>();
			for (Iterator iterator = rs.iterator(); iterator.hasNext();) {
				Map<String, Object> payBankMap = (Map<String, Object>) iterator.next();
				String kstate = String.valueOf(payBankMap.get(HFBusiDict.KSTATE));
				//11只开通新增 13新增与续费全部开通
				if(kstate.equals("11") || kstate.equals("13")){
				  map.putAll(payBankMap);
				  break;
				}				
			}
			if(map.size()==0){
				logInfo(" 商品银行未开通[RetCode]:%s:%s", "1303", "商品银行未开通");
				respMap.setRetCode("1303");
				responseEorr(requestMsg,respMap);
				return;
			}
			
			long amount = Long.valueOf(requestMsg.getStr(DataDict.MER_REQ_AMOUNT));
			long price = Long.valueOf(map.get(HFBusiDict.AMOUNT).toString());//制定价格
			if(price!=amount){
				logInfo("定价商品，金额不符，制定价格为：%s,输入价格为:%s",price,amount);
				respMap.setRetCode("1404");
				responseEorr(requestMsg,respMap);
				return;
			}
			logInfo("金额校验通过");
			goodsPrice = price;
		}
		
		//1-8-保存无线订单表
		requestMsg.put(HFBusiDict.EXPIRETIME, checkMerGoodsResp.get(HFBusiDict.EXPIRETIME));
		requestMsg.put(HFBusiDict.VERSION, "3.0");
		String maxNum = ObjectUtil.trim(messageService.getSystemParam("SDKorder.maxNum")); //sdk 最大下单次数
		requestMsg.put(HFBusiDict.MAX,maxNum);
		MpspMessage createWxOrderResp = restService.createWxOrder(requestMsg);
		if(!createWxOrderResp.isRetCode0000()){
			logInfo("createWxOrder Result Failed[RetCode]:%s:%s", createWxOrderResp.getRetCode(), "保存无线接入商户订单失败");
			respMap.setRetCode(createWxOrderResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("WxOrderSave Result Success[RetCode]:0000:保存无线订单表成功");
		
		//1-9查询手机绑定关系
		MpspMessage queryWxBindResp = restService.queryWxSDKBind(IMEI,IMSI);
		if(!queryWxBindResp.isRetCode0000()&&!queryWxBindResp.getRetCode().equals("86001124")){
			logInfo("queryWxBindResp Result Failed[RetCode]:%s:%s", queryWxBindResp.getRetCode(), "查询绑定关系失败");
			respMap.setRetCode(queryWxBindResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("queryWxBindResp Result Success[RetCode]:0000:查询手机绑定关系表成功");
		requestMsg.put(HFBusiDict.BINDFLAG, "0"); //默认未绑定
		String mobileId=queryWxBindResp.getStr(HFBusiDict.MOBILEID);
		requestMsg.put(HFBusiDict.MOBILEID, mobileId);
		//1-10保存实际订单表
		if(queryWxBindResp.get(HFBusiDict.MOBILEID)!=null){ // 手机号已绑定						
			requestMsg.put(HFBusiDict.BINDFLAG, "1");
			//1-10-1-交易鉴权
			MpspMessage checkTradeResp = restService.checkTrade(mobileId, merId, goodsId);
			if(!checkTradeResp.isRetCode0000()){
				respMap.setRetCode(checkTradeResp.getRetCode());
				respMap.setRetCodeBussi(checkTradeResp.getRetCodeBussi());
				responseEorr(requestMsg,respMap);
				logInfo("TradeCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),"用户交易鉴权失败");
				return;
			}
			logInfo("TradeCheck Result Success[RetCode]0000:用户交易鉴权通过");
			//1-10-2-确认可支付银行
			String bankId = (String)checkTradeResp.get(HFBusiDict.BANKID);
			if(ObjectUtil.isEmpty(bankId)){
				//无可支付银行的返回码 <您暂时不能使用支付服务>
				logInfo("BankCheck Result Failed[RetCode]:%s:%s","1303","支付银行校验未通过");
				respMap.setRetCode("1303");
				responseEorr(requestMsg,respMap);
				return;
			}
			logInfo("BankCheck Result Success[RetCode]:0000:可支付银行确认通过");
			
			//1-10-3-通信账户订单保存
			Map<String,String> reqMap = new HashMap<String,String>();
			//过期时间
			Long expireTime = (Long)checkMerGoodsResp.get(HFBusiDict.EXPIRETIME);
			Calendar calendar = Calendar.getInstance();
			long now = calendar.getTimeInMillis();
			long future = now + expireTime*1000; 
			reqMap.put(HFBusiDict.ORDERID,requestMsg.getStr(DataDict.MER_REQ_ORDERID));   
			reqMap.put(HFBusiDict.ORDERDATE,requestMsg.getStr(DataDict.MER_REQ_MERDATE));   
			reqMap.put(HFBusiDict.AMOUNT,requestMsg.getStr(DataDict.MER_REQ_AMOUNT)); 
			reqMap.put(HFBusiDict.MERID,requestMsg.getStr(DataDict.MER_REQ_MERID));  
			reqMap.put(HFBusiDict.GOODSID,requestMsg.getStr(DataDict.MER_REQ_GOODSID));
			reqMap.put(HFBusiDict.RPID,SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
			reqMap.put(HFBusiDict.ORIGAMOUNT,requestMsg.getStr(DataDict.MER_REQ_AMOUNT));
			reqMap.put(HFBusiDict.BUSINESSTYPE, "0210");//12580 SDK支付流程
			reqMap.put(HFBusiDict.MOBILEID, queryWxBindResp.getStr(HFBusiDict.MOBILEID));
			reqMap.put(HFBusiDict.PORDERID,createWxOrderResp.getStr(HFBusiDict.PORDERID));
			reqMap.put(HFBusiDict.EXPIRETIME,new Timestamp(future).toString());
			reqMap.put(HFBusiDict.BANKID,bankId);
			reqMap.put(HFBusiDict.MERPRIV,requestMsg.getStr(DataDict.MER_REQ_MERPRIV));
			reqMap.put(HFBusiDict.EXPAND, requestMsg.getStr(DataDict.MER_REQ_EXPAND));
			reqMap.put(HFBusiDict.ACCESSTYPE,"W");
			reqMap.put(HFBusiDict.VERIFYCODE,"8");
			reqMap.put(HFBusiDict.AMTTYPE,"02");// 01：人民币 02：移动话费 03：移动积分
			reqMap.put(HFBusiDict.VERSION, "3.0");
			reqMap.put(HFBusiDict.SIGN, requestMsg.getStr(DataDict.MER_REQ_SIGN));
			MpspMessage orderSaveResp = restService.createOrder(reqMap);
			if(!orderSaveResp.isRetCode0000()){
				logInfo("createOrder Result Failed[RetCode]:%s:%s", orderSaveResp.getRetCode(), "保存订单表失败");
				respMap.setRetCode(orderSaveResp.getRetCode());
				responseEorr(requestMsg,respMap);
				return;
			}
			logInfo("createOrder Result Success[RetCode]:0000:保存订单表成功");
		}
	
		requestMsg.put(HFBusiDict.PORDERID,createWxOrderResp.get(HFBusiDict.PORDERID));
		responseSuccess(checkMerGoodsResp,requestMsg,respMap,goodsPrice);
		
	}
	//生成商户签名明文串
	private String getUnsignStr(RequestMsg requestMsg) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(DataDict.MER_REQ_MERID).append("=").append(requestMsg.getStr(DataDict.MER_REQ_MERID)).append("&");
	    buffer.append(DataDict.MER_REQ_GOODSID).append("=").append(requestMsg.getStr(DataDict.MER_REQ_GOODSID)).append("&");
	    buffer.append(DataDict.MER_REQ_ORDERID).append("=").append(requestMsg.getStr(DataDict.MER_REQ_ORDERID)).append("&");
	    buffer.append(DataDict.MER_REQ_MERDATE).append("=").append(requestMsg.getStr(DataDict.MER_REQ_MERDATE)).append("&");	
		buffer.append(DataDict.MER_REQ_AMOUNT).append("=").append(requestMsg.getStr(DataDict.MER_REQ_AMOUNT));
		return buffer.toString();
	}
	
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_SDKXD;
	}
	private void responseSuccess(MpspMessage mpspMessage,RequestMsg requestMsg, ResponseMsg respMap,long price){
		respMap.setRetCode0000();
        String merId=ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
        String merDate=ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERDATE));
        String porderId=ObjectUtil.trim(requestMsg.getStr(HFBusiDict.PORDERID));
		String amount = String.valueOf(price);
		String merName = ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.MERNAME));
		String goodsName = ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.GOODSNAME));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
//		String cusphone = ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.CUSPHONE));//客服电话
//		String servmonth = ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.SERVMONTH));//服务月份
//		String servtype = ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.SERVTYPE));//服务类型
		String bindFlag = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.BINDFLAG));//服务类型
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String mobileId = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.MOBILEID));
		
		
//		if(servmonth==null||"".equals(servmonth)) servmonth="0";
		
		Map <String,String> map = new HashMap<String,String>();
		map.put(DataDict.MER_REQ_AMOUNT,amount);
		map.put(DataDict.MER_REQ_MERNAME,merName);
		map.put(DataDict.MER_REQ_GOODSNAME, goodsName);
		map.put(DataDict.MER_REQ_MERID,merId);
		map.put(DataDict.MER_REQ_GOODSID,goodsId);
		map.put(DataDict.MER_REQ_ORDERID,orderId);
		map.put(DataDict.MER_REQ_PORDERID,porderId);
		map.put(DataDict.MER_REQ_MERDATE, merDate);
		map.put(HFBusiDict.BINDFLAG, bindFlag);
		map.put(DataDict.MER_REQ_MOBILEID, mobileId);
//		map.put("cusPhone", cusphone);
//		map.put("servMonth", servmonth);
//		map.put("servType", servtype);
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg", "success");
		
		//判断是否为测试系统
		boolean test = checkIsTest();
		String smsFrom = "";
		String smsPrex = messageService.getSystemParam("smsPrex");
		if (test){
			smsFrom += "4";
		}
		String mo1Called = smsPrex+smsFrom+"1017"+mpspMessage.getStr(HFBusiDict.MERID);
		map.put("mo1Called", mo1Called);
		map.put("mo1Msg", goodsId+"#"+merDate+"#"+orderId+"#"+porderId);
        
		
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据:%s",jsonStr);
			data = jsonStr.getBytes("UTF-8");
		//	data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
	}

	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String merDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERDATE));
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg =  getRetMessage(respMap);
		
		Map <String,String> map = new HashMap<String,String>();
		map.put(DataDict.MER_REQ_MERID, merId);
		map.put(DataDict.MER_REQ_GOODSID,goodsId);
		map.put(DataDict.MER_REQ_ORDERID, orderId);
		map.put(DataDict.MER_REQ_MERDATE,merDate);
		map.put("retCode", retCode);
		map.put("retMsg", retMsg);

		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据为:%s", jsonStr);
			data = jsonStr.getBytes("UTF-8");
		//	data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
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
    
    private boolean orderLimit(){
    	
    	return false;
    }
    
    @Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request,false);//非加密数据
	}
    
    
	private String getSdksignStr(RequestMsg requestMsg){
		StringBuffer unSignStr = new StringBuffer();
		String IMEI = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.IMEI));
		String IMSI = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.IMSI));

		if(!IMEI.equals("")){
			unSignStr.append(IMEI+"&");
		}
		if(!IMSI.equals("")){
			unSignStr.append(IMSI+"&");
		}	
		unSignStr.append("rgz");
		

		logInfo("组装原串为:%s",unSignStr.toString());
		return RSAUtils.getMD5String(unSignStr.toString());
	}
	
}
