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

package org.openecomp.sdc.ci.tests.US;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.InputsPage;
import org.openecomp.sdc.ci.tests.pages.PropertyPopup;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.TestException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Inputs extends SetupCDTest {
	
	private static final String DESCRIPTION = "kuku";
	private static final String ARTIFACT_LABEL = "artifact3";
	private static final String ARTIFACT_LABEL_UPDATE = "artifactUpdate";
	private static final String GET_ARTIFACT_LIST_BY_CLASS_NAME = "i-sdc-designer-sidebar-section-content-item-artifact";
	private static final String HEAT_FILE_YAML_NAME = "Heat-File.yaml";
	private static final String HEAT_FILE_YAML_UPDATE_NAME = "Heat-File-Update.yaml";
	private String filePath;
	
	@BeforeMethod
	public void beforeTest(){
		filePath = FileHandling.getFilePath("");
	}
	
	// TODO: There is defect that imported VFC checkin not appear in service until refresh
	// TODO: add support for CP (there is no normative CP's with complex properties which can be selected - import one) - importVFCWithComplexProperty.yml
	// TC1508249
	// Delete Input declared from VLi/CPi in service level - Deleting an Input that was declared from Complex property.
	@Test
	public void  deletingAnInputThatWasDeclaredFromComplexProperty() throws Exception{
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		Map<String, List<String>> resourceInstanceToProperty = new HashMap<>();
		CompositionPage.searchForElement("ExtVL");
		CanvasElement computeElement = canvasManager.createElementOnCanvas("ExtVL");
		canvasManager.clickOnCanvaElement(computeElement);
		resourceInstanceToProperty.put(CompositionPage.getSelectedInstanceName(), Arrays.asList("network_homing", "instance_node_target"));
		
//		CompositionPage.searchForElement("ExtCP");
//		computeElement = canvasManager.createElementOnCanvas("ExtCP");
//		canvasManager.clickOnCanvaElement(computeElement);
//		resourceInstanceToProperty.put(CompositionPage.getSelectedInstanceName(), "order");
		
		GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");
		DeploymentArtifactPage.getLeftMenu().moveToInputsScreen();
		
		
		for(String element: resourceInstanceToProperty.keySet()) {
			String propertyName = resourceInstanceToProperty.get(element).get(0);
			String innerPropertyName = resourceInstanceToProperty.get(element).get(1);
			String dataTestIdPropertyCheckbox = DataTestIdEnum.InputsScreenService.RESOURCE_INSTANCE_PROPERTY_CHECKBOX.getValue() + propertyName;
			
			GeneralUIUtils.clickOnElementByText(element);
			GeneralUIUtils.ultimateWait();
			
			InputsPage.clickOnProperty(propertyName);
			
			PropertyPopup propertyPopup = new PropertyPopup();
			propertyPopup.selectPropertyRadioButton(innerPropertyName);
			propertyPopup.clickSave();
			
			InputsPage.clickOnAddInputButton();
			
			// Verify that input checkbox selected
			verifyPropertyCheckBoxSelected(dataTestIdPropertyCheckbox);
			
			InputsPage.deleteServiceInput(element, propertyName + "_" + innerPropertyName);
			
			// Trying to find deleted service input
			try{
				InputsPage.getServiceInput(element, propertyName + "_" + innerPropertyName);
				assert(false);
			} catch(TestException e){	
				System.out.println("Verfied that service input deleted");
			}
			
			// Verify that input checkbox not selected
			verifyPropertyCheckBoxNotSelected(dataTestIdPropertyCheckbox);
			
			GeneralUIUtils.clickOnElementByText(element);
			GeneralUIUtils.ultimateWait();
		}
		
	}

	
	// TC1508248
	// Delete inputs who come from CP/VL properties
	@Test
	public void  deleteInputsWhoComeFromCpVlProperties() throws Exception{
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		Map<String, String> resourceInstanceToProperty = new HashMap<>();
		CompositionPage.searchForElement("ExtVL");
		CanvasElement computeElement = canvasManager.createElementOnCanvas("ExtVL");
		canvasManager.clickOnCanvaElement(computeElement);
		resourceInstanceToProperty.put(CompositionPage.getSelectedInstanceName(), "network_role");
		
		CompositionPage.searchForElement("ExtCP");
		computeElement = canvasManager.createElementOnCanvas("ExtCP");
		canvasManager.clickOnCanvaElement(computeElement);
		resourceInstanceToProperty.put(CompositionPage.getSelectedInstanceName(), "order");
		
		GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");
		DeploymentArtifactPage.getLeftMenu().moveToInputsScreen();
		
		
		for(String element: resourceInstanceToProperty.keySet()) {
			GeneralUIUtils.clickOnElementByText(element);
			GeneralUIUtils.ultimateWait();
			
			WebElement webElementByTestID = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.InputsScreenService.RESOURCE_INSTANCE_PROPERTY_CHECKBOX.getValue() + resourceInstanceToProperty.get(element));
			InputsPage.clickOnVFInputCheckbox(webElementByTestID);
			
			InputsPage.clickOnAddInputButton();
			
			// Verify that input checkbox selected
			verifyPropertyCheckBoxSelected(DataTestIdEnum.InputsScreenService.RESOURCE_INSTANCE_PROPERTY_CHECKBOX.getValue() + resourceInstanceToProperty.get(element));
			
			InputsPage.deleteServiceInput(element, resourceInstanceToProperty.get(element));
			
			// Trying to find deleted service input
			try{
				InputsPage.getServiceInput(element, resourceInstanceToProperty.get(element));
				assert(false);
			} catch(TestException e){	
				System.out.println("Verfied that service input deleted");
			}
			
			// Verify that input checkbox not selected
			verifyPropertyCheckBoxNotSelected(DataTestIdEnum.InputsScreenService.RESOURCE_INSTANCE_PROPERTY_CHECKBOX.getValue() + resourceInstanceToProperty.get(element));
			
			GeneralUIUtils.clickOnElementByText(element);
			GeneralUIUtils.ultimateWait();
		}
		
	}
	
	
	
	
	
	
	
	public String verifyPropertyCheckBox(String dataTestId) {
		WebElement webElementByTestID = GeneralUIUtils.getWebElementByTestID(dataTestId);
		webElementByTestID = webElementByTestID.findElement(By.className("tlv-checkbox-i"));
		if(webElementByTestID.getAttribute("checked") == null) {
			return "false";
		}
		return "true";
	}
	
	public void verifyPropertyCheckBoxSelected(String dataTestId) {
		if(!verifyPropertyCheckBox(dataTestId).equals("true")) {
			Assert.assertEquals(true, false, "Expected that checkbox will be selected.");
		}
	}
	
	public void verifyPropertyCheckBoxNotSelected(String dataTestId) {
		if(!verifyPropertyCheckBox(dataTestId).equals("false")) {
			Assert.assertEquals(false, true, "Expected that checkbox will not be selected.");
		}
	}

	public String getNormalizedName(String notNormalizedName) {
		String normalizedName = notNormalizedName.toLowerCase();
		normalizedName = normalizedName.replaceAll(" ", "");
		
		return normalizedName;
	}
	



	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
