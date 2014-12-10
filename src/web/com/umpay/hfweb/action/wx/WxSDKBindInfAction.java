package com.umpay.hfweb.action.wx;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  WxStatisticsAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  12580客户端绑定关系同步公钥
 * @see        :                        
 * ************************************************/   
public class WxSDKBindInfAction extends WxOrderBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		//检查请求参数
		logInfo("校验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}

		//调用资源层同步SDK客户端公钥
		logInfo("调用资源层同步SDK客户端公钥.....");
		MpspMessage UpdateSDKpbKeyResp =restService.updateSDKpbKey(requestMsg);
		
		if(!UpdateSDKpbKeyResp.isRetCode0000()){
			logInfo("UpdateSDKpbKeyResp Result Failed[RetCode]:%s:%s", UpdateSDKpbKeyResp.getRetCode(), "同步SDK客户端公钥失败");
			respMap.setRetCode(UpdateSDKpbKeyResp.getRetCode());
//			respMap.setRetCodeBussi(UpdateSDKpbKeyResp.getRetCodeBussi());
			respMap.putAll(UpdateSDKpbKeyResp);
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("UpdateSDKpbKeyResp Result Success[RetCode]:0000:同步SDK客户端公钥成功");
		responseSuccess(UpdateSDKpbKeyResp,requestMsg,respMap);
	}
	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg = getRetMessage(respMap);
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.RETCODE, retCode);
		map.put(HFBusiDict.RETMSG, retMsg);
    	String jsonStr = JSONObject.fromObject(map).toString();
		byte[] by = null;
		try {
			logInfo("返回客户端的信息：%s", jsonStr);
			by = jsonStr.getBytes("UTF-8");
		} catch (Exception e) {
			logInfo("返回数据时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(by);
	}
	private void responseSuccess(MpspMessage mpsp,RequestMsg requestMsg, ResponseMsg respMap){
		respMap.setRetCode0000();
		Map<String,String> map = new HashMap<String,String>();
		map.put(DataDict.MER_REQ_MOBILEID, mpsp.getStr(HFBusiDict.MOBILEID));
		map.put(HFBusiDict.RETCODE, DataDict.SUCCESS_RET_CODE);
		map.put(HFBusiDict.RETMSG, mpsp.getStr(HFBusiDict.RETMSG));
    	String jsonStr = JSONObject.fromObject(map).toString();
		byte[] by = null;
		try {
			logInfo("返回客户端的信息：%s", jsonStr);
			by = jsonStr.getBytes("UTF-8");
		} catch (Exception e) {
			logInfo("返回数据时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(by);
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_SDKBD;
	}
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request,false);//非加密数据
	}

}
