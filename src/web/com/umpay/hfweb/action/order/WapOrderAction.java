package com.umpay.hfweb.action.order;

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
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  WapOrderAction
 * @author     :  zhaoyan 
 * description :  WAP版下单-确认支付，生成订单处理类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class WapOrderAction extends BaseAbstractAction{

	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap){
		//1-取得session数据，判断是否验签，是否交易鉴权
		HttpSession session = request.getSession();
		Boolean checkSignFlag = (Boolean)session.getAttribute(DataDict.CHECK_SIGN_FLAG);
		Boolean checkTradeFlag = (Boolean)session.getAttribute(DataDict.CHECK_TRADE_FLAG);
		PageOrderCmd cmd = (PageOrderCmd)session.getAttribute(DataDict.FUNCODE_ORDER_PARAM);
		MpspMessage message = (MpspMessage)session.getAttribute("tradeMessage");
		session.removeAttribute(DataDict.FUNCODE_ORDER_PARAM);
		session.removeAttribute("tradeMessage");
		session.removeAttribute(DataDict.CHECK_SIGN_FLAG);
		session.removeAttribute(DataDict.CHECK_TRADE_FLAG);
		if(checkSignFlag==null || checkTradeFlag==null || cmd==null || message==null){
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s","1127","会话已过期");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
			modelMap.put(DataDict.RET_CODE, "1127");
			return super.ERROR_WAP;
		}
		logInfo("SessionCheck Result Success[RetCode]:0000:session数据确认通过");
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		String bankId = (String)message.get(HFBusiDict.BANKID);
		modelMap.put(HFBusiDict.PROVCODE, ObjectUtil.trim((String)message.get(HFBusiDict.PROVCODE)));
		modelMap.put(HFBusiDict.AREACODE, ObjectUtil.trim((String)message.get(HFBusiDict.AREACODE)));
		modelMap.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		
		//2-判断是否验签，是否交易鉴权
		if(!checkSignFlag || !checkTradeFlag){
			logInfo("checkSignFlag And CheckTradeFlag Result Failed[RetCode]:%s:%s", DataDict.SYSTEM_ERROR_CODE, "验签或交易鉴权未通过，终止下单");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(DataDict.SYSTEM_ERROR_CODE));
			modelMap.put(DataDict.RET_CODE, DataDict.SYSTEM_ERROR_CODE);
			return super.ERROR_WAP;
		}
		logInfo("checkSignFlag And CheckTradeFlag Result Success[RetCode]:0000:签名和交易鉴权确认通过");
		//3-交易屏蔽模板校验
		MpspMessage transaclResp = restService.transacl(message, cmd);
		if(!transaclResp.isRetCode0000()){
			modelMap.put(DataDict.RET_CODE, transaclResp.getRetCode());
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(transaclResp.getRetCode()));
			
			logInfo("Transacl Result Failed[RetCode]:%s:%s", transaclResp.getRetCode(), "交易屏蔽模板校验失败");
			return super.ERROR_PAGE;
		}
		logInfo("Transacl Result Success[RetCode]:0000:交易屏蔽模板通过");
		//4-保存订单
		//cmd.setBusinessType(DataDict.BUSI_STANDARD_WAP);
		MpspMessage orderResp = null;
		if("XE791000".equals(bankId)){
			logInfo("调用江西小额异步下单接口 bankId:%s",bankId);
			cmd.setBusinessType(DataDict.BUSI_JX_WAP);//江西小额wap支付
			orderResp = tradeService.asynOrder(bankId, message, cmd);
		}else{
			cmd.setBusinessType(DataDict.BUSI_STANDARD_WAP);//标商wap支付
			orderResp = tradeService.saveOrder(bankId, message, cmd);
		}	
		if(!orderResp.isRetCode0000()){
			logInfo("saveOrder Result Failed[RetCode]:%s:%s", orderResp.getRetCode()+":"+orderResp.getRetCodeBussi(), "保存订单异常");
			//begin modify by zhaoYan 保存订单失败时，根据后台返回码进行页面提示 20131227
			String errorMessage = messageService.getMessageDetail(orderResp.getRetCodeBussi());
			if("".equals(errorMessage)){
				errorMessage = messageService.getMessageDetail(orderResp.getRetCode());
			}
			//end modify by zhaoYan 保存订单失败时，根据返回码进行页面提示 20131227
//			logInfo("saveOrder Result Failed[RetCode]:%s:%s", orderResp.getRetCode(), "保存订单异常");
//			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(orderResp.getRetCode()));
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE,errorMessage);
			modelMap.put(DataDict.RET_CODE, orderResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, orderResp.getRetCodeBussi());
			return super.ERROR_WAP;
		}else{
			logInfo("saveOrder Result Success[RetCode]:0000:下单成功");
			//5-取得短信发送的对象，组装发送短信参数
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
			String smsSub = "";
			if("XE791000".equals(bankId)){
				smsSub = messageService.getSystemParam("XE791000.smsSub");
			}else{
				smsSub = smsParam.getSmsSub();
			}
			logInfo("saveOrder Result 页面展示需要的短信长号码[smsSub]:%s", smsSub);
			modelMap.put("smsSub", smsSub);
			OrderParam orderParam = getPageParam(orderResp, smsSub, cmd, bankId);
			orderParam.setMerName((String)message.get(HFBusiDict.MERNAME));
			orderParam.setGoodsName((String)message.get(HFBusiDict.GOODSNAME));
			if(super.isLevel2Mer(cmd.getMerId())){
				String merName2 = messageService.getSystemParam("unRealMerName");
				String goodsName2 = messageService.getSystemParam("unRealGoodsName");
				orderParam.setMerName2(merName2);
				orderParam.setGoodsName2(goodsName2);
			}
			modelMap.put("order", orderParam);
			session.setAttribute("orderParam", orderParam);
			//TODO 放入session中 省份 地市
			session.setAttribute(HFBusiDict.PROVCODE, ObjectUtil.trim((String)message.get(HFBusiDict.PROVCODE)));
			session.setAttribute(HFBusiDict.AREACODE, ObjectUtil.trim((String)message.get(HFBusiDict.AREACODE)));
			modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
			String retView = "";
			if(bankId.startsWith("XE")){
				retView = "order/wap_payresult";
			}else if(bankId.startsWith("MW")){
				retView = "order/wap_mw_payresult";
			}
			return retView;
		}
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
		
		orderParam.setGoodsName(cmd.getGoodsName());
		orderParam.setRetUrl(cmd.getRetUrl());
		orderParam.setAmtType(cmd.getAmtType());
		orderParam.setBankType(cmd.getBankType());
		orderParam.setVersion(cmd.getVersion());
		orderParam.setMerPriv(cmd.getMerPriv());

		String tableName = "";
		String porderId = orderParam.getPorderId();
		if(ObjectUtil.isNotEmpty(porderId)){
			tableName = porderId.substring(porderId.length()-1,porderId.length());
		}
		orderParam.setTableName(tableName);
		String retCode = orderResp.getRetCode();
		if(retCode.equals(DataDict.SUCCESS_RET_CODE)){
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
		String nextDirect = "";
		if(orderParam.getOrderState().equals("0")){
			if (retCode.equals(DataDict.SUCCESS_RET_CODE)) {
				nextDirect = "尊敬的“"+ orderParam.getMobileId()
					+ "”用户，请注意查收“"
					+ smsSub + "”短信，并按短信提示进行支付";
			} else if (retCode.equals("1163")) {
				nextDirect = "尊敬的用户，您不必重复下单，请按短信提示完成支付！";
			}
		}else if(orderParam.getOrderState().equals("1")){
			nextDirect = "尊敬的用户，您不用重复下单，系统正在受理您的支付请求，请注意查收“"
				+ smsSub + "”下发的提示短信！";
		}else if(orderParam.getOrderState().equals("2")){
			if (retCode.equals("1163")) {
				nextDirect = "尊敬的用户，订单已成功支付，不必重复下订单。";
			}else if (retCode.equals("1165")) {
				nextDirect = "尊敬的用户，您已定制该服务，不必重复下单。";
			}

		}else if(orderParam.getOrderState().equals("3")){
			nextDirect = "非常抱歉！由于系统忙，订单支付失败！请您稍候再试。";
		}else if(orderParam.getOrderState().equals("4")){
			nextDirect = "尊敬的用户，请您重新下订单。如仍有疑问，请咨询客服中心：4006125880";
		}
		orderParam.setNextDirect(nextDirect);
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
		return com.umpay.hfweb.dict.DataDict.FUNCODE_WAP_ORDSV;
	}
}
