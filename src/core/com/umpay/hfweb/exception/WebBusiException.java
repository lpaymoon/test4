package com.umpay.hfweb.exception;

public class WebBusiException extends BusinessException{

	private static final long serialVersionUID = 1L;

	private String messageDetail;
	
	private String funCode;
	
	public WebBusiException(String code,Exception e) {
		super(code,e);
	}
	public String getMessageDetail() {
		return messageDetail;
	}
	public void setMessageDetail(String messageDetail) {
		this.messageDetail = messageDetail;
	}
	public String getFunCode() {
		return funCode;
	}
	public void setFunCode(String funCode) {
		this.funCode = funCode;
	}
}
