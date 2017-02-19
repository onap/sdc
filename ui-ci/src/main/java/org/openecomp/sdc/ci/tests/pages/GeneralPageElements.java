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

import static org.testng.AssertJUnit.assertTrue;
import java.util.List;
import java.util.function.Supplier;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.relevantcodes.extentreports.LogStatus;

public class GeneralPageElements {

	public GeneralPageElements() {
		super();
	}

	public static ResourceLeftMenu getLeftMenu() {
		return new ResourceLeftMenu();
	}

	public static void clickCreateButton() {
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "creating...");
		GeneralUIUtils.getWebButton(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue()).click();
		GeneralUIUtils.waitForLoader();
	}

	public static void clickCheckinButton(String componentName) throws Exception {
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "clicking on checkin");
		GeneralUIUtils.getWebButton(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue()).click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.ACCEP_TESTING_MESSAGE.getValue())
				.sendKeys("Checkin " + componentName);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.OK.getValue()).click();
		GeneralUIUtils.waitForLoader();
		assertTrue(GeneralUIUtils.getWebElementWaitForVisible("formlifecyclestate").getText()
				.equals(LifeCycleStateEnum.CHECKIN.getValue()));
	}

	public static void clickSubmitForTestingButton(String componentName) throws Exception {
		try {
			SetupCDTest.getExtendTest().log(LogStatus.INFO, "submiting for testing");
			GeneralUIUtils
					.getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.SUBMIT_FOR_TESTING.getValue())
					.click();
			GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.SUMBIT_FOR_TESTING_MESSAGE.getValue())
					.sendKeys("Submit for testing for " + componentName);
			GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.OK.getValue()).click();
			GeneralUIUtils.waitForLoader();
			GeneralUIUtils.sleep(2000);
			GeneralUIUtils.getWebElementWaitForVisible("main-menu-input-search");
		} catch (Exception e) {
			throw e;
		}
	}

	public static void clickDeleteVersionButton() {
		GeneralUIUtils.getWebButton(DataTestIdEnum.GeneralElementsEnum.DELETE_VERSION_BUTTON.getValue()).click();
	}

	public static void clickRevertButton() {
		GeneralUIUtils.getWebButton(DataTestIdEnum.GeneralElementsEnum.REVERT_BUTTON.getValue()).click();
	}

	public static String getLifeCycleState() {
		return GeneralUIUtils.getWebButton(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue()).getText();
	}

	public static void selectVersion(String version) {
		GeneralUIUtils.getSelectList(version, DataTestIdEnum.GeneralElementsEnum.VERSION_HEADER.getValue());
	}

	public static List<WebElement> getElemenetsFromTable() {
		GeneralUIUtils.waitForLoader();
		return GeneralUIUtils.getElemenetsFromTable(By.className("flex-container"));
	}

	public static boolean checkElementsCountInTable(int expectedElementsCount) {
		// int maxWaitingPeriodMS = 1000;
		// int napPeriodMS = 100;
		// int sumOfWaiting = 0;
		// List<WebElement> elememts = null;
		// boolean isKeepWaiting = false;
		// while (!isKeepWaiting){
		// GeneralUIUtils.sleep(napPeriodMS);
		// sumOfWaiting += napPeriodMS;
		// elememts = getElemenetsFromTable();
		// isKeepWaiting = ( expectedElementsCount == elememts.size() );
		// if (sumOfWaiting > maxWaitingPeriodMS)
		// return false;
		// }
		//
		// return true;
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "checking number of elements in table");
		return checkElementsCountInTable(expectedElementsCount, () -> getElemenetsFromTable());
	}

	public static boolean checkElementsCountInTable(int expectedElementsCount, Supplier<List<WebElement>> func) {
		int maxWaitingPeriodMS = 10000;
		int napPeriodMS = 100;
		int sumOfWaiting = 0;
		List<WebElement> elements = null;
		boolean isKeepWaiting = false;
		while (!isKeepWaiting) {
			GeneralUIUtils.sleep(napPeriodMS);
			sumOfWaiting += napPeriodMS;
			elements = func.get();
			isKeepWaiting = (expectedElementsCount == elements.size());
			if (sumOfWaiting > maxWaitingPeriodMS)
				return false;
		}
		return true;
	}

}
