package com.umpay.hfweb.action.wap;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.HFMerReferCache;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.MerReferUtil;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;


/** ******************  类说明  *********************
 * class       :  WapVerifyCodeRequestAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  接收来自商户页面的请求，做初步验证以后返回到通信账户wap验证码获取页面
 * 				    （此页面包含商品名称，金额，手机号输入框）
 * @see        :                        
 * ************************************************/   
public class WapVerifyCodeRequestAction extends BaseAbstractAction{
	private final String WAP_XE_REQUEST = "wap/wap_verify_xe_request";
	private final String WAP_MW_REQUEST = "wap/wap_verify_mw_request";
	private final String WAP_XE_PAYRESULT = "wap/wap_verify_xe_payresult";
	private final String WAP_MW_PAYRESULT = "wap/wap_verify_mw_payresult";
	
	protected String processBussiness(HttpServletRequest request,HttpServletResponse response,Map<String,Object> modelMap) {
		//1-重置session，组装请求数据，进行数据校验
		HttpSession session = request.getSession();
		session.removeAttribute(DataDict.SEND_SMS_CAPTCHAINFO);
		session.removeAttribute("tradeMessage");
		session.removeAttribute(DataDict.MER_REQ_MOBILEID);
		session.removeAttribute(DataDict.FUNCODE_ORDER_PARAM);
		
		RequestMsg reqMap = new RequestMsg();
		reqMap.putAllParam(HttpUtil.parseRequestParam(request));
		reqMap.setFunCode(this.getFunCode());
		reqMap.setRpid(ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID)));
		String wholeRetUrl =  ObjectUtil.trim(reqMap.getStr(DataDict.MER_REQ_RETURL));;
		session.setAttribute("wholeRetUrl", wholeRetUrl);
		modelMap.put("wholeRetUrl",wholeRetUrl);
		
		MpspMessage checkParamResp = checkService.doCheck(reqMap);
		if(!checkParamResp.isRetCode0000()){
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s", checkParamResp.getRetCode(), messageService.getMessageDetail(checkParamResp.getRetCode()));
			modelMap.put(DataDict.RET_MSG, messageService.getMessageDetail(checkParamResp.getRetCode()));
			modelMap.put(DataDict.RET_CODE, checkParamResp.getRetCode());
			return WAP_MW_PAYRESULT;
		}
		logInfo("ParamCheck Result Success[RetCode]:0000:请求参数校验通过");
		
		//-检查session
