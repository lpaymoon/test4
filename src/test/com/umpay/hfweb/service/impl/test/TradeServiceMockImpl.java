package com.umpay.hfweb.service.impl.test;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bs.mpsp.util.DateTimeUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.command.AbstractOrderCmd;
import com.umpay.hfweb.action.command.ChannelPayCmd;
import com.umpay.hfweb.action.command.ChannelRevokeCmd;
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

public class TradeServiceMockImpl implements TradeService{
	
	private static Logger log = Logger.getLogger(TradeServiceMockImpl.class);
	private MessageService messageService;
	private SmsService smsService;
	private TradeConnPool tradeConnPool;

	public MpspMessage saveOrder(String bankId, MpspMessage message, AbstractOrderCmd cmd) {
		MpspMessage orderResp = new MpspMessage();
		orderResp.setRetCode0000();
		orderResp.put(HFBusiDict.MERID, "9996");
		orderResp.put(HFBusiDict.ORDERID, "111000");
		orderResp.put(HFBusiDict.GOODSID, "11111222223");
		orderResp.put(HFBusiDict.AMOUNT, "1000");
		orderResp.put(HFBusiDict.ORDERDATE, "20111201");
		orderResp.put(HFBusiDict.ORDERID4p, "222111");
		orderResp.put(HFBusiDict.MOBILEID, "15110259623");
		orderResp.put(HFBusiDict.ORDERSTATE, "2");
		return orderResp;
	}
	
	public MpspMessage queryBalance(String merId, String mobileId) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		responseMsgGlobal.setRetCode0000();
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
		String srvURL = messageService.getSystemParam(DataDict.TRADE_SRV_URL);
		if(srvURL.endsWith("/")){
			srvURL = srvURL.substring(0, srvURL.length()-1); 
		}
		return srvURL;
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

	public MpspMessage channelPay(ChannelPayCmd cmd) {
		// TODO Auto-generated method stub
		return null;
	}

	public MpspMessage channelRevoke(ChannelRevokeCmd cmd) {
		// TODO Auto-generated method stub
		return null;
	}

	public MpspMessage asynOrder(String bankId, MpspMessage message,
			AbstractOrderCmd cmd) {
		// TODO Auto-generated method stub
		return null;
	}
	public MpspMessage queryPayment(String merId, String mobileId) {
		// TODO Auto-generated method stub
		return null;
	}

	public MpspMessage wxHfOrderSave(MpspMessage message) {
		// TODO Auto-generated method stub
		return null;
	}

	

	public MpspMessage wxUFNotifyService(MpspMessage message) {
		// TODO Auto-generated method stub
		return null;
	}

	public MpspMessage queryMobileInfo(String merId, String mobileId) {
		// TODO Auto-generated method stub
		return null;
	}

	public MpspMessage preAuthPay(MpspMessage message) {
		// TODO Auto-generated method stub
		return null;
	}

	public MpspMessage qdOrderCreate(RequestMsg requestMsg) {
		// TODO Auto-generated method stub
		return null;
	}

	public MpspMessage specialMerPay(RequestMsg requestMsg) {
		// TODO Auto-generated method stub
		return null;
	}

	public MpspMessage wxQueryUserInf(RequestMsg requestMsg) {
		// TODO Auto-generated method stub
		return null;
	}

	public MpspMessage wapDirectPay(PageOrderCmd cmd, String mobileId) {
		// TODO Auto-generated method stub
		return null;
	}

}
