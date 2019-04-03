package com.imooc.selenium.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

/**
 * @author yuanfang1.qin
 *
 */
public class Driver {

	public static final String FIREFOX = "firefox";
	public static final String CHROME = "chrome";
	public static final String IE = "internet explorer";
	public static final String EDGE = "MicrosoftEdge";
	public static final String SAFARI = "Safari";
	
	private Log log = new Log(Driver.class);

	public WebDriver getDriver(String browser) {
		WebDriver newDriver = null;
		try {
			switch (browser) {
			case FIREFOX:
				log.info("firefox driver path: "+JsonMapperUtil.getBrowerDriver(FIREFOX));
				System.setProperty("webdriver.gecko.driver", JsonMapperUtil.getBrowerDriver(FIREFOX));
				newDriver = new FirefoxDriver();
				break;

			case CHROME:
				log.info("chrome driver path: "+JsonMapperUtil.getBrowerDriver(CHROME));
				System.setProperty("webdriver.chrome.driver", JsonMapperUtil.getBrowerDriver(CHROME));
				newDriver = new ChromeDriver();
				break;

			case IE:
				log.info("ie driver path: "+JsonMapperUtil.getBrowerDriver(IE));
				System.setProperty("webdriver.ie.driver", JsonMapperUtil.getBrowerDriver(IE));
				newDriver = new InternetExplorerDriver();
				break;
			
			case SAFARI:
				log.info("safari driver path: "+JsonMapperUtil.getBrowerDriver(SAFARI));
				System.setProperty("webdriver.safari.driver", JsonMapperUtil.getBrowerDriver(SAFARI));
				newDriver = new SafariDriver();
				break;

			default:
				log.warn("********dirver does not set********");
				log.warn(browser + " driver has not been assigned in class: " + Driver.class.getName());
				log.warn("The script will run in the default browser: Firefox");
				System.setProperty("webdriver.gecko.driver", JsonMapperUtil.getBrowerDriver(FIREFOX));
				newDriver = new FirefoxDriver();
				break;
			}
		} catch (Exception e) {
			log.error("Create browser object failed.", e);
		}

		return newDriver;

	}
}
