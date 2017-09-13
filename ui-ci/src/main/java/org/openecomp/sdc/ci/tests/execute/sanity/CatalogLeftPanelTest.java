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

package org.openecomp.sdc.ci.tests.execute.sanity;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.CheckBoxStatusEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.CatalogPageLeftPanelSubCategoryCheckbox;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.TopMenuButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.TypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.CatalogUIUtilitis;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.verificator.CatalogVerificator;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

@Test(singleThreaded = true)
public class CatalogLeftPanelTest extends  SetupCDTest{
	
	private String filePath;
	@BeforeMethod
	public void beforeTest(){
		filePath = FileHandling.getFilePath("");
	}
	
	public static String[] resourceTypes = Arrays.stream(ResourceTypeEnum.class.getEnumConstants()).
			map(ResourceTypeEnum::name).toArray(String[]::new);
	
	public static String[] catalogTypes = {"RESOURCE", "SERVICE"};	
	
	public static Object[][] provideData(String[] arObj) {
	    Object[][] arObject = new Object[arObj.length][];

	    int index = 0;
	    for (Object obj : arObj) {
	        arObject[index++] = new Object[]{obj};
	    }
	    return arObject;
	}
	
	@DataProvider(name = "Resource_Type_List")
	private static final Object[][] resourceTypeList(){
		// Extract names of constants from enum as array of Strings 
		ResourceTypeEnum[] resourceEnums = {ResourceTypeEnum.CP, ResourceTypeEnum.VF, ResourceTypeEnum.VFC, ResourceTypeEnum.VL};
		String[] resourcesForTest = Arrays.stream(resourceEnums).map(ResourceTypeEnum::name).toArray(String[]::new);				
		return provideData(resourcesForTest); 
	}
	
	@DataProvider(name = "Type_List")
	private static final Object[][] typeList(){
		// Extract names of constants from enum as array of Strings 
		Object[][] arObject = new Object[catalogTypes.length][];
		int index = 0;
        for (String catalogType : catalogTypes) {
        	if (catalogType.equals("RESOURCE")){
        		arObject[index++] = new Object[]{catalogType, resourceTypes};
        	} else {
        		arObject[index++] = new Object[]{catalogType, new String[] {catalogType}};
        	}
		}
		return arObject; 
	}
	
	@DataProvider(name = "Status_List")
	private static final Object[][] statusList(){
		CheckBoxStatusEnum[] checkboxes = {CheckBoxStatusEnum.CERTIFIED, 
				                           CheckBoxStatusEnum.IN_DESIGN, 
				                           CheckBoxStatusEnum.DISTRIBUTED,
				                           CheckBoxStatusEnum.IN_TESTING,
				                           CheckBoxStatusEnum.READY_FOR_TESTING};
		Object[][] arObject = new Object[checkboxes.length][];
		int index = 0;
		for (CheckBoxStatusEnum checkbox: checkboxes) {
			if (checkbox.equals(CheckBoxStatusEnum.CERTIFIED)){
				arObject[index++] = new Object[]{checkbox, Arrays.asList(LifeCycleStateEnum.CERTIFIED, LifeCycleStateEnum.DISTRIBUTED, LifeCycleStateEnum.WAITING_FOR_DISTRIBUTION) };
			} else if (checkbox.equals(CheckBoxStatusEnum.IN_DESIGN)) {
				arObject[index++] = new Object[]{checkbox, Arrays.asList(LifeCycleStateEnum.CHECKIN, LifeCycleStateEnum.CHECKOUT)};
			} else if (checkbox.equals(CheckBoxStatusEnum.DISTRIBUTED)) {
				arObject[index++] = new Object[]{checkbox, Arrays.asList(LifeCycleStateEnum.DISTRIBUTED)};
			} else if (checkbox.equals(CheckBoxStatusEnum.IN_TESTING)) {
				arObject[index++] = new Object[]{checkbox, Arrays.asList(LifeCycleStateEnum.IN_TESTING)};
			} else if (checkbox.equals(CheckBoxStatusEnum.READY_FOR_TESTING)) {
				arObject[index++] = new Object[]{checkbox, Arrays.asList(LifeCycleStateEnum.READY_FOR_TESTING)};
			}
		}
		return arObject;
	}
	
