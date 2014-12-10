package com.umpay.hfweb.action.order;

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
 * class       :  WapDirectCaptchaAction
 * @author     :  lizhen 
 * description :  WAP直接支付-验证码生成和发送类，完成交易鉴权、确认可支付银行、验证码发送
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class WapDirectCaptchaAction extends BaseAbstractAction{
	
	private final String WAP_DIRECT_REQUEST = "order/wap_direct_request";//商户请求页面
	private final String WAP_DIRECT_PAYRESULT = "order/wap_direct_payresult";//支付结果页面

	protected String processBussiness(HttpServletRequest request,HttpServletResponse response,Map<String,Object> modelMap) {
		//1-获取参数
		RequestMsg reqMap = new RequestMsg();
		reqMap.putAllParam(HttpUtil.parseRequestParam(request));
		reqMap.setFunCode(this.getFunCode());
		String mobileId=(String)reqMap.get(DataDict.MER_REQ_MOBILEID);
		PageOrderCmd cmd=(PageOrderCmd)request.getSession().getAttribute(DataDict.FUNCODE_ORDER_PARAM);
		String wholeRetUrl=(String)request.getSession().getAttribute("wholeRetUrl");
		modelMap.put("wholeRetUrl", wholeRetUrl);
		OrderParam orderParam = getPageParam(cmd);
		modelMap.put("order", orderParam);
		modelMap.put(DataDict.MER_REQ_MOBILEID, mobileId);
		//检验手机号
		if(ObjectUtil.isEmpty(mobileId)){
			String retMsg = messageService.getSystemParam("Captcha.InputMobileId.Msg");//请输入11位手机号
			modelMap.put(DataDict.RET_MSG, retMsg);
			return WAP_DIRECT_REQUEST;
		}
		String regExp = messageService.getSystemParam("Captcha.MobileId.RegExp");
		if(ObjectUtil.isEmpty(regExp)){
			regExp="^1\\d{10}";//默认为1开头的11位数字
		}
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(mobileId);  
		if(!m.matches()){
			String retMsg = messageService.getSystemParam("Captcha.PatternError.Msg");//格式错误
			modelMap.put(DataDict.RET_MSG, retMsg);
			return WAP_DIRECT_REQUEST;
		}
		//2-交易鉴权
		boolean isCheckTrade = false;
		MpspMessage checkTradeResp = restService.checkTrade(mobileId, cmd.getMerId(), cmd.getGoodsId());
		if(!checkTradeResp.isRetCode0000()){
			logInfo("checkTrade Result Failed[RetCode]:%s:%s", checkTradeResp.getRetCode(), "交易鉴权失败");
			String errorMessage = messageService.getMessageDetail(checkTradeResp.getRetCodeBussi());
			if("".equals(errorMessage)){//modify by zhuoyangyang 20140430 交易鉴权失败返回码对应信息显示在页面上
				errorMessage = messageService.getMessageDetail(checkTradeResp.getRetCode());
			}
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, errorMessage);
			modelMap.put(DataDict.RET_CODE, checkTradeResp.getRetCode());
			return WAP_DIRECT_PAYRESULT;
		}
		logInfo("checkTrade Result Success[RetCode]0000:交易鉴权通过");
		isCheckTrade = true;
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		String bankId = ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.BANKID));
		modelMap.put(HFBusiDict.PROVCODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.PROVCODE)));
		modelMap.put(HFBusiDict.AREACODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.AREACODE)));
		modelMap.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		
		//使用鉴权后的商户名称和商品名称
		orderParam.setMerName((String)checkTradeResp.get(HFBusiDict.MERNAME));
		orderParam.setGoodsName((String)checkTradeResp.get(HFBusiDict.GOODSNAME));
		modelMap.put("order", orderParam);
		
		//3-确认可支付银行
		
		if(ObjectUtil.isEmpty(bankId)){
			//无可支付银行的返回码 <您暂时不能使用支付服务>
			logInfo("BankCheck Result Failed[RetCode]:%s:%s","1303","交易鉴权通过，但无可支付银行");
			modelMap.put(DataDict.RET_MSG, messageService.getMessageDetail("1303"));
			modelMap.put(DataDict.RET_CODE, "1303");
			return WAP_DIRECT_PAYRESULT;
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
			String retMsg = messageService.getSystemParam("Captcha.SentSuccess.Msg");
			retMsg=MessageFormat.format(retMsg, mobileId);//本次交易需要您进行验证码确认，已向您的手机{0}发送短信，请按提示操作
			modelMap.put(DataDict.RET_MSG, retMsg);
			modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		}else{
			//4-2 不是第一次生成验证码，则该请求是重新获取验证码，验证是否已经达到最大次数，默认3次
			String maxSentTimesStr = messageService.getSystemParam("Captcha.MaxSentTimes");
			if(captchaInfo.isLessThanMaxSentTimes(maxSentTimesStr)){
				String sentIntervalStr = messageService.getSystemParam("Captcha.SentInterval");
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
				}
			}else{
				String retMsg = messageService.getSystemParam("Captcha.MaxSentTimes.Msg");
				modelMap.put(DataDict.RET_MSG, retMsg);//验证码获取过于频繁，请重新下单后再次获取
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
			String smsContent=messageService.getSystemParam("Captcha.SmsContent.Msg");
			smsContent=MessageFormat.format(smsContent, captchaInfo.getCaptcha(),orderParam.getGoodsName(),orderParam.getAmount4dollar());
			smsService.pushSms(cmd.getMerId(), mobileId, smsContent);
		}
		
		//4-组装session
		request.getSession().setAttribute(DataDict.MER_REQ_MOBILEID, mobileId);
		request.getSession().setAttribute("tradeMessage", checkTradeResp);
		request.getSession().setAttribute(DataDict.CHECK_TRADE_FLAG, isCheckTrade);
		request.getSession().setAttribute(DataDict.SEND_SMS_CAPTCHAINFO,captchaInfo);
		//5-跳转
		String retView = "order/wap_direct_confirmpay";
		return retView;
	}
	
	private OrderParam getPageParam(PageOrderCmd cmd) {
		OrderParam orderParam = new OrderParam();
		orderParam.setOrderId(cmd.getOrderId());
		orderParam.setMerDate(cmd.getMerDate());
		orderParam.setAmount(MoneyUtil.Cent2Dollar(cmd.getAmount()));
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
		return com.umpay.hfweb.dict.DataDict.FUNCODE_WAPCAPTCHA;
	}
}
