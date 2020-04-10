package com.lgt.qa.cmdparsers;

import com.lgt.qa.okhttp.EasyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 接口测试上下文环境，用于保存自定义全局变量，以及EasyRequest对象，所有接口测试的stepDefine类必须继承该类
 *
 */
public class StepContext {
	private static ThreadLocal<EasyRequest> currentEeasyRequest = new ThreadLocal<>();
	private static ThreadLocal<Map<String, String>>currentVars=ThreadLocal.withInitial(StepContext::init);// Map<String,String> vars = new HashMap<>(); // 存放自定义全局变量的容器
	private static final Logger logger = LoggerFactory.getLogger(StepContext.class);
	
	public static Map<String, String> init() {
		// 获取本地保存的参数定义文件
		Map<String, String> map=new HashMap<>();
		try {
			Properties prop = PropertiesUtils.read("vars");
			if(prop != null) {
				Enumeration<?> keys = prop.propertyNames();
				while(keys.hasMoreElements()) {
					String name = keys.nextElement().toString();
					map.put(name,prop.getProperty(name));
				}
			}
			return map;
		} catch (Exception e) {
			logger.info("未找到本地配置的参数文件，全局变量置空",e);
		}
		return null;
	}
	
	/**
	 * 从上下文环境中获取指定名称的变量值
	 * @param key 变量名
	 * @return 变量值
	 */
	public static String getVar(String key) {
		return currentVars.get().get(key);
	}
	/**
	 * 将变量添加到上下文环境
	 * @param key 变量名
	 * @param value 变量值
	 */
	public static void setVar(String key, String value) {
		currentVars.get().put(key, value);
	}
	public void setVar(Map<String, String> valMap) {
		currentVars.get().putAll(valMap);
	}
	/**
	 * 判断是否存在指定的变量
	 * @param key 变量名
	 * @return true-存在，false-不存在
	 */
	public boolean containsVar(String key) {
		return currentVars.get().containsKey(key);
	}
	/**
	 * 获取EasyRequest对象
	 * @return EasyRequest对象
	 */
	public EasyRequest getEasyRequest() {
		return currentEeasyRequest.get();
	}
	/**
	 * 设置EasyRequest对象
	 * @param easyRequest EasyRequest对象
	 */
	public void setEasyRequest(EasyRequest easyRequest) {
		currentEeasyRequest.set(easyRequest);
	}
	
}
