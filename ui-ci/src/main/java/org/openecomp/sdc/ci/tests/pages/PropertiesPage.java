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
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class PropertiesPage extends GeneralPageElements {

	public PropertiesPage() {
		super();
	}

	public static List<WebElement> getElemenetsFromTable() {
		return GeneralUIUtils.getWebElementsListByDataTestId(DataTestIdEnum.PropertiesPageEnum.PROPERTY_ROW.getValue());
	}

	public static void clickAddPropertyArtifact() {
		GeneralUIUtils.getWebButton(DataTestIdEnum.PropertiesPageEnum.ADD_NEW_PROPERTY.getValue()).click();
	}

	public static void clickEditPropertyArtifact(String propertyName) {
		GeneralUIUtils.getWebButton(DataTestIdEnum.PropertiesPageEnum.EDIT_PROPERTY.getValue() + propertyName).click();
	}

	public static void clickDeletePropertyArtifact(String propertyName) {
		GeneralUIUtils.getWebButton(DataTestIdEnum.PropertiesPageEnum.DELETE_PROPERTY.getValue() + propertyName)
				.click();
	}

	public static void clickOnProperty(String propertyName) {
		GeneralUIUtils.getWebButton(propertyName).click();
	}

	public static PropertyPopup getPropertyPopup() {
		return new PropertyPopup();
	}

}
