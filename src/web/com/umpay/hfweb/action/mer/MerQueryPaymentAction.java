package com.umpay.hfweb.action.mer;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectCommonAction;
import com.umpay.hfweb.action.command.ChannelPayCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;



/** ******************  类说明  *********************
 * class       :  MerQueryPaymentAction
 * @author     :  LiuJiLong
 * @version    :  1.0  
 * description :  查询北京市余额信息
 * @see        :                        
 * ************************************************/   
public class MerQueryPaymentAction extends DirectCommonAction{

	protected void responseEorr2Mer(RequestMsg requestMsg,ResponseMsg responseMsg){		
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		String memo = getRetMessage(responseMsg);
		String meta = merId+"|"+mobileId+"|||||||" + responseMsg.getRetCode() +
				"|" + memo + "||";
		responseMsg.setDirectResMsg(meta);
	}
	
	/** ********************************************
	 * method name   : responseSuccess2Mer 
	 * modified      : LiuJiLong ,  2012-10-25
	 * describe      : merId|mobileid|balance|cardtype|dealtime|dayPayLtd|monthpayltd|balsign|retCode|retMsg
	 * @see          : @see com.umpay.hfweb.action.base.DirectCommonAction#responseSuccess2Mer(com.umpay.hfweb.model.RequestMsg, com.umpay.hfweb.model.ResponseMsg)
	 * ********************************************/     
	protected void responseSuccess2Mer(RequestMsg requestMsg,ResponseMsg responseMsg){
		MpspMessage infoMsg = responseMsg.getInfoMsg();
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		String retMsg = merId + "|" + mobileId + "|" + infoMsg.getStr(HFBusiDict.BALANCE)
											   + "|" + infoMsg.getStr(HFBusiDict.CARDTYPE)
											   + "|" + infoMsg.getStr(HFBusiDict.DEALTIME)
											   + "|" + infoMsg.getStr(HFBusiDict.DAYPAYLTD)
											   + "|" + infoMsg.getStr(HFBusiDict.MONTHPAYLTD)
											   + "|" + infoMsg.getStr(HFBusiDict.BALSIGN) + "|0000|查询余额成功";
		String sign=platSign(retMsg);
		String meta = retMsg + "|" + sign;
		responseMsg.setDirectResMsg(meta);
	}
	
	/** ********************************************
	 * method name   : getPlainText 
	 * describe      : merId=9996&mobileId=13426399070
	 * modified      : LiuJiLong ,  2012-10-25
	 * @see          : @see com.umpay.hfweb.action.base.DirectCommonAction#getPlainText(com.umpay.hfweb.model.RequestMsg)
	 * ********************************************/     
	protected String getPlainText(RequestMsg requestMsg){
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		
		String content = "merId=" + merId +"&mobileId=" + mobileId;
		return content;
	}

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_MER_QUERY_PAYMENT;//用户手机号可支付余额查询
	}

	@Override
	protected MpspMessage mainProcess(RequestMsg requestMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		
		MpspMessage queryResp = null;
		
		ChannelPayCmd cmd = this.populateCommand(requestMsg);
		// 特殊访问权限校验,IP校验
		requestMsg.put(DataDict.NET_CLIENTIP+"#FLAG", "true");
		MpspMessage checkIPResp = merAuthService.accessCheck(getFunCode(), cmd.getMerId(),requestMsg);
		if(!checkIPResp.isRetCode0000()){
			//特殊访问权限校验未通过
			queryResp = new MpspMessage();
			queryResp.setRetCode(checkIPResp.getRetCode());
			logInfo("MerAuthCheck special Result Failed[RetCode]:%s:商户访问特殊校验未通过",checkIPResp.getRetCode());
			return queryResp;
		}	
		logInfo("MerAuthCheck special Result Success[RetCode]:0000:商户访问特殊校验已通过");
		
		queryResp = tradeService.queryPayment(merId, mobileId);
		return queryResp;
	}

	@Override
	protected MpspMessage specialCheck(RequestMsg requestMsg) {
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		//校验号段信息是否符合要求
		MpspMessage mpspResp =  restService.getMobileSeg(mobileId);
		if(mpspResp.isRetCode0000()){
			String segCode = mpspResp.getStr(HFBusiDict.PROVCODE);
			if(!"010".equals(segCode)){
				mpspResp.setRetCode("1168");
			}
		}
		return mpspResp;
	}
	
	protected  ChannelPayCmd populateCommand(RequestMsg requestMsg){
		return new ChannelPayCmd(requestMsg.getWrappedMap());
	}
}
