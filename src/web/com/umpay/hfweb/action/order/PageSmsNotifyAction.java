package com.umpay.hfweb.action.order;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.action.param.SmsParam;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  PageSmsNotifyAction
 * @author     :  zhaoyan 
 * description :  页面下单-请求短信重发处理类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class PageSmsNotifyAction extends BaseAbstractAction{

	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap){
		//1-系统升级提示信息
		String notice = messageService.getSystemParam("notice");
		if (ObjectUtil.isNotEmpty(notice)) { 
			modelMap.put("notice", notice);
		}
		//2-获取session数据
		HttpSession session = request.getSession();
		OrderParam sessionParam = (OrderParam)session.getAttribute("orderParam");
		SmsParam smsParam = (SmsParam)session.getAttribute("smsParam");
		if(sessionParam==null || smsParam==null){
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s", "1127", "会话已过期");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
			modelMap.put(DataDict.RET_CODE, "1127");
			return super.ERROR_PAGE;
		}
		logInfo("SessionCheck Result Success[RetCode]:0000:session数据确认通过");
		String provcode = ObjectUtil.trim(session.getAttribute(HFBusiDict.PROVCODE));
		String areacode = ObjectUtil.trim(session.getAttribute(HFBusiDict.AREACODE));
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		String bankId = ObjectUtil.trim((String)sessionParam.getBankId());
		modelMap.put(HFBusiDict.BANKID, bankId);
		modelMap.put(HFBusiDict.PROVCODE, provcode);
		modelMap.put(HFBusiDict.AREACODE, areacode);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		
		//3-判断短信发送次数
		if(smsParam.getSendCount() > Integer.valueOf(messageService.getSystemParam("SMS_SEND_LIMITS"))){
			logInfo("smsSendCountCheck Result Failed[RetCode]:%s:%s", "1309", "短信发送次数大于限定发送次数--SMS_SEND_LIMITS="+messageService.getSystemParam("SMS_SEND_LIMITS"));
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1309"));
			modelMap.put(DataDict.RET_CODE, "1309");
			return super.ERROR_PAGE;
		}
		logInfo("smsSendCountCheck Result Success[RetCode]:0000:短信发送次数确认通过，重发短信次数=" + smsParam.getSendCount());
		//4-获取订单信息
		MpspMessage orderInfoResp = restService.queryMerOrder(sessionParam.getMerId(), sessionParam.getMerDate(), sessionParam.getOrderId());
		if(!orderInfoResp.isRetCode0000()){
			logInfo("queryOrderByMobileId Result Failed[RetCode]:%s:%s", orderInfoResp.getRetCode(), "查询订单信息失败");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(orderInfoResp.getRetCode()));
			modelMap.put(DataDict.RET_CODE, orderInfoResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, orderInfoResp.getRetCodeBussi());
			return super.ERROR_PAGE;
		}
		logInfo("queryOrderByMobileId Result Success[RetCode]0000:查询订单信息成功");
		String orderState = String.valueOf(orderInfoResp.get(HFBusiDict.ORDERSTATE));
		String payRetCode = ObjectUtil.trim((String)orderInfoResp.get(HFBusiDict.RESERVED));//新增查询订单信息记录中的reserved字段：表示支付返回码
		
		String retView = "";
		if(bankId.startsWith("XE")){
			retView = "order/web_xe_payresult";
		}else if(bankId.startsWith("MW")){
			retView = "order/web_mw_payresult";
		}
		OrderParam orderParam = getPageParam(orderInfoResp, smsParam.getSmsSub(), sessionParam);
		//5-根据订单状态，判断是否下发短信以及跳转
		logInfo("订单状态 Info4Order orderState:%s", orderState);
		logInfo("订单支付返回码 Info4Order payRetCode:%s", payRetCode);
		if (orderState.equals("2") && payRetCode.equals(DataDict.SUCCESS_RET_CODE)) {
			if(bankId.startsWith("XE")){
				retView = "order/web_xe_tomer";
			}else if(bankId.startsWith("MW")){
				retView = "order/web_mw_tomer";
			}
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
			orderParam.setRetUrl(retUrl);
			//6-判断广告是否展示
			boolean advalid = merAuthService.canAccess("ShowAD", sessionParam.getMerId());
			modelMap.put("advalid", advalid);
		}else{
			Map<String, String> smsMap = getSmsMap(sessionParam.getGoodsName(), smsParam,
					orderInfoResp);
			//调用下发短信接口
			smsService.pushPaySms(smsMap);
			smsParam.setSendCount(smsParam.getSendCount()+1);
			session.setAttribute("smsParam", smsParam);
		}
		modelMap.put("isLevel2", super.isLevel2Mer(sessionParam.getMerId()));
		modelMap.put("order", orderParam);
		//added by wanghaiwei 2012-11-12
		modelMap.put("smsSub", smsParam.getSmsSub());
		modelMap.put("exAction", "pageSmsNotifyAction");
		//end
		modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		return retView;

	}
	/**
	 * ********************************************
	 * method name   : getSmsMap 
	 * description   : 组装短信发送的数据
	 * @return       : Map<String,String>
	 * @param        : @param sessionParam
	 * @param        : @param smsParam
	 * @param        : @param orderInfoResp
	 * @param        : @param bankId
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 22, 2011 4:00:26 PM
	 * @see          : 
	 * *******************************************
	 */
	private Map<String, String> getSmsMap(String goodsName,
			SmsParam smsParam, MpspMessage orderInfoResp) {
		Map<String, String> smsMap = new HashMap<String, String>();
		smsMap.put(HFBusiDict.CALLED, messageService.getSystemParam("smsPrex"));//10658008
		smsMap.put(HFBusiDict.GOODSNAME, goodsName);
		smsMap.put(HFBusiDict.BANKID, (String)orderInfoResp.get(HFBusiDict.BANKID));
		smsMap.put(HFBusiDict.GOODSID, (String)orderInfoResp.get(HFBusiDict.GOODSID));
		smsMap.put(HFBusiDict.MERID, (String)orderInfoResp.get(HFBusiDict.MERID));
		smsMap.put(HFBusiDict.SERVTYPE, smsParam.getServType());
		smsMap.put(HFBusiDict.CALLING, (String)orderInfoResp.get(HFBusiDict.MOBILEID));
		if(ObjectUtil.isNotEmpty(smsParam.getServMonth())){
			smsMap.put(HFBusiDict.SERVMONTH, smsParam.getServMonth());
		}
		smsMap.put(HFBusiDict.AMOUNT, String.valueOf(orderInfoResp.get(HFBusiDict.AMOUNT)));
		smsMap.put(HFBusiDict.VERIFYCODE, String.valueOf(orderInfoResp.get(HFBusiDict.VERIFYCODE)));
		smsMap.put(HFBusiDict.PORDERID, smsParam.getSmsFrom());//长号码
		return smsMap;
	}
	/**
	 * ********************************************
	 * method name   : getPageParam 
	 * description   : 组装页面参数
	 * @return       : OrderParam
	 * @param        : @param orderInfoResp
	 * @param        : @param smsSub
	 * @param        : @param merName
	 * @param        : @param tableName
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 10, 2011 8:05:54 PM
	 * @see          : 
	 * *******************************************
	 */
	private OrderParam getPageParam(MpspMessage orderInfoResp, String smsSub, OrderParam sessionParam) {
		OrderParam orderParam = new OrderParam();
		orderParam.setOrderId((String)orderInfoResp.get(HFBusiDict.ORDERID));
		orderParam.setMerId((String)orderInfoResp.get(HFBusiDict.MERID));
		orderParam.setMerName(sessionParam.getMerName());
		orderParam.setMerName2(sessionParam.getMerName2());
		orderParam.setMerDate((String)orderInfoResp.get(HFBusiDict.ORDERDATE));
		orderParam.setAmount(String.valueOf(orderInfoResp.get(HFBusiDict.AMOUNT)));
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar(orderParam.getAmount()));
		orderParam.setTableName(sessionParam.getTableName());
		orderParam.setGoodsId(sessionParam.getGoodsId());
		orderParam.setGoodsName(sessionParam.getGoodsName());
		orderParam.setGoodsName2(sessionParam.getGoodsName2());
		orderParam.setServType(sessionParam.getServType());
		orderParam.setCusPhone(sessionParam.getCusPhone());
		String orderState = String.valueOf(orderInfoResp.get(HFBusiDict.ORDERSTATE));
		orderParam.setOrderState(orderState);
		orderParam.setPayRetCode(ObjectUtil.trim((String)orderInfoResp.get(HFBusiDict.RESERVED)));//支付返回码
		
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
		return com.umpay.hfweb.dict.DataDict.FUNCODE_PAGE_SMSRESEND;
	}
}
