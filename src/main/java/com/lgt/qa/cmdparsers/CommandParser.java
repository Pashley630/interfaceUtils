package com.lgt.qa.cmdparsers;

import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 内置命令解释器类，通过识别关键字符串${}来判断是否在字符串中使用了内置命令。
 * 目前命令解释器可以识别的命令有：
 * <ul>
 * 	<li>request</li>
 * 	<ul>
 * 		<li>url</li>
 * 		<ul>
 * 			<li>host</li>
 * 			<li>query</li>
 * 			<li>path</li>
 * 		</ul>
 * 		<li>code</li>
 * 		<li>headers</li>
 * 		<li>header</li>
 * 		<ul>
 * 			<li>the name of header</li>
 * 		</ul>
 * 	</ul>
 * 	<li>response</li>
 * 	<ul>
 * 		<li>code</li>
 * 		<li>headers</li>
 * 		<li>header</li>
 * 		<ul>
 * 			<li>the name of header</li>
 * 		</ul>
 * 		<li>body</li>
 * 	</ul>
 * 	<li>json(json-expression)</li>
 * 	<li>int(整数)</li>
 *  <li>double(浮点数)</li>
 * </ul>
 * 除去上述关键命令，其他字符串将被识别为全局参数
 *
 */
public class CommandParser {
	private static final Logger logger = LoggerFactory.getLogger(CommandParser.class);
	/**
	 * FUNCTIONS用于做函数和实际方法的映射
	 */
	private static final Map<String,String[]> FUNCTIONS = new HashMap<>();
	static {
		FUNCTIONS.put("json", new String[]{"com.ssic.qa.functions.ExtractUtils","jsonExtract"});
		FUNCTIONS.put("int", new String[]{"com.ssic.qa.functions.TypeConverter","convert2int"});
		FUNCTIONS.put("double", new String[]{"com.ssic.qa.functions.TypeConverter","convert2double"});
		FUNCTIONS.put("regex", new String[]{"com.ssic.qa.functions.ExtractUtils","regexExtract"});
	}
	
	private StepContext context;
	
	/**
	 * 构造一个带有指定上下文的命令解释器实例
	 * @param context 上下文对象，用于存放全局用户自定义变量，以及EasyRequest对象
	 */
	public CommandParser(StepContext context) {
		this.context = context;
	}
	
	/**
	 * 参数解释方法，提取${}中的命令，并做分析
	 * @param arg 需要分析的字符串
	 * @return 执行了${}中的命令后，替换成的新字符串。
	 */
	public Object parserArg(String arg){
		String str = arg;
		Pattern p = Pattern.compile("\\$\\{.*?\\}"); // 定义要提取的结构
		Matcher m = p.matcher(str);
		//int gi = 0;
		while(m.find()) { // 使用循环解决字符串中多处引用命令
			String toReplace = m.group();
			//gi = str.indexOf(toReplace)+toReplace.length()-1;
			logger.info("找到字符串中引用的命令"+toReplace);
			Object value = parserCmd(toReplace.substring(2, toReplace.length()-1)); // 将命令字符串从${}中提取出来交给命令解释器处理
			logger.info("已将引用命令解析为"+value);
			if(toReplace.equals(arg)) {
				return value;
			}
			str = str.replace(toReplace, value.toString());
		}
		return str;
	}
	
