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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.exception.WebDriverThreadRuntimeException;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openqa.selenium.Platform;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDriverThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverThread.class);
    private static final String SELENIUM_NODE_URL = "http://%s:%s/wd/hub";
    static final String AUTOMATION_DOWNLOAD_DIR = "automationDownloadDir";
    private final Config config;
    private WebDriver webdriver;
    private FirefoxProfile firefoxProfile;

    WebDriverThread(final Config config) {
        this.config = config;
        initDriver(config);
        webdriver.manage().window().maximize();
    }

    public WebDriver getDriver() {
        return webdriver;
    }

    void quitDriver() {
        if (webdriver != null) {
            webdriver.quit();
            webdriver = null;
        }
    }

    private void initDriver(final Config config) {
        if (config.isRemoteTesting()) {
            LOGGER.info("Opening REMOTE browser");
            initRemoteDriver(config);
            return;
        }

        LOGGER.info("Opening LOCAL browser");
        initLocalDriver();
    }

    private void initLocalDriver() {
        config.loadGeckoDriverLocation();
        initFirefoxProfile();
        final FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setProfile(firefoxProfile);
        firefoxOptions.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
        firefoxOptions.setCapability(CapabilityType.BROWSER_NAME, "firefox");

        webdriver = new FirefoxDriver(firefoxOptions);
    }

    private void initRemoteDriver(final Config config) {
        final String remoteEnvIP = config.getRemoteTestingMachineIP();
        final String remoteEnvPort = config.getRemoteTestingMachinePort();

        final DesiredCapabilities cap = DesiredCapabilities.firefox();
        cap.setPlatform(Platform.ANY);
        cap.setBrowserName("firefox");

        final String remoteUrlString = String.format(SELENIUM_NODE_URL, remoteEnvIP, remoteEnvPort);
        final URL remoteUrl;
        try {
            remoteUrl = new URL(remoteUrlString);
        } catch (MalformedURLException e) {
            throw new WebDriverThreadRuntimeException(String.format("Malformed URL '%s'", remoteUrlString), e);
        }
        final RemoteWebDriver remoteWebDriver = new RemoteWebDriver(remoteUrl, cap);
        remoteWebDriver.setFileDetector(new LocalFileDetector());
        webdriver = remoteWebDriver;
    }

    private void initFirefoxProfile() {
        firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("browser.download.folderList", 2);
        firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
        firefoxProfile.setPreference("browser.download.dir", getDownloadDirectory());
        firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream, application/xml, text/plain, text/xml, image/jpeg");
        firefoxProfile.setPreference("network.proxy.type", 2);
        firefoxProfile.setPreference("network.proxy.autoconfig_url", "http://autoproxy.sbc.com/autoproxy.cgi");
        firefoxProfile.setPreference("network.proxy.no_proxies_on", "localhost");
    }

    private String getDownloadDirectory() {
        String downloadDirectory = FileHandling.getBasePath() + File.separator + AUTOMATION_DOWNLOAD_DIR + UUID.randomUUID().toString().split("-")[0] + File.separator;
        File dir = new File(downloadDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    FirefoxProfile getFirefoxProfile() {
        return firefoxProfile;
    }

}