	// filter by Type Resource in catalog
	@Test(dataProvider = "Type_List")
	public void filterByType(String catalogType, String[] classValues ) throws Exception {
        setLog(catalogType);		
//		getExtendTest().setDescription(catalogType);
		
		CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
		CatalogUIUtilitis.catalogFilterTypeChecBox(TypesEnum.valueOf(catalogType));
		
		CatalogVerificator.validateType(TypesEnum.valueOf(catalogType));				
	}
	
	@Test(dataProvider = "Resource_Type_List")
	public void filterByResourceType(String resourceType) throws Exception {
		setLog(resourceType);		
//		getExtendTest().setDescription(resourceType);			
		
		CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
		CatalogUIUtilitis.catalogFilterTypeChecBox(TypesEnum.valueOf(resourceType));
		
		CatalogVerificator.validateType(TypesEnum.valueOf(resourceType));	
	}
	
	@Test(dataProvider = "Status_List")
	public void filterByStatus(CheckBoxStatusEnum statusCheckbox, List<LifeCycleStateEnum> lifecycleStates) throws Exception{
		setLog(statusCheckbox.name());		
//		getExtendTest().setDescription(statusCheckbox.name());
		
		CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
		CatalogUIUtilitis.clickOnLeftPanelElement(DataTestIdEnum.CatalogPageLeftPanelFilterTitle.CATEGORIES);
		CatalogUIUtilitis.catalogFilterStatusChecBox(statusCheckbox);
		
		CatalogVerificator.validateStatus(lifecycleStates, statusCheckbox.name());
	}

	@Test
	public void filterByUpperCategory() throws Exception{		
		CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
		CatalogUIUtilitis.clickOnLeftPanelElement(DataTestIdEnum.CatalogPageLeftPanelFilterTitle.TYPE);
		
		WebElement categorieCheckbox = CatalogUIUtilitis.clickOnUpperCategoryCheckbox();
		
		CatalogVerificator.validateCategory(categorieCheckbox.getAttribute("textContent").trim());
	}

	@Test
	public void filterByGenericDtabaseSubCategory() throws Exception{		
		CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
		CatalogUIUtilitis.clickOnLeftPanelElement(DataTestIdEnum.CatalogPageLeftPanelFilterTitle.TYPE);

		WebElement checkboxElement = GeneralUIUtils.getElementsByCSS(CatalogPageLeftPanelSubCategoryCheckbox.DATABASE.getValue()).get(0);
		String checkboxElementName = checkboxElement.getAttribute("textContent").trim();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s subcategory ...", checkboxElementName));
		GeneralUIUtils.clickOnAreaJS(checkboxElement);
		CatalogVerificator.validateSubCategory("Generic", checkboxElementName);
	}
	
	@Test(priority = 1)
	public void lastUpdatedService() throws Exception{
		// create service 
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		ResourceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
		List<WebElement> cardElements = GeneralUIUtils.getElementsByCSS(DataTestIdEnum.DashboardCardEnum.INFO_NAME.getValue());
		String firstElementName = cardElements.get(0).getAttribute("textContent").trim();
		assertTrue(String.format("Wrong element name, Exepected : %s , Actual: %s", serviceMetadata.getName(), firstElementName), serviceMetadata.getName().equals(firstElementName));			
	}
	
	@Test(priority = 17)
	public void lastUpdatedResource() throws Exception{
		// create resource 
		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createVF(vfMetaData, getUser());
		
		ResourceGeneralPage.clickCheckinButton(vfMetaData.getName());
		CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
		List<WebElement> cardElements = GeneralUIUtils.getElementsByCSS(DataTestIdEnum.DashboardCardEnum.INFO_NAME.getValue());
		String firstElementName = cardElements.get(0).getAttribute("textContent").trim();
		assertTrue(String.format("Wrong element name, Exepected : %s , Actual: %s", vfMetaData.getName(), firstElementName), vfMetaData.getName().equals(firstElementName));			
	}
	
	@Test(priority = 5)
	public void fromCatalogCheckout() throws Exception{
		// create resource 
		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createVF(vfMetaData, getUser());
		
		ResourceGeneralPage.clickCheckinButton(vfMetaData.getName());
		CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
		GeneralUIUtils.findComponentAndClickInCatalog(vfMetaData.getName());
		ResourceGeneralPage.clickCheckoutButton();
		ResourceGeneralPage.clickCheckinButton(vfMetaData.getName());		
	}
	
	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
