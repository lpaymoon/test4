package com.umpay.hfweb.action.order;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;

import com.bs.mpsp.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.util.CaptchaServiceSingleton;
import com.umpay.hfweb.util.MessageUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  PageReqNoMblAction
 * @author     :  zhaoyan 
 * description :  页面下单-接收带手机号页面请求处理类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class PageReqNoMblAction extends BaseAbstractAction{
	private MessageSource validateSource;
	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap){
		//1-系统升级提示信息
		String notice = messageService.getSystemParam("notice");
		if (ObjectUtil.isNotEmpty(notice)) {
			modelMap.put("notice", notice);
		}
		//2-获取session中页面所需数据，判断session是否过期
		OrderParam orderPageParam = (OrderParam)request.getSession().getAttribute("orderPageParam");
		PageOrderCmd cmd = (PageOrderCmd)request.getSession().getAttribute(DataDict.FUNCODE_ORDER_PARAM);
		if(orderPageParam==null || cmd==null){
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s","1127","会话已过期");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
			modelMap.put(DataDict.RET_CODE, "1127");
			return super.ERROR_PAGE;
		}
		logInfo("SessionCheck Result Success[RetCode]:0000:session数据确认通过");
		//3-将二级商户信息放入response
		modelMap.put("isLevel2", super.isLevel2Mer(orderPageParam.getMerId()));	
		//4-校验手机号
		String userMobileId = StringUtil.trim(request.getParameter("userMobileId"));
		String mobileIdRegex = MessageUtil.getLocalProperty(validateSource,"REGEXP."+DataDict.MER_REQ_MOBILEID);
		String random = ObjectUtil.trim((String)request.getParameter("j_captcha_response"));
		if (!Pattern.matches(mobileIdRegex, userMobileId)) {
			logInfo("mobileIdRegex Result Failed[RetCode]:%s:%s","1122","接收用户的手机号校验失败");
			modelMap.put("mobileIdError","true");
			modelMap.put("userMobileId", userMobileId);
			modelMap.put("order", orderPageParam);
			modelMap.put(DataDict.RET_CODE, "1122");
			modelMap.put("mobileIdRegex", mobileIdRegex);
			return "order/web_confirmPayNoMbl";
		}
		logInfo("mobileIdRegex Result Success[RetCode]:0000:接收用户的手机号校验通过");
		String key = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID)+"-wrongNum";
		//5-校验验证码
		if(!CaptchaServiceSingleton.getInstance().validateCaptchaResponse(random, request.getSession())){
			Object num = request.getSession().getAttribute(key);
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
				request.getSession().removeAttribute(key);
				return super.ERROR_PAGE;
			}
			request.getSession().setAttribute(key,wrongNum);
			
			logInfo("validateCaptchaResponse Result Failed[RetCode]:%s:%s","1307","验证码校验失败");
			modelMap.put("jcaptchaError","true");
			modelMap.put("userMobileId", userMobileId);
			modelMap.put("order", orderPageParam);
			modelMap.put("mobileIdRegex", mobileIdRegex);
			modelMap.put(DataDict.RET_CODE, "1307");
			return "order/web_confirmPayNoMbl";
		}
		logInfo("validateCaptchaResponse Result Success[RetCode]:0000:验证码校验通过");
		request.getSession().removeAttribute(key);
		request.getSession().removeAttribute("orderPageParam");
		PageOrderCmd newCmd = new PageOrderCmd(cmd, userMobileId);
		//6-交易鉴权
		boolean isCheckTrade = false;
		MpspMessage checkTradeResp = restService.checkTrade(userMobileId, orderPageParam.getMerId(), orderPageParam.getGoodsId());
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
		//7-确认可支付银行
		String bankId = ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.BANKID));
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
		//交易鉴权信息
		request.getSession().setAttribute("tradeMessage", checkTradeResp);
		//交易鉴权标志
		request.getSession().setAttribute(DataDict.CHECK_TRADE_FLAG, isCheckTrade);
		//old:根据bankId定义跳转地址（小额支付，全网支付）
		//new:验证码和手机号输入正确直接完成下单，跳转到回复2次确认页面。
//		String retView = "";
//		if(bankId.startsWith("XE")){
//			retView = "order/web_xe_confirmpay";
//		}else if(bankId.startsWith("MW")){
//			retView = "order/web_mw_confirmpay";
//		}
		orderPageParam.setMobileId(userMobileId);
		modelMap.put("order", orderPageParam);
		request.getSession().setAttribute(DataDict.FUNCODE_ORDER_PARAM, newCmd);
		modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
//		return retView;

		//验证码和手机号输入正确直接完成下单，跳转到回复2次确认页面。
		//转到 saveOrder.do --> 显示 web_xe_payresult.jsp 或 web_mw_payresult.jsp
		return "forward:/pay/saveOrder.do";
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
		//不带手机号页面下单的功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_PAGE_XDNOMBL;
	}
	public void setValidateSource(MessageSource validateSource) {
		this.validateSource = validateSource;
	}
}
