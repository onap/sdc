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
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.datatypes.ServiceCategoriesNameEnum;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.Status;

public class ServiceUIUtils {

	protected static WebDriver driver;

	public ServiceUIUtils(TestName name, String className) {
		super();
	}

	public static String defineServiceName(String Name) {
		WebElement serviceName = GeneralUIUtils.getWebElementByTestID("name");
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
		GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
		return Type;
	}

	public static List<String> catalogServiceTypeChecBox(ServiceCategoriesNameEnum enumtype) throws Exception {
		List<String> categories = null;
		switch (enumtype) {
		case NETWORK_L13:
			GeneralUIUtils.getWebElementByTestID(enumtype.getValue()).click();
			categories = Arrays.asList("network_l_1-3");
			break;
		case NETWORKL4:
			GeneralUIUtils.getWebElementByTestID(enumtype.getValue()).click();
			categories = Arrays.asList("network_l_4 ");
			break;
		case MOBILITY:
			GeneralUIUtils.getWebElementByTestID(enumtype.getValue()).click();
			categories = Arrays.asList("mobility");
			break;
		case VOIPCALL_CONTROL:
			GeneralUIUtils.getWebElementByTestID(enumtype.getValue()).click();
			categories = Arrays.asList("call_controll ");
			break;
		}
		return categories;
	}

	public static WebElement waitToNextButtonEnabled() {
		return GeneralUIUtils.getWebElementByTestID("Next");
	}

	public static WebElement waitToFinishButtonEnabled() {
		return GeneralUIUtils.getWebElementByTestID("Finish");
	}

	public static WebElement deleteServiceInUI() {

		return GeneralUIUtils.getWebElementByTestID("deleteVersion");
	}

	// get the service view data for validate.
	// created by tedy.
	public static void getServiceGeneralInfo(ServiceReqDetails service, User user) throws InterruptedException {
		Thread.sleep(2000);
		String version = GeneralUIUtils.getSelectList(null, "versionHeader").getFirstSelectedOption().getText()
				.substring(1);
		String name = GeneralUIUtils.getWebElementByTestID("name").getAttribute("value");
		String description = GeneralUIUtils.getWebElementByTestID("description").getAttribute("value");
		String category = GeneralUIUtils.getSelectList(null, "selectGeneralCategory").getFirstSelectedOption()
				.getText();
		List<WebElement> tags = GeneralUIUtils.getWebElementsListByTestID("i-sdc-tag-text");
		String type = GeneralUIUtils.getWebElementsListByTestID("type").get(1).getText();
		int index = type.lastIndexOf(":");
		System.out.println(type.substring(0, index));
		String attContact = GeneralUIUtils.getWebElementByTestID("attContact").getAttribute("value");
		String pmatt = GeneralUIUtils.getWebElementByTestID("pmatt").getAttribute("value");
		System.out.println(service.getVersion());
		assertTrue(service.getVersion().equals(version));
		assertTrue(service.getName().equals(name));
		assertTrue(service.getDescription().equals(description));
		assertTrue(service.getCategories().get(0).getName().equals(category));
		System.out.println(service.getContactId());
		assertTrue(service.getContactId().equals(attContact));
		assertTrue(service.getProjectCode().equals(pmatt));
		for (int i = 0; i < tags.size(); i++) {
			assertEquals(service.getTags().get(i), tags.get(i).getText());
		}

	}

//	public static void defineTagsList(ServiceReqDetails service, String[] serviceTags) {
//		List<String> taglist = new ArrayList<String>();		
//		WebElement serviceTagsTextbox = GeneralUIUtils.getWebElementByTestID("i-sdc-tag-input");
//		for (String tag : serviceTags) {
//			serviceTagsTextbox.clear();
//			serviceTagsTextbox.sendKeys(tag);
//			GeneralUIUtils.sleep(1000);
//			serviceTagsTextbox.sendKeys(Keys.ENTER);
//			taglist.add(tag);
//		}
//		taglist.add(0, service.getName());
//		service.setTags(taglist);
//	}
	
