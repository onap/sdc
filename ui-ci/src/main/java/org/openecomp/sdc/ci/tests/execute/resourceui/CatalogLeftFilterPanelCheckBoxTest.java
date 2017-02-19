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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openecomp.sdc.ci.tests.datatypes.BreadCrumbsButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.CatalogFilterTitlesEnum;
import org.openecomp.sdc.ci.tests.datatypes.CheckBoxStatusEnum;
import org.openecomp.sdc.ci.tests.datatypes.CreateAndImportButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.TypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.CatalogUIUtilitis;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CatalogLeftFilterPanelCheckBoxTest extends SetupCDTest {

	public CatalogLeftFilterPanelCheckBoxTest() {
		// TODO Auto-generated constructor stub
	}

	private ResourceReqDetails resourceDetails;
	FileWriter filwriter = GeneralUIUtils.InitializeprintToTxt("CatalogLeftFilterPanelCheckBoxTest");

	@BeforeMethod
	public void beforTest() {
		resourceDetails = ElementFactory.getDefaultResource();
	}

	// filter by Type Resource in catalog
	@Test
	public void filterByAssetTypeResource() throws Exception {
		List<WebElement> elements = null;
		List<String> validValues = Arrays.asList("VF", "VL", "CP", "VFC");
		GeneralUIUtils.checkIn();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogFilterTypeChecBox(TypesEnum.RESOURCE);
		GeneralUIUtils.getWorkspaceElements();
		elements = GeneralUIUtils.waitForElementsListVisibilityTestMethod("asset-type");
		for (WebElement webElement : elements) {
			assertTrue(validValues.contains(webElement.getAttribute("class")));
		}
	}

	@Test
	public void filterByResourceTypeVF() throws Exception {
		List<WebElement> elements = null;
		List<String> validValues = Arrays.asList("VF");
		GeneralUIUtils.checkIn();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogFilterTypeChecBox(TypesEnum.VF);
		GeneralUIUtils.getWorkspaceElements();
		elements = GeneralUIUtils.waitForElementsListVisibilityTestMethod("asset-type");
		for (WebElement webElement : elements) {
			assertTrue(validValues.contains(webElement.getAttribute("class")));
		}
	}

	@Test
	public void filterByResourceTypeVFC() throws Exception {
		List<WebElement> elements = null;
		List<String> validValues = Arrays.asList("VFC");
		GeneralUIUtils.checkIn();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogFilterTypeChecBox(TypesEnum.VFC);
		GeneralUIUtils.getWorkspaceElements();
		elements = GeneralUIUtils.waitForElementsListVisibilityTestMethod("asset-type");
		for (WebElement webElement : elements) {
			assertTrue(validValues.contains(webElement.getAttribute("class")));
		}
	}

	@Test
	public void filterByResourceTypeCP() throws Exception {
		List<WebElement> elements = null;
		List<String> validValues = Arrays.asList("CP");
		GeneralUIUtils.checkIn();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogFilterTypeChecBox(TypesEnum.CP);
		GeneralUIUtils.getWorkspaceElements();
		elements = GeneralUIUtils.waitForElementsListVisibilityTestMethod("asset-type");
		for (WebElement webElement : elements) {
			assertTrue(validValues.contains(webElement.getAttribute("class")));
		}
	}

	@Test
	public void filterByResourceTypeVL() throws Exception {
		List<WebElement> elements = null;
		List<String> validValues = Arrays.asList("VL");
		GeneralUIUtils.checkIn();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		GeneralUIUtils.catalogFilterTypeChecBox(TypesEnum.VL);
		GeneralUIUtils.getWorkspaceElements();
		elements = GeneralUIUtils.waitForElementsListVisibilityTestMethod("asset-type");
		for (WebElement webElement : elements) {
			assertTrue(validValues.contains(webElement.getAttribute("class")));
		}
	}

	// @Test
	// public void filterByProducTType() throws Exception{
	// List<WebElement> elements = null;
	// List<String> validValues = Arrays.asList("PRODUCT");
	// GeneralUIUtils.checkIn();
	// GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
	// GeneralUIUtils.catalogFilterTypeChecBox(TypesEnum.PRODUCT);
	// GeneralUIUtils.getWorkspaceElements();
	// try {
	// elements =
	// GeneralUIUtils.getEelementsByClassName1("w-sdc-dashboard-card-avatar");
	// for (WebElement webElement : elements) {
	// assertTrue(validValues.contains(webElement.findElement(By.xpath(".//*")).getAttribute("class")));
	// }
	// } catch (Exception e) {
	// System.out.println("No Elements founds!");
	// }
	// }

	@Test
	public void filterByResourceCategories() throws Exception {
		List<WebElement> elements = null;
		List<String> validValues = null;
		GeneralUIUtils.checkIn();
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		Thread.sleep(2000);
		GeneralUIUtils.minimizeCatalogFilterByTitle(CatalogFilterTitlesEnum.TYPE);
		List<String> categories = CatalogUIUtilitis.abcd();
		for (String category : categories) {
			validValues = CatalogUIUtilitis.getAllSubcategoriesByUniqueId(category);
			boolean bool = false;
			try {
				GeneralUIUtils.getWebElementWaitForVisible(category).click();
				bool = true;
			} catch (Exception e) {
				while (!bool) {
					GeneralUIUtils.scrollDown();
					try {
						GeneralUIUtils.getWebElementWaitForVisible(category).click();
						bool = true;
					} catch (Exception e2) {

					}
				}
			}
			String checkBox = GeneralUIUtils.getWebElementWaitForVisible(category).findElement(By.xpath(".//input"))
					.getAttribute("class");
			if (checkBox.contains("ng-not-empty") && validValues != null) {
				try {
					GeneralUIUtils.getWorkspaceElements();
					elements = GeneralUIUtils.getEelementsBycontainsClassName("sprite-resource-icons");
					for (WebElement webElement : elements) {
						String elementUniqueId = webElement.getAttribute("data-tests-id");
						if (!validValues.contains(elementUniqueId)) {
							System.out.println("assert error!");
						}
					}
					GeneralUIUtils.getWebElementWaitForVisible(category).click();
				} catch (Exception e) {
					GeneralUIUtils.getWebElementWaitForVisible(category).click();
					System.out.println("No Elements founds!");
				}
			}
		}
	}

	@SuppressWarnings("null")
	@Test
	public void filterByStatus() throws Exception {

		List<WebElement> elements = null;
		List<String> validValues = null;
		List<ResourceReqDetails> createdComponents = new ArrayList<ResourceReqDetails>();
		List<String> catalogStatuses = Arrays.asList("IN_DESIGN", "READY_FOR_TESTING", "IN_TESTING", "CERTIFIED",
				"DISTRIBUTED");
		GeneralUIUtils.checkIn();
		for (int i = 1; i < catalogStatuses.size() - 1; i++) {
			GeneralUIUtils.sleep(3000);
			GeneralUIUtils.createAndImportButtons(CreateAndImportButtonsEnum.CREATE_VF, GeneralUIUtils.getDriver());
			resourceDetails.setName(getRandomComponentName("ResourceCDTest-"));
			ResourceUIUtils.createResourceInUI(resourceDetails, getUser());
			GeneralUIUtils.clickSubmitForTest();
			if (catalogStatuses.get(i) == "IN_TESTING") {
				GeneralUIUtils.testerUser(true, false, resourceDetails);
				GeneralUIUtils.sleep(3000);
				navigateToUrl(getUrl());

			}
			if (catalogStatuses.get(i) == "CERTIFIED") {
				GeneralUIUtils.testerUser(true, true, resourceDetails);
				GeneralUIUtils.sleep(3000);
				navigateToUrl(getUrl());
			}
			if (catalogStatuses.get(i) == "DISTRIBUTED") {
				GeneralUIUtils.testerUser(true, true, resourceDetails);
				GeneralUIUtils.governorUser(false, true, resourceDetails);
				GeneralUIUtils.opsUser(true, false, resourceDetails);
				GeneralUIUtils.sleep(3000);
				navigateToUrl(getUrl());

			}
			createdComponents.add(resourceDetails);
		}
		GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		// get filters title close.
		Thread.sleep(2000);
		GeneralUIUtils.minimizeCatalogFilterByTitle(CatalogFilterTitlesEnum.TYPE);
		GeneralUIUtils.minimizeCatalogFilterByTitle(CatalogFilterTitlesEnum.CATEGORIES);
		for (CheckBoxStatusEnum statusEnum : CheckBoxStatusEnum.values()) {
			if (catalogStatuses.contains(statusEnum.name().toString())) {
				validValues = GeneralUIUtils.catalogFilterStatusChecBox(statusEnum);
				if (GeneralUIUtils.getWorkspaceElements().size() > 0) {
					String checkBox = GeneralUIUtils.getWebElementByName(statusEnum.getCatalogValue())
							.getAttribute("class");
					if (checkBox.contains("ng-not-empty") && validValues != null) {
						try {
							elements = GeneralUIUtils.getEelementsBycontainsClassName("w-sdc-dashboard-card-edit");
							for (WebElement webElement : elements) {
								String className = webElement.getAttribute("class");
								String textCategory = className.substring(className.indexOf(" "));
								assertTrue(validValues.contains(textCategory.replace(" ", "")));
							}
							GeneralUIUtils.catalogFilterStatusChecBox(statusEnum);
						} catch (Exception e) {
							GeneralUIUtils.catalogFilterStatusChecBox(statusEnum);
							System.out.println("No Elements founds!");
						}
					}
				} else {
					break;
				}
			}
		}
	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}
}
