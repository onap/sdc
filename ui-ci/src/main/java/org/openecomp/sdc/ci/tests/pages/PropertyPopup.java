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
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.WebElement;

public class PropertyPopup {

	public PropertyPopup() {
	}

	public void insertPropertyName(String name) {
		WebElement propertyNameField = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.PropertiesPageEnum.PROPERTY_NAME.getValue());
		propertyNameField.clear();
		propertyNameField.sendKeys(name);
	}

	public void insertPropertyDefaultValue(String value) {
		WebElement propertyValue = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.PropertiesPageEnum.PROPERTY_VALUE.getValue());
		propertyValue.clear();
		propertyValue.sendKeys(value);
	}

	public void insertPropertyDescription(String description) {
		WebElement propertyDescription = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.PropertiesPageEnum.PROPERTY_DESCRIPTION.getValue());
		propertyDescription.clear();
		propertyDescription.sendKeys(description);
	}

	public void selectPropertyType(String propertyType) {
		GeneralUIUtils.getSelectList(propertyType, DataTestIdEnum.PropertiesPageEnum.PROPERTY_TYPE.getValue());
	}

	public void clickAdd() {
		GeneralUIUtils.getWebButton(DataTestIdEnum.PropertiesPageEnum.ADD.getValue()).click();
		GeneralUIUtils.waitForLoader();
	}

	public void clickSave() {
		GeneralUIUtils.getWebButton(DataTestIdEnum.PropertiesPageEnum.SAVE.getValue()).click();
		GeneralUIUtils.waitForLoader();
	}

	public void clickCancel() {
		GeneralUIUtils.getWebButton(DataTestIdEnum.PropertiesPageEnum.CANCEL.getValue()).click();
		GeneralUIUtils.waitForLoader();
	}

	public void clickDone() {
		GeneralUIUtils.getWebButton(DataTestIdEnum.PropertiesPageEnum.DONE.getValue()).click();
		GeneralUIUtils.waitForLoader();
	}

}
