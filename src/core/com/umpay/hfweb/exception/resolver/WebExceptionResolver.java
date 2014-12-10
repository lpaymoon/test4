package com.umpay.hfweb.exception.resolver;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.exception.WebBusiException;
import com.umpay.hfweb.service.MessageService;
import com.umpay.hfweb.util.ObjectUtil;

public class WebExceptionResolver extends SimpleMappingExceptionResolver {
	private static final Logger log = Logger.getLogger(WebExceptionResolver.class);
	
	protected MessageService messageService;

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	@Override
	protected String determineViewName(Exception ex, HttpServletRequest request) {
		String errorMessage = "";
		String retView = "";
		if(ex instanceof WebBusiException){
			WebBusiException we = (WebBusiException)ex;
			errorMessage = we.getMessageDetail();
			if(we.getFunCode().equals(DataDict.FUNCODE_WAPPAGEXD)||we.getFunCode().equals(DataDict.FUNCODE_WAP_ORDSV)
					||we.getFunCode().equals(DataDict.FUNCODE_WAP_SMSRESEND)){
				retView = "wap_error";
			}
		}else{
			errorMessage = messageService.getSystemParam("error.unknown");
			ObjectUtil.logInfo(log, "系统未知异常");
		}
		ObjectUtil.logInfo(log, "系统异常error: " + errorMessage +"--ex.getMessage()="+ex);
		request.setAttribute("errorMessage", errorMessage);
		if(ObjectUtil.isNotEmpty(retView)){
			return retView;
		}
		return super.determineViewName(ex, request);
	}
}
