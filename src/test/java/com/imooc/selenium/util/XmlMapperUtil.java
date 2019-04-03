package com.imooc.selenium.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.imooc.selenium.util.Locator.ByType;



/**
 * @author yuanfang1.qin
 *
 */
public class XmlMapperUtil {

	public static HashMap<String, Locator> readXMLDocument(String path, String pageName) throws Exception {

		Log log = new Log(XmlMapperUtil.class);
		log.info("page name is " + pageName);
		HashMap<String, Locator> locatorMap = new HashMap<String, Locator>();
		locatorMap.clear();
		File file = new File(path);
		if (!file.exists()) {
			log.error("Can't find xml file: " + path);
			return locatorMap = null;
		}
		SAXReader reader = new SAXReader();
		Document document = reader.read(file);
		Element root = document.getRootElement();
		// pages
		for (Iterator<?> i = root.elementIterator(); i.hasNext();) {
			Element page = (Element) i.next();
			if (page.attribute(0).getValue().equalsIgnoreCase(pageName)) {
				log.info("page Info is:" + pageName);
				// locator or container
				for (Iterator<?> lc = page.elementIterator(); lc.hasNext();) {
					Element pageEle = (Element) lc.next();
					Boolean isContainer = pageEle.getName().trim().equalsIgnoreCase("container");
					if (isContainer) {
						// handle container and locators that under the container
						Locator container = getLocator(pageEle, isContainer);
						locatorMap.put(container.getName(), container);
						for (Iterator<?> containerEle = pageEle.elementIterator(); containerEle.hasNext();) {
							Element locator = (Element) containerEle.next();
							locatorMap.put(locator.getText().trim(), getLocator(locator, isContainer));
							continue;

						}
					} else {
						// handle locators that under the Page tag
						for (Iterator<?> l = page.elementIterator(); l.hasNext();) {
							Element locator = (Element) l.next();
							locatorMap.put(locator.getText().trim(), getLocator(locator, isContainer));
							continue;
						}
					}

					continue;
				}

				continue;
			} else {
				log.error("Can not find the pageName: " + pageName + " in XMLfile: " + path);
			}

		}
		return locatorMap;

	}

	private static Locator getLocator(Element element, Boolean isContainer) {
		String type = null;
		String timeOut = "5";
		String value = null;
		String name = null;

		for (Iterator<?> j = element.attributeIterator(); j.hasNext();) {
			Attribute attribute = (Attribute) j.next();
			switch (attribute.getName()) {
			case "type":
				type = attribute.getValue().trim();
				break;
			case "timeOut":
				timeOut = attribute.getValue().trim();
				break;
			case "name":
				name = attribute.getValue().trim();
				break;
			default:
				value = attribute.getValue().trim();
				break;
			}
		}

		Locator temp = new Locator(value, Integer.parseInt(timeOut), getByType(type), name, isContainer);

		return temp;

	}

	public static ByType getByType(String type) {
		ByType byType;
		switch (type.toLowerCase()) {
		case "cssselector":
			byType = ByType.cssSelector;
			break;
		case "id":
			byType = ByType.id;
			break;
		case "name":
			byType = ByType.name;
			break;
		case "partiallinktext":
			byType = ByType.partialLinkText;
			break;
		case "linktext":
			byType = ByType.linkText;
			break;
		case "classname":
			byType = ByType.className;
			break;
		case "xpath":
			byType = ByType.xpath;
			break;
		case "tagname":
			byType = ByType.tagName;
			break;
		default:
			byType = ByType.cssSelector;
			break;
		}
		return byType;
	}

}