	/**
	 * 核心命令解释方法
	 * @param cmd 命令字符串
	 * @return 执行命令后返回的字符串
	 */
	private Object parserCmd(String cmd) {
		if(cmd.equals("")) {
			logger.warn("需要解析的命令为空");
			return "";
		}
		String functionCmd = "";
		if(cmd.endsWith(")")) { // 如果命令结尾是)，则表示命令在最后位置调用了某一函数，需要将最后的函数从命令行中截取出来单独处理
			String pre_cmd = cmd.substring(0,cmd.indexOf("(")); 
			functionCmd = pre_cmd.substring(pre_cmd.lastIndexOf(".")+1)
					+cmd.substring(cmd.indexOf("(")); // 通过拼装，组成函数字符串，如fn(var1,var2)
			if(!cmd.equals(functionCmd)) { // 判断是否该命令字符串仅仅只是一个函数调用，如果不是，则表示函数调用前面还有命令字符串
				cmd = pre_cmd.substring(0, pre_cmd.lastIndexOf(".")); // 截取出函数前面的命令字符串
			}else {
				cmd = "";
			}
		}
		logger.trace("解析出需要执行的命令为："+cmd);
		logger.trace("解析出需要执行的函数为："+functionCmd);
		Request request = null;
		Response response = null;
		if(cmd.startsWith("request")) {
			request = context.getEasyRequest().getRequest();
		}
		if(cmd.startsWith("response")) {
			response = context.getEasyRequest().getResponse();
		}
		/*
		 * 通过switch执行指定的命令
		 */
		switch(cmd) {
			case "request":
				cmd = request.toString();
				break;
			case "request.url":
				cmd = request.url().toString();
				break;
			case "request.url.host":
				cmd = request.url().host();
				break;
			case "request.url.query":
				cmd = request.url().query();
				break;
			case "request.url.path":
				cmd = request.url().encodedPath();
				break;
			case "request.headers":
				cmd = request.headers().toString();
				break;
			case "response":
				cmd = response.toString();
				break;
			case "response.headers":
				cmd = response.headers().toString();
				break;
			case "response.body":
				cmd = context.getEasyRequest().getRespnseBody();
				break;
			case "response.code":
				cmd = response.code() + "";
				break;
			default:
				int idx = cmd.lastIndexOf(".");
				if(idx != -1 && cmd.substring(0, idx).endsWith("header")) { // 如果命令是header.xxxx结尾的，则需要使用header去获取指定的头信息
					String headerName = cmd.substring(idx+1); // 获取header的名字
					cmd = cmd.substring(0, idx);
					if(cmd.equals("request.header")) { // 判断如果是请求头
						cmd = context.getEasyRequest().getRequest().header(headerName);
					}else if(cmd.equals("response.header")) { // 如果是响应头
						cmd = context.getEasyRequest().getResponse().header(headerName);
					}else {
						logger.error("该命令"+cmd+"无法正常解析");
						throw new RuntimeException(cmd+" is not valid object");
					}
				}else { // 如果不属于任何上述命令，则认为是用户自定义变量
					String value = StepContext.getVar(cmd);
					cmd = value == null ? cmd : value;
				}
		}
		if(!functionCmd.equals("")) { // 如果命令字符串最后位置存在函数调用
			return functionInvoke(cmd,functionCmd);
		}
		return cmd;
	}
	
	/**
	 * 执行指定函数的方法
	 * @param content 执行函数的上下文，将会作为函数的第一个参数传递给调用的函数
	 * @param function 需要执行的函数，包含参数，函数的调用名和实际所在的类和方法保存在FUNCTIONS中
	 * @return 函数执行后的结果
	 */
	private Object functionInvoke(String content, String function) {
		String functionName = function.substring(0, function.indexOf("(")); // 截取出字符串中的函数名字
		String args = "";
		Object retVal = null;
		if(function.lastIndexOf(")") - function.indexOf("(") > 1) {
			args = function.substring(function.indexOf("(")+1, function.lastIndexOf(")"));
		}
		Object[] argsArr = args.split(","); // 截取出函数的参数
		if(content!=null && !content.equals("")) { // 如果有content的值，则将content作为第一个参数
			Object[] tmp = new Object[argsArr.length+1];
			tmp[0] = content;
			for(int i = 0; i < argsArr.length; i++) {
				tmp[i+1] = argsArr[i];
			}
			argsArr = tmp;
		}
		if(FUNCTIONS.containsKey(functionName)) { // 检查是否存在该函数
			try {
				Class<?> clazz = Class.forName(FUNCTIONS.get(functionName)[0]); // 反射该类对象
				if(!args.equals("")) { // 是否存在参数
					Class<?>[] paramsType = new Class<?>[argsArr.length];
					for(int i = 0; i < paramsType.length; i++) {
						paramsType[i] = String.class;
					}
					Method method = clazz.getDeclaredMethod(FUNCTIONS.get(functionName)[1],paramsType); // 通过方法名和传递的参数类型反射该方法
					retVal = method.invoke(null, argsArr); // 执行该方法
				}else {
					Method method = clazz.getDeclaredMethod(FUNCTIONS.get(functionName)[1]); // 如果没有参数，则直接查询该方法
					retVal = method.invoke(null);
				}
			} catch (ClassNotFoundException | NoSuchMethodException | 
					SecurityException | IllegalAccessException | 
					IllegalArgumentException | InvocationTargetException e) {
				logger.error("内置方法执行异常",e);
			}
		}else {
			logger.error(functionName+"不是可用的一个方法");
			throw new RuntimeException(functionName+" is not a function");
		}
		logger.info("函数"+function+"的执行结果为"+retVal);
		return retVal;
	}
	
	public String replaceMagic(String str,List<String> args) {
		for(int i = 0; i < args.size(); i++) {
			int index = str.indexOf("?");
			if(index != -1) {
				String head = str.substring(0, index);
				String tail = index!=str.length()-1?str.substring(index+1):"";
				str = head + args.get(i) + tail;
			}
		}
		return str;
	}
	
}
