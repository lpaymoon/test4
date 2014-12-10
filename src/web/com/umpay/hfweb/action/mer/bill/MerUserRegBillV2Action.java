package com.umpay.hfweb.action.mer.bill;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import com.umpay.hfweb.action.base.DirectCommonAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;
/**
 * ******************  类说明  *********************
 * class       :  MonthUserRegBillAction
 * @author     :  Administrator
 * @version    :  1.0  
 * description :  商户获取定制包月关系用户信息
 * @see        :                        
 * ***********************************************
 */
public class MerUserRegBillV2Action extends DirectCommonAction {

	@Override
	protected String getPlainText(RequestMsg requestMsg) {
		StringBuffer buffer = new StringBuffer();
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
		//标签
		String regtag = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_TAG_V2));
		//随机数
		String rand = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_RDPWD_V2));
	    buffer.append("SPID="+merId);
	    buffer.append("&TAG="+regtag);
	    buffer.append("&RDPWD="+rand);
	    return buffer.toString();
	}

	@Override
	protected MpspMessage mainProcess(RequestMsg requestMsg) {
		//文件处理，已在processfile里实现
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
		//无特殊校验
		return null;
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
	protected String getFunCode() {
		return DataDict.FUNCODE_MER_MONTH_REGIST_BILL;
	}
	@Override
	 protected String processFile(RequestMsg requestMsg,HttpServletResponse response) throws IOException{
		//商户号
		String merId=ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
		
		SimpleDateFormat fmon = new SimpleDateFormat ("yyyyMM"); 
        String filedate=fmon.format(new Date());
        String dirPath = ObjectUtil.unifyUrl(messageService.getSystemParam(DataDict.FILEPATH_BILL_SYNUSER_V2));
        response.setContentType("text/text;charset=GB2312");
        PrintWriter out = response.getWriter();
        //判断文件是否存在
        //测试后添加+"/user."
       
        File file=new File(dirPath+"/user."+merId+"."+filedate+".txt");
        if(!file.exists())
        {//当前路径没有对帐文件
		    out.print("&UMPAYLOUTHEAD"+merId+"|"+filedate+"|"+filedate+"\r\n");
			out.print("&UMPAYLOUTTAIL"+merId+"|-1\r\n");
			logInfo(dirPath+"/user."+merId+"."+filedate+".txt 对帐文件暂时不存在！");
            
        }else{//当前路径下存在对帐文件
            FileReader input = new FileReader(dirPath+"/user."+merId+"."+filedate+".txt");
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
