/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.ci.tests.US;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.ProxySearch.Strategy;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
 
public class MobProxy {
	public static WebDriver driver;
	public static BrowserMobProxyServer server;
 
	@BeforeClass
	public void setup() throws Exception {
		
		ProxySearch proxySearch = new ProxySearch();
		proxySearch.addStrategy(Strategy.OS_DEFAULT); 
		proxySearch.addStrategy(Strategy.JAVA); 
		proxySearch.addStrategy(Strategy.BROWSER); 
		ProxySelector proxySelector = proxySearch.getProxySelector(); 

		ProxySelector.setDefault(proxySelector); 
		URI home = URI.create("http://www.google.com"); 
		System.out.println("ProxySelector: " + proxySelector); 
		System.out.println("URI: " + home); 
		List<Proxy> proxyList = proxySelector.select(home); 
		String host = null;
		String port = null;
		if (proxyList != null && !proxyList.isEmpty()) { 
		 for (Proxy proxy : proxyList) { 
		   System.out.println(proxy); 
		   SocketAddress address = proxy.address(); 
		   if (address instanceof InetSocketAddress) { 
		     host = ((InetSocketAddress) address).getHostName(); 
		     port = Integer.toString(((InetSocketAddress) address).getPort()); 
		     System.setProperty("http.proxyHost", host); 
		     System.setProperty("http.proxyPort", port); 
		   } 
		 } 
		}
		
		server = new BrowserMobProxyServer();
		InetSocketAddress address = new InetSocketAddress(host, Integer.parseInt(port));
	    server.setChainedProxy(address);
		server.start();
		int port1 = server.getPort();
		DesiredCapabilities seleniumCapabilities = new DesiredCapabilities();
		seleniumCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		seleniumCapabilities.setCapability(CapabilityType.PROXY, ClientUtil.createSeleniumProxy(server));
		driver = new FirefoxDriver(seleniumCapabilities);
		server.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
		System.out.println("Port started:" + port1);
	}
 
	@Test
	public void first_test1() throws InterruptedException {
 
		server.newHar("asdc.har");
 
		driver.get("https://www.e-access.att.com/QA-SCRUM1/sdc1/portal#/dashboard");
		driver.manage().window().maximize();
		
		WebElement userNameTextbox = driver.findElement(By.name("userid"));
		userNameTextbox.sendKeys("m99121");
		WebElement passwordTextbox = driver.findElement(By.name("password"));
		passwordTextbox.sendKeys("66-Percent");
		
		WebElement submitButton = driver.findElement(By.name("btnSubmit"));
		submitButton.click();
		Thread.sleep(300);
		WebElement buttonOK = driver.findElement(By.name("successOK"));
		AssertJUnit.assertTrue(buttonOK.isDisplayed());
		buttonOK.click();
		Thread.sleep(2000);
		driver.findElement(By.xpath(getXpath("main-menu-button-catalog"))).click();
		Thread.sleep(2000);		
		driver.findElement(By.xpath(getXpath("checkbox-service"))).click();
		Thread.sleep(2000);
	}
	
	public static String getXpath(String dataTestId){
		return String.format("//*[@data-tests-id='%s']", dataTestId);
	}
 
	@AfterClass
	public void shutdown() {
		try {
	        
			// Get the HAR data
			Har har = server.getHar();
			File harFile = new File("C:\\temp\\asdc.har");
			har.writeTo(harFile);
 
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		driver.quit();
		server.stop();
	}
}
