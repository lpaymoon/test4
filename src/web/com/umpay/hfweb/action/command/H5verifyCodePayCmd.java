package com.umpay.hfweb.action.command;

import java.util.Map;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.ObjectUtil;
/**
 * 渠道支付处理参数处理类
 * @author yangwr
 */
public class H5verifyCodePayCmd extends AbstractOrderCmd{
	private String chnlId;
	private String goodsInf;

	public String getGoodsInf() {
		return goodsInf;
	}
	public void setGoodsInf(String goodsInf) {
		this.goodsInf = goodsInf;
	}
	public H5verifyCodePayCmd(){
		
	}
	public H5verifyCodePayCmd(Map<String,Object> reqMap) {
		this.chnlId = (String)reqMap.get("chnlId");
		this.merId = (String)reqMap.get("merId");
		this.goodsId = (String)reqMap.get("goodsId");
		this.amount = (String)reqMap.get("amount");
		this.goodsInf = (String)reqMap.get("goodsInf");
	}
	
	/**
	 * 获取请求信息的明文串
	 * @return String
	 */
	public String getPlainText(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(DataDict.MER_REQ_CHNLID).append("=").append(chnlId).append("&");
		buffer.append(DataDict.MER_REQ_MERID).append("=").append(merId).append("&");
		buffer.append(DataDict.MER_REQ_GOODSID).append("=").append(ObjectUtil.trim(goodsId)).append("&");
		buffer.append(DataDict.MER_REQ_GOODSINF).append("=").append(ObjectUtil.trim(goodsInf)).append("&");
		buffer.append(DataDict.MER_REQ_AMOUNT).append("=").append(amount);

		return buffer.toString();
	}
	public String getChnlId() {
		return chnlId;
	}
	public void setChnlId(String chnlId) {
		this.chnlId = chnlId;
	}
	
}
