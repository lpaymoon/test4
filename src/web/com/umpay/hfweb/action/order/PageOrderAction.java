package com.umpay.hfweb.action.order;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.action.param.SmsParam;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.util.CaptchaServiceSingleton;
import com.umpay.hfweb.util.CommonUtil;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  PageOrderAction
 * @author     :  zhaoyan 
 * description :  页面下单-确认支付，生成订单处理类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class PageOrderAction extends BaseAbstractAction{

	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap){
		//1-判断系统升级提示是否显示
		String notice = messageService.getSystemParam("notice");
		if (ObjectUtil.isNotEmpty(notice)) {
			modelMap.put("notice", notice);
		}
		//2-获取session数据
		HttpSession session = request.getSession();
		Boolean checkSignFlag = (Boolean)session.getAttribute(DataDict.CHECK_SIGN_FLAG);
		Boolean checkTradeFlag = (Boolean)session.getAttribute(DataDict.CHECK_TRADE_FLAG);
		PageOrderCmd cmd = (PageOrderCmd)session.getAttribute(DataDict.FUNCODE_ORDER_PARAM);
		MpspMessage message = (MpspMessage)session.getAttribute("tradeMessage");
//		session.removeAttribute(DataDict.FUNCODE_ORDER_PARAM);
//		session.removeAttribute("tradeMessage");
//		session.removeAttribute(DataDict.CHECK_SIGN_FLAG);
//		session.removeAttribute(DataDict.CHECK_TRADE_FLAG);
		if(checkSignFlag==null || checkTradeFlag==null || cmd==null || message==null){
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s","1127","会话已过期");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
			modelMap.put(DataDict.RET_CODE, "1127");
			return super.ERROR_PAGE;
		}
		logInfo("SessionCheck Result Success[RetCode]:0000:session数据确认通过");
		String bankId = ObjectUtil.trim((String)message.get(HFBusiDict.BANKID));
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		modelMap.put(HFBusiDict.PROVCODE, ObjectUtil.trim((String)message.get(HFBusiDict.PROVCODE)));
		modelMap.put(HFBusiDict.AREACODE, ObjectUtil.trim((String)message.get(HFBusiDict.AREACODE)));
		modelMap.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		//3-校验验证码 added by wanghaiwei 2012-11-12
		String retView = "";
		String key = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID)+"-wrongNum";
		//判断是否需要验证 验证码
		if (ObjectUtil.isNotEmpty(request.getParameter("isAuthCodeMer"))) {
			String random = ObjectUtil.trim((String)request.getParameter("j_captcha_response"));
			//判断验证码是否正确
			if (!CaptchaServiceSingleton.getInstance().validateCaptchaResponse(random, request.getSession())) {
				Object num = session.getAttribute(key);
				if(num==null){
					num=0;
				}
				String count = String.valueOf(num);
				int wrongNum = Integer.parseInt(count);
				wrongNum++;
				if(wrongNum==3){
					modelMap.put(DataDict.RET_CODE, "1407");
					modelMap.put(DataDict.RET_CODE_BUSSI, "1407");
					modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1407"));
					session.removeAttribute(key);
					return super.ERROR_PAGE;
				}
				session.setAttribute(key,wrongNum);
				
				//验证码错误
				logInfo("validateCaptchaResponse Result Failed[RetCode]:%s:%s","1307", "验证码校验失败");
				//需要重回confrimpay页面，获取页面所需参数
				OrderParam orderParam = getReConfrimParam(cmd,message);
				modelMap.put("jcaptchaError", "true");
				modelMap.put("isAuthCodeMer", "true");
				modelMap.put("order", orderParam);
				modelMap.put(DataDict.RET_CODE, "1307");
				if(bankId.startsWith("XE")){
					retView = "order/web_xe_confirmpay";
				}else if(bankId.startsWith("MW")){
					retView = "order/web_mw_confirmpay";
				}
				// begin 二维码数据，江西小额不展示二维码 modified by lizhen
				String qrCodeUrl=messageService.getSystemParam("QRCode.URL","");
				String qrCodeAccess=messageService.getSystemParam("QRCode.ACCESS","");
				if(CommonUtil.isQRCodeAccess(qrCodeAccess,cmd.getMerId()) && !"".equals(qrCodeUrl) && !"XE791000".equals(bankId)){
					qrCodeUrl=this.getQRCodeURL(qrCodeUrl,cmd);
					modelMap.put("qrCodeUrl", qrCodeUrl);
				}
				// end 二维码数据，江西小额不展示二维码 modified by lizhen
				return retView;
			}
			//验证码正确
			logInfo("validateCaptchaResponse Result Success[RetCode]:0000:验证码校验通过");	
		}
