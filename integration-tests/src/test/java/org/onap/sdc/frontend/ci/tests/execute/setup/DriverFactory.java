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


import org.apache.commons.io.FileUtils;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.backend.ci.tests.utils.Utils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class DriverFactory {

    private static ThreadLocal<WebDriverThread> driverThread;
    private static List<WebDriverThread> webDriverThreadPool = Collections.synchronizedList(new ArrayList<WebDriverThread>());
    private static Config config;

    public DriverFactory() {
        try {
            config = Utils.getConfig();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @BeforeSuite(alwaysRun = true)
    public static void instantiateDriverObject() {

        File basePath = new File(FileHandling.getBasePath());
        File[] listFiles = basePath.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File basePath, String name) {
                return name.startsWith(config.getDownloadAutomationFolder());
            }
        });
        //Arrays.asList(listFiles).forEach(e -> FileHandling.deleteDirectory(e.getAbsolutePath()));


        driverThread = new ThreadLocal<WebDriverThread>() {
            @Override
            protected WebDriverThread initialValue() {
                WebDriverThread webDriverThread = new WebDriverThread(config);
                webDriverThreadPool.add(webDriverThread);
                return webDriverThread;
            }
        };
    }

    public static WebDriver getDriver() {
        return driverThread.get().getDriver();
    }

    public static FirefoxProfile getDriverFirefoxProfile() throws Exception {
        return driverThread.get().getFirefoxProfile();
    }

    @AfterSuite(alwaysRun = true)
    public static void quitDriverAfterSuite() throws Exception {
        for (WebDriverThread webDriverThread : webDriverThreadPool) {
            if (webDriverThread.getDriver() != null) {
                webDriverThread.quitDriver();
            }
        }
        MobProxy.removeAllProxyServers();
        cleanDownloadDirs();
    }

    private static void cleanDownloadDirs() throws IOException {
        HashMap<Long, WindowTest> windowMap = WindowTestManager.getWholeMap();
        for (WindowTest win : windowMap.values()) {
            String downloadDirectory = win.getDownloadDirectory();
            FileUtils.cleanDirectory(new File(downloadDirectory));
        }
    }

    public static void quitDriver() throws Exception {
        driverThread.get().quitDriver();
        driverThread.remove();
        WindowTestManager.removeWindowTest();
        MobProxy.removePoxyServer();
    }

    public static Config getConfig() {
        return config;
    }

    public static void setConfig(Config config) {
        DriverFactory.config = config;
    }


}
