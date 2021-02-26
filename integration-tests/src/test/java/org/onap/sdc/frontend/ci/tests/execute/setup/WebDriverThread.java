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

package org.onap.sdc.frontend.ci.tests.execute.setup;

import java.net.MalformedURLException;
import java.net.URL;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.frontend.ci.tests.exception.WebDriverThreadRuntimeException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDriverThread {

    private final static Logger LOGGER = LoggerFactory.getLogger(SetupCDTest.class);

    private WebDriver webdriver;
    private FirefoxProfile firefoxProfile;
    private static final String SELENIUM_NODE_URL = "http://%s:%s/wd/hub";

    WebDriverThread(Config config) {
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
            final String remoteEnvIP = config.getRemoteTestingMachineIP();
            final String remoteEnvPort = config.getRemoteTestingMachinePort();
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.setProfile(initFirefoxProfile(config));
            firefoxOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            final String remoteUrlString = String.format(SELENIUM_NODE_URL, remoteEnvIP, remoteEnvPort);
            final URL remoteUrl;
            try {
                remoteUrl = new URL(remoteUrlString);
            } catch (MalformedURLException e) {
                throw new WebDriverThreadRuntimeException(String.format("Malformed URL '%s'", remoteUrlString), e);
            }
            final RemoteWebDriver remoteWebDriver = new RemoteWebDriver(remoteUrl, firefoxOptions);
            remoteWebDriver.setFileDetector(new LocalFileDetector());
            remoteWebDriver.manage().window().setSize(new Dimension(1920,1440));
            webdriver = remoteWebDriver;

        } else {
            LOGGER.info("Opening LOCAL browser");
            System.setProperty("webdriver.gecko.driver", "target/gecko/geckodriver");
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            final FirefoxProfile firefoxProfile = initFirefoxProfile(config);
            firefoxProfile.setPreference("browser.download.dir", config.getDownloadAutomationFolder());
            firefoxProfile.setPreference("browser.download.folderList", 2);
            firefoxOptions.setProfile(firefoxProfile);
            firefoxOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            firefoxOptions.setHeadless(false);
            webdriver = new FirefoxDriver(firefoxOptions);
            webdriver.manage().window().maximize();
        }
    }

    private FirefoxProfile initFirefoxProfile(Config config) {
        firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("browser.download.folderList", 0);
        //firefoxProfile.setPreference("browser.alwaysOpenInSystemViewerContextMenuItem", false);
        //firefoxProfile.setPreference("browser.download.useDownloadDir", false);
        //firefoxProfile.setPreference("browser.download.downloadDir", config.getContainerDownloadAutomationFolder());
        //firefoxProfile.setPreference("browser.download.dir", config.getContainerDownloadAutomationFolder());
        //firefoxProfile.setPreference("app.update.notifyDuringDownload", false);
        //firefoxProfile.setPreference("browser.download.lastDir", config.getContainerDownloadAutomationFolder());
        firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream, application/xml, text/plain, text/xml, image/jpeg");
        firefoxProfile.setPreference("network.proxy.type", 4);
        firefoxProfile.setAcceptUntrustedCertificates(true);
        firefoxProfile.setAssumeUntrustedCertificateIssuer(true);

        return firefoxProfile;
    }

    FirefoxProfile getFirefoxProfile() {
        return firefoxProfile;
    }
}
