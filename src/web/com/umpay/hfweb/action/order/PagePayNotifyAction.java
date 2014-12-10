package com.umpay.hfweb.action.order;

import java.util.List;
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
import com.umpay.hfweb.model.Advertisement;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  PagePayNotifyAction
 * @author     :  zhaoyan 
 * description :  页面下单-购买完成，前台支付结果通知处理类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class PagePayNotifyAction extends BaseAbstractAction{

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
		String bankId = ObjectUtil.trim(sessionParam.getBankId());
		//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		modelMap.put(HFBusiDict.PROVCODE, provcode);
		modelMap.put(HFBusiDict.AREACODE, areacode);
		modelMap.put(HFBusiDict.BANKID, bankId);
		//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
		//3-查询订单信息
		MpspMessage orderInfoResp = restService.queryMerOrder(sessionParam.getMerId(), sessionParam.getMerDate(), sessionParam.getOrderId());
		if(!orderInfoResp.isRetCode0000()){
			logInfo("queryOrderByMobileId Result Failed[RetCode]:%s:%s", orderInfoResp.getRetCode(), "查询订单信息失败");
			modelMap.put(DataDict.RET_CODE, orderInfoResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, orderInfoResp.getRetCodeBussi());
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(orderInfoResp.getRetCode()));
			return super.ERROR_PAGE;
		}
		logInfo("queryOrderByMobileId Result Success[RetCode]0000:查询订单信息成功");
		String orderState = String.valueOf(orderInfoResp.get(HFBusiDict.ORDERSTATE));
		String payRetCode = StringUtil.trim((String)orderInfoResp.get(HFBusiDict.RESERVED));//新增查询订单信息记录中的reserved字段：表示支付返回码
		OrderParam orderParam = getPageParam(orderInfoResp, smsParam.getSmsSub(), sessionParam);
		//4-区分小额 全网
		String retView = "";
		
		if(bankId.startsWith("XE")){
			retView = "order/web_xe_payresult";
		}else if(bankId.startsWith("MW")){
			retView = "order/web_mw_payresult";
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
			//5-1-2 判断广告是否展示
			boolean advalid = merAuthService.canAccess("ShowAD", sessionParam.getMerId());
			modelMap.put("advalid", advalid);
			try {
				session.invalidate();
			} catch (Exception e) {
				logger.error("session 置为无效之前已经失效==="+e);
			}
			//5-1-3判断展示哪一套广告以及广告图片
			//TODO
			if(advalid){
				int adNum=merAuthService.getAdByMerId(sessionParam.getMerId());
				if(adNum!=0){
					modelMap.put("adNum", adNum);
					//查找广告图片
					List<Advertisement> ads=merAuthService.getAds(adNum);
					if(ads!=null){
						modelMap.put("ads", ads);
					}
				}else{
					//商户没有配置广告则，等同于不展示广告
					modelMap.put("advalid", false);
				}
			}
			sessionParam.setPlateDate((String)orderInfoResp.get(HFBusiDict.PLATDATE));
			sessionParam.setSettleDate((String)orderInfoResp.get(HFBusiDict.PLATDATE));
			sessionParam.setRetCode((String)orderInfoResp.get(HFBusiDict.RETCODE));
			String retUrl = this.genWholeRetUrl(sessionParam);
			logInfo("Info2Mer retUrl:%s", retUrl);
			orderParam.setRetUrl(retUrl);
		}
		modelMap.put("isLevel2", super.isLevel2Mer(sessionParam.getMerId()));
		modelMap.put("order", orderParam);
		modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		//added by wanghaiwei 2012-11-12
		//modify by zhaoYan 2013-5-28 begin
		String smsSub = "";
		if("XE791000".equals(bankId)){
			smsSub = messageService.getSystemParam("XE791000.smsSub");
		}else{
			smsSub = smsParam.getSmsSub();
		}
		logInfo("saveOrder Result 页面展示需要的短信长号码[smsSub]:%s", smsSub);
		modelMap.put("smsSub", smsSub);
		//modify by zhaoYan 2013-5-28 end
		modelMap.put("exAction", "pagePayNotifyAction");
		//end
		return retView;
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
	private OrderParam getPageParam(MpspMessage orderInfoResp, String smsSub, OrderParam sessionParam) {
		OrderParam orderParam = new OrderParam();
		orderParam.setOrderId((String)orderInfoResp.get(HFBusiDict.ORDERID));
		orderParam.setMerId((String)orderInfoResp.get(HFBusiDict.MERID));
		orderParam.setMerName(sessionParam.getMerName());
		orderParam.setMerName2(sessionParam.getMerName2());
		orderParam.setMerDate((String)orderInfoResp.get(HFBusiDict.ORDERDATE));
		orderParam.setAmount(String.valueOf(orderInfoResp.get(HFBusiDict.AMOUNT)));
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar(orderParam.getAmount()));
		String orderState = String.valueOf(orderInfoResp.get(HFBusiDict.ORDERSTATE));
		orderParam.setOrderState(orderState);
		orderParam.setTableName(sessionParam.getTableName());
		orderParam.setGoodsName(sessionParam.getGoodsName());
		orderParam.setGoodsName2(sessionParam.getGoodsName2());
		orderParam.setPayRetCode(StringUtil.trim((String)orderInfoResp.get(HFBusiDict.RESERVED)));//支付返回码
		orderParam.setBankId(sessionParam.getBankId());
		orderParam.setGoodsId(sessionParam.getGoodsId());
		orderParam.setCusPhone(sessionParam.getCusPhone());
		orderParam.setServType(sessionParam.getServType());
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
		//前台支付结果通知功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_PAGE_NOTIFY;
	}
}
