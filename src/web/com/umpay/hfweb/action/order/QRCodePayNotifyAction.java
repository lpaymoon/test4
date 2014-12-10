package com.umpay.hfweb.action.order;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bs.mpsp.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.action.param.SmsParam;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.util.CommonUtil;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  QRCodePayNotifyAction
 * @author     :  lizhen 
 * description :  商户请求页面通过二维码支付，前台支付结果通知处理类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class QRCodePayNotifyAction extends BaseAbstractAction{

	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap){
		//1-系统升级提示信息
		String notice = messageService.getSystemParam("notice");
		if (ObjectUtil.isNotEmpty(notice)) { 
			modelMap.put("notice", notice);
		}
		//2-获取session数据
		HttpSession session = request.getSession();
		MpspMessage message = (MpspMessage)session.getAttribute("tradeMessage");
		PageOrderCmd cmd = (PageOrderCmd)session.getAttribute(DataDict.FUNCODE_ORDER_PARAM);
//		OrderParam sessionParam = (OrderParam)session.getAttribute("orderParam");
		if(message==null){
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s", "1127", "会话已过期");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
			modelMap.put(DataDict.RET_CODE, "1127");
			return super.ERROR_PAGE;
		}
		logInfo("SessionCheck Result Success[RetCode]:0000:session数据确认通过");
		String provcode = ObjectUtil.trim((String)message.get(HFBusiDict.PROVCODE));
		String areacode = ObjectUtil.trim((String)message.get(HFBusiDict.AREACODE));
		String bankId = ObjectUtil.trim((String)message.get(HFBusiDict.BANKID));
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15（来自PagePayNotifyAction）
		modelMap.put(HFBusiDict.PROVCODE, provcode);
		modelMap.put(HFBusiDict.AREACODE, areacode);
		modelMap.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		OrderParam orderParam=getReqParam(cmd,message);
		//3-区分小额 全网
		String retView = "";
		if(bankId.startsWith("XE")){
			retView = "order/web_xe_confirmpay";
		}else if(bankId.startsWith("MW")){
			retView = "order/web_mw_confirmpay";
		}
		//4-查询订单信息
		MpspMessage orderInfoResp = restService.queryMerOrder(cmd.getMerId(), cmd.getMerDate(), cmd.getOrderId());
		if(!orderInfoResp.isRetCode0000()){
			//订单不存在时留在原页面
			if("86001101".equals(orderInfoResp.getRetCode())){
				logInfo("queryOrderByMobileId Result Failed[RetCode]:%s:%s", orderInfoResp.getRetCode(), "订单不存在");
				//二维码数据
				String qrCodeUrl=messageService.getSystemParam("QRCode.URL","");
				String qrCodeAccess=messageService.getSystemParam("QRCode.ACCESS","");
				if(CommonUtil.isQRCodeAccess(qrCodeAccess,cmd.getMerId()) && !"".equals(qrCodeUrl)){
					qrCodeUrl=this.getQRCodeURL(qrCodeUrl,cmd);
					modelMap.put("qrCodeUrl", qrCodeUrl);
				}
				modelMap.put("order", orderParam);
				return retView;

			}else{
				logInfo("queryOrderByMobileId Result Failed[RetCode]:%s:%s", orderInfoResp.getRetCode(), "查询订单信息失败");
				modelMap.put(DataDict.RET_CODE, orderInfoResp.getRetCode());
				modelMap.put(DataDict.RET_CODE_BUSSI, orderInfoResp.getRetCodeBussi());
				modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(orderInfoResp.getRetCode()));
				return super.ERROR_PAGE;
			}
		}
		logInfo("queryOrderByMobileId Result Success[RetCode]0000:查询订单信息成功");
		String orderState = String.valueOf(orderInfoResp.get(HFBusiDict.ORDERSTATE));
		String payRetCode = StringUtil.trim((String)orderInfoResp.get(HFBusiDict.RESERVED));//新增查询订单信息记录中的reserved字段：表示支付返回码
		//非二维码支付，走统一页面支付流程
		if(!DataDict.BUSI_QRCODE.equals(orderInfoResp.get(HFBusiDict.BUSINESSTYPE))){
			if(bankId.startsWith("XE")){
				retView = "order/web_xe_payresult";
			}else if(bankId.startsWith("MW")){
				retView = "order/web_mw_payresult";
			}
			SmsParam smsParam = (SmsParam)session.getAttribute("smsParam");
			//smsParam必须在判断是否为二维码支付后判断
			if(smsParam==null){
				logInfo("SessionCheck Result Failed[RetCode]:%s:%s", "1127", "会话已过期");
				modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
				modelMap.put(DataDict.RET_CODE, "1127");
				return super.ERROR_PAGE;
			}
			String smsSub = "";
			if("XE791000".equals(bankId)){
				smsSub = messageService.getSystemParam("XE791000.smsSub");
			}else{
				smsSub = smsParam.getSmsSub();
			}
			logInfo("QRCodePayNotifyAction Result 页面展示需要的短信长号码[smsSub]:%s", smsSub);
			modelMap.put("smsSub", smsSub);
		}else{
			//二维码数据
			String qrCodeUrl=messageService.getSystemParam("QRCode.URL","");
			String qrCodeAccess=messageService.getSystemParam("QRCode.ACCESS","");
			if(CommonUtil.isQRCodeAccess(qrCodeAccess,cmd.getMerId()) && !"".equals(qrCodeUrl)){
				qrCodeUrl=this.getQRCodeURL(qrCodeUrl,cmd);
				modelMap.put("qrCodeUrl", qrCodeUrl);
			}
			//判断是否显示验证码
			String authCodeMeridList = messageService.getSystemParam("AuthCode.SHOW","");
			boolean isAuthCodeMer = authCodeMeridList.indexOf(cmd.getMerId()) != -1;
			if(isAuthCodeMer){
				modelMap.put("isAuthCodeMer", "true");
			}
		}
		
		//5-根据订单状态，判断是否下发短信以及跳转	
		logInfo("订单状态 Info4Order orderState:%s", orderState);
		logInfo("订单支付返回码 Info4Order payRetCode:%s", payRetCode);
		if (orderState.equals("2") && payRetCode.equals(DataDict.SUCCESS_RET_CODE)) {//订单支付成功 orderstate=2且payretcode=0000
			//5-1-1 订单支付完成，跳转至通知商户页面
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
			orderParam = getPageParam(cmd,message,orderInfoResp);
			String retUrl = this.genWholeRetUrl(orderParam);
			logInfo("Info2Mer retUrl:%s", retUrl);
			orderParam.setRetUrl(retUrl);
		}
		
		boolean isLevel2Mer=super.isLevel2Mer(cmd.getMerId());
		modelMap.put("isLevel2", isLevel2Mer);
		if(isLevel2Mer){
			String merName2 = messageService.getSystemParam("unRealMerName");
			String goodsName2 = messageService.getSystemParam("unRealGoodsName");
			orderParam.setMerName2(merName2);
			orderParam.setGoodsName2(goodsName2);
		}
		orderParam.setOrderState(orderState);
		orderParam.setPayRetCode(payRetCode);
		modelMap.put("order", orderParam);
		modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		modelMap.put("exAction", "qrCodePayNotifyAction");
		return retView;
	}
	/**
	 * *****************  方法说明  *****************
	 * method name   :  getPageParam
	 * @param		 :  @param cmd
	 * @param		 :  @param message
	 * @param		 :  @param orderInfoResp
	 * @param		 :  @return
	 * @return		 :  OrderParam
	 * @author       :  LiZhen 2014-6-18 下午7:34:31
	 * description   :  
	 * @see          :  
	 * **********************************************
	 */
	private OrderParam getReqParam(PageOrderCmd cmd,MpspMessage message) {
		OrderParam orderParam = new OrderParam();
		orderParam.setGoodsId(cmd.getGoodsId());
		orderParam.setAmount(cmd.getAmount());
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar(cmd.getAmount()));
		orderParam.setMerName((String)message.get(HFBusiDict.MERNAME));
		orderParam.setGoodsName((String)message.get(HFBusiDict.GOODSNAME));
		orderParam.setMerId(cmd.getMerId());
		orderParam.setServType(String.valueOf(message.get(HFBusiDict.SERVTYPE)));
		orderParam.setServMonth(String.valueOf(message.get(HFBusiDict.SERVMONTH)));
		orderParam.setCusPhone((String)message.get(HFBusiDict.CUSPHONE));
		orderParam.setMobileId(cmd.getMobileId());
		orderParam.setOrderId(cmd.getOrderId());
		orderParam.setMerDate(cmd.getMerDate());
		return orderParam;
	}
	
	/**
	 * ********************************************
	 * method name   : getPageParam 
	 * description   : 组装订单参数
	 * @return       : OrderParam
	 * @param        : @param orderInfoResp
	 * @param        : @param smsSub
	 * @param        : @param sessionParam
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 22, 2011 11:22:02 AM
	 * @see          : 
	 * *******************************************
	 */
	private OrderParam getPageParam(PageOrderCmd cmd,MpspMessage message,MpspMessage orderInfoResp) {
		OrderParam orderParam = new OrderParam();
		orderParam.setMerId((String)orderInfoResp.get(HFBusiDict.MERID));
		orderParam.setGoodsId((String)orderInfoResp.get(HFBusiDict.GOODSID));
		orderParam.setOrderId((String)orderInfoResp.get(HFBusiDict.ORDERID));
		orderParam.setMerName((String)message.get(HFBusiDict.MERNAME));
		orderParam.setGoodsName((String)message.get(HFBusiDict.GOODSNAME));
		orderParam.setMerDate((String)orderInfoResp.get(HFBusiDict.ORDERDATE));
		orderParam.setAmount(String.valueOf(orderInfoResp.get(HFBusiDict.AMOUNT)));
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar(String.valueOf(orderInfoResp.get(HFBusiDict.AMOUNT))));
		orderParam.setAmtType(cmd.getAmtType());
		orderParam.setMobileId(String.valueOf(orderInfoResp.get(HFBusiDict.MOBILEID)));
		orderParam.setServMonth(String.valueOf(orderInfoResp.get(HFBusiDict.SERVMONTH)));
		orderParam.setOrderState(String.valueOf(orderInfoResp.get(HFBusiDict.ORDERSTATE)));
		orderParam.setPayRetCode(StringUtil.trim((String)orderInfoResp.get(HFBusiDict.RESERVED)));//支付返回码
		orderParam.setBankId(ObjectUtil.trim((String)message.get(HFBusiDict.BANKID)));
		orderParam.setBankType(cmd.getBankType());
		orderParam.setVersion(cmd.getVersion());
		orderParam.setMerPriv(cmd.getMerPriv());
		orderParam.setCusPhone(ObjectUtil.trim((String)message.get(HFBusiDict.CUSPHONE)));
		orderParam.setServType(String.valueOf(message.get(HFBusiDict.SERVTYPE)));
		orderParam.setRetUrl(cmd.getRetUrl());
		orderParam.setPlateDate((String)orderInfoResp.get(HFBusiDict.PLATDATE));
		orderParam.setSettleDate((String)orderInfoResp.get(HFBusiDict.PLATDATE));
		orderParam.setRetCode((String)orderInfoResp.get(HFBusiDict.RETCODE));
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
	/**
	 * *****************  方法说明  *****************
	 * method name   :  getQRCodeURL
	 * @param		 :  @param url
	 * @param		 :  @param cmd
	 * @param		 :  @return
	 * @return		 :  String
	 * @author       :  LiZhen 2014-6-19 下午3:59:52
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
		//前台支付结果通知功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_PAGE_NOTIFY;
	}
}
