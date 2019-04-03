package com.imooc.selenium.business;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.imooc.selenium.page.PageBase;
import com.imooc.selenium.util.Driver;

public class Login  {

	protected Login(WebDriver driver) {
	
		      Driver d = new Driver();
	         driver=  d.getDriver("chorome");
	}
	 
  @Test
	public void test1(){
		System.out.println("This is mock demo");
	}
}
