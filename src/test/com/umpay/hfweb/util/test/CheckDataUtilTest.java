package com.umpay.hfweb.util.test;

import org.junit.Test;

import com.umpay.hfweb.util.CheckDataUtil;

public class CheckDataUtilTest {

	@Test
	public void check_ok1(){
		System.out.println(CheckDataUtil.check("abc","[.]{1,}"));
		System.out.println(CheckDataUtil.check("1#@",".{1,32}"));
		System.out.println(CheckDataUtil.check("",".{1,32}"));
		System.out.println(CheckDataUtil.check("首府大厦佛挡杀佛东四饭店首府大厦佛挡杀佛",".{1,32}"));
	}
}
