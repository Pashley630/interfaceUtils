package com.lgt.qa.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 各类提取器常用类
 *
 */
public class ExtractUtils {
	private static JsonParser parser = new JsonParser();
	/**
	 * json数据提取器
	 * @param jsonStr 需要提取的原文json数据
	 * @param extractStr json提取表达式
	 * @return 提取出的字符串
	 */
	public static String jsonExtract(String jsonStr,String extractStr){
		JsonObject jsonObj = (JsonObject) parser.parse(jsonStr);
		String[] parts = extractStr.split("\\.");
		for(String part : parts) {
			if(part.matches(".*\\[\\d+\\]$")) { // 如果是数组
				int position = part.lastIndexOf('[');
				int idx = Integer.parseInt(part.substring(position+1, part.length()-1));
				part = part.substring(0, position);
				JsonArray array = jsonObj.getAsJsonArray(part);
				jsonObj = array.get(idx).getAsJsonObject();
			}else {
				JsonElement element = jsonObj.get(part);
				if(element.isJsonPrimitive()) { // 如果是叶子节点，则直接返回
					return element.getAsString();
				}else if(element.isJsonArray()) { // 如果节点是数组，则直接返回
					return element.getAsJsonArray().toString();
				}else {
					jsonObj = (JsonObject) element;
				}
			}
		}
		return jsonObj.toString();
	}
	
	public static String regexExtract(String content, String regPattern) {
		Pattern p = Pattern.compile(regPattern);
		Matcher m = p.matcher(content);
		String result = "";
//		int i = 0;
		int gi = 0;
		if(m.find(gi)) {
//			i++;
			result = m.group(1);
			gi = content.indexOf(result)+result.length()-1;
			/*if(Integer.parseInt(index) == i) {
				break;
			}*/
		}
		return result;
	}
	
	public static void main(String[] args) {
		String s = "{\"retCode\":100000,\"version\":\"1.0\",\"timestamp\":\"1524450874156\",\"data\":\"http://www.baidu.com?code=5aBcd\"}";
		String p = "code=(.*?)\"}";
		System.out.println(regexExtract(s, p));
	}
}
