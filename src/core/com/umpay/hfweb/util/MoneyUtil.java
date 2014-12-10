package com.umpay.hfweb.util;

import java.text.DecimalFormat;
import java.text.FieldPosition;

public class MoneyUtil { 

	public static String Dollar2Cent(String str) {
		DecimalFormat df = new DecimalFormat("0.00");
		StringBuffer sb = df.format(Double.parseDouble(str),
				new StringBuffer(), new FieldPosition(0));
		// int idx = sb.indexOf(".");
		int idx = sb.toString().indexOf(".");
		sb.deleteCharAt(idx);
		for (; sb.length() != 1;) {
			if (sb.charAt(0) == '0') {
				sb.deleteCharAt(0);
			} else {
				break;
			}
		}
		return sb.toString();
	}

	public static String Cent2Dollar(String s) {
		long l = 0;
		try {
			if (s.charAt(0) == '+') {
				s = s.substring(1);
			}
			l = Long.parseLong(s);
		} catch (Exception e) {
			//e.printStackTrace();
			return "";
		}
		boolean negative = false;
		if (l < 0) {
			negative = true;
			l = Math.abs(l);
		}
		s = Long.toString(l);
		if (s.length() == 1)
			return (negative ? ("-0.0" + s) : ("0.0" + s));
		if (s.length() == 2)
			return (negative ? ("-0." + s) : ("0." + s));
		else
			return (negative ? ("-" + s.substring(0, s.length() - 2) + "." + s
					.substring(s.length() - 2)) : (s.substring(0,
					s.length() - 2)
					+ "." + s.substring(s.length() - 2)));
	}

}
