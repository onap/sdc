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
import java.util.stream.Collectors;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.InputsScreenService;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.TestException;

import com.aventstack.extentreports.Status;

public class InputsPage extends GeneralPageElements {

	public InputsPage() {
		super();
		
	}

	public static List<WebElement> getInstancePropertiesList(String instanceName) {
		List<WebElement> propertyRows = null;
		GeneralUIUtils.clickOnElementByText(instanceName);
		GeneralUIUtils.ultimateWait();
		propertyRows = getVisibleProperites();
		return propertyRows;
	}

	public static List<WebElement> getVisibleProperites() {
		List<WebElement> instancesFromTable = GeneralUIUtils.getDriver().findElements(By.cssSelector("div[class^='vf-instance-list']"));
		for (WebElement instance : instancesFromTable){
			Object parentAttributes = GeneralUIUtils.getAllElementAttributes(instance);
			if (!parentAttributes.toString().contains("hidden")){
				return instance.findElements(By.className("property-row"));
			}
		}
		return null;
	}
	
	public static void addInputToService(String VFInstanceName, String propertyName) throws Exception{
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Adding property %s from VF instance %s to Service", propertyName, VFInstanceName));
		List<WebElement> instaceInputs = getInstanceInputsList(VFInstanceName);
		for(WebElement instancInput: instaceInputs){
			String actualPropertyName = instancInput.findElement(By.className("title-text")).getText();
			if (actualPropertyName.equals(propertyName) && clickOnVFInputCheckbox(instancInput)){
				clickOnAddInputButton();
			}
		}
	}

    public static List<WebElement> getInstanceInputsList(String instanceName) {
		List<WebElement> inputRows = null;
		GeneralUIUtils.clickOnElementByText(instanceName);
		GeneralUIUtils.ultimateWait();
		inputRows = getVisibleInputs(inputRows);
		return inputRows;
    }
    
	public static List<WebElement> getVisibleInputs(List<WebElement> inputRows) {
		List<WebElement> instancesFromTable = GeneralUIUtils.getDriver().findElements(By.cssSelector("div[class^='vf-instance-list']"));
		for (WebElement instance : instancesFromTable){
			Object parentAttributes = GeneralUIUtils.getAllElementAttributes(instance);
			if (!parentAttributes.toString().contains("hidden")){
				inputRows = instance.findElements(By.className("input-row"));
				break;
			}
		}
		return inputRows;
	}
	
	public static void clickOnAddInputButton(){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on Add Input aka Greater than symbol button"));
		GeneralUIUtils.clickOnElementByTestId(InputsScreenService.ADD_SELECTED_INPUTS_BTN.getValue());
		GeneralUIUtils.ultimateWait();		
	}
	
	public static boolean clickOnVFInputCheckbox(WebElement instancInput){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on VF instance input checkbox"));
		instancInput.findElement(By.className("tlv-checkbox-label")).click();
		GeneralUIUtils.ultimateWait();
		return instancInput.findElement(By.className("tlv-checkbox-i")).getAttribute("class").contains("ng-not-empty");
	}
	
	public static WebElement getServiceInput(String VFInstanceName, String propertyName) throws Exception{
		String expectedInputName = String.format("%s_%s", VFInstanceName.replace(" ", "").toLowerCase(), propertyName);
		List<WebElement> inputsFromTable = GeneralUIUtils.getElementsByCSS(InputsScreenService.SERVICE_INPUT_ROW.getValue());
	    for(WebElement inputFromTable: inputsFromTable){
	    	String actualInputName = inputFromTable.findElement(By.className("title-text")).getText();
	    	if(actualInputName.equals(expectedInputName)){
	    		return inputFromTable;
	    	}
	    }
	    throw new TestException(String.format("%s input don't exist", expectedInputName));
	}
	    
	public static void deleteServiceInput(String VFInstanceName, String propertyName) throws Exception{
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Deleting property %s in VF instance %s ", propertyName, VFInstanceName));
		WebElement serviceInput = getServiceInput(VFInstanceName, propertyName);
		serviceInput.findElement(By.cssSelector(InputsScreenService.DELETE_INPUT_BTN.getValue())).click();
		GeneralUIUtils.ultimateWait();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on OK button "));
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.GeneralElementsEnum.OK.getValue());
		GeneralUIUtils.ultimateWait();		
	}
	
	public static List<String> getVFCInstancesNamesFromTable() throws Exception{
		WebElement inputsTable = getInputsTable("VFC ");
	    return	 inputsTable.findElements(By.cssSelector("span[class^='title-text']")).stream().
	    		     map(e -> e.getText()).
	    		     collect(Collectors.toList());
	}
	
	public static WebElement getInputsTable(String tableName) throws Exception{
		 List<WebElement> tableElements = GeneralUIUtils.getElementsByCSS("div.table");
		 for(WebElement tableElement: tableElements){
			 String tableTitle = GeneralUIUtils.getElementfromElementByCSS(tableElement, "div.table-header").getText();
			 if (tableTitle.contains(tableName)){
				 return tableElement;
			 }
		 }
		 throw new TestException(String.format("Can't find %s table", tableName));
	}
	
	public static void clickOnProperty(String propertyName) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on property %s ", propertyName));
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.InputsScreenService.RESOURCE_INSTANCE_PROPERTY_NAME.getValue() + propertyName);
		GeneralUIUtils.ultimateWait();
	}
	
	
	
	
	
	

}
