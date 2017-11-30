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

package org.openecomp.sdc.ci.tests.verificator;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.PropertiesPopupEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentPage;
import org.openecomp.sdc.ci.tests.pages.PropertyPopup;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.aventstack.extentreports.Status;

public class ServiceVerificator {

	private ServiceVerificator() {	
	}	

	public static void verifyNumOfComponentInstances(ComponentReqDetails component, String version, int numOfVFC,
			User user) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verifing the number of components on the canvas; should be %s", numOfVFC));  
		String responseAfterDrag = null;
		component.setVersion(version);
		if (component instanceof ServiceReqDetails) {
			responseAfterDrag = RestCDUtils.getService((ServiceReqDetails) component, user).getResponse();
		} else if (component instanceof ResourceReqDetails) {
			responseAfterDrag = RestCDUtils.getResource((ResourceReqDetails) component, user).getResponse();
		}
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		int size = ((JSONArray) jsonResource.get("componentInstances")).size();
		assertTrue(size == numOfVFC, "Expected number of componenet instances is " + numOfVFC + ", but actual is " + size);
		ExtentTestActions.log(Status.INFO, "The number of components on the canvas was verified.");
	}
	
	public static void verifyServiceUpdatedInUI(ServiceReqDetails service) {
		assertTrue(service.getName().equals(ResourceGeneralPage.getNameText()));
		assertTrue(service.getDescription().equals(ResourceGeneralPage.getDescriptionText()));
		assertTrue(service.getCategory().equals(ServiceGeneralPage.getCategoryText()));
		assertTrue(service.getProjectCode().equals(ServiceGeneralPage.getProjectCodeText()));
		for(String tag: ServiceGeneralPage.getTags()){
			assertTrue(service.getTags().contains(tag));
		}
		assertTrue(service.getContactId().equals(ResourceGeneralPage.getContactIdText()));		
	}
	
	public static void verifyServiceDeletedInUI(ServiceReqDetails service) throws InterruptedException {
		Thread.sleep(1000);
		List<WebElement> cardElements = GeneralUIUtils.getElementsByCSS(DataTestIdEnum.DashboardCardEnum.DASHBOARD_CARD.getValue());
		if (!(cardElements.isEmpty())){
			for (WebElement cardElement: cardElements){
				WebElement componentName = GeneralUIUtils.getElementfromElementByCSS(cardElement, 
                        DataTestIdEnum.DashboardCardEnum.INFO_NAME.getValue());
				WebElement componentType = GeneralUIUtils.getElementfromElementByCSS(cardElement,
                        DataTestIdEnum.DashboardCardEnum.ASSET_TYPE_CSS.getValue());
				
				String componentNameStr    = componentName.getAttribute("textContent").trim(), 
					   componentTypeStr    = componentType.getAttribute("class");
				
				if(componentTypeStr.equals("S")){
					assertTrue( !(componentNameStr.equals(service.getName())), "Deleted service was found !!!");
				}
			}
		}
	}
	
	public static void verifyServiceLifecycle(ServiceReqDetails service, User user, LifecycleStateEnum expectedLifecycleState) {
		String responseAfterDrag = RestCDUtils.getService(service, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		String actualLifecycleState = jsonResource.get("lifecycleState").toString();
		assertTrue(expectedLifecycleState.name().equals(actualLifecycleState), "actual: " + actualLifecycleState + "-- expected: " + expectedLifecycleState);
	}
	
	public static void verifyLinkCreated(ServiceReqDetails createServiceInUI, User user, int expectedRelationsSize) {
		String responseAfterDrag = RestCDUtils.getService(createServiceInUI, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		assertTrue(((JSONArray) jsonResource.get("componentInstancesRelations")).size() == expectedRelationsSize);

	}
	
	public static void verifyManagmentWorkflow(String expectedName, String expectedDescription){
		String actualName = GeneralUIUtils.getWebElementBy(By.cssSelector("div[class='text name']")).getText();
		String actualDescription = GeneralUIUtils.getWebElementBy(By.cssSelector("div[class='text description']")).getText();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verifing name ( should be %s ) and description ( should be %s ) ", expectedName, expectedDescription));
		assertTrue(actualName.equals(expectedName) && actualDescription.equals(expectedDescription));
	}
	
	public static void verifyVersionUI(String expected){
		String actualVersion = GeneralUIUtils.getSelectedElementFromDropDown(DataTestIdEnum.GeneralElementsEnum.VERSION_HEADER.getValue()).getText().replace("V", "");
		assertTrue(actualVersion.equals(expected), String.format( "Expected version: %s, Actual version: %s", expected, actualVersion));
	}
	
	public static void verifyOpenTabTitle(DataTestIdEnum.CompositionScreenEnum currentTab) throws Exception{
		List<String> expectedTitles  = new ArrayList<String>();
		for(String expectedTitle: currentTab.getTitle()){
			expectedTitles.add(expectedTitle.toLowerCase());
		}		
		for (WebElement actualTitle: CompositionPage.getOpenTabTitle()){
			int indexOfTitle = expectedTitles.indexOf(actualTitle.getText().trim().toLowerCase());
			assertTrue(indexOfTitle >= 0, "Wrong title");
			expectedTitles.remove(indexOfTitle);
		}
		assertTrue(expectedTitles.size() == 0, "Missing titles in " + currentTab.getValue());
	}
	
	public static void verifyDeploymentPageSubElements(String moduleName, DeploymentViewVerificator verificatorObj) throws Exception{
		HashMap<String, List<String>> moduleProperties = verificatorObj.getDeploymentViewData().get(moduleName);
		
		ServiceVerificator.moveMetadataPropertiesArtifactSection(-700);
		
		List<WebElement> artifacts, properties;
		artifacts = DeploymentPage.getArtifactNames();
		properties = DeploymentPage.getPropertyNames();
		assertTrue(moduleProperties.get("artifacts").size() == artifacts.size(), "Artifacts amount not as expected, expected " + moduleProperties.get("artifacts").size());
		assertTrue(moduleProperties.get("artifacts").containsAll(artifacts.stream().
				                                                map(e -> e.getAttribute("textContent")).
				                                                collect(Collectors.toList())));
		assertTrue(moduleProperties.get("properties").size() == properties.size(), "Properties amount not as expected, expected " + moduleProperties.get("properties").size());
		assertTrue(moduleProperties.get("properties").containsAll(properties.stream().
												                map(e -> e.getAttribute("textContent")).
											 	                collect(Collectors.toList())));
		
		DeploymentPage.clickOnProperties();
		DeploymentPage.clickOnArtifacts();
		ServiceVerificator.moveMetadataPropertiesArtifactSection(700);
	}
	
	public static void verifyVFModuleCustomizationUUID(ServiceReqDetails service) throws Exception {
		Predicate<String> componentInstancePredicate = e -> e.length() > 35;
		List<String> customizationUUIDList = getAllVFModuleCustomizationUUIDs(service);
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating vfModuleCustomizationUUID uniqness ... "));
		assertTrue(customizationUUIDList.stream().allMatch(componentInstancePredicate), "vfModuleCustomizationUUID is less then 35 chars");
		CustomizationUUIDVerificator.validateCustomizationUUIDuniqueness(customizationUUIDList);
	}

	public static List<String> getAllVFModuleCustomizationUUIDs(ServiceReqDetails service) throws Exception {
		Service serviceObj = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, service.getName(), service.getVersion());
		List<String> customizationUUIDList = serviceObj.getComponentInstances().get(0).getGroupInstances().stream().
                                                                                     map(e -> e.getCustomizationUUID()).
                                                                                     collect(Collectors.toList());
		
		return customizationUUIDList;
	}
	
	public static String getVFModulePropertieValue(ServiceReqDetails service, String propertyName, String moduleName) throws Exception {
		Service serviceObj = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, service.getName(), service.getVersion());	
		List<GroupInstance> groupInstances = serviceObj.getComponentInstances().get(0).getGroupInstances();		
		List<GroupInstanceProperty> groupInstancesProperties = groupInstances.stream().
				                                                              filter(e -> e.getName().equals(moduleName)).
				                                                              findFirst().
				                                                              get().
				                                                              convertToGroupInstancesProperties();		
		String propertieValue = groupInstancesProperties.stream().
				                                         filter(e -> e.getName().equals(propertyName)).
				                                         findFirst().
				                                         get().
				                                         getValue();
		return propertieValue;
	}
	
	public static boolean isEqualCustomizationUUIDsAfterChanges(List<String> listBefore, List<String> listAfter){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating if vfModuleCustomizationUUID changed after certification ... "));
		return (listBefore.size() == listAfter.size()) && (listBefore.containsAll(listAfter));
	}

	public static void verifyDisabledServiceProperties() throws Exception{
		List<String> propertiesForCheck = Arrays.asList("isBase", "vf_module_type", "vf_module_label", "vf_module_description");
		List<PropertiesPopupEnum> popupElementsForCheck = Arrays.asList(PropertiesPopupEnum.PROPERTY_NAME, 
				                                                        PropertiesPopupEnum.PROPERTY_DESCRIPTION, 
				                                                        PropertiesPopupEnum.PROPERTY_TYPE, 
				                                                        PropertiesPopupEnum.PROPERTY_VALUE);
		ServiceVerificator.moveMetadataPropertiesArtifactSection(-700);
		List<WebElement> properties = DeploymentPage.getPropertyNames();
		
		for(WebElement property : properties){
			if (propertiesForCheck.contains(property.getAttribute("textContent"))){
				DeploymentPage.clickOnProperty(property);
				Select propertTypeElement = new Select(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_TYPE.getValue()));
				boolean isTypeBoolean = propertTypeElement.getFirstSelectedOption().getText().contains("boolean");
				for (PropertiesPopupEnum popupElement: popupElementsForCheck){
					if (isTypeBoolean && popupElement == PropertiesPopupEnum.PROPERTY_VALUE){
						assertTrue(GeneralUIUtils.checkForDisabledAttribute(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_BOOLEAN_VALUE.getValue()), String.format("Element %s not disabled ", property.getText()));
					} else {
						assertTrue(GeneralUIUtils.checkForDisabledAttribute(popupElement.getValue()), String.format("Element %s not disabled ", property.getText()));
					}					
				}
				new PropertyPopup().clickCancel();
			}
		}
		
		DeploymentPage.clickOnProperties();
		ServiceVerificator.moveMetadataPropertiesArtifactSection(700);
	}

	public static void verifyEnabledServiceProperties() throws Exception{
		List<String> propertiesForCheck = Arrays.asList("initial_count", "max_vf_module_instances", "min_vf_module_instances");
		
		ServiceVerificator.moveMetadataPropertiesArtifactSection(-700);
		List<WebElement> properties = DeploymentPage.getPropertyNames();
		
		ServiceVerificator.positiveFlow(propertiesForCheck, properties);
		ServiceVerificator.negativeFlow(propertiesForCheck, properties);
		
		DeploymentPage.clickOnProperties();
		ServiceVerificator.moveMetadataPropertiesArtifactSection(700);			
	}

	public static void positiveFlow(List<String> propertiesForCheck, List<WebElement> properties)
			throws InterruptedException {
		int baseNumber = new Random().nextInt(100) + 2;
		for(WebElement property : properties){
			String propertyName = property.getAttribute("textContent");
			if (propertiesForCheck.contains(propertyName)){
				DeploymentPage.clickOnProperty(property);				
				int actualNumber = 0;
				if (propertyName.equals("initial_count")){
					actualNumber = baseNumber;
				} else if (propertyName.equals("max_vf_module_instances")) {
					actualNumber = baseNumber + 1;
				} else if (propertyName.equals("min_vf_module_instances")){
					actualNumber = baseNumber - 1;				
				}
				
				new PropertyPopup().insertPropertyDefaultValue(String.valueOf(actualNumber));
				new PropertyPopup().clickSave();
				assertTrue(DeploymentPage.getPropertyValueFromPropertiesList(propertyName).equals(String.valueOf(actualNumber)));
			}
		}
	}

	public static void negativeFlow(List<String> propertiesForCheck, List<WebElement> properties)
				throws Exception {
			int currentMaxValue = Integer.valueOf(DeploymentPage.getPropertyValueFromPropertiesList("max_vf_module_instances"));
			int currentMinValue = Integer.valueOf(DeploymentPage.getPropertyValueFromPropertiesList("min_vf_module_instances"));
			int currentInitialValue = Integer.valueOf(DeploymentPage.getPropertyValueFromPropertiesList("initial_count"));		
			PropertyPopup propertyPopupObj = new PropertyPopup();
			
			for(WebElement property : properties){
				String propertyName = property.getAttribute("textContent");
				if (propertiesForCheck.contains(propertyName)){
					DeploymentPage.clickOnProperty(property);				
					if (propertyName.equals("initial_count")){
						
						propertyPopupObj.insertPropertyDefaultValue(String.valueOf(currentMaxValue + 1));
						ServiceVerificator.verifyErrorPresentAndSaveDisabled();
						propertyPopupObj.insertPropertyDefaultValue(String.valueOf(currentMinValue - 1));
						ServiceVerificator.verifyErrorPresentAndSaveDisabled();
						propertyPopupObj.insertPropertyDefaultValue(String.valueOf(0));
						ServiceVerificator.verifyErrorPresentAndSaveDisabled();
						
					} else if (propertyName.equals("max_vf_module_instances")) {
						
						propertyPopupObj.insertPropertyDefaultValue(String.valueOf(currentInitialValue - 1));
						ServiceVerificator.verifyErrorPresentAndSaveDisabled();
						propertyPopupObj.insertPropertyDefaultValue(String.valueOf(currentMinValue - 1));
						ServiceVerificator.verifyErrorPresentAndSaveDisabled();
						propertyPopupObj.insertPropertyDefaultValue(String.valueOf(0));
						verifyErrorPresentAndSaveDisabled();
						
					} else if (propertyName.equals("min_vf_module_instances")){
						
						propertyPopupObj.insertPropertyDefaultValue(String.valueOf(currentInitialValue + 1));
						ServiceVerificator.verifyErrorPresentAndSaveDisabled();
						propertyPopupObj.insertPropertyDefaultValue(String.valueOf(currentMaxValue + 1));
						ServiceVerificator.verifyErrorPresentAndSaveDisabled();				
					}				
									
					new PropertyPopup().clickCancel();
				}
			}
		}

	public static void verifyErrorPresentAndSaveDisabled() throws Exception{
		assertTrue(DeploymentPage.isPropertySaveButtonDisabled());
		assertTrue(DeploymentPage.getPropertyErrorValidationMessdge().size() == 1);
	}

	public static void moveMetadataPropertiesArtifactSection(int offset) throws InterruptedException {
		WebElement dragLineElement = GeneralUIUtils.getElementByCSS("div.rg-top");
		GeneralUIUtils.dragAndDropElementByY(dragLineElement, offset);
	}
	
    
}