//		session.removeAttribute(key);
//		session.removeAttribute(DataDict.FUNCODE_ORDER_PARAM);
//		session.removeAttribute("tradeMessage");
//		session.removeAttribute(DataDict.CHECK_SIGN_FLAG);
//		session.removeAttribute(DataDict.CHECK_TRADE_FLAG);
		//4-判断是否验签，是否交易鉴权
		if(!checkSignFlag || !checkTradeFlag){
			logInfo("checkSignFlag And CheckTradeFlag Result Failed[RetCode]:%s:%s", DataDict.SYSTEM_ERROR_CODE, "验签或交易鉴权未通过，终止下单");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(DataDict.SYSTEM_ERROR_CODE));
			modelMap.put(DataDict.RET_CODE, DataDict.SYSTEM_ERROR_CODE);
			return super.ERROR_PAGE;
		}
		logInfo("checkSignFlag And CheckTradeFlag Result Success[RetCode]:0000:签名和交易鉴权确认通过");
		//TODO 这个四川的确认条款页面还没做
//		if (ObjectUtil.isNotEmpty(bankId) && bankId.equals("XE028000")) {
//			
//			return "umpay_xe_sc_new";
//		}
		//5-交易屏蔽模板校验
		MpspMessage transaclResp = restService.transacl(message, cmd);
		if(!transaclResp.isRetCode0000()){
			modelMap.put(DataDict.RET_CODE, transaclResp.getRetCode());
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(transaclResp.getRetCode()));
			
			logInfo("Transacl Result Failed[RetCode]:%s:%s", transaclResp.getRetCode(), "交易屏蔽模板校验失败");
			return super.ERROR_PAGE;
		}
		logInfo("Transacl Result Success[RetCode]:0000:交易屏蔽模板通过");
		//6-保存订单
		MpspMessage orderResp = null;
		if("XE791000".equals(bankId)){
			logInfo("调用江西小额异步下单接口 bankId:%s",bankId);
			cmd.setBusinessType(DataDict.BUSI_JX_WEB);//江西小额
			orderResp = tradeService.asynOrder(bankId, message, cmd);
		}else{
			cmd.setBusinessType(DataDict.BUSI_STANDARD_WEB);//标商web支付
			orderResp = tradeService.saveOrder(bankId, message, cmd);
		}
		if(!orderResp.isRetCode0000()){
			//二维码下单后，页面再次下单则重回confirmpay页面  modified by lizhen
			if("86001102".equals(orderResp.getRetCodeBussi()) && !"XE791000".equals(bankId)){
				//4-查询订单信息
				MpspMessage orderInfoResp = restService.queryMerOrder(cmd.getMerId(), cmd.getMerDate(), cmd.getOrderId());
				if(DataDict.BUSI_QRCODE.equals(orderInfoResp.get(HFBusiDict.BUSINESSTYPE))){
					//订单已存在且支付类型为二维码支付时重回confrimpay页面，除江西小额
					logInfo("queryOrderByMobileId Result Failed[RetCode]:%s:%s", orderResp.getRetCode(), "订单已存在");
					String qrCodeUrl=messageService.getSystemParam("QRCode.URL","");
					if(!"".equals(qrCodeUrl)){
						qrCodeUrl=this.getQRCodeURL(qrCodeUrl,cmd);
						modelMap.put("qrCodeUrl", qrCodeUrl);
					}
					OrderParam orderParam = getReConfrimParam(cmd,message);
					modelMap.put("order", orderParam);
					modelMap.put("isQRCodeOrder", true);
					//判断是否显示验证码
					String authCodeMeridList = messageService.getSystemParam("AuthCode.SHOW","");
					boolean isAuthCodeMer = authCodeMeridList.indexOf(cmd.getMerId()) != -1;
					if(isAuthCodeMer){
						modelMap.put("isAuthCodeMer", "true");
					}
					
					if(bankId.startsWith("XE")){
						retView = "order/web_xe_confirmpay";
					}else if(bankId.startsWith("MW")){
						retView = "order/web_mw_confirmpay";
					}
					return retView;
				}
			}
			logInfo("saveOrder Result Failed[RetCode]:%s:%s", orderResp.getRetCode(), "保存订单异常");
			String errorMessage = messageService.getMessageDetail(orderResp.getRetCodeBussi());
			if("".equals(errorMessage)){//modify by zhuoyangyang 限控不通过时，根据返回码进行页面提示，现在只有对86011931，86011927，86011928做了信息配置
				errorMessage = messageService.getMessageDetail(orderResp.getRetCode());
			}
		    modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, errorMessage);
			modelMap.put(DataDict.RET_CODE, orderResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, orderResp.getRetCodeBussi());
			return super.ERROR_PAGE;
		}else{
			logInfo("saveOrder Result Success[RetCode]:0000:下单成功");
			
			session.removeAttribute(key);
			session.removeAttribute(DataDict.FUNCODE_ORDER_PARAM);
			session.removeAttribute("tradeMessage");
			session.removeAttribute(DataDict.CHECK_SIGN_FLAG);
			session.removeAttribute(DataDict.CHECK_TRADE_FLAG);
			
			//7-取得短信发送的对象，组装发送短信参数
			String porderId = (String)orderResp.get(HFBusiDict.ORDERID4p);
			String goodsId = (String)orderResp.get(HFBusiDict.GOODSID);
			String servType = String.valueOf(orderResp.get(HFBusiDict.SERVTYPE));
			String servMonth = String.valueOf(orderResp.get(HFBusiDict.SERVMONTH));
			//modify by yangwr 2012-04-05 start 短信子号迁移
			SmsParam smsParam = null;
			String merId = (String)orderResp.get(HFBusiDict.MERID);
			String mers = messageService.getSystemParam("special.called.mer.list","");
			if(mers.toUpperCase().contains("ALL") || mers.contains(merId)){
				smsParam = smsService.genSmsInfoSpecial(servType, servMonth, goodsId, porderId,merId);
			}else{
				smsParam = smsService.genSmsInfo(servType, servMonth, goodsId, porderId);
			}
			//SmsParam smsParam = smsService.genSmsInfo(servType, servMonth, goodsId, porderId);
			//modify by yangwr 2012-04-05 end
			session.setAttribute("smsParam", smsParam);
			OrderParam orderParam = getPageParam(orderResp, smsParam.getSmsSub(), cmd, bankId);
			orderParam.setMerName((String)message.get(HFBusiDict.MERNAME));
			orderParam.setGoodsName((String)message.get(HFBusiDict.GOODSNAME));
			orderParam.setServType(String.valueOf(message.get(HFBusiDict.SERVTYPE)));
			orderParam.setServMonth(servMonth);
			orderParam.setCusPhone(String.valueOf(message.get(HFBusiDict.CUSPHONE)));
			if(super.isLevel2Mer(cmd.getMerId())){
				String merName2 = messageService.getSystemParam("unRealMerName");
				String goodsName2 = messageService.getSystemParam("unRealGoodsName");
				orderParam.setMerName2(merName2);
				orderParam.setGoodsName2(goodsName2);
			}
			modelMap.put("order", orderParam);
			//modify by zhaoYan 2013-5-28 begin
			String smsSub = "";
			if("XE791000".equals(bankId)){
				smsSub = messageService.getSystemParam("XE791000.smsSub");
			}else{
				smsSub = smsParam.getSmsSub();
			}
			logInfo("saveOrder Result 页面展示需要的短信长号码[smsSub]:%s", smsSub);
			modelMap.put("smsSub", smsSub);
			//modify by zhaoYan 2013-5-28 end
			modelMap.put("exAction", "pageOrderAction");
			session.setAttribute("orderParam", orderParam);
			//TODO 放入session中 省份 地市
			session.setAttribute(HFBusiDict.PROVCODE, ObjectUtil.trim((String)message.get(HFBusiDict.PROVCODE)));
			session.setAttribute(HFBusiDict.AREACODE, ObjectUtil.trim((String)message.get(HFBusiDict.AREACODE)));
			//判断此商户是否允许显示5iplay广告
			//modelMap.put("show5iPlay", isShow5iPlay(orderParam.getMerId()));
			//用于判断支付类型 小额支付 全网支付
			
			if(bankId.startsWith("XE")){
				retView = "order/web_xe_payresult";
			}else if(bankId.startsWith("MW")){
				retView = "order/web_mw_payresult";
			}
			modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
			return retView;
		}
	}
