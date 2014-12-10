package com.umpay.hfweb.model;

import java.io.Serializable;

public class OrderSyncReq implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8726647553634964007L;
	
	private String BPID; // 业务平台的编号
	
	private String BSID; // 服务代码（认证鉴权平台定义）
	
	private UserID UserID; // 赋值手机号码
	
	private String Opr_Type; // 01-订购   02-取消  03-变更  04-重新下发串码

	
	private Orderlist Orderlist; // 商品订单

	public String getBPID() {
		return BPID;
	}

	public void setBPID(String bPID) {
		BPID = bPID;
	}

	public String getBSID() {
		return BSID;
	}

	public void setBSID(String bSID) {
		BSID = bSID;
	}

	public UserID getUserID() {
		return UserID;
	}

	public void setUserID(UserID userID) {
		UserID = userID;
	}

	public String getOpr_Type() {
		return Opr_Type;
	}

	public void setOpr_Type(String opr_Type) {
		Opr_Type = opr_Type;
	}

	public Orderlist getOrderlist() {
		return Orderlist;
	}

	public void setOrderlist(Orderlist orderlist) {
		Orderlist = orderlist;
	}
	
	
}
