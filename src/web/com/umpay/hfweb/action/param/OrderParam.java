package com.umpay.hfweb.action.param;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.ObjectUtil;


/**
 * ******************  类说明  *********************
 * class       :  OrderPageParam
 * @author     :  zhaoyan 
 * description :  支付订单相关页面展示参数类
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class OrderParam{
	private static Logger log = Logger.getLogger(OrderParam.class);

	//商户号
	private String merId;
	//商品号 3.0
	private String goodsId;
	//商品信息
	private String goodsInf;
	//手机号 3.0
	private String mobileId;
	//订单号
	private String orderId;
	//商户日期 格式yyyyMMdd
	private String merDate;
	//金额 为用户展示为 元
	private String amount;
	
	private String amount4dollar;
	//
	private String amtType;
	
	private String bankType;
	
	private String gateId;
	
	private String retUrl;
	
	private String notifyUrl;
	
	private String merPriv;
	
	private String expand;
	
	private String version;
	
	private String sign;
	//商户名称
	private String merName;
	//商品名称
	private String goodsName;
	//二级商户商户名称
	private String merName2;
	//二级商户商品名称
	private String goodsName2;
	
	//服务类型 2：按次；3：包月
	private String servType;
	//服务月份
	private String servMonth;
	//客服电话
	private String cusPhone;
	//银行号
	private String bankId;
	//订单状态
	private String orderState;
	//下单结果页面提示
	private String nextDirect;
	
	private String porderId;
	//分表名
	private String tableName;
	
	private String retCode;
	
	private String plateDate;
	
	private String settleDate;
	//支付返回码，主要用户存储支付失败时，业务层输出的错误返回码
	private String payRetCode;
	
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
	/**
	 * ********************************************
	 * 获取页面下单时，请求信息的明文串
	 * method name   : getPlainText 
	 * modified      : zhaoyan ,  Nov 3, 2011
	 * @see          : @see com.umpay.hfweb.action.command.AbstractOrderCmd#getPlainText()
	 * *******************************************
	 */
	public String getPlainText(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(DataDict.MER_REQ_MERID).append("=").append(merId).append("&");
		if (ObjectUtil.isNotEmpty(goodsId)){
			buffer.append(DataDict.MER_REQ_GOODSID).append("=").append(goodsId).append("&");
		}else{
			buffer.append(DataDict.MER_REQ_GOODSID).append("=").append("&");
		}
		buffer.append(DataDict.MER_REQ_ORDERID).append("=").append(orderId).append("&");
		buffer.append(DataDict.MER_REQ_MERDATE).append("=").append(merDate).append("&");
		if(retCode.equals(DataDict.SUCCESS_RET_CODE)){
			buffer.append(DataDict.MER_REQ_PAYDATE).append("=").append(plateDate).append("&");
		}else{
			buffer.append(DataDict.MER_REQ_PAYDATE).append("=").append(merDate).append("&");
		}
		buffer.append(DataDict.MER_REQ_AMOUNT).append("=").append(amount).append("&");
		buffer.append(DataDict.MER_REQ_AMTTYPE).append("=").append(amtType).append("&");
		buffer.append(DataDict.MER_REQ_BANKTYPE).append("=").append("3").append("&");
		if (ObjectUtil.isNotEmpty(mobileId)){
			buffer.append(DataDict.MER_REQ_MOBILEID).append("=").append(mobileId).append("&");
		}else{
			buffer.append(DataDict.MER_REQ_MOBILEID).append("=").append("&");
		}
		buffer.append("transType").append("=").append("0").append("&");
		buffer.append("settleDate").append("=").append(plateDate).append("&");
		if (ObjectUtil.isNotEmpty(merPriv)){
			buffer.append(DataDict.MER_REQ_MERPRIV).append("=").append(merPriv).append("&");
		}else{
			buffer.append(DataDict.MER_REQ_MERPRIV).append("=").append("&");
		}
		buffer.append(DataDict.RET_CODE).append("=").append(retCode).append("&");
		buffer.append(DataDict.MER_REQ_VERSION).append("=").append(version);
		return buffer.toString();
	}
	/**
	 * ********************************************
	 * method name   : getEncodedText 
	 * description   : 组装完整retUrl
	 * @return       : String
	 * @param        : @param signStr
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 10, 2011 11:47:42 AM
	 * @see          : 
	 * *******************************************
	 */
	public String getEncodedText(String signStr){
		StringBuffer buffer = new StringBuffer();
		try {
			buffer.append(DataDict.MER_REQ_MERID).append("=").append(URLEncoder.encode(merId, "UTF-8")).append("&");
			if (ObjectUtil.isNotEmpty(goodsId)){
				buffer.append(DataDict.MER_REQ_GOODSID).append("=").append(URLEncoder.encode(goodsId, "UTF-8")).append("&");
			}else{
				buffer.append(DataDict.MER_REQ_GOODSID).append("=").append("&");
			}
			buffer.append(DataDict.MER_REQ_ORDERID).append("=").append(URLEncoder.encode(orderId, "UTF-8")).append("&");
			buffer.append(DataDict.MER_REQ_MERDATE).append("=").append(URLEncoder.encode(merDate, "UTF-8")).append("&");
			if(retCode.equals(DataDict.SUCCESS_RET_CODE)){
				buffer.append(DataDict.MER_REQ_PAYDATE).append("=").append(URLEncoder.encode(plateDate, "UTF-8")).append("&");
			}else{
				buffer.append(DataDict.MER_REQ_PAYDATE).append("=").append(URLEncoder.encode(merDate, "UTF-8")).append("&");
			}
			buffer.append(DataDict.MER_REQ_AMOUNT).append("=").append(URLEncoder.encode(amount, "UTF-8")).append("&");
			buffer.append(DataDict.MER_REQ_AMTTYPE).append("=").append(URLEncoder.encode(amtType, "UTF-8")).append("&");
			buffer.append(DataDict.MER_REQ_BANKTYPE).append("=").append(URLEncoder.encode("3", "UTF-8")).append("&");
			if (ObjectUtil.isNotEmpty(mobileId)){
				buffer.append(DataDict.MER_REQ_MOBILEID).append("=").append(URLEncoder.encode(mobileId, "UTF-8")).append("&");
			}else{
				buffer.append(DataDict.MER_REQ_MOBILEID).append("=").append("&");
			}
			buffer.append("transType").append("=").append("0").append("&");
			buffer.append("settleDate").append("=").append(URLEncoder.encode(plateDate, "UTF-8")).append("&");
			if (ObjectUtil.isNotEmpty(merPriv)){
				buffer.append(DataDict.MER_REQ_MERPRIV).append("=").append(URLEncoder.encode(merPriv, "UTF-8")).append("&");
			}else{
				buffer.append(DataDict.MER_REQ_MERPRIV).append("=").append("&");
			}
			buffer.append(DataDict.RET_CODE).append("=").append(URLEncoder.encode(retCode, "UTF-8")).append("&");
			buffer.append(DataDict.MER_REQ_VERSION).append("=").append(URLEncoder.encode(version, "UTF-8")).append("&");
			buffer.append(DataDict.MER_REQ_SIGN).append("=").append(URLEncoder.encode(signStr, "UTF-8"));
			
			return buffer.toString();
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			return null;
		}
	}
	public String getMerId() {
		return merId;
	}
	public void setMerId(String merId) {
		this.merId = merId;
	}
	public String getGateId() {
		return gateId;
	}
	public void setGateId(String gateId) {
		this.gateId = gateId;
	}
	public String getRetUrl() {
		return retUrl;
	}
	public void setRetUrl(String retUrl) {
		this.retUrl = retUrl;
	}
	public String getNotifyUrl() {
		return notifyUrl;
	}
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	public String getMerPriv() {
		return merPriv;
	}
	public void setMerPriv(String merPriv) {
		this.merPriv = merPriv;
	}
	public String getExpand() {
		return expand;
	}
	public void setExpand(String expand) {
		this.expand = expand;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}
	public String getMobileId() {
		return mobileId;
	}
	public void setMobileId(String mobileId) {
		this.mobileId = mobileId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getMerDate() {
		return merDate;
	}
	public void setMerDate(String merDate) {
		this.merDate = merDate;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getMerName() {
		return merName;
	}
	public void setMerName(String merName) {
		this.merName = merName;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public String getServType() {
		return servType;
	}
	public void setServType(String servType) {
		this.servType = servType;
	}
	public String getServMonth() {
		return servMonth;
	}
	public void setServMonth(String servMonth) {
		this.servMonth = servMonth;
	}
	public String getCusPhone() {
		return cusPhone;
	}
	public void setCusPhone(String cusPhone) {
		this.cusPhone = cusPhone;
	}
	public String getBankId() {
		return bankId;
	}
	public void setBankId(String bankId) {
		this.bankId = bankId;
	}
	public String getOrderState() {
		return orderState;
	}
	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}
	public String getNextDirect() {
		return nextDirect;
	}
	public void setNextDirect(String nextDirect) {
		this.nextDirect = nextDirect;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getAmtType() {
		return amtType;
	}
	public void setAmtType(String amtType) {
		this.amtType = amtType;
	}
	public String getBankType() {
		return bankType;
	}
	public void setBankType(String bankType) {
		this.bankType = bankType;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getAmount4dollar() {
		return amount4dollar;
	}
	public void setAmount4dollar(String amount4dollar) {
		this.amount4dollar = amount4dollar;
	}
	public String getPorderId() {
		return porderId;
	}
	public void setPorderId(String porderId) {
		this.porderId = porderId;
	}
	public String getRetCode() {
		return retCode;
	}
	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}
	public String getPlateDate() {
		return plateDate;
	}
	public void setPlateDate(String plateDate) {
		this.plateDate = plateDate;
	}
	public String getSettleDate() {
		return settleDate;
	}
	public void setSettleDate(String settleDate) {
		this.settleDate = settleDate;
	}
	public String getMerName2() {
		return merName2;
	}
	public void setMerName2(String merName2) {
		this.merName2 = merName2;
	}
	public String getGoodsName2() {
		return goodsName2;
	}
	public void setGoodsName2(String goodsName2) {
		this.goodsName2 = goodsName2;
	}
	public String getGoodsInf() {
		return goodsInf;
	}
	public void setGoodsInf(String goodsInf) {
		this.goodsInf = goodsInf;
	}
	public String getPayRetCode() {
		return payRetCode;
	}
	public void setPayRetCode(String payRetCode) {
		this.payRetCode = payRetCode;
	}
}
