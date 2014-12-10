package com.umpay.hfweb.action.command;

import java.util.HashMap;
import java.util.Map;

import com.umpay.hfweb.util.ObjectUtil;

/**
 * @Title: AbstractOrderCmd.java
 * @Package com.umpay.hfweb.action.command
 * @Description: 支付订单相关字段对应实体类
 * @author zhaoyan
 * @date Oct 28, 2011 1:59:04 PM
 * @version V1.0
 */
public abstract class AbstractOrderCmd{
	//商户号
	protected String merId;
	//商品号 3.0
	protected String goodsId;
	//手机号 3.0
	protected String mobileId;
	//订单号
	protected String orderId;
	//商户日期 格式yyyyMMdd
	protected String merDate;
	//金额 单位：分
	protected String amount;
	//货币类型 定值：02
	protected String amtType;
	//银行类型 定值：3
	protected String bankType;
	//通知url
	protected String notifyUrl;
	//商户私有信息
	protected String merPriv;
	//商户扩展信息
	protected String expand;
	//版本号 定值：3.0
	protected String version;
	//签名信息
	protected String sign;
	//业务类型  20130903 add （panxingwu）
	protected String businesstype;

	/**
	 * 初始化参数值
	 * @param reqMap
	 */
	public AbstractOrderCmd(Map<String,Object> reqMap){
		this.merId = (String)reqMap.get("merId");
		this.goodsId = (String)reqMap.get("goodsId");
		this.mobileId = (String)reqMap.get("mobileId");
		this.orderId = (String)reqMap.get("orderId");
		this.merDate = (String)reqMap.get("merDate");
		this.amount = (String)reqMap.get("amount");
		this.amtType = (String)reqMap.get("amtType");
		this.bankType = (String)reqMap.get("bankType");
		this.notifyUrl = (String)reqMap.get("notifyUrl");
		this.merPriv = (String)reqMap.get("merPriv");
		this.expand = (String)reqMap.get("expand");
		this.version = (String)reqMap.get("version");
		this.sign = (String)reqMap.get("sign");
		this.businesstype = (String)reqMap.get("businessType");
	}
	public AbstractOrderCmd(){
		
	}
	
	/**
	 * @Title: getPayPlain
	 * @Description: 获取后台直连下单时，请求信息的明文串
	 * @param @param payFlag
	 * @param @return    String
	 * @return String    
	 * @throws
	 */
	public abstract String getPlainText();
	public String getExpand() {
		return expand;
	}
	
	public String getMerDate() {
		return merDate;
	}

	public String getMerId() {
		return merId;
	}

	public String getMerPriv() {
		return merPriv;
	}

	public String getOrderId() {
		return orderId;
	}


	public String getVersion() {
		return version;
	}


	public String getAmtType() {
		return amtType;
	}


	public String getGoodsId() {
		return goodsId;
	}

	public String getBankType() {
		return bankType;
	}

	public String getAmount() {
		return amount;
	}


	public String getMobileId() {
		return mobileId;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}
	
	public String getSign() {
		return sign;
	}
	
	public Map<String, String> getLogData() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("orderId", orderId);
		map.put("merDate", merDate);
		map.put("merId", merId);
		map.put("goodsId", goodsId);
		map.put("amount", amount);
		if(ObjectUtil.isNotEmpty(mobileId)){
			map.put("mobileId", mobileId);
		}
		return map;
	}
	public String getBusinessType() {
		return businesstype;
	}
	public void setBusinessType(String businessType) {
		this.businesstype = businessType;
	}

	
	
}
