package com.umpay.hfweb.action.order;

import com.umpay.hfweb.action.base.DirectBaseAction;
import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.HFMerReferCache;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;

public class ReferCacheRefreshAction extends DirectBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		// 1、验证加密码
		logInfo("1、VerifyPWDCode Result Success[RetCode]:0000:接口校验码通过。");
		
		// 2、获取接口参数
		String merid = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String goodsid = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		
		if("".equals(merid) || "".equals(goodsid)){
			String retCode = "1113";
			if("".equals(goodsid)){
				retCode = "1121";
			}
			logInfo("接口参数检验不通过retCode[%s]：merid[%s] goodsid[%s]", retCode, merid, goodsid);
			responseMsg.setRetCode(retCode);
//			responseMsg.setRetCodeBussi(retCode);
			responseMsg.setDirectResMsg(merid + "|" + goodsid + "|" + retCode + "|" + "刷新缓存失败");
			return;
		}
		
		logInfo("接口参数检验通过：merid[%s] goodsid[%s]", merid, goodsid);
		
		// 获取到报备信息的缓存对象
		HFMerReferCache hfMerReferCache = (HFMerReferCache) AbstractCacheFactory.getInstance().getCacheClient(HFMerReferCache.CACHE_NAME);
		String cacheKey = merid + "-" + goodsid;
		
		if("all".equals(merid) && "all".equals(goodsid)){
			hfMerReferCache.removeAll();
		}
		else{
			hfMerReferCache.remove(cacheKey);
		}
		
		
		responseMsg.setRetCode0000();
//		responseMsg.setRetCodeBussi(retCode);
		responseMsg.setDirectResMsg(merid + "|" + goodsid + "|0000|" + "刷新缓存成功");
	}

	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		
	}

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_DIRECT_SXURL;
	}

}