	public static void defineTagsList2(List<String> serviceTags){
		WebElement serviceTagsTextbox = GeneralUIUtils.getWebElementByTestID("i-sdc-tag-input");
		for (String tag : serviceTags) {
			serviceTagsTextbox.clear();
			serviceTagsTextbox.sendKeys(tag);
			GeneralUIUtils.waitForAngular();
			serviceTagsTextbox.sendKeys(Keys.ENTER);
		}
	}

	public static Select defineServiceCategory(String category) {

		return GeneralUIUtils.getSelectList(category, "selectGeneralCategory");
	}

	public static void defineServicePmatt(String pmatt) {
		WebElement attPmattTextbox = GeneralUIUtils.getWebElementByTestID("pmatt");
		attPmattTextbox.clear();
		attPmattTextbox.sendKeys(pmatt);
	}

	public static void selectRandomResourceIcon() throws Exception {
		GeneralUIUtils.moveToStep(StepsEnum.ICON);
		WebDriverWait wait = new WebDriverWait(driver, 6);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(@data-tests-id, 'iconBox')]")));
		List<WebElement> iconElement = driver.findElements(By.xpath("//*[contains(@data-tests-id, 'iconBox')]"));
		iconElement.get(0).click();
	}

	public static String defineDescription(String description) {
		WebElement descriptionTextbox = GeneralUIUtils.getWebElementByTestID("description");
		descriptionTextbox.clear();
		descriptionTextbox.sendKeys(description);
		return description;
	}

	public static void defineContactId(String userId) {
		WebElement attContact = GeneralUIUtils.getWebElementByTestID("attContact");
		attContact.clear();
		attContact.sendKeys(userId);
	}

	public static WebElement clickAddArtifact() {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking Add Artifact button"));
		return GeneralUIUtils.getWebElementByTestID("addArtifactButton");
	}

	public static WebElement getArtifactName() {
		return GeneralUIUtils.getWebElementByTestID("artifactName");
	}

	public static WebElement getArtifactDetails() {
		return GeneralUIUtils.getWebElementByTestID("artifactDisplayName");
	}

	public static void fillServiceGeneralPage(ServiceReqDetails service, User user) throws Exception {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Fill in metadata values in general page"));
		ServiceGeneralPage.defineName(service.getName());
		ServiceGeneralPage.defineDescription(service.getDescription());
		ServiceGeneralPage.defineCategory(service.getCategories().get(0).getName());
		ServiceGeneralPage.defineProjectCode(service.getProjectCode());
		defineTagsList2(service.getTags());
		ServiceGeneralPage.defineContactId(service.getContactId());
		GeneralUIUtils.clickSomewhereOnPage();		
	}
    
	public static void createService(ServiceReqDetails service, User user) throws Exception, AWTException {
		clickAddService();
		fillServiceGeneralPage(service, user);
		GeneralPageElements.clickCreateButton();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("The service %s was created", service.getName()));
	}
	
	public static void setServiceCategory(ServiceReqDetails service, ServiceCategoriesEnum category){
		CategoryDefinition categoryDefinition = new CategoryDefinition();
		categoryDefinition.setName(category.getValue());
		List<CategoryDefinition> categories = new ArrayList<>();
		categories.add(categoryDefinition);
		service.setCategories(categories);
	}
	
	public static void createServiceWithDefaultTagAndUserId(ServiceReqDetails service, User user) {
		clickAddService();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Defining General Page fields"));
		ServiceGeneralPage.defineName(service.getName());
		ServiceGeneralPage.defineDescription(service.getDescription());
		ServiceGeneralPage.defineCategory(service.getCategories().get(0).getName());
		ServiceGeneralPage.defineProjectCode(service.getProjectCode());
		GeneralUIUtils.ultimateWait();
		GeneralPageElements.clickCreateButton();
	}
	
	public static void clickAddService(){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking the Add Service button"));
		try {
	    GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.Dashboard.ADD_AREA.getValue());
			GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue()).click();
		GeneralUIUtils.ultimateWait();
		} catch (Exception e){
			SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exception on catched on Add Service button, retrying ..."));
			GeneralUIUtils.hoverOnAreaByClassName("w-sdc-dashboard-card-new");			
			GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue()).click();
			GeneralUIUtils.ultimateWait();
		}
	}

}
