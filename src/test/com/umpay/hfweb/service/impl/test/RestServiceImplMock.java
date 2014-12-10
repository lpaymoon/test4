package com.umpay.hfweb.service.impl.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.command.AbstractOrderCmd;
import com.umpay.hfweb.common.RestConnPool;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.service.MessageService;
import com.umpay.hfweb.service.RestService;
import com.umpay.hfweb.service.impl.RestServiceImpl;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;

public class RestServiceImplMock implements RestService{
private Map<String, String> retCodesMap = new HashMap<String, String>();
private static Logger log = Logger.getLogger(RestServiceImplMock.class);
	public RestServiceImplMock(){ 
		initRetCodeMap();
	}
//	public MpspMessage checkTrade(String mobileId, String merId, String goodsId){
//		MpspMessage responseMsgGlobal = new MpspMessage();
//		Map<String, String> map = new HashMap<String, String>();
//		map.put(HFBusiDict.MOBILEID, mobileId);
//		map.put(HFBusiDict.MERID, merId);
//		map.put(HFBusiDict.GOODSID, goodsId);
//		//调用资源服务层交易鉴权接口
//		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
//		String id =  new StringBuffer(mobileId).append("-").append(merId).append("-").append(goodsId).toString();
//		StringBuffer requstURL = new StringBuffer(getRestSrvPath()).append("/checktrans/mobileid/").append(rpid).append("/").append(id).append(".xml");
//		MpspMessage respMessage = restConnPool.getHttpRest(requstURL.toString(), map);
//		exchangeResult(responseMsgGlobal,respMessage, DataDict.SYSTEM_ERROR_CODE);
//		if(!responseMsgGlobal.getRetCode().equals(DataDict.SUCCESS_RET_CODE)){
//			return responseMsgGlobal;
//		}
//		//交易鉴权成功,确认可支付银行
//		List<Map<String, Object>> payBankList = (List<Map<String, Object>>)respMessage.get(HFBusiDict.MERBANKS);
//		List<Map<String, Object>> userBankList = (List<Map<String, Object>>)respMessage.get(HFBusiDict.USERBANKS);
//		if(payBankList!=null && userBankList!=null && payBankList.size()>0 && userBankList.size()>0){
//			List<Map<String, Object>> combineList = findCombineBanks(
//					payBankList, userBankList);
//			if(combineList.size() <= 0){
//				//无可支付银行
//				log.info("**********no_bank_to_pay***********");
//				return responseMsgGlobal;
//			}
//			String bankId = getBankId(combineList);
//			responseMsgGlobal.put(HFBusiDict.BANKID, bankId);
//		}
//		return responseMsgGlobal;
//	}
	
	public MpspMessage queryMerGoodsInfo(String merId, String goodsId) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		//调用资源层商品查询服务
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.MERID, merId);
		map.put(HFBusiDict.GOODSID, goodsId);
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		String id = merId + "-" + goodsId;
		StringBuffer requstURL = new StringBuffer(getRestSrvPath()).append("/checktrans/common/").append(rpid).append("/").append(id).append(".xml");
		log.info("queryMerGoodsInfo  REST_SRV_URL: "+requstURL.toString());
		MpspMessage respMessage = restConnPool.doGet(requstURL.toString(), map);
		exchangeResult(responseMsgGlobal,respMessage, DataDict.SYSTEM_ERROR_CODE);
		return responseMsgGlobal;
	}

	public MpspMessage cancelMonthUserState(String merId, String mobileId,
			String goodsId) {
		MpspMessage message = new MpspMessage();
		message.setRetCode0000();
		return message;
	}

