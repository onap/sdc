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

package org.openecomp.sdc.ci.tests.execute.setup;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
//import org.openqa.selenium.firefox.FirefoxOptions; // Selenium 3.4.0 change
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.CaptureType;

public class WebDriverThread {

	public static final String AUTOMATION_DOWNLOAD_DIR = "automationDownloadDir";
	private WebDriver webdriver;
	private FirefoxProfile firefoxProfile;
	public static final String SELENIUM_NODE_URL = "http://%s:%s/wd/hub";
	
	public WebDriverThread(Config config) {
		initDriver(config);
		webdriver.manage().window().maximize();
	}
	
	public WebDriver getDriver() throws Exception {
		return webdriver;
	}
	
	public void quitDriver() {
		if (webdriver != null) {
			webdriver.quit();
			webdriver = null;
		}
	}
	
	
	public void initDriver(Config config){
		try {
			boolean remoteTesting = config.isRemoteTesting();
			if (!remoteTesting) {
				boolean mobProxyStatus = config.getUseBrowserMobProxy();
				if (mobProxyStatus){
					setWebDriverWithMobProxy();
				} else {
					
                    // Selenium 3.4.0 change, add firefox options : set, add firefox 5.X location and enable 
//					FirefoxOptions options = new FirefoxOptions();
//					options.setBinary("C:\\Program Files (x86)\\Mozilla Firefox5\\firefox.exe"); //Location where Firefox is installed
//					options.addPreference("dom.file.createInChild", true); // Enable file upload with sendKeys
					// End of Selenium 3.4.0 change 
					
					System.out.println("Opening LOCAL browser");
					DesiredCapabilities cap = new DesiredCapabilities();
									
					cap = DesiredCapabilities.firefox();
					cap.setBrowserName("firefox");
					cap.setCapability(FirefoxDriver.PROFILE, initFirefoxProfile());
					
//					cap.setCapability("moz:firefoxOptions", options); //Add options to Capabilities, Selenium 3.4.0 change
					
			        firefoxProfile.setPreference("network.proxy.type", 2);
					firefoxProfile.setPreference("network.proxy.autoconfig_url", "http://emea-auto.proxy.att.com:8001/");
					firefoxProfile.setPreference("network.proxy.no_proxies_on", "localhost");
					
					webdriver = new FirefoxDriver(cap);
				}								
			} else {
				System.out.println("Opening REMOTE browser");
				String remoteEnvIP = config.getRemoteTestingMachineIP();
				String remoteEnvPort = config.getRemoteTestingMachinePort();
				
				DesiredCapabilities cap = new DesiredCapabilities();
				cap = DesiredCapabilities.firefox();
				cap.setPlatform(Platform.ANY);
				cap.setBrowserName("firefox");
				
				String remoteNodeUrl = String.format(SELENIUM_NODE_URL, remoteEnvIP, remoteEnvPort);
				RemoteWebDriver remoteWebDriver = new RemoteWebDriver(new URL(remoteNodeUrl), cap);
				remoteWebDriver.setFileDetector(new LocalFileDetector());
				webdriver = remoteWebDriver;
			}
			

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private FirefoxProfile initFirefoxProfile() {
		firefoxProfile = new FirefoxProfile();
		firefoxProfile.setPreference("browser.download.folderList",2);
		firefoxProfile.setPreference("browser.download.manager.showWhenStarting",false);
		firefoxProfile.setPreference("browser.download.dir", getDownloadDirectory());
		firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk","application/octet-stream, application/xml, text/plain, text/xml, image/jpeg");
		return firefoxProfile;
	}

	private String getDownloadDirectory() {
		String downloadDirectory = FileHandling.getBasePath() + File.separator + AUTOMATION_DOWNLOAD_DIR + UUID.randomUUID().toString().split("-")[0] + File.separator;
		File dir = new File(downloadDirectory);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir.getAbsolutePath();
	}

	public FirefoxProfile getFirefoxProfile() {
		return firefoxProfile;
	}
	
	private void setWebDriverWithMobProxy(){
		WebDriver driver = null;
		MobProxy.setProxyServer();
		BrowserMobProxyServer proxyServer = MobProxy.getPoxyServer();
		
		firefoxProfile = new FirefoxProfile();
		firefoxProfile.setPreference("browser.download.folderList",2);
		firefoxProfile.setPreference("browser.download.manager.showWhenStarting",false);
		firefoxProfile.setPreference("browser.download.dir", getDownloadDirectory());
		firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk","application/octet-stream, application/xml, text/plain, text/xml, image/jpeg");
		firefoxProfile.setAcceptUntrustedCertificates(true);
		firefoxProfile.setAssumeUntrustedCertificateIssuer(true);
//		firefoxProfile.setPreference("network.proxy.http", "localhost");
//		firefoxProfile.setPreference("network.proxy.http_port", proxyServer.getPort());
//		firefoxProfile.setPreference("network.proxy.ssl", "localhost");
//		firefoxProfile.setPreference("network.proxy.ssl_port", proxyServer.getPort());
//		firefoxProfile.setPreference("network.proxy.type", 1);
//		firefoxProfile.setPreference("network.proxy.no_proxies_on", "");

        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        capabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
        capabilities.setCapability(CapabilityType.PROXY, ClientUtil.createSeleniumProxy(proxyServer));
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

        webdriver = new FirefoxDriver(capabilities);
		proxyServer.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT, CaptureType.REQUEST_COOKIES, CaptureType.REQUEST_BINARY_CONTENT,
				                          CaptureType.REQUEST_HEADERS, CaptureType.RESPONSE_COOKIES, CaptureType.RESPONSE_HEADERS, CaptureType.RESPONSE_BINARY_CONTENT);
	}

}
