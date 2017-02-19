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

package org.openecomp.sdc.ci.tests.utilities;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.datatypes.ServiceCategoriesNameEnum;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.relevantcodes.extentreports.LogStatus;

public class ServiceUIUtils {

	protected static WebDriver driver;

	public ServiceUIUtils(TestName name, String className) {
		super();
	}

	public static String defineServiceName(String Name) {
		WebElement serviceName = GeneralUIUtils.getWebElementWaitForVisible("name");
		serviceName.clear();
		serviceName.sendKeys(Name);
		return Name;
	}

	public void moveResourceInstanceToCanvasUI() throws Exception {
		List<WebElement> moveResource = driver.findElements(By.className("sprite-resource-icons"));
		WebElement moveResourceToCanvasResourceOne = moveResource.get(0);
		// WebElement moveResource =
		// driver.findElement(By.className("sprite-resource-icons"));
		Actions action = new Actions(driver);
		action.moveToElement(moveResourceToCanvasResourceOne);
		action.clickAndHold(moveResourceToCanvasResourceOne);
		action.moveByOffset(635, 375);
		action.release();
		action.perform();
		WebElement moveResourceToCanvasResourceTwo = moveResource.get(1);
		action.moveToElement(moveResourceToCanvasResourceTwo);
		action.clickAndHold(moveResourceToCanvasResourceTwo);
		action.moveByOffset(535, 375);
		action.release();
		action.perform();
		WebElement moveResourceToCanvasResourceTree = moveResource.get(2);
		action.moveToElement(moveResourceToCanvasResourceTree);
		action.clickAndHold(moveResourceToCanvasResourceTree);
		action.moveByOffset(435, 375);
		action.release();
		action.perform();
		Thread.sleep(2000);
	}

	public static String catalogFilterServiceCategoriesChecBox(ServiceCategoriesNameEnum enumName) throws Exception {
		String Type = null;
		GeneralUIUtils.getWebElementWaitForVisible(enumName.getValue()).click();
		return Type;
	}

	public static List<String> catalogServiceTypeChecBox(ServiceCategoriesNameEnum enumtype) throws Exception {
		List<String> categories = null;
		switch (enumtype) {
		case NETWORK_L13:
			GeneralUIUtils.getWebElementWaitForVisible(enumtype.getValue()).click();
			categories = Arrays.asList("network_l_1-3");
			break;
		case NETWORKL4:
			GeneralUIUtils.getWebElementWaitForVisible(enumtype.getValue()).click();
			categories = Arrays.asList("network_l_4 ");
			break;
		case MOBILITY:
			GeneralUIUtils.getWebElementWaitForVisible(enumtype.getValue()).click();
			categories = Arrays.asList("mobility");
			break;
		case VOIPCALL_CONTROL:
			GeneralUIUtils.getWebElementWaitForVisible(enumtype.getValue()).click();
			categories = Arrays.asList("call_controll ");
			break;
		}
		return categories;
	}

	public static WebElement waitToNextButtonEnabled() {
		return GeneralUIUtils.getWebButton("Next");
	}

	public static WebElement waitToFinishButtonEnabled() {
		return GeneralUIUtils.getWebButton("Finish");
	}

	public static WebElement deleteServiceInUI() {

		return GeneralUIUtils.getWebButton("deleteVersion");
	}

