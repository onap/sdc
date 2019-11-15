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

import static org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions.addScreenshot;
import static org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions.addTag;
import static org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions.log;

import com.aventstack.extentreports.Status;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentManager.suiteNameXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;

public class ReportAfterTestManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportAfterTestManager.class);
    private static String testName;
    private static Throwable throwable;
    private static String exceptionMsgFormat = "%s - The following exception occurred:";

    private ReportAfterTestManager() {

    }

    public static void report(final ITestResult result, final ITestContext context) {
        testName = result.getName();
        throwable = result.getThrowable();

        final String suiteName = ExtentManager.getSuiteName(context);

        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                logSuccessAfterTest();
                break;

            case ITestResult.FAILURE:
                logFailure(suiteName);
                break;

            case ITestResult.SKIP:
                logSkipAfterTest();
                break;

            default:
                break;
        }

    }

    private static void logSuccessAfterTest() {
        addTag(Status.PASS, "Success");
        takeScreenshot(Status.PASS);
    }

    private static void logFailAfterTest() {
        addTag(Status.FAIL, "Failure");
        log(Status.ERROR, String.format(exceptionMsgFormat, Status.ERROR));
        log(Status.ERROR, throwable);
        takeScreenshot(Status.FAIL);
    }

    private static void logSkipAfterTest() {
        addTag(Status.SKIP, "Skipped");
        log(Status.SKIP, String.format(exceptionMsgFormat, Status.SKIP));
        log(Status.SKIP, throwable);
        takeScreenshot(Status.SKIP);
    }

    private static void logFatalAfterTest() {
        addTag(Status.FATAL, "Fatal");
        log(Status.FATAL, String.format(exceptionMsgFormat, Status.FATAL));
        log(Status.FATAL, throwable);
        takeScreenshot(Status.FATAL);
    }

    private static void takeScreenshot(final Status status) {
        String adjustedTestName = testName;
        String infoFromDataProvider = WindowTestManager.getWindowMap().getAddedValueFromDataProvider();
        if (StringUtils.isNotEmpty(infoFromDataProvider)) {
            infoFromDataProvider = infoFromDataProvider.replace(":", "-");
            adjustedTestName = String.format("%s | %s", testName, infoFromDataProvider);
        }
        try {
            addScreenshot(status, adjustedTestName, "Finished the test with the following screenshot:");
        } catch (final IOException e) {
            final String warnMsg = "Could not take screenshot of the final screen";
            LOGGER.warn(warnMsg, e);
            log(Status.WARNING, String.format("%s: %s", warnMsg, e.getMessage()));
        }
    }

    private static void logFailure(final String suiteName) {
        if (suiteNameXml.TESTNG_FAILED_XML_NAME.getValue().equals(suiteName)) {
            logFatalAfterTest();
        } else {
            logFailAfterTest();
        }
    }

}

