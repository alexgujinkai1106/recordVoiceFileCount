package com.util;

import com.ctid.core.exception.ServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * HTTP请求处理工具类
 * 
 * @author 李志鹏
 * @date 2018-5-3
 * @email lizhipeng_xy@189.cn
 */
public class HttpUtil {

	private static Log LOGGER = LogFactory.getLog(HttpUtil.class);

	/**
	 * GET请求
	 * 
	 * @author 李志鹏
	 * @param getUrl
	 * @throws IOException
	 * @return 提取HTTP响应报文包体，以字符串形式返回
	 */
	public static String httpGet(String url, Map<String, String> getHeaders)
			throws IOException {
		Long startTime = System.currentTimeMillis();
		URL getURL = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) getURL
				.openConnection();
		connection.setRequestProperty("accept", "*/*");
		connection.setRequestProperty("connection", "Keep-Alive");
		connection.setRequestProperty("user-agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		// 设置连接主机超时（单位：毫秒）
		connection.setConnectTimeout(5000);
		// 设置从主机读取数据超时（单位：毫秒），解决卡死导致定时任务暂停执行问题
		connection.setReadTimeout(5000);
		if (getHeaders != null) {
			for (String pKey : getHeaders.keySet()) {
				connection.setRequestProperty(pKey, getHeaders.get(pKey));
			}
		}
		connection.connect();
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));
		StringBuilder sbStr = new StringBuilder();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			sbStr.append(line);
		}
		bufferedReader.close();
		connection.disconnect();
		Long endTime = System.currentTimeMillis();
		LOGGER.info("[Calculate the HTTP request take time] [url:" + url
				+ "] spend time " + (endTime - startTime) + " ms");
		return new String(sbStr.toString().getBytes(), "utf-8");
	}

	/**
	 * @Title: 发送GET请求
	 * @author 李志鹏
	 * @Description: get请求
	 * @param url
	 *            请求url ，不可为空
	 * @param charset
	 *            编码格式，不可为空
	 * @param params
	 *            请求参数
	 * @param timeOut
	 *            超时时间 ，默认5000，即5秒超时
	 * @param headers
	 *            http头信息
	 * @return result 返回结果
	 * @throws ServiceException
	 * @date 2018-5-03
	 */
	public static String doGet(Map<String, String> parameters,
			Map<String, String> headers, String url, String charset, int timeOut)
			throws ServiceException {
		Long startTime = System.currentTimeMillis();
		if (timeOut == 0) {
			// 默认5000，即5秒超时
			timeOut = 5000;
		}
		String result = "";
		// 读取响应输入流
		InputStream in = null;
		// 存储参数
		StringBuffer sb = new StringBuffer();
		// 编码之后的参数
		String params = "";
		System.out.println(parameters.size());
		try {

			// 编码请求参数
			if (parameters.size() == 1) {
				for (String name : parameters.keySet()) {
					sb.append(name)
							.append("=")
							.append(java.net.URLEncoder.encode(
									parameters.get(name), charset));
				}
				params = sb.toString();
			} else {
				for (String name : parameters.keySet()) {
					sb.append(name)
							.append("=")
							.append(java.net.URLEncoder.encode(
									parameters.get(name), charset)).append("&");
				}
				String tempParams = sb.toString();
				params = tempParams.substring(0, tempParams.length() - 1);
			}
			String fullUrl = url + "?" + params;
			// 创建URL对象
			URL connURL = new URL(fullUrl);
			// 打开URL连接
			HttpURLConnection httpConn = (HttpURLConnection) connURL
					.openConnection();
			// 设置请求头
			if (headers != null) {
				for (String pKey : headers.keySet()) {
					httpConn.setRequestProperty(pKey, headers.get(pKey));
				}
			}
			// 设置通用属性
			httpConn.setRequestProperty("Accept", "*/*");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
			// 设置连接主机超时（单位：毫秒）
			httpConn.setConnectTimeout(timeOut);
			// 设置从主机读取数据超时（单位：毫秒）
			httpConn.setReadTimeout(timeOut);
			// 建立实际的连接
			httpConn.connect();
			// 定义BufferedReader输入流来读取URL的响应,并设置编码方式
			// 读取返回的内容
			in = httpConn.getInputStream();
			result = readInputStreamToString(in, charset);
			httpConn.disconnect();
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException(
					"HttpUtil.doGet.UnsupportedEncodingException", e);
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException("HttpUtil.doGet.MalformedURLException",
					e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException("HttpUtil.doGet.IOException", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.error(e.getMessage());
					in = null;
				}
			}
			Long endTime = System.currentTimeMillis();
			LOGGER.info("[Calculate the HTTP request take time] [url:" + url
					+ "] spend time " + (endTime - startTime) + " ms");
		}
		return result;
	}

	/**
	 * @Title: 发送post请求
	 * @author 李志鹏
	 * @Description: post请求
	 * @param url
	 *            请求url ，不可为空
	 * @param charset
	 *            编码格式，不可为空
	 * @param params
	 *            请求参数
	 * @param timeOut
	 *            超时时间 ，默认5000，即5秒超时
	 * @param headers
	 *            http头信息
	 * @return result 返回结果
	 * @throws ServiceException
	 * @date 2018-5-03
	 */
	public static String doPost(Map<String, String> parameters,
			Map<String, String> headers, String url, String charset, int timeOut)
			throws ServiceException {
		Long startTime = System.currentTimeMillis();
		if (timeOut == 0) {
			// 默认5000，即5秒超时
			timeOut = 5000;
		}
		if (parameters == null) {
			throw new ServiceException("post请求params为空");
		}
		// 返回的结果
		String result = "";
		// 读取响应输入流
		InputStream in = null;
		PrintWriter out = null;
		// 处理请求参数
		StringBuffer sb = new StringBuffer();
		// 编码之后的参数
		String params = "";
		try {
			// 编码请求参数
			if (parameters.size() == 1) {
				for (String name : parameters.keySet()) {
					sb.append(name)
							.append("=")
							.append(java.net.URLEncoder.encode(
									parameters.get(name), charset));
				}
				params = sb.toString();
			} else {
				for (String name : parameters.keySet()) {
					sb.append(name)
							.append("=")
							.append(java.net.URLEncoder.encode(
									parameters.get(name), charset)).append("&");
				}
				String tempParams = sb.toString();
				params = tempParams.substring(0, tempParams.length() - 1);
			}
			// 创建URL对象
			URL connURL = new URL(url);
			// 打开URL连接
			HttpURLConnection httpConn = (HttpURLConnection) connURL
					.openConnection();
			// 设置请求头
			if (headers != null) {
				for (String pKey : headers.keySet()) {
					httpConn.setRequestProperty(pKey, headers.get(pKey));
				}
			}
			// 设置通用属性
			httpConn.setRequestProperty("Accept", "*/*");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
			// 设置POST方式
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			// 设置连接主机超时（单位：毫秒）
			httpConn.setConnectTimeout(timeOut);
			// 设置从主机读取数据超时（单位：毫秒）
			httpConn.setReadTimeout(timeOut);
			// 建立实际的连接
			httpConn.connect();
			// 获取HttpURLConnection对象对应的输出流
			out = new PrintWriter(httpConn.getOutputStream());
			// 发送请求参数
			out.write(params);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应，设置编码方式
			in = httpConn.getInputStream();
			result = readInputStreamToString(in, charset);
			httpConn.disconnect();
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException(
					"HttpUtil.doGet.UnsupportedEncodingException", e);
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException("HttpUtil.doGet.MalformedURLException",
					e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException("HttpUtil.doGet.IOException", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.error(e.getMessage());
					in = null;
				}
			}
			if (out != null) {
				out.close();
			}
			Long endTime = System.currentTimeMillis();
			LOGGER.info("[Calculate the HTTP request take time] [url:" + url
					+ "] spend time " + (endTime - startTime) + " ms");
		}
		return result;
	}

	/**
	 * @Title: 发送post请求 ，
	 * @author 李志鹏
	 * @Description: post请求
	 * @param url
	 *            请求url ，不可为空
	 * @param charset
	 *            编码格式，不可为空
	 * @param params
	 *            请求参数
	 * @param timeOut
	 *            超时时间 ，默认5000，即5秒超时
	 * @param headers
	 *            http头信息
	 * @return result 返回结果
	 * @throws ServiceException
	 * @date 2018-5-03
	 */
	public static String doPostForStringParam(String parameters,
			Map<String, String> headers, String url, String charset, int timeOut)
			throws ServiceException {
		Long startTime = System.currentTimeMillis();
		if (timeOut == 0) {
			// 默认5000，即5秒超时
			timeOut = 5000;
		}
		if (parameters == null) {
			throw new ServiceException("post请求params为空");
		}
		// 返回的结果
		String result = "";
		// 读取响应输入流
		InputStream in = null;
		PrintWriter out = null;
		// 编码之后的参数
		// String params = "";
		try {
			// 编码请求参数
			// params = java.net.URLEncoder.encode(parameters,charset);
			// 创建URL对象
			URL connURL = new URL(url);
			// 打开URL连接
			HttpURLConnection httpConn = (HttpURLConnection) connURL
					.openConnection();
			// 设置请求头
			if (headers != null) {
				for (String pKey : headers.keySet()) {
					httpConn.setRequestProperty(pKey, headers.get(pKey));
				}
			}
			// 设置通用属性
			httpConn.setRequestProperty("Accept", "*/*");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
			// 设置POST方式
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			// 设置连接主机超时（单位：毫秒）
			httpConn.setConnectTimeout(timeOut);
			// 设置从主机读取数据超时（单位：毫秒）
			httpConn.setReadTimeout(timeOut);
			// 建立实际的连接
			httpConn.connect();

			// 获取HttpURLConnection对象对应的输出流
			out = new PrintWriter(httpConn.getOutputStream());
			// 发送请求参数
			out.write(parameters);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应，设置编码方式
			in = httpConn.getInputStream();
			result = readInputStreamToString(in, charset);
			httpConn.disconnect();
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException(
					"HttpUtil.doGet.UnsupportedEncodingException", e);
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException("HttpUtil.doGet.MalformedURLException",
					e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException("HttpUtil.doGet.IOException", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.error(e.getMessage());
					in = null;
				}
			}
			if (out != null) {
				out.close();
			}
			Long endTime = System.currentTimeMillis();
			LOGGER.info("[Calculate the HTTP request take time] [url:" + url
					+ "] spend time " + (endTime - startTime) + " ms");
		}
		return result;
	}

	/**
	 * 读取输入流并以字符串形式返回
	 * 
	 * @author 李志鹏
	 * @param in
	 *            HTTP响应的输入流
	 * @return HTTP响应正文，字符串形式
	 * @throws ServiceException
	 */
	public static String readInputStreamToString(InputStream in, String charset)
			throws ServiceException {
		StringBuffer myBuffer = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in, charset));
			String line = reader.readLine();
			while (line != null) {
				myBuffer.append(line);
				line = reader.readLine();
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException("IOException", e);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException("IOException", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					reader = null;
					LOGGER.error(e.getMessage());
				}
			}
		}
		return myBuffer.toString();
	}

	/**
	 * http接口返回响应给调用者
	 * 
	 * @author 李志鹏
	 * @Title: pwWrite
	 * @Description: 输出结果
	 * @param resp
	 * @param resultJson
	 *            输出内容
	 * @date 2018-5-3
	 */
	public static void pwWrite(HttpServletResponse resp, String resultJson) {
		PrintWriter out = null;
		try {
			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");
			out = resp.getWriter();
			out.write(resultJson);
		} catch (IOException e) {
			LOGGER.error("pwWrite输出响应结果异常", e);
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

}
