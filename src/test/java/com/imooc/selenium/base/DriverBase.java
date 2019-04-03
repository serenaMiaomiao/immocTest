package com.imooc.selenium.base;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.imooc.selenium.util.Driver;






public class DriverBase {
    public WebDriver driver;
     public DriverBase(String browser){
    	  Driver d = new Driver();
    	  this.driver = d.getDriver(browser);
     }
    
     public void closeDriver(){
    	 driver.close();
     }
     /*查找Element*/
     public WebElement getElement(By by){
    	 WebElement element = driver.findElement(by);
    	 return element;
     }
     /*
      * 封装点击方法
      * */
     public void click(WebElement element){
    	if(element!=null){
    		element.click();
    	}else{
    		System.out.println("element is null");
    	}
     }
}
