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

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;


public class ResourceGeneralPage extends GeneralPageElements {

	public ResourceGeneralPage() {
		super();
	}

	public static WebElement getNameField() {
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ResourceMetadataEnum.RESOURCE_NAME.getValue());
	}

	public static WebElement getDescriptionField() {
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ServiceMetadataEnum.DESCRIPTION.getValue());
	}

	public static String getCategoryDataTestsIdAttribute() {
		return DataTestIdEnum.ResourceMetadataEnum.CATEGORY.getValue();
	}

	public static WebElement getVendorNameField() {
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ResourceMetadataEnum.VENDOR_NAME.getValue());
	}

	public static WebElement getVendorReleaseField() {
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ResourceMetadataEnum.VENDOR_RELEASE.getValue());
	}

	public static WebElement getTagsField() {
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ResourceMetadataEnum.TAGS.getValue());
	}

	public static WebElement getContactIdField() {
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ResourceMetadataEnum.CONTACT_ID.getValue());
	}

	/***************************************************************/

	public static String getNameText() {
		return getNameField().getAttribute("value");
	}

	public static void defineName(String resourceName) {
		WebElement resourceNameTextbox = getNameField();
		resourceNameTextbox.clear();
		resourceNameTextbox.sendKeys(resourceName);
	}
	
	public static void defineNameWithPaste() {
		defineTextBoxWithPaste(getNameField());
	}

	public static String getDescriptionText() {
		return getDescriptionField().getAttribute("value");
	}

	public static void defineDescription(String description) {
		WebElement descriptionTextbox = getDescriptionField();
		descriptionTextbox.clear();
		descriptionTextbox.sendKeys(description);
	}
	
	public static void defineDescriptionWithPaste() {
		defineTextBoxWithPaste(getDescriptionField());
	}

	public static String getVendorNameText() {
		return getVendorNameField().getAttribute("value");
	}

	public static void defineVendorName(String vendorName) {
		WebElement vendorNameTextbox = getVendorNameField();
		vendorNameTextbox.clear();
		vendorNameTextbox.sendKeys(vendorName);
	}
	
	public static void defineVendorNameWithPaste() {
		defineTextBoxWithPaste(getVendorNameField());
	}

	public static String getVendorReleaseText() {
		return getVendorReleaseField().getAttribute("value");
	}

	public static void defineVendorRelease(String vendorRelease) {
		WebElement vendorReleaseTextbox = getVendorReleaseField();
		vendorReleaseTextbox.clear();
		vendorReleaseTextbox.sendKeys(vendorRelease);
	}
	
	public static void defineVendorReleaseWithPaste() {
		defineTextBoxWithPaste(getVendorReleaseField());
	}

	public static void defineTag(String resourceTags) {
		WebElement tagTextbox = getTagsField();
		tagTextbox.clear();
		tagTextbox.sendKeys(resourceTags);
		tagTextbox.sendKeys(Keys.ENTER);
	}

	public static void defineTagsList(ComponentReqDetails component, String[] tags) {
		List<String> taglist = new ArrayList<String>();
		WebElement resourceTagsTextbox = getTagsField();
		for (String tag : tags) {
			resourceTagsTextbox.clear();
			resourceTagsTextbox.sendKeys(tag);
			GeneralUIUtils.sleep(500);
			resourceTagsTextbox.sendKeys(Keys.ENTER);
			taglist.add(tag);
		}
		component.getTags().addAll(taglist);
	}
	
	public static void defineTagsListWithPaste() {
		List<String> taglist = new ArrayList<String>();
		WebElement resourceTagsTextbox = getTagsField();
		defineTextBoxWithPaste(resourceTagsTextbox);
		resourceTagsTextbox.sendKeys(Keys.ENTER);
	}

	public static void defineCategory(String category) {
		GeneralUIUtils.getSelectList(category, getCategoryDataTestsIdAttribute());
	}

	public static String getContactIdText() {
		return getContactIdField().getAttribute("value");
	}

	public static void defineContactId(String userId) {
		WebElement contactIdTextbox = getContactIdField();
		contactIdTextbox.clear();
		contactIdTextbox.sendKeys(userId);
		GeneralUIUtils.waitForLoader();
	}
	
	public static List<WebElement> getElementsFromTagsTable(){
		return GeneralUIUtils.getWebElementsListByTestID(DataTestIdEnum.ResourceMetadataEnum.TAGS_TABLE.getValue());
	}
	
	public static void defineTextBoxWithPaste(WebElement textBox) {
		textBox.clear();
		textBox.sendKeys(Keys.CONTROL + "v");
		GeneralUIUtils.ultimateWait();
	}
	
}
