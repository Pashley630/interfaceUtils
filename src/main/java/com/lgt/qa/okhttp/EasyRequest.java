package com.lgt.qa.okhttp;

import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 简单请求处理类，封装了okhttp工具包，自带cookie容器，兼容https协议
 *
 */
public class EasyRequest {
	private static final Logger logger = LoggerFactory.getLogger(EasyRequest.class);
	private static final TempCookieJar cookieJar = new TempCookieJar();
	private SSLContext sslContext;
	private final OkHttpClient client = new OkHttpClient.Builder()
			.cookieJar(cookieJar)
			.followRedirects(false)
			.connectTimeout(60, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.build();
	private Request request;
	private Response response;
	private String responseBody;

	private String url;
	private String queryString;
	private String method;
	private RequestBody requestBody;
	private Map<String, String> headers;

	private static final String PLAIN = "text/plain; charset=utf-8";
	private static final String JSON = "application/json; charset=utf-8";
	private static final String XML = "application/xml; charset=utf-8";
	private static final String SOAP12 = "application/xml+soap; charset=utf-8";

	/**
	 * 初始化TSL证书
	 */
	public void init() {
		X509TrustManager[] tms = initTlsCert();
		if(sslContext != null) {
			client.newBuilder()
			.sslSocketFactory(sslContext.getSocketFactory(), tms[0])
			.hostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			})
			.build();
		}
	}
	/**
	 * 根据给定的url构造EasyRequest对象
	 * @param url 要访问的url地址
	 */
	public EasyRequest(String url) {
		init();
		this.url = url;
		request = new Request.Builder()
				.url(url)
				.build();	

		//cookieJar.clearCookie();  //清除cookie，避免cookie干扰
	}
	/**
	 * 设置需要传递的query string参数
	 * @param queryStr 指定的query字符串，格式为key1=value1&key2=value2....多个参数之间用&隔开 
	 */
	public void setQueryString(String queryStr) {
//		this.queryString = queryStr;
		
		if (StringUtils.isBlank(this.queryString))
			this.queryString = queryStr;
		else
			this.queryString=this.queryString+"&"+queryStr;		
		
		/*
		String url = request.url().toString();
		if(url.indexOf("?")!=-1) { // 如果已经存在query字符串
			url = url.substring(0, url.indexOf("?"));
		}
		url = url+(queryStr!=null?"?"+queryStr:"");
		request = request.newBuilder() //重新构建request
				.url(url)
				.build();
		 */
	}

	public String getQueryString() {
		return queryString;
	}
	/**
	 * 根据给定的url和query参数构造EasyRequest对象
	 * @param url 要访问的url地址
	 * @param queryStr 需要传递的query字符串参数
	 */
	public EasyRequest(String url, String queryStr) {
		this(url);
		//request = new Request.Builder().build();
		this.queryString = queryStr;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return this.url;
	}

	/**
	 * 主动向cookie容器添加cookie
	 * @param name cookie名字
	 * @param value cookie的值
	 * @param domain 主机
	 * @param path 路径
	 * @param expire 过期时间，单位毫秒
	 */
	public void addCookie(String name, String value, String domain,String path,long expire) {
		Cookie cookie = new Cookie.Builder()
				.name(name)
				.value(value)
				.domain(domain)
				.path(path)
				.expiresAt(expire)
				.build();
		cookieJar.addCookie(request.url().host(), cookie);
	}

	/**
	 * 主动向cookie容器添加cookie
	 * @param cookie 
	 */
	public void addCookie(Cookie cookie) {
		cookieJar.addCookie(request.url().host(), cookie);		
	}

	public void deleteCookie(Cookie cookie) {
		cookieJar.clearCookie(request.url().host(), cookie);
	}

	public Cookie getCookie(String name) {
		return cookieJar.getCookie(request.url().host(), name);
	}

	/**
	 * 设置请求方法
	 * @param method 方法名称，对大小写不明感
	 */
	public void setMethod(String method) {
		//Request.Builder builder = request.newBuilder();
		this.method = method.toUpperCase();
		//request = builder.method(method, null).build();
	}

	/**
	 * 设置需要传参数的请求方法，如post
	 * @param method 方法名称，对大小写不明感
	 * @param formDate 需要传递的数据
	 */
	public void setMethod(String method, Map<String,String> formDate) {
		//Request.Builder builder = request.newBuilder();
		this.method = method.toUpperCase();
		FormBody.Builder formBuilder = new FormBody.Builder();
		for(Map.Entry<String, String> entry : formDate.entrySet()) {
			formBuilder.add(entry.getKey(), entry.getValue());
		}
		requestBody = formBuilder.build();
		//request = builder.method(method, formBody).build();
	}

	public void setMethod(String method, String mimeType ,String bodyData) {
		this.method = method.toUpperCase();
		MediaType mt = null;
		switch(mimeType) {
		case "json":
			mt = MediaType.parse(JSON);
			break;
		case "xml":
			mt = MediaType.parse(XML);
			break;
		case "soap12":
			mt = MediaType.parse(SOAP12);
			break;
		case "plain":
			mt = MediaType.parse(PLAIN);
			break;
		default:
			logger.warn("不支持的mime_type类型");
			break;
		}
		if(mt!=null) {
			/*request = request.newBuilder()
				.method(method, RequestBody.create(mt, bodyData))
				.build();*/
			requestBody = RequestBody.create(mt, bodyData);
		}
	}

	public String getMethod() {
		return method;
	}

	/**
	 * 设置头信息
	 * @param headers 需要设置的header的集合
	 */
	public void setHeaders(Map<String, String> headers) {
		/*Request.Builder builder = request.newBuilder();
		for(Map.Entry<String, String> entry : headers.entrySet()) {
			builder.addHeader(entry.getKey(), entry.getValue());
		}
		request = builder.build();*/
		this.headers = headers;
	}
	
	/**
	 * @author 王健
	 * 设置头信息
	 * @param 设置单条头
	 */
	public void setHeaders(String headName, String headValue) {
		if (this.headers==null)
			this.headers=new HashMap<String,String>();
		this.headers.put(headName, headValue);
	}

	public List<Cookie> getCookies(){
		return cookieJar.getAllCookie(request.url().host());
	}

	public void setCookies(List<Cookie> cookies) {
		String host = request.url().host();
		if(cookies != null) {
			int len = cookies.size();
			for(int i = 0; i < len; i++) {
				cookieJar.addCookie(host, cookies.get(i));
			}
		}
	}

	public void setRequestBody(RequestBody requestBody) {
		this.requestBody = requestBody;
	}

	public RequestBody getRequestBody() {
		return requestBody;
	}

	public Map<String,String> getHeaders(){
		return headers;
	}

	/**
	 * 执行请求
	 * @return 请求的Response对象
	 * @throws IOException 如果连接已关闭，则会报读写异常，注意，response在OkHttp中是自动关闭的
	 */
	public Response executeRequest() throws IOException {
		Request.Builder build = request.newBuilder();
		// 拼装url
		if(url.indexOf("?")!=-1) { // 如果已经存在query字符串
			url = url.substring(0, url.indexOf("?"));
		}
		url = url+(queryString!=null?"?"+queryString:"");
		build.url(url);
		// 填充headers
		if(headers != null) {
			for(Map.Entry<String, String> header : headers.entrySet()) {
				build.addHeader(header.getKey(), header.getValue());
			}
		}
		// 设置请求方法和body参数
		build.method(method, requestBody);
		request = build.build();
		response = client.newCall(request).execute();
		responseBody = response.body().string();

		cookieJar.clearCookie();

		return response;
	}
	/**
	 * 获取应答身体部分的数据
	 * @return 应答的body部分字符串表达式
	 */
	public String getRespnseBody() {
		return this.responseBody;
	}
	/**
	 * 获取response对象
	 * @return Response对象
	 */
	public Response getResponse() {
		return this.response;
	}
	/**
	 * 获取request对象
	 * @return request对象
	 */
	public Request getRequest() {
		return this.request;
	}
	/**
	 * 创建509认证管理器，信任所有证书
	 * @return 证书管理器
	 */
	private X509TrustManager[] initTlsCert() {
		final X509TrustManager[] trustAllCerts = 
				new X509TrustManager[] {
						new X509TrustManager() {

							@Override
							public X509Certificate[] getAcceptedIssuers() {
								X509Certificate[] x509Certificates = new X509Certificate[0];
								return x509Certificates;
							}

							@Override
							public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

							}

							@Override
							public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
							}
						}};
		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			logger.error("证书协议错误", e);
			e.printStackTrace();
		} catch (KeyManagementException e) {
			logger.error("钥匙管理异常", e);
			e.printStackTrace();
		}
		return trustAllCerts;
	}

	public static void main(String[] args) {
		EasyRequest easy = new EasyRequest("http://test.tfsicsp.com/api/v1.0/catering_service/application/login");
		easy.setQueryString("device_info=eyJkZXZpY2VUeXBlIjoiUEMiLCJvc1R5cGUiOiJ3aW5kb3dzIiwiYWdlbnRUeXBlIjoiQnJvd3NlciJ9&code=Mr6YL9o4SvyPpdSHajyWlg&installation_id=JVMgA22ASsQLY57k9NGyHA&state=eyJmcm9tX1VSTCI6Imh0dHA6Ly90ZXN0LnRmc2ljc3AuY29tIiwidGFyZ2V0X3VybCI6Imh0dHA6Ly90ZXN0LnRmc2ljc3AuY29tIn0&client_id=N_sr5zVpSKSvxL38nlKgVQ");
		Cookie cookie = new Cookie.Builder()
				.name("access_token")
				.value("l8_bEAEaQm-xT6Y3UV4Jvw")
				.domain("test.tfsicsp.com")
				.path("/").build();
		easy.addCookie(cookie);
		easy.setMethod("get");
		try {
			easy.executeRequest();
			System.out.println(easy.getRespnseBody());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
