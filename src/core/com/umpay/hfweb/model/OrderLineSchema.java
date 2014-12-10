package com.umpay.hfweb.model;

import java.io.Serializable;

public class OrderLineSchema implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7606128543249871483L;

	private String BPID; // 第三方业务平台的编号，由认证鉴权平台分配
	
	private String OrderID; // 订单号
	
	private String MercID; // 消费商户ID，即消费时POS所属商户ID
	
	private String MercName; // 商户名称
	
	private String MercAddr; // 商户地址
	
	private String OrderDate; // 订单生成日期
	
	private String OrderLineID; // 订单行号
	
	/**
	 * 订单状态，
	5：新建（未消费）
	6：有效（未消费完）
	8：消费完成
	4：取消
	 */
	private String OrderStatus; // 
	
	private String LastConsDate; // 订单最后一次消费日期，订单初始值可以赋值OrderDate订单生成日期
	
	private String GoodsID; // 商品ID
	
	private String GoodsName; // 商品名称
	
	private String GoodsName_Pos; // POS机展示商品名称
	
	private String GoodsDetail; // 商品详情
	
	private String GoodsPrice; // 商品价格，单位分
	
	private String BeginTime; // 开始有效期
	
	private String EndTime; // 截止有效期
	
	private String GoodsType; // 商品类型，1 表示实物类商品， 2 表示金额类商品
	
	private String GoodsNum; // 商品总数量 ，当GoodsType=1时，该字段有效
	
	private String GoodsNum_Rem; // 商品剩余数量，当GoodsType=1时，该字段有效
	
	private String GoodsAmount; // 商品总金额，当GoodsType=2时，该字段有效，单位分
	
	private String GoodsAmount_Rem; // 商品剩余金额，当GoodsType=2时，该字段有效，单位分
	
	private String OriginalUrl; // 商品原始链接,订单在BP平台的URL地址，用户点击之后可以直接打开BP网站的订单信息（如果是用户在BP平台是登录状态）
	
	private String GoodsPicPath; // 商品图片链接,是商品图片的URL，由BP提供。如果为空，www/cs门户，安排一个缺省图片，没有图片时显示缺省图片
	
	/**
	 * 业务自定义字段，当上面的接口不能满足业务平台的要求时，利用本字段扩展。本字段的编码规则和业务含义由业务平台和POS机商定，认证鉴权平台不做限制，透传
		增加商户客服电话字段，格式：
		tag/value;tag/value格式，tag名同数据库字段名（第三方订单表增加客户电话字段【O】：Ser_Tel）
		如：ser_tel/4008123456

	 */
	private String GoodsResdata;

	public String getBPID() {
		return BPID;
	}

	public void setBPID(String bPID) {
		BPID = bPID;
	}

	public String getOrderID() {
		return OrderID;
	}

	public void setOrderID(String orderID) {
		OrderID = orderID;
	}

	public String getMercID() {
		return MercID;
	}

	public void setMercID(String mercID) {
		MercID = mercID;
	}

	public String getMercName() {
		return MercName;
	}

	public void setMercName(String mercName) {
		MercName = mercName;
	}

	public String getMercAddr() {
		return MercAddr;
	}

	public void setMercAddr(String mercAddr) {
		MercAddr = mercAddr;
	}

	public String getOrderDate() {
		return OrderDate;
	}

	public void setOrderDate(String orderDate) {
		OrderDate = orderDate;
	}

	public String getOrderLineID() {
		return OrderLineID;
	}

	public void setOrderLineID(String orderLineID) {
		OrderLineID = orderLineID;
	}

	public String getOrderStatus() {
		return OrderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		OrderStatus = orderStatus;
	}

	public String getLastConsDate() {
		return LastConsDate;
	}

	public void setLastConsDate(String lastConsDate) {
		LastConsDate = lastConsDate;
	}

	public String getGoodsID() {
		return GoodsID;
	}

	public void setGoodsID(String goodsID) {
		GoodsID = goodsID;
	}

	public String getGoodsName() {
		return GoodsName;
	}

	public void setGoodsName(String goodsName) {
		GoodsName = goodsName;
	}

	public String getGoodsName_Pos() {
		return GoodsName_Pos;
	}

	public void setGoodsName_Pos(String goodsName_Pos) {
		GoodsName_Pos = goodsName_Pos;
	}

	public String getGoodsDetail() {
		return GoodsDetail;
	}

	public void setGoodsDetail(String goodsDetail) {
		GoodsDetail = goodsDetail;
	}

	public String getGoodsPrice() {
		return GoodsPrice;
	}

	public void setGoodsPrice(String goodsPrice) {
		GoodsPrice = goodsPrice;
	}

	public String getBeginTime() {
		return BeginTime;
	}

	public void setBeginTime(String beginTime) {
		BeginTime = beginTime;
	}

	public String getEndTime() {
		return EndTime;
	}

	public void setEndTime(String endTime) {
		EndTime = endTime;
	}

	public String getGoodsType() {
		return GoodsType;
	}

	public void setGoodsType(String goodsType) {
		GoodsType = goodsType;
	}

	public String getGoodsNum() {
		return GoodsNum;
	}

	public void setGoodsNum(String goodsNum) {
		GoodsNum = goodsNum;
	}

	public String getGoodsNum_Rem() {
		return GoodsNum_Rem;
	}

	public void setGoodsNum_Rem(String goodsNum_Rem) {
		GoodsNum_Rem = goodsNum_Rem;
	}

	public String getGoodsAmount() {
		return GoodsAmount;
	}

	public void setGoodsAmount(String goodsAmount) {
		GoodsAmount = goodsAmount;
	}

	public String getGoodsAmount_Rem() {
		return GoodsAmount_Rem;
	}

	public void setGoodsAmount_Rem(String goodsAmount_Rem) {
		GoodsAmount_Rem = goodsAmount_Rem;
	}

	public String getOriginalUrl() {
		return OriginalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		OriginalUrl = originalUrl;
	}

	public String getGoodsPicPath() {
		return GoodsPicPath;
	}

	public void setGoodsPicPath(String goodsPicPath) {
		GoodsPicPath = goodsPicPath;
	}

	public String getGoodsResdata() {
		return GoodsResdata;
	}

	public void setGoodsResdata(String goodsResdata) {
		GoodsResdata = goodsResdata;
	}
	
	
	
}
