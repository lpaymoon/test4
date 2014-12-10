package com.umpay.hfweb.service.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bs.mpsp.util.DateTimeUtil;
import com.bs.mpsp.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.command.AbstractOrderCmd;
import com.umpay.hfweb.action.command.H5verifyCodePayCmd;
import com.umpay.hfweb.common.RestConnPool;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.service.MessageService;
import com.umpay.hfweb.service.RestService;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;

public class RestServiceImpl implements RestService{
	
	private static Logger log = Logger.getLogger(RestServiceImpl.class);
	private MessageService messageService;
	private RestConnPool restConnPool;
	
	/**
	 * ********************************************
	 * method name   : getMobileSeg 
	 * modified      : yangwr ,  Nov 4, 2011
	 * @see          : @see  com.umpay.hfweb.service.RestService#getMobileSeg(java.lang.String)
	 * *******************************************
	 */
	public MpspMessage getMobileSeg(String mobileId){
		MpspMessage responseMsgGlobal = new MpspMessage();
		String pre7num = mobileId.substring(0, 7);
		String id = mobileId.substring(0, 7);
		//1-查询包月定制关系
		String requstQueryURL = new StringBuffer(getRestSrvPath()).append("/seginf/").append(getRpid()).append("/").append(id).append(".xml").toString();
		logInfo("MobileSeg Query RequstURL %s",requstQueryURL);
		Map<String, String> para4Query = new HashMap<String, String>();
		para4Query.put(HFBusiDict.MOBILEID, pre7num);
		MpspMessage respQueryMsg = restConnPool.doGet(requstQueryURL, para4Query);
		//1132 号段信息不存在
		exchangeResult(responseMsgGlobal,respQueryMsg,"1132");
		logInfo("MobileSeg Query Result[RetCode]%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}
	

	public MpspMessage queryMonthUserState(String merId,String mobileId,String goodsId) {
		//1-查询包月定制关系
		MpspMessage responseMsgGlobal = new MpspMessage();
		String id = new StringBuffer(getRpid()).append("/").append(mobileId).append("-").append(merId).append("-")
					.append(goodsId).toString();
		String requstQueryURL = new StringBuffer(getRestSrvPath()).append("/hfuser/common/").append(id).append(".xml").toString();
		logInfo("MonthUserState Query RequstURL %s",requstQueryURL);
		Map<String, String> para4Query = new HashMap<String, String>();
		para4Query.put(HFBusiDict.MERID,merId);
		para4Query.put(HFBusiDict.GOODSID, goodsId);
		para4Query.put(HFBusiDict.MOBILEID, mobileId);
		MpspMessage respQueryMsg = restConnPool.doGet(requstQueryURL, para4Query);
		//1201 用户没有定制该服务
		exchangeResult(responseMsgGlobal,respQueryMsg,"1201");
		logInfo("MonthUserState Query Result[RetCode]%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
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
		String id = new StringBuffer(getRpid()).append("/").append(mobileId).append("-").append(merId).append("-")
					.append(goodsId).toString();
//		//1-查询包月定制关系
//		log.info("MonthUserState Check Before Cancel--rpid[" + getRpid());
//		MpspMessage respQueryMsg = queryMonthUserState(merId,mobileId,goodsId);
//		if(!respQueryMsg.isRetCode0000()){
//			responseMsgGlobal.setRetCode(respQueryMsg.getRetCode());
//			responseMsgGlobal.setRetCodeBussi(respQueryMsg.getRetCodeBussi());
//			log.info("MonthUserState Cancel Failed --rpid[" + getRpid() + "]:" 
//					+ responseMsgGlobal.getRetCode() + ":" + responseMsgGlobal.getRetCodeBussi());
//			return responseMsgGlobal;
//		}
//
//		String retCode = "";
//		String state = ObjectUtil.trim(respQueryMsg.get(HFBusiDict.STATE));
//		if(state.equals("0")){
//			//1202 已提交申请，还未开通服务!
//			retCode = "1202";
//		}else if(state.equals("3")){
//			//用户已经取消服务，在平台计费失败取消服务
//			retCode = "1203";
//		}else if(state.equals("4")){
//			//用户已经取消服务，在sp取消
//			retCode = "1204";
//		}else if(state.equals("5") || state.equals("6") || state.equals("7")){
//			//用户已经取消服务，在平台取消
//			retCode = "1205";
//		}else if(!state.equals("2")){
//			//用户状态不正确，不能取消
//			retCode = "1207";
//		}
//		if(!retCode.equals("")){
//			responseMsgGlobal.setRetCode(retCode);
//			log.info("MonthUserState Check Before Cancel Failed --rpid[" + getRpid() + "]:" 
//					+ responseMsgGlobal.getRetCode() + ":" + responseMsgGlobal.getRetCodeBussi());
//			return responseMsgGlobal;
//		}
//		log.info("MonthUserState Check Success--rpid[" + getRpid());
		//2-调用资源层接口，取消订购关系
		String requstCancelURL = new StringBuffer(getRestSrvPath()).append("/hfuser/state/").append(id).append(".xml").toString();
		logInfo("MonthUserState Cancel RequstURL %s",requstCancelURL);
		Map<String, String> para4Cancel = new HashMap<String, String>();
		para4Cancel.put(HFBusiDict.MERID,merId);
		para4Cancel.put(HFBusiDict.GOODSID, goodsId);
		para4Cancel.put(HFBusiDict.MOBILEID, mobileId);
		para4Cancel.put(HFBusiDict.BSTATE, "4"); //4-注销 更新为注销
		para4Cancel.put(HFBusiDict.FSTATE, "2"); //
		para4Cancel.put(HFBusiDict.CAUSE, "4");
		para4Cancel.put(HFBusiDict.DETAIL, "商户取消服务");
		MpspMessage respCancelMsg = restConnPool.doPost(requstCancelURL, para4Cancel);
		//1305 取消包月定制关系失败
		exchangeResult(responseMsgGlobal,respCancelMsg,"1305");
		logInfo("MonthUserState Cancel Result[RetCode]%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
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
		//调用资源层商户验签服务
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.MERID, merId);
		String rpid = getRpid();
		StringBuffer requstURL = new StringBuffer(getRestSrvPath()).append("/merinf/").append(rpid).append("/").append(merId).append(".xml");
		logInfo("Mer Check RequstURL %s",requstURL);
		MpspMessage respMessage = restConnPool.doGet(requstURL.toString(), map);
		//1134 商户未开通
		exchangeResult(responseMsgGlobal,respMessage,"1134");
		logInfo("Mer Check Result[RetCode]%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
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
		logInfo("SignCheck plainText %s",plainText);
		logInfo("SignCheck signedText %s",signedText);
		MpspMessage responseMsgGlobal = new MpspMessage();
		//调用资源层商户验签服务
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.UNSIGNSTR, plainText);
		map.put(HFBusiDict.SIGNSTR, signedText);
		map.put(HFBusiDict.MERID, merId);
		StringBuffer requstURL = new StringBuffer(getRestSrvPath()).append("/mersign/").append(getRpid()).append("/").append(merId).append(".xml");
		logInfo("SignCheck RequstURL %s",requstURL);
		MpspMessage respMessage = restConnPool.doGet(requstURL.toString(), map);
		
		/*
		 * 20131105 liujilong 临时增加一条规则,凡时因与REST连接异常导致的验签失败全都算成功
		 */
		if(respMessage.isRetCodeSysError())	respMessage.setRetCode0000();
		
		//1144 商户签名验证异常
		exchangeResult(responseMsgGlobal,respMessage,"1144");
		logInfo("SignCheck Result[RetCode]%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}

	public MpspMessage queryMerOrder(String merId, String merDate, String orderId ) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.MERID, merId);
		map.put(HFBusiDict.ORDERID, orderId);
		map.put(HFBusiDict.ORDERDATE, merDate);
		//调用资源服务层话费订单信息商户接口
		String id =  new StringBuffer(merId).append("-").append(orderId).append("-").append(merDate).toString();
		String requstURL = new StringBuffer(getRestSrvPath()).append("/hforder/mer/").append(getRpid()).append("/").append(id).append(".xml").toString();
		logInfo("MerOrder Query RequstURL %s",requstURL);
		MpspMessage respMessage = restConnPool.doGet(requstURL, map);
		//1139 产品支付服务已关闭
		exchangeResult(responseMsgGlobal,respMessage,"1139");
		logInfo("MerOrder Query Result[RetCode]%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}
	public MpspMessage checkTrade(String mobileId, String merId, String goodsId){
		MpspMessage responseMsgGlobal = new MpspMessage();
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.MOBILEID, mobileId);
		map.put(HFBusiDict.MERID, merId);
		map.put(HFBusiDict.GOODSID, goodsId);
		//调用资源服务层交易鉴权接口
		String rpid = getRpid();
		String id =  new StringBuffer(mobileId).append("-").append(merId).append("-").append(goodsId).toString();
		String requstURL = new StringBuffer(getRestSrvPath()).append("/checktrans/mobileid/").append(rpid).append("/").append(id).append(".xml").toString();
		logInfo("Trade Check RequstURL %s", requstURL);
		MpspMessage respMessage = restConnPool.doGet(requstURL.toString(), map);
		//1301 交易鉴权失败
		exchangeResult(responseMsgGlobal,respMessage, "1301");
		if(!responseMsgGlobal.isRetCode0000()){
			logInfo("Trade Check Result[RetCode]%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
			return responseMsgGlobal;
		}
		//返回信息处理--只返回下单用到的信息
		responseMsgGlobal.getWrappedMap().clear();
		responseMsgGlobal.setRetCode0000();
		responseMsgGlobal.put(HFBusiDict.SERVTYPE, respMessage.get(HFBusiDict.SERVTYPE));
		responseMsgGlobal.put(HFBusiDict.SERVMONTH, respMessage.get(HFBusiDict.SERVMONTH));
		responseMsgGlobal.put(HFBusiDict.PRICEMODE, respMessage.get(HFBusiDict.PRICEMODE));
		responseMsgGlobal.put(HFBusiDict.GOODSNAME, respMessage.get(HFBusiDict.GOODSNAME));
		responseMsgGlobal.put(HFBusiDict.MERNAME, respMessage.get(HFBusiDict.MERNAME));
		responseMsgGlobal.put(HFBusiDict.EXPIRETIME, respMessage.get(HFBusiDict.EXPIRETIME));
		responseMsgGlobal.put(HFBusiDict.CUSPHONE, respMessage.get(HFBusiDict.CUSPHONE));
		responseMsgGlobal.put(HFBusiDict.ISCONTROL, respMessage.getStr(HFBusiDict.ISCONTROL));
		responseMsgGlobal.put(HFBusiDict.PROVCODE, respMessage.getStr(HFBusiDict.PROVCODE));
		responseMsgGlobal.put(HFBusiDict.AREACODE, respMessage.getStr(HFBusiDict.AREACODE));
		responseMsgGlobal.put(HFBusiDict.NETTYPE, respMessage.getStr(HFBusiDict.NETTYPE));
		responseMsgGlobal.put(HFBusiDict.CARDTYPE, respMessage.getStr(HFBusiDict.CARDTYPE));
		responseMsgGlobal.put(HFBusiDict.GRADE, respMessage.getStr(HFBusiDict.GRADE));
		responseMsgGlobal.put(HFBusiDict.BUSIROLLTYPE, respMessage.getStr(HFBusiDict.BUSIROLLTYPE));
		
		StringBuffer jqMessage = new StringBuffer("Trade Check Result--rpid[").append(getRpid()).append("]--priceMode:").append(responseMsgGlobal.get(HFBusiDict.PRICEMODE));
		jqMessage.append("&expireTime:").append(responseMsgGlobal.get(HFBusiDict.EXPIRETIME));
		jqMessage.append("&servType:").append(responseMsgGlobal.get(HFBusiDict.SERVTYPE));
		jqMessage.append("&servMonth:").append(responseMsgGlobal.get(HFBusiDict.SERVMONTH));
		jqMessage.append("&goodsName:").append(responseMsgGlobal.get(HFBusiDict.GOODSNAME));
		logInfo(jqMessage.toString());
		
		//交易鉴权成功,确认可支付银行
		List<Map<String, Object>> payBankList = (List<Map<String, Object>>)respMessage.get(HFBusiDict.MERBANKS);
		List<Map<String, Object>> userBankList = (List<Map<String, Object>>)respMessage.get(HFBusiDict.USERBANKS);
		
		responseMsgGlobal.put(HFBusiDict.MERBANKS, payBankList);
		responseMsgGlobal.put(HFBusiDict.USERBANKS, userBankList);
		
		String bankId = "";
		if(payBankList!=null && userBankList!=null && payBankList.size()>0 && userBankList.size()>0){
			List<Map<String, Object>> combineList = findCombineBanks(
					payBankList, userBankList);
			if(combineList.size() <= 0){
				//无可支付银行
				log.info("Trade Check Result No BankId To Use");
				return responseMsgGlobal;
			}
			bankId = getBankId(combineList);
			responseMsgGlobal.put(HFBusiDict.BANKID, bankId);
		}
		logInfo("Trade Check Result[RetCode]%s:%s bankId:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi(),bankId);
		return responseMsgGlobal;
	}
	
	public MpspMessage queryMerGoodsInfo(String merId, String goodsId) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		//调用资源层商品查询服务
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.MERID, merId);
		map.put(HFBusiDict.GOODSID, goodsId);
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		String id = merId + "-" + goodsId;
		String requstURL = new StringBuffer(getRestSrvPath()).append("/checktrans/common/").append(rpid).append("/").append(id).append(".xml").toString();
		logInfo("MerGoods Query RequstURL %s",requstURL);
		MpspMessage respMessage = restConnPool.doGet(requstURL, map);
		//1302 查询商户商品失败
		exchangeResult(responseMsgGlobal,respMessage, "1302");
		logInfo("MerGoods Query Result[RetCode]%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}
	
	public MpspMessage transacl(MpspMessage message, AbstractOrderCmd cmd) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		//1-请求参数组装 START
		Map<String, String> req = new HashMap<String, String>();
		req.put(HFBusiDict.MERID, cmd.getMerId());
		req.put(HFBusiDict.GOODSID, cmd.getGoodsId());
		req.put(HFBusiDict.MOBILEID, cmd.getMobileId());
		req.put(HFBusiDict.BANKID, message.getStr(HFBusiDict.BANKID));
		req.put(HFBusiDict.ISCONTROL, message.getStr(HFBusiDict.ISCONTROL));
		req.put(HFBusiDict.PROVCODE, message.getStr(HFBusiDict.PROVCODE));
		req.put(HFBusiDict.AREACODE, message.getStr(HFBusiDict.AREACODE));
		req.put(HFBusiDict.NETTYPE, message.getStr(HFBusiDict.NETTYPE));
		req.put(HFBusiDict.CARDTYPE, message.getStr(HFBusiDict.CARDTYPE));
		//req.put(HFBusiDict.GRADE, message.getStr(HFBusiDict.GRADE)); //交易鉴权目前不取用户等级,此处不传由交易屏蔽去取 modify by yangwr
		//1-请求参数组装 END
		
		//2-交易屏蔽校验执行 START
		String id = new StringBuffer(cmd.getMerId()).append("-").append(cmd.getGoodsId()).append("-").append(cmd.getMobileId()).append("-").append(message.getStr(HFBusiDict.BANKID)).append(".xml").toString();
		String requstURL = new StringBuffer(getRestSrvPath()).append("/transacl/").append(getRpid()).append("/").append(id).toString();
		MpspMessage respMessage = restConnPool.doGet(requstURL, req);
		//1308 交易屏蔽校验未通过
		exchangeResult(responseMsgGlobal,respMessage,"1308");
		logInfo("Transacl Result[RetCode]:%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		//2-交易屏蔽校验执行  END
		return responseMsgGlobal;
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
			String kstate = String.valueOf(payBankMap.get(HFBusiDict.KSTATE));
			//11只开通新增 13新增与续费全部开通
			if(kstate.equals("11") || kstate.equals("13")){
				for (Iterator iterator2 = userBankList.iterator(); iterator2.hasNext();) {
					Map<String, Object>  userBankMap = (Map<String, Object>) iterator2.next();
					String userBankKey = (String)userBankMap.get(HFBusiDict.BANKID);
					if(payBankKey.equals(userBankKey)){
						combineList.add(payBankMap);
					}
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
//		String srvURL = messageService.getSystemParam(DataDict.REST_SRV_URL);
//		if(srvURL.endsWith("/")){
//			srvURL = srvURL.substring(0, srvURL.length()-1); 
//		}
//		return srvURL;
		/*
		 * 策略负载将IP及端口资源的获取放置在底层代码	liujilong 2013-10-10
		 */
		return "";
	}
	
	private String getRpid(){
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		return rpid;
	}
	
	private void logInfo(String message,Object... args){
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String funCode = SessionThreadLocal.getSessionValue(DataDict.FUNCODE);
		log.info(String.format("%s,%s,%s",funCode,rpid,String.format(message,args)));
	}
	
//	private void logError(Exception e){
//		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
//		log.error(ObjectUtil.handlerException(e, rpid));
//	}

	
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
//				String changedCode = retCode2WebRetCode(msg.getRetCodeBussi());
//				if(ObjectUtil.isNotEmpty(changedCode)){
//					retCode = changedCode;
//				}
				responseMsgGlobal.setRetCode(retCode);
				responseMsgGlobal.setRetCodeBussi(msg.getRetCodeBussi());
			}
		}else{
			responseMsgGlobal.putAll(msg);
			responseMsgGlobal.setRetCode0000();
		}
	}
	/**
	public MpspMessage queryOrderByMobileId(String mobileId, String porderId) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(HFBusiDict.MOBILEID, mobileId);
		paramMap.put(HFBusiDict.PORDERID, porderId);
		// 调用资源服务层话费订单信息商户接口
		String id = new StringBuffer(mobileId).append("-").append(porderId).toString();
		String requstURL = new StringBuffer(getRestSrvPath()).append("/hforder/common/").append(getRpid()).append("/").append(id).append(".xml").toString();
		logInfo("OrderQuery By PorderId RequstURL %s",requstURL);
		MpspMessage respMessage = restConnPool.doGet(requstURL,paramMap);
		//1162 订单信息不存在
		exchangeResult(responseMsgGlobal,respMessage,"1162");
		logInfo("OrderQuery By PorderId Result[RetCode]%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}
	**/
	public void setRestConnPool(RestConnPool restConnPool) {
		this.restConnPool = restConnPool;
	}
	
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}


	public MpspMessage queryMerReferInf(String merid, String goodsid) {
		//调用资源层商户报备信息查询服务
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.MERID, merid);
		map.put(HFBusiDict.GOODSID, goodsid);
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		String requstURL = new StringBuffer(getRestSrvPath()).append("/merrefer/").append(rpid).append(".xml").toString();
		logInfo("HFMerRefer Query RequstURL %s", requstURL);
		MpspMessage responseMsgGlobal = restConnPool.doGet(requstURL, map);
		logInfo("HFMerRefer Query Result[RetCode]%s:%s", responseMsgGlobal.getRetCode(), responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}
	public MpspMessage createWxOrder(MpspMessage message) {
		MpspMessage responseMsg = new MpspMessage();
		Map<String,String> req = new HashMap<String,String>();
		//过期时间
		Long expireTime = (Long)message.get(HFBusiDict.EXPIRETIME);
		//处理订单有效期 订单有效时间+当前时间 （Timestamp）
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();
		long future = now + expireTime * 1000;
		String orderdate = message.getStr(DataDict.MER_REQ_MERDATE);
		String maXNum = message.getStr(HFBusiDict.MAX);
		String IMEI =  message.getStr(HFBusiDict.IMEI);
		String IMSI =  message.getStr(HFBusiDict.IMSI);
		String channelid = ObjectUtil.trim(message.getStr("chnlid"));
		if("".equals(channelid)){
			channelid="0002";
		}
//		if(IMEI==null||"".equals(IMEI)){
//			if(simId==null||"".equals(simId)){
//				IMEI="R4000000";
//			}else{
//				IMEI=simId;
//			}
//		}
		String clientVersion = message.getStr(DataDict.WX_REQ_VERSIONNAME);
		if(clientVersion==null||!clientVersion.contains(".")){
			clientVersion = message.getStr(DataDict.WX_REQ_VERSIONCODE);
		}
		req.put(HFBusiDict.EXPIRETIME, new Timestamp(future).toString());
		req.put(HFBusiDict.MERID, message.getStr(DataDict.MER_REQ_MERID));
		req.put(HFBusiDict.GOODSID, message.getStr(DataDict.MER_REQ_GOODSID));
		req.put(HFBusiDict.ORDERID, message.getStr(DataDict.MER_REQ_ORDERID));
		req.put(HFBusiDict.ORDERDATE,orderdate);
		req.put(HFBusiDict.AMOUNT, message.getStr(DataDict.MER_REQ_AMOUNT));
		req.put(HFBusiDict.MERPRIV, message.getStr(DataDict.MER_REQ_MERPRIV));
		req.put(HFBusiDict.EXPAND, message.getStr(DataDict.MER_REQ_EXPAND));
		req.put(HFBusiDict.VERSION, message.getStr(DataDict.MER_REQ_VERSION));
		req.put(HFBusiDict.SIGN, message.getStr(DataDict.MER_REQ_SIGN));
		String cid = StringUtil.trim(message.getStr(DataDict.WX_REQ_CLIENTID));
		if (cid.contains("null")) {
			cid = "nullError";
		}
		req.put(HFBusiDict.CLIENTID,cid);//客户端ID
		req.put(HFBusiDict.IMEI, IMEI);
		req.put(HFBusiDict.IMSI, IMSI); 
		req.put(HFBusiDict.CLIENTVERSION, clientVersion);
		req.put(HFBusiDict.CHANNELID, channelid);
		req.put(HFBusiDict.MAX, maXNum);
		if ("0106".equals(StringUtil.trim(message.getStr(HFBusiDict.BUSINESSTYPE)))) {
			req.put(HFBusiDict.BUSINESSTYPE, message.getStr(HFBusiDict.BUSINESSTYPE));
			req.put(HFBusiDict.VERIFYCODE, message.getStr(HFBusiDict.VERIFYCODE));
		}
		
		req.put("ICCID", StringUtil.trim(message.getStr("iccid")));
		req.put("PHONETYPE", StringUtil.trim(message.getStr("model")));
		req.put("PHONEOS", StringUtil.trim(message.getStr("mobileos")));
		req.put("PLATTPYE", StringUtil.trim(message.getStr("platType")));
		req.put("SDKTPYE", StringUtil.trim(message.getStr("sdkType")));
		
		String isRoot = "0";
		if ("true".equals(StringUtil.trim(message.getStr("isRoot")))) {
			isRoot = "1";
		}
		req.put("ISROOT", isRoot);//0：未root 1：已root
		req.put("PHONEOPERATOR", StringUtil.trim(message.getStr("networkOperatorName")));
		req.put("NETTYPE", StringUtil.trim(message.getStr("mobileNet")));
		req.put("LASTGPSLOCATION", StringUtil.trim(message.getStr("lastGpsLocation")));
		req.put("GETPHONENUM", StringUtil.trim(message.getStr("mobileNo")));
		String userAppsList = StringUtil.trim(message.getStr("userAppsList"));
		if (userAppsList.length() >512) {
			userAppsList = userAppsList.substring(0, 512);
		}
		req.put("APPSLISI", userAppsList);

		String rpid = getRpid();
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/wxOrderComplex/").append(rpid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doPost(requstCreateURL, req);
		logInfo("%s %s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		if("86001106".equals(respMessage.getStr(HFBusiDict.RETCODE))){
			//1311:订单已经存在
			exchangeResult(responseMsg,respMessage,"1311");
		}else{
			//1304:下单失败
			exchangeResult(responseMsg,respMessage,"1304");
		}
		logInfo("CreateWxOrder Result[RetCode]:%s:%s",responseMsg.getRetCode(),responseMsg.getRetCodeBussi());
		return responseMsg;
	}
	public MpspMessage createSDkLxWxOrder(MpspMessage message) {
		MpspMessage responseMsg = new MpspMessage();
		Map<String,String> req = new HashMap<String,String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String orderdate = sdf.format(new Date());//orderdate取当前日期
		String IMEI =  StringUtil.trim(message.getStr(HFBusiDict.IMEI));
		String IMSI =  StringUtil.trim(message.getStr(HFBusiDict.IMSI));
		String channelid = StringUtil.trim(message.getStr("chnlid"));
		if("".equals(channelid)){
			channelid="0002";
		}
		String clientVersion = StringUtil.trim(message.getStr(DataDict.WX_REQ_VERSIONNAME));
		if(clientVersion==null||!clientVersion.contains(".")){
			clientVersion = StringUtil.trim(message.getStr(DataDict.WX_REQ_VERSIONCODE));
		}
		req.put(HFBusiDict.MERID, StringUtil.trim(message.getStr(DataDict.MER_REQ_MERID)));
		String goodsid= StringUtil.trim(message.getStr(DataDict.MER_REQ_GOODSID));
		if ("".equals(goodsid)) {
			goodsid = "000";
		}
		req.put(HFBusiDict.GOODSID, goodsid);
		//按照短信网关生成20位的订单号   参考生成rpid的方法
		String orderId = String.format("%5s%08x%07d", "LXSDK", System.currentTimeMillis()/1000, makeInt4()%10000000);//确保%06d不超过6位
//		String orderId = getRpid();
		req.put(HFBusiDict.EXPIRETIME, new Timestamp((new Date()).getTime()+86400000).toString());
		req.put(HFBusiDict.ORDERID, orderId);
		req.put(HFBusiDict.ORDERDATE,orderdate);
		String amount = StringUtil.trim(message.getStr(DataDict.MER_REQ_AMOUNT));
		if ("".equals(amount)) {
			amount = "0";
		}
		req.put(HFBusiDict.AMOUNT, amount);
		req.put(HFBusiDict.MERPRIV, StringUtil.trim(message.getStr(DataDict.MER_REQ_MERPRIV)));
		req.put(HFBusiDict.EXPAND, StringUtil.trim(message.getStr(DataDict.MER_REQ_EXPAND)));
		req.put(HFBusiDict.VERSION, StringUtil.trim(message.getStr(DataDict.MER_REQ_VERSION)));
		req.put(HFBusiDict.SIGN, StringUtil.trim(message.getStr(DataDict.MER_REQ_SIGN)));
		String cid = StringUtil.trim(message.getStr(DataDict.WX_REQ_CLIENTID));
		if (cid.contains("null")) {
			cid = "nullError";
		}
		req.put(HFBusiDict.CLIENTID,cid);//客户端ID
		req.put(HFBusiDict.IMEI, IMEI);
		req.put(HFBusiDict.IMSI, IMSI); 
		req.put(HFBusiDict.CLIENTVERSION, clientVersion);
		req.put(HFBusiDict.CHANNELID, channelid);
		if ("0106".equals(StringUtil.trim(message.getStr(HFBusiDict.BUSINESSTYPE)))) {
			req.put(HFBusiDict.BUSINESSTYPE, message.getStr(HFBusiDict.BUSINESSTYPE));
			req.put(HFBusiDict.VERIFYCODE, message.getStr(HFBusiDict.VERIFYCODE));
		}
		req.put("ICCID", StringUtil.trim(message.getStr("iccid")));
		req.put("PHONETYPE", StringUtil.trim(message.getStr("model")));
		req.put("PHONEOS", StringUtil.trim(message.getStr("mobileos")));
		req.put("PLATTPYE", StringUtil.trim(message.getStr("platType")));
		req.put("SDKTPYE", StringUtil.trim(message.getStr("sdkType")));
		
		String isRoot = "0";
		if ("true".equals(StringUtil.trim(message.getStr("isRoot")))) {
			isRoot = "1";
		}
		req.put("ISROOT", isRoot);//0：未root 1：已root
		req.put("PHONEOPERATOR", StringUtil.trim(message.getStr("networkOperatorName")));
		req.put("NETTYPE", StringUtil.trim(message.getStr("mobileNet")));
		req.put("LASTGPSLOCATION", StringUtil.trim(message.getStr("lastGpsLocation")));
		req.put("GETPHONENUM", StringUtil.trim(message.getStr("mobileNo")));
		String userAppsList = StringUtil.trim(message.getStr("userAppsList"));
		if (userAppsList.length() >512) {
			userAppsList = userAppsList.substring(0, 512);
		}
		req.put("APPSLISI", userAppsList);

		String rpid = getRpid();
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/wxOrderComplex/").append(rpid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doPost(requstCreateURL, req);
		logInfo("%s %s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		if("86001106".equals(respMessage.getStr(HFBusiDict.RETCODE))){
			//1311:订单已经存在
			exchangeResult(responseMsg,respMessage,"1311");
		}else{
			//1304:下单失败
			exchangeResult(responseMsg,respMessage,"1304");
		}
		logInfo("CreateWxOrder Result[RetCode]:%s:%s",responseMsg.getRetCode(),responseMsg.getRetCodeBussi());
		return responseMsg;
	}
	
	private static long C = 0L;
	public static synchronized int makeInt4() {
		return (int) (C++ % 2147483647L + 1L);
	}


	public MpspMessage queryWxOrder(MpspMessage message) {
		Map<String,String> req = new HashMap<String,String>();
		String IMEI =  message.getStr(DataDict.WX_REQ_IMEI);
		String IMSI =  message.getStr(DataDict.WX_REQ_IMSI);
		String ICCID =  message.getStr(DataDict.WX_REQ_ICCID);
		String orderdate = message.getStr(DataDict.MER_REQ_MERDATE);
		String orderId = message.getStr(DataDict.MER_REQ_ORDERID);
		String merId = message.getStr(DataDict.MER_REQ_MERID);

		req.put(DataDict.WX_REQ_IMEI, IMEI);
		req.put(DataDict.WX_REQ_IMSI, IMSI); 
		req.put(DataDict.WX_REQ_ICCID, ICCID); 
		req.put(HFBusiDict.ORDERDATE,orderdate);
		req.put(HFBusiDict.ORDERID,orderId);
		req.put(HFBusiDict.MERID,merId);
		req.put("queryForKJZF", "true"); 
		String id = ObjectUtil.trim(message.get("orderId"))+"-"+ObjectUtil.trim(message.get("merDate"))+"-"+ObjectUtil.trim(message.get("merId"));
		String rpid = getRpid();
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/wxorder/").append(rpid).append("/").append(id).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requstCreateURL, req);
		logInfo("%s %s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}

	public MpspMessage getGoodsBank(String merid, String goodsid) {
		MpspMessage responseMsg = new MpspMessage();
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.MERID,merid);
		map.put(HFBusiDict.GOODSID,goodsid);
		String rpid = getRpid();
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/goodsbank/").append(rpid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requstCreateURL, map);
		logInfo("%s %s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		logInfo("GetGoodsBank Result[RetCode]:%s:%s",respMessage.getRetCode(),respMessage.getRetCodeBussi());
		return respMessage;
	}


	public MpspMessage addWxUser(MpspMessage message) {
		Map<String,String> req = new HashMap<String,String>();
		String IMEI =  message.getStr(HFBusiDict.IMEI);
		String simId =  message.getStr("simId");
		if(IMEI==null||"".equals(IMEI)){
			if(simId==null||"".equals(simId)){
				IMEI="10000000";
			}else{
				IMEI=simId;
			}
		}
		req.put(HFBusiDict.IMEI,IMEI);
		req.put(HFBusiDict.PLATTYPE, message.getStr(HFBusiDict.PLATTYPE));
		req.put(HFBusiDict.CLIENTVERSION, message.getStr(HFBusiDict.CLIENTVERSION));
		req.put(HFBusiDict.MOBILEOS, message.getStr(HFBusiDict.MOBILEOS));
		req.put(HFBusiDict.MODEL, message.getStr(HFBusiDict.MODEL));
		req.put(HFBusiDict.SOURCEMER, message.getStr(DataDict.MER_REQ_MERID));
		req.put(HFBusiDict.CHNLID, message.getStr(HFBusiDict.CHNLID));
		
		String rpid = getRpid();
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/wxclientuser/").append(rpid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doPost(requstCreateURL, req);
		logInfo("WxUserAdd Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}
	
	
	/** ********************************************
	 * method name   : getSmsRandomKey 
	 * modified      : panxingwu ,  2012-12-5
	 * description   : 获取动态验证吗
	 * ********************************************/     
	public MpspMessage getSmsRandomKey(String mobileid) {
		String rpid = getRpid();
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.MOBILEID,mobileid);
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/hfSmsRandomKeyRest/").append(rpid).append("/").append(mobileid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requstCreateURL, map);
		logInfo("wxSmsRandomKey Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}
	
	
	/** ********************************************
	 * method name   : checkRandomKey 
	 * modified      : panxingwu ,  2012-12-5
	 * description   : 动态码验证
	 * ********************************************/     
	public MpspMessage checkRandomKey(MpspMessage message) {
		String rpid = getRpid();
		String mobileid = message.getStr(DataDict.MER_REQ_MOBILEID);
		String randomkey = message.getStr(HFBusiDict.RANDOMKEY);
		String monthSign = message.getStr(HFBusiDict.MONTHSIGN);
		String transMonth = message.getStr(HFBusiDict.TRANSMONTH);
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.MOBILEID,mobileid);
		map.put(HFBusiDict.MONTHSIGN, monthSign);
		map.put(HFBusiDict.TRANSMONTH, transMonth);
		map.put(HFBusiDict.RANDOMKEY, randomkey);
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/hfSmsRandomKeyRest/").append(rpid).append("/").append(mobileid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doPost(requstCreateURL, map);
		logInfo("wxSmsRandomKeyCheck Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}


	
	/** ********************************************
	 * method name   : getHistoryTrans 
	 * modified      : panxingwu ,  2012-12-5
	 * description   : 查询交易记录
	 * ********************************************/     
	public MpspMessage getHistoryTrans(RequestMsg requestMsg) {
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		String transMonth = requestMsg.getStr(HFBusiDict.TRANSMONTH);
		String monthsign = requestMsg.getStr(HFBusiDict.MONTHSIGN);
		String rpid = getRpid();
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.MOBILEID, mobileid);
		map.put(HFBusiDict.TRANSMONTH, transMonth);
		map.put(HFBusiDict.MONTHSIGN, monthsign);
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/hfWxTransRecordRest/").append(rpid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requstCreateURL, map);
		logInfo("getHistoryTrans Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}


	public MpspMessage createOrder(Map<String, String> reqMap) {
		String rpid = getRpid();
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/hforder/common/").append(rpid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doPost(requstCreateURL, reqMap);
		logInfo("OrderCreate Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}

	public MpspMessage updateOrder(Map<String, String> reqMap) {
		String rpid = getRpid();
		String merid = reqMap.get(HFBusiDict.MERID);
		String orderid = reqMap.get(HFBusiDict.ORDERID);
		String orderdate = reqMap.get(HFBusiDict.ORDERDATE);
		String id = merid+"-"+orderid+"-"+orderdate;
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/hforder/mer/").append(rpid).append("/").append(id).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doPost(requstCreateURL, reqMap);
		logInfo("UpdateOrder Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return null;
	}
	/** ********************************************
	 * method name   : queryWXPlatOrder 
	 * description   : 查询R5下订单是否成功
	 * @return       : MpspMessage
	 * @param        : @param platOrderId
	 * @param        : @return
	 * modified      : LiuJiLong ,  2012-11-16 上午11:42:14
	 * @see          : 
	 * ********************************************/   
	public MpspMessage queryWXPlatOrder(String platOrderId) {
		//调用资源层查询服务
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.PLATORDERID, platOrderId);
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		String requstURL = new StringBuffer(getRestSrvPath()).append("/platorder/").append(rpid).append("/" + platOrderId).append(".xml").toString();
		logInfo("WXPlatOrder Query RequstURL %s", requstURL);
		MpspMessage responseMsgGlobal = restConnPool.doGet(requstURL, map);
		logInfo("WXPlatOrder Query Result[RetCode]%s:%s", responseMsgGlobal.getRetCode(), responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}
	
	/** ********************************************
	 * method name   : addWXPlatOrder 
	 * description   : R5下平台（临时）订单
	 * @return       : MpspMessage
	 * @param        : @param platOrderId
	 * @param        : @return
	 * modified      : LiuJiLong ,  2012-11-16 上午11:42:14
	 * @see          : 
	 * ********************************************/      
	public MpspMessage saveWXPlatOrder(MpspMessage requestMsg) {
		//调用资源层商户插入服务
		Map<String, String> map = new HashMap<String, String>();
		String merId = requestMsg.getStr(DataDict.MER_REQ_MERID);
		String goodsId = requestMsg.getStr(DataDict.MER_REQ_GOODSID);
		String expand = requestMsg.getStr(DataDict.MER_REQ_EXPAND);
		String businessType = "0205";//02代表无线业务，05代表R5
		String versionCode = requestMsg.getStr("versionCode");
		String imei = requestMsg.getStr("IMEI");
		map.put(HFBusiDict.MERID, merId);
		map.put(HFBusiDict.GOODSID, goodsId);
		if(imei.length()<=15){
			map.put(HFBusiDict.IMEI, imei);
		}
		map.put(HFBusiDict.CLIENTVERSION, versionCode);
		map.put(HFBusiDict.BUSINESSTYPE, businessType);
		//处理goodsdesc
		String merPrive = messageService.getSystemParam("wxr5.goodsdesc." + merId);
		merPrive = merPrive.replaceFirst("merid", merId);
		merPrive = merPrive.replaceFirst("goodsid", goodsId);
		merPrive = merPrive.replaceFirst("expand", expand);
		map.put(HFBusiDict.GOODSDESC, merPrive);
		
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		String requstURL = new StringBuffer(getRestSrvPath()).append("/platorder/").append(rpid).append(".xml").toString();
		logInfo("WXPlatOrder Query RequstURL %s", requstURL);
		MpspMessage responseMsgGlobal = restConnPool.doPost(requstURL, map);
		logInfo("WXPlatOrder Query Result[RetCode]%s:%s", responseMsgGlobal.getRetCode(), responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}


	public MpspMessage getRandomVerifyTimes(String mobileid) {
		String rpid = getRpid();
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.MOBILEID,mobileid);
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/hfVerifyInfoRest/").append(rpid).append("/").append(mobileid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requstCreateURL, map);
		logInfo("wxSmsRandomVerifyTimes Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}


	public MpspMessage queryMobileidInf(String mobileid) {
		Map<String,String> map = new HashMap<String,String>();
		String rpid = getRpid();
		map.put(HFBusiDict.MOBILEID, mobileid.substring(0, 7));
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/seginf/").append(rpid).append("/").append(mobileid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requstCreateURL, map);
		logInfo("QueryMobileidInf Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}


	public MpspMessage getWxUserSeg(RequestMsg requestMsg) {
		String rpid = getRpid();
		String IMEI =  requestMsg.getStr(HFBusiDict.IMEI);
		String platType = requestMsg.getStr(HFBusiDict.PLATTYPE);
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.IMEI,IMEI);
		map.put(HFBusiDict.PLATTYPE, platType);
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/wxuserinf/").append(rpid).append("/").append(IMEI).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requstCreateURL, map);
		logInfo("QueryMobileidInf Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}

	public MpspMessage checkChnlSign(String chnlid,String signStr,String unSignStr) {
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.CHANNELID, chnlid);
		map.put(HFBusiDict.SIGNSTR, signStr);
		map.put(HFBusiDict.UNSIGNSTR, unSignStr);
		String requestURL = new StringBuffer(getRestSrvPath()).append("/checkChannelRest/").append(rpid).append("/").append(chnlid).append(".xml").toString();
		logInfo("调用资源层进行渠道验签,参数为%s,请求URL：%s", map,requestURL);
		MpspMessage respMessage = restConnPool.doPost(requestURL, map);
		logInfo("ChannelCheckSign Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}


	public MpspMessage addWxUserReplyInf(RequestMsg requestMsg) {
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		String IMEI = requestMsg.getStr(DataDict.WX_REQ_IMEI);
		String contacInfo = requestMsg.getStr(DataDict.WX_ERQ_CONTACTINFO);
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
		String replyTime = sf.format(new Date());
//		String replyTime = requestMsg.getStr(DataDict.WX_ERQ_REPLYTIME);
		String replyInfo = requestMsg.getStr(DataDict.WX_ERQ_REPLYINFO);
		String clientName = requestMsg.getStr(DataDict.WX_REQ_CLIENTNAME);
		String platType = requestMsg.getStr(DataDict.WX_REQ_PLATTYPE);
		String versionCode = requestMsg.getStr(DataDict.WX_REQ_VERSIONCODE);
		String replyType = requestMsg.getStr("replyType");
		
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.IMEI, IMEI);
		map.put(HFBusiDict.CONTACINFO, contacInfo);
		map.put(HFBusiDict.REPLYTIME, replyTime);
		map.put(HFBusiDict.REPLYINFO, replyInfo);
		map.put(HFBusiDict.PLATTYPE, platType);
		map.put("clientName", clientName);
		map.put(HFBusiDict.VERSION, versionCode);
		map.put("replyType", replyType);
		
		String requestURL = new StringBuffer(getRestSrvPath()).append("/wxUserReplyRest/").append(rpid).append(".xml").toString();
		logInfo("调用资源层进行用户反馈信息保存,参数为%s,请求URL：%s", map,requestURL);
		MpspMessage respMessage = restConnPool.doPost(requestURL, map);
		return respMessage;
	}


	public MpspMessage queryClientConf(String clientName, String clientType) {
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		Map<String,String> map = new HashMap<String,String>();
		map.put("clientName", clientName);
		map.put("clientType", clientType);
		String requestURL = new StringBuffer(getRestSrvPath()).append("/wxClientConfig/").append(rpid).append("/").append(clientName+"-"+clientType).append(".xml").toString();
		logInfo("调用资源层查询无线客户端配置信息:%s,%s", map,requestURL);
		MpspMessage respMessage = restConnPool.doGet(requestURL, map);
		return respMessage;
	}
	public MpspMessage queryQDOrder(RequestMsg requestMsg) {
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		String chnlId = requestMsg.getStr(DataDict.MER_REQ_CHNLID);
		String chnlOrderId = requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID);
		String chnlDate = requestMsg.getStr(DataDict.MER_REQ_CHNLDATE);
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.CHANNELID, chnlId);
		map.put(HFBusiDict.CHANNELORDERID, chnlOrderId);
		map.put(HFBusiDict.CHANNELDATE, chnlDate);

		String requestURL = new StringBuffer(getRestSrvPath()).append("/channelOrder/").append(rpid).append("/").append(chnlId).append("-").append(chnlOrderId).append("-").append(chnlDate).append(".xml").toString();
		logInfo("查询渠道订单,参数为%s,请求URL：%s", map,requestURL);
		MpspMessage respMessage = restConnPool.doGet(requestURL, map);
		logInfo("ChannelCheckSign Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}

	public MpspMessage getWxOrderVerifyCode(RequestMsg requestMsg) {
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		String orderid = requestMsg.getStr(DataDict.MER_REQ_ORDERID);
		String orderdate = requestMsg.getStr(DataDict.MER_REQ_MERDATE);
		String merid = requestMsg.getStr(DataDict.MER_REQ_MERID);
		String mobileId = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.ORDERID, orderid);
		map.put(HFBusiDict.ORDERDATE, orderdate);
		map.put(HFBusiDict.MERID, merid);
		map.put(HFBusiDict.MOBILEID, mobileId);
		String requestURL = new StringBuffer(getRestSrvPath()).append("/orderVerifyRest/").append(rpid).append(".xml").toString();
		logInfo("调用资源层获取验证码,参数为%s,请求URL：%s", map,requestURL);
		MpspMessage respMessage = restConnPool.doPost(requestURL, map);
		logInfo("Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}


	public MpspMessage checkVerifyCode(RequestMsg requestMsg) {
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		String orderid = requestMsg.getStr(DataDict.MER_REQ_ORDERID);
		String orderdate = requestMsg.getStr(DataDict.MER_REQ_MERDATE);
		String merid = requestMsg.getStr(DataDict.MER_REQ_MERID);
		String verifyCode = requestMsg.getStr(DataDict.MER_REQ_VERIFYCODE);//验证码
		String mobileId = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.ORDERID, orderid);
		map.put(HFBusiDict.ORDERDATE, orderdate);
		map.put(HFBusiDict.MERID, merid);
		map.put(HFBusiDict.MOBILEID, mobileId);
		map.put(HFBusiDict.RANDOMKEY, verifyCode);
		String requestURL = new StringBuffer(getRestSrvPath()).append("/orderVerifyRest/").append(rpid).append("/").append(orderid).append("-").append(orderdate).append("-").append(merid).append(".xml").toString();
		logInfo("调用资源层校验验证码,参数为%s,请求URL：%s", map,requestURL);
		MpspMessage respMessage = restConnPool.doPost(requestURL, map);
		logInfo("Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}

  /**
   * 12580客户端查询手机号绑定关系
   */
	public MpspMessage queryWxSDKBind(String IMEI, String IMSI) {
		// TODO 确认实际资源名称和参数
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.IMEI, IMEI);
		map.put(HFBusiDict.IMSI, IMSI);		
		String requestURL = new StringBuffer(getRestSrvPath()).append("/wxClientBind/").append(rpid).append("/").append(IMSI).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requestURL, map);
		logInfo("%s %s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}
	/** 
	 * 查询海南包月订购关系
	 */
	public MpspMessage queryUserMonthlyServiceInfo(String mobileid , String merId) {
		MpspMessage responseMsgGlobal = new MpspMessage();
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.MOBILEID, mobileid);
		map.put(HFBusiDict.MERID, merId);
		map.put(HFBusiDict.STATE, "2");
		long dt = DateTimeUtil.currentDateTime();
		String reqtime = DateTimeUtil.getDateString(dt);
		String reqdate = DateTimeUtil.getTimeString(dt);
		map.put(HFBusiDict.VALIDTIME, (reqtime+reqdate).substring(2, 14));
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		String requstURL = new StringBuffer(getRestSrvPath()).append("/HfHncpUserInfoRest/").append(rpid).append("/").append(mobileid).append(".xml").toString();
		logInfo("HncpUserInfo Query RequstURL %s",requstURL);
		MpspMessage respMessage = restConnPool.doGet(requstURL, map);
		exchangeResult(responseMsgGlobal,respMessage, "1408");
		logInfo("HncpUserInfo Query Result[RetCode]%s:%s",responseMsgGlobal.getRetCode(),responseMsgGlobal.getRetCodeBussi());
		return responseMsgGlobal;
	}

   /**
    * 同步更新SDK 公钥
    */
    public MpspMessage updateSDKpbKey(RequestMsg requestMsg) {

	// TODO 确认实际资源名称和参数
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		Map<String,String> map = new HashMap<String,String>();
		String IMEI=requestMsg.getStr(HFBusiDict.IMEI);
		String IMSI=requestMsg.getStr(HFBusiDict.IMSI);
		map.put(HFBusiDict.IMEI, IMEI);
		map.put(HFBusiDict.IMSI,IMSI);
		map.put(HFBusiDict.PUBLICKEY, requestMsg.getStr(HFBusiDict.PUBLICKEY));
		String requestURL = new StringBuffer(getRestSrvPath()).append("/wxBindComplex/").append(rpid).append("/").append(IMSI).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doPost(requestURL, map);
		logInfo("%s %s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		
	    return respMessage;
    }
	public MpspMessage recordClientUser(RequestMsg message) {
		Map<String,String> req = new HashMap<String,String>();
		String iccid = message.getStr(DataDict.WX_REQ_ICCID);
		String areaCode = "";
		if(iccid!=null&&iccid.length()>=10){
			areaCode = iccid.substring(8,10);
		}
		String cid = StringUtil.trim(message.getStr(DataDict.WX_REQ_CLIENTID));
		if (cid.contains("null")) {
			cid = "nullError";
		}
		req.put(HFBusiDict.CLIENTID,cid);//客户端ID
		req.put(HFBusiDict.IMSI, message.getStr(DataDict.WX_REQ_IMSI));
		req.put(HFBusiDict.IMEI,message.getStr(HFBusiDict.IMEI));
		req.put(HFBusiDict.PLATTYPE, message.getStr(HFBusiDict.PLATTYPE));
		req.put(HFBusiDict.CLIENTVERSION, message.getStr(DataDict.WX_REQ_VERSIONNAME));
		req.put(HFBusiDict.MOBILEOS, message.getStr(HFBusiDict.MOBILEOS));
		req.put(HFBusiDict.MODEL, message.getStr(HFBusiDict.MODEL));
		req.put(HFBusiDict.CHANNELID, message.getStr(HFBusiDict.CHNLID));
		req.put(HFBusiDict.AREACODE,areaCode);
//		req.put(HFBusiDict.SOURCEMER, message.getStr(DataDict.MER_REQ_MERID));
		
		String rpid = getRpid();
		String requstCreateURL = new StringBuffer(getRestSrvPath()).append("/hfClientUserComplex/").append(rpid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doPost(requstCreateURL, req);
		logInfo("RecodClientUser Result[RetCode]:%s:%s", respMessage.getRetCode(),respMessage.getStr(HFBusiDict.RETMSG));
		return respMessage;
	}


	public MpspMessage getClientUser(RequestMsg requestMsg) {
		Map<String,String> req = new HashMap<String,String>();
		String rpid = getRpid();
		String clientId = requestMsg.getStr(DataDict.WX_REQ_CLIENTID);
		req.put(HFBusiDict.CLIENTID,clientId);
		req.put(HFBusiDict.PLATTYPE, requestMsg.getStr(HFBusiDict.PLATTYPE));	
		String requestURL = new StringBuffer(getRestSrvPath()).append("/clientUser/").append(rpid).append("/").append(clientId).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requestURL, req);
		return respMessage;
	}


	public MpspMessage getMwUserLtd(RequestMsg requestMsg) {

		Map<String,String> req = new HashMap<String,String>();
		String rpid = getRpid();
		String mobileId = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		req.put(HFBusiDict.MOBILEID,mobileId);	
		String requestURL = new StringBuffer(getRestSrvPath()).append("/mwuserltd/").append(rpid).append("/").append(mobileId).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requestURL, req);
		return respMessage;
	}


	public MpspMessage getHfUserLtd(RequestMsg requestMsg) {
		Map<String,String> req = new HashMap<String,String>();
		String rpid = getRpid();
		String mobileId = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		req.put(HFBusiDict.MOBILEID,mobileId);	
		String requestURL = new StringBuffer(getRestSrvPath()).append("/hfQueryUserLtd/").append(rpid).append("/").append(mobileId).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requestURL, req);
		return respMessage;
	}


	public MpspMessage queryChnlGoodInf(String channlid,String merid, String goodsid) {
		// TODO Auto-generated method stub
		Map<String,String> req = new HashMap<String,String>();
		String rpid = getRpid();
		req.put(HFBusiDict.MERID, merid);
		req.put(HFBusiDict.GOODSID, goodsid);
		req.put(HFBusiDict.CHANNELID, channlid);
		String requestURL = new StringBuffer(getRestSrvPath()).append("/HfChannelGoodInfRest/").append(rpid).append("/").append(channlid).append("-").append(merid).append("-").append(goodsid).append(".xml").toString();
		MpspMessage respMessage = restConnPool.doGet(requestURL, req);
		return respMessage;
	}
}
