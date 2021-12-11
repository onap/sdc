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

package org.onap.sdc.backend.ci.tests.api;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;
import java.io.File;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.utils.Utils;
import org.onap.sdc.backend.ci.tests.utils.general.FileHandling;
import org.onap.sdc.backend.ci.tests.utils.rest.AutomationUtils;
import org.testng.ITestContext;

public class ExtentManager {

    private static final String VERSIONS_INFO_FILE_NAME = "versions.info";
    private static ExtentReports extent;
    private static ExtentHtmlReporter htmlReporter;

    private static synchronized ExtentReports setReporter(String filePath, String htmlFile, Boolean isAppend) throws Exception {
        if (extent == null) {
            extent = new ExtentReports();
            initAndSetExtentHtmlReporter(filePath, htmlFile, isAppend);
        }
        return extent;
    }

    private static synchronized void initAndSetExtentHtmlReporter(String filePath, String htmlFile, Boolean isAppend) throws Exception {
        htmlReporter = new ExtentHtmlReporter(filePath + htmlFile);
        setConfiguration(htmlReporter);
        htmlReporter.setAppendExisting(isAppend);
        extent.attachReporter(htmlReporter);
    }

    public static synchronized ExtentReports getReporter() {
        return extent;
    }

    public static void initReporter(String filepath, String htmlFile, ITestContext context) throws Exception {

        String onboardVersion = AutomationUtils.getOnboardVersion();
        String osVersion = AutomationUtils.getOSVersion();
        Config config = Utils.getConfig();
        String envData = config.getUrl();
        String suiteName = getSuiteName(context);

        if (suiteName.equals(suiteNameXml.TESTNG_FAILED_XML_NAME.getValue())) {
            if (config.isUseBrowserMobProxy()) {
                setTrafficCaptue(config);
            }

            setReporter(filepath, htmlFile, true);
            String suiteNameFromVersionInfoFile = FileHandling.getKeyByValueFromPropertyFormatFile(filepath + VERSIONS_INFO_FILE_NAME, "suiteName");
            reporterDataDefinition(onboardVersion, osVersion, envData, suiteNameFromVersionInfoFile);
        } else {
            FileHandling.deleteDirectory(ComponentBaseTest.getReportFolder());
            FileHandling.createDirectory(filepath);
            setReporter(filepath, htmlFile, false);
            reporterDataDefinition(onboardVersion, osVersion, envData, suiteName);
            AutomationUtils.createVersionsInfoFile(filepath + VERSIONS_INFO_FILE_NAME, onboardVersion, osVersion, envData, suiteName);
        }

    }

    private static void reporterDataDefinition(String onboardVersion, String osVersion, String envData, String suiteNameFromVersionInfoFile)
        throws Exception {
        extent.setSystemInfo("Onboard Version", onboardVersion);
        extent.setSystemInfo("OS Version", osVersion);
        extent.setSystemInfo("ExecutedOn", envData);
        extent.setSystemInfo("SuiteName", suiteNameFromVersionInfoFile);
    }

    private static String getSuiteName(ITestContext context) {
        String suitePath = context.getSuite().getXmlSuite().getFileName();
        if (suitePath != null) {
            File file = new File(suitePath);
            String suiteName = file.getName();
            return suiteName;
        }
        return null;
    }

    private static synchronized ExtentHtmlReporter setConfiguration(ExtentHtmlReporter htmlReporter) throws Exception {

        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setEncoding("UTF-8");
        htmlReporter.config().setProtocol(Protocol.HTTPS);
        htmlReporter.config().setDocumentTitle("SDC Automation Report");
        htmlReporter.config().setChartVisibilityOnOpen(true);
        htmlReporter.config().setReportName("SDC Automation Report");
        htmlReporter.config().setChartVisibilityOnOpen(false);
        return htmlReporter;
    }

    private static void setTrafficCaptue(Config config) {
        boolean mobProxyStatus = config.isUseBrowserMobProxy();
        if (mobProxyStatus) {
            config.setCaptureTraffic(true);
        }
    }

    enum suiteNameXml {

        TESTNG_FAILED_XML_NAME("testng-failed.xml");

        private String value;

        suiteNameXml(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }
}
