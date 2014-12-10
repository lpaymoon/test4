package com.umpay.hfweb.action.base;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;
/**
 * ******************  类说明  *********************
 * class       :  DirectCommonAction
 * @author     :  yangwr 
 * description :  商户直连的共通类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public abstract class DirectCommonAction extends DirectBaseAction{

	@Override
	public void processBussiness(RequestMsg requestMsg, ResponseMsg responseMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//签名
		String sign = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_SIGN));
		
		if(!isVer3()){
			merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
			sign = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_SIGN_V2));
		}
		//1-1请求数据校验
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			responseMsg.setRetCode(checkParamResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),(String)checkParamResp.get("message"));
			return;
		}
		//1-2特殊校验
		MpspMessage specialCheckResp = specialCheck(requestMsg);
		if(specialCheckResp != null && !specialCheckResp.isRetCode0000()){
			responseMsg.setRetCode(specialCheckResp.getRetCode());
			responseMsg.setRetCodeBussi(specialCheckResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"特殊校验失败");
			return;
		}
		logInfo("ParamCheck Result Success[RetCode]0000:请求参数校验通过");
		//2-校验访问权限
		if(!merAuthService.canAccess(getFunCode(), merId)){
			//商户未开通此项支付服务
			responseMsg.setRetCode("1128");
			logInfo("MerAuthCheck Result Failed[RetCode]:1128:商户访问权限校验未通过");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		logInfo("MerAuthCheck Result Success[RetCode]:0000:商户访问权限校验已通过");
		//3-校验商户签名
		String plainText = getPlainText(requestMsg);
		MpspMessage checkSignResp = restService.checkSign(merId,plainText,sign);
		if(!checkSignResp.isRetCode0000()){
			responseMsg.setRetCode(checkSignResp.getRetCode());
			responseMsg.setRetCodeBussi(checkSignResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("SignCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"验证商户签名未通过");
			return;
		}
		logInfo("SignCheck Result Success[RetCode]:0000:商户签名验证通过");
		//4-校验商户信息
		MpspMessage checkMerResp = restService.queryMerInfo(merId);
		if(!checkMerResp.isRetCode0000()){
			responseMsg.setRetCode(checkMerResp.getRetCode());
			responseMsg.setRetCodeBussi(checkMerResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("MerStatusCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"商户状态校验未通过");
			return;
		}
		logInfo("MerStatusCheck Result Success[RetCode]:0000:商户状态校验通过");
		//主处理
		MpspMessage mainResp = mainProcess(requestMsg);
		if(mainResp != null && !mainResp.isRetCode0000()){
			responseMsg.setInfoMsg(mainResp);
			responseMsg.setRetCode(mainResp.getRetCode());
			responseMsg.setRetCodeBussi(mainResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("MainProcess Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"业务处理失败");
			return;
		}
		logInfo("MainProcess Result Success[RetCode]:0000:业务处理完成");
		//处理成功
		responseMsg.setRetCode0000();
		if(mainResp != null){
			logInfo("Set MainProcess Result To InfoMsg");
			responseMsg.setInfoMsg(mainResp);
		}
		responseSuccess2Mer(requestMsg,responseMsg);
	}
	/**
	 * ********************************************
	 * method name   : responseEorr2Mer 
	 * modified      : yangwr ,  Nov 6, 2011
	 * @see          : @see com.umpay.hfweb.action.base.DirectBaseAction#responseEorr2Mer(com.umpay.hfweb.model.RequestMsg, com.umpay.hfweb.model.ResponseMsg)
	 * *******************************************
	 */
	protected abstract void responseEorr2Mer(final RequestMsg requestMsg,final ResponseMsg responseMsg);
	/**
	 * ********************************************
	 * method name   : responseSuccess2Mer 
	 * description   : 组装响应给商户的成功信息
	 * @return       : void
	 * @param        : @param requestMsg
	 * @param        : @param responseMsg
	 * modified      : yangwr ,  Nov 6, 2011  3:51:44 PM
	 * @see          : 
	 * *******************************************
	 */
	protected abstract void responseSuccess2Mer(final RequestMsg requestMsg,final ResponseMsg responseMsg);
	/**
	 * ********************************************
	 * method name   : getPlainText 
	 * description   : 组装用户签名原文
	 * @return       : String
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : yangwr ,  Nov 6, 2011  3:51:25 PM
	 * @see          : 
	 * *******************************************
	 */
	protected abstract String getPlainText(final RequestMsg requestMsg);
	/**
	 * ********************************************
	 * method name   : specialCheck 
	 * description   : 特殊校验
	 * @return       : MpspMessage
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : yangwr ,  Nov 6, 2011  3:51:15 PM
	 * @see          : 
	 * *******************************************
	 */
	protected abstract MpspMessage specialCheck(final RequestMsg requestMsg);
	/**
	 * ********************************************
	 * method name   : mainProcess 
	 * description   : 主处理
	 * @return       : MpspMessage
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : yangwr ,  Nov 6, 2011  3:51:06 PM
	 * @see          : 
	 * *******************************************
	 */
	protected abstract MpspMessage mainProcess(final RequestMsg requestMsg);
}
