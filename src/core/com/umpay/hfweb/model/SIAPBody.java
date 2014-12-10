package com.umpay.hfweb.model;

import java.io.Serializable;

public class SIAPBody implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2172124741424272627L;
	
	private OrderSyncReq OrderSyncReq; // 具体的消息
	
	private OrderSyncResp OrderSyncResp; // 返回

	public OrderSyncReq getOrderSyncReq() {
		return OrderSyncReq;
	}

	public void setOrderSyncReq(OrderSyncReq orderSyncReq) {
		OrderSyncReq = orderSyncReq;
	}

	public OrderSyncResp getOrderSyncResp() {
		return OrderSyncResp;
	}

	public void setOrderSyncResp(OrderSyncResp orderSyncResp) {
		OrderSyncResp = orderSyncResp;
	}

	

}
