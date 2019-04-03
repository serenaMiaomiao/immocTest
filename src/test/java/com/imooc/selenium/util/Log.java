package com.imooc.selenium.util;

import static org.testng.Assert.fail;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuanfang1.qin
 *
 */
public class Log {
	
	private static final String LOG = "src/test/resources/config/log4j.properties";
	static{
		PropertyConfigurator.configure(LOG);
	}
	
	private final Class<?> clazz;
	private Logger logger;
	
	
	public Log(Class<?> clazz){
		this.clazz = clazz;
		this.logger = LoggerFactory.getLogger(this.clazz);
	}

	public void info(String message) {
		logger.info(clazz.getCanonicalName() + ": " + message);
	}
	
	public void info(Exception e) {
		logger.info(clazz.getCanonicalName()+ " " + e.getMessage(), e);
	}
	
	public void info(String message, Exception e) {
		logger.info(clazz.getCanonicalName()+ " " + e.getMessage() + ", " + message, e);
	}
	
	public void debug(String message) {
		logger.debug(clazz.getCanonicalName() + ": " + message);
	}
	
	public void warn(String message) {
		logger.warn(clazz.getCanonicalName() + ": " + message);
	}
	
	public void error(String message) {
		logger.error(clazz.getCanonicalName() + ": " + message);
		fail(clazz.getCanonicalName() + ": " + message);
	}
	
	public void error(Exception e) {
		logger.error(clazz.getCanonicalName()+ " " + e.getMessage(), e);
		fail(clazz.getCanonicalName()+ " " + e.getMessage(), e);
	}
	
	public void error(String message, Exception e) {
		logger.error(clazz.getCanonicalName()+ " " + e.getMessage() + ", " + message, e);
		fail(clazz.getCanonicalName()+ " " + e.getMessage() + ", " + message, e);
	}
}
