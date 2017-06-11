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

import java.util.List;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.AdminGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Categories extends SetupCDTest {

	
	@Test
	public void createResourceCategory() throws Exception {
		
		AdminGeneralPage.selectCategoryManagmetTab();
		String newResourceCategory = ElementFactory.getDefaultCategory().getName();
		AdminGeneralPage.createNewResourceCategory(newResourceCategory);
		GeneralUIUtils.clickSomewhereOnPage();
		List<WebElement> resourceCategoriesList = AdminGeneralPage.getResourceCategoriesList();
		List<String> collect = resourceCategoriesList.stream().map(f -> f.getText()).collect(Collectors.toList());
		collect.contains(newResourceCategory);
		
	
	}
	
	@Test
	public void createServiceCategory() throws Exception {
		
		AdminGeneralPage.selectCategoryManagmetTab();
		String newserviceCategory = ElementFactory.getDefaultCategory().getName();
		AdminGeneralPage.createNewServiceCategory(newserviceCategory);
		GeneralUIUtils.clickSomewhereOnPage();
		List<WebElement> serviceCategoriesList = AdminGeneralPage.getServiceCategoriesList();
		List<String> collect = serviceCategoriesList.stream().map(f -> f.getText()).collect(Collectors.toList());
		collect.contains(newserviceCategory);
		
	}
	
	
	@Test
	public void createResourceSubCategory() throws Exception {
	
		AdminGeneralPage.selectCategoryManagmetTab();
		String newResourceCategory = ElementFactory.getDefaultCategory().getName();
		String newserviceCategory = ElementFactory.getDefaultCategory().getName();
		String newSubCategory = ElementFactory.getDefaultSubCategory().getName();
		AdminGeneralPage.createNewResourceCategory(newResourceCategory);
		AdminGeneralPage.createNewServiceCategory(newserviceCategory);
		List<WebElement> serviceCategoriesList = AdminGeneralPage.getServiceCategoriesList();
		List<WebElement> resourceCategoriesList = AdminGeneralPage.getResourceCategoriesList();
		AdminGeneralPage.addSubCategoryToResource(resourceCategoriesList, newResourceCategory , newSubCategory);
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.clickSomewhereOnPage();
		
		
	}
	
	
	@Test
	public void createExistingResourceCategory() throws Exception {
		
		AdminGeneralPage.selectCategoryManagmetTab();
		String newResourceCategory = ElementFactory.getDefaultCategory().getName();
		AdminGeneralPage.createNewResourceCategory(newResourceCategory);
		AdminGeneralPage.createNewResourceCategory(newResourceCategory);

		String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
		String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.COMPONENT_CATEGORY_ALREADY_EXISTS.name());
		Assert.assertTrue(errorMessage.contains(checkUIResponseOnError));
		
			
	}
	
	@Test
	public void createExistingServiceCategory() throws Exception {
		
		AdminGeneralPage.selectCategoryManagmetTab();
		String newserviceCategory = ElementFactory.getDefaultCategory().getName();
		AdminGeneralPage.createNewServiceCategory(newserviceCategory);
		AdminGeneralPage.createNewServiceCategory(newserviceCategory);
		AdminGeneralPage.selectUserManagmetTab();
		
		String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
		String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.COMPONENT_CATEGORY_ALREADY_EXISTS.name());
		Assert.assertTrue(errorMessage.contains(checkUIResponseOnError));
		
	}
	
	@Test
	public void createExsitingResourceSubCategory() throws Exception {
	
		
		AdminGeneralPage.selectCategoryManagmetTab();
		String newResourceCategory = ElementFactory.getDefaultCategory().getName();
		String newserviceCategory = ElementFactory.getDefaultCategory().getName();
		String newSubCategory = ElementFactory.getDefaultSubCategory().getName();
		AdminGeneralPage.createNewResourceCategory(newResourceCategory);
		AdminGeneralPage.createNewServiceCategory(newserviceCategory);
		List<WebElement> serviceCategoriesList = AdminGeneralPage.getServiceCategoriesList();
		List<WebElement> resourceCategoriesList = AdminGeneralPage.getResourceCategoriesList();
		AdminGeneralPage.addSubCategoryToResource(resourceCategoriesList, newResourceCategory , newSubCategory);
		AdminGeneralPage.addSubCategoryToResource(resourceCategoriesList, newResourceCategory , newSubCategory);
		GeneralUIUtils.waitForLoader();
		String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
		String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.COMPONENT_SUB_CATEGORY_EXISTS_FOR_CATEGORY.name());
		Assert.assertTrue(errorMessage.contains(checkUIResponseOnError));
		
	}
	
	
	@Test
	public void createServiceWithNewCategory() throws Exception {
		
		AdminGeneralPage.selectCategoryManagmetTab();
		String newResourceCategory = ElementFactory.getDefaultCategory().getName();
		String newserviceCategory = ElementFactory.getDefaultCategory().getName();
		String newSubCategory = ElementFactory.getDefaultSubCategory().getName();
		AdminGeneralPage.createNewResourceCategory(newResourceCategory);
		AdminGeneralPage.createNewServiceCategory(newserviceCategory);
		List<WebElement> serviceCategoriesList = AdminGeneralPage.getServiceCategoriesList();
		List<WebElement> resourceCategoriesList = AdminGeneralPage.getResourceCategoriesList();
		AdminGeneralPage.addSubCategoryToResource(resourceCategoriesList, newResourceCategory , newSubCategory);
		GeneralUIUtils.waitForLoader();
		AdminGeneralPage.selectUserManagmetTab();
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		List<CategoryDefinition> categories = serviceMetadata.getCategories();
		categories.get(0).setName(newserviceCategory);
		
		ServiceUIUtils.createService(serviceMetadata, getUser());
	
	}
	
	
	@Test
	public void createResourceWithNewCategory() throws Exception {
		
		AdminGeneralPage.selectCategoryManagmetTab();
		String newResourceCategory = ElementFactory.getDefaultCategory().getName();
		String newserviceCategory = ElementFactory.getDefaultCategory().getName();
		String newSubCategory = ElementFactory.getDefaultSubCategory().getName();
		AdminGeneralPage.createNewResourceCategory(newResourceCategory);
		AdminGeneralPage.createNewServiceCategory(newserviceCategory);
		List<WebElement> serviceCategoriesList = AdminGeneralPage.getServiceCategoriesList();
		List<WebElement> resourceCategoriesList = AdminGeneralPage.getResourceCategoriesList();
		AdminGeneralPage.addSubCategoryToResource(resourceCategoriesList, newResourceCategory , newSubCategory);
		GeneralUIUtils.waitForLoader();
		AdminGeneralPage.selectUserManagmetTab();
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		
		List<CategoryDefinition> categories = resourceMetaData.getCategories();
		CategoryDefinition categoryDefinition = categories.get(0);
		categoryDefinition.setName(newResourceCategory);
		SubCategoryDefinition subCategoryDefinition = categoryDefinition.getSubcategories().get(0);
		subCategoryDefinition.setName(newSubCategory);
		
		ResourceUIUtils.createResource(resourceMetaData, getUser());
		
	
	}
	
	
	

	
	
	


	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.ADMIN;
	}

}
