package com.umpay.hfweb.action.test;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thoughtworks.xstream.XStream;
import com.umpay.hfweb.action.base.BaseAbstractAction;
import com.umpay.hfweb.exception.BusinessException;
import com.umpay.hfweb.model.RecvAddress;
import com.umpay.hfweb.model.SIAP;
import com.umpay.hfweb.model.SIAPBody;
import com.umpay.hfweb.model.SIAPHeader;
import com.umpay.hfweb.model.SendAddress;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.SequenceUtil;

public class KFKPayNotifyActionTest extends BaseAbstractAction{

	@Override
	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response, Map<String, Object> modelMap)
			throws BusinessException {
		try {
			String returnInterBOSSXml = HttpUtil.inputStream2String(request.getInputStream());
			System.out.println(returnInterBOSSXml);
			
			String BPID = "XEZF";
			// 报文头  TODO
			SIAPHeader header = new SIAPHeader();
			
			SequenceUtil su = SequenceUtil.getInstance();
			String TransactionID = DateUtil.getDate(new Date(), "yyyyMMddHHmmss") + SequenceUtil.formatSequence(su.getSequence4File("hfWebBusi.kfk.rpid"), 6);
			header.setTransactionID(TransactionID);
			header.setVersion("2.0"); // 定值2.0
			header.setMessageName("OrderSyncReq");
			header.setTestFlag(this.messageService.getSystemParam("KFK.TestFlag"));
			header.setReturnCode(""); // 请求时可选
			header.setReturnMessage(""); // 请求时可选
			
			SendAddress sendAddress = new SendAddress();
			sendAddress.setDeviceID("203"); // 定值
			sendAddress.setDeviceType(BPID);
			
			header.setSendAddress(sendAddress);
			
			RecvAddress recvAddress = new RecvAddress();
			recvAddress.setDeviceID("203"); // 定值
			recvAddress.setDeviceType(BPID);
			
			header.setRecvAddress(recvAddress);
			
			
			// 组装整体报文
			SIAP siap = new SIAP();
			siap.setSIAPHeader(header);
			
			SIAPBody body = new SIAPBody();
			
			siap.setSIAPBody(body);
			
			String kfkReqUrl = "http://10.10.36.49:8602/hfWebBusi/pay/kfkPayNotify.do";
			
			SIAP rtnSIAP = null;
			try {
				HttpUtil httpUtil = new HttpUtil();
				rtnSIAP = httpUtil.sendRequestForPost(kfkReqUrl, siap);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			if(rtnSIAP != null){
				String rtnXML = new XStream().toXML(rtnSIAP);
				System.out.println(rtnXML);
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected String getFunCode() {
		
		return null;
	}

	
}
