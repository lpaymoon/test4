package com.umpay.hfweb.action.wx;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONObject;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  WxVerifyPayAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  话付宝验证码支付（校验验证码，完成支付）
 * @see        :                        
 * ************************************************/   
public class WxVerifyPayAction extends WxOrderBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		// 1-检查请求参数
		logInfo("检验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}
		
		//3-验证码校验
		MpspMessage respMessage = restService.checkVerifyCode(requestMsg);
		if(!respMessage.isRetCode0000()){
			logInfo("验证码校验失败,retCode=%s", respMessage.getRetCode());
			respMap.setRetCode(respMessage.getRetCode());
			responseEorr(requestMsg, respMap);
			return;
		}
		
		//4-下单，支付
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		PageOrderCmd cmd = new PageOrderCmd(requestMsg.getWrappedMap());
		cmd.setBusinessType(DataDict.BUSI_WX_YZM);//话付宝验证码支付业务号0209
		MpspMessage orderResp = tradeService.wapDirectPay(cmd,mobileid,true);
		if(orderResp.isRetCode0000() || "86011571".equals(orderResp.getRetCode())){
			String retMsg=messageService.getSystemParam("Verify.PaySuccess.Msg");//支付成功的提示语
			retMsg=MessageFormat.format(retMsg,cmd.getGoodsName(),cmd.getAmount());
			logInfo("支付成功[RetCode]:0000");
			responseSuccess(respMap);
			return;
		}else{
			logInfo("支付失败[RetCode]:%s",orderResp.getRetCode());
			respMap.setRetCode(orderResp.getRetCode());
			responseEorr(requestMsg, respMap);
			return;
		}
	}
	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg =  getRetMessage(respMap);
		Map <String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.RETCODE, retCode);
		map.put(HFBusiDict.RETMSG, retMsg);
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据为:%s", jsonStr);
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
	}

	private void responseSuccess(ResponseMsg respMap) {
		respMap.setRetCode0000();
		String retMsg =  getRetMessage(respMap);
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg", retMsg);
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据:%s",jsonStr);
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
		
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_VERIFY_ZHIFU;
	}
}
