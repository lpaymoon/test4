package com.umpay.hfweb.action.order;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Element;

import com.bs.mpsp.util.DateTimeUtil;
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
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
/**
 * ******************  类说明  *********************
 * class       :  H5VerifyCodeAction
 * @author     :  xuwei 
 * description :  接收获取验证码的请求，向用户下发验证码短信（短信内容包含验证码）
 *   			    
 * ***********************************************
 */
public class H5VerifyCodeAction extends BaseAbstractAction{
	
	private final String H5_REQUEST = "order/wap_crcode_verify_request";
	private final String H5_PAYRESULT = "order/wap_crcode_verify_payresult";
	private final String H5_ERROR = "order/wap_crcode_error";
	private static final String Sended_YZM_Count= "yzmCount"; //获取短信验证码次数
	
	protected String processBussiness(HttpServletRequest request,HttpServletResponse response,Map<String,Object> modelMap) {
		PrintWriter out = null;
		try {
			  out = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s","1127","IOException");
			response.setContentType("text/html;charset=UTF-8");
			out.write("false");
			return null;
		}
		
		//1-获取参数
		RequestMsg reqMap = new RequestMsg();
		reqMap.putAllParam(HttpUtil.parseRequestParam(request));
		reqMap.setFunCode(this.getFunCode());
		
		H5verifyCodePayCmd cmd=(H5verifyCodePayCmd)request.getSession().getAttribute(DataDict.FUNCODE_ORDER_PARAM);
		if(cmd==null){
			logInfo("SessionCheck Result Failed[RetCode]:%s:%s","1127","会话已过期");
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1127"));
			modelMap.put(DataDict.RET_CODE, "1127");
			String retMsg=messageService.getSystemParam("Captcha.Timeout.Msg");//交易超时
			modelMap.put(DataDict.RET_MSG, retMsg);
			response.setContentType("text/html;charset=UTF-8");
			out.write("false");
			return null;
		}
		String mobileId=(String)reqMap.get(DataDict.MER_REQ_MOBILEID);
		String wholeRetUrl=(String)request.getSession().getAttribute("wholeRetUrl");
		modelMap.put("wholeRetUrl", wholeRetUrl);
		OrderParam orderParam = (OrderParam)request.getSession().getAttribute("order");
		modelMap.put("order", orderParam);
		request.getSession().removeAttribute("order");//取出后必须remove掉，否则会影响其他页面
		modelMap.put(DataDict.MER_REQ_MOBILEID, mobileId);
		request.getSession().setAttribute(DataDict.MER_REQ_MOBILEID, mobileId);
		String goodsName=(String)request.getSession().getAttribute("goodsName");
		//2 校验访问权限
		if(!merAuthService.canAccess("H5YZM", cmd.getMerId())){
			//商户未开通此项支付服务
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1128"));
			modelMap.put(DataDict.RET_CODE, "1128");
			logInfo("MerAuthCheck merId["+cmd.getMerId()+"] Result Failed[RetCode]:1128:商户访问权限校验未通过");	
			request.getSession().setAttribute("goodsName", goodsName);
			request.getSession().setAttribute("order", orderParam);
			response.setContentType("text/html;charset=UTF-8");
			out.write("false");
			return null;
		}
		logInfo("MerAuthCheck Result Success[RetCode]:0000:商户访问权限校验已通过");
				
		//2.做缓存，用于限制获取动态验证码的次数
		CommonEcc ecc = (CommonEcc)AbstractCacheFactory.getInstance().getCacheClient("wapYzmCode");
		String key = mobileId;
		Element cache = ecc.getElementByDefaultMap(key,Sended_YZM_Count,new Integer(0));
		Map<String,Object> cacheInfo = (Map<String,Object>)cache.getValue();
		Integer intSendedCount = (Integer)cacheInfo.get(Sended_YZM_Count);
		Integer maxNum = Integer.parseInt(messageService.getSystemParam("WAP_QRCODE.MAX"));
		if(intSendedCount>=maxNum){
			logInfo("超过获取验证码次数的次数限制！");
			modelMap.put(DataDict.RET_CODE, "86021451");
			request.getSession().setAttribute("goodsName", goodsName);
			request.getSession().setAttribute("order", orderParam);
			response.setContentType("text/html;charset=UTF-8");
			out.write("false");
			return null;
		}
		
		//向商户下单并下发验证码短信
		long dt = DateTimeUtil.currentDateTime();
		reqMap.put(DataDict.MER_REQ_CHNLDATE, DateTimeUtil.getDateString(dt));
		reqMap.put(DataDict.MER_REQ_CHNLID, cmd.getChnlId());
		reqMap.put(DataDict.MER_REQ_CHNLORDERID,getRandom()+DateTimeUtil.getTimeString(dt));//TODO：随机生成
		reqMap.put(DataDict.MER_REQ_CHNLPRIV, "priv");
		reqMap.put(DataDict.MER_REQ_AMOUNT, cmd.getAmount());
		reqMap.put(DataDict.MER_REQ_GOODSINF, cmd.getGoodsInf());//TODO:确认放在二维码中
		reqMap.put(DataDict.MER_REQ_VERSION, "3.0");//
		reqMap.put(HFBusiDict.BUSINESSTYPE, DataDict.BUSI_WAP_CRCODE_YZMZF);//业务区分：wap线下扫码 验证码下单，需增加新的类型
		MpspMessage payResp = tradeService.qdVerifyOrder(reqMap);
		if(!payResp.isRetCode0000()){
			reqMap.setRetCode(payResp.getRetCode());
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(payResp.getRetCode()));
//			modelMap.put(DataDict.RET_MSG, retMsg);
			modelMap.put(DataDict.RET_CODE, payResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, payResp.getRetCodeBussi());
			request.getSession().setAttribute("goodsName", goodsName);
			request.getSession().setAttribute("order", orderParam);
			request.getSession().setAttribute(DataDict.FUNCODE_ORDER_PARAM, cmd);
			logInfo("QDOrderCreate Result Failed[RetCode]:%s:%s",payResp.getRetCode(),"向业务层下单失败");
			response.setContentType("text/html;charset=UTF-8");
			out.write("false");
			return null;
		}
		logInfo("渠道验证码下单成功");
		String orderId= ObjectUtil.trim(payResp.getStr(HFBusiDict.ORDERID));
		String orderdate = ObjectUtil.trim(payResp.getStr(HFBusiDict.ORDERDATE));
		//4-组装session
		modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		request.getSession().setAttribute(DataDict.MER_REQ_MOBILEID, mobileId);
		request.getSession().setAttribute(HFBusiDict.ORDERID, orderId);
		request.getSession().setAttribute(HFBusiDict.ORDERDATE, orderdate);
		request.getSession().setAttribute("goodsName", goodsName);
		request.getSession().setAttribute("order", orderParam);
		request.getSession().setAttribute(DataDict.FUNCODE_ORDER_PARAM, cmd);	
		intSendedCount++;
		cacheInfo.put(Sended_YZM_Count, intSendedCount);
		//5-跳转
		out.write("success");
		return null;
	}
	
	private OrderParam getPageParam(MpspMessage checkTradeResp, H5verifyCodePayCmd cmd) {
		OrderParam orderParam = new OrderParam();
		orderParam.setGateId(cmd.getChnlId());
		orderParam.setAmount(cmd.getAmount());
		orderParam.setAmount4dollar(MoneyUtil.Cent2Dollar(cmd.getAmount()));
		orderParam.setServType(String.valueOf(checkTradeResp.get(HFBusiDict.SERVTYPE)));
		orderParam.setServMonth(String.valueOf(checkTradeResp.get(HFBusiDict.SERVMONTH)));
		orderParam.setCusPhone((String)checkTradeResp.get(HFBusiDict.CUSPHONE));
		orderParam.setMobileId(cmd.getMobileId());
		orderParam.setGoodsId(cmd.getGoodsId());
		orderParam.setGoodsName((String)checkTradeResp.get(HFBusiDict.GOODSNAME));
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
		//WAP直接支付发送验证码的功能码
		return com.umpay.hfweb.dict.DataDict.FUNCODE_H5YZM;
	}
	
	
	protected String getRandom(){
		Random random = new Random();
		String sRand="";
		for (int i=0;i<6;i++){
		    String rand=String.valueOf(random.nextInt(10));
		    sRand+=rand;
		}
		return sRand;
	}
}