//	public MpspMessage queryMerGoodsInfo(String merId, String goodsId) {
//		MpspMessage message = new MpspMessage();
//		message.setRetCode0000();
//		message.put(HFBusiDict.SERVTYPE, "2");
//		message.put(HFBusiDict.SERVMONTH, "11");
//		message.put(HFBusiDict.MERNAME, "小捣蛋鬼来也");
//		message.put(HFBusiDict.CUSPHONE, "010-89471234");
//		return message;
//	}

	public MpspMessage checkSign(String merId, String plainText,
			String signedText) {
		MpspMessage message = new MpspMessage();
		message.setRetCode0000();
		return message;
	}

	public MpspMessage checkTrade(String mobileId, String merId, String goodsId) {
		MpspMessage message = new MpspMessage();
		message.setRetCode0000();
		message.put(HFBusiDict.BANKID, "XE01000");
		message.put(HFBusiDict.SERVTYPE, "2");
		message.put(HFBusiDict.SERVMONTH, "11");
		message.put(HFBusiDict.MERNAME, "怎么回事");
		message.put(HFBusiDict.PORDERID, "PORDERID002");
		message.put(HFBusiDict.CUSPHONE, "010-89471234");
	    message.put(HFBusiDict.CUSPHONE, "010-89471234");
	    message.put(HFBusiDict.GOODSNAME, "瞎添的");

		return message;
	}

	public MpspMessage getMobileSeg(String mobileId) {
		MpspMessage message = new MpspMessage();
		message.setRetCode0000();
		return message;
	}

	public MpspMessage queryMerInfo(String merId) {
		MpspMessage message = new MpspMessage();
		message.setRetCode0000();
		return message;
	}

	public MpspMessage queryMerOrder(String merId, String merDate,
			String orderId) {
		MpspMessage message = new MpspMessage();
		message.setRetCode0000();
		return message;
	}

	public MpspMessage queryMonthUserState(String merId, String mobileId,
			String goodsId) {
		MpspMessage message = new MpspMessage();
		message.setRetCode0000();
		return message;
	}

	public MpspMessage queryOrderByMobileId(String mobileId, String porderId) {
		MpspMessage message = new MpspMessage();
		message.setRetCode0000();
		MpspMessage orderInfoResp = new MpspMessage();
		orderInfoResp.setRetCode0000();
		orderInfoResp.put(HFBusiDict.MERID, "9996");
		orderInfoResp.put(HFBusiDict.ORDERID, "111000");
		orderInfoResp.put(HFBusiDict.GOODSID, "11111");
		orderInfoResp.put(HFBusiDict.AMOUNT, "1000");
		orderInfoResp.put(HFBusiDict.ORDERDATE, "20111201");
		orderInfoResp.put(HFBusiDict.PORDERID, "222111");
		orderInfoResp.put(HFBusiDict.BANKID, "XE01000");
		orderInfoResp.put(HFBusiDict.ORDERSTATE, "2");
		orderInfoResp.put(HFBusiDict.VERSION, "3.0");
		orderInfoResp.put(HFBusiDict.PLATDATE, "20121221");
		orderInfoResp.put(HFBusiDict.VERIFYCODE, "8");
		return orderInfoResp;
	}
	/**
	 * ********************************************
	 * method name   : getBankId 
	 * description   : 根据支付银行列表和用户银行列表交集，获取唯一的bankId（小额优先）
	 * @return       : String
	 * @param        : @param combineList
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 11, 2011 11:27:57 AM
	 * @see          : 
	 * *******************************************
	 */
	private String getBankId(List<Map<String, Object>> combineList) {
		String bankId = "";
		for (Map<String, Object> combineMap : combineList) {
			String tempBankId = (String)combineMap.get(HFBusiDict.BANKID);
			if(tempBankId.startsWith("XE")){
				bankId = tempBankId;
				break;
			}else{
				bankId = tempBankId;
			}
		}
		return bankId;
	}
	/**
	 * ********************************************
	 * method name   : findCombineBanks 
	 * description   : 取得支付银行和用户银行列表交集
	 * @return       : List<Map<String,Object>>
	 * @param        : @param payBankList
	 * @param        : @param userBankList
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 11, 2011 11:26:07 AM
	 * @see          : 
	 * *******************************************
	 */
	private List<Map<String, Object>> findCombineBanks(
			List<Map<String, Object>> payBankList,
			List<Map<String, Object>> userBankList) {
		List<Map<String, Object>> combineList = new ArrayList<Map<String, Object>>();
		for (Iterator iterator = payBankList.iterator(); iterator.hasNext();) {
			Map<String, Object> payBankMap = (Map<String, Object>) iterator.next();
			String payBankKey = (String)payBankMap.get(HFBusiDict.BANKID);
			for (Iterator iterator2 = userBankList.iterator(); iterator2
					.hasNext();) {
				Map<String, Object>  userBankMap = (Map<String, Object>) iterator2.next();
				String userBankKey = (String)userBankMap.get(HFBusiDict.BANKID);
				if(payBankKey.equals(userBankKey)){
					combineList.add(payBankMap);
				}
			}
			
		}
		return combineList;
	}
	/**
	 * ********************************************
	 * method name   : getRestSrvPath 
	 * description   : 获取资源层服务路径
	 * @return       : String
	 * modified      : yangwr ,  Nov 4, 2011  11:11:03 AM
	 * @see          : 
	 * *******************************************
	 */
	private String getRestSrvPath(){
		String srvURL = messageService.getMessage(DataDict.REST_SRV_URL);
		if(srvURL.endsWith("/")){
			srvURL = srvURL.substring(0, srvURL.length()-1); 
		}
		return srvURL;
	}
	
	private String getRpid(){
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		return rpid;
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
				String changedCode = retCode2WebRetCode(msg.getRetCodeBussi());
				if(ObjectUtil.isNotEmpty(changedCode)){
					retCode = changedCode;
				}
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
	 * method name   : retCode2WebRetCode 
	 * description   : 业务层返回码与web返回码转换
	 * @return       : String
	 * @param        : @param retCodeBussi
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 11, 2011 11:56:21 AM
	 * @see          : 
	 * *******************************************
	 */
	private String retCode2WebRetCode(String retCodeBussi) {
//		String webRetCode = "";
//		if(retCodeBussi.equals("**商户信息不存在")){
//			webRetCode = "1133";
//		}else if(retCodeBussi.equals("**获取商户信息异常")){
//			webRetCode = "1110";
//		}else if(retCodeBussi.equals("**黑名单用户")){
//			webRetCode = "1131";
//		}else if(retCodeBussi.equals("**商户未开通支付服务")){
//			webRetCode = "1134";
//		}else if(retCodeBussi.equals("**商品未配置")){
//			webRetCode = "1135";
//		}else if(retCodeBussi.equals("**号段信息不存在")){
//			webRetCode = "1132";
//		}
//		return webRetCode;
		return retCodesMap.get(retCodeBussi);
	}

	public void initRetCodeMap(){
		retCodesMap.put("86001234", "1234");
		
		//获取商户信息异常
		retCodesMap.put("86001234", "1110");
		//黑名单用户
		retCodesMap.put("86001234", "1131");
		//号段信息不存在
		retCodesMap.put("86001234", "1132");
		//商户信息不存在
		retCodesMap.put("86001234", "1133");
		//商户未开通支付服务
		retCodesMap.put("86001234", "1134");
		//商品未配置
		retCodesMap.put("86001234", "1135");
		//商品金额不正确
		retCodesMap.put("86001234", "1146");
		//金额类型不正确
		retCodesMap.put("86001234", "1152");
		//银行类型不正确
		retCodesMap.put("86001234", "1128");
	}
	private MessageService messageService;
	private RestConnPool restConnPool; 
	
	public void setRestConnPool(RestConnPool restConnPool) {
		this.restConnPool = restConnPool;
	}
	
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public MpspMessage transacl(MpspMessage message,
			AbstractOrderCmd cmd) {
		// TODO Auto-generated method stub
		return null;
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
