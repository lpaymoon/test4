package com.umpay.hfweb.action.mer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.umpay.hfweb.action.base.DirectCommonAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;

public class MerPushSmsV2Action extends DirectCommonAction{

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_MER_PUSH_SMS_V2;
	}
	@Override
	protected String getPlainText(RequestMsg requestMsg) {
		StringBuffer sb = new StringBuffer();
		sb.append("FUNCODE=");
        sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_FUNCODE_V2)));
        sb.append("&SPID=");
        sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID_V2)));
        sb.append("&ORDERID=");
        sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID_V2)));
        sb.append("&AMOUNT=");
        sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_AMOUNT_V2)));
        sb.append("&DATETIME=");
        sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_DATETIME_V2)));
        sb.append("&MOBILEID=");
        sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MOBILEID_V2)));
        sb.append("&RDPWD=");
        sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_RDPWD_V2)));
        sb.append("&REMARK=");
        sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_REMARK_V2)));
        sb.append("&REMARK2=");
        sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_REMARK2_V2)));
        sb.append("&URL=");
        sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_URL_V2)));
		return sb.toString();
	}

	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		String memo = getRetMessage(responseMsg);
		StringBuffer meta = new StringBuffer(responseMsg.getRetCode()).append("|").append("|||||").append(memo);
		responseMsg.setDirectResMsg(meta.toString());
	}

	@Override
	protected void responseSuccess2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {		
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
		String retMsg = responseMsg.getRetCode()+"|"+merId;
		String sign=platSign(retMsg);
		String meta = retMsg + "|" + sign;
		responseMsg.setDirectResMsg(meta);
		
	}
	@Override
	protected boolean isVer3(){
		return false;
	}

	@Override
	public MpspMessage specialCheck(RequestMsg requestMsg) {
		MpspMessage respMsg = new MpspMessage();
		String funCodeFromMer =  ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_FUNCODE_V2));
		if(!funCodeFromMer.equals("8820")){
			respMsg.setRetCode("1112");
			logInfo("参数不正确！");
			return respMsg;
		}
		//校验短信模板
		boolean check=false;
		String merId = requestMsg.getStr(DataDict.MER_REQ_MERID_V2);
		String strTemplateNum=ObjectUtil.trim(messageService.getSystemParam("Template."+merId));
		if(ObjectUtil.isEmpty(strTemplateNum)) {
			respMsg.setRetCode("1128");
			logInfo("商户未开通此项支付服务！");
			return respMsg;
		}else{
			int iTemplateNum = Integer.parseInt(strTemplateNum);
			String remark = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_REMARK_V2));
			for(int i=0;i<iTemplateNum;i++){
				String pattern = ObjectUtil.trim(messageService.getSystemParam("Pattern."+merId+"."+i));
				logger.debug("Pattern."+merId+"."+i+"="+pattern);
				Pattern p = Pattern.compile(pattern);
				Matcher m = p.matcher(remark);
				check = m.matches();
				if(check) break;
			}
			if(!check){
				//1150 商户未开通该版本接口
				respMsg.setRetCode("1150");
				logInfo("商户未开通该版本接口！");
			}else{
				respMsg.setRetCode0000();
			}
		}
		return respMsg;
	}

	@Override
	protected MpspMessage mainProcess(RequestMsg requestMsg) {
		MpspMessage respMsg = new MpspMessage(); 
		String merId = requestMsg.getStr(DataDict.MER_REQ_MERID_V2);
		String mobileId = requestMsg.getStr(DataDict.MER_REQ_MOBILEID_V2);
		String smsContent = requestMsg.getStr(DataDict.MER_REQ_REMARK_V2);
		smsService.pushSms(merId, mobileId, smsContent);
		respMsg.setRetCode0000();
		return respMsg;
	}

}
