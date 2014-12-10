package com.umpay.hfweb.action.command;

import java.util.Map;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.ObjectUtil;
/**
 * ******************  类说明  *********************
 * class       :  DirectOrderV2Cmd
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  后台直连处理参数处理类Ver2.0
 * @see        :                        
 * ***********************************************
 */
public class DirectMonthOrderV2Cmd extends DirectOrderCmd{
	
	private String funCode;
	private String rdpwd;
	private String remark;
	
	public DirectMonthOrderV2Cmd(Map<String,Object> reqMap) {

		this.merId = ObjectUtil.trim(reqMap.get(DataDict.MER_REQ_MERID_V2));
		this.goodsId = ObjectUtil.trim(reqMap.get(DataDict.MER_REQ_GOODSID_V2));
		this.mobileId = ObjectUtil.trim(reqMap.get(DataDict.MER_REQ_MOBILEID_V2));
		this.orderId = mobileId+goodsId;
		this.merDate = ObjectUtil.trim(reqMap.get(DataDict.MER_REQ_DATETIME_V2));
		this.amount = ObjectUtil.trim(reqMap.get(DataDict.MER_REQ_AMOUNT_V2));
		this.sign = ObjectUtil.trim(reqMap.get(DataDict.MER_REQ_SIGN_V2));
		
		this.funCode = ObjectUtil.trim(reqMap.get(DataDict.MER_REQ_FUNCODE_V2));
		this.rdpwd = ObjectUtil.trim(reqMap.get(DataDict.MER_REQ_RDPWD_V2));
		this.remark = ObjectUtil.trim(reqMap.get(DataDict.MER_REQ_REMARK_V2));
		this.notifyUrl = "";
		this.amtType ="02";
		this.bankType = "3";
		this.version = "2.0";
	}
	

	public String getPlainText(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("FUNCODE=").append(this.getFunCode());
		buffer.append("&SPID=").append(this.getMerId());
		buffer.append("&GOODSID=").append(this.getGoodsId());
		buffer.append("&AMOUNT=").append(this.getAmount());
		buffer.append("&DATETIME=").append(this.getMerDate());
		buffer.append("&MOBILEID=").append(this.getMobileId());
		buffer.append("&RDPWD=").append(this.getRdpwd());
		return buffer.toString();
	}

	public String getFunCode() {
		return funCode;
	}

	public String getRdpwd() {
		return rdpwd;
	}

	public String getRemark() {
		return remark;
	}
	
}
