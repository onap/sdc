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
import org.onap.sdc.backend.ci.tests.utils.Utils;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DriverFactory {

    private static ThreadLocal<WebDriverThread> driverThread;
    private static final List<WebDriverThread> webDriverThreadPool = Collections.synchronizedList(new ArrayList<>());
    private Config config;

    public DriverFactory() {
        try {
            config = Utils.getConfig();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @BeforeSuite(alwaysRun = true)
    public void instantiateDriverObject() {

        File basePath = new File(FileHandling.getBasePath());
        File[] listFiles = basePath.listFiles((basePath1, name) -> name.startsWith(config.getDownloadAutomationFolder()));
        assert listFiles != null;
        Arrays.asList(listFiles).forEach(e -> FileHandling.deleteDirectory(e.getAbsolutePath()));

        driverThread = ThreadLocal.withInitial(() -> {
            WebDriverThread webDriverThread = new WebDriverThread(config);
            webDriverThreadPool.add(webDriverThread);
            return webDriverThread;
        });
    }

    public WebDriver getDriver() {
        return driverThread.get().getDriver();
    }

    @AfterSuite(alwaysRun = true)
    public void quitDriverAfterSuite() throws Exception {
        for (WebDriverThread webDriverThread : webDriverThreadPool) {
            if (webDriverThread.getDriver() != null) {
                webDriverThread.quitDriver();
            }
        }
        MobProxy.removeAllProxyServers();
        cleanDownloadDirs();
    }

    private void cleanDownloadDirs() throws IOException {
        HashMap<Long, WindowTest> windowMap = WindowTestManager.getWholeMap();
        for (WindowTest win : windowMap.values()) {
            String downloadDirectory = win.getDownloadDirectory();
            FileUtils.cleanDirectory(new File(downloadDirectory));
        }
    }

    public void quitDriver() throws Exception {
        driverThread.get().quitDriver();
        driverThread.remove();
        WindowTestManager.removeWindowTest();
        MobProxy.removePoxyServer();
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }


}