	// get the service view data for validate.
	// created by tedy.
	public static void getServiceGeneralInfo(ServiceReqDetails service, User user) throws InterruptedException {
		Thread.sleep(2000);
		String version = GeneralUIUtils.getSelectList(null, "versionHeader").getFirstSelectedOption().getText()
				.substring(1);
		String name = GeneralUIUtils.getWebElementWaitForVisible("name").getAttribute("value");
		String description = GeneralUIUtils.getWebElementWaitForVisible("description").getAttribute("value");
		String category = GeneralUIUtils.getSelectList(null, "selectGeneralCategory").getFirstSelectedOption()
				.getText();
		List<WebElement> tags = GeneralUIUtils.waitForElementsListVisibility("i-sdc-tag-text");
		String type = GeneralUIUtils.waitForElementsListVisibility("type").get(1).getText();
		int index = type.lastIndexOf(":");
		System.out.println(type.substring(0, index));
		String contactId = GeneralUIUtils.getWebElementWaitForVisible("contactId").getAttribute("value");
		String projectCode = GeneralUIUtils.getWebElementWaitForVisible("projectCode").getAttribute("value");
		System.out.println(service.getVersion());
		assertTrue(service.getVersion().equals(version));
		assertTrue(service.getName().equals(name));
		assertTrue(service.getDescription().equals(description));
		assertTrue(service.getCategories().get(0).getName().equals(category));
		System.out.println(service.getContactId());
		assertTrue(service.getContactId().equals(contactId));
		assertTrue(service.getProjectCode().equals(projectCode));
		for (int i = 0; i < tags.size(); i++) {
			assertEquals(service.getTags().get(i), tags.get(i).getText());
		}

	}

	public static void defineTagsList(ServiceReqDetails service, String[] serviceTags) {
		List<String> taglist = new ArrayList<String>();
		;
		WebElement serviceTagsTextbox = GeneralUIUtils.getWebElementWaitForVisible("i-sdc-tag-input");
		for (String tag : serviceTags) {
			serviceTagsTextbox.clear();
			serviceTagsTextbox.sendKeys(tag);
			serviceTagsTextbox.sendKeys(Keys.ENTER);
			taglist.add(tag);
		}
		taglist.add(0, service.getName());
		service.setTags(taglist);
	}

	public static Select defineServiceCategory(String category) {

		return GeneralUIUtils.getSelectList(category, "selectGeneralCategory");
	}

	public static void defineServiceProjectCode(String projectCode) {
		WebElement projectCodeTextbox = GeneralUIUtils.getWebElementWaitForVisible("projectCode");
		projectCodeTextbox.clear();
		projectCodeTextbox.sendKeys(projectCode);
	}

	public static void selectRandomResourceIcon() throws Exception {
		GeneralUIUtils.moveToStep(StepsEnum.ICON);
		WebDriverWait wait = new WebDriverWait(driver, 6);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(@data-tests-id, 'iconBox')]")));
		List<WebElement> iconElement = driver.findElements(By.xpath("//*[contains(@data-tests-id, 'iconBox')]"));
		iconElement.get(0).click();
	}

	public static String defineDescription(String description) {
		WebElement descriptionTextbox = GeneralUIUtils.getWebElementWaitForVisible("description");
		descriptionTextbox.clear();
		descriptionTextbox.sendKeys(description);
		return description;
	}

	public static void defineContactId(String userId) {
		WebElement contactId = GeneralUIUtils.getWebElementWaitForVisible("contactId");
		contactId.clear();
		contactId.sendKeys(userId);
	}

	public static WebElement clickAddArtifact() {

		return GeneralUIUtils.getWebButton("addArtifactButton");
	}

	public static WebElement getArtifactName() {
		return GeneralUIUtils.getWebButton("artifactName");
	}

	public static WebElement getArtifactDetails() {
		return GeneralUIUtils.getWebButton("artifactDisplayName");
	}

	public static void fillServiceGeneralPage(ServiceReqDetails service, User user) throws Exception {
		ServiceGeneralPage.defineName(service.getName());
		ServiceGeneralPage.defineDescription(service.getDescription());
		ServiceGeneralPage.defineCategory(service.getCategories().get(0).getName());
		ServiceGeneralPage.defineProjectCode(service.getProjectCode());
		ServiceGeneralPage.defineTagsList(service, new String[] { "This-is-tag", "another-tag" });

	}

	public static void createService(ServiceReqDetails service, User user) throws Exception, AWTException {

		ResourceUIUtils.moveToHTMLElementByClassName("w-sdc-dashboard-card-new");
		ResourceUIUtils.clickOnHTMLElementByDataTestId(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue());
		GeneralUIUtils.waitForLoader();
		fillServiceGeneralPage(service, user);
		GeneralUIUtils.clickCreateButton();
		SetupCDTest.getExtendTest().log(LogStatus.INFO, String.format("Service %s created", service.getName()));
	}

}
