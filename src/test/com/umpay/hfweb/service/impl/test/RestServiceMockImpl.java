package com.umpay.hfweb.service.impl.test;

import java.util.Map;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.command.AbstractOrderCmd;
import com.umpay.hfweb.common.RestConnPool;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.service.MessageService;
import com.umpay.hfweb.service.RestService;

public class RestServiceMockImpl implements RestService{
	
	//private static Logger log = Logger.getLogger(RestServiceMockImpl.class);
	@SuppressWarnings("unused")
	private MessageService messageService;
	@SuppressWarnings("unused")
	private RestConnPool restConnPool;
	
	/**
	 * ********************************************
	 * method name   : getMobileSeg 
	 * modified      : yangwr ,  Nov 4, 2011
	 * @see          : @see com.umpay.hfweb.service.RestService#getMobileSeg(java.lang.String)
	 * *******************************************
	 */
	public MpspMessage getMobileSeg(String mobileId){ 
		MpspMessage responseMsgGlobal = new MpspMessage();
		responseMsgGlobal.setRetCode0000();
		return responseMsgGlobal;
	}
	

	public MpspMessage queryMonthUserState(String merId,String mobileId,String goodsId) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		responseMsgGlobal.setRetCode0000();
		return responseMsgGlobal;
	}
	
	/**
	 * ********************************************
	 * method name   : cancelMonthUserState 
	 * modified      : yangwr ,  Nov 4, 2011
	 * @see          : @see com.umpay.hfweb.service.RestService#cancelMonthUserState(java.lang.String, java.lang.String, java.lang.String)
	 * *******************************************
	 */
	public MpspMessage cancelMonthUserState(String merId,String mobileId,String goodsId){
		MpspMessage responseMsgGlobal = new MpspMessage();
		responseMsgGlobal.setRetCode0000();
		return responseMsgGlobal;
	}
	
	/**
	 * ********************************************
	 * method name   : checkMerInfo 
	 * modified      : yangwr ,  Nov 2, 2011
	 * @see          : @see com.umpay.hfweb.service.RestService#checkMerInfo(java.lang.String)
	 * *******************************************
	 */
	public MpspMessage queryMerInfo(String merId){
		MpspMessage responseMsgGlobal = new MpspMessage();
		responseMsgGlobal.setRetCode0000();
		return responseMsgGlobal;
	}
	
	/**
	 * ********************************************
	 * method name   : checkSign 
	 * modified      : yangwr ,  Nov 2, 2011
	 * @see          : @see com.umpay.hfweb.service.RestService#checkSign(java.lang.String, java.lang.String, java.lang.String)
	 * *******************************************
	 */
	public MpspMessage checkSign(String merId, String plainText, String signedText){
		MpspMessage responseMsgGlobal = new MpspMessage();
		responseMsgGlobal.setRetCode0000();
		return responseMsgGlobal;
	}

	public MpspMessage queryMerOrder(String merId, String merDate, String orderId ) {
		MpspMessage orderInfoResp = new MpspMessage();
		orderInfoResp.setRetCode0000();
		orderInfoResp.put(HFBusiDict.MERID, "9996");
		orderInfoResp.put(HFBusiDict.ORDERID, "111000");
		orderInfoResp.put(HFBusiDict.GOODSID, "11111222223");
		orderInfoResp.put(HFBusiDict.AMOUNT, "1000");
		orderInfoResp.put(HFBusiDict.ORDERDATE, "20111201");
		orderInfoResp.put(HFBusiDict.PORDERID, "222111");
		orderInfoResp.put(HFBusiDict.BANKID, "MW01000");
		orderInfoResp.put(HFBusiDict.ORDERSTATE, "2");
		orderInfoResp.put(HFBusiDict.VERSION, "3.0");
		orderInfoResp.put(HFBusiDict.PLATDATE, "20121221");
		orderInfoResp.put(HFBusiDict.VERIFYCODE, "8");
		orderInfoResp.put(HFBusiDict.RESERVED, "0000");
		return orderInfoResp;
	}
	public MpspMessage queryOrderByMobileId(String mobileId, String porderId) {
		MpspMessage orderInfoResp = new MpspMessage();
		orderInfoResp.setRetCode0000();
		orderInfoResp.put(HFBusiDict.MERID, "9996");
		orderInfoResp.put(HFBusiDict.ORDERID, "111000");
		orderInfoResp.put(HFBusiDict.GOODSID, "11111222223");
		orderInfoResp.put(HFBusiDict.AMOUNT, "1000");
		orderInfoResp.put(HFBusiDict.ORDERDATE, "20111201");
		orderInfoResp.put(HFBusiDict.PORDERID, "222111");
		orderInfoResp.put(HFBusiDict.BANKID, "MW01000");
		orderInfoResp.put(HFBusiDict.ORDERSTATE, "3");
		orderInfoResp.put(HFBusiDict.VERSION, "3.0");
		orderInfoResp.put(HFBusiDict.PLATDATE, "20121221");
		orderInfoResp.put(HFBusiDict.VERIFYCODE, "8");
		return orderInfoResp;
	}
	public MpspMessage checkTrade(String mobileId, String merId, String goodsId){
		MpspMessage responseMsgGlobal = new MpspMessage();
		responseMsgGlobal.setRetCode0000();
		responseMsgGlobal.put(HFBusiDict.BANKID, "MW01000");
		responseMsgGlobal.put(HFBusiDict.SERVTYPE, "2");
		responseMsgGlobal.put(HFBusiDict.SERVMONTH, "11");
		responseMsgGlobal.put(HFBusiDict.MERNAME, "联动优势VIP商户");
		responseMsgGlobal.put(HFBusiDict.PORDERID, "PORDERID002");
		responseMsgGlobal.put(HFBusiDict.CUSPHONE, "xxxx-xxxxxxx");
		responseMsgGlobal.put(HFBusiDict.GOODSNAME, "VIP商户商品");
		return responseMsgGlobal;
	}
	
	public MpspMessage queryMerGoodsInfo(String merId, String goodsId) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		responseMsgGlobal.setRetCode0000();
		responseMsgGlobal.put(HFBusiDict.BANKID, "MW01000");
		responseMsgGlobal.put(HFBusiDict.SERVTYPE, "2");
		responseMsgGlobal.put(HFBusiDict.SERVMONTH, "11");
		responseMsgGlobal.put(HFBusiDict.MERNAME, "联动优势VIP商户");
		responseMsgGlobal.put(HFBusiDict.PORDERID, "PORDERID002");
		responseMsgGlobal.put(HFBusiDict.CUSPHONE, "xxxx-xxxxxxx");
		responseMsgGlobal.put(HFBusiDict.GOODSNAME, "VIP商户商品");
		return responseMsgGlobal;
	}
	
	public MpspMessage transacl(MpspMessage message, AbstractOrderCmd cmd) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		responseMsgGlobal.setRetCode0000();
		return responseMsgGlobal;
	}

	public void setRestConnPool(RestConnPool restConnPool) {
		this.restConnPool = restConnPool;
	}
	
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}


	public MpspMessage queryMerReferInf(String merid, String goodsid) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage createWxOrder(MpspMessage message) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage getGoodsBank(String merid, String goodsid) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage addWxUser(MpspMessage message) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage checkRandomKey(MpspMessage message) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage createOrder(Map<String, String> reqMap) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage getHistoryTrans(RequestMsg requestMsg) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage getSmsRandomKey(String mobileid) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage queryWXPlatOrder(String platOrderId) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage saveWXPlatOrder(MpspMessage message) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage updateOrder(Map<String, String> reqMap) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage addWxUserReplyInf(RequestMsg requestMsg) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage checkChnlSign(String chnlid, String signStr,
			String unSignStr) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage getRandomVerifyTimes(String mobileid) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage getWxUserSeg(RequestMsg requestMsg) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage queryClientConf(String clientName, String clientType) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage queryMobileidInf(String mobileid) {
		// TODO Auto-generated method stub
		return null;
	}


	public MpspMessage queryQDOrder(RequestMsg requestMsg) {
		// TODO Auto-generated method stub
		return null;
	}

}
