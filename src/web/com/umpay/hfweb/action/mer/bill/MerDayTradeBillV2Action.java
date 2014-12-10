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
 * class       :  MerDayTradeAddressV2Action
 * @author     :  Administrator
 * @version    :  1.0  
 * description :  商户取日交易文件（仅下载包含地区信息的交易对账）
 * @see        :                        
 * ***********************************************
 */

public class MerDayTradeBillV2Action extends DirectCommonAction{

	@Override
	protected String getPlainText(RequestMsg requestMsg) {
		StringBuffer buffer = new StringBuffer();
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
		//日期
		String reqDateStr = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_REQDATE_V2));
		//随机数
		String rand = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_RDPWD_V2));
	    buffer.append("SPID="+merId);
	    buffer.append("&REQDATE="+reqDateStr);
	    buffer.append("&RDPWD="+rand);
	    return buffer.toString();
	}

	@Override
	protected MpspMessage mainProcess(RequestMsg requestMsg) {
		//已在processFile里实现
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
		
		String payDateStr = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_REQDATE_V2));
		Date reqDate = DateUtil.parseDateyyyyMMdd(payDateStr);
		
		if(reqDate == null){
			//1112:参数不正确
			rtnMap.setRetCode("1112");
			logInfo("请求日期不能为空！");
			return rtnMap;
		}
		//1-3 请求日期校验：只能取前一天或者更早的对账文件
		if(DateUtil.getDateyyyMMdd(reqDate).compareTo(DateUtil.getDateyyyMMdd(DateUtil.getDateBefore(new Date()))) > 0){
			//1112:参数不正确
			rtnMap.setRetCode("1112");
			logInfo("请求日期只能是前一天或者更早的对账文件！");
			return rtnMap;
		}
		

		return rtnMap;
	}

	@Override
	protected String getFunCode() {
		 return DataDict.FUNCODE_MER_TRADE2_BILL_V2;
	}
	@Override
    protected boolean isVer3(){
		return false;
	}
	@Override
	protected boolean isFileProcess(){
		return true;
	}
    @Override
    protected String processFile(RequestMsg requestMsg,HttpServletResponse response) throws IOException{
    	// 商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
		// 日期
		String reqDate = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_REQDATE_V2));
    	
    	response.setContentType("text/text;charset=GB2312");
        PrintWriter out = response.getWriter();
       
        String dirpath = ObjectUtil.unifyUrl(messageService.getSystemParam(DataDict.FILEPATH_BILL_TRADE2_V2));
        File file=new File(dirpath+"/"+merId+"."+reqDate+"trans.txt");
		if(!file.exists()){
			//当前路径没有对帐文件
		    out.print("&UMPAYLOUTHEAD"+merId+"|"+reqDate+"|"+reqDate+"\r\n");
			out.print("&UMPAYLOUTTAIL"+merId+"|-1\r\n");
			logInfo(dirpath+"/"+merId+"."+reqDate+"trans.txt 对帐文件暂时不存在！");
		}else{
			FileReader input = new FileReader(file);
			BufferedReader br = new BufferedReader(input);
            String str =null;
            while((str=br.readLine())!=null){
            	out.print(str+"\r\n");
            }
            br.close();
        }
		return DataDict.SUCCESS_RET_CODE;
  }
}