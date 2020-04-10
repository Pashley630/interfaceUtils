package com.lgt.qa.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类型转换专用的静态类
 * @author liudao
 *
 */
public class TypeConverter {
	private static final Logger logger = LoggerFactory.getLogger(TypeConverter.class);
	/**
	 * 将字符串转换为整形，通常用于cucumber中一定要用整数传递时
	 * @param content 需要转换为整数的字符串
	 * @return 整数
	 */
	public static int convert2int(String content) {
		int res = 0;
		try {
			res = Integer.parseInt(content);
		}catch(NumberFormatException e) {
			logger.error("无法将"+content+"转换为整数.", e);
			e.printStackTrace();
		}
		return res;
	}
	/**
	 * 将字符串转换为浮点型，通常用于cucumber中一定要用整数传递时
	 * @param content 需要转换为浮点型的字符串
	 * @return 浮点数
	 */
	public static double convert2double(String content) {
		double res = 0;
		try {
			res = Double.parseDouble(content);
		}catch(NumberFormatException e) {
			logger.error("无法将"+content+"转换为double型.", e);
			e.printStackTrace();
		}
		return res;
	}
}
