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

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.LogStatus;

public class TesterOperationPage {

	public TesterOperationPage() {
		super();
	}

	public static void certifyComponent(String componentName) throws Exception {
		clickStartTestingButton();
		clickAccpetCertificationButton(componentName);

	}

	public static void clickAccpetCertificationButton(String componentName) {
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "clicking on accept certification button");
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.ACCEPT.getValue()).click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.ACCEP_TESTING_MESSAGE.getValue())
				.sendKeys(componentName + " tested successfuly");
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.OK.getValue()).click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.sleep(2000);
		GeneralUIUtils.getWebElementWaitForVisible("main-menu-input-search");
		SetupCDTest.getExtendTest().log(LogStatus.INFO, componentName + " is certifed ");
	}

	public static void clickStartTestingButton() {
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "start testing");
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.START_TESTING.getValue())
				.click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.ACCEPT.getValue());
		GeneralUIUtils.sleep(1000);

		// bug
		// Actions actionObject = new Actions(GeneralUIUtils.getDriver());
		// actionObject.keyDown(Keys.CONTROL).sendKeys(Keys.F5).perform();
		//
	}

}
