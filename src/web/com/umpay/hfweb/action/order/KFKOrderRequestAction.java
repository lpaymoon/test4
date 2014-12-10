package com.umpay.hfweb.action.order;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.bs.mpsp.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.exception.SignEncException;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
import com.umpay.hfweb.util.SignEnc;

public class KFKOrderRequestAction extends DirectBaseAction {

	@SuppressWarnings("unchecked")
	@Override
	protected void processBussiness(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		
		PageOrderCmd cmd = new PageOrderCmd(requestMsg.getWrappedMap());
		
		//交易鉴权 查询商品金额
		long amount = 0;
		MpspMessage checkTradeResp = restService.checkTrade(cmd.getMobileId(), cmd.getMerId(), cmd.getGoodsId());
		// 如果鉴权成功    取金额
		if(checkTradeResp.isRetCode0000()){
			//TODO begin 获取交易鉴权获得的provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
			responseMsg.put(HFBusiDict.PROVCODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.PROVCODE)));
			responseMsg.put(HFBusiDict.AREACODE, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.AREACODE)));
			responseMsg.put(HFBusiDict.BANKID, ObjectUtil.trim((String)checkTradeResp.get(HFBusiDict.BANKID)));
			//TODO end 获取交易鉴权获得的bankid,provcode,areacode ，放入简要日志中 modify by zhaoYan 2013-11-15
			
			List<Map<String, Object>> merbanks =  (List<Map<String, Object>>)checkTradeResp.get(HFBusiDict.MERBANKS);
			for(Map<String, Object> merbank : merbanks){
				String merBankId  = StringUtil.trim((String)merbank.get(HFBusiDict.BANKID));
				// 北京小额银行
				if(merBankId.equals("XE010000")){
					amount = (Long)merbank.get(HFBusiDict.AMOUNT);
					logInfo("获取数据库中商户[%s]-商品[%s]的金额:[%s]", cmd.getMerId(), cmd.getGoodsId(), amount);
					break;
				}
			}
		}
		
		String notifyUrl = this.messageService.getSystemParam("KFKXD.notifyUrl");
		
		// 12位订单号  sessionId的第一位+11位的序列数
		String orderId = getOrderId();
		
		String retCode = DataDict.SUCCESS_RET_CODE;
		
		String retMsg = "下单成功";
		
		String merDate = DateUtil.getDate(new Date(), "yyyyMMdd");
		
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			retCode = "0002";
			retMsg = "订单参数有误";
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s", checkParamResp.getRetCode(), messageService.getMessageDetail(checkParamResp.getRetCode()));
			
			// 返回
			doResponse(responseMsg, cmd, orderId, amount, merDate, notifyUrl, retCode, retMsg);
			return;
		}
		
		//3-商户验签
		String url = "merId=" + cmd.getMerId() 
					+ "&goodsId=" + cmd.getGoodsId()
				    + "&goodsInf=" + cmd.getGoodsName()
				    + "&mobileId=" + cmd.getMobileId()
				    + "&amtType=" + cmd.getAmtType() 
				    + "&bankType=" + cmd.getBankType() 
				    + "&version=" + cmd.getVersion();
		
		boolean verify = false; // 验签结果标记
		try {
			verify = SignEnc.verify(cmd.getMerId(), url, cmd.getSign(), super.messageService);
		} catch (SignEncException e) {
			e.printStackTrace();
		}
		
		// 验签失败   返回
		if(!verify){
			retCode = "1330";
			retMsg = "验签失败";
			// 返回
			doResponse(responseMsg, cmd, orderId, amount, merDate, notifyUrl, retCode, retMsg);
			return;
		}
		
		doResponse(responseMsg, cmd, orderId, amount, merDate, notifyUrl, retCode, retMsg);
	}

	private void doResponse(ResponseMsg responseMsg, PageOrderCmd cmd, String orderId, long amount, String merDate, String notifyUrl, String retCode, String retMsg){
		responseMsg.setRetCode(retCode);
		responseMsg.setRetCodeBussi(retMsg);
		
		String plain = this.getRetSign(cmd, orderId, amount, merDate, notifyUrl, retCode, retMsg);
		String retSign = "";
		try {
			retSign = SignEnc.sign(cmd.getMerId(), plain, super.messageService);
		} catch (SignEncException e) {
			e.printStackTrace();
		}
		responseMsg.setDirectResMsg(plain + "|" + retSign);
	}

	private String getRetSign(PageOrderCmd cmd, String orderId, long amount, String merDate, String notifyUrl, String retCode, String retMsg){
		String plain = cmd.getMerId() + "|"
					+ cmd.getGoodsId() + "|"
					+ orderId + "|"
					+ merDate  + "|"
					+ amount + "|"
					+ notifyUrl + "|||"
					+ retCode + "|"
					+ retMsg + "|"
					+ cmd.getVersion();
		return plain;
	}

	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {

	}
	
	private String getOrderId(){
		// rpid的规则为  W+merid+perfix+10位序列数
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		
		// 从后往前算12位
		StringBuffer buffer = new StringBuffer(rpid);
		return new StringBuffer(buffer.reverse().toString().substring(0, 10)).reverse().toString();
	}

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_KFKXD;
	}

}
