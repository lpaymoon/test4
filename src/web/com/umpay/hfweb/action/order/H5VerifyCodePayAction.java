package com.umpay.hfweb.action.order;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.ehcache.Element;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.action.command.H5verifyCodePayCmd;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.CommonEcc;
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
public class H5VerifyCodePayAction extends BaseAbstractAction{
	private final String H5_REQUEST = "order/wap_crcode_verify_request";
	private final String H5_PAYRESULT = "order/wap_crcode_verify_payresult";
	private final String H5_ERROR = "order/wap_crcode_error";
	private static final String Sended_YZM_Count= "yzmCount"; //获取短信验证码次数
	protected String processBussiness(HttpServletRequest request,HttpServletResponse response,Map<String,Object> modelMap){
		//1-校验session
		RequestMsg reqMap = new RequestMsg();
		reqMap.putAllParam(HttpUtil.parseRequestParam(request));
		reqMap.setFunCode(this.getFunCode());
		HttpSession session = request.getSession();	

		H5verifyCodePayCmd cmd=(H5verifyCodePayCmd)request.getSession().getAttribute(DataDict.FUNCODE_ORDER_PARAM);
		if(cmd==null){
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s","1127","会话已过期");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
			modelMap.put(DataDict.RET_CODE, "1127");
			String retMsg=messageService.getSystemParam("Captcha.Timeout.Msg");//交易超时
			modelMap.put(DataDict.RET_MSG, retMsg);
			return H5_ERROR;
		}
		OrderParam orderParam = (OrderParam)session.getAttribute("order");
		String mobileId=(String)reqMap.get(DataDict.MER_REQ_MOBILEID);
		String orderid=(String)session.getAttribute(HFBusiDict.ORDERID);
		String merDate=(String)session.getAttribute(HFBusiDict.ORDERDATE);
		String goodsName=(String)request.getSession().getAttribute("goodsName");
		String wholeRetUrl=(String)session.getAttribute("wholeRetUrl");
		modelMap.put("wholeRetUrl", wholeRetUrl);
		String merId=ObjectUtil.trim(cmd.getMerId());
		logInfo("SessionCheck Result Failed[RetCode]:%s:%s","1127","会话已过期");
		//2 校验访问权限
		if(!merAuthService.canAccess("H5YZM", merId)){
			//商户未开通此项支付服务
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1128"));
			modelMap.put(DataDict.RET_CODE, "1128");
			logInfo("MerAuthCheck merId["+cmd.getMerId()+"] Result Failed[RetCode]:1128:商户访问权限校验未通过");	
			return H5_ERROR;
		}
		logInfo("MerAuthCheck Result Success[RetCode]:0000:商户访问权限校验已通过");

		String verifycode=ObjectUtil.trim(reqMap.getStr("captcha"));
		reqMap.put(DataDict.MER_REQ_ORDERID, orderid);
		reqMap.put(DataDict.MER_REQ_VERIFYCODE, verifycode);
		reqMap.put(DataDict.MER_REQ_MERDATE, merDate);
		reqMap.put(DataDict.MER_REQ_MERID, merId);
		reqMap.put(DataDict.MER_REQ_MOBILEID, mobileId);
		//3-验证码校验
		MpspMessage respMessage = restService.checkVerifyCode(reqMap);
		if(!respMessage.isRetCode0000()){
			logInfo("验证码校验失败,retCode=%s", respMessage.getRetCode());
			String retMsg=ObjectUtil.trim((String) respMessage.get(DataDict.RET_MSG));//验证码错误，请重新输入
			modelMap.put(DataDict.RET_MSG,retMsg );
			if(retMsg.equals("")||!retMsg.contains("验证码")){
			    modelMap.put(DataDict.RET_MSG, "验证码校验失败");
			}
			modelMap.put(DataDict.RET_CODE, respMessage.getRetCode());
			request.getSession().setAttribute(DataDict.MER_REQ_MOBILEID, mobileId);
			request.getSession().setAttribute(HFBusiDict.ORDERID, orderid);
			request.getSession().setAttribute(HFBusiDict.ORDERDATE, merDate);
			request.getSession().setAttribute("goodsName", goodsName);
			request.getSession().setAttribute("order", orderParam);
			request.getSession().setAttribute(DataDict.FUNCODE_ORDER_PARAM, cmd);	
			return H5_REQUEST;
		}
		logInfo("Captcha Result Success[RetCode]:0000:验证码校验通过");
		//5-查询订单信息，获取订单验证码和porderid
		MpspMessage QueryOrderResp=restService.queryMerOrder(merId, merDate, orderid);				
		if(!QueryOrderResp.isRetCode0000()){//订单不存在
			logInfo("QueryOrderResp Result Success[RetCode]:0000:查询订单失败");
		    modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1139"));
			modelMap.put(DataDict.RET_CODE, respMessage.getRetCode());
			return H5_ERROR;
		}
		//6-支付
		reqMap.put(HFBusiDict.BUSINESSTYPE, DataDict.BUSI_WAP_CRCODE_YZMZF);//业务区分：渠道验证码下单，需增加新的类型
		
		MpspMessage payResp = tradeService.qdVerifyPay(reqMap,QueryOrderResp);
		if(!payResp.isRetCode0000()&&!payResp.getRetCode().equals("86011571")){
			logInfo("支付失败[RetCode]:%s",payResp.getRetCode());
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(payResp.getRetCode()));
			modelMap.put(DataDict.RET_CODE, respMessage.getRetCode());
			return H5_ERROR;
		}
		// 3-下发支付成功通知短信
		smsService.pushPayOkSms(payResp.getWrappedMap());
		merId=payResp.getStr(HFBusiDict.MERID);	
		mobileId=payResp.getStr(HFBusiDict.MOBILEID);
		String send_promotion=ObjectUtil.trim(payResp.getStr("send_mt_promotion"));
		if(send_promotion.equals("true")){ //下发限额提示短信
			String smsContent=payResp.getStr("promotion_msg");
			 smsService.pushSms(merId, mobileId, smsContent);
		}
		
		//清除手机号码缓存，允许继续获取验证码
		CommonEcc ecc = (CommonEcc)AbstractCacheFactory.getInstance().getCacheClient("wapYzmCode");
		ecc.remove(mobileId);
			
		modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		modelMap.put(DataDict.RET_MSG, "购买【"+goodsName+"】费用为【"+orderParam.getAmount4dollar()+"】元");
		logInfo("支付成功[RetCode]:0000");
		return H5_PAYRESULT;
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
		return com.umpay.hfweb.dict.DataDict.FUNCODE_H5YZMZF;
	}
}