//		HttpSession session = request.getSession();
//		PageOrderCmd cmd = (PageOrderCmd)session.getAttribute(DataDict.FUNCODE_ORDER_PARAM);
//		String orderIdReq=(String)reqMap.get("orderId");
//		if(cmd!=null && !orderIdReq.equals(cmd.getOrderId())){
//			//session中存在商户请求信息，则提示先支付完上一笔订单或等待15分钟
//			logInfo("WAP Request has existed in the Session:%s","该session中已存在请求信息");
//			String retMsg=messageService.getSystemParam("Captcha.PayLastOrder.Msg");
//			modelMap.put(DataDict.RET_MSG, retMsg);
//			return WAP_DIRECT_PAYRESULT;
//		}
		
		PageOrderCmd cmd = new PageOrderCmd(reqMap.getWrappedMap());
		//根据输入参数组织返回信息
		OrderParam oparam = getTomerParam(cmd);
		wholeRetUrl = genWholeRetUrl(oparam);
		request.getSession().setAttribute("wholeRetUrl", wholeRetUrl);
		modelMap.put("wholeRetUrl",wholeRetUrl);
		//3-商户验签
		boolean isCheckSign = false;
		MpspMessage checkSignResp = restService.checkSign(cmd.getMerId(), cmd.getPlainText(), cmd.getSign());
		if(!checkSignResp.isRetCode0000()){
			logInfo("SignCheck Result Failed[RetCode]:%s:%s", checkSignResp.getRetCode(), "验证商户签名失败");
			modelMap.put(DataDict.RET_MSG, messageService.getMessageDetail(checkSignResp.getRetCode()));
			modelMap.put(DataDict.RET_CODE, checkSignResp.getRetCode());
			return WAP_MW_PAYRESULT;
		}
		logInfo("SignCheck Result Success[RetCode]0000:商户签名验证通过");
		
		
		
		/**
		 * 根据备案信息校验url
		 * 1、获取到refer信息
		 * 2、获取到缓存的url，如果缓存中没有，调用资源层接口查找
		 * 3、记录过滤日志
		 */
		
		try {
			// 1、获取到refer信息
			String refer = ObjectUtil.trim(request.getHeader("Referer"));
			logInfo("refer[%s]", refer);
			// 2、获取到缓存的url，如果缓存中没有，调用资源层接口查找
			HFMerReferCache hfMerReferCache = (HFMerReferCache) AbstractCacheFactory.getInstance().getCacheClient(HFMerReferCache.CACHE_NAME);
			
			String merid = ObjectUtil.trim(cmd.getMerId());
			String goodsid = ObjectUtil.trim(cmd.getGoodsId());
			logInfo("merid[%s]-goodsid[%s]", merid, goodsid);
			
			String cacheKey = merid + "-" + goodsid;
			
			@SuppressWarnings("unchecked")
			List<String> referCacheList = (List<String>) hfMerReferCache.getMerReferFromCache(cacheKey);
			// 如果缓存中不存在
			if(referCacheList == null || referCacheList.size() == 0){
				MpspMessage rtnMerReferInf = this.restService.queryMerReferInf(merid, goodsid);
				// 当调用资源层接口出现异常时
				if(!rtnMerReferInf.isRetCode0000()){
					logInfo("queryMerReferInf Result Failed[RetCode]:%s:%s", rtnMerReferInf.getRetCode(), "获取商户渠道报备信息失败");
					referCacheList = null;
				}
				else{
					@SuppressWarnings("unchecked")
					// 从资源层接口返回数据中取出需要的
					List<Map<String, Object>> rtnReferList =  (List<Map<String, Object>>) rtnMerReferInf.get(MerReferUtil.REST_RTN_REFER_KEY);
					referCacheList = MerReferUtil.getReferListFromMap(rtnReferList);
					// 放入缓存
					hfMerReferCache.put(cacheKey, referCacheList);
				}
			}
			
			// 当有报备信息存在时   进行请求url的过滤   记录日志
			/**
			 * 过滤码说明
			 * 	1、合法  (已配置并匹配)
			 * 	0、非法	(未配置)
			 * 	-1、没有配置
			 */
			String rtnCode = "-1"; // -1 说明没有配置报备信息
			if(referCacheList != null){
				rtnCode = MerReferUtil.filterTheRefer(refer, referCacheList);
			}
			
			/**
			 *  记录refer验证结果日志
			 *  	日志格式：date,time,funcode,rtnCode,merid,googsid,goodsName,orderid,mobileid,alarmtype,refer
			 *  	格式字段部分说明：
			 *  		funcode：YMXD 代表页面下单请求 , WAPXD代表wap方式下单
			 *  		alarmtype：1代表有手机号请求的url  2代表无手机号请求的url   3代表爬虫关键字   
			 */
			String logInfo = MerReferUtil.logFilterRefer(cmd, refer, rtnCode, this.getFunCode(), MerReferUtil.ALARM_URL_HAS_MOBILE);
			logInfo("refer过滤结果：%s", logInfo);
		} catch (Throwable e) {
			e.printStackTrace();
			logInfo("商户渠道报备信息校验失败：%s", e.getMessage());
		}
		
		
		//4 商户下单时间校验
		if(!DateUtil.verifyOrderDateStrict(cmd.getMerDate())){
			modelMap.put(DataDict.RET_MSG, messageService.getMessageDetail("1310"));
			modelMap.put(DataDict.RET_CODE, "1310");
			logInfo("VerifyOrderDate Result Failed[RetCode]:%s:%s", "1310","商户下单时间校验未通过");
			return WAP_MW_PAYRESULT;
		}
		logInfo("VerifyOrderDate Result Success[RetCode]:0000:商户下单时间校验通过");
		isCheckSign = true;
		
		//begin 获取bankid 用于判断全网还是小额 modified by lizhen 2014-05-21
		//5 有手机号交易鉴权，无手机号查询商品信息
		boolean isXEBank = false;
		OrderParam orderParam=getPageParam(cmd);
		if(cmd.getMobileId()!=null){
			MpspMessage checkTradeResp = restService.checkTrade(cmd.getMobileId(), cmd.getMerId(), cmd.getGoodsId());
			String bankId = ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.BANKID));
			orderParam = getPageParam(checkTradeResp, cmd);
			if (bankId != null && bankId.startsWith("XE")) {
				isXEBank = true;
			}
		}else{
			//4-1-不带手机号的商户请求，获取商户商品信息
			MpspMessage checkMerGoodsResp = restService.queryMerGoodsInfo(cmd.getMerId(), cmd.getGoodsId());
			if(!checkMerGoodsResp.isRetCode0000()){
				logInfo("queryMerGoodsInfo Result Failed[RetCode]:%s:%s", checkMerGoodsResp.getRetCode(), "获取商户商品信息失败");
				modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(checkMerGoodsResp.getRetCode()));
				modelMap.put(DataDict.RET_CODE, checkMerGoodsResp.getRetCode());
				modelMap.put(DataDict.RET_CODE_BUSSI, checkMerGoodsResp.getRetCodeBussi());
				return WAP_MW_PAYRESULT;
			}
			logInfo("queryMerGoodsInfo Result Success[RetCode]0000:获取商户商品信息通过");
			orderParam = this.getPageParam(checkMerGoodsResp, cmd);
		}
		request.getSession().setAttribute("orderParam", orderParam);
		//end 获取bankid 用于判断全网还是小额 modified by lizhen 2014-05-21
		
		//5-组装session数据
		request.getSession().setAttribute(DataDict.FUNCODE_ORDER_PARAM, cmd);
		request.getSession().setAttribute(DataDict.CHECK_SIGN_FLAG, isCheckSign);
		
		modelMap.put("order", orderParam);
		modelMap.put(DataDict.MER_REQ_MOBILEID, cmd.getMobileId());
