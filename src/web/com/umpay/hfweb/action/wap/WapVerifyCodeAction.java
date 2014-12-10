package com.umpay.hfweb.action.wap;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.CaptchaInfo;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  WapVerifyCodeAction
 * @author     :  panxingwu 
 * description :  接收获取验证码的请求，向用户下发短信（短信内容包含验证码）
 *   			     返回到wap确认支付的页面（页面包含验证码输入框，商品名称，金额）
 * ***********************************************
 */
public class WapVerifyCodeAction extends BaseAbstractAction{
	
	private final String WAP_XE_REQUEST = "wap/wap_verify_xe_request";
	private final String WAP_MW_REQUEST = "wap/wap_verify_mw_request";
	private final String WAP_XE_PAYRESULT = "wap/wap_verify_xe_payresult";
	private final String WAP_MW_PAYRESULT = "wap/wap_verify_mw_payresult";
	private final String WAP_XE_CONFIRMPAY = "wap/wap_verify_xe_confirmpay";
	private final String WAP_MW_CONFIRMPAY = "wap/wap_verify_mw_confirmpay";
	  
	protected String processBussiness(HttpServletRequest request,HttpServletResponse response,Map<String,Object> modelMap) {
		//1-获取参数
		RequestMsg reqMap = new RequestMsg();
		reqMap.putAllParam(HttpUtil.parseRequestParam(request));
		reqMap.setFunCode(this.getFunCode());
		
		PageOrderCmd cmd=(PageOrderCmd)request.getSession().getAttribute(DataDict.FUNCODE_ORDER_PARAM);
		if(cmd==null){
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s","1127","会话已过期");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
			modelMap.put(DataDict.RET_CODE, "1127");
			String retMsg=messageService.getSystemParam("Captcha.Timeout.Msg");//交易超时
			modelMap.put(DataDict.RET_MSG, retMsg);
			return WAP_MW_PAYRESULT;
		}
		String mobileId=(String)reqMap.get(DataDict.MER_REQ_MOBILEID);
		String wholeRetUrl=(String)request.getSession().getAttribute("wholeRetUrl");
		modelMap.put("wholeRetUrl", wholeRetUrl);
		OrderParam orderParam = (OrderParam)request.getSession().getAttribute("orderParam");
		modelMap.put("order", orderParam);
		request.getSession().removeAttribute("orderParam");//取出后必须remove掉，否则会影响其他页面
		modelMap.put(DataDict.MER_REQ_MOBILEID, mobileId);
		//2检验手机号
		boolean isMblLegal=true;
		if(ObjectUtil.isEmpty(mobileId)){
			isMblLegal=false;
			String retMsg = messageService.getSystemParam("Captcha.InputMobileId.Msg");//请输入11位手机号
			modelMap.put(DataDict.RET_MSG, retMsg);
			modelMap.put(DataDict.RET_CODE, "10001");//请输入手机号
			modelMap.put("flag", "0");
		}else{
			String regExp = messageService.getSystemParam("Captcha.MobileId.RegExp");
			if(ObjectUtil.isEmpty(regExp)){
				regExp="^1\\d{10}";//默认为1开头的11位数字
			}
			Pattern p = Pattern.compile(regExp);
			Matcher m = p.matcher(mobileId);  
			if(!m.matches()){
				isMblLegal=false;
				String retMsg = messageService.getSystemParam("Verify.PatternError.Msg");//格式错误
				modelMap.put(DataDict.RET_MSG, retMsg);
				modelMap.put(DataDict.RET_CODE, "10002");//输入错误
				modelMap.put("flag", "0");
			}
		}
		if(!isMblLegal){//手机号不合法，获取商品信息
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
			request.getSession().setAttribute("orderParam", orderParam);
			modelMap.put("order", orderParam);
			return WAP_MW_REQUEST;
		}
		
		//begin 获取bankid 用于判断全网还是小额 modified by lizhen 2014-05-21
		MpspMessage checkTradeResp = restService.checkTrade(mobileId, cmd.getMerId(), cmd.getGoodsId());
		String bankId = ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.BANKID));
		boolean isXEBank = false;
		if (bankId != null && bankId.startsWith("XE")) {
			isXEBank = true;
		}
		//end 获取bankid 用于判断全网还是小额 modified by lizhen 2014-05-21
		
		//2-交易鉴权
		boolean isCheckTrade = false;
		if(!checkTradeResp.isRetCode0000()){
			logInfo("checkTrade Result Failed[RetCode]:%s:%s", checkTradeResp.getRetCode(), "交易鉴权失败");
			String errorMessage = messageService.getMessageDetail(checkTradeResp.getRetCodeBussi());
			if("".equals(errorMessage)){//modify by zhuoyangyang 20140430 交易鉴权失败返回码对应信息显示在页面上
				errorMessage = messageService.getMessageDetail(checkTradeResp.getRetCode());
			}
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, errorMessage);
			modelMap.put(DataDict.RET_CODE, checkTradeResp.getRetCode());
			return isXEBank ? WAP_XE_PAYRESULT : WAP_MW_PAYRESULT;
		}
		logInfo("checkTrade Result Success[RetCode]0000:交易鉴权通过");
		isCheckTrade = true;
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		modelMap.put(HFBusiDict.PROVCODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.PROVCODE)));
		modelMap.put(HFBusiDict.AREACODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.AREACODE)));
		modelMap.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		
		//使用鉴权后的值，如商户名称和商品名称等等
		orderParam = getPageParam(checkTradeResp, cmd);
		modelMap.put("order", orderParam);
		
		//3-确认可支付银行
		
		if(ObjectUtil.isEmpty(bankId)){
			//无可支付银行的返回码 <您暂时不能使用支付服务>
			logInfo("BankCheck Result Failed[RetCode]:%s:%s","1303","交易鉴权通过，但无可支付银行");
			modelMap.put(DataDict.RET_MSG, messageService.getMessageDetail("1303"));
			modelMap.put(DataDict.RET_CODE, "1303");
			return isXEBank ? WAP_XE_PAYRESULT : WAP_MW_PAYRESULT;
		}
		logInfo("BankCheck Result Success[RetCode]:0000:可支付银行确认通过---bankId[ "+ bankId +" ]");
		
		//4-发送验证码
		//第一次发验证码则保存到session，重发时需要判断是否超过设定的最大次数，还要判断是否距前一次发送的时间间隔太短
		CaptchaInfo captchaInfo=(CaptchaInfo) request.getSession().getAttribute(DataDict.SEND_SMS_CAPTCHAINFO);
		Timestamp now=new Timestamp(System.currentTimeMillis());
		boolean sendFlag=false;
		if(captchaInfo==null){
			//4-1 首次生成验证码，保存到session，页面跳到确认支付页面
			captchaInfo=new CaptchaInfo();
			sendFlag=true;
			String retMsg = messageService.getSystemParam("Verify.SentSuccess.Msg");
			retMsg=MessageFormat.format(retMsg, mobileId);//本次交易需要您进行验证码确认，已向您的手机{0}发送短信，请按提示操作
			modelMap.put(DataDict.RET_MSG, retMsg);
			modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		}else{
			//4-2 不是第一次生成验证码，则该请求是重新获取验证码，验证是否已经达到最大次数，默认3次
			String maxSentTimesStr = messageService.getSystemParam("Verify.MaxSentTimes");
			if(captchaInfo.isLessThanMaxSentTimes(maxSentTimesStr)){
				String sentIntervalStr = messageService.getSystemParam("Verify.SentInterval");
				//两次获取验证码的间隔不能太短，默认5秒
				if(captchaInfo.isMoreThanInterval(now,sentIntervalStr)){
					captchaInfo.regenerateCaptcha();//重新生成
					sendFlag=true;
					String retMsg = messageService.getSystemParam("Captcha.SentSuccess.Msg");
					retMsg=MessageFormat.format(retMsg, mobileId);//本次交易需要您进行验证码确认，已向您的手机{0}发送短信，请按提示操作
					modelMap.put(DataDict.RET_MSG, retMsg);
					modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
				}else{
					String retMsg = messageService.getSystemParam("Captcha.IntervalShort.Msg");
					modelMap.put(DataDict.RET_MSG, retMsg);//验证码获取时间间隔太短，请稍后重新获取
					modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
				}
			}else{
				String retMsg = messageService.getSystemParam("Verify.MaxSentTimes.Msg");
				modelMap.put(DataDict.RET_MSG, retMsg);//验证码获取过于频繁，请重新下单后再次获取
				modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
				modelMap.put("flag", "0");
			}
		}
		logInfo("验证码为  "+captchaInfo.getCaptcha());
		
		if(sendFlag){
			//4-3 发验证码短信
//			Map<String,String> smsMap = new HashMap<String,String>();
//			smsMap.put(HFBusiDict.RPID, ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID)));
//			smsMap.put(HFBusiDict.MOBILEID, mobileId);
//			smsMap.put(HFBusiDict.MERID, cmd.getMerId());
//			smsMap.put(HFBusiDict.GOODSID, cmd.getGoodsId());
//			smsMap.put(HFBusiDict.GOODSNAME, (String)checkTradeResp.get(HFBusiDict.GOODSNAME));
//			smsMap.put(HFBusiDict.BANKID, bankId);
//			smsMap.put(HFBusiDict.AMOUNT, cmd.getAmount());
//			smsMap.put(HFBusiDict.VERIFYCODE, captchaInfo.getCaptcha());
//			smsMap.put(HFBusiDict.SERVTYPE, ObjectUtil.trim(checkTradeResp.get(HFBusiDict.SERVTYPE)));
//			smsMap.put(HFBusiDict.PORDERID, cmd.getMerId()+cmd.getGoodsId());
//			smsService.pushCaptchaSms(smsMap);
			String smsContent=messageService.getSystemParam("Verify.SmsContent.Msg");
			smsContent=MessageFormat.format(smsContent, captchaInfo.getCaptcha(),orderParam.getGoodsName(),orderParam.getAmount4dollar());
			smsService.pushSms(cmd.getMerId(), mobileId, smsContent);
		}
		
		//4-组装session
		request.getSession().setAttribute(DataDict.MER_REQ_MOBILEID, mobileId);
		request.getSession().setAttribute("tradeMessage", checkTradeResp);
		request.getSession().setAttribute(DataDict.CHECK_TRADE_FLAG, isCheckTrade);
		request.getSession().setAttribute(DataDict.SEND_SMS_CAPTCHAINFO,captchaInfo);
		request.getSession().setAttribute("orderParam", orderParam);
		//5-跳转
		return isXEBank ? WAP_XE_CONFIRMPAY : WAP_MW_CONFIRMPAY;
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
	
	private OrderParam getPageParam(PageOrderCmd cmd) {
		OrderParam orderParam = new OrderParam();
		orderParam.setOrderId(cmd.getOrderId());
		orderParam.setMerDate(cmd.getMerDate());
		orderParam.setAmount(cmd.getAmount());
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar(cmd.getAmount()));
		orderParam.setMobileId(cmd.getMobileId());
		orderParam.setGoodsId(cmd.getGoodsId());
		orderParam.setGoodsName(cmd.getGoodsName());
		return orderParam;
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
		//WAP直接支付发送验证码的功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_WAPYZM;
	}
}
