package com.umpay.hfweb.action.mer;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectCommonAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;

/**
 * ******************  类说明  *********************
 * class       :  QueryBalanceAction
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  查询用户余额
 * @see        :                        
 * ***********************************************
 */
public class MerQueryBalanceAction extends DirectCommonAction{

	protected void responseEorr2Mer(RequestMsg requestMsg,ResponseMsg responseMsg){		
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		String amount = "";
		String memo = getRetMessage(responseMsg);
		String meta = merId+"|"+mobileId+"|"+amount+"|" + responseMsg.getRetCode() +
				"|" + memo + "||";
		responseMsg.setDirectResMsg(meta);
	}
	protected void responseSuccess2Mer(RequestMsg requestMsg,ResponseMsg responseMsg){
		MpspMessage infoMsg = responseMsg.getInfoMsg();
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		String retMsg = merId+"|"+mobileId+"|"+infoMsg.getStr(HFBusiDict.AMT)+"|0000|查询余额成功|3.0";
		String sign=platSign(retMsg);
		String meta = retMsg + "|" + sign;
		responseMsg.setDirectResMsg(meta);
	}
	protected String getPlainText(RequestMsg requestMsg){
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//商户日期
		String merDate = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERDATE));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		//版本号
		String version = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_VERSION));
		String content = "merId="+merId+"&merDate="+merDate+"&mobileId="+mobileId+"&version="+version;
		return content;
	}

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_MER_QUERY_BALANCE;
	}

	@Override
	protected MpspMessage mainProcess(RequestMsg requestMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		MpspMessage queryResp = tradeService.queryBalance(merId, mobileId);
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
			if(!"591".equals(segCode)){
				mpspResp.setRetCode("1168");
			}
		}
		return mpspResp;
	}
}
