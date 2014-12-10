package com.umpay.hfweb.action.order;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
/**
 * ******************  类说明  *********************
 * class       :  WapRequestAction
 * @author     :  zhaoyan 
 * description :  WAP版下单-WAP页面请求处理类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class WapRequestAction extends BaseAbstractAction{
	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap) {
		//1-组装请求数据，进行数据校验
		RequestMsg reqMap = new RequestMsg();
		reqMap.putAllParam(HttpUtil.parseRequestParam(request));
		reqMap.setFunCode(this.getFunCode());
		reqMap.setRpid(ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID)));
		String wholeRetUrl = "";
		MpspMessage checkParamResp = checkService.doCheck(reqMap);
		if(!checkParamResp.isRetCode0000()){
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s", checkParamResp.getRetCode(), messageService.getMessageDetail(checkParamResp.getRetCode()));
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(checkParamResp.getRetCode()));
			modelMap.put(DataDict.RET_CODE, checkParamResp.getRetCode());
			if(!ObjectUtil.isEmpty(reqMap.getStr(DataDict.MER_REQ_RETURL))){
				wholeRetUrl = reqMap.getStr(DataDict.MER_REQ_RETURL);
			}
			request.getSession().setAttribute("errorRetUrl", wholeRetUrl);
			return super.ERROR_WAP;
		}
		logInfo("ParamCheck Result Success[RetCode]:0000:请求参数校验通过");
		PageOrderCmd cmd = new PageOrderCmd(reqMap.getWrappedMap());
		//根据输入参数组织返回信息
		OrderParam oparam = getTomerParam(cmd);
		wholeRetUrl = genWholeRetUrl(oparam);
		request.getSession().setAttribute("errorRetUrl", wholeRetUrl);
		//2-商户验签
		boolean isCheckSign = false;
		MpspMessage checkSignResp = restService.checkSign(cmd.getMerId(), cmd.getPlainText(), cmd.getSign());
		if(!checkSignResp.isRetCode0000()){
			logInfo("SignCheck Result Failed[RetCode]:%s:%s", checkSignResp.getRetCode(), "验证商户签名失败");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(checkSignResp.getRetCode()));
			modelMap.put(DataDict.RET_CODE, checkSignResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, checkSignResp.getRetCodeBussi());
			return super.ERROR_WAP;
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
		
		
		
		
		//2-2 商户下单时间校验
		if(!DateUtil.verifyOrderDate(cmd.getMerDate())){
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1310"));
			modelMap.put(DataDict.RET_CODE, "1310");
			logInfo("VerifyOrderDate Result Failed[RetCode]:%s:%s", "1310","商户下单时间校验未通过");
			return super.ERROR_WAP;
		}
		logInfo("VerifyOrderDate Result Success[RetCode]:0000:商户下单时间校验通过");
		isCheckSign = true;
		//3-交易鉴权
		boolean isCheckTrade = false;
		MpspMessage checkTradeResp = restService.checkTrade(cmd.getMobileId(), cmd.getMerId(), cmd.getGoodsId());
		if(!checkTradeResp.isRetCode0000()){
			logInfo("checkTrade Result Failed[RetCode]:%s:%s", checkTradeResp.getRetCode(), "交易鉴权失败");
			String errorMessage = messageService.getMessageDetail(checkTradeResp.getRetCodeBussi());
			if("".equals(errorMessage)){//modify by zhuoyangyang 20140430 交易鉴权失败返回码对应信息显示在页面上
				errorMessage = messageService.getMessageDetail(checkTradeResp.getRetCode());
			}
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, errorMessage);
			modelMap.put(DataDict.RET_CODE, checkTradeResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, checkTradeResp.getRetCodeBussi());
			return super.ERROR_WAP;
		}
		logInfo("checkTrade Result Success[RetCode]0000:交易鉴权通过");
		isCheckTrade = true;
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-19
		modelMap.put(HFBusiDict.PROVCODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.PROVCODE)));
		modelMap.put(HFBusiDict.AREACODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.AREACODE)));
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-19
		//4-确认可支付银行
		String bankId = (String)checkTradeResp.get(HFBusiDict.BANKID);
		if(ObjectUtil.isEmpty(bankId)){
			//无可支付银行的返回码 <您暂时不能使用支付服务>
			logInfo("BankCheck Result Failed[RetCode]:%s:%s","1303","交易鉴权通过，但无可支付银行");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1303"));
			modelMap.put(DataDict.RET_CODE, "1303");
			return super.ERROR_WAP;
		}
		logInfo("BankCheck Result Success[RetCode]:0000:可支付银行确认通过---bankId[ "+ bankId +" ]");
		//TODO begin 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-19
		modelMap.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-19
		//5-组装session数据
		request.getSession().setAttribute(DataDict.FUNCODE_ORDER_PARAM, cmd);
		request.getSession().setAttribute("tradeMessage", checkTradeResp);
		request.getSession().setAttribute(DataDict.CHECK_SIGN_FLAG, isCheckSign);
		request.getSession().setAttribute(DataDict.CHECK_TRADE_FLAG, isCheckTrade);
		OrderParam orderParam = getPageParam(checkTradeResp, cmd, bankId);
		modelMap.put("order", orderParam);
		modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		//6-根据bankId判断跳转地址（小额支付，全网支付）
		String retView = "";
		if(bankId.startsWith("XE")){
			retView = "order/wap_confirmpay";
		}else if(bankId.startsWith("MW")){
			retView = "order/wap_mw_confirmpay";
		}
		return retView;
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
	private OrderParam getPageParam(MpspMessage checkTradeResp,
			PageOrderCmd cmd, String bankId) {
		OrderParam orderParam = new OrderParam();
		orderParam.setOrderId(cmd.getOrderId());
		orderParam.setMerDate(cmd.getMerDate());
		orderParam.setMerName((String)checkTradeResp.get(HFBusiDict.MERNAME));
		orderParam.setAmount(cmd.getAmount());
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar(cmd.getAmount()));
		orderParam.setServType(String.valueOf(checkTradeResp.get(HFBusiDict.SERVTYPE)));
		orderParam.setServMonth(String.valueOf(checkTradeResp.get(HFBusiDict.SERVMONTH)));
		orderParam.setCusPhone((String)checkTradeResp.get(HFBusiDict.CUSPHONE));
		orderParam.setBankId(bankId);
		orderParam.setMobileId(cmd.getMobileId());
		orderParam.setGoodsId(cmd.getGoodsId());
		orderParam.setGoodsName((String)checkTradeResp.get(HFBusiDict.GOODSNAME));
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
		//页面下单的功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_WAPPAGEXD;
	}
}