//		String goodsName=ObjectUtil.trim(orderParam.getGoodsName());
//		if(ObjectUtil.getStrLenthWithChinese(goodsName)>16){
//			goodsName=goodsName.substring(0,16)+"......";
//		}
//		modelMap.put("goodsName", goodsName);
		if(ObjectUtil.isEmpty(cmd.getMobileId())){
			modelMap.put(DataDict.RET_MSG, "plsInput");//请输入11位手机号
		}
		modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		
		//6-跳转地址
		return isXEBank ? WAP_XE_REQUEST : WAP_MW_REQUEST;
	}
	
	private OrderParam getPageParam(MpspMessage checkTradeResp, PageOrderCmd cmd) {
		OrderParam orderParam = new OrderParam();
		orderParam.setOrderId(cmd.getOrderId());
		orderParam.setMerDate(cmd.getMerDate());
		orderParam.setMerName((String)checkTradeResp.get(HFBusiDict.MERNAME));
		orderParam.setAmount(cmd.getAmount());
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar(cmd.getAmount()));
		orderParam.setServType(String.valueOf(checkTradeResp.get(HFBusiDict.SERVTYPE)));
		orderParam.setServMonth(String.valueOf(checkTradeResp.get(HFBusiDict.SERVMONTH)));
		orderParam.setCusPhone((String)checkTradeResp.get(HFBusiDict.CUSPHONE));
		orderParam.setBankId((String)checkTradeResp.get(HFBusiDict.BANKID));
		orderParam.setMobileId(cmd.getMobileId());
		orderParam.setGoodsId(cmd.getGoodsId());
		orderParam.setGoodsName((String)checkTradeResp.get(HFBusiDict.GOODSNAME));
		orderParam.setMerId(cmd.getMerId());
		return orderParam;
	}
	/**
	 * ********************************************
	 * method name   : getPageParam 
	 * description   : 获取下单页面参数
	 * @return       : OrderPageParam
	 * @param        : @param checkTradeResp
	 * @param        : @param cmd
	 * @param        : @param bankId
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 4, 2011 5:35:29 PM
	 * @see          : 
	 * *******************************************
	 */
	private OrderParam getPageParam(PageOrderCmd cmd) {
		OrderParam orderParam = new OrderParam();
		orderParam.setOrderId(cmd.getOrderId());
		orderParam.setMerDate(cmd.getMerDate());
		orderParam.setAmount(cmd.getAmount());
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar(cmd.getAmount()));
		orderParam.setMobileId(cmd.getMobileId());
		orderParam.setGoodsId(cmd.getGoodsId());
		orderParam.setGoodsName(cmd.getGoodsName());
		orderParam.setMerId(cmd.getMerId());
		return orderParam;
	}
	
	private OrderParam getTomerParam(PageOrderCmd param) {
		OrderParam tomerParam = new OrderParam();
		tomerParam.setGoodsId(param.getGoodsId());
		tomerParam.setMerId(param.getMerId());
		tomerParam.setOrderId(param.getOrderId());
		tomerParam.setMerDate(param.getMerDate());
		tomerParam.setAmount(param.getAmount());
		tomerParam.setAmtType(param.getAmtType());
		tomerParam.setMobileId(param.getMobileId());
		tomerParam.setPlateDate(param.getMerDate());
		tomerParam.setMerPriv(param.getMerPriv());
		tomerParam.setRetCode(DataDict.SYSTEM_ERROR_CODE);
		tomerParam.setRetUrl(param.getRetUrl());
		tomerParam.setVersion(param.getVersion());
		return tomerParam;
	}
	private String genWholeRetUrl(OrderParam param) {
		String retUrl = ObjectUtil.trim(param.getRetUrl());
		if(ObjectUtil.isEmpty(retUrl)){
			return "";
		}
		if (retUrl.indexOf("?") == -1){
			retUrl += "?";
		}else{
			retUrl += "&";
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(retUrl);
		String sign = super.platSign(param.getPlainText());
		logInfo("Info2Mer Plain Text:%s", param.getPlainText());
		logInfo("Info2Mer Sign Text:%s", sign);
		buffer.append(param.getEncodedText(sign));
		return buffer.toString();
	}
	@Override
	protected String getFunCode() {
		//WAP直接支付处理商户请求的功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_WAPYZMCL;
	}
}
