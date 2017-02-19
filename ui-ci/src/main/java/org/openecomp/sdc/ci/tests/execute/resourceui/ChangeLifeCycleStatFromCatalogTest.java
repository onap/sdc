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

import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.openecomp.sdc.ci.tests.datatypes.BreadCrumbsButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChangeLifeCycleStatFromCatalogTest extends SetupCDTest {

	public ChangeLifeCycleStatFromCatalogTest() {
		// TODO Auto-generated constructor stub
	}

	// This test check the status filter .

	private ResourceReqDetails resourceDetails;

	@BeforeMethod
	public void beforTest() {
		resourceDetails = ElementFactory.getDefaultResource();
	}

	@Test
	public void checkInFromCatalog() throws InterruptedException {
		GeneralUIUtils.clickSaveIcon();
		GeneralUIUtils.clickASDCLogo();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogSearchBox(resourceDetails.getName());
		List<WebElement> assets = GeneralUIUtils.getWorkspaceElements();
		if (assets.isEmpty()) {
			System.out.println("error elements not found.");
		} else {
			for (WebElement webElement : assets) {
				webElement.click();
				GeneralUIUtils.checkIn();
				GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();
				Thread.sleep(2000);
				System.out.println(ResourceUIUtils.lifeCycleStateUI());
				System.out.println(LifeCycleStateEnum.CHECKIN.getValue());
				assertTrue(ResourceUIUtils.lifeCycleStateUI().contentEquals(LifeCycleStateEnum.CHECKIN.getValue()));

			}
		}

	}

	@Test
	public void checkOutFromCatalog() throws Exception {
		GeneralUIUtils.clickSaveIcon();
		GeneralUIUtils.clickASDCLogo();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogSearchBox(resourceDetails.getName());
		List<WebElement> assets = GeneralUIUtils.getWorkspaceElements();
		if (assets.isEmpty()) {
			System.out.println("error elements not found.");
		} else {
			for (WebElement webElement : assets) {
				webElement.click();
				GeneralUIUtils.checkinCheckout(resourceDetails.getName());
				assertTrue(ResourceUIUtils.lifeCycleStateUI().contentEquals(LifeCycleStateEnum.CHECKOUT.getValue()));

			}
		}

	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
