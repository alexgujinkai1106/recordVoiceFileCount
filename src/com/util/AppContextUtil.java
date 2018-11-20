package com.util;

import com.ctid.core.exception.ServiceException;

import java.util.Random;
import java.util.ResourceBundle;

/**
 * 获取配置数据工具类
 * 
 * @author 李志鹏
 * @date 2018-5-3
 * @email lizhipeng_xy@189.cn
 */
public class AppContextUtil {

	/** key文件路径 */
	private static String propertiesPath = "com/config/properties/app";

	/**
	 * 取得配置文件中参数
	 */
	public static String getProperties(String key) throws ServiceException {
		String returnKeyValue = "";
		try {
			ResourceBundle rb = ResourceBundle.getBundle(propertiesPath);
			returnKeyValue = rb.getString(key).trim();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("获取属性文件异常");
		}
		if (returnKeyValue != null && !returnKeyValue.trim().equals("")) {
			return returnKeyValue;
		} else {
			throw new ServiceException("获取属性文件异常");
		}
	}

	
	/**
	 * 生成指定位数的随机数
	 * 
	 * @param num
	 *            随机数位数
	 * @return 随机数
	 */
	public static String getRandom(int num) {
		Random random = new Random();
		String result = "";
		for (int i = 0; i < num; i++) {
			result += random.nextInt(10);
		}
		return result;
	}
	
}
