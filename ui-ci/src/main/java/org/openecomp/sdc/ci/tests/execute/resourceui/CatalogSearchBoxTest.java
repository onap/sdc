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

import java.io.FileWriter;
import java.util.List;

import org.openecomp.sdc.ci.tests.datatypes.BreadCrumbsButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CatalogSearchBoxTest extends SetupCDTest {

	private ResourceReqDetails resourceDetails;

	@BeforeMethod
	public void beforTest() {
		resourceDetails = ElementFactory.getDefaultResource();
	}

	// search by ResourceName
	@Test
	public void searchResourceInCatalogMenuTest() throws Exception {
		GeneralUIUtils.checkIn();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogSearchBox(resourceDetails.getName());
		Thread.sleep(500);
		assertTrue(GeneralUIUtils.getWorkspaceElements().size() == 1);
	}

	// search by Description
	@Test
	public void searchResourceInCatalogMenuByDescriptionTest() throws Exception {
		GeneralUIUtils.checkIn();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogSearchBox(resourceDetails.getDescription());
		Thread.sleep(2000);
		List<WebElement> assets = GeneralUIUtils.getWorkspaceElements();

		int count = 0;
		for (WebElement webElement : assets) {
			if (count != 0) {
				GeneralUIUtils.catalogSearchBox(resourceDetails.getDescription());
			}
			if (count == 0) {
				webElement.click();
			} else {
				List<WebElement> assets1 = GeneralUIUtils.getWorkspaceElements();
				assets1.get(count).click();
			}
			GeneralUIUtils.getWebElementWaitForVisible("description").getText()
					.equals(resourceDetails.getDescription());
			GeneralUIUtils.clickExitSign();
			Thread.sleep(500);
			count++;
		}
	}

	// search by tags
	@Test
	public void searchResourceInCatalogMenuBytagsTest() throws Exception {
		GeneralUIUtils.checkIn();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogSearchBox(resourceDetails.getTags().get(0));
		Thread.sleep(500);
		GeneralUIUtils.getWorkspaceElements().get(0).click();
		List<WebElement> expectedTagsList = GeneralUIUtils.getWebElements("i-sdc-tag-text");
		for (int i = 0; i < expectedTagsList.size(); i++) {
			expectedTagsList.get(i).equals(resourceDetails.getTags().get(i));
		}

	}

	// search by Version
	@Test
	public void searchResourceInCatalogMenuByVersionTest() throws Exception {
		GeneralUIUtils.checkIn();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogSearchBox(resourceDetails.getVersion().replace("V", ""));
		Thread.sleep(500);
		GeneralUIUtils.getWorkspaceElements().get(0).click();
		GeneralUIUtils.getSelectList(null, "versionHeader").getFirstSelectedOption().getText()
				.equals(resourceDetails.getVersion());
	}

	// search by SpecialCharacters
	@Test
	public void searchResourceInCatalogMenuBySpecialCharactersTest() throws Exception {
		GeneralUIUtils.moveToStep(StepsEnum.GENERAL);
		GeneralUIUtils.defineDescription(resourceDetails.getDescription() + "!@#$%^&*");
		GeneralUIUtils.checkIn();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogSearchBox("!@#$%^&*");
		Thread.sleep(500);
		GeneralUIUtils.getWorkspaceElements().get(0).click();
		GeneralUIUtils.getWebElementWaitForVisible("description").getText().equals(resourceDetails.getDescription());
	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}
}
