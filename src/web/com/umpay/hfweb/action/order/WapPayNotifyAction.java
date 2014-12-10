package com.umpay.hfweb.action.order;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bs.mpsp.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.action.param.SmsParam;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  WapPayNotifyAction
 * @author     :  zhaoyan 
 * description :  Wap版下单-购买完成，前台支付结果通知处理类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class WapPayNotifyAction extends BaseAbstractAction{

	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap) {
		//1-获取session中的数据
		HttpSession session = request.getSession();
		OrderParam sessionParam = (OrderParam)session.getAttribute("orderParam");
		SmsParam smsParam = (SmsParam)session.getAttribute("smsParam");
		if(sessionParam==null || smsParam==null){
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s", "1127", "会话已过期");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
			modelMap.put(DataDict.RET_CODE, "1127");
			return super.ERROR_WAP;
		}
		logInfo("SessionCheck Result Success[RetCode]:0000:session数据确认通过");
		String provcode = ObjectUtil.trim(session.getAttribute(HFBusiDict.PROVCODE));
		String areacode = ObjectUtil.trim(session.getAttribute(HFBusiDict.AREACODE));
		String bankId = ObjectUtil.trim(sessionParam.getBankId());
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		modelMap.put(HFBusiDict.PROVCODE, provcode);
		modelMap.put(HFBusiDict.AREACODE, areacode);
		modelMap.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		
		//2-获取订单信息
		MpspMessage orderInfoResp = restService.queryMerOrder(sessionParam.getMerId(), sessionParam.getMerDate(), sessionParam.getOrderId());
		if(!orderInfoResp.isRetCode0000()){
			logInfo("queryOrderByMobileId Result Failed[RetCode]:%s:%s", orderInfoResp.getRetCode(), "查询订单信息失败");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(orderInfoResp.getRetCode()));
			modelMap.put(DataDict.RET_CODE, orderInfoResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, orderInfoResp.getRetCodeBussi());
			return super.ERROR_WAP;
		}
		logInfo("queryOrderByMobileId Result Success[RetCode]0000:查询订单信息成功");
		String orderState = String.valueOf(orderInfoResp.get(HFBusiDict.ORDERSTATE));
		String payRetCode = StringUtil.trim((String)orderInfoResp.get(HFBusiDict.RESERVED));//新增查询订单信息记录中的reserved字段：表示支付返回码
		logInfo("订单状态 Info4Order orderState:%s", orderState);
		logInfo("订单支付返回码 Info4Order payRetCode:%s", payRetCode);
		
		//查看form表单是由哪个按钮提交的，submitBtn=retMer表示由返回商家按钮提交
		String submitBtn=request.getParameter("submitBtn");
		if (orderState.equals("2") && payRetCode.equals(DataDict.SUCCESS_RET_CODE)) {
			try {
				session.invalidate();
			} catch (Exception e) {
				logger.error("session 置为无效之前已经失效==="+e);
			}
			sessionParam.setPlateDate((String)orderInfoResp.get(HFBusiDict.PLATDATE));
			sessionParam.setSettleDate((String)orderInfoResp.get(HFBusiDict.PLATDATE));
			sessionParam.setRetCode((String)orderInfoResp.get(HFBusiDict.RETCODE));
			String retUrl = this.genWholeRetUrl(sessionParam);
			logInfo("Info2Mer retUrl:%s", retUrl);
			try {
				response.sendRedirect(retUrl);
				modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
				//start 订单支付成功状态后，标注用户点击返回商家或购买完成按钮返回商家。modified by lizhen 2014-05-28
				if(submitBtn!=null && "retMer".equals(submitBtn)){
					logger.info("Wap下单，支付成功，用户点击返回商家直接跳转回商户结果通知页面");
				}else{
					logger.info("Wap下单，支付成功，用户点击购买完成直接跳转回商户结果通知页面");
				}
				//end 订单支付成功状态后，标注用户点击返回商家或购买完成按钮返回商家。modified by lizhen 2014-05-28
				
				return null;
			} catch (IOException e) {
				logger.error(e);
				modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(DataDict.SYSTEM_ERROR_CODE));
				modelMap.put(DataDict.RET_CODE, DataDict.SYSTEM_ERROR_CODE);
				return super.ERROR_WAP;
			}
		}
		//start 订单非成功状态时，点击返回商家按钮，retcode为9999。added by lizhen 2014-05-28
		if(submitBtn!=null && "retMer".equals(submitBtn)){
			String errorRetUrl = (String)session.getAttribute("errorRetUrl");
			try {
				session.invalidate();
			} catch (Exception e) {
				logger.error("session 置为无效之前已经失效==="+e);
			}
			try {
				response.sendRedirect(errorRetUrl);
				modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
				if(orderState.equals("3")){
					logger.info("Wap下单，支付失败，用户点击返回商家跳转回商户结果通知页面");
				}else if(orderState.equals("0")){
					logger.info("Wap下单，尚未支付，用户点击返回商家跳转回商户结果通知页面");
				}
				return null;
			} catch (IOException e) {
				logger.error(e);
				modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(DataDict.SYSTEM_ERROR_CODE));
				modelMap.put(DataDict.RET_CODE, DataDict.SYSTEM_ERROR_CODE);
				return super.ERROR_WAP;
			}
		}
		//end 订单非成功状态时，点击返回商家按钮，retcode为9999。added by lizhen 2014-05-28
		OrderParam orderParam = getPageParam(orderInfoResp, smsParam.getSmsSub(), sessionParam.getMerName());
		modelMap.put("order", orderParam);
		modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		String retView = "";
		if(sessionParam.getBankId().startsWith("XE")){
			retView = "order/wap_payresult";
		}else if(sessionParam.getBankId().startsWith("MW")){
			retView = "order/wap_mw_payresult";
		}
		return retView;
	}
	private OrderParam getPageParam(MpspMessage orderInfoResp, String smsSub, String merName) {
		OrderParam orderParam = new OrderParam();
		orderParam.setOrderId((String)orderInfoResp.get(HFBusiDict.ORDERID));
		orderParam.setMerId((String)orderInfoResp.get(HFBusiDict.MERID));
		orderParam.setMerName(merName);
		orderParam.setMerDate((String)orderInfoResp.get(HFBusiDict.ORDERDATE));
		String orderState = String.valueOf(orderInfoResp.get(HFBusiDict.ORDERSTATE));
		orderParam.setOrderState(orderState);
		String payRetCode = StringUtil.trim((String)orderInfoResp.get(HFBusiDict.RESERVED));
		orderParam.setPayRetCode(payRetCode);
		String nextDirect = "";
		if (orderState.equals("0")) {
			nextDirect = "您尚未回复订单支付的确认短信，请注意查收“" + smsSub + "”下发的提示短信，并按短信提示进行回复，即可完成支付";
		}else if(orderState.equals("1")){
			nextDirect = "系统正在受理您的支付请求，请注意查收“" + smsSub + "”下发的短信，感谢您耐心等待";
		}else if(orderState.equals("2") && payRetCode.equals(DataDict.SUCCESS_RET_CODE)){
			nextDirect = "请注意查收“" + smsSub + "”支付信息短信";
		}else{
			nextDirect = "订单支付失败！请查收“" + smsSub + "”下发的通知短信，如提示回复错误，重新回复后点击“购买完成”即可完成支付。其他失败原因请咨询4006125880";
		}
		orderParam.setNextDirect(nextDirect);
		return orderParam;
	}
	/**
	 * ********************************************
	 * method name   : genWholeRetUrl 
	 * description   : 组装完整的通知URL
	 * @return       : String
	 * @param        : @param cmd
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 7, 2011 9:05:28 PM
	 * @see          : 
	 * *******************************************
	 */
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
		//WAP版支付结果通知功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_WAP_NOTIFY;
	}
}
