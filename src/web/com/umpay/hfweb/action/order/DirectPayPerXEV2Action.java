package com.umpay.hfweb.action.order;

import com.umpay.hfweb.dict.DataDict;
/**
 * ******************  类说明  *********************
 * class       :  DirectPayPerXEV2Action
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  小额按次下单 V2.0
 * @see        :                        
 * ***********************************************
 */
public class DirectPayPerXEV2Action extends DirectPayPerV2Action{
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_DIRECT_HTXD_XEPER_V2;
	}
	protected boolean isVer3(){
		return false;
	}
}
