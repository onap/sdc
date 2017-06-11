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

import java.util.List;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.UserManagementTab;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

public class AdminGeneralPage extends GeneralPageElements {

	public AdminGeneralPage() {
		super();
	}
	
	private static UserManagementTab userManagementTab = new UserManagementTab();
	
	public static UserManagementTab getUserManagementTab() {
		return userManagementTab;
	}

	public static void selectCategoryManagmetTab() throws Exception {
	
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.AdminPageTabs.CATEGORY_MANAGEMENT.getValue());
	}
	
	public static void selectUserManagmetTab() throws Exception {
		
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.AdminPageTabs.USER_MANAGEMENT.getValue());
	}
	
	public static List<WebElement> getServiceCategoriesList() throws Exception {
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CategoryManagement.SERVICE_CATEGORY_HEADER.getValue());
		GeneralUIUtils.waitForLoader();
		return GeneralUIUtils.getWebElementsListByTestID(DataTestIdEnum.CategoryManagement.SERVICE_CATEGORY_LIST.getValue());
	}
	
	public static List<WebElement> getResourceCategoriesList() throws Exception {
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CategoryManagement.RESOURCE_CATEGORY_HEADER.getValue());
		GeneralUIUtils.waitForLoader();
		return GeneralUIUtils.getWebElementsListByTestID(DataTestIdEnum.CategoryManagement.RESOURCE_CATEGORY_LIST.getValue());
	}
	
	public static void createNewServiceCategory(String name) throws Exception {
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CategoryManagement.SERVICE_CATEGORY_HEADER.getValue());
		SetupCDTest.getExtendTest().log(Status.INFO, "Creating service...");
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CategoryManagement.NEW_CATEGORY_BUTTON.getValue());
		GeneralUIUtils.waitForLoader();
		defineNewResourceCategoryName(name);
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
		GeneralUIUtils.waitForLoader();
	}
	
	public static void selectElementFromList(List<WebElement> list, String elementToSelect) throws Exception {
				
		for (WebElement webElement : list) {
			if (webElement.getText().toLowerCase().equals(elementToSelect.toLowerCase())){
				webElement.click();
			}
		}

	}
	
	
	
	public static void addSubCategoryToResource(List<WebElement> resourceList, String parentResource, String subCategoryName) throws Exception{
		
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CategoryManagement.RESOURCE_CATEGORY_HEADER.getValue());
		selectElementFromList(resourceList, parentResource);
		SetupCDTest.getExtendTest().log(Status.INFO, "Creating...");
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CategoryManagement.NEW_SUB_CATEGORY_BUTTON.getValue());
		GeneralUIUtils.waitForLoader();
		defineNewResourceCategoryName(subCategoryName);
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
		GeneralUIUtils.waitForLoader();
		
		
	}
	
	public static void createNewResourceCategory(String name) throws Exception {
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CategoryManagement.RESOURCE_CATEGORY_HEADER.getValue());
		SetupCDTest.getExtendTest().log(Status.INFO, "Creating...");
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CategoryManagement.NEW_CATEGORY_BUTTON.getValue());
		GeneralUIUtils.waitForLoader();
		defineNewResourceCategoryName(name);
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
		GeneralUIUtils.waitForLoader();
		
	}	
	
	public void insertPropertyDefaultValue(String value) {
		WebElement propertyValue = GeneralUIUtils
				.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_VALUE.getValue());
		propertyValue.clear();
		propertyValue.sendKeys(value);
	}
	
	private static void defineNewResourceCategoryName(String name) {
		WebElement categoryNameTextbox = getCategoryName();
		categoryNameTextbox.clear();
		categoryNameTextbox.sendKeys(name);
	}
	
	private static WebElement getCategoryName() {
		return GeneralUIUtils.getWebElementByClassName(DataTestIdEnum.CategoryManagement.NEW_CATEGORY_NAME.getValue());
	}
	

}
