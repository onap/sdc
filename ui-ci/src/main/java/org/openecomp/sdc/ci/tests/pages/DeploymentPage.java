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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.DeploymentScreen;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

public class DeploymentPage  {
	
	public DeploymentPage() {
		super();		
	}
	
	public static List<WebElement> getGroupMembersList(String instanceName) {
		List<WebElement> propertyRows = null;
		clickOnModuleName(instanceName);
		propertyRows = getVisibleMembers();
		return propertyRows;
	}

	public static void clickOnModuleName(String instanceName) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s", instanceName));
		GeneralUIUtils.clickOnElementByText(instanceName);		
		GeneralUIUtils.ultimateWait();
	}

	public static List<WebElement> getVisibleMembers() {
		List<WebElement> instancesFromTable = GeneralUIUtils.getDriver().findElements(By.cssSelector("div[class^='hierarchy-module-member-list']"));
		for (WebElement instance : instancesFromTable){
			Object parentAttributes = GeneralUIUtils.getAllElementAttributes(instance);
			if (!parentAttributes.toString().contains("hidden")){
				return instance.findElements(By.cssSelector("div[class^='expand-collapse-sub-title']"));
			}
		}
		return null;
	}
	
	public static void clickOnProperties() throws Exception{
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on Properties button"));
		GeneralUIUtils.clickOnElementByCSS(DataTestIdEnum.DeploymentScreen.BUTTON_PROPERTIES.getValue());
		GeneralUIUtils.ultimateWait();	
	}
	
	public static void clickOnArtifacts() throws Exception{
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on Artifacts button"));
		GeneralUIUtils.clickOnElementByCSS(DataTestIdEnum.DeploymentScreen.BUTTON_ARTIFACTS.getValue());
		GeneralUIUtils.ultimateWait();	
	}
	
	public static void clickOnSaveButton(){
		clickInDeployment(DataTestIdEnum.DeploymentScreen.SAVE);
		GeneralUIUtils.waitForElementInVisibilityByTestId(By.className("popover-inner"));
	}
	
    public static void clickOnCancelButton(){
    	clickInDeployment(DataTestIdEnum.DeploymentScreen.CANCEL);
    	GeneralUIUtils.waitForElementInVisibilityByTestId(By.className("popover-inner"));
	}
    
    public static void clickOnXIcon(){
    	clickInDeployment(DataTestIdEnum.DeploymentScreen.X_BUTTON);
    	GeneralUIUtils.waitForElementInVisibilityByTestId(By.className("popover-inner"));
    }
    
    public static void clickOnEditIcon(){
    	clickInDeployment(DataTestIdEnum.DeploymentScreen.PENCIL_ICON);
    }
    
    public static void clickOnProperty(WebElement property) {
    	SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s propertie ...", property.getText()));
    	property.click();
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPageEnum.POPUP_FORM.getValue());
	}
    
    private static void clickInDeployment(DataTestIdEnum.DeploymentScreen element){
    	SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s ...", element.getValue()));
    	GeneralUIUtils.clickOnElementByTestId(element.getValue());
    	GeneralUIUtils.ultimateWait();
    }
    
    public static List<WebElement> getPropertyNames() throws Exception{
    	clickOnProperties();
    	return GeneralUIUtils.getElementsByCSS(DataTestIdEnum.DeploymentScreen.PROPERTY_NAMES.getValue());
    }
    
    public static List<WebElement> getArtifactNames() throws Exception{
    	clickOnArtifacts();
    	return GeneralUIUtils.getInputElements(DataTestIdEnum.DeploymentScreen.ARTIFACT_NAME.getValue());
    }
    
    public static String updateModuleName(String currentModuleName, String newModuleName) throws Exception {
		GeneralUIUtils.ultimateWait();
		ResourceUIUtils.clickOnElementByText(currentModuleName, null);
		GeneralUIUtils.ultimateWait();
		clickOnEditIcon();
		WebElement moduleNameField = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DeploymentScreen.NAME_INPUT.getValue());
		String oldModuleName = moduleNameField.getAttribute("value");
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating %s module name ...", currentModuleName));
		moduleNameField.clear();
		GeneralUIUtils.ultimateWait();
		moduleNameField.sendKeys(newModuleName);
		GeneralUIUtils.ultimateWait();
		clickOnSaveButton();	
		String newReconstructedModuleName = reconstructModuleName(currentModuleName.split("\\.\\."), newModuleName);
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Name of element instance changed from %s to %s", currentModuleName, newReconstructedModuleName));
		return oldModuleName;
	}
    
    public static String reconstructModuleName(String[] splittedName, String middleName){
    	int i = 0;
    	StringBuilder builder = new StringBuilder();
    	for(String s : splittedName) {
    	    if (i == 1){
    	    	builder.append(middleName);
    	    } else {
    	    	builder.append(s);
    	    }
    	    if (i < 2 ){
    	    	builder.append("..");
    	    }
    	    i++;
    	}
    	return builder.toString();
    }
    
    public static List<WebElement> getVisibleModulesService() {
		List<WebElement> instancesFromTable = GeneralUIUtils.getDriver().findElements(By.cssSelector("div[class^='hierarchy-modules-list']"));
		for (WebElement instance : instancesFromTable){
			Object parentAttributes = GeneralUIUtils.getAllElementAttributes(instance);
			if (!parentAttributes.toString().contains("hidden")){
				return instance.findElements(By.cssSelector("span[class^='expand-collapse-title-text']"));
			}
		}
		return null;
	}
    
    public static List<WebElement> getInstanceModulesList(String instanceName) {
		List<WebElement> propertyRows = null;
		ResourceUIUtils.clickOnElementByText(instanceName, null);
		GeneralUIUtils.ultimateWait();
		propertyRows = getVisibleModulesService();
		return propertyRows;
	}
    
    public static String getGroupVersion() throws Exception{
    	return GeneralUIUtils.getElementsByCSS("div[data-tests-id='selected-module-version']").get(0).getText();
    }
    
    public static String getModuleID() throws Exception{
    	return GeneralUIUtils.getElementsByCSS("div[data-tests-id='selected-module-group-uuid'] span[class^='small-font']").get(0).getText();
    }   
    
    public static Map<String, HashMap<String, String>> collectMetaDataFromUI() throws Exception{
		ResourceGeneralPage.getLeftMenu().moveToDeploymentViewScreen();
		Map<String, HashMap<String, String>> deploymentViewMetaData = new HashMap<String, HashMap<String, String>>();
		List<WebElement> moduleRowsFromTable = GeneralUIUtils.getElementsByCSS("span[class^='expand-collapse-title-text']");
		for(WebElement moduleRow :moduleRowsFromTable){
			HashMap<String, String> tempGroupMap = new HashMap<String, String>();
			String moduleRowText = moduleRow.getText();
			GeneralUIUtils.clickOnElementByText(moduleRowText);
			tempGroupMap.put("moduleID", getModuleID());
			tempGroupMap.put("version", DeploymentPage.getGroupVersion().split(":")[1].trim());
			deploymentViewMetaData.put(moduleRowText.split("\\.\\.")[1], tempGroupMap);
			GeneralUIUtils.clickOnElementByText(moduleRowText);
		}
		return deploymentViewMetaData;
	}
    
    public static void updateAndCancel(String newModuleName, DataTestIdEnum.DeploymentScreen buttonToClick){
    	WebElement moduleNameField = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DeploymentScreen.NAME_INPUT.getValue());
		String oldModuleName = moduleNameField.getAttribute("value");
		moduleNameField.clear();
		GeneralUIUtils.ultimateWait();
		moduleNameField.sendKeys(newModuleName);
		GeneralUIUtils.ultimateWait();
		if (buttonToClick.equals(DataTestIdEnum.DeploymentScreen.X_BUTTON))
		    clickOnXIcon();
		else
			clickOnCancelButton();
    }
    
    public static String getPropertyValueFromPropertiesList(String property) throws InterruptedException{
    	List<WebElement> propertyDataElements = GeneralUIUtils.getElementsByCSS("div[class^='list-item property-data']");
    	for(WebElement propertyDataElement: propertyDataElements){
    		WebElement propertyNameElement = GeneralUIUtils.getElementfromElementByCSS(propertyDataElement, DeploymentScreen.PROPERTY_NAMES.getValue());
    		if (propertyNameElement.getText().equals(property)){
    			WebElement propertyValueElement = GeneralUIUtils.getElementfromElementByCSS(propertyDataElement, String.format("div[data-tests-id='%s']", DeploymentScreen.PROPERTY_SCHEMA_TYPE.getValue()));
    			return propertyValueElement.getText().trim().split(":")[1].trim();
    		}
    	}
    	return null;
    }
    
    public static List<WebElement> getPropertyErrorValidationMessdge() throws Exception{
    	List<WebElement> propertyErrorElements = GeneralUIUtils.getElementsByCSS("div[class='input-error'] span[class='ng-scope']");
    	return propertyErrorElements;
    }
    
    public static boolean isPropertySaveButtonDisabled(){
    	WebElement saveButtonElement = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.SAVE.getValue());
    	return GeneralUIUtils.isElementDisabled(saveButtonElement);
    }
    
    
    
}
