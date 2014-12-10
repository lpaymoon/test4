package com.umpay.hfweb.util.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.umpay.hfweb.util.HttpUtil;

public class HttpUtilTest {

	@Test
	public void testMapToRequestParameter(){
		Map map = new HashMap();
		map.put("merId", "9996");
		String rs = HttpUtil.mapToRequestParameter(map);
		System.out.println(rs);
	}
}
