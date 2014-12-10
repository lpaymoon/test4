package com.umpay.hfweb.service.impl;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.bs.mpsp.util.DateTimeUtil;
import com.bs.mpsp.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.command.AbstractOrderCmd;
import com.umpay.hfweb.action.command.ChannelPayCmd;
import com.umpay.hfweb.action.command.ChannelRevokeCmd;
import com.umpay.hfweb.action.command.DirectOrderCmd;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.UserCacheClient;
import com.umpay.hfweb.common.TradeConnPool;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.service.MessageService;
import com.umpay.hfweb.service.SmsService;
import com.umpay.hfweb.service.TradeService;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;

public class TradeServiceImpl implements TradeService{
	
	private static Logger log = Logger.getLogger(TradeServiceImpl.class);
	private MessageService messageService;
	private SmsService smsService;
	private TradeConnPool tradeConnPool;
	
	public MpspMessage saveOrder(String bankId, MpspMessage message, AbstractOrderCmd cmd) {
		long beginTime = System.currentTimeMillis();
		MpspMessage responseMsgGlobal = new MpspMessage();
		//调用业务层保存订单服务
		//1-请求参数组装 START
		Map<String, String> req = new HashMap<String, String>();
		req.put(HFBusiDict.RPID, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		long dt = DateTimeUtil.currentDateTime();
		req.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		req.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		
		req.put(HFBusiDict.ORDERID, cmd.getOrderId());
		req.put(HFBusiDict.ORDERDATE, cmd.getMerDate());
		req.put(HFBusiDict.MERID, cmd.getMerId());
		String goodsId = cmd.getGoodsId();
		if(ObjectUtil.isNotEmpty(goodsId)){
			req.put(HFBusiDict.GOODSID, goodsId);
		}
		//定价模式 0定价,1非定价
		String priceMode = String.valueOf(message.get(HFBusiDict.PRICEMODE));
		//过期时间
		Long expireTime = (Long)message.get(HFBusiDict.EXPIRETIME);
		if(ObjectUtil.isNotEmpty(priceMode)){
			req.put(HFBusiDict.PRICEMODE, priceMode);
		}
		//商品服务类型 2为按次;3为包月
		String servType = String.valueOf(message.get(HFBusiDict.SERVTYPE));
		//服务月份
		String servMonth = String.valueOf(message.get(HFBusiDict.SERVMONTH));
		req.put(HFBusiDict.SERVTYPE, servType);
		req.put(HFBusiDict.SERVMONTH, servMonth);
		req.put(HFBusiDict.GOODSNAME, String.valueOf(message.get(HFBusiDict.GOODSNAME)));
		req.put(HFBusiDict.MOBILEID, cmd.getMobileId());
		//处理订单有效期 订单有效时间+当前时间 （Timestamp）
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();
		long future = now + expireTime * 1000;
		req.put(HFBusiDict.EXPIRETIME, new Timestamp(future).toString());
		req.put(HFBusiDict.BANKID, bankId);
		req.put(HFBusiDict.TRANSTYPE, "0");
		req.put(HFBusiDict.ACCESSTYPE, "W");
		req.put(HFBusiDict.AMOUNT, cmd.getAmount());
		String notifyUrl = cmd.getNotifyUrl();
		if(ObjectUtil.isNotEmpty(notifyUrl)){
			req.put(HFBusiDict.NOTIFYURL, notifyUrl);
		}
		String merPriv = cmd.getMerPriv();
		if(ObjectUtil.isNotEmpty(merPriv)){
			req.put(HFBusiDict.MERPRIV, merPriv);
		}
		String expand = cmd.getExpand();
		if(ObjectUtil.isNotEmpty(expand)){
			req.put(HFBusiDict.EXPAND, expand);
		}
		req.put(HFBusiDict.VERSION, cmd.getVersion());
		//1-请求参数组装 END
		//2-下单前校验 START
		//2-1 金额校验
		int amt = Integer.parseInt(req.get(HFBusiDict.AMOUNT));
		int amtLimit = Integer.parseInt(messageService.getSystemParam("AmtLimit"));
		if(amt < amtLimit){
			logInfo("OrderSave PreCheck Failed[RetCode]1157:WEB下单金额太小");
			responseMsgGlobal.setRetCode("1157");
			return responseMsgGlobal;
		}
		logInfo("OrderSave PreCheck Success[RetCode]0000:订单金额限制判断通过");
		//2-2 下单频率限制
		//先查白名单表，如果白名单表中存在该手机号，则不显示下单频率
		String busirolltype = String.valueOf(message.get(HFBusiDict.BUSIROLLTYPE));
		boolean isLimit=true;
		String userOrderVRule = "";
		if("11".equals(busirolltype)){
			isLimit=false;
			logInfo("RateCheck is not needed[mobileid]:%s:%s",req.get(HFBusiDict.MOBILEID),"该用户是白名单用户");
		}else{
		//add by zhuoyangyang 20140326 多维度控制下单频率
		String userOrderVRuleKey = StringUtil.trim(messageService.getSystemParam("userOrderVRuleKeys"));
		String[] userOrderVRuleKeys = userOrderVRuleKey.split(";");
		String merId = StringUtil.trim((String)cmd.getMerId());
		String provCode = StringUtil.trim((String)message.get(HFBusiDict.PROVCODE));
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.PROVCODE, provCode);
		map.put(HFBusiDict.MERID, merId);
		map.put(HFBusiDict.GOODSID, goodsId);
		try {
			//根据省份、商户、商品获取最优匹配规则对应的限制条件
			userOrderVRule = findCondition(userOrderVRuleKeys, "userOrderVRule", map);
			log.info("匹配后获得的限制规则为 userOrderVRule["+userOrderVRule+"]");
		} catch (Exception e) {
			log.error("匹配下单频率控制规则时异常e：", e);
		}
		if("-1".equals(userOrderVRule)){//配置为-1的不做下单频率控制
			isLimit = false;
		}else if("".equals(StringUtil.trim(userOrderVRule))){
				userOrderVRule = StringUtil.trim(messageService.getSystemParam("UserOrderV"));
				logInfo("GetUserOrderVBy[merid][provcode][goodsid]AndUserOrderVIs[_userOrderV] :%s:%s:%s",merId,provCode,userOrderVRule);
			  }
		}
		if(isLimit==true){
			UserCacheClient userCache = (UserCacheClient)AbstractCacheFactory.getInstance().getUserCacheClient();
			String retCode = userCache.checkUserOrdersLtd(req.get(HFBusiDict.MOBILEID), userOrderVRule);
			if(!DataDict.SUCCESS_RET_CODE.equals(retCode)){
				if(retCode.equals("1170")){
					smsService.sendUserOrderLtdSms(req.get(HFBusiDict.MOBILEID));
					logInfo("Send Alarm Sms To User");
				}
				responseMsgGlobal.setRetCode(retCode);
				logInfo("RateCheck Failed[RetCode]:%s:%s",retCode,"下单频率控制未通过");
				return responseMsgGlobal;
			}
		}
		logInfo("RateCheck Success[RetCode]:0000:下单频率控制已通过");
		
		//2-下单前校验 END
		//3-下单业务执行 START
		String requstURL = new StringBuffer(getTradeSrvPath()).append("/ORDSV").toString();

		long time2 = System.currentTimeMillis();
		
		logInfo("before order useTime:"+(time2-beginTime));
		req.put(HFBusiDict.BUSINESSTYPE,cmd.getBusinessType());//业务区分字段  panxingwu add
		req.put(HFBusiDict.PROVCODE, StringUtil.trim((String)message.get(HFBusiDict.PROVCODE)));
		req.put(HFBusiDict.AREACODE, StringUtil.trim((String)message.get(HFBusiDict.AREACODE)));// 业务层下单前限控取配置所用  zhuoyangyang add
		MpspMessage respMessage = tradeConnPool.doPost(requstURL, req);
		long time3 = System.currentTimeMillis();
		logInfo("order useTime:"+(time3-time2));
		//1304 下单失败
		exchangeResult(responseMsgGlobal,respMessage,"1304");
		logInfo("OrderSave Result[RetCode]:%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		//3-下单业务执行 END
		long time4 = System.currentTimeMillis();
		logInfo("after order useTime:"+(time4-time3));
		return responseMsgGlobal;
	}
	/**
	 * 异步下单接口，江西web后台下单接口
	 * 注意：web页面下单如果也需要接入江西小额的话，合并该接口到saveOrder方法
	 * @param bankId
	 * @param message
	 * @param cmd
	 * @return
	 */
	public MpspMessage asynOrder(String bankId, MpspMessage message, AbstractOrderCmd cmd){
		long beginTime = System.currentTimeMillis();
		MpspMessage responseMsgGlobal = new MpspMessage();
		//调用业务层保存订单服务
		//1-请求参数组装 START
		Map<String, String> req = new HashMap<String, String>();
		req.put(HFBusiDict.RPID, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		long dt = DateTimeUtil.currentDateTime();
		req.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		req.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		
		req.put(HFBusiDict.ORDERID, cmd.getOrderId());
		req.put(HFBusiDict.ORDERDATE, cmd.getMerDate());
		req.put(HFBusiDict.MERID, cmd.getMerId());
		String goodsId = cmd.getGoodsId();
		if(ObjectUtil.isNotEmpty(goodsId)){
			req.put(HFBusiDict.GOODSID, goodsId);
		}
		//定价模式 0定价,1非定价
		String priceMode = String.valueOf(message.get(HFBusiDict.PRICEMODE));
		//过期时间
		Long expireTime = (Long)message.get(HFBusiDict.EXPIRETIME);
		if(ObjectUtil.isNotEmpty(priceMode)){
			req.put(HFBusiDict.PRICEMODE, priceMode);
		}
		//商品服务类型 2为按次;3为包月
		String servType = String.valueOf(message.get(HFBusiDict.SERVTYPE));
		//服务月份
		String servMonth = String.valueOf(message.get(HFBusiDict.SERVMONTH));
		req.put(HFBusiDict.SERVTYPE, servType);
		req.put(HFBusiDict.SERVMONTH, servMonth);
		req.put(HFBusiDict.GOODSNAME, String.valueOf(message.get(HFBusiDict.GOODSNAME)));
		req.put(HFBusiDict.MOBILEID, cmd.getMobileId());
		//处理订单有效期 订单有效时间+当前时间 （Timestamp）
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();
		long future = now + expireTime * 1000;
		req.put(HFBusiDict.EXPIRETIME, new Timestamp(future).toString());
		req.put(HFBusiDict.BANKID, bankId);
		req.put(HFBusiDict.TRANSTYPE, "0");
		req.put(HFBusiDict.ACCESSTYPE, "W");
		req.put(HFBusiDict.AMOUNT, cmd.getAmount());
		String notifyUrl = cmd.getNotifyUrl();
		if(ObjectUtil.isNotEmpty(notifyUrl)){
			req.put(HFBusiDict.NOTIFYURL, notifyUrl);
		}
		String merPriv = cmd.getMerPriv();
		if(ObjectUtil.isNotEmpty(merPriv)){
			req.put(HFBusiDict.MERPRIV, merPriv);
		}
		String expand = cmd.getExpand();
		if(ObjectUtil.isNotEmpty(expand)){
			req.put(HFBusiDict.EXPAND, expand);
		}
		req.put(HFBusiDict.VERSION, cmd.getVersion());
		//1-请求参数组装 END
		//2-下单前校验 START
		//2-1 金额校验
		int amt = Integer.parseInt(req.get(HFBusiDict.AMOUNT));
		int amtLimit = Integer.parseInt(messageService.getSystemParam("AmtLimit"));
		if(amt < amtLimit){
			logInfo("OrderSave PreCheck Failed[RetCode]1157:WEB下单金额太小");
			responseMsgGlobal.setRetCode("1157");
			return responseMsgGlobal;
		}
		logInfo("OrderSave PreCheck Success[RetCode]0000:订单金额限制判断通过");
		//2-2 下单频率限制
		UserCacheClient userCache = (UserCacheClient)AbstractCacheFactory.getInstance().getUserCacheClient();
		String _userOrderV = messageService.getSystemParam("UserOrderV");
		String retCode = userCache.checkUserOrdersLtd(req.get(HFBusiDict.MOBILEID), _userOrderV);
		if(!DataDict.SUCCESS_RET_CODE.equals(retCode)){
			if(retCode.equals("1170")){
				smsService.sendUserOrderLtdSms(req.get(HFBusiDict.MOBILEID));
				logInfo("Send Alarm Sms To User");
			}
			responseMsgGlobal.setRetCode(retCode);
			logInfo("RateCheck Failed[RetCode]:%s:%s",retCode,"下单频率控制未通过");
			return responseMsgGlobal;
		}
		logInfo("RateCheck Success[RetCode]:0000:下单频率控制已通过");
		
		//2-下单前校验 END
		//3-下单业务执行 START
		String requstURL = new StringBuffer(getTradeSrvPath()).append("/HFJXZF").toString();//江西小额调用的是拓维公司异步支付接口

		long time2 = System.currentTimeMillis();
		
		logInfo("before order useTime:"+(time2-beginTime));
		req.put(HFBusiDict.BUSINESSTYPE, cmd.getBusinessType());
		MpspMessage respMessage = tradeConnPool.doPost(requstURL, req);
		long time3 = System.currentTimeMillis();
		logInfo("order useTime:"+(time3-time2));
		//1304 下单失败
		exchangeResult(responseMsgGlobal,respMessage,"1304");
		logInfo("OrderSave Result[RetCode]:%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		//3-下单业务执行 END
		long time4 = System.currentTimeMillis();
		logInfo("after order useTime:"+(time4-time3));
		return responseMsgGlobal;
	}
	
	public MpspMessage queryBalance(String merId, String mobileId) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		//1-查询余额
		String requstQueryURL = new StringBuffer(getTradeSrvPath()).append("/QUIFJ").toString();
		//logInfo("Balance Query RequstURL--rpid[" + getRpid() + "]:" + requstQueryURL);
		Map<String, String> para4Query = new HashMap<String, String>();
		para4Query.put(HFBusiDict.MOBILEID, mobileId);
		para4Query.put(HFBusiDict.MERID, merId);
		para4Query.put(HFBusiDict.RPID, getRpid());
		long dt = DateTimeUtil.currentDateTime();
		para4Query.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		para4Query.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		//logInfo("Balance Query RequstData--rpid[" + getRpid() + "]:" + HttpUtil.mapToRequestParameter(para4Query));
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstQueryURL.toString(), para4Query);
		//1166 查询用户余额失败
		exchangeResult(responseMsgGlobal,respQueryMsg,"1166");
		logInfo("Balance Query Result[RetCode]:%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}
	
	/** ********************************************
	 * method name   : queryBalanceLeft 
	 * description   : 
	 * @return       : MpspMessage
	 * @param        : @param merId
	 * @param        : @param mobileId
	 * @param        : @return
	 * modified      : LiuJiLong ,  2012-10-25  上午10:25:09
	 * @see          : 
	 * ********************************************/      
	public MpspMessage queryPayment(String merId, String mobileId){
		MpspMessage responseMsgGlobal = new MpspMessage();
		//1-查询余额
		String requstQueryURL = new StringBuffer(getTradeSrvPath()).append("/HFHBCX").toString();
		logInfo("Balance Query RequstURL--rpid[" + getRpid() + "]:" + requstQueryURL);
		Map<String, String> para4Query = new HashMap<String, String>();
		para4Query.put(HFBusiDict.MOBILEID, mobileId);
		para4Query.put(HFBusiDict.MERID, merId);
		para4Query.put(HFBusiDict.RPID, getRpid());
		long dt = DateTimeUtil.currentDateTime();
		para4Query.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		para4Query.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		para4Query.put(HFBusiDict.BANKID, "XE010000");
		//logInfo("Balance Query RequstData--rpid[" + getRpid() + "]:" + HttpUtil.mapToRequestParameter(para4Query));
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstQueryURL.toString(), para4Query);
		//1166 查询用户余额失败
		exchangeResult(responseMsgGlobal,respQueryMsg,"1166");
		logInfo("Payment Query Result[RetCode]:%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}
	
	public MpspMessage channelPay(ChannelPayCmd cmd){
		MpspMessage responseMsgGlobal = new MpspMessage();
		Map<String, String> para4Req = new HashMap<String, String>();
		String merlist = messageService.getSystemParam("shqjs.merlist");
		String merId = cmd.getMerId();
		String requstQueryURL = "";
		if(merlist.indexOf(merId)!=-1){//上海清洁算平台商户
			para4Req.put(HFBusiDict.BUSINESSTYPE, DataDict.BUSI_QJSZF);//清结算支付 (0304) 20131017
			requstQueryURL = new StringBuffer(getTradeSrvPath()).append("/SHQJSZF").toString();
		}else{
			para4Req.put(HFBusiDict.BUSINESSTYPE, DataDict.BUSI_QDZL);//渠道直接支付(0302) 20131017
			requstQueryURL = new StringBuffer(getTradeSrvPath()).append("/QDHFZF").toString();
		}
		    
				
		para4Req.put(HFBusiDict.RPID, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		long dt = DateTimeUtil.currentDateTime();
		para4Req.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		para4Req.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		
		para4Req.put(HFBusiDict.MOBILEID, cmd.getMobileId());
		para4Req.put(HFBusiDict.AMT, cmd.getAmount());
		para4Req.put(HFBusiDict.MERID, cmd.getMerId());
		para4Req.put(HFBusiDict.GOODSID, cmd.getGoodsId());
		para4Req.put(HFBusiDict.ORDERID, cmd.getOrderId());
		para4Req.put(HFBusiDict.MEMO, cmd.getMerId()); //目前无效、但必输字段
		para4Req.put(HFBusiDict.VALSIGN, cmd.getMerId()); //目前无效、但必输字段
		String expand = cmd.getExpand();
		if(ObjectUtil.isEmpty(expand)){
			expand = cmd.getMerId();//目前无效、但必输字段
		}
		para4Req.put(HFBusiDict.REQTRACE, expand);
		//商户日期没有传递给渠道支付，导致商户订单时间与平台所记录的订单时间，有可能不一致，导致冲正失败
		//因此要求接入流程中的商户日期与系统当前日期一致

		MpspMessage respQueryMsg = tradeConnPool.doPost(requstQueryURL.toString(), para4Req);
		//1312 渠道支付失败
		exchangeResult(responseMsgGlobal,respQueryMsg,"1312");
		logInfo("Channel pay Result[RetCode]:%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}
	
	/**
	 * ********************************************
	 * method name   : channelRevoke 
	 * description   : 直接冲正
	 * @param        : cmd merId
	 * @return       : 
	 * *******************************************
	 */
	public MpspMessage channelRevoke(ChannelRevokeCmd cmd){
		MpspMessage responseMsgGlobal = new MpspMessage();
		String merId = cmd.getMerId();
		String requstQueryURL = "";
		if("2537".equals(merId)){
			requstQueryURL = new StringBuffer(getTradeSrvPath()).append("/BJCPCZ").toString();
		}else{
			requstQueryURL = new StringBuffer(getTradeSrvPath()).append("/QDHFCZ").toString();
		}
		Map<String, String> para4Req = new HashMap<String, String>();		
		para4Req.put(HFBusiDict.RPID, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		long dt = DateTimeUtil.currentDateTime();
		para4Req.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		para4Req.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		para4Req.put(HFBusiDict.MOBILEID, cmd.getMobileId());
		para4Req.put(HFBusiDict.AMT, cmd.getAmount());
		para4Req.put(HFBusiDict.MERID, cmd.getMerId());
		para4Req.put(HFBusiDict.GOODSID, cmd.getGoodsId());
		para4Req.put(HFBusiDict.ORDERID, cmd.getOrderId());
		para4Req.put(HFBusiDict.PLATDATE, cmd.getMerDate());
		//商户日期没有传递给渠道支付，导致商户订单时间与平台所记录的订单时间，有可能不一致，导致冲正失败
		//因此要求接入流程中的商户日期与系统当前日期一致
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstQueryURL.toString(), para4Req);
		//1313 渠道冲正失败
		exchangeResult(responseMsgGlobal,respQueryMsg,"1313");
		logInfo("Channel revoke Result[RetCode]:%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}
	
	/**
	 * ********************************************
	 * method name   : exchangeResult 
	 * description   : 交换资源层处理结果
	 * @return       : void
	 * @param        : @param responseMsgGlobal
	 * @param        : @param msg    资源层调用的返回消息
	 * @param        : @param retCode web接入返回码
	 * modified      : yangwr ,  Nov 10, 2011  2:36:11 PM
	 * @see          : 
	 * *******************************************
	 */
	private void exchangeResult(MpspMessage responseMsgGlobal,MpspMessage msg,String retCode){
		if(!msg.isRetCode0000()){
			if(msg.isRetCodeSysError()){
				responseMsgGlobal.setRetCodeSysError();
				responseMsgGlobal.setRetCodeBussi(msg.getRetCodeBussi());
			}else{
				responseMsgGlobal.setRetCode(retCode);
				responseMsgGlobal.setRetCodeBussi(msg.getRetCodeBussi());
			}
		}else{
			responseMsgGlobal.putAll(msg);
			responseMsgGlobal.setRetCode0000();
		}
	}

	/**
	 * ********************************************
	 * method name   : getTradeSrvPath 
	 * description   : 获取业务层服务路径
	 * @return       : String
	 * modified      : yangwr ,  Nov 4, 2011  11:11:03 AM
	 * @see          : 
	 * *******************************************
	 */
	private String getTradeSrvPath(){
//		String srvURL = messageService.getSystemParam(DataDict.TRADE_SRV_URL);
//		if(srvURL.endsWith("/")){
//			srvURL = srvURL.substring(0, srvURL.length()-1); 
//		}
//		return srvURL;
		
		/*
		 * 20131031 liujilong 策略负载均衡修改,URL获取改在TradeConnPool中
		 */
		return "";
	}
	public void logInfo(String message,Object... args){
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String funCode = SessionThreadLocal.getSessionValue(DataDict.FUNCODE);
		log.info(String.format("%s,%s,%s",funCode,rpid,String.format(message,args)));
	}
	
	private String getRpid(){
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		return rpid;
	}

	public void setTradeConnPool(TradeConnPool tradeConnPool) {
		this.tradeConnPool = tradeConnPool;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public void setSmsService(SmsService smsService) {
		this.smsService = smsService;
	}
	public MpspMessage wxHfOrderSave(MpspMessage message) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		String orderdate = message.getStr(DataDict.MER_REQ_MERDATE);
		String orderid = message.getStr(DataDict.MER_REQ_ORDERID);
		String merid = message.getStr(DataDict.MER_REQ_MERID);
		String mobileid = message.getStr(DataDict.MER_REQ_MOBILEID);
		String goodsid = message.getStr(DataDict.MER_REQ_GOODSID);
		Map<String,String> reqMap = new HashMap<String,String>();
		long dt = DateTimeUtil.currentDateTime();
		reqMap.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		reqMap.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		reqMap.put(HFBusiDict.ORDERDATE, orderdate);
		reqMap.put(HFBusiDict.ORDERID, orderid);
		reqMap.put(HFBusiDict.MERID, merid);
		reqMap.put(HFBusiDict.GOODSID, goodsid);
		reqMap.put(HFBusiDict.MOBILEID, mobileid);
		reqMap.put(HFBusiDict.RPID, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		reqMap.put(HFBusiDict.BUSINESSTYPE, DataDict.BUSI_R4WKZF);
		
		String requstURL = new StringBuffer(getTradeSrvPath()).append("/R4HFORD").toString();
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstURL, reqMap);
		exchangeResult(responseMsgGlobal,respQueryMsg,"1304");
		return responseMsgGlobal;
	}
	
		
	public MpspMessage preAuthPay(MpspMessage message) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		String orderdate = message.getStr(DataDict.MER_REQ_MERDATE);
		String orderid = message.getStr(DataDict.MER_REQ_ORDERID);
		String merid = message.getStr(DataDict.MER_REQ_MERID);
		String mobileid = message.getStr(DataDict.MER_REQ_MOBILEID);
		String goodsid = message.getStr(DataDict.MER_REQ_GOODSID);
		String transtate = message.getStr(DataDict.MER_REQ_TRANSSTATE);
		if(!transtate.equalsIgnoreCase("0")) //0: 交易状态成功
		{
		    logInfo("transtate Exceptioon[RetCode]1319:商户发送交易状态异常 transtate "+transtate);
		    responseMsgGlobal.setRetCode("1319");
		    return responseMsgGlobal;
			
		}
		Map<String,String> reqMap = new HashMap<String,String>();
		long dt = DateTimeUtil.currentDateTime();
		reqMap.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		reqMap.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		reqMap.put(HFBusiDict.ORDERDATE, orderdate);
		reqMap.put(HFBusiDict.ORDERID, orderid);
		reqMap.put(HFBusiDict.MERID, merid);
		reqMap.put(HFBusiDict.GOODSID, goodsid);
		reqMap.put(HFBusiDict.MOBILEID, mobileid);
		reqMap.put(HFBusiDict.TRANSTATE, transtate);
		reqMap.put(HFBusiDict.VERSION, "3.0");
		reqMap.put(HFBusiDict.RPID, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		
		String requstURL = new StringBuffer(getTradeSrvPath()).append("/DCZF").toString();
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstURL, reqMap);
		exchangeResult(responseMsgGlobal,respQueryMsg,"1318");
		return responseMsgGlobal;
	}
	
	public MpspMessage wxUFNotifyService(MpspMessage message){
		String merId = message.getStr("mer_id");
		String goodsId = message.getStr("goods_id");
		String orderid = message.getStr("order_id");
		String amount = message.getStr("amount");
		String orderdate = message.getStr("mer_date");
		String merpriv = message.getStr("mer_priv");
		String payretcode = message.getStr("error_code");
		String bankcheckdate = message.getStr("settle_date");//清算日期
		
		String mobileid = "15000000000";//手机号
		String bankid = "XE010000";//银行号
		String servtype = "2";//2为按次；3为包月
		String version = "3.0";//版本号
		String chnlid = "0";//0: 第一次请求发货；1:重发；2:客服触发
		String transtype = "0";//0：新增1：续费（针对包月商品）

		Map<String,String> reqMap = new HashMap<String,String>();
		long dt = DateTimeUtil.currentDateTime();
		reqMap.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		reqMap.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		reqMap.put(HFBusiDict.MERID,merId);
		reqMap.put(HFBusiDict.GOODSID, goodsId);
		reqMap.put(HFBusiDict.ORDERID, orderid);
		reqMap.put(HFBusiDict.AMOUNT,amount);
		reqMap.put(HFBusiDict.BANKCHECKDATE, bankcheckdate);
		reqMap.put(HFBusiDict.ORDERDATE,orderdate);
		reqMap.put(HFBusiDict.MOBILEID, mobileid);
		reqMap.put(HFBusiDict.MERPRIV, merpriv);
		reqMap.put(HFBusiDict.PAYRETCODE, payretcode);
		reqMap.put(HFBusiDict.BANKID,bankid);
		reqMap.put(HFBusiDict.SERVTYPE,servtype);
		reqMap.put(HFBusiDict.VERSION, version);
		reqMap.put(HFBusiDict.CHNLID, chnlid);
		reqMap.put(HFBusiDict.TRANSTYPE,transtype);
		reqMap.put(HFBusiDict.RPID, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		logInfo("请求业务层商户结果通知数据:%s", reqMap);
		String requstURL = new StringBuffer(getTradeSrvPath()).append("/NOTIMER").toString();
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstURL, reqMap);
		return respQueryMsg;
	}
	
	/** ********************************************
	 * method name   : queryMobileInfo 
	 * description   : 商户查询手机号信息接口
	 * @return       : MpspMessage
	 * @param        : @param merId 商户号
	 * @param        : @param mobileId 手机号
	 * @param        : @return
	 * modified      : LiuJiLong ,  2012-12-12  上午10:25:09
	 * @see          : 
	 * ********************************************/        
	public MpspMessage queryMobileInfo(String merId, String mobileId) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		//1-查询余额
		String requstQueryURL = new StringBuffer(getTradeSrvPath()).append("/FKYHCX").toString();
		logInfo("Mobile Info Query RequstURL--rpid[" + getRpid() + "]:" + requstQueryURL);
		Map<String, String> para4Query = new HashMap<String, String>();
		para4Query.put(HFBusiDict.MOBILEID, mobileId);
		//para4Query.put(HFBusiDict.MERID, merId);
		para4Query.put(HFBusiDict.RPID, getRpid());
		long dt = DateTimeUtil.currentDateTime();
		para4Query.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		para4Query.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		//para4Query.put(HFBusiDict.BANKID, "XE010000");
		//logInfo("Balance Query RequstData--rpid[" + getRpid() + "]:" + HttpUtil.mapToRequestParameter(para4Query));
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstQueryURL.toString(), para4Query);
		exchangeResult(responseMsgGlobal,respQueryMsg,"1167");//查询信息失败
		logInfo("Mobile Info Query Result[RetCode]:%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}
	/**
	 * ********************************************
	 * method name   : specialMerPay 
	 * modified      : panxingwu ,  2013-1-22
	 * description	 : 福建12580买Q币
	 * *******************************************
	 */
	public MpspMessage specialMerPay(RequestMsg requestMsg) {
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		String merid =  requestMsg.getStr(DataDict.MER_REQ_MERID);
		String goodsid =  requestMsg.getStr(DataDict.MER_REQ_GOODSID);
		String chnlPriv =  requestMsg.getStr(DataDict.MER_REQ_CHNLPRIV);
		String version = requestMsg.getStr(DataDict.MER_REQ_VERSION);
		//获取发送给腾讯的信息模板
		String msgCon = messageService.getSystemParam("sporder.goodsdesc."+merid);
		msgCon = msgCon.replaceFirst("merid", merid);
		msgCon = msgCon.replaceFirst("goodsid", goodsid);
		msgCon = msgCon.replaceFirst("expand", chnlPriv);
		String called = "8"+merid; 
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		long dt = DateTimeUtil.currentDateTime();
		Map<String,String> reqMap = new HashMap<String,String>();
		reqMap.put(HFBusiDict.MOBILEID, mobileid);
		reqMap.put(HFBusiDict.MERID,merid);
		reqMap.put(HFBusiDict.GOODSID, goodsid);
		reqMap.put(HFBusiDict.RPID, rpid);
		reqMap.put(HFBusiDict.MSGCON, msgCon);
		reqMap.put(HFBusiDict.CALLED, called);
		reqMap.put(HFBusiDict.VERSION,version);
		reqMap.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		reqMap.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		String requstURL = new StringBuffer(getTradeSrvPath()).append("/TSQDXD").toString();
		logInfo("请求业务层URL:%s,请求参数:%s", requstURL,reqMap);
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstURL.toString(), reqMap);
		exchangeResult(respQueryMsg,respQueryMsg,"1304");//下单失败
		logInfo("Mobile Info Query Result[RetCode]:%s:%s",respQueryMsg.getRetCode(),respQueryMsg.getRetCodeBussi());
		return respQueryMsg;
	}
	public MpspMessage qdOrderCreate(RequestMsg requestMsg) {
		String chnlId = requestMsg.getStr(DataDict.MER_REQ_CHNLID);//渠道号
		String chnlDate = requestMsg.getStr(DataDict.MER_REQ_CHNLDATE);//渠道日期
		String chnlOrderid = requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID);//渠道订单号
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);//用户手机号
		String msgcon = requestMsg.getStr(DataDict.MER_REQ_GOODSINF);
		String merid = requestMsg.getStr(DataDict.MER_REQ_MERID);
		String goodsid = requestMsg.getStr(DataDict.MER_REQ_GOODSID);
		String version = requestMsg.getStr(DataDict.MER_REQ_VERSION);
		String amount = requestMsg.getStr(DataDict.MER_REQ_AMOUNT);
		String chnlPriv = requestMsg.getStr(DataDict.MER_REQ_CHNLPRIV);
		String expand = requestMsg.getStr(DataDict.MER_REQ_EXPAND);
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String businessType = requestMsg.getStr(HFBusiDict.BUSINESSTYPE);
		long dt = DateTimeUtil.currentDateTime();
		
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.RPID, rpid);
		map.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		map.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		map.put(HFBusiDict.CHANNELID, chnlId);
		map.put(HFBusiDict.CHANNELDATE, chnlDate);
		map.put(HFBusiDict.CHANNELORDERID,chnlOrderid);
		map.put(HFBusiDict.MOBILEID, mobileid);
		map.put(HFBusiDict.MSGCON, msgcon);
		map.put(HFBusiDict.MERID, merid);
		map.put(HFBusiDict.GOODSID, goodsid);
		map.put(HFBusiDict.VERSION, version);
		map.put(HFBusiDict.AMOUNT, amount);
		map.put(HFBusiDict.CHANNELPRIV, chnlPriv);
		map.put(HFBusiDict.CHANNELEXPAND, expand);
		map.put(HFBusiDict.BUSINESSTYPE,businessType);
		String requstURL = new StringBuffer(getTradeSrvPath()).append("/QDXD").toString();
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstURL.toString(), map);
		logInfo("业务层返回信息:%s", respQueryMsg.getWrappedMap());
//		exchangeResult(respQueryMsg,respQueryMsg,"1304");//下单失败
		return respQueryMsg;
	}
	/* 
	 *查询用户信息，包括余额，日可用额度等
	 */
	public MpspMessage wxQueryUserInf(RequestMsg requestMsg) {
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.MOBILEID, mobileid);
		map.put(HFBusiDict.RPID,rpid);
		String requstURL = new StringBuffer(getTradeSrvPath()).append("/HFYHCX").toString();
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstURL.toString(), map);
		logInfo("业务层返回信息:%s", respQueryMsg.getWrappedMap());
		return respQueryMsg;
	}
	
	public MpspMessage wapDirectPay(PageOrderCmd cmd,String mobileId,boolean isCommon) {
		String requstQueryURL = new StringBuffer(getTradeSrvPath()).append("/WAPZJZF").toString();
		Map<String, String> para4Req = new HashMap<String, String>();		
		para4Req.put(HFBusiDict.RPID, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		long dt = DateTimeUtil.currentDateTime();
		para4Req.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		para4Req.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		para4Req.put(HFBusiDict.ORDERDATE, cmd.getMerDate());
		para4Req.put(HFBusiDict.MOBILEID, mobileId);
		para4Req.put(HFBusiDict.AMOUNT, cmd.getAmount());
		para4Req.put(HFBusiDict.MERID, cmd.getMerId());
		para4Req.put(HFBusiDict.GOODSID, cmd.getGoodsId());
		para4Req.put(HFBusiDict.ORDERID, cmd.getOrderId());
		para4Req.put(HFBusiDict.MEMO, cmd.getMerId()); //目前无效、但必输字段
		para4Req.put(HFBusiDict.VALSIGN, cmd.getMerId()); //目前无效、但必输字段
		para4Req.put(HFBusiDict.BUSINESSTYPE, cmd.getBusinessType());
		if(isCommon)para4Req.put("isNeedAreaLimit", "1"); //通用验证码支付流程不做地区限制   20130718 panxingwu add
		String expand = cmd.getExpand();
		if(ObjectUtil.isEmpty(expand)){
			expand = cmd.getMerId();//目前无效、但必输字段
		}
		para4Req.put(HFBusiDict.REQTRACE, expand);
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstQueryURL.toString(), para4Req);
		logInfo("WapDirectPay Result[RetCode]:%s:%s",respQueryMsg.getRetCode(),respQueryMsg.get(HFBusiDict.RETMSG));
		return respQueryMsg;
	}
	/**
	 * 12580 SDK客户端支付
	 */
	public MpspMessage SDKPay(RequestMsg requestMsg) {
		// TODO Auto-generated method stub
		String code = StringUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CODE));
		String mobileId = StringUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MOBILEID));
		if("".equals(mobileId)){
			mobileId = StringUtil.trim(requestMsg.getStr(HFBusiDict.MOBILEID));
		}
		String requstPayURL = new StringBuffer(getTradeSrvPath()).append("/HFDXZF").toString();
		Map<String, String> para4Req = new HashMap<String, String>();		
		para4Req.put(HFBusiDict.RPID, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		long dt = DateTimeUtil.currentDateTime();
		para4Req.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		para4Req.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		para4Req.put(HFBusiDict.MOBILEID, mobileId);
		para4Req.put(HFBusiDict.PORDERID, requestMsg.getStr(DataDict.MER_REQ_PORDERID));
		para4Req.put(HFBusiDict.MSGCON, "8");
		if(!"".equals(code)){
			para4Req.put(HFBusiDict.MSGCON, code);
		}
		MpspMessage respPayMsg = tradeConnPool.doPost(requstPayURL.toString(), para4Req);
		logInfo("respPayMsg Result[RetCode]:%s:%s",respPayMsg.getRetCode(),respPayMsg.get(HFBusiDict.RETMSG));
		return respPayMsg;
	}
	/**
	 * 海南基地彩票向业务层发起用户包月订购请求
	 */
	public MpspMessage sendMonthlyServiceOrder(DirectOrderCmd cmd) {
		String requstOrderURL = new StringBuffer(getTradeSrvPath()).append("/HFHNCPDG").toString();
		Map<String, String> para4Req = new HashMap<String, String>();		
		para4Req.put(HFBusiDict.RPID, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		long dt = DateTimeUtil.currentDateTime();
		String reqtime = DateTimeUtil.getDateString(dt);
		String reqdate = DateTimeUtil.getTimeString(dt);
		para4Req.put(HFBusiDict.REQDATE, reqtime);
		para4Req.put(HFBusiDict.REQTIME, reqdate);
		para4Req.put(HFBusiDict.MOBILEID, cmd.getMobileId());
		para4Req.put(HFBusiDict.MERID, cmd.getMerId());
		MpspMessage respOrderMsg = tradeConnPool.doPost(requstOrderURL.toString(), para4Req);
		logInfo("sendMonthlyServiceOrder Result[RetCode]:%s:%s",respOrderMsg.getRetCode(),respOrderMsg.get(HFBusiDict.RETMSG));
		return respOrderMsg;
	}
	/**
	 * *****************  方法说明  *****************
	 * method name   :  findCondition
	 * @param  :  @param keys
	 * @param  :  @param funcode
	 * @param  :  @param msg
	 * @param  :  @return
	 * @param  :  @throws Exception
	 * @return  :  String
	 * @author       :  zhaoYan 2013-9-23 下午12:18:38 
	 * @Description  :  根据匹配条件，获取配置信息(有优先级关系)
	 * @see          : 
	 * @throws       :
	 * **********************************************
	 */
	private String findCondition(String[] keys, String funcode,
			Map<String, String> msg) throws Exception {
		//首先判断配置模板获取标签，如果为空，则退出
		int limitLen = keys.length;
		if (limitLen == 1 && keys[0].trim().length() == 0) {
			log.debug("功能码:" + funcode
					+ " 没有配置模板获取标签，请检查配置文件!");
			return null;
		}
		StringBuilder limitStr = new StringBuilder();
		for (int i = 0; i < limitLen; i++) {
			//确定参数个数，用二进制位数表示
			limitStr.append("1");
		}
		//将获取的标签长度，由2进制改为10进制
		int limit = Integer.parseInt(limitStr.toString(), 2);
		log.info("二进制limitStr:" + limitStr + " 转为十进制 limit:" + limit);
		StringBuilder baseCaseStr = new StringBuilder();
		String[] values = new String[limitLen];
		StringBuilder tags = new StringBuilder();
		for (int i = 0; i < limitLen; i++) {
			tags.append(keys[i]).append(".");
			String value = msg.get(keys[i]);
			values[i] = value;
			if (value == null) {
				baseCaseStr.append("0");
				log.debug("功能码:" + funcode + " 模板查找key [无]:" + keys[i]
						+ "-->" + value);
			} else {
				baseCaseStr.append("1");
				log.debug("功能码:" + funcode + " 模板查找key [有]:" + keys[i]
						+ "-->" + value);
			}
		}

		int baseCase = Integer.parseInt(baseCaseStr.toString(), 2);
		Set<Integer> used = new HashSet<Integer>();
		// 遍历所有情况
		for (int i = limit; i >= 0; i--) {
			int x = i & baseCase;
			log.debug("第:" + i + " 次查找！ 相与后的键图:" + x + " baseCase:"
			 +
			 baseCase);
			if (used.contains(x)) {
				continue;
			}
			used.add(x);
			StringBuilder condition = new StringBuilder();
			for (int n = limitLen - 1; n >= 0; n--) {
				if ((x & 1) == 1) {
					condition.insert(0, values[n] + ".");
				} else {
					condition.insert(0, "*.");
				}
				x >>= 1;
			}
			if (condition.length() > 0){
				condition.deleteCharAt(condition.length() - 1);
			}
			String catchedStr =  StringUtil.trim(messageService.getSystemParam(funcode+"."+condition.toString()));
			if ("".equals(catchedStr)) {
				log.debug(">>功能码:" + funcode + " 条件:" + condition
				 + " 没有找到模板配置，继续下一个");
				continue;
			}
			log.info("查找的键值:" + condition + " 模板:" + catchedStr);
			return catchedStr;
		}
		return null;
	}
	
	public MpspMessage qdVerifyPay(RequestMsg requestMsg,MpspMessage message) {
		// TODO Auto-generated method stub
		String chnlId = requestMsg.getStr(DataDict.MER_REQ_CHNLID);//渠道号
		String chnlDate = requestMsg.getStr(DataDict.MER_REQ_CHNLDATE);//渠道日期
		String chnlOrderid = requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID);//渠道订单号
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);//用户手机号
		String merid = requestMsg.getStr(DataDict.MER_REQ_MERID);
		String goodsid = requestMsg.getStr(DataDict.MER_REQ_GOODSID);
		String orderdate = requestMsg.getStr(HFBusiDict.ORDERDATE);
		String orderid = requestMsg.getStr(DataDict.MER_REQ_ORDERID);
		String amount = requestMsg.getStr(DataDict.MER_REQ_AMOUNT);
		String chnlPriv = requestMsg.getStr(DataDict.MER_REQ_CHNLPRIV);
		String expand = requestMsg.getStr(DataDict.MER_REQ_EXPAND);
		String porderid=ObjectUtil.trim(message.getStr(HFBusiDict.PORDERID));
		String verifyCode=ObjectUtil.trim(message.getStr(HFBusiDict.VERIFYCODE));//订单验证码
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String businessType = requestMsg.getStr(HFBusiDict.BUSINESSTYPE);
         long dt = DateTimeUtil.currentDateTime();
		
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.RPID, rpid);
		map.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		map.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		map.put(HFBusiDict.CHANNELID, chnlId);
		map.put(HFBusiDict.CHANNELDATE, chnlDate);
		map.put(HFBusiDict.CHANNELORDERID,chnlOrderid);
		map.put(HFBusiDict.MOBILEID, mobileid);
		map.put(HFBusiDict.MSGCON, verifyCode);
		map.put(HFBusiDict.MERID, merid);
		map.put(HFBusiDict.GOODSID, goodsid);
		map.put(HFBusiDict.PORDERID, porderid);
		map.put(HFBusiDict.AMOUNT, amount);
		map.put(HFBusiDict.CHANNELPRIV, chnlPriv);
		map.put(HFBusiDict.CHANNELEXPAND, expand);
		map.put(HFBusiDict.ORDERDATE, orderdate);
		map.put(HFBusiDict.ORDERID, orderid);
		map.put(HFBusiDict.BUSINESSTYPE,businessType);
