package com.umpay.hfweb.model;

import java.io.Serializable;

public class OrderSchema implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8874049230324237431L;
	
	private String OrderID; // 订单号
	
	private String Num; // 订单商品种类数
	
	private OrderLine List; // 订单行信息列表

	public String getOrderID() {
		return OrderID;
	}

	public void setOrderID(String orderID) {
		OrderID = orderID;
	}

	public String getNum() {
		return Num;
	}

	public void setNum(String num) {
		Num = num;
	}

	public OrderLine getList() {
		return List;
	}

	public void setList(OrderLine list) {
		List = list;
	}
	
	

}
