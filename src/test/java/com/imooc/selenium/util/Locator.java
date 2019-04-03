package com.imooc.selenium.util;

/**
 * @author yuanfang1.qin
 *
 */
public class Locator {

	private String element;
	private ByType byType = ByType.cssSelector;
	private int waitSec = 5;
	private Boolean isContainer = false;
	private String name;

	public enum ByType {
		xpath, id, linkText, name, className, cssSelector, partialLinkText, tagName
	}

	public Locator(String element) {
		this.element = element;
	}

	public Locator(String element, int waitSec) {
		this.waitSec = waitSec;
		this.element = element;
	}
	
	public Locator(String element, int waitSec, ByType byType) {
		this.waitSec = waitSec;
		this.element = element;
		this.byType = byType;
	}

	public Locator(String element, int waitSec, ByType byType, String name,Boolean isContainer) {
		this.waitSec = waitSec;
		this.element = element;
		this.byType = byType;
		this.name = name;
		this.isContainer = isContainer;
	}

	public String getElement() {
		return element;
	}

	public int getWaitSec() {
		return waitSec;
	}

	public ByType getBy() {
		return byType;
	}
	
	public String getName() {
		return name;
	}
	
	public void setBy(ByType byType) {
		this.byType = byType;
	}
	
	public Boolean getIsContainer() {
		return isContainer;
	}

	public void setReplace(String rep, String rex) {
		StringUtil.replaceAll(element, rex, rep);
	}
}
