package com.imooc.selenium.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author yuanfang1.qin
 *
 */
public class JsonMapperUtil {

	private static final String SITES = "src/test/resources/config/sitesCfg.json";
	private static final String BROWSERS = "src/test/resources/config/browsersCfg.json";
	private static final String DRIVERS = "src/test/resources/config/driversCfg.json";
	private static Log log = new Log(JsonMapperUtil.class);

	public static Object[][] getBrowsers() {
		return getObjectArraysFromJsonFile(BROWSERS);
	}

	private static HashMap<String, String> getParameterHashMapFromJsonFile(String pathname) {
		HashMap<String, String> parameterList = null;

		ObjectMapper mapper = new ObjectMapper();

		try {
			parameterList = mapper.readValue(new File(pathname), new TypeReference<HashMap<String, String>>() {
			});

		} catch (JsonGenerationException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (JsonMappingException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (IOException e) {
			log.error("No configuration file available.", e);
		}
		return parameterList;
	}

	public static Object[][] getObjectArraysFromJsonFile(String pathname) {

		ObjectMapper mapper = new ObjectMapper();
		Object[][] browsersList = null;

		try {
			browsersList = mapper.readValue(new File(pathname), new TypeReference<Object[][]>() {
			});

			switch (pathname) {
			case BROWSERS:
				/*
				 * CROSS_BROWSER = true will run case in multi-browsers, CROSS_BROWSER = false
				 * all test scripts will run in the first browser that is setting in the
				 * browsersCfg.json
				 */
				if (!isCrossBrowser()) {

					int columnLength = browsersList[0].length;
					Object[][] firstRecord = new Object[1][columnLength];
					for (int i = 0; i < columnLength; i++) {
						System.out.println("browsersList[0][" + i + "]" + browsersList[0][i]);
						firstRecord[0][i] = browsersList[0][i];
					}
					browsersList = firstRecord;
				}
				break;
			}

		} catch (JsonGenerationException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (JsonMappingException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (IOException e) {
			log.error("No configuration file available.", e);

		}

		return browsersList;

	}

	/**
	 * Through this method to get JSONObject from any of JSON files.
	 * 
	 * @param pathname
	 * @return
	 */
	public static JSONObject getJasonObjectFromJsonFile(String pathname) {

		File file = new File(pathname);
		String content;
		JSONObject jsonObject = null;

		try {
			content = FileUtils.readFileToString(file, "UTF-8");
			jsonObject = new JSONObject(content);

		} catch (JsonGenerationException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (JsonMappingException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (IOException e) {
			log.error("No configuration file available.", e);
		}

		return jsonObject;
	}

	/**
	 * 
	 * @return URLs that will be used from sitesCfg.json
	 */
	public static JSONObject getEnvUrlFromJsonFile() {

		JSONObject jsonObject = getJasonObjectFromJsonFile(SITES);
		// Get the URLs that is related to the parameter TEST_ENV setting
		return jsonObject.getJSONObject(jsonObject.getString("TEST_ENV"));

	}

	/**
	 * Through this method to read sitesCfg.json
	 * 
	 * @return true/false - will scripts run in cross browsers?
	 */
	public static Boolean isCrossBrowser() {
		/*
		 * Get the value of CROSS_BROWSER in sitesCfg.json
		 */
		Boolean isCrossBrowser = null;
		JSONObject jsonObject;
		
		try {
			jsonObject = getJasonObjectFromJsonFile(SITES);
			isCrossBrowser = jsonObject.getBoolean("CROSS_BROWSER");
			log.info("Is cross browser:" + isCrossBrowser);
		} catch (JSONException e) {
			log.error("The boolean type parameter CROSS_BROWSER should be true / false, please check it in the file: "
					+ SITES, e);

		}
		return isCrossBrowser;

	}

	/**
	 * Through this method to read sitesCfg.json
	 * 
	 * @return true/false - will use which browsers?
	 */
	public static Boolean isRunAtLocal() {
		/*
		 * Get the value of RUN_AT_LOCAL in sitesCfg.json
		 */
		Boolean isRunAtLocal = null;
		JSONObject jsonObject;
		
		try {
			jsonObject = getJasonObjectFromJsonFile(SITES);
			isRunAtLocal = jsonObject.getBoolean("RUN_AT_LOCAL");
		} catch (JSONException e) {
			log.error("The boolean type parameter RUN_AT_LOCAL should be true / false, please check it in the file: "
					+ SITES, e);
		}
		return isRunAtLocal;

	}
	
	/**
	 * Through this method to read sitesCfg.json
	 * 
	 * @return true/false - will run source code on windows or not?
	 */
	public static Boolean isSourcecodeOnWindows() {
		/*
		 * Get the value of SOURCECODE_IS_ON_WINDOWS in sitesCfg.json
		 */
		Boolean isRunAtLocal = null;
		JSONObject jsonObject;
		
		try {
			jsonObject = getJasonObjectFromJsonFile(SITES);
			isRunAtLocal = jsonObject.getBoolean("SOURCECODE_IS_ON_WINDOWS");
		} catch (JSONException e) {
			log.error("The boolean type parameter RUN_AT_LOCAL should be true / false, please check it in the file: "
					+ SITES, e);
		}
		return isRunAtLocal;

	}

	/**
	 * Through this method to read driversCfg.json
	 * 
	 * @param browser:
	 *            the test script will run in which browser
	 * @return the browser dirver's path
	 */
	public static String getBrowerDriver(String browser) {
		/*
		 * Currently, we've already set 4 drivers:
		 * 1. firefox 2. chrome 3. internet explorer 4. safari
		 */
		File file = new File(DRIVERS);
		String content;
		JSONObject jsonObject;

		try {
			content = FileUtils.readFileToString(file, "UTF-8");
			if(isSourcecodeOnWindows()) {
				jsonObject = new JSONObject(content).getJSONObject("windows");
			} else {
				jsonObject = new JSONObject(content).getJSONObject("other");
			}
			browser = browser.toLowerCase();
			switch (browser) {
			case Driver.FIREFOX:
				return jsonObject.getString("firefox");
			case Driver.CHROME:
				return jsonObject.getString("chrome");
			case Driver.IE:
				return jsonObject.getString("internet explorer");
			case Driver.SAFARI:
				return jsonObject.getString("Safari");
			}
		} catch (JsonGenerationException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (JsonMappingException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (IOException e) {
			log.error("No configuration file available.", e);
		}
		return null;
	}

	/**
	 * Through this method to read data JSON file which will be used in the test
	 * scripts, and you must know where the test data JSON file is.
	 * 
	 * @param path:
	 *            the test data JSON file
	 * @param browser:
	 *            if we need to run cases in cross browsers, the order in which the
	 *            data is used needs to match the browsers Settings
	 * @return only one test record
	 */
	public static JSONObject getTestData(String dataJsonFilePath, String browser) {
		File file = new File(dataJsonFilePath);
		String content;
		JSONArray testData;
		
		try {
			content = FileUtils.readFileToString(file, "UTF-8");
			testData = new JSONArray(content);
			if (testData.length() > 0) {
				if (!isCrossBrowser()) {
					// The first record will be used in the single browser mode
					return testData.getJSONObject(0);

				} else if (isCrossBrowser() && testData.length() != getBrowsers().length && getBrowsers() != null) {
					/*
					 * For multiple browsers mode if the number of test data is not equal to the
					 * number of browsers, the first test data will be used in all browsers.
					 */
					log.warn("The record number of the test data file: " + dataJsonFilePath
							+ " is not equal to the number of the running browsers, "
							+ "so only the first test data will be used in the multi-browsers!");
					return testData.getJSONObject(0);

				} else {
					// And each test data will be mapped with each browser.
					browser = browser.toLowerCase();
					switch (browser) {
					case Driver.FIREFOX:
						return testData.getJSONObject(getTestDataForBrowser(Driver.FIREFOX));
					case Driver.CHROME:
						return testData.getJSONObject(getTestDataForBrowser(Driver.CHROME));
					case Driver.IE:
						return testData.getJSONObject(getTestDataForBrowser(Driver.IE));
					case Driver.SAFARI:
						return testData.getJSONObject(getTestDataForBrowser(Driver.SAFARI));
					default:
						log.warn("The browser '" + browser + "' is not setup, so use the first record to run.");
						return testData.getJSONObject(0);
					}

				}
			} else {
				log.error("The following test data file is empty: " + dataJsonFilePath);
			}

		} catch (JsonGenerationException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (JsonMappingException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (IOException e) {
			log.error("No configuration file available." + dataJsonFilePath, e);
		}
		return null;
	}

	private static int getTestDataForBrowser(String browser) {
		Object[][] browsers = getBrowsers();
		for(int i = 0; i < browsers.length; i++) {
			if(browsers[i][0].equals(browser)) {
				return i;
			}
		}
		return 0;
		
	}
	
	/**
	 * Through this method to read data JSON file which will be used in the test
	 * scripts, you DONOT need to know where the test data JSON file is.
	 * 
	 * @param c:
	 *            the current running case
	 * @param browser
	 * @return only one record
	 */
	public static JSONObject getTestData(Class<?> c, String browser) {
		String dataJsonFilePath = getTestDataFile(c);
		File dataJsonFile = new File(dataJsonFilePath);
		String content;
		JSONArray testData;
		
		try {
			content = FileUtils.readFileToString(dataJsonFile, "UTF-8");
			testData = new JSONArray(content);
			if (testData.length() > 0) {

				if (!isCrossBrowser()) {
					// The first record will be used in the single browser mode
					return testData.getJSONObject(0);

				} else if (isCrossBrowser() && testData.length() != getBrowsers().length && getBrowsers() != null) {
					/*
					 * For multiple browsers mode if the number of test data is not equal to the
					 * number of browsers, the first test data will be used in all browsers.
					 */
					log.warn("The record number of the test data file: " + dataJsonFilePath
							+ " is not equal to the number of the running browsers, "
							+ "so only the first test data will be used in the multi-browsers!");
					return testData.getJSONObject(0);

				} else if(isCrossBrowser() && testData.length() == getBrowsers().length){				
					// Each test data will be mapped with each browser.
					browser = browser.toLowerCase();
					switch (browser) {
					case Driver.FIREFOX:
						return testData.getJSONObject(0);
					case Driver.CHROME:
						return testData.getJSONObject(1);
					case Driver.IE:
						return testData.getJSONObject(2);
					case Driver.EDGE:
						return testData.getJSONObject(3);
					case Driver.SAFARI:
						return testData.getJSONObject(4);
					default:
						return testData.getJSONObject(0);
					}

				}
			} else {
				log.error("The following test data file is empty: " + dataJsonFilePath);
			}

		} catch (JsonGenerationException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (JsonMappingException e) {
			log.error("The json configuration file is not valid. Please verify the file.", e);

		} catch (IOException e) {
			log.error("No configuration file available: " + dataJsonFilePath, e);
		}
		return null;
	}

	/**
	 * To get the Test case's corresponding data JSON file
	 * 
	 * @param c:
	 *            we will use the class info to automatically match the
	 *            corresponding data file
	 * @return JSON file path
	 */
	public static String getTestDataFile(Class<?> c) {
		String path;
		
		try {
			path = c.getName().replace(getJasonObjectFromJsonFile(SITES).getString("TEST_CASES"),
					getJasonObjectFromJsonFile(SITES).getString("TEST_DATA")).replace(".", "/");
			return path + ".json";

		} catch (JSONException e) {
			log.error(e);
		}
		return null;
	}

}
