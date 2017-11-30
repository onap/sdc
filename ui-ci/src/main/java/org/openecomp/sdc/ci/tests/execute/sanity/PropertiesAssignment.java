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

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.PropertiesAssignmentPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;


public class PropertiesAssignment extends SetupCDTest {

	private String filePath;
	@BeforeClass
	public void beforeClass(){
		filePath = FileHandling.getFilePath("");
	}
	
	@BeforeMethod
	public void beforeTest(){
		System.out.println("File repository is : " + filePath);
		getExtendTest().log(Status.INFO, "File repository is : " + filePath);
	}
	

	
	@Test
	public void declareAndDeleteInputVfTest() throws Exception {
//		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
//		ResourceUIUtils.createResource(vfMetaData, getUser());

		String csarFile = "PCRF_OS_FIXED.csar";
		String componentName = "abstract_pcm";
		String propertyName = "min_instances";
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		resourceMetaData.setVersion("0.1");
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarFile, getUser());
		

		ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
		PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
		PropertiesAssignmentPage.findSearchBoxAndClick(propertyName);
		PropertiesAssignmentPage.clickOnDeclareButton();
		AssertJUnit.assertTrue(PropertiesAssignmentPage.isPropertyChecked(propertyName));
		
		PropertiesAssignmentPage.clickOnInputTab();
		PropertiesAssignmentPage.findInput(componentName, propertyName);
		PropertiesAssignmentPage.clickOnDeleteInputButton();
		PropertiesAssignmentPage.clickOnDeleteInputDialogConfirmationButton();
		PropertiesAssignmentPage.clickOnPropertiesTab();
		PropertiesAssignmentPage.findProperty(propertyName);
		AssertJUnit.assertFalse(PropertiesAssignmentPage.isPropertyChecked(propertyName));
		

	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}

