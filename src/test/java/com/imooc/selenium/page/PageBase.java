package com.imooc.selenium.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.imooc.selenium.util.Locator;
import com.imooc.selenium.util.Log;
import com.imooc.selenium.util.XmlMapperUtil;



/**
 * @author yuanfang1.qin
 *
 */
public class PageBase {
	protected Log log = new Log(this.getClass());
	protected WebDriver driver;
	HashMap<String, Locator> locatorMap;
	String path;
	
	public enum UI {
		canSee, canClick, presence, isSelected, isInvisible;
	}

	protected PageBase(WebDriver driver) {
		this.driver = driver;
		path = "src/test/java/" + this.getClass().getCanonicalName().replace(".", "/") + ".xml";
		log.info(path);
		try {
			locatorMap = XmlMapperUtil.readXMLDocument(path, this.getClass().getCanonicalName());
		} catch (Exception e) {
			log.error("Read xml is failed: " + path);
		}
	}

	protected void click(Locator locator) {
		try {
			log.info("try to click " + locator.getElement());
			WebElement element = (WebElement)waitForElement(locator, UI.canClick);
			log.info("click " + locator.getElement());
			element.click();
		} catch (Exception e) {
			log.error(e);
		}
		
	}

	protected void sendKeys(Locator locator, String values) {
		try {
			WebElement element = (WebElement)waitForElement(locator, UI.canSee);
			if (element.isEnabled()) {
				log.info("type value: '" + values + "' into " + locator.getElement());
				element.sendKeys(values);
			} else {
				log.error("The locator: " + locator.getElement() + "is not enabled, cannot send keys.");
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	protected String getValue(Locator locator) {
		WebElement element;
		String value = null;
		try {
			element = (WebElement)waitForElement(locator, UI.canSee);
		    value = element.getAttribute("value").trim();
			log.info("The value of the element '"+locator.getElement()+"' is:" + value);
		} catch (Exception e) {
			log.error(e);
		}
		return value;
	}
	
	/**
	 * Through JS to handle that body / iframe ... elements
	 * @param handler
	 * @param isLocator
	 * @param willGet
	 * @param text
	 * @return
	 */
	private String handleSpecialTextBox(Object handler, Boolean isLocator, Boolean willGet, String text) {
		WebElement element;
		String result = null;
		JavascriptExecutor js = (JavascriptExecutor) driver;
		
		try {
			if(isLocator) {
				element = (WebElement)waitForElement((Locator) handler, UI.canSee);
			} else {
				element = (WebElement)handler;
			}
		
			if (willGet) {
				result = (String)js.executeScript("var result=arguments[0].innerHTML;return result;", element);
				log.info("The text of the richTextBox is " + result );
			} else {
				log.info("type value: '" + text + "' into richTextBox.");
				js.executeScript("arguments[0].innerHTML = \"" + text + "\"", element);
			}
		} catch (Exception e) {
			log.error(e);
		}
		
		return result;
		
	}
	
	protected void sendKeysByJS(Locator locator, String text) {
		boolean isLocator = true;
		boolean willGet = false;
		handleSpecialTextBox(locator, isLocator, willGet, text);
	}
	
	protected void sendKeysByJS(WebElement element, String text) {
		boolean isLocator = false;
		boolean willGet = false;
		handleSpecialTextBox(element, isLocator, willGet, text);
	}
	
	protected String getTextByJS(Locator locator) {
		boolean isLocator = true;
		boolean willGet = true;
		return  handleSpecialTextBox(locator, isLocator, willGet, null);
	}
	
	protected String getTextByJS(WebElement element) {
		boolean isLocator = false;
		boolean willGet = true;
		return  handleSpecialTextBox(element, isLocator, willGet, null);
	}
	
	protected WebElement getElement(final Locator sourceLocator, UI ui) {
		WebElement element = (WebElement) waitForElement(sourceLocator, ui);
		if (element == null) {
			log.error("Can not find the element: " + sourceLocator.getElement());
		}
		return element;
	}
	
	private Object waitForElement(final Locator sourceLocator, UI ui) {
		int timeOut = 10;
		String locatorPath = null;
		Locator locator;
		WebDriverWait wait;

		try {
			locator = getLocator(sourceLocator.getElement());
			locatorPath = locator.getElement();
			timeOut = locator.getWaitSec();
			log.info("wait for element: '" + sourceLocator.getElement() + "' with value " + locatorPath);

			wait = new WebDriverWait(driver, timeOut);

			switch (ui) {
			case canSee:
				return wait.until(ExpectedConditions.visibilityOfElementLocated(getBy(sourceLocator)));

			case canClick:
				return wait.until(ExpectedConditions.elementToBeClickable(getBy(sourceLocator)));

			case presence:
				return wait.until(ExpectedConditions.presenceOfElementLocated(getBy(sourceLocator)));

			case isInvisible:
				return wait.until(ExpectedConditions.invisibilityOfElementLocated(getBy(sourceLocator)));
				
			case isSelected:
				return wait.until(ExpectedConditions.elementToBeSelected(getBy(sourceLocator)));
			
				
			default:
				return wait.until(new ExpectedCondition<WebElement>() {
					@Override
					public WebElement apply(WebDriver d) {
						try {
							return d.findElement(getBy(sourceLocator));
						} catch (NoSuchElementException e) {
							log.error(e);
							return null;
						}
					}
				});
			}
			
		} catch (NullPointerException e) {
			log.error(e);		
		} catch (TimeoutException e) {	
			log.info(e);
		}

		return null;
	}
	
	/**
	 * Will wait for the text displayed in the text value of the element.
	 * @param sourceLocator
	 * @param text should be A regular expression
	 * @return
	 */
	protected Boolean isTextDisplayed(final Locator sourceLocator, String text) {
		int timeOut = 10;
		String locatorPath = null;
		Locator locator;
		WebDriverWait wait;
		Boolean isExisted = null;
		Pattern pattern = Pattern.compile(text);

		try {
			locator = getLocator(sourceLocator.getElement());
			locatorPath = locator.getElement();
			timeOut = locator.getWaitSec();
			log.info("wait for element: '" + sourceLocator.getElement() + "' with value " + locatorPath);

			wait = new WebDriverWait(driver, timeOut);
			isExisted = wait.until(ExpectedConditions.textMatches(getBy(sourceLocator), pattern));			
		} catch (NullPointerException e) {
			log.error(e);
	    } catch (TimeoutException e) {
			log.info(e);
			return false;
		}
		
		return isExisted;
	}

	protected Boolean isElementDisappeared(final Locator sourceLocator) {
		boolean isDisappeared = false;
        String locatorPath;
		try {
			locatorPath = getLocator(sourceLocator.getElement()).getElement();
			log.info("wait for element disappeared: '" + sourceLocator.getElement() + "' with value " + locatorPath);
			isDisappeared = (Boolean) waitForElement(sourceLocator, UI.isInvisible);
		} catch (NullPointerException e) {
			log.error(e);
		} catch (TimeoutException e) {
			log.info(e);
			isDisappeared = false;
		}

		log.info("The element is disappeared: " + isDisappeared);
		return isDisappeared;
	}
	
	protected Boolean isElementDisappeared(WebElement element) {
		int timeOut = 5;
		long startTime = System.currentTimeMillis();
		long endTime;

		endTime = startTime + timeOut * 1000;
		log.info("wait for element disappeared");
		do {
			try {
				Thread.sleep(1000);
				startTime = startTime + 1000;
				element.isDisplayed();
			} catch (NullPointerException e) {
				log.info("The element is disappeared from the DOM.");
				return true;
			} catch (InterruptedException e) {
				// nothing
			}
		} while (startTime < endTime);

		log.info("The element is still displayed.");
		return false;
	}

	protected List<WebElement> getElements(final Locator sourceLocator){
		List<WebElement> listE = waitForElements(sourceLocator);
		if (listE == null) {
			log.error("Can not find the elements: " + sourceLocator.getElement());
		}
		return listE;
	}
	
	private List<WebElement> waitForElements(final Locator sourceLocator) {
		int timeOut = 10;
		String locatorPath = null;
		final Locator locator;
		List<WebElement> listE = null;

		try {
			locator = getLocator(sourceLocator.getElement());
			timeOut = locator.getWaitSec();
			locatorPath = locator.getElement();
			log.info("wait for element: '" + sourceLocator.getElement() + "' with value " + locatorPath);

			listE = new WebDriverWait(driver, timeOut).until(new ExpectedCondition<List<WebElement>>() {
				@Override
				public List<WebElement> apply(WebDriver d) {
					List<WebElement> list = null;
					try {
						list = d.findElements(getBy(sourceLocator));
						log.info("list size: " + list.size());
					} catch (NoSuchElementException e) {
						log.info("can't find element: '" + sourceLocator.getElement() + "' with value "
								+ locator.getElement());
					}
					return list;
				}
			});

		} catch (NullPointerException e) {
			log.error(e);
		} catch (TimeoutException e) {
			log.info("can't get element: " + sourceLocator.getElement() + " with value " + locatorPath);
		}
		
		return listE;

	}

	protected List<Object> getElementsText(final Locator sourceLocator) {
		List<WebElement> listE = waitForElements(sourceLocator);
		List<Object> listN = new ArrayList<>();
		for (int i = 0; i < listE.size(); i++) {
			log.info("element" + i + " text is: " + listE.get(i).getText().trim());
			listN.add(listE.get(i).getText().trim());
		}

		return listN;
	}

	protected By getBy(Locator sourceLocator) {
		Locator locator;
		locator = getLocator(sourceLocator.getElement());
		By by;
		
		switch (locator.getBy()) {
		case xpath:
			log.debug("find element By xpath");
			by = By.xpath(locator.getElement());
			break;
		case id:
			log.debug("find element By id");
			by = By.id(locator.getElement());
			break;
		case name:
			log.debug("find element By name");
			by = By.name(locator.getElement());
			break;
		case cssSelector:
			log.debug("find element By cssSelector");
			by = By.cssSelector(locator.getElement());
			break;
		case className:
			log.debug("find element By className");
			by = By.className(locator.getElement());
			break;
		case tagName:
			log.debug("find element By tagName");
			by = By.tagName(locator.getElement());
			break;
		case linkText:
			log.debug("find element By linkText");
			by = By.linkText(locator.getElement());
			break;
		case partialLinkText:
			log.debug("find element By partialLinkText");
			by = By.partialLinkText(locator.getElement());
			break;
		default:
			by = By.id(locator.getElement());
		}

		return by;
	}

	/**
	 * get an element from a list condition: if (the text of list option contains
	 * listText) then { we're going to get the list option from the list}
	 * 
	 * @param listText
	 * @param container
	 * @return web element or null
	 */
	protected WebElement getListOptionByText(String listText, final Locator container) {
		List<WebElement> list = waitForElements(container);
		WebElement option;
		
		try {
			for (int i = 0; i < list.size(); i++) {
				option = list.get(i);
				log.info("list option is: '" + option.getText() + "'.");
				if (option.getText().contains(listText)) {
					return option;
				}
			}
		} catch (NullPointerException e) {
			log.error(e);
		}
		log.error("There is not the '" + listText + "' option in the locator container: " + container.getElement());
		return null;
	}

	/**
	 * get an element from container condition: if (the text of the container
	 * contains listText) then { we're going to get the containerEle from the
	 * container}
	 * 
	 * @param listText
	 * @param container
	 * @param containerEle
	 * @return web element or null
	 */
	protected WebElement getElementFromContainerByText(String listText, final Locator container,
			final Locator containerEle) {
		log.info("getElementFromContainerByText with parameter: listText is '" + listText + "', container is '"
				+ container.getElement() + "', containerEle is '" + containerEle.getElement() + "'.");
		int timeOut = 10;
		List<WebElement> list;
		WebElement subEle = null;
		final String locatorPath;
		Locator locatorEle = null;

		try {
			list = waitForElements(container);
			locatorEle = getLocator(containerEle.getElement());
			locatorPath = locatorEle.getElement();
			timeOut = locatorEle.getWaitSec();

			for (int i = 0; i < list.size(); i++) {
				final WebElement parent = list.get(i);
				log.info("parent element's text: " + parent.getText());
				if (parent.getText().contains(listText)) {

					subEle = new WebDriverWait(driver, timeOut).until(new ExpectedCondition<WebElement>() {
						@Override
						public WebElement apply(WebDriver driver) {
							try {
								return parent.findElement(getBy(containerEle));
							} catch (NoSuchElementException e) {
								log.error("can't find the sub container element: '" + containerEle.getElement()
										+ "' with value " + locatorPath);
								return null;
							}
						}
					});
					
					if (subEle == null) {
						log.error("can't get element: " + containerEle.getElement() + " with value " + locatorEle.getElement());
					}
					
					log.info("sub element is displayed: " + subEle.isDisplayed());
					break;
				}

			}
			
		} catch (NullPointerException e) {
			log.error(e);
		}

		return subEle;

	}

	/**
	 * Is element existed or not? condition: if (the text of the container contains
	 * listText) then { we're going to determine whether the containerEle is existed
	 * in container or not}
	 * 
	 * @param container
	 * @param containerEle
	 * @param containerEleWithText
	 * @param eleText
	 * @return
	 */
	protected Boolean isElementExistedInContainerByText(String listText, final Locator container,
			final Locator containerEle) {
		int timeOut = 10;
		List<WebElement> list;
		Locator locatorEle;
		String locatorPath = null;
		WebElement subEle;

		try {
			list = waitForElements(container);
			locatorEle = getLocator(containerEle.getElement());
			locatorPath = locatorEle.getElement();
			timeOut = locatorEle.getWaitSec();

			for (int i = 0; i < list.size(); i++) {
				final WebElement parent = list.get(i);
				if (parent.getText().contains(listText)) {

					subEle = new WebDriverWait(driver, timeOut).until(new ExpectedCondition<WebElement>() {
						@Override
						public WebElement apply(WebDriver driver) {
							try {
								return parent.findElement(getBy(containerEle));
							} catch (NoSuchElementException e) {
								return null;
							}
						}
					});

					if (subEle != null) {
						return true;
					}

					break;

				}
			}

		} catch (NullPointerException e) {
			log.error(e);
		} catch (TimeoutException e) {
			log.info("Timeout to get element: " + containerEle.getElement() + " with value " + locatorPath + " in " + timeOut+ " sec.");
			return false;
		}
		
		return false;
	}

	/**
	 * get an element from container condition: if (each text in texts is
	 * contained in the container's text) then { we're going to get the containerEle
	 * from the container}
	 * 
	 * @param container
	 * @param containerEle
	 * @param containerEleWithText
	 * @param eleText
	 * @return
	 */
	protected WebElement getElementFromContainerByText(List<String> texts, final Locator container,
			final Locator containerEle) {
		int timeOut = 10;
		List<WebElement> list;
		final String locatorPath;
		Locator locatorEle = null;
		WebElement subEle = null;

		try {
			list = waitForElements(container);
			locatorEle = getLocator(containerEle.getElement());
			locatorPath = locatorEle.getElement();
			timeOut = locatorEle.getWaitSec();

			for (int i = 0; i < list.size(); i++) {
				final WebElement parent = list.get(i);
				boolean containerWithText = true;
				for (String s : texts) {
					if (!parent.getText().contains(s)) {
						containerWithText = false;
						break;
					}
				}
				if (containerWithText) {
					subEle = new WebDriverWait(driver, timeOut).until(new ExpectedCondition<WebElement>() {
						@Override
						public WebElement apply(WebDriver driver) {
							try {
								return parent.findElement(getBy(containerEle));
							} catch (NoSuchElementException e) {
								log.error("can't find the sub container element: '" + containerEle.getElement()
										+ "' with value " + locatorPath);
								return null;
							}
						}
					});
					
					if (subEle == null) {
						log.error("can't get element: " + containerEle.getElement() + " with value " + locatorEle.getElement());
					}
					
					break;
				}

			}
		} catch (NullPointerException e) {
			log.info("Try to get element: " + containerEle.getElement() + " with value " + locatorEle.getElement());
			log.error(e);
		}

		return subEle;
	}

	/**
	 * Is element existed or not? condition: if (each text in texts is
	 * contained in the container's text) then { we're going to determine whether
	 * the containerEle is existed in container or not}
	 * 
	 * @param container
	 * @param containerEle
	 * @param containerEleWithText
	 * @param eleText
	 * @return
	 */
	protected Boolean isElementExistedInContainerByText(List<String> texts, final Locator container,
			final Locator containerEle) {
		int timeOut = 10;
		List<WebElement> list;
		Locator locatorEle;
		String locatorPath = null;
		WebElement subEle = null;

		try {
			list = waitForElements(container);
			locatorEle = getLocator(containerEle.getElement());
			locatorPath = locatorEle.getElement();
			timeOut = locatorEle.getWaitSec();

			for (int i = 0; i < list.size(); i++) {
				final WebElement parent = list.get(i);
				boolean containerWithText = true;
				for (String s : texts) {
					if (!parent.getText().contains(s)) {
						containerWithText = false;
						break;
					}
				}
				if (containerWithText) {
					subEle = new WebDriverWait(driver, timeOut).until(new ExpectedCondition<WebElement>() {
						@Override
						public WebElement apply(WebDriver driver) {
							try {
								return parent.findElement(getBy(containerEle));
							} catch (NoSuchElementException e) {
								return null;
							}
						}
					});

					if (!subEle.equals(null)) {
						return true;
					}

					break;

				}
			}

		} catch (Exception e) {
			log.info("Try to get element: " + containerEle.getElement() + " with value " + locatorPath);
			return false;
		}
		
		return false;
	}

	/**
	 * get a set of elements from containers 
	 * condition: 
	 * if (each text in texts is contained in the container's text) 
	 * then { we're going to get a set of sub elements: containerEle from the containers }
	 * 
	 * @param container
	 * @param texts
	 * @return
	 */
	protected List<WebElement> getElementsFromContainersByText(List<String> texts, final Locator container,
			final Locator containerEle) {
		int timeOut = 10;
		List<WebElement> list;
		List<WebElement> listE = new ArrayList<WebElement>();
		WebElement element;
		String locatorPath = null;
		final Locator locatorEle;

		try {
			list = waitForElements(container);
			locatorEle = getLocator(containerEle.getElement());
			locatorPath = locatorEle.getElement();
			timeOut = locatorEle.getWaitSec();

			for (int i = 0; i < list.size(); i++) {
				final WebElement parent = list.get(i);
				boolean containerWithText = true;
				for (String s : texts) {
					if (!parent.getText().contains(s)) {
						containerWithText = false;
						break;
					}
				}
				
				if (containerWithText) {
					element = new WebDriverWait(driver, timeOut).until(new ExpectedCondition<WebElement>() {
						@Override
						public WebElement apply(WebDriver driver) {
							try {
								return parent.findElement(getBy(containerEle));
							} catch (NoSuchElementException e) {
								log.error("can't find the sub element: '" + containerEle.getElement() + "' with value " + locatorEle.getElement());
								return null;
							}
						}
					});
					
					listE.add(element);
				}
				
			}
		} catch (NullPointerException e) {
			log.info("Try to get element: " + container.getElement() + " with value " + locatorPath);
			log.error(e);
		}

		return listE;
	}

	/**
	 * get an element from container condition: if (the text of element
	 * containerELeWithText contains eleText) then { we're going to get the element
	 * containerEle from the container}
	 * 
	 * @param container
	 * @param containerEle
	 * @param containerEleWithText
	 * @param eleText
	 * @return web element or null
	 */
	protected WebElement getElementFromContainerByText(final Locator container, final Locator containerEle,
			final Locator containerEleWithText, String eleText) {
		int timeOut = 10;
		List<WebElement> list;
		final String locatorPath;
		Locator locatorEle = null;
		WebElement element = null;

		try {
			list = waitForElements(container);
			locatorEle = getLocator(containerEle.getElement());
			locatorPath = locatorEle.getElement();
			timeOut = locatorEle.getWaitSec();

			for (int i = 0; i < list.size(); i++) {
				final WebElement parent = list.get(i);
				if (parent.findElement(getBy(containerEleWithText)).getText().contains(eleText)) {

					element = new WebDriverWait(driver, timeOut).until(new ExpectedCondition<WebElement>() {
						@Override
						public WebElement apply(WebDriver driver) {
							try {
								return parent.findElement(getBy(containerEle));
							} catch (NoSuchElementException e) {
								log.error("can't find the sub element: '" + containerEle.getElement()
										+ "' with value " + locatorPath);
								return null;
							}
						}
					});
					
					break;
				}
			}
		} catch (NullPointerException e) {
			log.info("Try to get element: " + containerEle.getElement() + " with value " + locatorEle.getElement());
			log.error(e);
		}
		
		return element;
	}

	/**
	 * Is element existed or not? condition: if (the text of the element
	 * containerELeWithText contains eleText) then { we're going to determine
	 * whether the containerEle is existed in container or not}
	 * 
	 * @param container
	 * @param containerEle
	 * @param containerEleWithText
	 * @param eleText
	 * @return
	 */
	protected Boolean isElementExistedInContainerByText(final Locator container, final Locator containerEle,
			final Locator containerEleWithText, String eleText) {
		int timeOut = 10;
		List<WebElement> list;
		Locator locatorEle;
		String locatorPath = null;

		try {
			list = waitForElements(container);
			locatorEle = getLocator(containerEle.getElement());
			locatorPath = locatorEle.getElement();
			timeOut = locatorEle.getWaitSec();

			for (int i = 0; i < list.size(); i++) {
				final WebElement parent = list.get(i);
				if (parent.findElement(getBy(containerEleWithText)).getText().contains(eleText)) {

					WebElement element = new WebDriverWait(driver, timeOut).until(new ExpectedCondition<WebElement>() {
						@Override
						public WebElement apply(WebDriver driver) {
							try {
								return parent.findElement(getBy(containerEle));
							} catch (NoSuchElementException e) {
								return null;
							}
						}
					});

					if (element != null) {
						return true;
					}

					break;

				}
			}

		} catch (Exception e) {
			log.info("Try to get element: " + containerEle.getElement() + " with value " + locatorPath);
			return false;
		}

		return false;
		
	}

	
	/**
	 * Is container existed or not? condition: if (each text in texts is
	 * contained in the container's text) then { we're going to determine whether
	 * the container is existed or not }
	 * 
	 * @param container
	 * @param texts
	 * @return
	 */
	protected Boolean isContainerExistedByText(final Locator container, List<String> texts) {
		List<WebElement> list;
		boolean flag;
        
		try {
			list = waitForElements(container);
			for (int i = 0; i < list.size(); i++) {
				WebElement parent = list.get(i);
				flag = true;
				for (String s : texts) {
					if (!parent.getText().contains(s)) {
						flag = false;
						break;
					}
				}
				
				if (flag) return true;
			}

		} catch (Exception e) {
			return false;	
		}

		return false;
	}

	/**
	 * Is container existed or not? condition: if (the text of the element
	 * containerELeWithText contains eleText) then { we're going to determine
	 * whether the container is existed or not}
	 * 
	 * @param container
	 * @param containerEle
	 * @param containerEleWithText
	 * @param eleText
	 * @return
	 */
	protected Boolean isContainerExistedByText(final Locator container, final Locator containerEleWithText,
			String eleText) {
		int timeOut = 10;
		List<WebElement> list;
		Locator locatorEle;
		String locatorPath = null;
		WebElement element;

		try {
			list = waitForElements(container);
			locatorEle = getLocator(containerEleWithText.getElement());
			locatorPath = locatorEle.getElement();
			timeOut = locatorEle.getWaitSec();

			for (int i = 0; i < list.size(); i++) {
				final WebElement parent = list.get(i);
				if (parent.findElement(getBy(containerEleWithText)).getText().contains(eleText)) {

					element = new WebDriverWait(driver, timeOut).until(new ExpectedCondition<WebElement>() {
						@Override
						public WebElement apply(WebDriver driver) {
							try {
								return parent.findElement(getBy(containerEleWithText));
							} catch (NoSuchElementException e) {
								return null;
							}
						}
					});

					if (element != null) {
						return true;
					}

					break;
				}
			}

		} catch (Exception e) {
			log.info("Try to get element: " + containerEleWithText.getElement() + " with value " + locatorPath);
			return false;
		}
		
		return false;
	}

	protected Boolean isElementExisted(final Locator sourceLocator) {
		Boolean isExisted = false;
		try {
			Locator locator = getLocator(sourceLocator.getElement());
			int timeOut = locator.getWaitSec();
			WebDriverWait wait = new WebDriverWait(driver, timeOut);
			WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(getBy(sourceLocator)));
			if (element != null) {
				isExisted = true;
			}
		} catch (Exception e) {
			isExisted = false;
		}
		log.info("The element '" + sourceLocator.getElement()+ "' is existed: " + isExisted);
		return isExisted;

	}

	protected Locator getLocator(String locatorName) {
		Locator locator = null;
		try {
			locator = locatorMap.get(locatorName);
			if (locator.equals(null) || locator == null) {
				log.error(
						"The locator: '" + locatorName + "' has not been defined in the " + this.getClass() + ".xml.");
			}
		} catch (NullPointerException e) {
			log.error("No locator in your:" + this.getClass() + ".xml. Please set your locators first!");
		}
		return locator;
	}

	protected Boolean isElementDisplayed(Locator sourceLocator) {
		WebElement element;
		Boolean isDisplayed;
		String locatorPath;
		
		try {
			locatorPath = getLocator(sourceLocator.getElement()).getElement();
			log.info("wait for element: '" + sourceLocator.getElement() + "' with value " + locatorPath);

			element= (WebElement) waitForElement(sourceLocator, UI.canSee);
			isDisplayed = element.isDisplayed();
		} catch (Exception e) {
			isDisplayed = false;
		}
		log.info("The element '" + sourceLocator.getElement() +"' is displayed: " + isDisplayed);
		return isDisplayed;
	}

	protected Boolean isElementSelected(Locator sourceLocator) {
		
		Boolean isSelected = (Boolean) waitForElement(sourceLocator, UI.isSelected);
		if (isSelected == null) {
			return false;
		} else {
			return isSelected;
		}
		
		
	}

	protected Boolean isElementEnabled(Locator sourceLocator) {
	    Boolean isEnabled = true;
		WebElement element = (WebElement)waitForElement(sourceLocator, UI.canSee);
		try {
		    isEnabled = element.isEnabled();
		} catch (Exception e) {
			log.error(e);
		}
		return isEnabled;
	}

	protected Boolean isTextPresent(String text) {
		Boolean isPresent = true;
		WebElement element = null;
		long startTime = System.currentTimeMillis();
		long endTime = startTime = 5 * 1000;

		try {
			log.info("wait body element to be present.");
			do {
				Thread.sleep(200);
				startTime = startTime + 200;
				element = driver.findElement(By.tagName("body"));
				if (element != null) break;
			}while(startTime < endTime );
			
			if (element != null) {
				isPresent = element.getText().contains(text);
			} else {
				log.error("The body element is not present");
			}			
		} catch (NullPointerException e) {
			log.error(e);
		} catch (InterruptedException | NoSuchElementException e) {
			// nothing
		} 

		return isPresent;
	}
		

	protected String getText(Locator locator) {
		WebElement element = (WebElement)waitForElement(locator, UI.canSee);
		try {
		    log.info("the text of element is '" + element.getText().trim() + "'.");
		} catch (Exception e) {
			log.error("Get the text of the " + locator.getElement() + " failed.", e);
		}
		return element.getText().trim();
	}

	protected void clearText(Locator locator) {
		WebElement element = (WebElement)waitForElement(locator, UI.canSee);
		try {
		    element.clear();
		} catch (Exception e) {
			log.error("Clear failed.", e);
		}
	}

	protected String getEleAttribute(Locator locator, String attribute) {
		WebElement element = (WebElement)waitForElement(locator, UI.canSee);
		try {
			log.info("The attribute is " + element.getAttribute(attribute));
		} catch (Exception e) {
			log.error(e);
		}
		return  element.getAttribute(attribute);
		
	}

	protected String getEleAttribute(WebElement element, String attribute) {
		try {
			log.info("The attribute is " + element.getAttribute(attribute));
		} catch (Exception e) {
			log.error(e);
		}
		return element.getAttribute(attribute);
	}

	protected void alertConfirm() {
		Alert alert = driver.switchTo().alert();
		try {
			alert.accept();
		} catch (Exception notFindAlert) {
			log.error("Alert did not find.");
		}
	}

	protected void alertDismiss() {
		Alert alert = driver.switchTo().alert();
		try {
			alert.dismiss();
		} catch (Exception notFindAlert) {
			log.error("Alert did not find.");
		}
	}

	protected String getAlertText() {
		Alert alert = driver.switchTo().alert();
		try {
			return alert.getText().trim();
		} catch (Exception notFindAlert) {
			log.error("Alert did not find.");
			return null;
		}
	}

	protected void scrollToElement(Locator locator) {
		WebElement element = (WebElement) this.waitForElement(locator, UI.canSee);
		scrollToElement(element);
	}
	
	protected void scrollToElement(WebElement element) {
		try {
			log.info("scroll view element");
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].scrollIntoView();", element);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	protected void scrollToTop() {
		try {
			log.info("scroll to the top of the page");
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollTo(0,0)");
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	protected void scrollToBottom() {
		try {
			log.info("scroll to the bottom of the page");
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollTo(0,document.body.scrollHeight");
		} catch (Exception e) {
			log.error(e);
		}
	}

	protected String title() {
		return driver.getTitle();
	}

	protected WebElement getListOptionRandom(Locator sourceLocator) {
		List<WebElement> list = waitForElements(sourceLocator);
		Random ran = new Random(System.currentTimeMillis());
		int num = 100;
		try {
			num = (int) (ran.nextInt(list.size()));
			log.info("random num is " + num);
			for (int i = 0; i < list.size(); i++) {
				log.info("text " + i + ": '" + list.get(i).getText() +"'");
			}
		} catch (Exception e) {
			log.error(e);
		}
		
		if (list.size() > num) {
			log.info("The random text is '" + list.get(num).getText() +"'");
		} else {
			log.error("The random number is bigger than or equals to the count of the list: " + sourceLocator.getElement());
		}
		return list.get(num);
	}

	/**
	 * click one option from list at random, and return the option's text. 
	 * if (the random option's text != each of excluedTexts) 
	 * then {we're going to click the option and return its text}
	 * 
	 * @param excludedText
	 * @param sourceLocator
	 * @return
	 */
	protected String selectListOptionRandom(List<String> excludedTexts, Locator sourceLocator) {
		String randomText;
		WebElement element;
		int times = 0;
		{
			++times;
			element = getListOptionRandom(sourceLocator);
			randomText = element.getText().trim();
			
		}while (excludedTexts.contains(randomText) | times < 5);

		log.info("click the element '" +sourceLocator.getElement()+ "' after " + times + " times.");
		element.click();
		return randomText;
	}

	/**
	 * click one option from list at random, and return the option's text. 
	 * if (the random option's text != excludedText) 
	 * then {we're going to click the option and return its text}
	 * 
	 * @param excludedText
	 * @param sourceLocator
	 * @return
	 */
	protected String selectListOptionRandom(String excludedText, Locator sourceLocator) {
		String randomText;
		WebElement element;
		int times = 0;
		{
			++times;
			element = getListOptionRandom(sourceLocator);
			randomText = element.getText().trim();
		}while (excludedText.equals(randomText) | times < 5);

		log.info("click the element '" +sourceLocator.getElement()+ "' after " + times + " times.");
		element.click();
		return randomText;
	}
	
	protected String selectListOptionRandom(Locator sourceLocator) {
		String randomText;
		WebElement element;
		
		element = getListOptionRandom(sourceLocator);
		randomText = element.getText().trim();
		
		log.info("click the element '" +sourceLocator.getElement()+ "'");
		element.click();
		return randomText;
	}
	
	protected void switchToFrame(Locator sourceLocator) {
		try {
		    driver.switchTo().frame((WebElement)waitForElement(sourceLocator, UI.presence));
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	protected WebElement waitEleBySleep(Locator sourceLocator) {
		
		long startTime = System.currentTimeMillis();
		long endTime;
		String locatorPath = null;
		Locator locator;
		WebElement element = null;

		try {
			locator = getLocator(sourceLocator.getElement());
			locatorPath = locator.getElement();
			endTime = startTime + locator.getWaitSec() * 1000;
			log.info("wait for element: '" + sourceLocator.getElement() + "' with value " + locatorPath);
			do {
				Thread.sleep(200);
				startTime = startTime + 200;
				element = driver.findElement(getBy(sourceLocator));
				if (element != null) break;
			}while(startTime < endTime );
			
		} catch (NullPointerException e) {
			log.error(e);
		} catch (InterruptedException | NoSuchElementException e) {
		}

		return element;
	}
	protected void moveMouseToElement(WebElement sourceLocator){
		try{
		Actions action=new Actions(driver);
		action.moveToElement(sourceLocator).perform();
		}catch (Exception e) {
			log.error(e);
		}
	}
	
	protected void mouseOverToElement(Locator sourceLocator) {
		WebElement element = (WebElement)waitForElement(sourceLocator, UI.canSee);
		mouseOverToElement(element);
	}
	
	protected void mouseOverToElement(WebElement element) {
		Actions action = new Actions(driver);
		try {
		    action.moveToElement(element).perform();
		} catch (Exception e) {
			log.error("Mouse over error.", e);
		}
	}
	
	protected void mouseClickElement(Locator sourceLocator){
		WebElement element = (WebElement)waitForElement(sourceLocator, UI.canSee);
		mouseClickElement(element);
	}
	
	protected void mouseClickElement(WebElement element){
		try{
			Actions action=new Actions(driver);
			action.moveToElement(element).click().build().perform();
			}catch (Exception e) {
				log.error("Mouse click error.", e);
			}
	}
}
