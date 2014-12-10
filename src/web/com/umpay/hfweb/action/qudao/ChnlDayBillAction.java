package com.umpay.hfweb.action.qudao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SequenceUtil;
import com.umpay.hfweb.util.SessionThreadLocal;


/** ******************  类说明  *********************
 * class       :  ChnlDayBillAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  渠道商户取对账文件
 * @see        :                        
 * ************************************************/   
public class ChnlDayBillAction extends DirectBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		//1 请求数据校验
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		logInfo("请求参数为:%s", requestMsg.getWrappedMap());
		if(!checkParamResp.isRetCode0000()){
			responseMsg.setRetCode(checkParamResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",responseMsg.getRetCode(),(String)checkParamResp.get("message"));
			return;
		}
		logInfo("参数校验通过");
		
		String chnlId = requestMsg.getStr(DataDict.MER_REQ_CHNLID);//渠道号
		String chnlDate = requestMsg.getStr(DataDict.MER_REQ_CHNLDATE);//渠道日期
		String signstr = requestMsg.getStr(DataDict.MER_REQ_SIGN);
		String version = requestMsg.getStr(DataDict.MER_REQ_VERSION);//版本号，定值3.0
		
		//2-校验访问权限
		logInfo("访问权限校验");
		if(!merAuthService.canAccess(getFunCode(), chnlId)){
			//商户未开通此项支付服务
			responseMsg.setRetCode("1128");
			logInfo("MerAuthCheck Result Failed[RetCode]:1128:访问权限校验未通过");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		
		//3 渠道验签
		String unsignstr = "chnlId="+chnlId+"&chnlDate="+chnlDate+"&version="+version;
		MpspMessage checkSignResp = restService.checkChnlSign(chnlId,signstr,unsignstr);
		if(!checkSignResp.isRetCode0000()){
			responseMsg.setRetCode(checkSignResp.getRetCode());
			responseEorr2Mer(requestMsg,responseMsg);
			logInfo("渠道验签失败");
			return;
		}
		logInfo("渠道验签通过");
		
		//4  请求日期校验：只能取前一天或者更早的对账文件
		Date payDate = DateUtil.parseDateyyyyMMdd(chnlDate);
		if(DateUtil.getDateyyyMMdd(payDate).compareTo(DateUtil.getDateyyyMMdd(DateUtil.getDateBefore(new Date()))) > 0){
			responseMsg.setRetCode("1112");
			logInfo("请求日期只能是前一天或者更早的对账文件！");
			responseEorr2Mer(requestMsg,responseMsg);
			return;
		}
		//处理成功
		responseMsg.setRetCode0000();
	}

	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		String memo = getRetMessage(responseMsg);
		StringBuffer meta = new StringBuffer(responseMsg.getRetCode()).append("|").append("|||||").append(memo);
		responseMsg.setDirectResMsg(meta.toString());
	}

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_MER_REQ_QDDZ;
	}

	@Override
	protected String processFile(RequestMsg requestMsg,HttpServletResponse response) throws IOException {
		String chnlId = requestMsg.getStr(DataDict.MER_REQ_CHNLID);//渠道号
		String chnlDate = requestMsg.getStr(DataDict.MER_REQ_CHNLDATE);//渠道日期
		response.setContentType("text/text;charset=GB2312");
		PrintWriter out = response.getWriter();
		String billPath = ObjectUtil.unifyUrl(messageService.getSystemParam(DataDict.FILEPATH_BILL_QUDAO));
        String filePath = billPath + "/" + chnlId + "." + chnlDate + ".txt";
        logInfo("获取对账文件开始,文件路径：%s", filePath);
		File billFile = new File(filePath);
		if (!billFile.exists()) {
			out.print("TRADEDETAIL-START," + chnlId + "," + chnlDate+ ",3.0,0001,对帐文件暂时不存在\r\n");
			out.print("TRADEDETAIL-END," + chnlId + "," + chnlDate+ ",-1,-1\r\n");
			logInfo(chnlId + "." + chnlDate + ".txt 对帐文件暂时不存在！");
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
	@Override
	protected boolean isFileProcess(){
		return true;
	}

	@Override
	protected String createTransRpid(HttpServletRequest request) {
		String rpid = "";
		String chnlId = ObjectUtil.trim(request.getParameter(DataDict.MER_REQ_CHNLID));//渠道号
		if(ObjectUtil.trim(chnlId).length()>4){
			chnlId = chnlId.substring(ObjectUtil.trim(chnlId).length()-5, ObjectUtil.trim(chnlId).length()-1);
		}
		String sid = request.getSession().getId();
		String prefix = "0";
		if(!ObjectUtil.isEmpty(sid) && sid.length() > 1){
			prefix = sid.substring(0, 1);
		}
		SequenceUtil su = SequenceUtil.getInstance();
		rpid = "W" + ObjectUtil.trim(chnlId) + prefix + SequenceUtil.formatSequence(su.getSequence4File("hfWebBusi.rpid"), 10);
		request.getSession().setAttribute(DataDict.REQ_MER_RPID,rpid);
		SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, rpid);
		return super.createTransRpid(request);
	}
	
}
