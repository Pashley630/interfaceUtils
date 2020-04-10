package com.lgt.qa.okhttp;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义非持久化cookie容器
 *
 */
public class TempCookieJar implements CookieJar {
	
	private final Map<String, List<Cookie>> cookiesMap = new HashMap<>(); // 非持久化容器

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		cookiesMap.put(url.host(), cookies);
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {
		List<Cookie> cookies = cookiesMap.get(url.host());
		if (cookies != null)
			return cookies;
		else
			return new ArrayList<>();
//		return cookies != null ? cookies : new ArrayList<>();
	}
	/**
	 * 自定义cookie添加功能
	 * @param host 主机地址
	 * @param cookie 需要添加的cookie
	 */
	public void addCookie(String host, Cookie cookie) {
		List<Cookie> cookies = null;
		if(cookiesMap.containsKey(host)) {
			cookies = cookiesMap.get(host);
		}else {
			cookies = new ArrayList<>();
		}
		int idx = containsCookie(cookies, cookie);
		if(idx == -1) {
			cookies.add(cookie);
		}else {
			cookies.remove(idx);
			cookies.add(cookie);
		}
		cookiesMap.put(host, cookies);
	}
	
	public void clearCookie(String host, Cookie cookie) {
		List<Cookie> cookieList = cookiesMap.get(host);
		if(cookieList != null) {
			int index = containsCookie(cookieList, cookie);
			cookieList.remove(index);
		}
	}
	
	//清除所有Cookie
	public void clearCookie() {		
		cookiesMap.clear();
	}
	
	public Cookie getCookie(String host, String name) {
		List<Cookie> cookies = null;
		if(cookiesMap.containsKey(host)) {
			cookies = cookiesMap.get(host);
		}else {
			cookies = new ArrayList<>();
		}
		int idx = containsCookie(cookies, name);
		if(idx != -1) {
			return cookies.get(idx);
		}
		return null;
	}
	
	public List<Cookie> getAllCookie(String host){
		return cookiesMap.get(host);
	}
	
	private int containsCookie(List<Cookie> cookies, Cookie cookie) {
		for(int i = 0; i < cookies.size(); i++) {
			if(cookies.get(i).name().equals(cookie.name())) {
				return i;
			}
		}
		return -1;
	}

	private int containsCookie(List<Cookie> cookies, String name) {
		for(int i = 0; i < cookies.size(); i++) {
			if(cookies.get(i).name().equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
}
