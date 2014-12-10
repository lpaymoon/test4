package com.umpay.hfweb.action.pay;


import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.action.command.ChannelPayCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  SpecialChannelPayAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  特殊直接支付接口（目前仅供福建12580买Q币用）
 * 				     对我们平台来说12580和腾讯都是商户
 * 				  chnlid是12580的商户号，merid是腾讯的商户号
 * @see        :                        
 * ************************************************/   
public class SpecialChannelPayAction extends DirectBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg,ResponseMsg responseMsg) {
		//1 请求数据校验
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		
		logInfo("请求参数为:%s", requestMsg.getWrappedMap());
		if(!checkParamResp.isRetCode0000()){
			responseMsg.setRetCode(checkParamResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),(String)checkParamResp.get("message"));
			return;
		}
		logInfo("参数校验通过");
		String chnlId = requestMsg.getStr(DataDict.MER_REQ_CHNLID);
		String merId = requestMsg.getStr(DataDict.MER_REQ_MERID);
		String mobileId = requestMsg.getStr(DataDict.MER_REQ_MOBILEID); 
		String sign = requestMsg.getStr(DataDict.MER_REQ_SIGN); 
		
		if(!merAuthService.canAccess(getFunCode(),merId)){
			//商户未开通此项支付服务
			responseMsg.setRetCode("1128");
			logInfo("MerAuthCheck Result Failed[RetCode]:1128:商户访问权限校验未通过");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		
		//2 校验访问权限
		if(!canAccess(chnlId,merId)){
			//商户未开通此项支付服务
			responseMsg.setRetCode("1128");
			logInfo("MerAuthCheck Result Failed[RetCode]:1128:商户访问权限校验未通过");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		logInfo("MerAuthCheck Result Success[RetCode]:0000:商户访问权限校验已通过");
		
		//3IP验证
		requestMsg.put(DataDict.NET_CLIENTIP+"#FLAG", "true");
		MpspMessage checkIPResp = merAuthService.accessCheck(getFunCode(),chnlId,requestMsg);
		if(!checkIPResp.isRetCode0000()){
			//特殊访问权限校验（IP）未通过
			responseMsg.setRetCode(checkIPResp.getRetCode());
			logInfo("MerAuthCheck special Result Failed[RetCode]:%s:商户IP校验未通过",checkIPResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		logInfo("MerAuthCheck special Result Success[RetCode]:0000:商户IP校验已通过");
		
		//4 支付频率控制
		String checkRateResp = merAuthService.checkChannelPayRate(mobileId);
		if(!DataDict.SUCCESS_RET_CODE.equals(checkRateResp)){
			//支付频率控制未通过
			responseMsg.setRetCode(checkRateResp);
			logInfo("PayRate Check Result Failed[RetCode]:%s:支付频率控制未通过",checkRateResp);
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		//组装请求的明文串
		String unSignStr = getUnSignStr(requestMsg);
		//5商户验签
		MpspMessage checkSignResp = restService.checkSign(chnlId,unSignStr,sign);
		if(!checkSignResp.isRetCode0000()){
			responseMsg.setRetCode(checkSignResp.getRetCode());
			responseMsg.setRetCodeBussi(checkSignResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("SignCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"验证商户签名失败");
			return;
		}
		//6请求业务层进行下单
		MpspMessage payResp = tradeService.specialMerPay(requestMsg);
		if(!payResp.isRetCode0000()){
			responseMsg.setRetCode(payResp.getRetCode());
			responseMsg.setRetCodeBussi(checkSignResp.getRetCodeBussi());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("SignCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),"交易失败");
			return;
		}
		//7响应12580请求
		responseSuccess2Mer(requestMsg,responseMsg);
	}
	/**
	 * ********************************************
	 * method name   : canAccess 
	 * description   : 验证商户权限（是否有权限对指定的商户进行交易）
	 * @return       : boolean
	 * @param        : @param chnlid
	 * @param        : @param merId
	 * @param        : @return
	 * modified      : panxingwu ,  2013-1-22  上午10:23:25
	 * @see          : 
	 * *******************************************
	 */
	private boolean canAccess(String chnlid, String merId) {
		String accMers = messageService.getSystemParam(chnlid+".ACCESS");
		String denyMers = messageService.getSystemParam(chnlid+".DENY");
		boolean flag = false;
		//1验证要交易的商户对象是否为可交易商户
		if(!ObjectUtil.isEmpty(accMers)){
			if(!accMers.equals("ALL")){
				String[] merids = accMers.split(",");
				for (String mid : merids) {
					if(merId.equals(mid)){
						flag = true;
						break;
					}
				}
			}else{
				flag=true;
			}
		}else{
			flag=true;//没有登记商户，不做限制
		}
		//2验证要交易的商户对象是否为禁止访问的商户
		if(flag){
			if(!ObjectUtil.isEmpty(denyMers)){
				if(!denyMers.equals("ALL")){
					String[] merids = denyMers.split(",");
					for (String mid : merids) {
						if(merId.equals(mid)){
							flag = false;
							break;
						}
					}
				}else{
					flag=false;
				}
			}
		}
		return flag;
	}

	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		String mobileId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MOBILEID));
		String chnlOrderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID));
		String chnlDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLDATE));
		String chnlid = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLID)); 
		StringBuffer sb = new StringBuffer();
		sb.append(chnlid).append("|");
		sb.append(chnlOrderId).append("|");
		sb.append(chnlDate).append("|");
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append(mobileId).append("|");
		sb.append(responseMsg.getRetCode()).append("|");
		sb.append(getRetMessage(responseMsg)).append("||");
		responseMsg.setDirectResMsg(sb.toString());
	}
	
	protected void responseSuccess2Mer(RequestMsg requestMsg,final ResponseMsg responseMsg){
		String retCode = DataDict.SUCCESS_RET_CODE;
		responseMsg.setRetCode0000();
		
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		String mobileId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MOBILEID));
		String version = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_VERSION));
		String chnlOrderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID));
		String chnlDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLDATE));
		String chnlid = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_CHNLID)); 
		
		StringBuffer sb = new StringBuffer();
		sb.append(chnlid).append("|");
		sb.append(chnlOrderId).append("|");
		sb.append(chnlDate).append("|");
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append(mobileId).append("|");
		sb.append(retCode).append("|");
		sb.append("支付成功").append("|");
		sb.append(version);
		logInfo("Info2Mer Plain Text:%s",sb.toString());
		String sign = super.platSign(sb.toString());
		logInfo("Info2Mer Signed Text:%s",sign);
		sb.append("|").append(sign);
		
		responseMsg.setDirectResMsg(sb.toString());
	}
	
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_TSQDZF;
	}
	protected  ChannelPayCmd populateCommand(RequestMsg requestMsg){
		return new ChannelPayCmd(requestMsg.getWrappedMap());
	}
	protected String getUnSignStr(RequestMsg requestMsg){
		String chnlid = requestMsg.getStr(DataDict.MER_REQ_CHNLID);
		String chnlOrderid = requestMsg.getStr(DataDict.MER_REQ_CHNLORDERID);
		String chnlDate = requestMsg.getStr(DataDict.MER_REQ_CHNLDATE);
		String chnlPriv = requestMsg.getStr(DataDict.MER_REQ_CHNLPRIV);
		String expand = requestMsg.getStr(DataDict.MER_REQ_EXPAND);
		String merid = requestMsg.getStr(DataDict.MER_REQ_MERID);
		String goodsid = requestMsg.getStr(DataDict.MER_REQ_GOODSID);
		String amount = requestMsg.getStr(DataDict.MER_REQ_AMOUNT);
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		String version = requestMsg.getStr(DataDict.MER_REQ_VERSION);
		String unSignStr = "chnlId="+chnlid+"&chnlOrderId="+chnlOrderid+"&chnlDate="+chnlDate+"&merId="+merid+"&goodsId="+goodsid+"&amount="+amount+"&mobileId="+mobileid+"&chnlPriv="+chnlPriv+"&expand="+expand+"&version="+version;
		return unSignStr;
	}
}
