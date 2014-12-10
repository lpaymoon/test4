package com.umpay.hfweb.action.order;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.action.command.H5verifyCodePayCmd;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.MoneyUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;


/** ******************  类说明  *********************
 * class       :  H5VerifyCodeRequestAction
 * @author     :  xuwei
 * @version    :  1.0  
 * description :  接收来自用户扫描二维码后的请求，做初步验证以后返回到验证码获取页面
 * 				    （此页面包含商品名称，订单金额，手机号输入框，验证码输入框）
 * @see        :                        
 * ************************************************/   
public class H5VerifyCodeRequestAction extends BaseAbstractAction{
	private final String H5_REQUEST = "order/wap_crcode_verify_request";
	private final String H5_ERROR = "order/wap_crcode_error";
	private final String H5_PAYRESULT = "order/wap_crcode_verify_payresult";
	
	protected String processBussiness(HttpServletRequest request,HttpServletResponse response,Map<String,Object> modelMap) {
		//1-重置session，组装请求数据，进行数据校验
		HttpSession session = request.getSession();
		session.removeAttribute(DataDict.SEND_SMS_CAPTCHAINFO);
		session.removeAttribute("tradeMessage");
		session.removeAttribute(DataDict.MER_REQ_MOBILEID);
		session.removeAttribute(DataDict.FUNCODE_ORDER_PARAM);
		session.removeAttribute("order");
		
		RequestMsg reqMap = new RequestMsg();
		reqMap.putAllParam(HttpUtil.parseRequestParam(request));
		reqMap.setFunCode(this.getFunCode());
		reqMap.setRpid(ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID)));
//		String wholeRetUrl =  ObjectUtil.trim(reqMap.getStr(DataDict.MER_REQ_RETURL));;
//		session.setAttribute("wholeRetUrl", wholeRetUrl);
//		modelMap.put("wholeRetUrl",wholeRetUrl);
		
		MpspMessage checkParamResp = checkService.doCheck(reqMap);
		if(!checkParamResp.isRetCode0000()){
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s", checkParamResp.getRetCode(), messageService.getMessageDetail(checkParamResp.getRetCode()));
			modelMap.put(DataDict.RET_MSG, messageService.getMessageDetail(checkParamResp.getRetCode()));
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(checkParamResp.getRetCode()));
			modelMap.put(DataDict.RET_CODE, checkParamResp.getRetCode());
			return H5_ERROR;
		}
		logInfo("ParamCheck Result Success[RetCode]:0000:请求参数校验通过");
	
		H5verifyCodePayCmd cmd = new H5verifyCodePayCmd(reqMap.getWrappedMap());
		//根据输入参数组织返回信息
		OrderParam oparam = getTomerParam(cmd);
		String wholeRetUrl = genWholeRetUrl(cmd);
		request.getSession().setAttribute("wholeRetUrl", wholeRetUrl);
		modelMap.put("wholeRetUrl",wholeRetUrl);
	//	logInfo("wholeRetUrl["+wholeRetUrl+"]");
		//2 校验访问权限
		if(!merAuthService.canAccess("H5YZM", cmd.getMerId())){
			//商户未开通此项支付服务
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail("1128"));
			modelMap.put(DataDict.RET_CODE, "1128");
			logInfo("MerAuthCheck merId["+cmd.getMerId()+"] Result Failed[RetCode]:1128:商户访问权限校验未通过");	
			return H5_ERROR;
		}

        //查询渠道和商品信息
		MpspMessage queryGoodsInfResp = restService.queryChnlGoodInf(cmd.getChnlId(), cmd.getMerId(), cmd.getGoodsId());
		if(!queryGoodsInfResp.isRetCode0000()){
			logInfo("SignCheck Result Failed[RetCode]:%s:%s", queryGoodsInfResp.getRetCode(), "查询渠道商品信息失败");
			modelMap.put(DataDict.RET_CODE, queryGoodsInfResp.getRetCode());
			modelMap.put(DataDict.RET_CODE_BUSSI, queryGoodsInfResp.getRetCodeBussi());
			modelMap.put(DataDict.MER_RESP_ERROR_MESSAGE, messageService.getMessageDetail(queryGoodsInfResp.getRetCode()));
			return H5_ERROR;
		}
		logInfo("queryGoodsInfResp  Result Success[RetCode]0000:查询渠道商品信息成功");
		session.setAttribute("goodsName", queryGoodsInfResp.getStr(HFBusiDict.GOODSNAME));
		modelMap.put(DataDict.RET_CODE, DataDict.SUCCESS_RET_CODE);
		request.getSession().setAttribute("order", oparam);
		request.getSession().setAttribute(DataDict.FUNCODE_ORDER_PARAM, cmd);
		
		//6-跳转显示页面
		return H5_REQUEST;
	}
	

	/**
	 * ********************************************
	 * method name   : getPageParam 
	 * description   : 获取下单页面参数
	 * @return       : OrderPageParam
	 * @param        : @param checkTradeResp
	 * @param        : @param cmd
	 * @param        : @param bankId
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 4, 2011 5:35:29 PM
	 * @see          : 
	 * *******************************************
	 */
	
	private OrderParam getTomerParam(H5verifyCodePayCmd param) {
		OrderParam tomerParam = new OrderParam();
		tomerParam.setGoodsId(param.getGoodsId());
		tomerParam.setMerId(param.getMerId());
		tomerParam.setAmount(param.getAmount());
		tomerParam.setGateId(param.getChnlId());
		tomerParam.setAmount4dollar(MoneyUtil.Cent2Dollar(param.getAmount()));
		return tomerParam;
	}
	private String genWholeRetUrl(H5verifyCodePayCmd param)  {
//		String retUrl ="http://10.10.38.214:8602/hfwebbusi/pay/h5VerifyCodeRequest.do?";
		String retUrl = ObjectUtil.trim(messageService.getSystemParam("H5YZM.RetUrl"));
		if(retUrl.equals("")){
		    retUrl ="http://payment.umpay.com/hfwebbusi/pay/h5VerifyCodeRequest.do";
		}
		StringBuffer buffer = new StringBuffer();
		try{
			
			buffer.append(retUrl).append("?chnlId=");
			buffer.append(URLEncoder.encode(param.getChnlId(), "UTF-8")).append("&merId=");
			buffer.append(URLEncoder.encode(param.getMerId(), "UTF-8")).append("&goodsId=");
			buffer.append(URLEncoder.encode(param.getGoodsId(),"UTF-8")).append("&goodsInf=");
			buffer.append(URLEncoder.encode(param.getGoodsInf(),"UTF-8")).append("&amount=");
			buffer.append(URLEncoder.encode(param.getAmount(),"UTF-8"));
		}catch(UnsupportedEncodingException e){
			logInfo("UnsupportedEncodingException %s", buffer.toString());
			e.printStackTrace();
		}
		logInfo("requesturl: %s", buffer.toString());

		return buffer.toString();
	}
	@Override
	protected String getFunCode() {
		return com.umpay.hfweb.dict.DataDict.FUNCODE_H5YZMCL;
	}
}