//		String requstURL = new StringBuffer(getTradeSrvPath()).append("/QDYZMZF").toString();
		String requstURL = new StringBuffer(getTradeSrvPath()).append("/QDZF").toString();
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstURL.toString(), map);
		logInfo("业务层返回信息:%s", respQueryMsg.getWrappedMap());
//		exchangeResult(respQueryMsg,respQueryMsg,"1304");//下单失败
		return respQueryMsg;
	}
	public MpspMessage qdVerifyOrder(RequestMsg requestMsg) {
		// TODO Auto-generated method stub
		String chnlId = requestMsg.getStr(DataDict.MER_REQ_CHNLID);//渠道号
		String chnlDate = requestMsg.getStr(DataDict.MER_REQ_CHNLDATE);//渠道日期
		String chnlOrderid = requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID);//渠道订单号
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);//用户手机号
		String msgcon = requestMsg.getStr(DataDict.MER_REQ_GOODSINF);
		String merid = requestMsg.getStr(DataDict.MER_REQ_MERID);
		String goodsid = requestMsg.getStr(DataDict.MER_REQ_GOODSID);
		String version = requestMsg.getStr(DataDict.MER_REQ_VERSION);
		String amount = requestMsg.getStr(DataDict.MER_REQ_AMOUNT);
		String chnlPriv = requestMsg.getStr(DataDict.MER_REQ_CHNLPRIV);
		String expand = requestMsg.getStr(DataDict.MER_REQ_EXPAND);
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String businessType = requestMsg.getStr(HFBusiDict.BUSINESSTYPE);
		long dt = DateTimeUtil.currentDateTime();
		
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.RPID, rpid);
		map.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		map.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		map.put(HFBusiDict.CHANNELID, chnlId);
		map.put(HFBusiDict.CHANNELDATE, chnlDate);
		map.put(HFBusiDict.CHANNELORDERID,chnlOrderid);
		map.put(HFBusiDict.MOBILEID, mobileid);
		map.put(HFBusiDict.MSGCON, msgcon);
		map.put(HFBusiDict.MERID, merid);
		map.put(HFBusiDict.GOODSID, goodsid);
		map.put(HFBusiDict.VERSION, version);
		map.put(HFBusiDict.AMOUNT, amount);
		map.put(HFBusiDict.CHANNELPRIV, chnlPriv);
		map.put(HFBusiDict.CHANNELEXPAND, expand);
		map.put(HFBusiDict.BUSINESSTYPE,businessType);
		String requstURL = new StringBuffer(getTradeSrvPath()).append("/QDYZMXD").toString();
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstURL.toString(), map);
		logInfo("业务层返回信息:%s", respQueryMsg.getWrappedMap());
