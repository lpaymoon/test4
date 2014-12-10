package com.umpay.hfweb.action.base;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bs.mpsp.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.exception.WebBusiException;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;

/**
 * ******************  类说明  *********************
 * class       :  DirectBaseAction
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  商户直连的基础类    
 * ***********************************************
 */
public abstract class DirectBaseAction extends BaseAbstractAction{

	@Override
	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap)throws WebBusiException {
			//do some common process
			RequestMsg requestMsg = new RequestMsg();
			requestMsg.putAllParam(HttpUtil.parseRequestParam(request));
			//reqMap.putAll(HttpUtil.parseRequestAttr(request));
			requestMsg.setRpid(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
			ResponseMsg respMap = new ResponseMsg(modelMap);
			requestMsg.setFunCode(getFunCode());
			requestMsg.put(DataDict.NET_CLIENTIP, request.getRemoteAddr());//增加IP地址信息
			try{
				processBussiness(requestMsg,respMap);
				if(DataDict.FUNCODE_QDXD.equals(requestMsg.getFunCode())){
					modelMap.put(HFBusiDict.ORDERID, StringUtil.trim(respMap.getStr(HFBusiDict.ORDERID)));
					modelMap.put(HFBusiDict.ORDERDATE, StringUtil.trim(respMap.getStr(HFBusiDict.ORDERDATE)));
				}
			}catch (Exception e) {
				logError(e);
				respMap.setRetCode(DataDict.SYSTEM_ERROR_CODE);
				responseEorr2Mer(requestMsg,respMap);
			}
			try {
				if(isFileProcess() && respMap.isRetCode0000()){
					String retCode = processFile(requestMsg,response);
					respMap.setRetCode(retCode);
					logInfo("File Process Result [RetCode]:%s",retCode);
					return "";
				}
				StringBuffer buffer = new StringBuffer("<META NAME=\"MobilePayPlatform\" CONTENT=\"");//modify by yangwr 2012-07-19 去掉CONTENT后边的空格
				buffer.append(respMap.getDirectResMsg());
				buffer.append("\">");
				String info2Mer = buffer.toString();
				logInfo("Info2Mer:%s",info2Mer);
				response.setCharacterEncoding("GBK");
				response.getWriter().print(info2Mer);
				response.flushBuffer();
				return "";
			} catch (IOException e) {
				throw new WebBusiException(DataDict.SYSTEM_ERROR_CODE,e);
			}
	}
	
	protected abstract void processBussiness(RequestMsg requestMsg, ResponseMsg responseMsg);
	protected abstract void responseEorr2Mer(RequestMsg requestMsg,ResponseMsg responseMsg);
	
	/**
	 * ********************************************
	 * method name   : isFileProcess 
	 * description   : 是否文件下载
	 * @return       : boolean
	 * @param        : @return
	 * modified      : yangwr ,  Nov 16, 2011  10:04:27 PM
	 * @see          : 
	 * *******************************************
	 */
	protected boolean isFileProcess(){
		return false;
	}
	/**
	 * ********************************************
	 * method name   : processFile 
	 * description   : 文件下载类型处理
	 * @return       : String 返回码
	 * @param        : @param requestMsg
	 * @param        : @param response
	 * @param        : @throws IOException
	 * @see          : 
	 * *******************************************
	 */
	protected String processFile(RequestMsg requestMsg,HttpServletResponse response)throws IOException{
		return DataDict.SUCCESS_RET_CODE;
	}

	/**
	 * ********************************************
	 * method name   : getRetMessage 
	 * description   : 获取返回信息
	 * @return       : String
	 * @param        : @param responseMsg
	 * *******************************************
	 */
	protected String getRetMessage(ResponseMsg responseMsg){
		String retMsg = "";
		String retCode = responseMsg.getRetCode();
		String retCodeBussi = responseMsg.getRetCodeBussi();
		if(!ObjectUtil.isEmpty(retCodeBussi)){
			retMsg = messageService.getMessage(retCodeBussi);
		}
		if(ObjectUtil.isEmpty(retMsg)){
			retMsg = messageService.getMessage(retCode);
		}
		return retMsg;
	}
}
