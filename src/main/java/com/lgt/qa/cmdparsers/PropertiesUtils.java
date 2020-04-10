package com.lgt.qa.cmdparsers;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

public class PropertiesUtils {
	/**
	 * 读取(只能)properties配置文件.
	 * @param packagePathFileName 配置文件路径及文件名(.properties后缀可省略),注意,可同时读取多个相同类型的配置文件,如:
	 * Properties conf = PropertiesUtil.read("config");//config文件位于根目录下,config=config.properties,下同
	 * conf = PropertiesUtil.read("com/ssic/cloud/util/redis/config/chars");//包路径+文件名
	 * @return java.util.Properties Object
	 * @throws Exception 异常对象
	 */
	public static Properties read(String packagePathFileName) throws Exception {
		Properties properties = new Properties();
		if(packagePathFileName.equals("")){
			return null;
		}
		ResourceBundle rb = ResourceBundle.getBundle(packagePathFileName);
		Enumeration<String> res = rb.getKeys();
		LoadFormat(properties, rb, res);
		ResourceBundle.clearCache();
		return properties;
	}

	private static void LoadFormat(Properties properties, ResourceBundle rb, Enumeration<String> res) throws UnsupportedEncodingException {
		while (res.hasMoreElements()) {
			String key = res.nextElement();
			String str = rb.getString(key);
			String value = new String(str.getBytes("ISO-8859-1"), "UTF-8");
			//System.out.println( key +"----:"+value);
			properties.setProperty(key, value);
		}
	}

	/*public static  void main(String[] args) throws Exception {
		System.out.println(read("dd"));
	}*/

}
