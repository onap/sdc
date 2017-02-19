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

package org.openecomp.sdc.ci.tests.execute.resourceui;

import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WorkspaceCheckBoxFilterTest extends SetupCDTest {

	// This test check the status filter .

	private ResourceReqDetails resourceDetails;

	@BeforeMethod
	public void beforTest() {
		resourceDetails = ElementFactory.getDefaultResource();
	}

	@Test
	public void selectCheckOutMenuTest() throws Exception {
		GeneralUIUtils.clickSaveIcon();
		GeneralUIUtils.clickASDCLogo();
		String Status = GeneralUIUtils
				.checkBoxLifeCyclestate(org.openecomp.sdc.ci.tests.datatypes.CheckBoxStatusEnum.CHECKOUT);
		Thread.sleep(500);
		GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();
		AssertJUnit.assertEquals(Status, GeneralUIUtils.getWebElementWaitForVisible("lifecyclestate").getText());
	}

	@Test
	public void selectCheckInMenuTest() throws Exception {
		GeneralUIUtils.clickSaveIcon();
		GeneralUIUtils.checkIn();
		String Status = GeneralUIUtils
				.checkBoxLifeCyclestate(org.openecomp.sdc.ci.tests.datatypes.CheckBoxStatusEnum.CHECKIN);
		Thread.sleep(500);
		GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();
		AssertJUnit.assertEquals(Status, GeneralUIUtils.getWebElementWaitForVisible("lifecyclestate").getText());
	}

	@Test
	public void selectReadyForTestingMenuTest() throws Exception {
		GeneralUIUtils.clickSaveIcon();
		GeneralUIUtils.clickSubmitForTest();
		String Status = GeneralUIUtils
				.checkBoxLifeCyclestate(org.openecomp.sdc.ci.tests.datatypes.CheckBoxStatusEnum.READY_FOR_TESTING);
		Thread.sleep(500);
		GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();

		AssertJUnit.assertEquals(Status, GeneralUIUtils.getWebElementWaitForVisible("lifecyclestate").getText());
	}

	@Test
	public void selectInTestIngMenuTest() throws Exception {
		GeneralUIUtils.clickSaveIcon();
		GeneralUIUtils.clickSubmitForTest();
		GeneralUIUtils.waitForClassNameVisibility("w-sdc-dashboard-card-footer");
		GeneralUIUtils.getDriver().navigate().to(SetupCDTest.getUrl().replace("designer", "tester"));
		GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();
		GeneralUIUtils.clickStartTesting();
		GeneralUIUtils.clickASDCLogo();
		GeneralUIUtils.waitForClassNameVisibility("w-sdc-dashboard-card-footer");
		GeneralUIUtils.getDriver().navigate().to(SetupCDTest.getUrl().replace("tester", "designer"));
		String Status = GeneralUIUtils
				.checkBoxLifeCyclestate(org.openecomp.sdc.ci.tests.datatypes.CheckBoxStatusEnum.IN_TESTING);
		Thread.sleep(500);
		GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();

		AssertJUnit.assertEquals(Status, GeneralUIUtils.getWebElementWaitForVisible("lifecyclestate").getText());
	}

	@Test
	public void selectCertifiedMenuTest() throws Exception {
		GeneralUIUtils.clickSaveIcon();
		GeneralUIUtils.clickSubmitForTest();
		GeneralUIUtils.waitForClassNameVisibility("w-sdc-dashboard-card-footer");
		GeneralUIUtils.getDriver().navigate().to(SetupCDTest.getUrl().replace("designer", "tester"));
		GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();
		GeneralUIUtils.clickStartTesting();
		GeneralUIUtils.clickAccept();
		GeneralUIUtils.waitForClassNameVisibility("w-sdc-dashboard-card-footer");
		GeneralUIUtils.getDriver().navigate().to(SetupCDTest.getUrl().replace("tester", "designer"));
		String Status = GeneralUIUtils
				.checkBoxLifeCyclestate(org.openecomp.sdc.ci.tests.datatypes.CheckBoxStatusEnum.CERTIFIED);
		Thread.sleep(500);
		GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();

		AssertJUnit.assertEquals(Status, GeneralUIUtils.getWebElementWaitForVisible("lifecyclestate").getText());
	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
