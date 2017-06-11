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

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.aventstack.extentreports.Status;

public class PropertyPopup {
	

	public PropertyPopup() {
	}

	public boolean getPopupForm(){
		return GeneralUIUtils.waitForElementInVisibilityByTestId(DataTestIdEnum.PropertiesPageEnum.POPUP_FORM.getValue(), 60);
	}
	
	public void insertPropertyName(String name) {
		WebElement propertyNameField = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_NAME.getValue());
		propertyNameField.clear();
		propertyNameField.sendKeys(name);
	}

	public void insertPropertyDefaultValue(String value) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Inserting to property default value: %s ", value));
		WebElement selectedType = new Select(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_TYPE.getValue())).getFirstSelectedOption();
		if(selectedType.getText().equals("boolean")) {
			GeneralUIUtils.getSelectList(value, DataTestIdEnum.PropertiesPopupEnum.PROPERTY_BOOLEAN_VALUE.getValue());
		} else {
			WebElement propertyValue = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_VALUE.getValue());
			propertyValue.clear();
			propertyValue.sendKeys(value);
		}	
		
		GeneralUIUtils.ultimateWait();
	}

	public void insertPropertyDescription(String description) {
		WebElement propertyDescription = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_DESCRIPTION.getValue());
		propertyDescription.clear();
		propertyDescription.sendKeys(description);
	}

	public void selectPropertyType(String propertyType) {
		boolean isEntrySchemaDisplayed;
		try{
			GeneralUIUtils.getSelectList(propertyType, DataTestIdEnum.PropertiesPopupEnum.PROPERTY_TYPE.getValue());
			isEntrySchemaDisplayed = GeneralUIUtils.getDriver().findElement(By.xpath(DataTestIdEnum.PropertiesPopupEnum.ENTRY_SCHEMA.getValue())).isDisplayed();
			if (isEntrySchemaDisplayed){
				PropertiesPage.getPropertyPopup().selectEntrySchema(propertyType);
			}
		}
		catch(NoSuchElementException e){
			
		}
	}
	
	public void selectEntrySchema(String propertyType){
		GeneralUIUtils.getSelectList(propertyType, DataTestIdEnum.PropertiesPopupEnum.ENTRY_SCHEMA.getValue());
	}

	public void clickAdd() {
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesPopupEnum.ADD.getValue());
		GeneralUIUtils.ultimateWait();
	}

	public void clickSave() {
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesPopupEnum.SAVE.getValue());
		getPopupForm();
	}

	public void clickCancel() {
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesPopupEnum.CANCEL.getValue());
		GeneralUIUtils.ultimateWait();
	}

	public void clickDone() {
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesPopupEnum.DONE.getValue());
		GeneralUIUtils.ultimateWait();
	}
	
	public void selectPropertyRadioButton(String propertyName) {
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_RADIO_BUTTON_CONTAINER.getValue() + propertyName).findElement(By.className(DataTestIdEnum.PropertiesPopupEnum.RADIO_BUTTON_CLASS.getValue())).click();
	}

}
