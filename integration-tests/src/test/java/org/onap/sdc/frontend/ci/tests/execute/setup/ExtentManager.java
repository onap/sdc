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

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentXReporter;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.RestCDUtils;
import org.onap.sdc.backend.ci.tests.utils.Utils;
import org.onap.sdc.backend.ci.tests.utils.rest.AutomationUtils;
import org.testng.ITestContext;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ExtentManager {

    private static final String VERSIONS_INFO_FILE_NAME = "versions.info";
    private static ExtentReports extent;
    private static ExtentHtmlReporter htmlReporter;
    private static ExtentXReporter extentxReporter;

    public enum suiteNameXml {

        TESTNG_FAILED_XML_NAME("testng-failed.xml");

        suiteNameXml(String value) {
            this.value = value;
        }

        private String value;

        public String getValue() {
            return value;
        }

    }

    private static synchronized  ExtentReports setReporter(String filePath, String htmlFile, Boolean isAppend) throws Exception {
        String dbIp = DriverFactory.getConfig().getReportDBhost();
        int dbPort = DriverFactory.getConfig().getReportDBport();

        if (extent == null) {
            extentxReporter = new ExtentXReporter(dbIp, dbPort);
            extent = new ExtentReports();
            initAndSetExtentHtmlReporter(filePath, htmlFile, isAppend);
            if (extentxReporter.config().getReportObjectId() != null) {
                setExtentXReporter(isAppend);
            } else {
                extentxReporter.stop();
            }
        }
        return extent;
    }

    private static synchronized  void setExtentXReporter(Boolean isAppend) {
        extentxReporter.setAppendExisting(isAppend);
        extent.attachReporter(extentxReporter);
    }

    private static synchronized  void initAndSetExtentHtmlReporter(String filePath, String htmlFile, Boolean isAppend) throws Exception {
        htmlReporter = new ExtentHtmlReporter(filePath + htmlFile);
        setConfiguration(htmlReporter);
        htmlReporter.setAppendExisting(isAppend);
        extent.attachReporter(htmlReporter);
    }

    static synchronized  ExtentReports getReporter() {
        return extent;
    }

    static void initReporter(String filepath, String htmlFile, ITestContext context) throws Exception {

        String onboardVersion = AutomationUtils.getOnboardVersion();
        String osVersion = AutomationUtils.getOSVersion();
        Config config = Utils.getConfig();
        String envData = config.getUrl();
        String suiteName = getSuiteName(context);
        String reportStartDate = null;
        if (suiteName.equals(suiteNameXml.TESTNG_FAILED_XML_NAME.getValue())) {
            if (config.getUseBrowserMobProxy()) {
                setTrafficCaptue(config);
            }
            setReporter(filepath, htmlFile, true);
            String suiteNameFromVersionInfoFile = FileHandling.getKeyByValueFromPropertyFormatFile(filepath + VERSIONS_INFO_FILE_NAME, "suiteName");
            reporterDataDefinition(onboardVersion, osVersion, envData, suiteNameFromVersionInfoFile);
        } else {
            FileHandling.deleteDirectory(SetupCDTest.getReportFolder());
            FileHandling.createDirectory(filepath);
            setReporter(filepath, htmlFile, false);
            Calendar calendar = new GregorianCalendar();
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
            reportStartDate = formatter.format(calendar.getTime());
            reporterDataDefinition(onboardVersion, osVersion, envData, suiteName);
            AutomationUtils.createVersionsInfoFile(filepath + VERSIONS_INFO_FILE_NAME, onboardVersion, osVersion, envData, suiteName, reportStartDate);
        }

    }

    private static void reporterDataDefinition(String onboardVersion, String osVersion, String envData, String suiteNameFromVersionInfoFile) throws Exception {
        extent.setSystemInfo("Onboard Version", onboardVersion);
        extent.setSystemInfo("OS Version", osVersion);
        extent.setSystemInfo("Host Name Address", RestCDUtils.getExecutionHostAddress());
        extent.setSystemInfo("ExecutedOn", envData);
        extent.setSystemInfo("SuiteName", suiteNameFromVersionInfoFile);
    }

    static String getSuiteName(ITestContext context) {
        String suitePath = context.getSuite().getXmlSuite().getFileName();
        if (suitePath != null) {
            File file = new File(suitePath);
            String suiteName = file.getName();
            return suiteName;
        }
        return null;
    }

    private static synchronized  ExtentHtmlReporter setConfiguration(ExtentHtmlReporter htmlReporter) throws Exception {

        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setEncoding("UTF-8");
        htmlReporter.config().setProtocol(Protocol.HTTPS);
        htmlReporter.config().setDocumentTitle("SDC Automation Report");
        htmlReporter.config().setChartVisibilityOnOpen(true);
        htmlReporter.config().setReportName("SDC Automation Report");
        htmlReporter.config().setChartVisibilityOnOpen(false);
        htmlReporter.setStartTime(null);
        return htmlReporter;
    }

    public static void closeReporter() {
        extent.flush();
    }

    private static void setTrafficCaptue(Config config) {
        boolean mobProxyStatus = config.getUseBrowserMobProxy();
        if (mobProxyStatus) {
            config.setCaptureTraffic(true);
        }
    }
}