//		exchangeResult(respQueryMsg,respQueryMsg,"1304");//下单失败
		return respQueryMsg;
	}
	public MpspMessage sdkQuickPay(RequestMsg requestMsg) {
		String requstQueryURL = new StringBuffer(getTradeSrvPath()).append("/SDKKJZF").toString();
		Map<String, String> para4Req = new HashMap<String, String>();		
		para4Req.put(HFBusiDict.RPID, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		long dt = DateTimeUtil.currentDateTime();
		para4Req.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		para4Req.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		para4Req.put(HFBusiDict.MERID, ObjectUtil.trim(requestMsg.get("merId")));
		para4Req.put(HFBusiDict.GOODSID, ObjectUtil.trim(requestMsg.get("goodsId")));
		para4Req.put(HFBusiDict.MOBILEID, ObjectUtil.trim(requestMsg.get("payPhoneNum")));
		para4Req.put(HFBusiDict.ORDERID, ObjectUtil.trim(requestMsg.get("orderId")));
		para4Req.put(HFBusiDict.ORDERDATE, ObjectUtil.trim(requestMsg.get("merDate")));
		para4Req.put(HFBusiDict.AMOUNT, ObjectUtil.trim(requestMsg.get("amount")));
		para4Req.put(HFBusiDict.MERPRIV, ObjectUtil.trim(requestMsg.get("merPriv")));
		para4Req.put(HFBusiDict.EXPAND, ObjectUtil.trim(requestMsg.get("expand")));
		para4Req.put(HFBusiDict.BUSINESSTYPE, StringUtil.trim(requestMsg.getStr(HFBusiDict.BUSINESSTYPE)));
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstQueryURL.toString(), para4Req);
		logInfo("SdkQuickPay Result[RetCode]:%s:%s",respQueryMsg.getRetCode(),respQueryMsg.get(HFBusiDict.RETMSG));
		return respQueryMsg;
	}

}
