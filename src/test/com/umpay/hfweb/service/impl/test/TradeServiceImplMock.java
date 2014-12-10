package com.umpay.hfweb.service.impl.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.command.AbstractOrderCmd;
import com.umpay.hfweb.action.command.ChannelPayCmd;
import com.umpay.hfweb.action.command.ChannelRevokeCmd;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.common.TradeConnPool;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.service.MessageService;
import com.umpay.hfweb.service.SmsService;
import com.umpay.hfweb.service.TradeService;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;

public class TradeServiceImplMock implements TradeService{
	
	private static Logger log = Logger.getLogger(TradeServiceImplMock.class);
	private MessageService messageService;
	private SmsService smsService;
	private TradeConnPool tradeConnPool;
	
	public MpspMessage queryBalance(String merId, String mobileId) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		String id = new StringBuffer(getRpid()).append("/").append(mobileId).append("-").append(merId).toString();
		//1-查询余额
		StringBuffer requstQueryURL = new StringBuffer(getTradeSrvPath()).append("/todo/todo/").append(id).append(".xml");
		log.info("queryBalance  requstURL: "+requstQueryURL.toString());
		Map<String, String> para4Query = new HashMap<String, String>();
//		long dt = DateTimeUtil.currentDateTime();
//		para4Query.put(HFBusiDict.MERID,merId);
		para4Query.put(HFBusiDict.MOBILEID, mobileId);
		para4Query.put(HFBusiDict.RPID, getRpid());
//		para4Query.put(HFBusiDict.ACCESSTYPE, "W");
//		para4Query.put(HFBusiDict.CALLED, "WEB");
		//para4Query.put(HFBusiDict.FUNCODE, funcode); 改为业务层处理
//		para4Query.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
//		para4Query.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
//		para4Query.put(HFBusiDict.CALLING, mobileId);
		
		MpspMessage respQueryMsg = tradeConnPool.doPost(requstQueryURL.toString(), para4Query);
		if(!respQueryMsg.isRetCode0000()){
			if(respQueryMsg.isRetCodeSysError()){
				responseMsgGlobal.setRetCodeSysError();
				return responseMsgGlobal;
			}
			//1166 查询用户余额失败
			responseMsgGlobal.setRetCode("1166");
		}else{
			responseMsgGlobal.setRetCode0000();
			responseMsgGlobal.put(HFBusiDict.AMT, respQueryMsg.get(HFBusiDict.AMT));
		}
		return responseMsgGlobal;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
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
		String srvURL = messageService.getMessage(DataDict.TRADE_SRV_URL);
		if(srvURL.endsWith("/")){
			srvURL = srvURL.substring(0, srvURL.length()-1); 
		}
		return srvURL;
	}
	
	private String getRpid(){
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		return rpid;
	}

	public void setTradeConnPool(TradeConnPool tradeConnPool) {
		this.tradeConnPool = tradeConnPool;
	}

	public MpspMessage saveOrder(String bankId, MpspMessage message,
			AbstractOrderCmd cmd) {
		MpspMessage orderResp = new MpspMessage();
		orderResp.setRetCode0000();
		orderResp.put(HFBusiDict.MERID, "9996");
		orderResp.put(HFBusiDict.ORDERID, "111000");
		orderResp.put(HFBusiDict.GOODSID, "11111");
		orderResp.put(HFBusiDict.AMOUNT, "1000");
		orderResp.put(HFBusiDict.ORDERDATE, "20111201");
		orderResp.put(HFBusiDict.ORDERID4p, "222111");
		orderResp.put(HFBusiDict.MOBILEID, "15110259623");
		orderResp.put(HFBusiDict.ORDERSTATE, "2");
		return orderResp;
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
