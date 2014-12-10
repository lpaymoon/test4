package com.umpay.hfweb.exception;

public class BusinessException  extends Exception { 
  
	private static final long serialVersionUID = 1L;

	private String code;

	private String message;

	
	public BusinessException(String code,Exception e){
			this(code,null,e);
	}
 
	public BusinessException(String code, String message,Exception e) {
		super(code,e);
		this.code = code;
		this.message = message; 
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	} 
	
	public void setMessage(String msg){
		this.message = msg;
	}
}
