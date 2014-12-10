package com.umpay.hfweb.action.wap;

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
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.CaptchaInfo;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;


/** ******************  类说明  *********************
 * class       :  WapVerifyCodePayAction
 * @author     :  panxingwu
 * description :  接收来自wap确认支付页面的的请求，进行支付流程
 * 				     返回到支付结果页面（页面包含支付结果）
 * @see        :                        
 * ************************************************/   
public class WapVerifyCodePayAction extends BaseAbstractAction{
	private final String WAP_XE_PAYRESULT = "wap/wap_verify_xe_payresult";//商户请求页面
	private final String WAP_MW_PAYRESULT = "wap/wap_verify_mw_payresult";//商户请求页面
	private final String WAP_XE_CONFIRMPAY = "wap/wap_verify_xe_confirmpay";
	private final String WAP_MW_CONFIRMPAY = "wap/wap_verify_mw_confirmpay";
	
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
		OrderParam orderParam = (OrderParam)session.getAttribute("orderParam");
		String mobileId=(String)session.getAttribute(DataDict.MER_REQ_MOBILEID);
		String wholeRetUrl=(String)session.getAttribute("wholeRetUrl");
		modelMap.put("wholeRetUrl", wholeRetUrl);
		
		MpspMessage checkTradeResp = restService.checkTrade(mobileId, cmd.getMerId(), cmd.getGoodsId());
		//begin 获取bankid 用于判断全网还是小额 modified by lizhen 2014-05-21
		String bankId = ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.BANKID));
		boolean isXEBank = false;
		if(bankId.startsWith("XE")){
			isXEBank = true;
		}
		//end 获取bankid 用于判断全网还是小额 modified by lizhen 2014-05-21
		
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
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		modelMap.put(HFBusiDict.PROVCODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.PROVCODE)));
		modelMap.put(HFBusiDict.AREACODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.AREACODE)));
		modelMap.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		

		if(checkSignFlag==null || checkTradeFlag==null || cmd==null || message==null || captchaInfo==null){
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s","1127","会话已过期");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
			modelMap.put(DataDict.RET_CODE, "1127");
			String retMsg=messageService.getSystemParam("Captcha.Timeout.Msg");//交易超时
			modelMap.put(DataDict.RET_MSG, retMsg);
			return isXEBank ? WAP_XE_PAYRESULT : WAP_MW_PAYRESULT;
		}
		logInfo("SessionCheck Result Success[RetCode]:0000:session数据确认通过");
		
		modelMap.put("order", orderParam);
		modelMap.put(DataDict.MER_REQ_MOBILEID, mobileId);
		
		//2-验证码校验
		String captcha=reqMap.getStr("captcha");
		//2-1 判断是否已输入
		if(ObjectUtil.isEmpty(captcha)){
			String retMsg=messageService.getSystemParam("Captcha.PlsInput.Msg");//请输入验证码
			modelMap.put(DataDict.RET_MSG, retMsg);
			modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
			modelMap.put("mustInput", "1");
			return isXEBank ? WAP_XE_CONFIRMPAY : WAP_MW_CONFIRMPAY;
		}
		//2-2 判断是否已失效
		Timestamp now=new Timestamp(System.currentTimeMillis());
		String validTimeStr = messageService.getSystemParam("Captcha.ValidTime");//有效时间，单位为秒
		if(captchaInfo.isExpired(now,validTimeStr)){
			String retMsg=messageService.getSystemParam("Captcha.Invalid.Msg");//验证码失效，请重新获取
			modelMap.put(DataDict.RET_MSG, retMsg);
			modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
			return isXEBank ? WAP_XE_CONFIRMPAY : WAP_MW_CONFIRMPAY;
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
				modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
				modelMap.put("mustInput", "1");
				return isXEBank ? WAP_XE_CONFIRMPAY : WAP_MW_CONFIRMPAY;
			}
		}else{
			//验证码错误次数超限
			String retMsg=messageService.getSystemParam("Verify.MaxErrorTimes.Msg");//验证码错误次数超限，请重新获取验证码
			modelMap.put(DataDict.RET_MSG, retMsg);
			modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
			return isXEBank ? WAP_XE_CONFIRMPAY : WAP_MW_CONFIRMPAY;
		}
		logInfo("Captcha Result Success[RetCode]:0000:验证码校验通过");
		session.removeAttribute(DataDict.SEND_SMS_CAPTCHAINFO);//无论是否支付成功，只要验证通过了就删除session中的验证码信息
		session.removeAttribute("tradeMessage");
		session.removeAttribute(DataDict.MER_REQ_MOBILEID);
		session.removeAttribute(DataDict.FUNCODE_ORDER_PARAM);
		session.removeAttribute("wholeRetUrl");
		
		//2交易频率限制
		String checkRateResp = merAuthService.checkChannelPayRate(mobileId);
		if(!DataDict.SUCCESS_RET_CODE.equals(checkRateResp)){
			//支付频率控制未通过
			logInfo("PayRate Check Result Failed[RetCode]:%s:支付频率控制未通过",checkRateResp);
			modelMap.put(DataDict.RET_MSG, messageService.getMessageDetail(checkRateResp));
			modelMap.put(DataDict.RET_CODE, checkRateResp);
			return isXEBank ? WAP_XE_PAYRESULT : WAP_MW_PAYRESULT;
		}	
		logInfo("PayRate Check Result Success[RetCode]:0000:支付频率控制已通过");
		
		//start added by lizhen 
		//商户下单时间校验  禁止零点前发起请求但零点后支付（WAPZJZF）
		if(!DateUtil.verifyOrderDateStrict(cmd.getMerDate())){
			modelMap.put(DataDict.RET_MSG, messageService.getMessageDetail("1310"));
			modelMap.put(DataDict.RET_CODE, "1310");
			logInfo("VerifyOrderDate Result Failed[RetCode]:%s:%s", "1310","商户下单时间校验未通过");
			return WAP_MW_PAYRESULT;
		}
		logInfo("VerifyOrderDateStrict Result Success[RetCode]:0000:商户下单时间校验通过");
		//end added by lizhen
		
		//6-支付 0000和86011571均为支付成功
		cmd.setBusinessType(DataDict.BUSI_WAP_YZMZF);//业务区分，wap验证码支付（0208）
		MpspMessage orderResp = tradeService.wapDirectPay(cmd,mobileId,true);
		if(orderResp.isRetCode0000() || "86011571".equals(orderResp.getRetCode())){
			orderParam.setPlateDate(orderParam.getMerDate());
			orderParam.setSettleDate(orderParam.getMerDate());
			orderParam.setRetCode((String)orderResp.get(HFBusiDict.RETCODE));
			orderParam.setAmtType("02");
			orderParam.setRetUrl(cmd.getRetUrl());
			orderParam.setVersion(cmd.getVersion());
			String retUrl = this.genWholeRetUrl(orderParam);
			modelMap.put("wholeRetUrl", retUrl);
			logInfo("Info2Mer retUrl:%s", retUrl);
			
			String retMsg=messageService.getSystemParam("Verify.PaySuccess.Msg");//支付成功的提示语
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
		return isXEBank ? WAP_XE_PAYRESULT : WAP_MW_PAYRESULT;
	}
	
	private String genWholeRetUrl(OrderParam orderParam) {
		String retUrl = orderParam.getRetUrl();
		if (retUrl.indexOf("?") == -1){
			retUrl += "?";
		}else{
			retUrl += "&";
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(retUrl);
		String sign = super.platSign(orderParam.getPlainText());
		logInfo("Info2Mer Plain Text:%s", orderParam.getPlainText());
		logInfo("Info2Mer Sign Text:%s", sign);
		buffer.append(orderParam.getEncodedText(sign));
		return buffer.toString();
	}
	
	private String getRetMsg(MpspMessage orderResp){
		String retCode = orderResp.getRetCode();
		String retMsg="";
		retMsg=messageService.getSystemParam("Captcha."+orderResp.getRetCode()+".Msg");
		if("86011927".equals(retCode)){
			retMsg=messageService.getSystemParam("Verify."+orderResp.getRetCode()+".Msg");
		}
		if(ObjectUtil.isEmpty(retMsg)){
			retMsg=messageService.getSystemParam("Verify.PayFailure.Msg");//支付失败的默认提示语
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
		orderParam.setAmount(cmd.getAmount());
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
		return com.umpay.hfweb.dict.DataDict.FUNCODE_WAPYZMZF;
	}
}
