package com.umpay.hfweb.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class CheckDataUtil{
	public static boolean check(String matched, String reg) {
		Pattern pattern = Pattern.compile(reg);
		return pattern.matcher(matched).matches();
	}

	public static boolean checkDateTime(String dateTime, String format) {
		SimpleDateFormat sf = new SimpleDateFormat(format);
		try {
			sf.parse(dateTime);
		}
		catch (ParseException e) {
			return false;
		}
		return true;
	}

	public static boolean check(String matched, String[] values) {
		for (String value : values) {
			if ((matched.equals(value))) {
				return true;
			}
		}
		return false;
	}

}
