package com.umpay.hfweb.action.pay;

import java.sql.Timestamp;
import java.text.MessageFormat;
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
import com.umpay.hfweb.model.CaptchaInfo;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  WapDirectPayAction
 * @author     :  lizhen 
 * description :  WAP直接支付-完成验证码的校验，支付
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class WapDirectPayAction extends BaseAbstractAction{
	private final String WAP_DIRECT_CONFIRMPAY = "order/wap_direct_confirmpay";
	private final String WAP_DIRECT_PAYRESULT = "order/wap_direct_payresult";
	
	protected String processBussiness(HttpServletRequest request,HttpServletResponse response,Map<String,Object> modelMap){
		//1-校验session
		RequestMsg reqMap = new RequestMsg();
		reqMap.putAllParam(HttpUtil.parseRequestParam(request));
		reqMap.setFunCode(this.getFunCode());
		
		HttpSession session = request.getSession();
		CaptchaInfo captchaInfo=(CaptchaInfo)session.getAttribute(DataDict.SEND_SMS_CAPTCHAINFO);
		Boolean checkSignFlag = (Boolean)session.getAttribute(DataDict.CHECK_SIGN_FLAG);
		Boolean checkTradeFlag = (Boolean)session.getAttribute(DataDict.CHECK_TRADE_FLAG);
		PageOrderCmd cmd = (PageOrderCmd)session.getAttribute(DataDict.FUNCODE_ORDER_PARAM);
		MpspMessage message = (MpspMessage)session.getAttribute("tradeMessage");
		String mobileId=(String)session.getAttribute(DataDict.MER_REQ_MOBILEID);
		String wholeRetUrl=(String)session.getAttribute("wholeRetUrl");
		modelMap.put("wholeRetUrl", wholeRetUrl);
		
		if(checkSignFlag==null || checkTradeFlag==null || cmd==null || message==null || captchaInfo==null){
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s","1127","会话已过期");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
			modelMap.put(DataDict.RET_CODE, "1127");
			String retMsg=messageService.getSystemParam("Captcha.Timeout.Msg");//交易超时
			modelMap.put(DataDict.RET_MSG, retMsg);
			return WAP_DIRECT_PAYRESULT;
		}
		logInfo("SessionCheck Result Success[RetCode]:0000:session数据确认通过");
		
		OrderParam orderParam = getPageParam(cmd);
		orderParam.setMerName((String)message.get(HFBusiDict.MERNAME));
		orderParam.setGoodsName((String)message.get(HFBusiDict.GOODSNAME));
		modelMap.put("order", orderParam);
		modelMap.put(DataDict.MER_REQ_MOBILEID, mobileId);
		//2-验证码校验
		String captcha=reqMap.getStr("captcha");
		//2-1 判断是否已输入
		if(ObjectUtil.isEmpty(captcha)){
			String retMsg=messageService.getSystemParam("Captcha.PlsInput.Msg");//请输入验证码
			modelMap.put(DataDict.RET_MSG, retMsg);
			return WAP_DIRECT_CONFIRMPAY;
		}
		//2-2 判断是否已失效
		Timestamp now=new Timestamp(System.currentTimeMillis());
		String validTimeStr = messageService.getSystemParam("Captcha.ValidTime");//有效时间，单位为秒
		if(captchaInfo.isExpired(now,validTimeStr)){
			String retMsg=messageService.getSystemParam("Captcha.Invalid.Msg");//验证码失效，请重新获取
			modelMap.put(DataDict.RET_MSG, retMsg);
			return WAP_DIRECT_CONFIRMPAY;
		}
		
		//2-3 判断输入是否正确以及错误次数是否过多
		String maxErrorTimesStr = messageService.getSystemParam("Captcha.MaxErrorTimes");//最大输错次数，3次
		if(captchaInfo.isLessThanMaxErrorTimes(maxErrorTimesStr)){
			if(!captcha.equals(captchaInfo.getCaptcha())){
				//输入验证码错误，且次数未超限
				captchaInfo.setErrorTimes(captchaInfo.getErrorTimes()+1);
				session.setAttribute(DataDict.SEND_SMS_CAPTCHAINFO, captchaInfo);
				String retMsg=messageService.getSystemParam("Captcha.InputError.Msg");//验证码错误，请重新输入
				modelMap.put(DataDict.RET_MSG, retMsg);
				return WAP_DIRECT_CONFIRMPAY;
			}
		}else{
			//验证码错误次数超限
			String retMsg=messageService.getSystemParam("Captcha.MaxErrorTimes.Msg");//验证码错误次数超限，请重新获取验证码
			modelMap.put(DataDict.RET_MSG, retMsg);
			return WAP_DIRECT_CONFIRMPAY;
		}
		logInfo("Captcha Result Success[RetCode]:0000:验证码校验通过");
		session.removeAttribute(DataDict.SEND_SMS_CAPTCHAINFO);//无论是否支付成功，只要验证通过了就删除session中的验证码信息
		session.removeAttribute("tradeMessage");
		session.removeAttribute(DataDict.MER_REQ_MOBILEID);
		session.removeAttribute(DataDict.FUNCODE_ORDER_PARAM);
		
		//6-支付 0000和86011571均为支付成功
		cmd.setBusinessType(DataDict.BUSI_WAP_ZJZF);//业务区分，wap直接支付（0207）
		MpspMessage orderResp = tradeService.wapDirectPay(cmd,mobileId,false);
		if(orderResp.isRetCode0000() || "86011571".equals(orderResp.getRetCode())){
			String retMsg=messageService.getSystemParam("Captcha.PaySuccess.Msg");//支付成功的提示语
			retMsg=MessageFormat.format(retMsg, orderParam.getGoodsName(),orderParam.getAmount4dollar());
			modelMap.put(DataDict.RET_MSG, retMsg);
			modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
			logInfo("WAPZJZF Result Success[RetCode]:0000:支付成功");
		}else{
			String retMsg=getRetMsg(orderResp);
			modelMap.put(DataDict.RET_MSG, retMsg);
			modelMap.put(DataDict.RET_CODE, orderResp.getRetCode());
			logInfo("WAPZJZF Result Failed[RetCode]:%s:%s",orderResp.getRetCode(),"支付失败");
		}
		return WAP_DIRECT_PAYRESULT;
	}
	
	private String getRetMsg(MpspMessage orderResp){
		String retMsg=messageService.getSystemParam("Captcha."+orderResp.getRetCode()+".Msg");
		if(ObjectUtil.isEmpty(retMsg)){
			retMsg=messageService.getSystemParam("Captcha.PayFailure.Msg");//支付失败的默认提示语
		}
		if(ObjectUtil.isEmpty(retMsg)){
			retMsg="交易失败，请稍后再试。详询4006125880。";
		}
		return retMsg;
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
		orderParam.setMerId(cmd.getMerId());
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
		//页面下单的功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_WAPZJZF;
	}
}
