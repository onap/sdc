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

package org.openecomp.sdc.ci.tests.pages;

import com.aventstack.extentreports.Status;
import java.util.List;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OpsOperationPage {

    private static final int WEB_DRIVER_TIME_OUT = 90;
    private static final int MAX_WAITING_PERIOD_MS = 5 * 60 * 1000;
    private static final int NAP_PERIOD = 10000;
    private static final int MAX_WAITING_PERIOF_DIVIDER = 1000;

    private OpsOperationPage() {
        super();
    }

    public static void distributeService() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Distributing");
        clickOnButton(DataTestIdEnum.DistributionChangeButtons.DISTRIBUTE);
        GeneralUIUtils.waitForLoader();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DistributionChangeButtons.MONITOR.getValue());
    }

    public static void displayMonitor() {
        GeneralUIUtils.moveToStep(StepsEnum.MONITOR);
    }

    private static void clickOnButton(DataTestIdEnum.DistributionChangeButtons button) {
        GeneralUIUtils.getWebElementByTestID(button.getValue()).click();
        GeneralUIUtils.waitForLoader();
    }

    public static List<WebElement> getRowsFromMonitorTable() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Counting the rows from the distribution table");
        GeneralPageElements.checkElementsCountInTable(1, () -> GeneralUIUtils.getWebElementsListByTestID("ditributionTable"));
        List<WebElement> distributionRecords = GeneralUIUtils.getWebElementsListByTestID("ditributionTable");
        return distributionRecords.get(0).findElements(By.className("w-sdc-distribute-parent-block"));
    }

    public static void showDistributionStatus(int rowIndex) {
        GeneralUIUtils.getWebElementByTestID("ShowRecordButton_" + rowIndex).click();
        GeneralUIUtils.waitForLoader();
    }

    public static String getTotalArtifactsSum(int rowIndex) {
        return GeneralUIUtils.getWebElementByTestID("totalArtifacts_" + rowIndex).getText();
    }

    public static String getNotifiedArtifactsSum(int rowIndex) {
        return GeneralUIUtils.getWebElementByTestID("notified_" + rowIndex).getText();
    }

    public static String getDownloadedArtifactsSum(int rowIndex) {
        return GeneralUIUtils.getWebElementByTestID("downloaded_" + rowIndex).getText();
    }

    public static String getDeployedArtifactsSum(int rowIndex) {
        return GeneralUIUtils.getWebElementByTestID("deployed_" + rowIndex).getText();
    }

    public static String getNotNotifiedArtifactsSum(int rowIndex) {
        return GeneralUIUtils.getWebElementByTestID("NotNotified_" + rowIndex).getText();
    }

    public static void clickRefreshTableButton(int rowIndex) {
        GeneralUIUtils.getWebElementByTestID("refreshButton").click();
        // wait until total artifacts field disappear
        WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), WEB_DRIVER_TIME_OUT);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@data-tests-id='" + "totalArtifacts_" + rowIndex
            + "']")));
    }

    public static void waitUntilArtifactsDistributed(int rowIndex) throws Exception {
        waitUntilArtifactsDistributed("0", rowIndex);
    }

    public static void waitUntilArtifactsDistributed(String expectedArtifactsSum, int rowIndex) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Waiting until all artifacts are distributed");
        boolean isKeepWaiting = true;
        int maxWaitingPeriodMS = MAX_WAITING_PERIOD_MS;
        int sumWaitingTime = 0;
        int napPeriod = NAP_PERIOD;
        while (isKeepWaiting) {
            showDistributionStatus(rowIndex);
            String actualTotalArtifactsSize = getTotalArtifactsSum(rowIndex);
            String actualNotifiedArtifactsSize = getNotifiedArtifactsSum(rowIndex);
            String actualDownloadedArtifactsSize = getDownloadedArtifactsSum(rowIndex);
            String actualDeployedArtifactsSize = getDeployedArtifactsSum(rowIndex);
            String actualNotNotifedArtifactsSize = getNotNotifiedArtifactsSum(rowIndex);
            isKeepWaiting = !actualTotalArtifactsSize.equals(actualDownloadedArtifactsSize)
                    || !actualTotalArtifactsSize.equals(actualNotifiedArtifactsSize)
                    || !actualTotalArtifactsSize.equals(actualDeployedArtifactsSize)
                    || actualTotalArtifactsSize.equals("0") || actualDownloadedArtifactsSize.equals("0")
                    || actualNotifiedArtifactsSize.equals("0") || actualDeployedArtifactsSize.equals("0");

            if (isKeepWaiting) {

                if (Integer.parseInt(actualNotNotifedArtifactsSize) > 1) {
                    SetupCDTest.getExtendTest().log(Status.INFO, "Some artifacts are not notified");
                    isKeepWaiting = false;
                    throw new Exception("Some artifacts are not notified...");
                }

                GeneralUIUtils.sleep(napPeriod);
                sumWaitingTime += napPeriod;

                if (sumWaitingTime > maxWaitingPeriodMS) {
                    SetupCDTest.getExtendTest().log(Status.INFO, "Not all artifacts are displayed");
                    isKeepWaiting = false;
                    throw new Exception(String.format("Not all artifacts are displayed withing %s seconds",
                        maxWaitingPeriodMS / MAX_WAITING_PERIOF_DIVIDER));
                }

                clickRefreshTableButton(rowIndex);
            }
        }

        SetupCDTest.getExtendTest().log(Status.INFO, "All artifacts were successfully distributed");
    }

}
