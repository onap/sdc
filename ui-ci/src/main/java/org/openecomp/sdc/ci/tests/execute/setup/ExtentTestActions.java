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

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import org.openecomp.sdc.ci.tests.api.SomeInterface;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public final class ExtentTestActions {

    private static final SomeInterface testManager = ExtentTestManager.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtentTestActions.class);

    private ExtentTestActions() {

    }

    public static void log(Status logStatus, Markup mark) {
        ExtentTest test = testManager.getTest();
        test.log(logStatus, mark);
    }

    public static void log(Status logStatus, String message) {
        ExtentTest test = testManager.getTest();
        test.log(logStatus, message);
    }

    public static void log(Status logStatus, String message, String duration) {
        log(logStatus, message + addDurationTag(duration));
    }

    public static void log(Status logStatus, Throwable throwabel) {
        ExtentTest test = testManager.getTest();
        test.log(logStatus, throwabel);
    }

    static void addTag(Status logStatus, String message) {
        Markup m = null;
        switch (logStatus) {
            case PASS:
                m = MarkupHelper.createLabel(message, ExtentColor.GREEN);
                break;
            case FAIL:
                m = MarkupHelper.createLabel(message, ExtentColor.RED);
                break;
            case SKIP:
                m = MarkupHelper.createLabel(message, ExtentColor.BLUE);
                break;
            case FATAL:
                m = MarkupHelper.createLabel(message, ExtentColor.BROWN);
                break;
            default:
                break;
        }

        if (m != null) {
            log(logStatus, m);
        }
    }

    public static Optional<String> takeScreenshot(final Status logStatus, final String screenshotName,
                                                  final String message) {
        try {
            return Optional.of(addScreenshot(logStatus, screenshotName, message));
        } catch (final IOException e) {
            LOGGER.warn("Could not take screenshot", e);
        }

        return Optional.empty();
    }

    public static String addScreenshot(final Status logStatus, String screenshotName,
                                       final String message) throws IOException {
        final String[] splitUuid = UUID.randomUUID().toString().split("-");
        screenshotName = screenshotName + "-" + splitUuid[splitUuid.length - 1];
        final File imageFile = GeneralUIUtils.takeScreenshot(screenshotName, SetupCDTest.getScreenshotFolder());
        final String imageFilePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFile.toURI())
            .getPath();
        testManager.getTest()
            .log(logStatus, message, MediaEntityBuilder.createScreenCaptureFromPath(imageFilePath).build());
        return imageFilePath;
    }

    private static String addDurationTag(String duration) {
        return "<td width=\"80px\">" + duration + "</td>";
    }

    private static String addLinkTag(String fileName, String pathToFile) {
        return String.format("<a download=\"%s\" href=\"%s\">HAR file</a>", fileName, pathToFile);
    }

    static void addFileToReportAsLink(File harFile, String pathToFileFromReportDirectory, String message) {
        log(Status.INFO, message, addLinkTag(harFile.getName(), pathToFileFromReportDirectory));
    }


}
