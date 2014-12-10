package com.umpay.hfweb.model;

import java.io.Serializable;

public class SIAPHeader implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1340461761432910977L;
	
	private String TransactionID; // 请求和应答消息都必须，格式如下：14位的时间串+6位的Sequence

	private String Version; // 请求和应答消息都必须，该接口消息的版本号，本次的接口消息的版本为“2.0”

	private String MessageName; // 消息名称,和<SIAP-Body>中的具体消息一致
	
	private String TestFlag; // 测试交易标记，0表示正式交易，1表示测试交易
	
	private String ReturnCode; // 请求消息可选，应答消息必须，返回值，见第9章的定义
	
	private String ReturnMessage; // 请求消息可选，应答消息必须，返回值详细信息，见第9章的定义
	
	private SendAddress SendAddress; // 发送网元信息
	
	private RecvAddress RecvAddress; // 接收网元信息

	public String getTransactionID() {
		return TransactionID;
	}

	public void setTransactionID(String transactionID) {
		TransactionID = transactionID;
	}

	public String getVersion() {
		return Version;
	}

	public void setVersion(String version) {
		Version = version;
	}

	public String getMessageName() {
		return MessageName;
	}

	public void setMessageName(String messageName) {
		MessageName = messageName;
	}

	public String getTestFlag() {
		return TestFlag;
	}

	public void setTestFlag(String testFlag) {
		TestFlag = testFlag;
	}

	public String getReturnCode() {
		return ReturnCode;
	}

	public void setReturnCode(String returnCode) {
		ReturnCode = returnCode;
	}

	public String getReturnMessage() {
		return ReturnMessage;
	}

	public void setReturnMessage(String returnMessage) {
		ReturnMessage = returnMessage;
	}

	public SendAddress getSendAddress() {
		return SendAddress;
	}

	public void setSendAddress(SendAddress sendAddress) {
		SendAddress = sendAddress;
	}

	public RecvAddress getRecvAddress() {
		return RecvAddress;
	}

	public void setRecvAddress(RecvAddress recvAddress) {
		RecvAddress = recvAddress;
	}


	
	
}
