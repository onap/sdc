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

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ComponentType;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

public class ResourceGeneralPage extends GeneralPageElements {

	public ResourceGeneralPage() {
		super();
	}

	private static WebElement getNameField() {
		return GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ResourceMetadataEnum.RESOURCE_NAME.getValue());
	}

	private static WebElement getDescriptionField() {
		return GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ServiceMetadataEnum.DESCRIPTION.getValue());
	}

	private static String getCategoryField() {
		return DataTestIdEnum.ResourceMetadataEnum.CATEGORY.getValue();
	}

	private static WebElement getVendorNameField() {
		return GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ResourceMetadataEnum.VENDOR_NAME.getValue());
	}

	private static WebElement getVendorReleaseField() {
		return GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.ResourceMetadataEnum.VENDOR_RELEASE.getValue());
	}

	private static WebElement getTagsField() {
		return GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ResourceMetadataEnum.TAGS.getValue());
	}

	private static WebElement getUserIdField() {
		return GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ResourceMetadataEnum.CONTACT_ID.getValue());
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

	public static String getDescriptionText() {
		return getDescriptionField().getAttribute("value");
	}

	public static void defineDescription(String description) {
		WebElement descriptionTextbox = getDescriptionField();
		descriptionTextbox.clear();
		descriptionTextbox.sendKeys(description);
	}

	public static String getVendorNameText() {
		return getVendorNameField().getAttribute("value");
	}

	public static void defineVendorName(String vendorName) {
		WebElement vendorNameTextbox = getVendorNameField();
		vendorNameTextbox.clear();
		vendorNameTextbox.sendKeys(vendorName);
	}

	public static String getVendorReleaseText() {
		return getVendorReleaseField().getAttribute("value");
	}

	public static void defineVendorRelease(String vendorRelease) {
		WebElement vendorReleaseTextbox = getVendorReleaseField();
		vendorReleaseTextbox.clear();
		vendorReleaseTextbox.sendKeys(vendorRelease);
	}

	public static void defineTag(String resourceTags) {
		WebElement tagTextbox = getTagsField();
		tagTextbox.clear();
		tagTextbox.sendKeys(resourceTags);
		tagTextbox.sendKeys(Keys.ENTER);
	}

	public static void defineTagsList(ComponentReqDetails resource, String[] resourceTags) {
		List<String> taglist = new ArrayList<String>();
		;
		WebElement resourceTagsTextbox = getTagsField();
		for (String tag : resourceTags) {
			resourceTagsTextbox.clear();
			resourceTagsTextbox.sendKeys(tag);
			GeneralUIUtils.sleep(1000);
			resourceTagsTextbox.sendKeys(Keys.ENTER);
			taglist.add(tag);
		}
		resource.setTags(taglist);
	}

	public static void defineCategory(String category) {
//		GeneralUIUtils.getSelectList(category, getCategoryField());
		
		Actions action = new Actions(GeneralUIUtils.getDriver());
		action.click(GeneralUIUtils.getWebElementByDataTestId(getCategoryField()));
		action.sendKeys(category).perform();
	}

	public static String getUserIdContactText() {
		return getUserIdField().getAttribute("value");
	}

	public static void defineUserIdContact(String userId) {
		WebElement contactIdTextbox = getUserIdField();
		contactIdTextbox.clear();
		contactIdTextbox.sendKeys(userId);
	}

}
