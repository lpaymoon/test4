package com.umpay.hfweb.action.mer.bill;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import com.umpay.hfweb.action.base.DirectCommonAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.ObjectUtil;

/**
 * ******************  类说明  *********************
 * class       :  MerDayUndoBillAction
 * @author     :  xuwei 20130716
 * @version    :  1.0  
 * description :  商户取冲正交易对账文件V3.0                      
 * ***********************************************
 */
public class MerDayUndoBillAction extends DirectCommonAction{
    @Override
	protected String getFunCode() {
		return DataDict.FUNCODE_UNDO_DAY_BILL;
	}
	@Override
	protected String getPlainText(RequestMsg requestMsg) {
		StringBuffer buffer = new StringBuffer();
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//支付日期
		String payDateStr = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_PAYDATE));
		//版本号
		String version = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_VERSION));
	    buffer.append("merId="+merId);
	    buffer.append("&payDate="+payDateStr);
	    buffer.append("&version="+version);
	    return buffer.toString();
	}
	@Override
	protected MpspMessage mainProcess(RequestMsg requestMsg) {
		//文件处理，已在processFile里实现
		return null;
	}
	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
		ResponseMsg responseMsg) {
		String memo = getRetMessage(responseMsg);
	
		StringBuffer meta = new StringBuffer(responseMsg.getRetCode()).append("|").append("|||||").append(memo);
		responseMsg.setDirectResMsg(meta.toString());
	}
	@Override
	protected void responseSuccess2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		//文件处理，不做修改
	}
	@Override
	protected MpspMessage specialCheck(RequestMsg requestMsg) {
		MpspMessage rtnMap = new MpspMessage();
		rtnMap.setRetCode0000();
		
		String payDateStr = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_PAYDATE));
		Date payDate = DateUtil.parseDateyyyyMMdd(payDateStr);
		String version = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_VERSION));
		if(payDate == null){
			//1112:参数不正确
			rtnMap.setRetCode("1112");
			logInfo("请求日期不能为空！");
			return rtnMap;
		}
		//1-3 请求日期校验：只能取前一天或者更早的对账文件
		if(DateUtil.getDateyyyMMdd(payDate).compareTo(DateUtil.getDateyyyMMdd(DateUtil.getDateBefore(new Date()))) > 0){
			//1112:参数不正确
			rtnMap.setRetCode("1112");
			logInfo("请求日期只能是前一天或者更早的对账文件！");
			return rtnMap;
		}
		
		//1-4 version 要求为3.0
		if(!version.equals("3.0")){
			rtnMap.setRetCode("1112");
			logInfo("版本号必须为3.0！");
			return rtnMap;
		}
		return rtnMap;
	}
	@Override
	protected boolean isFileProcess(){
		return true;
	}
	
	 @Override
	 protected String processFile(RequestMsg requestMsg,HttpServletResponse response) throws IOException {
        // 商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		// 支付日期
		String payDateStr = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_PAYDATE));
        response.setContentType("text/text;charset=GB2312");
		PrintWriter out = response.getWriter();
		String billPath = ObjectUtil.unifyUrl(messageService.getSystemParam(DataDict.FILEPATH_BILL_UNDO));
        String filePath = billPath + "/" + merId + "." + payDateStr + ".txt";
		File billFile = new File(filePath);
		if (!billFile.exists()) {
			out.print("TRADEDETAIL-START," + merId + "," + payDateStr+ ",3.0,0001,对帐文件暂时不存在\r\n");
			out.print("TRADEDETAIL-END," + merId + "," + payDateStr+ ",-1,-1\r\n");
			logInfo(merId + "." + payDateStr + ".txt 对帐文件暂时不存在！");
		} else {
			FileReader input = new FileReader(filePath);
			BufferedReader br = new BufferedReader(input);
			String str = null;
			while ((str = br.readLine()) != null) {
				out.print(str + "\r\n");
			}
			br.close();
		}
		return DataDict.SUCCESS_RET_CODE;
	} 
    
		
		
}