/**
 * method name   : getReConfrimParam 
 * description   : 验证码输入错误，需要重回confrimpay页面时，获取页面所需参数
 * @param cmd
 * @param merGoodsResp
 * @return
 * add      : wanghaiwei ,  2012-11-12 
 */
	private OrderParam getReConfrimParam(PageOrderCmd cmd, MpspMessage merGoodsResp) {
		OrderParam orderParam = new OrderParam();
		orderParam.setMobileId(cmd.getMobileId());
		orderParam.setGoodsId(cmd.getGoodsId());
		orderParam.setAmount(cmd.getAmount());
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar(cmd.getAmount()));
		orderParam.setMerName((String)merGoodsResp.get(HFBusiDict.MERNAME));
		orderParam.setGoodsName((String)merGoodsResp.get(HFBusiDict.GOODSNAME));
		orderParam.setMerId(cmd.getMerId());
		if(super.isLevel2Mer(cmd.getMerId())){
			String merName2 = messageService.getSystemParam("unRealMerName");
			String goodsName2 = messageService.getSystemParam("unRealGoodsName");
			orderParam.setMerName2(merName2);
			orderParam.setGoodsName2(goodsName2);
		}
		orderParam.setOrderId(cmd.getOrderId());
		orderParam.setMerDate(cmd.getMerDate());
		orderParam.setServType(String.valueOf(merGoodsResp.get(HFBusiDict.SERVTYPE)));
		orderParam.setServMonth(String.valueOf(merGoodsResp.get(HFBusiDict.SERVMONTH)));
		orderParam.setCusPhone((String)merGoodsResp.get(HFBusiDict.CUSPHONE));
		return orderParam;
	}	
	/**
	 * ********************************************
	 * method name   : getPageParam 
	 * description   : 获得页面展示的参数
	 * @return       : OrderPageParam
	 * @param        : @param message
	 * @param        : @param orderResp
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 4, 2011 5:23:18 PM
	 * @see          : 
	 * *******************************************
	 */
	private OrderParam getPageParam(MpspMessage orderResp, String smsSub, PageOrderCmd cmd, String bankId) {
		OrderParam orderParam = new OrderParam();
		orderParam.setGoodsId((String)orderResp.get(HFBusiDict.GOODSID));
		orderParam.setOrderId((String)orderResp.get(HFBusiDict.ORDERID));
		orderParam.setPorderId((String)orderResp.get(HFBusiDict.ORDERID4p));
		orderParam.setAmount((String)orderResp.get(HFBusiDict.AMOUNT));
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar((String)orderResp.get(HFBusiDict.AMOUNT)));
		orderParam.setMerId((String)orderResp.get(HFBusiDict.MERID));
		orderParam.setMerDate((String)orderResp.get(HFBusiDict.ORDERDATE));
		orderParam.setMobileId((String)orderResp.get(HFBusiDict.MOBILEID));
		orderParam.setBankId(bankId);
		
		orderParam.setRetUrl(cmd.getRetUrl());
		orderParam.setAmtType(cmd.getAmtType());
		orderParam.setBankType(cmd.getBankType());
		orderParam.setVersion(cmd.getVersion());
		orderParam.setMerPriv(cmd.getMerPriv());
		//added by wanghaiwei 2012-11-12
		orderParam.setRetCode(orderResp.getRetCode());
		//end
		String tableName = "";
		String porderId = orderParam.getPorderId();
		if(ObjectUtil.isNotEmpty(porderId)){
			tableName = porderId.substring(porderId.length()-1,porderId.length());
		}
		orderParam.setTableName(tableName);
		String retCode = orderResp.getRetCode();
		if(orderResp.isRetCode0000()){
			orderParam.setOrderState("0");
		}else if(retCode.equals("1163")){
			orderParam.setOrderState("4");
		}else if(retCode.equals("1164")){
			orderParam.setOrderState("1");
		}else if(retCode.equals("1165")){
			orderParam.setOrderState("2");
		}else{
			orderParam.setOrderState("1");
		}

		return orderParam;
	}
	/**
	 * ********************************************
	 * method name   : isShow5iPlay 
	 * description   : 根据商户号判断是否允许显示5iplay广告
	 * @return       : boolean
	 * @param        : @param merId
	 * @param        : @return
	 * modified      : zhaoyan ,  2011-11-30 下午09:03:57
	 * @see          : 
	 * *******************************************
	 */
//	private boolean isShow5iPlay(String merId){
//		logger.info("判断是否为二级商户号：" + merId);
//		String permitShow5iPlay = messageService.getSystemParam("permit_show_5iplay");
//	    // 判断是否是二级商户的交易  如果是为true
//	    boolean show5iPlay = permitShow5iPlay.indexOf(merId) != -1;
//	    return show5iPlay;
//
//	}
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
	protected String createTransRpid(HttpServletRequest request){
		String rpid = ObjectUtil.trim(request.getSession().getAttribute(DataDict.REQ_MER_RPID));
		if(ObjectUtil.isNotEmpty(rpid)){
			SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, rpid);
			return rpid;
		}else {
			return super.createTransRpid(request);
		}
	}
	@Override
	protected String getFunCode() {
		//页面下单的功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_PAGE_ORDSV;
	}
}
