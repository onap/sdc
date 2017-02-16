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

import static org.testng.AssertJUnit.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.relevantcodes.extentreports.LogStatus;

public class OpsOperationPage {

	public OpsOperationPage() {
		super();
	}

	public static void distributeService() {
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "distributing...");
		clickOnButton(DataTestIdEnum.DistributionChangeButtons.DISTRIBUTE);
		GeneralUIUtils.getWebButton(DataTestIdEnum.DistributionChangeButtons.MONITOR.getValue());
	}

	public static void displayMonitor() {
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "clicking on monitor button");
		// clickOnButton(DataTestIdEnum.DistributionChangeButtons.MONITOR);
		GeneralUIUtils.moveToStep(StepsEnum.MONITOR);
	}

	public static void re_distributeService() {
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "redistributing...");
		clickOnButton(DataTestIdEnum.DistributionChangeButtons.RE_DISTRIBUTE);
		GeneralUIUtils.getWebButton(DataTestIdEnum.DistributionChangeButtons.MONITOR.getValue());
	}

	private static void clickOnButton(DataTestIdEnum.DistributionChangeButtons button) {
		GeneralUIUtils.getWebElementWaitForVisible(button.getValue()).click();
		GeneralUIUtils.waitForLoader();
	}

	public static List<WebElement> getRowsFromMonitorTable() {
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "getting number of rows from distribution table");
		GeneralPageElements.checkElementsCountInTable(1, () -> GeneralUIUtils.waitForElementsListVisibility("ditributionTable"));
		List<WebElement> distributionRecords = GeneralUIUtils.waitForElementsListVisibility("ditributionTable");
		List<WebElement> findElements = distributionRecords.get(0).findElements(By.className("w-sdc-distribute-parent-block"));
		return findElements;
		
		

	}

	public static void showDistributionStatus(int rowIndex) {
		GeneralUIUtils.getWebElementWaitForVisible("ShowRecordButton_" + String.valueOf(rowIndex)).click();
		GeneralUIUtils.waitForLoader();
	}

	public static String getTotalArtifactsSum(int rowIndex) {
		return GeneralUIUtils.waitFordataTestIdVisibility("totalArtifacts_" + String.valueOf(rowIndex)).getText();
	}

	public static String getNotifiedArtifactsSum(int rowIndex) {
		return GeneralUIUtils.waitFordataTestIdVisibility("notified_" + String.valueOf(rowIndex)).getText();
	}

	public static String getDownloadedArtifactsSum(int rowIndex) {
		return GeneralUIUtils.waitFordataTestIdVisibility("downloaded_" + String.valueOf(rowIndex)).getText();
	}

	public static String getDeployedArtifactsSum(int rowIndex) {
		return GeneralUIUtils.waitFordataTestIdVisibility("deployed_" + String.valueOf(rowIndex)).getText();
	}

	public static String getNotNotifiedArtifactsSum(int rowIndex) {
		return GeneralUIUtils.waitFordataTestIdVisibility("NotNotified_" + String.valueOf(rowIndex)).getText();
	}

	public static String getErrorsSum(int rowIndex) {
		return GeneralUIUtils.waitFordataTestIdVisibility("errors_" + String.valueOf(rowIndex)).getText();
	}

	public static void clickRefreshTableButton(int rowIndex) {
		// SetupCDTest.getExtendTest().log(LogStatus.INFO, "refreshing
		// distribution table");
		GeneralUIUtils.getWebElementWaitForVisible("refreshButton").click();

		// wait until total artifacts field disappear
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 30);
		wait.until(ExpectedConditions.invisibilityOfElementLocated(
				By.xpath("//*[@data-tests-id='" + "totalArtifacts_" + String.valueOf(rowIndex) + "']")));
	}

	public static void waitUntilArtifactsDistributed(int rowIndex) throws Exception {
		waitUntilArtifactsDistributed("0", 0);
	}

	public static void waitUntilArtifactsDistributed(String expectedArtifactsSum, int rowIndex) throws Exception {
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "waiting until all artifacts distributed");
		boolean isKeepWaiting = true;
		int maxWaitingPeriodMS = 5 * 60 * 1000;
		int sumWaitingTime = 0;
		int napPeriod = 1000;
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
			// isKeepWaiting =
			// !expectedArtifactsSum.equals(actualTotalArtifactsSize) &&
			// !expectedArtifactsSum.equals(actualNotifiedArtifactsSize) &&
			// !expectedArtifactsSum.equals(actualDownloadedArtifactsSize) &&
			// !expectedArtifactsSum.equals(actualDeployedArtifactsSize) &&
			// actualNotNotifedArtifactsSize.equals("0");
			// if (Integer.valueOf(actualTotalArtifactsSize) >
			// Integer.valueOf(expectedArtifactsSum)){
			// isKeepWaiting = false;
			// throw new Exception(String.format("MORE ARTIFACTS THEN EXPECTED -
			// actual: %s, expected: %s", actualTotalArtifactsSize,
			// expectedArtifactsSum));
			//// Assert.fail(String.format("MORE ARTIFACTS THEN EXPECTED -
			// actual: %s, expected: %s", actualTotalArtifactsSize,
			// expectedArtifactsSum));
			// }
			if (isKeepWaiting) {

				if (Integer.parseInt(actualNotNotifedArtifactsSize) > 1) {
					SetupCDTest.getExtendTest().log(LogStatus.INFO, "Some artifacts are not notified...");
					isKeepWaiting = false;
					throw new Exception("Some artifacts are not notified... check distribution client");
				}

				GeneralUIUtils.sleep(napPeriod);
				sumWaitingTime += napPeriod;

				if (sumWaitingTime > maxWaitingPeriodMS) {
					SetupCDTest.getExtendTest().log(LogStatus.FAIL, "not all artifacts are displayed");
					isKeepWaiting = false;
					throw new Exception(String.format("NOT ALL ARTIFACTS ARE DISPLAYED WITHIN %s SECONDS",
							String.valueOf(maxWaitingPeriodMS / 1000)));
				}

				clickRefreshTableButton(rowIndex);
			}
		}

		SetupCDTest.getExtendTest().log(LogStatus.PASS, "all artifacts are distributed successfully");
	}

}
