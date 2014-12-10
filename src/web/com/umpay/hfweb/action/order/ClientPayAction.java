package com.umpay.hfweb.action.order;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.action.command.DirectOrderCmd;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.action.param.SmsParam;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  ClientPayAction
 * @author     :  zhaoyan 
 * description :  金山小页面下单支付流程处理类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class ClientPayAction extends BaseAbstractAction{
	private static final String JINSHAN_ERROR_PAGE = "jinshan_error";
	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap){
		//1-对请求数据进行校验,组装请求数据
		RequestMsg reqMap = new RequestMsg();
		reqMap.putAllParam(HttpUtil.parseRequestParam(request));
		reqMap.setFunCode(this.getFunCode());
		reqMap.setRpid(ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID)));
		MpspMessage checkParamResp = checkService.doCheck(reqMap);
		if(!checkParamResp.isRetCode0000()){
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s", checkParamResp.getRetCode(), messageService.getMessageDetail(checkParamResp.getRetCode()));
			modelMap.put("nextDirect", messageService.getMessageDetail(checkParamResp.getRetCode()));
			modelMap.put(DataDict.RET_CODE, checkParamResp.getRetCode());
			return JINSHAN_ERROR_PAGE;
		}
		logInfo("ParamCheck Result Success[RetCode]:0000:请求参数校验通过");
		DirectOrderCmd cmd = new DirectOrderCmd(reqMap.getWrappedMap());
		//1-2 商户下单时间校验
		if(!DateUtil.verifyOrderDate(cmd.getMerDate())){
			modelMap.put("nextDirect", messageService.getMessageDetail("1310"));
			modelMap.put(DataDict.RET_CODE, "1310");
			logInfo("VerifyOrderDate Result Failed[RetCode]:%s:%s", "1310","商户下单时间校验未通过");
			return JINSHAN_ERROR_PAGE;
		}
		logInfo("VerifyOrderDate Result Success[RetCode]:0000:商户下单时间校验通过");
		//2-商户验签
		MpspMessage checkSignResp = restService.checkSign(cmd.getMerId(), cmd.getPlainText(), cmd.getSign());
		if(!checkSignResp.isRetCode0000()){
			logInfo("SignCheck Result Failed[RetCode]:%s:%s", checkSignResp.getRetCode(), "验证商户签名失败");
			modelMap.put(DataDict.RET_CODE, checkSignResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, checkSignResp.getRetCodeBussi());
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(checkSignResp.getRetCode()));
			modelMap.put("nextDirect", messageService.getMessageDetail(checkSignResp.getRetCode()));
			return JINSHAN_ERROR_PAGE;
		}
		logInfo("SignCheck Result Success[RetCode]0000:商户签名验证通过");
		//3-交易鉴权
		MpspMessage checkTradeResp = restService.checkTrade(cmd.getMobileId(), cmd.getMerId(), cmd.getGoodsId());
		if(!checkTradeResp.isRetCode0000()){
			logInfo("checkTrade Result Failed[RetCode]:%s:%s",checkTradeResp.getRetCode(),"交易鉴权失败");
			String errorMessage = messageService.getMessageDetail(checkTradeResp.getRetCodeBussi());
			if("".equals(errorMessage)){//modify by zhuoyangyang 20140430 交易鉴权失败返回码对应信息显示在页面上
				errorMessage = messageService.getMessageDetail(checkTradeResp.getRetCode());
			}
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, errorMessage);
			modelMap.put(DataDict.RET_CODE, checkTradeResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, checkTradeResp.getRetCodeBussi());
			modelMap.put("nextDirect", messageService.getMessageDetail(checkTradeResp.getRetCode()));
			return JINSHAN_ERROR_PAGE;
		}
		logInfo("checkTrade Result Success[RetCode]0000:交易鉴权通过");
		//begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		modelMap.put(HFBusiDict.PROVCODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.PROVCODE)));
		modelMap.put(HFBusiDict.AREACODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.AREACODE)));
		//end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		
		//4-确认可支付银行
		String bankId = ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.BANKID));
		if(ObjectUtil.isEmpty(bankId)){
			//无可支付银行的返回码 <您暂时不能使用支付服务>
			logInfo("BankCheck Result Failed[RetCode]:%s:%s","1303","支付银行校验未通过");
			String errorMessage = messageService.getMessageDetail(checkTradeResp.getRetCodeBussi());
			if("".equals(errorMessage)){//modify by zhuoyangyang 20140430 交易鉴权失败返回码对应信息显示在页面上
				errorMessage = messageService.getMessageDetail(checkTradeResp.getRetCode());
			}
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, errorMessage);
			modelMap.put("nextDirect", messageService.getMessageDetail("1303"));
			modelMap.put(DataDict.RET_CODE, "1303");
			return JINSHAN_ERROR_PAGE;
		}
		logInfo("BankCheck Result Success[RetCode]:0000:可支付银行确认通过---bankId[ "+ bankId +" ]");
		//TODO begin 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-13
		modelMap.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-13
		
		//5-保存订单
		cmd.setBusinessType(DataDict.BUSI_JINSHAN_WEB);//业务区分:金山小页面 panxingwu add
		MpspMessage orderResp = tradeService.saveOrder(bankId, checkTradeResp, cmd);
		if(!orderResp.isRetCode0000()){
			logInfo("saveOrder Result Failed[RetCode]:%s:%s", orderResp.getRetCode(), "保存订单异常");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(orderResp.getRetCode()));
			modelMap.put(DataDict.RET_CODE, orderResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, orderResp.getRetCodeBussi());
			modelMap.put("nextDirect", messageService.getMessageDetail(orderResp.getRetCode()));
			return JINSHAN_ERROR_PAGE;
		}
		logInfo("saveOrder Result Success[RetCode]:0000:下单成功");
		//6-取得短信发送的对象，组装发送短信参数
		String porderId = ObjectUtil.trim((String)orderResp.get(HFBusiDict.ORDERID4p));
		String goodsId = ObjectUtil.trim((String)orderResp.get(HFBusiDict.GOODSID));
		String servType = ObjectUtil.trim(String.valueOf(orderResp.get(HFBusiDict.SERVTYPE)));
		String servMonth = ObjectUtil.trim(String.valueOf(orderResp.get(HFBusiDict.GOODSID)));
		//modify by zhaoYan 2013-11-26 start 短信子号迁移
		SmsParam smsParam = new SmsParam();
		String merId = (String)orderResp.get(HFBusiDict.MERID);
		String mers = messageService.getSystemParam("special.called.mer.list","");
		if(mers.toUpperCase().contains("ALL") || mers.contains(merId)){
			smsParam = smsService.genSmsInfoSpecial(servType, servMonth, goodsId, porderId,merId);
		}else{
			smsParam = smsService.genSmsInfo(servType, servMonth, goodsId, porderId);
		}
		//SmsParam smsParam = smsService.genSmsInfo(servType, servMonth, goodsId, porderId);
		//modify by zhaoYan 2013-11-26 end
		OrderParam orderParam = getPageParam(orderResp, smsParam.getSmsSub(), (String)checkTradeResp.get(HFBusiDict.GOODSNAME));
		modelMap.put("order", orderParam);
		String retView = "";
		String isNew = request.getParameter("isNew");
        if(ObjectUtil.isNotEmpty(isNew) && isNew.equals("1")){
        	retView = "order/web_client_payresult_new";
        }else{
        	retView = "order/web_client_payresult";
        }
        modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		return retView;
	}
	/**
	 * ********************************************
	 * method name   : getPageParam 
	 * description   : 获得页面展示的参数
	 * @return       : OrderParam
	 * @param        : @param orderResp
	 * @param        : @param smsSub
	 * @param        : @param goodsName
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 24, 2011 3:29:11 PM
	 * @see          : 
	 * *******************************************
	 */
	private OrderParam getPageParam(MpspMessage orderResp, String smsSub, String goodsName) {
		OrderParam orderParam = new OrderParam();
		orderParam.setGoodsId((String)orderResp.get(HFBusiDict.GOODSID));
		orderParam.setOrderId((String)orderResp.get(HFBusiDict.ORDERID));
		orderParam.setPorderId((String)orderResp.get(HFBusiDict.ORDERID4p));
		orderParam.setAmount((String)orderResp.get(HFBusiDict.AMOUNT));
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar((String)orderResp.get(HFBusiDict.AMOUNT)));
		orderParam.setMerId((String)orderResp.get(HFBusiDict.MERID));
		orderParam.setMerDate((String)orderResp.get(HFBusiDict.ORDERDATE));
		orderParam.setMobileId((String)orderResp.get(HFBusiDict.MOBILEID));
		orderParam.setGoodsName(goodsName);
		String orderState = (String)orderResp.get(HFBusiDict.ORDERSTATE);
		orderParam.setOrderState(orderState);
		String tableName = "";
		String porderId = orderParam.getPorderId();
		if(ObjectUtil.isNotEmpty(porderId)){
			tableName = porderId.substring(porderId.length()-1,porderId.length());
		}
		orderParam.setTableName(tableName);
		String retCode = orderResp.getRetCode();
		String nextDirect = "";
		if(orderResp.isRetCode0000()){
			orderParam.setOrderState("0");
			nextDirect = "尊敬的“"+orderParam.getMobileId()+"”用户：<br />您购买的"+orderParam.getGoodsName()+"产品订单已提交<br/> <strong>请注意查收</strong>“<span class=\"red\">"+smsSub+"</span>”<strong>发送的短信，并按短信提示进行回复</strong><br />";
		}else if(retCode.equals("1163")){
			orderParam.setOrderState("4");
			nextDirect = "尊敬的“"+ orderParam.getMobileId()+"”用户：<br />您购买的"+ orderParam.getGoodsName() +"产品订单为重复下单<br/>";
		}else if(retCode.equals("1164")){
			orderParam.setOrderState("1");
			nextDirect = "尊敬的“"+orderParam.getMobileId()+"”用户：<br />您购买的"+orderParam.getGoodsName()+"产品订单正在支付中！不用重复下单，系统正在受理您的支付请求<br />";
		}else if(retCode.equals("1165")){
			orderParam.setOrderState("2");
			nextDirect = "尊敬的“" + orderParam.getMobileId() + "”用户：<br />您已定制该服务，不必重复下单";
		}else{
			orderParam.setOrderState("1");
			nextDirect = "尊敬的“"+orderParam.getMobileId()+"”用户：<br />系统暂时不能提供服务,您购买的"+orderParam.getGoodsName()+"产品订单下订单失败！<br />";
		}
		orderParam.setNextDirect(nextDirect);
		return orderParam;
	}
	@Override
	protected String getFunCode() {
		//页面下单的功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_CLIENT_XD;
	}
}
