package com.umpay.hfweb.action.order;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.HFMerReferCache;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.util.CommonUtil;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.MerReferUtil;
import com.umpay.hfweb.util.MessageUtil;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  PageRequestAction
 * @author     :  zhaoyan 
 * description :  页面下单-接收商户页面请求处理类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class PageRequestAction extends BaseAbstractAction{

	private MessageSource validateSource;
	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap){
		//1-系统升级提示信息
		String notice = messageService.getSystemParam("notice");
		if (ObjectUtil.isNotEmpty(notice)) { 
			modelMap.put("notice", notice);
		}
		//2-对请求数据进行校验,接收请求数据类型转换
		RequestMsg reqMap = new RequestMsg();
		reqMap.putAllParam(HttpUtil.parseRequestParam(request));
		reqMap.setFunCode(this.getFunCode());
		reqMap.setRpid(ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID)));
		MpspMessage checkParamResp = checkService.doCheck(reqMap);
		String wholeRetUrl = "";
		if(!checkParamResp.isRetCode0000()){
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s", checkParamResp.getRetCode(), messageService.getMessageDetail(checkParamResp.getRetCode()));
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(checkParamResp.getRetCode()));
			modelMap.put(DataDict.RET_CODE, checkParamResp.getRetCode());
			if(!ObjectUtil.isEmpty(reqMap.getStr(DataDict.MER_REQ_RETURL))){
				wholeRetUrl = reqMap.getStr(DataDict.MER_REQ_RETURL);
			}
			request.getSession().setAttribute("errorRetUrl", wholeRetUrl);
			return super.ERROR_PAGE;
		}
		logInfo("ParamCheck Result Success[RetCode]:0000:请求参数校验通过");
		PageOrderCmd cmd = new PageOrderCmd(reqMap.getWrappedMap());
		//根据输入参数组织返回信息
		OrderParam oparam = getTomerParam(cmd);
		wholeRetUrl = genWholeRetUrl(oparam);
		request.getSession().setAttribute("errorRetUrl", wholeRetUrl);
		//2-2 商户下单时间校验
		if(!DateUtil.verifyOrderDate(cmd.getMerDate())){
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1310"));
			modelMap.put(DataDict.RET_CODE, "1310");
			logInfo("VerifyOrderDate Result Failed[RetCode]:%s:%s", "1310","商户下单时间校验未通过");
			return ERROR_PAGE;
		}
		logInfo("VerifyOrderDate Result Success[RetCode]:0000:商户下单时间校验通过");
		//3-商户验签
		boolean isCheckSign = false;
		MpspMessage checkSignResp = restService.checkSign(cmd.getMerId(), cmd.getPlainText(), cmd.getSign());
		if(!checkSignResp.isRetCode0000()){
			logInfo("SignCheck Result Failed[RetCode]:%s:%s", checkSignResp.getRetCode(), "验证商户签名失败");
			modelMap.put(DataDict.RET_CODE, checkSignResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, checkSignResp.getRetCodeBussi());
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(checkSignResp.getRetCode()));
			return super.ERROR_PAGE;
		}
		logInfo("SignCheck Result Success[RetCode]0000:商户签名验证通过");
		isCheckSign = true;
		
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
			logInfo("获取页面请求参数merid[%s]-goodsid[%s]-refer[%s]", merid, goodsid,refer);
			
			String cacheKey = merid + "-" + goodsid;
			
			@SuppressWarnings("unchecked")
			List<String> referCacheList = (List<String>) hfMerReferCache.getMerReferFromCache(cacheKey);
			// 如果缓存中不存在
			if(referCacheList == null || referCacheList.size() == 0){
				logger.info("获取商户报备信息：缓存数据为空。");
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
			
			logInfo("商户渠道报备信息refer集合[%s]", referCacheList);
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
			 *  		alarmtype：1代表有手机号请求的url   2代表无手机号请求的url   3代表爬虫关键字   
			 */
			String alarmType = MerReferUtil.ALARM_URL_HAS_MOBILE;
			if(ObjectUtil.isEmpty(cmd.getMobileId())){
				alarmType = MerReferUtil.ALARM_URL_NO_MOBILE;
			}
			String logInfo = MerReferUtil.logFilterRefer(cmd, refer, rtnCode, this.getFunCode(), alarmType);
			logInfo("refer过滤结果：%s", logInfo);
		} catch (Throwable e) {
			e.printStackTrace();
			logInfo("商户渠道报备信息校验失败：%s", e.getMessage());
		}
		
		//判断二级商户信息，将二级商户信息放入modelMap
		modelMap.put("isLevel2", super.isLevel2Mer(cmd.getMerId()));
	
		
		//if(ObjectUtil.isEmpty(cmd.getMobileId())||isAuthCodeMer){
		if(ObjectUtil.isEmpty(cmd.getMobileId())){
			//4-1-不带手机号的商户请求，获取商户商品信息
			MpspMessage checkMerGoodsResp = restService.queryMerGoodsInfo(cmd.getMerId(), cmd.getGoodsId());
			if(!checkMerGoodsResp.isRetCode0000()){
				logInfo("queryMerGoodsInfo Result Failed[RetCode]:%s:%s", checkMerGoodsResp.getRetCode(), "获取商户商品信息失败");
				modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(checkMerGoodsResp.getRetCode()));
				modelMap.put(DataDict.RET_CODE, checkMerGoodsResp.getRetCode());
				modelMap.put(DataDict.RET_CODE_BUSSI, checkMerGoodsResp.getRetCodeBussi());
				return super.ERROR_PAGE;
			}
			logInfo("queryMerGoodsInfo Result Success[RetCode]0000:获取商户商品信息通过");
			OrderParam order = this.getNoMblParam(cmd.getMerId(), cmd.getGoodsId(), cmd.getAmount(), checkMerGoodsResp);
			//下单数据
			request.getSession().setAttribute(DataDict.FUNCODE_ORDER_PARAM, cmd);
			//验签标志
			request.getSession().setAttribute(DataDict.CHECK_SIGN_FLAG, isCheckSign);
			//日志所需参数
			order.setOrderId(cmd.getOrderId());
			order.setMerDate(cmd.getMerDate());
			request.getSession().setAttribute("orderPageParam", order);
			modelMap.put("order", order);
			modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
			modelMap.put("mobileIdRegex", MessageUtil.getLocalProperty(validateSource,"REGEXP."+DataDict.MER_REQ_MOBILEID));
			return "order/web_confirmPayNoMbl";
		}
		//4-2-交易鉴权
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
			return super.ERROR_PAGE;
		}
		logInfo("checkTrade Result Success[RetCode]0000:交易鉴权通过");
		isCheckTrade = true;
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		modelMap.put(HFBusiDict.PROVCODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.PROVCODE)));
		modelMap.put(HFBusiDict.AREACODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.AREACODE)));
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		//5-确认可支付银行
		String bankId = (String)checkTradeResp.get(HFBusiDict.BANKID);
		if(ObjectUtil.isEmpty(bankId)){
			//无可支付银行的返回码 <您暂时不能使用支付服务>
			logInfo("BankCheck Result Failed[RetCode]:%s:%s","1303","交易鉴权通过，但无可支付银行");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1303"));
			modelMap.put(DataDict.RET_CODE, "1303");
			return super.ERROR_PAGE;
		}
		logInfo("BankCheck Result Success[RetCode]:0000:可支付银行确认通过---bankId[ "+ bankId +" ]");
		//TODO begin 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-13
		modelMap.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-13
		//6-根据bankId判断跳转地址（小额支付，全网支付）
		String retView = "";
		if(bankId.startsWith("XE")){
			retView = "order/web_xe_confirmpay";
		}else if(bankId.startsWith("MW")){
			retView = "order/web_mw_confirmpay";
		}
		//判断该商户的统一支付页面订单页是否需要输入验证码 是则返回true  modified by wanghaiwei 2012-11-01 end
		String authCodeMeridList = messageService.getSystemParam("AuthCode.SHOW","");
		boolean isAuthCodeMer = authCodeMeridList.indexOf(cmd.getMerId()) != -1;
		if(isAuthCodeMer){
			modelMap.put("isAuthCodeMer", "true");
		}
		OrderParam orderParam = this.getNoMblParam(cmd.getMerId(), cmd.getGoodsId(), cmd.getAmount(), checkTradeResp);
		orderParam.setMobileId(cmd.getMobileId());
		orderParam.setOrderId(cmd.getOrderId());
		orderParam.setMerDate(cmd.getMerDate());
		modelMap.put("order", orderParam);
		// begin 二维码数据，江西小额不展示二维码 modified by lizhen
		String qrCodeUrl=messageService.getSystemParam("QRCode.URL","");
		String qrCodeAccess=messageService.getSystemParam("QRCode.ACCESS","");
		if(CommonUtil.isQRCodeAccess(qrCodeAccess,cmd.getMerId()) && !"".equals(qrCodeUrl) && !"XE791000".equals(bankId)){
			qrCodeUrl=this.getQRCodeURL(qrCodeUrl,cmd);
			modelMap.put("qrCodeUrl", qrCodeUrl);
		}
		// begin 二维码部分商户不显示 modified by fanning
		if(!"".equals(cmd.getMerId())){
			String rejectMerIds = messageService.getSystemParam("RejectMerIds","");
			if(!"".equals(rejectMerIds)){
				String merIds[] = rejectMerIds.split(",");
				for(String rejectMerId : merIds){
					if(cmd.getMerId().equals(rejectMerId)){
						modelMap.remove("qrCodeUrl");
						break;
					}
				}
			}
		}
		// end 二维码部分商户不显示 modified by fanning
		
		// end 二维码数据，江西小额不展示二维码 modified by lizhen
		//订单数据
		request.getSession().setAttribute(DataDict.FUNCODE_ORDER_PARAM, cmd);
		//交易鉴权数据
		request.getSession().setAttribute("tradeMessage", checkTradeResp);
		//验签标志
		request.getSession().setAttribute(DataDict.CHECK_SIGN_FLAG, isCheckSign);
		//交易鉴权标志
		request.getSession().setAttribute(DataDict.CHECK_TRADE_FLAG, isCheckTrade);
		modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		return retView;
	}
	
	/**
	 * ********************************************
	 * method name   : getNoMblParam 
	 * description   : 获取页面所需参数
	 * @return       : OrderParam
	 * @param        : @param merId
	 * @param        : @param goodsId
	 * @param        : @param amount
	 * @param        : @param merGoodsResp
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 18, 2011 11:16:33 AM
	 * @see          : 
	 * *******************************************
	 */
	private OrderParam getNoMblParam(String merId, String goodsId, String amount, MpspMessage merGoodsResp) {
		OrderParam orderParam = new OrderParam();
		orderParam.setGoodsId(goodsId);
		orderParam.setAmount(amount);
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar(amount));
		orderParam.setMerName((String)merGoodsResp.get(HFBusiDict.MERNAME));
		orderParam.setGoodsName((String)merGoodsResp.get(HFBusiDict.GOODSNAME));
		orderParam.setMerId(merId);
		if(super.isLevel2Mer(merId)){
			String merName2 = messageService.getSystemParam("unRealMerName");
			String goodsName2 = messageService.getSystemParam("unRealGoodsName");
			orderParam.setMerName2(merName2);
			orderParam.setGoodsName2(goodsName2);
		}
		orderParam.setServType(String.valueOf(merGoodsResp.get(HFBusiDict.SERVTYPE)));
		orderParam.setServMonth(String.valueOf(merGoodsResp.get(HFBusiDict.SERVMONTH)));
		orderParam.setCusPhone((String)merGoodsResp.get(HFBusiDict.CUSPHONE));
		return orderParam;
	}
	
	private String genWholeRetUrl(OrderParam param) {
		String retUrl = param.getRetUrl();
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
	private OrderParam getTomerParam(PageOrderCmd param) {
		OrderParam tomerParam = new OrderParam();
		tomerParam.setGoodsId(param.getGoodsId());
		tomerParam.setMerId(param.getMerId());
		tomerParam.setOrderId(param.getOrderId());
		tomerParam.setMerDate(param.getMerDate());
		tomerParam.setAmount(param.getAmount());
		tomerParam.setAmtType(param.getAmtType());
		tomerParam.setMobileId(param.getMobileId());
		if(ObjectUtil.isEmpty(tomerParam.getMobileId())){
			tomerParam.setMobileId("blank");
		}
		tomerParam.setPlateDate(param.getMerDate());
		tomerParam.setMerPriv(param.getMerPriv());
		tomerParam.setRetCode(DataDict.SYSTEM_ERROR_CODE);
		tomerParam.setRetUrl(param.getRetUrl());
		tomerParam.setVersion(param.getVersion());
		return tomerParam;
	}
	/**
	 * *****************  方法说明  *****************
	 * method name   :  getQRCodeURL
	 * @param		 :  @param url
	 * @param		 :  @param cmd
	 * @param		 :  @return
	 * @return		 :  String
	 * @author       :  LiZhen 2014-6-17 下午2:57:12
	 * description   :  组装二维码中的url
	 * @see          :  
	 * **********************************************
	 */
	private String getQRCodeURL(String url,PageOrderCmd cmd){
		StringBuffer buffer = new StringBuffer(url);
		String plaintText=cmd.getPlainText();
		try {
			plaintText=cmd.getUrlEncodedPlainText("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		buffer.append("?").append(plaintText);
		buffer.append("&sign=").append(cmd.getSign());
		return buffer.toString();
	}
	@Override
	protected String getFunCode() {
		//页面下单的功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_PAGE_XD;
	}
	public void setValidateSource(MessageSource validateSource) {
		this.validateSource = validateSource;
	}
}
