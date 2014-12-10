package com.umpay.hfweb.action.order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.exception.SignEncException;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.OrderLine;
import com.umpay.hfweb.model.OrderSyncReq;
import com.umpay.hfweb.model.Orderlist;
import com.umpay.hfweb.model.RecvAddress;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.model.SIAP;
import com.umpay.hfweb.model.SIAPBody;
import com.umpay.hfweb.model.SIAPHeader;
import com.umpay.hfweb.model.SendAddress;
import com.umpay.hfweb.model.UserID;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SequenceUtil;
import com.umpay.hfweb.util.SignEnc;

/**
 * 卡父卡接入支付结果通知
 * 收到通知后请求卡父卡订单同步接口
 * @author FANXIANGCHI
 *
 */
public class KFKPayNotifyAction extends DirectBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		
		String retCode = DataDict.SUCCESS_RET_CODE;
		String retMsg = "成功";
		
		PageOrderCmd cmd = new PageOrderCmd(requestMsg.getWrappedMap());
		
		String reqFrom = ObjectUtil.trim(requestMsg.getStr("reqFrom"));
		// 如果不是客服平台的请求  则需要做以下验证
		if(!"CSTS".equals(reqFrom)){
			logInfo("非客服平台请求，将做商户下单时间校验和商户验签...");
			// 商户下单时间校验
			if(!DateUtil.verifyOrderDate(cmd.getMerDate())){
				retCode = "1310";
				retMsg = "商户下单时间校验未通过";
				logInfo("VerifyOrderDate Result Failed[RetCode]:%s:%s", "1310", "商户下单时间校验未通过");
				
//				responseMsg.setRetCode(retCode);
//				responseMsg.setRetCodeBussi(retMsg);
				doResponse(responseMsg, cmd, retCode, retMsg);
				return;
			}
			logInfo("VerifyOrderDate Result Success[RetCode]:0000:商户下单时间校验通过");
			
			String payDate = requestMsg.getStr("payDate");
			String transType = requestMsg.getStr("transType");
			String settleDate = requestMsg.getStr("settleDate");
			String merPriv = requestMsg.getStr("merPriv");
			
			// 验签
			String url = "merId=" + cmd.getMerId() + "&goodsId="
					+ cmd.getGoodsId() + "&orderId=" + cmd.getOrderId()
					+ "&merDate=" + cmd.getMerDate() + "&payDate=" + payDate
					+ "&amount=" + cmd.getAmount() + "&amtType=" + cmd.getAmtType()
					+ "&bankType=" + cmd.getBankType() + "&mobileId="
					+ cmd.getMobileId() + "&transType=" + transType
					+ "&settleDate=" + settleDate + "&merPriv=" + merPriv
					+ "&retCode=0000" + "&version=" + cmd.getVersion();		
			
			logInfo("KFKPayNotifyAction请求来源签名:%s", url);
			boolean verify = false; // 验签结果标记
			try {
				verify = SignEnc.verify(cmd.getMerId(), url, cmd.getSign(), super.messageService);
			} catch (SignEncException e) {
				e.printStackTrace();
			}
			
			// 验签失败   返回
			if(!verify){
				logInfo("SignEnc.verify Result Success[RetCode]:0000:商户验签未通过");
				
				retCode = "1330";
				retMsg = "验签失败";
				
//				responseMsg.setRetCode(retCode);
//				responseMsg.setRetCodeBussi(retMsg);
				// 返回
				doResponse(responseMsg, cmd, retCode, retMsg);
				return;
			}
			logInfo("SignEnc.verify Result Success[RetCode]:1330:商户验签通过");
		}
		
		// 收到参数后  请求卡父卡平台订单同步接口
		
		// 组装报文体
		logInfo("KFKPayNotifyAction 开始组装请求卡父卡平台的报文....");
		OrderSyncReq sync = new OrderSyncReq();
		
		String BPID = this.messageService.getSystemParam("KFK.BPID");
		sync.setBPID(BPID); // 平台编码
		sync.setOpr_Type("01"); // 订购
		
		// 用户标识
		UserID userID = new UserID();
		userID.setID(cmd.getMobileId());
		userID.setType("mobile");
		
		sync.setUserID(userID);
		
		SequenceUtil su = SequenceUtil.getInstance();
		// 商品订单
		Orderlist Orderlist = new Orderlist();
		Orderlist.setOrderID(cmd.getOrderId());
		Orderlist.setNum("1");
		
		OrderLine list = new OrderLine();
		list.setBPID(BPID);
		list.setOrderID(cmd.getOrderId());
		
		String merid = ObjectUtil.trim(cmd.getMerId());
		
		String mapMerid = ObjectUtil.trim(this.messageService.getSystemParam("KFK.merid." + merid));
		// 如果配置的映射关系不为空  则传映射的商户号
		if(!"".equals(mapMerid)){
			merid = mapMerid;
		}
		
		list.setMercID(merid);
		
		MpspMessage rtnMessage = this.restService.queryMerGoodsInfo(cmd.getMerId(), cmd.getGoodsId());
		list.setMercName(rtnMessage.getStr(HFBusiDict.MERNAME)); // 商户名 非必须
		list.setMercAddr(""); // 商户地址 非必须
		list.setOrderDate(cmd.getMerDate());
		list.setOrderLineID("1"); // 订单行号?
		list.setOrderStatus("5"); // 订单状态  新建
		list.setLastConsDate(cmd.getMerDate());
		list.setGoodsID(cmd.getGoodsId());
		
		String goodsName = rtnMessage.getStr(HFBusiDict.GOODSNAME);
		String goodsDesc = rtnMessage.getStr(HFBusiDict.GOODSDESC);
		
		list.setGoodsName(goodsName);
		list.setGoodsName_Pos(goodsName); // POS机显示的商品名称 
		list.setGoodsDetail(goodsDesc);
		
		
		list.setGoodsPrice(cmd.getAmount()); // 可选
		
		
		list.setBeginTime(cmd.getMerDate() + new SimpleDateFormat("HHmmss").format(new Date())); //订单有效期
		
		// KFK.merid.goodsid=type(1为有效期几个月  2为截止日期)|有效期(类型为1时代表月份  类型为2时就是截止日期)
		String deadLine = this.messageService.getSystemParam("KFK." + cmd.getMerId() + "." + cmd.getGoodsId());
		String endTime = "";
		if("".equals(deadLine)){
			endTime = "20501231235959";
		}
		else{
			String[] dlArray = deadLine.split("\\|");
			String type = dlArray[0];
			String value = dlArray[1];
			
			Date startTime = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try {
				startTime = sdf.parse(cmd.getMerDate());
				
				if("1".equals(type)){
					Calendar c = Calendar.getInstance();
					c.setTime(startTime);
					
					c.add(Calendar.MONTH, Integer.valueOf(value));
					
					endTime = sdf.format(c.getTime()) + "235959";
				}
				else if("2".equals(type)){
					endTime = value;
				}
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		
		list.setEndTime(endTime);
		
		// 商品类型
		String goodsType = "1"; // 和张磊确认后  全部传1
		list.setGoodsType(goodsType); // 商品类型  1实物类  2金额类	
		if("1".equals(goodsType)){
			list.setGoodsNum("1");
			list.setGoodsNum_Rem("1");
		}
		if("2".equals(goodsType)){
			list.setGoodsAmount(cmd.getAmount()); // 可选
			list.setGoodsAmount_Rem(cmd.getAmount()); // 可选
		}
		
		list.setOriginalUrl(""); // 可选
		list.setGoodsPicPath(""); // 可选
		list.setGoodsResdata("ser_tel/4006125880"); // 可选
		
		
		Orderlist.setList(list);
		
		sync.setOrderlist(Orderlist);
		
		SIAPBody body = new SIAPBody();
		body.setOrderSyncReq(sync);
		
		
		// 报文头 
		SIAPHeader header = new SIAPHeader();
		
		String TransactionID = DateUtil.getDate(new Date(), "yyyyMMddHHmmss") + SequenceUtil.formatSequence(su.getSequence4File("hfWebBusi.kfk.rpid"), 6);
		header.setTransactionID(TransactionID);
		header.setVersion("2.0"); // 定值2.0
		header.setMessageName("OrderSyncReq");
		header.setTestFlag(this.messageService.getSystemParam("KFK.TestFlag"));
//		header.setReturnCode(""); // 请求时可选
//		header.setReturnMessage(""); // 请求时可选
		
		SendAddress sendAddress = new SendAddress();
		sendAddress.setDeviceID(BPID); // 定值
		sendAddress.setDeviceType("200");
		
		header.setSendAddress(sendAddress);
		
		RecvAddress recvAddress = new RecvAddress();
		recvAddress.setDeviceID("SCS"); // 定值
		recvAddress.setDeviceType("0");
		
		header.setRecvAddress(recvAddress);
		
		
		// 组装整体报文
		SIAP siap = new SIAP();
		siap.setSIAPBody(body);
		siap.setSIAPHeader(header);
		
		logInfo("KFKPayNotifyAction 请求报文组装完成，开始请求卡父卡平台OrderSyncReq接口");
		
		String kfkReqUrl = this.messageService.getSystemParam("KFK.OrderSyncReq.reqUrl");
		
		SIAP rtnSIAP = null;
		try {
			HttpUtil httpUtil = new HttpUtil();
			rtnSIAP = httpUtil.sendRequestForPost(kfkReqUrl, siap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 组装返回
		
		// 如果接口异常
		if(rtnSIAP == null || (rtnSIAP != null && !"0".equals(rtnSIAP.getSIAPHeader().getReturnCode()))){
			logInfo("KFKPayNotifyAction 请求卡父卡平台OrderSyncReq接口出现异常！");
			
			if(rtnSIAP != null){
				retCode = rtnSIAP.getSIAPHeader().getReturnCode();
				retMsg = rtnSIAP.getSIAPHeader().getReturnMessage();
			}
			else{
				retCode = "1111";
				retMsg = "请求卡父卡平台失败";
			}
		}
		
//		responseMsg.setRetCode(retCode);
//		responseMsg.setRetCodeBussi(retMsg);
		doResponse(responseMsg, cmd, retCode, retMsg);
	}

	
	private void doResponse(ResponseMsg responseMsg, PageOrderCmd cmd,
			String retCode, String retMsg) {
		responseMsg.setRetCode(retCode);
		responseMsg.setRetCodeBussi(retMsg);
		
		logInfo("KFKPayNotifyAction 组装返回报文retCode[%s],retMsg[%s]", retCode, retMsg);
		String plain = this.getRetSign(cmd.getMerId(), cmd.getGoodsId(), cmd.getOrderId(), cmd.getMerDate(), retCode, retMsg, cmd.getVersion());
		String retSign = "";
		try {
			retSign = SignEnc.sign(cmd.getMerId(), plain, super.messageService);
		} catch (SignEncException e) {
			e.printStackTrace();
		}
		responseMsg.setDirectResMsg(plain + "|" + retSign);
		
	}

	private String getRetSign(String merId, String goodsId, String orderId, String merDate, String retCode, String retMsg, String version){
		String plain = merId + "|"
					+ goodsId + "|"
					+ orderId + "|"
					+ merDate  + "|"
					+ retCode + "|"
					+ retMsg + "|"
					+ version;
		return plain;
	}
	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		

	}

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_KFKXD;
	}
	

}
