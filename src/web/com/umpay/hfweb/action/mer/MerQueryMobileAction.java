package com.umpay.hfweb.action.mer;

import com.bs2.mpsp.XmlMobile;
import com.umpay.api.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectCommonAction;
import com.umpay.hfweb.action.command.ChannelPayCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;

public class MerQueryMobileAction extends DirectCommonAction{

	@Override
	protected String getPlainText(RequestMsg requestMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		
		String content = "merId=" + merId +"&mobileId=" + mobileId;
		return content;
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
		
		queryResp = tradeService.queryMobileInfo(merId, mobileId);
		return queryResp;
	}

	
	/** ********************************************
	 * method name   : responseEorr2Mer 
	 * modified      : LiuJiLong ,  2012-12-12
	 * @see          : @see com.umpay.hfweb.action.base.DirectCommonAction#responseEorr2Mer(com.umpay.hfweb.model.RequestMsg, com.umpay.hfweb.model.ResponseMsg)
	 * ********************************************/     
	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		String memo = getRetMessage(responseMsg);
		String meta = merId+"|"+mobileId+"|||||||||||||||||||||||||||||||||||" + responseMsg.getRetCode() +
				"|" + memo + "||";
		responseMsg.setDirectResMsg(meta);
	}

	
	/** ********************************************
	 * method name   : responseSuccess2Mer 
	 * modified      : LiuJiLong ,  2012-12-12
	 * @see          : @see com.umpay.hfweb.action.base.DirectCommonAction#responseSuccess2Mer(com.umpay.hfweb.model.RequestMsg, com.umpay.hfweb.model.ResponseMsg)
	 * ********************************************/     
	@Override
	protected void responseSuccess2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		MpspMessage infoMsg = responseMsg.getInfoMsg();
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		String retMsg = merId + "|" + mobileId + "|" + StringUtil.trim(infoMsg.getStr(HFBusiDict.PROVCODE))
											   + "|" + StringUtil.trim(infoMsg.getStr("provname"))
											   + "|" + StringUtil.trim(infoMsg.getStr(HFBusiDict.AREACODE))
											   + "|" + StringUtil.trim(infoMsg.getStr(HFBusiDict.AREANAME))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.CITYCODE))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.AMT))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.Seg_CardType))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.BALANCE))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.MFee_AccessTime))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.BUYAMT))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.FEETYPE))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.BALSIGN))
											   + "|" + StringUtil.trim(infoMsg.getStr("userStatus")	)										   
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.SALEAMT))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.CURTYPE))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.DODATE))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.MFEE_Status))											   
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.CARDTYPE))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.CANCELSIG))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.FEEAMOUNT))										   
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.MFEE_PaySign))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.MFEE_LastFee))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.MFEE_AccountID))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.ACCOUNTTYPE))											   
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.SUBSTATE))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.ACCOUNTSTATE))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.MFEE_PreFee))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.MFEE_START_DATE))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.MFEE_CreditDeep))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.OPENSIG))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.MEMO))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.BUYBALANCE))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.MFEE_FeeCount))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.CS_BALANCE))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.CURSIGN))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.MFEE_PaySignCN))
											   + "|" + StringUtil.trim(infoMsg.getStr("stateDate"))
											   + "|" + StringUtil.trim(infoMsg.getStr("mobilestate"))
											   + "|" + StringUtil.trim(infoMsg.getStr(XmlMobile.CURBALANCE)) + "|0000|查询手机帐号信息成功";
		String sign=platSign(retMsg);
		String meta = retMsg + "|" + sign;
		responseMsg.setDirectResMsg(meta);
	}

	
	/** ********************************************
	 * method name   : specialCheck 
	 * modified      : LiuJiLong ,  2012-12-12
	 * @see          : @see com.umpay.hfweb.action.base.DirectCommonAction#specialCheck(com.umpay.hfweb.model.RequestMsg)
	 * ********************************************/     
	@Override
	protected MpspMessage specialCheck(RequestMsg requestMsg) {
		return null;//用 null跳过特殊效验
	}

	
	/** ********************************************
	 * method name   : getFunCode 
	 * modified      : LiuJiLong ,  2012-12-12
	 * @see          : @see com.umpay.hfweb.action.base.BaseAbstractAction#getFunCode()
	 * ********************************************/     
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_MER_QUERY_USERINFO;//用户手机号可支付余额查询
	}
	
	/** ********************************************
	 * method name   : populateCommand 
	 * description   : IP效验用
	 * @return       : ChannelPayCmd
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : LiuJiLong ,  2012-12-12 下午09:03:31
	 * @see          : 
	 * ********************************************/      
	protected  ChannelPayCmd populateCommand(RequestMsg requestMsg){
		return new ChannelPayCmd(requestMsg.getWrappedMap());
	}

}
