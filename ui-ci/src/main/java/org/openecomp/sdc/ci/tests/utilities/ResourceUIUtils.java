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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.CheckBoxStatusEnum;
import org.openecomp.sdc.ci.tests.datatypes.CreateAndImportButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.Dashboard;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceCategoriesNameEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.AssertJUnit;

import com.aventstack.extentreports.Status;

public final class ResourceUIUtils {
	public static final String RESOURCE_NAME_PREFIX = "ResourceCDTest-";
	protected static final boolean IS_BEFORE_TEST = true;
	public static final String INITIAL_VERSION = "0.1";
	public static final String ICON_RESOURCE_NAME = "call_controll";
	protected static final String UPDATED_RESOURCE_ICON_NAME = "objectStorage";

	private ResourceUIUtils() {
	}

	static WebDriver driver = GeneralUIUtils.getDriver();

	public static void defineResourceName(String resourceName) {

		WebElement resourceNameTextbox = GeneralUIUtils.getDriver().findElement(By.name("componentName"));
		resourceNameTextbox.clear();
		resourceNameTextbox.sendKeys(resourceName);
	}

	public static void defineResourceCategory(String category, String datatestsid) {

		GeneralUIUtils.getSelectList(category, datatestsid);
	}

	// public static void uploadFileWithJavaRobot(String FilePath,String
	// FileName) throws Exception{
	//
	// StringSelection Path= new StringSelection(FilePath+FileName);
	// Thread.sleep(1000);
	// java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(Path,
	// null);
	// Robot robot = new Robot();
	// robot.delay(1000);
	// robot.keyPress(KeyEvent.VK_CONTROL);
	// robot.keyPress(KeyEvent.VK_V);
	// robot.keyRelease(KeyEvent.VK_V);
	// robot.keyRelease(KeyEvent.VK_CONTROL);
	// robot.delay(1000);
	// robot.keyPress(KeyEvent.VK_ENTER);
	// robot.keyRelease(KeyEvent.VK_ENTER);
	// robot.delay(1000);
	// }
	// click and upload tosca file //**to be changed.
	public static void importFileWithSendKey(String FilePath, String FileName, CreateAndImportButtonsEnum type)
			throws Exception {
		WebElement importButton = HomeUtils.createAndImportButtons(type, driver).findElement(By.tagName("input"));
		importButton.sendKeys(FilePath + FileName);
	}

	public static void importFileWithSendKeyBrowse(String FilePath, String FileName) throws Exception {
		WebElement browsebutton = GeneralUIUtils.getWebElementByTestID("browseButton");
		browsebutton.sendKeys(FilePath + FileName);
	}

	// public static void defineVendorName(String resourceVendorName) {
	//
	// WebElement resourceVendorNameTextbox =
	// GeneralUIUtils.getWebElementByTestID("vendorName");
	// resourceVendorNameTextbox.clear();
	// resourceVendorNameTextbox.sendKeys(resourceVendorName);
	// }

	// public static void defineTagsList(ResourceReqDetails resource,String
	// []resourceTags) {
	// List<String>taglist = new ArrayList<String>();;
	// WebElement resourceTagsTextbox =
	// GeneralUIUtils.getWebElementByTestID("i-sdc-tag-input");
	// for (String tag : resourceTags) {
	// resourceTagsTextbox.clear();
	// resourceTagsTextbox.sendKeys(tag);
	// resourceTagsTextbox.sendKeys(Keys.ENTER);
	// taglist.add(tag);
	// }
	// resource.setTags(taglist);
	// }

	public static String defineUserId(String userId) {
		//
		WebElement resourceUserIdTextbox = ResourceGeneralPage.getContactIdField();
		resourceUserIdTextbox.clear();
		resourceUserIdTextbox.sendKeys(userId);
		return userId;
	}

	public static void defineVendorRelease(String resourceVendorRelease) {

		WebElement resourceVendorReleaseTextbox = GeneralUIUtils.getWebElementByTestID("vendorRelease");
		resourceVendorReleaseTextbox.clear();
		resourceVendorReleaseTextbox.sendKeys(resourceVendorRelease);
	}

	public static void selectResourceIcon(String resourceIcon) throws Exception {
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 10);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@data-tests-id='" + resourceIcon + "']")))
				.click();
	}

	public static String definePropertyName(String name) {

		WebElement nameProperty = GeneralUIUtils.getDriver().findElement(By.name("propertyName"));
		nameProperty.sendKeys(name);
		return name;
	}

	public static void selectRandomResourceIcon() throws Exception {
		GeneralUIUtils.moveToStep(StepsEnum.ICON);
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 4);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(@data-tests-id, 'iconBox')]")));
		List<WebElement> iconElement = GeneralUIUtils.getDriver()
				.findElements(By.xpath("//*[contains(@data-tests-id, 'iconBox')]"));
		iconElement.get(0).click();
	}

	public static List<WebElement> getAllObjectsOnWorkspace(WebDriver driver, ResourceReqDetails resource)
			throws Exception {

		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 10);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@*='" + resource.getName() + "']")));
		return GeneralUIUtils.getDriver()
				.findElements(By.xpath("//div[@class='" + "w-sdc-dashboard-card-info-name" + "']"));

	}

	public static String getErrorMessageText(String text) throws Exception {

		return GeneralUIUtils.getWebElementByClassName(text).getText();

	}

	public static WebElement scrollElement(WebDriver driver) throws Exception {

		return GeneralUIUtils.getDriver().findElement(By.className("ps-scrollbar-y"));
	}

	public static void scrollDownPage() throws AWTException, InterruptedException {
		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_PAGE_DOWN);
		robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
		robot.keyPress(KeyEvent.VK_PAGE_DOWN);
		robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
		robot.keyPress(KeyEvent.VK_PAGE_DOWN);
		robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
		robot.keyPress(KeyEvent.VK_PAGE_DOWN);
		robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
		robot.keyPress(KeyEvent.VK_PAGE_DOWN);
		robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
		robot.keyPress(KeyEvent.VK_PAGE_DOWN);
		robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
		robot.keyPress(KeyEvent.VK_PAGE_DOWN);
		robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
	}

	public static void defineNewSelectList(String Text) {
		WebElement mySelectElm = GeneralUIUtils.getDriver().findElement(By.className("i-sdc-form-select"));
		Select mySelectString = new Select(mySelectElm);
		mySelectString.selectByVisibleText(Text);
	}

	public static void defineDefaultValueByType(String Value) {

		WebElement valueString = GeneralUIUtils.getDriver().findElement(By.name("value"));
		valueString.clear();
		valueString.sendKeys(Value);
	}

	public static void defineBoolenDefaultValue(String Value) {

		WebElement elementBoolean = GeneralUIUtils.getDriver().findElement(By.name("value"));
		Select se = new Select(elementBoolean);
		se.selectByValue(Value);
	}

	public static void clickButtonBlue() {
		WebElement clickButtonBlue = GeneralUIUtils.getDriver().findElement(By.className("w-sdc-btn-blue"));
		clickButtonBlue.click();
	}

	public static void clickButton(String selectButton) {

		WebElement clickButton = GeneralUIUtils.getDriver()
				.findElement(By.xpath("//*[@data-tests-id='" + selectButton + "']"));
		clickButton.click();
	}

	public static WebElement Waitfunctionforbuttons(String element, int timeout) {
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), timeout);
		return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(element)));
	}

	public static WebElement waitToButtonSubmitForTesting() {
		return Waitfunctionforbuttons("//*[@data-tests-id='submitForTesting']", 10);
	}

	public static WebElement waitToFinishButtonEnabled() {
		return Waitfunctionforbuttons("//button[@data-tests-id='Finish']", 10);
	}

	public static WebElement waitToNextButtonEnabled() {
		return Waitfunctionforbuttons("//button[@data-tests-id='Next']", 10);
	}

	public static WebElement waitToHomeMenu() {
		return Waitfunctionforbuttons("//*[@data-tests-id='main-menu-button-home']", 10);
	}

	public static WebElement waitToCatalogMenu() {
		return Waitfunctionforbuttons("//*[@data-tests-id='main-menu-button-catalog']", 10);
	}

	public static WebElement waitSearch() {
		return Waitfunctionforbuttons("//*[@data-tests-id='main-menu-input-search']", 10);
	}

	public static WebElement waitSubmitforTestingCard() {
		return Waitfunctionforbuttons("//*[@data-tests-id='i-sdc-dashboard-card-menu-item-SubmitforTesting']", 10);
	}

	public static WebElement waitViewCard() {
		return Waitfunctionforbuttons("//*[@data-tests-id='i-sdc-dashboard-card-menu-item-View']", 5);
	}

//	public static void waitOpenCard(String requiredElementUniqueId) throws Exception {
//		WebElement menu = GeneralUIUtils.getDriver()
//				.findElement(By.xpath("//*[@data-tests-id='" + requiredElementUniqueId + "']"));
//		GeneralUIUtils.hoverOnAreaByTestId(menu);
//	}

	public static void fillResourceGeneralInformationPage(ResourceReqDetails resource, User user, boolean isNewResource) {
		try {
			ResourceGeneralPage.defineName(resource.getName());
			ResourceGeneralPage.defineDescription(resource.getDescription());
			ResourceGeneralPage.defineCategory(resource.getCategories().get(0).getSubcategories().get(0).getName());
			ResourceGeneralPage.defineVendorName(resource.getVendorName());
			ResourceGeneralPage.defineVendorRelease(resource.getVendorRelease());
			if (isNewResource){
				ResourceGeneralPage.defineTagsList(resource, new String[] { "This-is-tag", "another-tag", "Test-automation-tag" });
			}
			else{
				ResourceGeneralPage.defineTagsList(resource, new String[] { "one-more-tag" });
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void fillMaxValueResourceGeneralInformationPage(ResourceReqDetails resource) {
			String stringPattern = "ABCDabcd123456";
			GeneralUIUtils.addStringtoClipboard(buildStringFromPattern(stringPattern, 5000));		  
		    ResourceGeneralPage.defineNameWithPaste();
		    ResourceGeneralPage.defineDescriptionWithPaste();
		    ResourceGeneralPage.defineVendorNameWithPaste();
		    ResourceGeneralPage.defineVendorReleaseWithPaste();
//			ResourceGeneralPage.defineName(buildStringFromPattern(stringPattern, 5000));
//			ResourceGeneralPage.defineDescription(buildStringFromPattern(stringPattern, 5000));
//			ResourceGeneralPage.defineVendorName(buildStringFromPattern(stringPattern, 5000));
//			ResourceGeneralPage.defineVendorRelease(buildStringFromPattern(stringPattern, 5000));
//			ResourceGeneralPage.defineTagsList(resource, new String[] { buildStringFromPattern(stringPattern, 5000) });
		    ResourceGeneralPage.defineTagsListWithPaste();
			GeneralUIUtils.waitForAngular();
	}
	
	public static String buildStringFromPattern(String stringPattern, int stringLength){
		char[] chars = stringPattern.toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < stringLength; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		return sb.toString();
	}

	public static void fillNewResourceValues(ResourceReqDetails resource, User user) throws Exception {
		fillResourceGeneralInformationPage(resource, user, true);
		GeneralPageElements.clickCreateButton();
		// selectIcon();
	}

	// coded by teddy.

	public static WebElement waitfunctionforallelements(String element) {
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 5);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@*='" + element + "']")));
	}

	public static WebElement waitFunctionForaGetElements(String element, int timeout) {
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), timeout);
		return wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@data-tests-id='" + element + "']")));
	}

	public static void getVFCGeneralInfo(ResourceReqDetails resource, User user) throws InterruptedException {
		Thread.sleep(2000);
		String version = GeneralUIUtils.getWebElementsListByTestID("versionvalue").get(0).getText().substring(1);
		String name = GeneralUIUtils.getWebElementByTestID("name").getAttribute("value");
		String description = GeneralUIUtils.getWebElementByTestID("description").getAttribute("value");
		String category = GeneralUIUtils.getSelectList(null, "selectGeneralCategory").getFirstSelectedOption()
				.getText();
		String vendorName = GeneralUIUtils.getWebElementByTestID("vendorName").getAttribute("value");
		String vendorRelease = GeneralUIUtils.getWebElementByTestID("vendorRelease").getAttribute("value");
		List<WebElement> tags = GeneralUIUtils.getWebElementsListByTestID("i-sdc-tag-text");
		String type = GeneralUIUtils.getWebElementsListByTestID("type").get(1).getText();
		int index = type.lastIndexOf(":");
		System.out.println(type.substring(0, index));
		String AttContact = GeneralUIUtils.getWebElementByTestID("attContact").getAttribute("value");
		System.out.println(resource.getVersion());
		assertTrue(resource.getVersion().equals(version));
		assertTrue(resource.getName().equals(name));
		assertTrue(resource.getDescription().equals(description));
		System.out.println(resource.getVendorName());
		System.out.println(resource.getVendorRelease());
		assertTrue(resource.getCategories().get(0).getSubcategories().get(0).getName().equals(category));
		assertTrue(resource.getVendorName().equals(vendorName));
		assertTrue(resource.getVendorRelease().equals(vendorRelease));
		assertTrue(resource.getCreatorUserId().equals(AttContact));
		assertEquals(type.substring(0, index), resource.getResourceType());

		for (int i = 0; i < tags.size(); i++) {
			assertEquals(resource.getTags().get(i), tags.get(i).getText());
		}
	}

	public static void getGeneralInfo(ResourceReqDetails resource, User user) {

		// clickMore();
		// String
		// componentType=waitFunctionForaGetElements("componentType",3).getText();
		// String version=waitFunctionForaGetElements("version",3).getText();
		// String
		// category=waitFunctionForaGetElements("category",3).getText();//get
		// right panel Category.
		// String
		// resourceType=waitFunctionForaGetElements("resourceType",3).getText();//get
		// right panel SubCategory.
		// String date=waitfunctionforelements("creationDate",3).getText();
		// String aouthor=waitfunctionforallelements("author'",3).getText();
		// String
		// vendorName=waitFunctionForaGetElements("vendorName",3).getText();
		// String
		// vendorRelease=waitFunctionForaGetElements("vendorRelease",3).getText();
		// String
		// AttContact=waitFunctionForaGetElements("attContact",3).getText();
		// String
		// Description=waitFunctionForaGetElements("description",3).getText();
		List<WebElement> tags = GeneralUIUtils.getWebElementsListByTestID("tag");
		// // String TagVF=waitFunctionForaGetElements("tag",3).getText();
		// assertTrue(componentType.equals("RESOURCE"));
		// assertTrue(version.equals(resource.getVersion()));
		// assertTrue(category.equals(resource.getCategories().get(0).getName()));
		// assertEquals(resourceType,resource.getResourceType());
		// // assertEquals(Date,resource.getCreationDate());
		// // assertEquals(Aouthor,resource.getCreatorFullName());
		// assertTrue(vendorName.equals(resource.getVendorName()));
		// assertTrue(vendorRelease.equals(resource.getVendorRelease()));
		// assertTrue(AttContact.equals(resource.getAttContact()));
		// assertTrue(Description.equals(resource.getDescription()+"\nLess"));
		for (WebElement tag : tags) {
			System.out.println(resource.getTags().get(0));
		}
	}

	public static void getGeneralInfoForTags(ResourceReqDetails resource, User user) {

		clickMore();
		String componentType = waitFunctionForaGetElements("componentType", 3).getText();
		String version = waitFunctionForaGetElements("version", 3).getText();
		String category = waitFunctionForaGetElements("category", 3).getText();// get
																				// right
																				// panel
																				// Category.
		String resourceType = waitFunctionForaGetElements("resourceType", 3).getText();// get
																						// right
																						// panel
																						// SubCategory.
		String date = GeneralUIUtils.getWebElementByClassName("creationDate").getText();
		String aouthor = waitfunctionforallelements("author'").getText();
		String vendorName = waitFunctionForaGetElements("vendorName", 3).getText();
		String vendorRelease = waitFunctionForaGetElements("vendorRelease", 3).getText();
		String attContact = waitFunctionForaGetElements("attContact", 3).getText();
		String description = waitFunctionForaGetElements("description", 3).getText();
		List<WebElement> tags = GeneralUIUtils.getWebElementsListByTestID("tag");
		assertTrue(componentType.equals("RESOURCE"));
		assertTrue(version.equals(resource.getVersion()));
		assertTrue(category.equals(resource.getCategories().get(0).getName()));
		assertEquals(resourceType, resource.getResourceType());
		// assertEquals(Date,resource.getCreationDate());
		// assertEquals(Aouthor,resource.getCreatorFullName());
		assertTrue(vendorName.equals(resource.getVendorName()));
		assertTrue(vendorRelease.equals(resource.getVendorRelease()));
		assertTrue(attContact.equals(resource.getContactId()));
		assertTrue(description.equals(resource.getDescription() + "\nLess"));
		assertTrue(tags.equals("Tag-150"));
	}

	public static WebElement searchVFNameInWorkspace(ResourceReqDetails resource, User user) throws Exception {

		List<WebElement> findElements = GeneralUIUtils.getDriver()
				.findElements(By.xpath("//div[@data-tests-id='" + resource.getUniqueId() + "']"));
		assertNotNull("did not find any elements", findElements);
		for (WebElement webElement : findElements) {
			if (webElement.getText().contains(resource.getUniqueId())) {
				System.out.println("I find it");
				return webElement;
			}
		}
		return null;
	}

	public static Boolean searchCheckOutWorkspace(ResourceReqDetails resource, User user,
			CheckBoxStatusEnum checkBoxStatusEnum) throws Exception {

		List<WebElement> findElements = GeneralUIUtils.getDriver()
				.findElements(By.xpath("//div[@data-tests-id='component.lifecycleState']"));
		assertNotNull("did not find any elements", findElements);
		for (WebElement webElement : findElements) {
			if (!webElement.getAttribute("class").contains(checkBoxStatusEnum.name())) {
				return false;
			}
		}
		return true;
	}

	// coded by tedy.
	public static void validateWithRightPalett(ResourceReqDetails resource, User user) {
		// String
		// Type=Waitfunctionforallelements("sharingService.selectedEntity.getTypeForView()",3).getText();
		String ResourceType = waitfunctionforallelements("selectedComponent.resourceType").getText();
		System.out.println(ResourceType);
		String Version = waitfunctionforallelements("selectedComponent.version").getText();
		String Category = waitfunctionforallelements("selectedComponent.categories[0].name").getText();// get
																										// right
																										// panel
																										// Category.
		String CanvasSubCategory = waitfunctionforallelements("selectedComponent.categories[0].subcategories[0].name")
				.getText();// get right panel SubCategory.
		// String Date=Waitfunctionforelements("selectedComponent.creationDate |
		// date: 'MM/dd/yyyy'").getText();
		// String
		// Aouthor=waitfunctionforallelements("selectedComponent.creatorFullName'").getText();
		String VendorName = waitfunctionforallelements("selectedComponent.vendorName").getText();
		String VendorRelease = waitfunctionforallelements("selectedComponent.vendorRelease").getText();
		String AttContact = waitfunctionforallelements("selectedComponent.attContact").getText();
		String Description = waitfunctionforallelements("selectedComponent.description").getText();
		String TagVF = waitfunctionforallelements("tag").getText();
		AssertJUnit.assertEquals(ResourceType, resource.getResourceType());
		AssertJUnit.assertEquals(Version, resource.getVersion());
		AssertJUnit.assertEquals(Category, resource.getCategories().get(0).getName());
		AssertJUnit.assertEquals(CanvasSubCategory,
				resource.getCategories().get(0).getSubcategories().get(0).getName());
		// assertEquals(Date,resource.getCreationDate());
		// assertEquals(Aouthor,resource.getCreatorFullName());
		AssertJUnit.assertEquals(VendorName, resource.getVendorName());
		AssertJUnit.assertEquals(VendorRelease, resource.getVendorRelease());
		AssertJUnit.assertEquals(AttContact, resource.getContactId());
		AssertJUnit.assertEquals(Description, resource.getDescription() + "\nLess");
		AssertJUnit.assertEquals(TagVF, "qa123");
	}

	public static void clickMore() {
		WebElement clickButtonSubmit = GeneralUIUtils.getDriver()
				.findElement(By.className("ellipsis-directive-more-less"));
		clickButtonSubmit.click();
	}
	
	public static RestResponse createResourceInUI(ResourceReqDetails resource, User user)
			throws Exception, AWTException {
		System.out.println("creating resource...");
		fillNewResourceValues(resource, user);
		RestResponse getCreatedResource = RestCDUtils.getResource(resource, user);
		AssertJUnit.assertEquals("Did not succeed to get any resource", HttpStatus.SC_OK,
				getCreatedResource.getErrorCode().intValue());

		return getCreatedResource;
	}

	/**
	 * @deprecated Use {@link #createVF(ResourceReqDetails,User)} instead
	 */
	public static  void createResource(ResourceReqDetails resource, User user) throws Exception {
		createVF(resource, user);
	}

	public static  void createVF(ResourceReqDetails resource, User user) throws Exception {
		ExtentTestActions.log(Status.INFO, "Going to create a new VF.");
		createResource(resource, user,  DataTestIdEnum.Dashboard.BUTTON_ADD_VF);
	}

	public static void createResource(ResourceReqDetails resource, User user, DataTestIdEnum.Dashboard button) {
		WebElement addVFButton = null;
        try {
			GeneralUIUtils.ultimateWait();
			try{
				GeneralUIUtils.hoverOnAreaByClassName("w-sdc-dashboard-card-new");
				addVFButton = GeneralUIUtils.getWebElementByTestID(button.getValue());
			}
			catch (Exception e){
				File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Warning_" + resource.getName());
				final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
				SetupCDTest.getExtendTest().log(Status.WARNING, "Add button is not visible after hover on import area of Home page, moving on ..." + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath));
				showButtonsADD();
				addVFButton = GeneralUIUtils.getWebElementByTestID(button.getValue());
			}
			addVFButton.click();
				GeneralUIUtils.ultimateWait();
	        } 
        catch (Exception e ) {
        	SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exeption catched on ADD button, retrying ... "));
        	GeneralUIUtils.hoverOnAreaByClassName("w-sdc-dashboard-card-new");
			GeneralUIUtils.ultimateWait();
			GeneralUIUtils.getWebElementByTestID(button.getValue()).click();
			GeneralUIUtils.ultimateWait();
        }
		fillResourceGeneralInformationPage(resource, user, true);
		resource.setVersion("0.1");
		GeneralPageElements.clickCreateButton();
	}
	
	public static void updateResource(ResourceReqDetails resource, User user){
		ResourceGeneralPage.defineContactId(resource.getContactId());
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating General screen fields ..."));
		fillResourceGeneralInformationPage(resource, user, false);
		ResourceGeneralPage.clickUpdateButton();
	}
	
	

	public static RestResponse updateResourceInformationPage(ResourceReqDetails resource, User user)
			throws Exception, AWTException {

		fillResourceGeneralInformationPage(resource, user, true);
		GeneralPageElements.clickCreateButton();
		return null;

	}

	public static RestResponse checkInResourceInUI(ResourceReqDetails resource, User user) throws Exception {

		WebElement ASDCLink = GeneralUIUtils.getDriver().findElement(By.className("w-sdc-header-logo-link"));
		ASDCLink.click();
		Thread.sleep(2000);

		List<WebElement> listFormInput = GeneralUIUtils.getDriver()
				.findElements(By.className("i-sdc-left-sidebar-nav-item"));
		WebElement addPropertyElement = listFormInput.get(0);
		addPropertyElement.click();
		Thread.sleep(2000);

		WebElement searchResource = GeneralUIUtils.getDriver()
				.findElement(By.className("w-sdc-header-catalog-search-input"));
		searchResource.sendKeys("newresource4test");

		Thread.sleep(1000);

		WebElement buttonClickMenu = GeneralUIUtils.getDriver()
				.findElement(By.className("w-sdc-dashboard-card-menu-button"));
		buttonClickMenu.click();

		WebElement clickMenu = GeneralUIUtils.getDriver().findElement(By.className("w-sdc-dashboard-card-menu"));
		clickMenu.click();

		List<WebElement> clickCheckIn = GeneralUIUtils.getDriver()
				.findElements(By.className("i-sdc-dashboard-card-menu-item"));
		WebElement clickCheckInMenu = clickCheckIn.get(1);
		clickCheckInMenu.click();

		WebElement descriptionForSubmit = GeneralUIUtils.getDriver()
				.findElement(By.className("w-sdc-modal-body-comment"));
		descriptionForSubmit.sendKeys("checkin resource");
		Thread.sleep(2000);
		WebElement clickButtonSubmitTwo = GeneralUIUtils.getDriver().findElement(By.className("w-sdc-btn-blue"));
		clickButtonSubmitTwo.click();
		Thread.sleep(2000);

		WebElement buttonClickMenu1 = GeneralUIUtils.getDriver()
				.findElement(By.className("w-sdc-dashboard-card-menu-button"));
		buttonClickMenu1.click();

		WebElement clickMenu1 = GeneralUIUtils.getDriver().findElement(By.className("w-sdc-dashboard-card-menu"));
		clickMenu1.click();

		List<WebElement> clickCheckOut = GeneralUIUtils.getDriver()
				.findElements(By.className("i-sdc-dashboard-card-menu-item"));
		WebElement clickCheckOutMenu = clickCheckOut.get(0);
		clickCheckOutMenu.click();

		Thread.sleep(3000);
		RestResponse getResource = RestCDUtils.getResource(resource, user);
		AssertJUnit.assertEquals("Did not succeed to get resource after create", 200,
				getResource.getErrorCode().intValue());
		return getResource;

	}

	public static String lifeCycleStateUI() throws InterruptedException {
		return GeneralUIUtils.getWebElementByTestID("formlifecyclestate").getText();
	}

	public static List<String> catalogFilterResourceCategoriesChecBox(ResourceCategoriesNameEnum enumName)
			throws Exception {
		List<String> categories = Arrays.asList();
		switch (enumName) {
		case APPLICATIONL4:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("applicationServer", "defaulticon", "vl", "cp", "call_controll", "borderElement",
					"network", "firewall", "database", "loadBalancer");
			break;
		case APPLICATION_SERVER:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("applicationServer", "vl", "cp", "defaulticon");
			break;
		case BORDER_ELEMENT:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("borderElement", "vl", "cp", "defaulticon");
			break;
		case CALL_CONTROL:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("call_controll", "vl", "cp", "defaulticon");
			break;
		case COMMON_NETWORK_RESOURCES:
			GeneralUIUtils.getWebElementByLinkText("Common Network Resources").click();
			categories = Arrays.asList("network", "vl", "cp", "defaulticon");
			break;
		case CONNECTION_POINTS:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("cp", "defaulticon");
			break;
		case DATABASE:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("database", "vl", "cp", "defaulticon");
			break;
		case DATABASE_GENERIC:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("database", "vl", "cp", "defaulticon");
			break;
		case FIREWALL:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("firewall", "vl", "cp", "defaulticon");
			break;
		case GATEWAY:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("gateway", "vl", "cp", "defaulticon");
			break;
		case INFRASTRUCTURE:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("connector", "vl", "cp", "defaulticon");
			break;
		case INFRASTRUCTUREL23:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("ucpe", "vl", "cp", "defaulticon");
			break;
		case LAN_CONNECTORS:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("network", "port", "connector", "vl", "cp", "defaulticon");
			break;
		case LOAD_BALANCER:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("loadBalancer", "vl", "cp", "defaulticon");
			break;
		case MEDIA_SERVERS:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("network", "vl", "cp", "defaulticon");
			break;
		case NETWORKL4:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("network", "vl", "cp", "defaulticon");
			break;
		case NETWORK_ELEMENTS:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("port", "defaulticon", "network", "connector", "vl", "cp");
			break;
		case NETWORK_L23:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("network", "vl", "defaulticon", "cp", "router", "port", "connector", "gateway",
					"ucpe");
			break;
		case NETWORK_CONNECTIVITY:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("network", "vl", "cp", "defaulticon");
			break;
		case GENERIC:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("database", "port", "loadBalancer", "vl", "cp", "objectStorage", "compute",
					"defaulticon", "ucpe", "network", "connector");
			break;
		case ABSTRACT:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("objectStorage", "compute", "defaulticon", "cp", "vl");
			break;
		case Router:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("router", "vl", "cp", "defaulticon");
			break;
		case VIRTUAL_LINKS:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("vl", "defaulticon");
			break;
		case WAN_Connectors:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("network", "port", "connector", "vl", "cp", "defaulticon");
			break;
		case WEB_SERVER:
			GeneralUIUtils.getWebElementByTestID(enumName.getValue()).click();
			categories = Arrays.asList("applicationServer", "vl", "cp", "defaulticon");
			break;
		}
		return categories;
	}

	public static void deleteVersionInUI() throws Exception {

		waitToDeleteVersion().click();
		ResourceUIUtils.clickButtonBlue();
	}

	public static void selectTabInRightPallete(String className) throws Exception {
		WebElement tab = GeneralUIUtils.getWebElementByClassName(className);
		tab.click();
	}

	public static WebElement waitToDeleteVersion() {
		return Waitfunctionforbuttons("//*[@data-tests-id='deleteVersion']", 10);
	}

	public static WebElement rihtPanelAPI() {
		return waitFunctionForaGetElements("tab-api", 10);
	}

	/**
	 * Click on HTML element.
	 * 
	 * @param dataTestId
	 * @throws Exception
	 */
	public static void getWebElementByTestID(String dataTestId) throws Exception {
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 20);
		WebElement element = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
		element.click();
		// wait.until(ExpectedConditions.elemetto)
		// WebElement serviceButton =
		// GeneralUIUtils.getDriver().findElement(By.xpath("//*[@data-tests-id='"
		// + dataTestId + "']"));
		// serviceButton.
		// serviceButton.click();
	}

	/**
	 * Move to HTML element by class name. When moving to the HTML element, it
	 * will raise hover event.
	 * 
	 * @param className
	 */
//	public static void moveToHTMLElementByClassName(String className) {
//		Actions actions = new Actions(GeneralUIUtils.getDriver());
//		final WebElement createButtonsArea = GeneralUIUtils
//				.retryMethodOnException(() -> GeneralUIUtils.getDriver().findElement(By.className(className)));
//		actions.moveToElement(createButtonsArea).perform();
//	}

	/**
	 * Move to HTML element by element id. When moving to the HTML element, it
	 * will raise hover event.
	 * 
	 * @param className
	 */
//	static void moveToHTMLElementByDataTestId(String dataTestId) {
//		// WebElement hoverArea =
//		// GeneralUIUtils.getDriver().findElement(By.xpath("//*[@data-tests-id='"
//		// + dataTestId + "']"));
//		WebElement hoverArea = GeneralUIUtils.waitForElementVisibility(dataTestId);
//		// WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(),
//		// 30);
//		// wait.until(ExpectedConditions.visibilityOf(hoverArea));
//
//		Actions actions = new Actions(GeneralUIUtils.getDriver());
//		actions.moveToElement(hoverArea).perform();
//	}

	// public static ResourceReqDetails createResourceInUI(User user){
	// try{
	// ResourceReqDetails defineResourceDetails =
	// defineResourceDetails(ResourceTypeEnum.VF);
	// ResourceUIUtils.moveToHTMLElementByClassName("w-sdc-dashboard-card-new");
	// ResourceUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_VF.getValue());
	// GeneralUIUtils.waitForLoader();
	//// GeneralUIUtils.sleep(1000);
	// fillResourceGeneralInformationPage(defineResourceDetails, user);
	// GeneralPageElements.clickCreateButton();
	// return defineResourceDetails;
	// }
	// catch( Exception e){
	// throw new RuntimeException(e);
	// }
	// }

	/**
	 * Import VFC
	 * 
	 * @param user
	 * @param filePath
	 * @param fileName
	 * @return
	 * @throws Exception
	 */

	public static void importVfc(ResourceReqDetails resourceMetaData, String filePath, String fileName, User user)
			throws Exception {
		GeneralUIUtils.ultimateWait();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating new VFC resource ", resourceMetaData.getName()));
		GeneralUIUtils.hoverOnAreaByTestId(Dashboard.IMPORT_AREA.getValue());
		GeneralUIUtils.ultimateWait();
		// Insert file to the browse dialog		
		WebElement buttonVFC = GeneralUIUtils.findByText("Import VFC");
		WebElement fileInputElement = GeneralUIUtils.getInputElement(DataTestIdEnum.Dashboard.IMPORT_VFC_FILE.getValue());
		if (!buttonVFC.isDisplayed()){
			File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Warning_" + resourceMetaData.getName());
			final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
			SetupCDTest.getExtendTest().log(Status.WARNING, "VFC button not visible after hover on import area of Home page, moving on ..." + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath));			
		}
		try{
			fileInputElement.sendKeys(filePath + fileName);
		} catch (ElementNotVisibleException e) {
			SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exeption catched on file input, converting VFC file input to visible"));
			showButtons();
			fileInputElement.sendKeys(filePath + fileName);
		} 
		// Fill the general page fields.
		GeneralUIUtils.ultimateWait();
		fillResourceGeneralInformationPage(resourceMetaData, user, true);
		GeneralPageElements.clickCreateButton();
	}
	
	public static void importVfcNoCreate(ResourceReqDetails resourceMetaData, String filePath, String fileName, User user)
			throws Exception {
		GeneralUIUtils.hoverOnAreaByTestId(Dashboard.IMPORT_AREA.getValue());
		// Insert file to the browse dialog
		WebElement buttonVFC = GeneralUIUtils.findByText("Import VFC");
		WebElement fileInputElement = GeneralUIUtils.getInputElement(DataTestIdEnum.Dashboard.IMPORT_VFC_FILE.getValue());
		if (!buttonVFC.isDisplayed()){
			File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Warning_" + resourceMetaData.getName());
			final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
			SetupCDTest.getExtendTest().log(Status.WARNING, "VFC button not visible after hover on import area of Home page, moving on ..." + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath));			
		}
		try{
			fileInputElement.sendKeys(filePath + fileName);
		} catch (ElementNotVisibleException e) {
			SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exeption catched on file input, converting VFC file input to visible"));
			showButtons();
			fileInputElement.sendKeys(filePath + fileName);
		}
		// Fill the general page fields.
		GeneralUIUtils.waitForLoader();
		fillResourceGeneralInformationPage(resourceMetaData, user, true);
	}
	
	
	public static void importVfFromCsar(ResourceReqDetails resourceMetaData, String filePath, String fileName, User user)
			throws Exception {
		GeneralUIUtils.ultimateWait();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating new VF asset resource %s", resourceMetaData.getName()));  
		GeneralUIUtils.hoverOnAreaByTestId(Dashboard.IMPORT_AREA.getValue());
		GeneralUIUtils.ultimateWait();
		// Insert file to the browse dialog	
		WebElement buttonDCAE = GeneralUIUtils.findByText("Import DCAE asset");
		WebElement fileInputElement = GeneralUIUtils.getInputElement(DataTestIdEnum.Dashboard.IMPORT_VF_FILE.getValue());
		if (!buttonDCAE.isDisplayed()){
			File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Warning_" + resourceMetaData.getName());
			final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
			SetupCDTest.getExtendTest().log(Status.WARNING, "DCAE button not visible after hover on import area of Home page, moving on ..." + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath));			
		}
		try{
			fileInputElement.sendKeys(filePath + fileName);
		} catch (ElementNotVisibleException e) {
			SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exeption catched on file input, converting DCAE file input to visible"));
			showButtons();
			fileInputElement.sendKeys(filePath + fileName);
		}    
		// Fill the general page fields.
		GeneralUIUtils.ultimateWait();
		fillResourceGeneralInformationPage(resourceMetaData, user, true);
		GeneralPageElements.clickCreateButton(10*60);
//		GeneralUIUtils.ultimateWait(); "don't change import of csar can take longer then 3 minutes"
		GeneralUIUtils.waitForLoader(10*60);
	}
	
	public static void importVfFromCsarNoCreate(ResourceReqDetails resourceMetaData, String filePath, String fileName, User user)
			throws Exception {
		GeneralUIUtils.ultimateWait();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating new VF asset resource %s, Create button will not be clicked", resourceMetaData.getName()));  
		GeneralUIUtils.hoverOnAreaByTestId(Dashboard.IMPORT_AREA.getValue());
		GeneralUIUtils.ultimateWait();
		// Insert file to the browse dialog
		WebElement buttonDCAE = GeneralUIUtils.findByText("Import DCAE asset");
		WebElement fileInputElement = GeneralUIUtils.getInputElement(DataTestIdEnum.Dashboard.IMPORT_VF_FILE.getValue());
		if (!buttonDCAE.isDisplayed()){
			File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Warning_" + resourceMetaData.getName());
			final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
			SetupCDTest.getExtendTest().log(Status.WARNING, "DCAE button not visible after hover on import area of Home page, moving on ..." + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath));
		}
		try{
			fileInputElement.sendKeys(filePath + fileName);
		} catch (ElementNotVisibleException e) {
			SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exeption catched on file input, converting DCAE file input to visible"));
			showButtons();
			fileInputElement.sendKeys(filePath + fileName);
		}
		// Fill the general page fields.
		GeneralUIUtils.ultimateWait();
		fillResourceGeneralInformationPage(resourceMetaData, user, true);
		GeneralUIUtils.waitForLoader(10*60);
	}
	
	public static void updateVfWithCsar(String filePath, String fileName) {
		ExtentTestActions.log(Status.INFO, "Updating VF with updated CSAR file named " + fileName);
		WebElement browseWebElement = GeneralUIUtils.getInputElement(DataTestIdEnum.GeneralElementsEnum.UPLOAD_FILE_INPUT.getValue());
		browseWebElement.sendKeys(filePath + fileName);
		GeneralUIUtils.ultimateWait();
		GeneralPageElements.clickUpdateButton();
		GeneralUIUtils.waitForLoader();
		ExtentTestActions.log(Status.INFO, "VF is updated.");
	}
	
	

	// public static ResourceReqDetails importVfcInUI(User user, String
	// filePath, String fileName, ResourceTypeEnum resourceType) {
	// ResourceReqDetails defineResourceDetails =
	// defineResourceDetails(resourceType);
	// ResourceUIUtils.moveToHTMLElementByDataTestId(Dashboard.IMPORT_AREA.getValue());
	//
	// // Insert file to the browse dialog
	// final WebElement browseWebElement =
	// GeneralUIUtils.getWebElementByDataTestId(DataTestIdEnum.Dashboard.IMPORT_VFC_FILE.getValue());
	// browseWebElement.sendKeys(filePath + fileName);
	//
	// // Fill the general page fields.
	// GeneralUIUtils.waitForLoader();
	// fillResourceGeneralInformationPage(defineResourceDetails, user);
	// GeneralPageElements.clickCreateButton();
	// return defineResourceDetails;
	// }

	/**
	 * Import VF
	 * 
	 * @param user
	 * @param filePath
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	// public static ResourceReqDetails importVfInUI(User user, String filePath,
	// String fileName) throws Exception {
	// ResourceReqDetails defineResourceDetails =
	// defineResourceDetails(ResourceTypeEnum.VF);
	// ResourceUIUtils.moveToHTMLElementByDataTestId(Dashboard.IMPORT_AREA.getValue());
	//
	// // Insert file to the browse dialog
	// final WebElement browseWebElement =
	// GeneralUIUtils.getWebElementByDataTestId(DataTestIdEnum.Dashboard.IMPORT_VF_FILE.getValue());
	// browseWebElement.sendKeys(filePath + fileName);
	//
	// // Fill the general page fields.
	// GeneralUIUtils.waitForLoader();
	// fillResourceGeneralInformationPage(defineResourceDetails, user);
	// GeneralPageElements.clickCreateButton();
	// return defineResourceDetails;
	// }

	// public static ResourceReqDetails defineResourceDetails(ResourceTypeEnum
	// resourceType) {
	// ResourceReqDetails resource = new ResourceReqDetails();
	// resource = ElementFactory.getDefaultResource(NormativeTypesEnum.ROOT,
	// ResourceCategoryEnum.APPLICATION_L4_CALL_CONTROL);
	// resource.setVersion(INITIAL_VERSION);
	// resource.setIcon(ICON_RESOURCE_NAME);
	// resource.setResourceType(resourceType.toString());
	// resource.setName(getRandomComponentName(RESOURCE_NAME_PREFIX));
	//
	// SetupCDTest.setCreatedComponents(Arrays.asList(resource));
	//
	// return resource;
	// }

	protected static String getRandomComponentName(String prefix) {
		return prefix + new Random().nextInt(10000);
	}

	public static ImmutablePair<String, String> getFirstRIPos(ResourceReqDetails createResourceInUI, User user) {
		String responseAfterDrag = RestCDUtils.getResource(createResourceInUI, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		String xPosPostDrag = (String) ((JSONObject) ((JSONArray) jsonResource.get("componentInstances")).get(0))
				.get("posX");
		String yPosPostDrag = (String) ((JSONObject) ((JSONArray) jsonResource.get("componentInstances")).get(0))
				.get("posY");
		return new ImmutablePair<String, String>(xPosPostDrag, yPosPostDrag);

	}

	public static WebElement getErrorMessageText(WebDriver driver, String text) throws Exception {

		return GeneralUIUtils.getWebElementByClassName(text);

	}

	public static void fillGeneralInfoValuesAndIcon(ResourceReqDetails resource, User user) throws Exception {
		fillResourceGeneralInformationPage(resource, user, true);
		
		GeneralPageElements.clickCreateButton();

		selectRandomResourceIcon();
	}

	// coded by teddy.
	public static void getVFCGeneralInfoAndValidate(ResourceReqDetails resource, User user)
			throws InterruptedException {
		Thread.sleep(2000);
		WebDriver driver = GeneralUIUtils.getDriver();
		String version = GeneralUIUtils.getSelectList(null, "versionHeader").getFirstSelectedOption().getText();
		String name = GeneralUIUtils.getWebElementByTestID( "name").getAttribute("value");
		String description = GeneralUIUtils.getWebElementByTestID( "description").getAttribute("value");
		String category = GeneralUIUtils.getSelectList(null, "selectGeneralCategory").getFirstSelectedOption()
				.getText();
		String vendorName = GeneralUIUtils.getWebElementByTestID( "vendorName").getAttribute("value");
		String vendorRelease = GeneralUIUtils.getWebElementByTestID( "vendorRelease").getAttribute("value");
		List<WebElement> tags = GeneralUIUtils.getWebElementsListByTestID("i-sdc-tag-text");
		String type = GeneralUIUtils.getWebElementsListByTestID("type").get(1).getText();
		int index = type.lastIndexOf(":");
		System.out.println(type.substring(0, index));
		String AttContact = GeneralUIUtils.getWebElementByTestID( "attContact").getAttribute("value");
		System.out.println(resource.getVersion());
		assertTrue(resource.getVersion().equals(version.substring(1)));
		assertTrue(resource.getName().equals(name));
		assertTrue(resource.getDescription().equals(description));
		System.out.println(resource.getVendorName());
		System.out.println(resource.getVendorRelease());
		assertTrue(resource.getCategories().get(0).getSubcategories().get(0).getName().equals(category));
		assertTrue(resource.getVendorName().equals(vendorName));
		assertTrue(resource.getVendorRelease().equals(vendorRelease));
		assertTrue(resource.getCreatorUserId().equals(AttContact));
		assertEquals(type.substring(0, index), resource.getResourceType());

		for (int i = 0; i < tags.size(); i++) {
			assertEquals(resource.getTags().get(i), tags.get(i).getText());
		}
	}

	public static RestResponse createResourceNG(ResourceReqDetails resource, User user) throws Exception, AWTException {

		GeneralUIUtils.hoverOnAreaByTestId("w-sdc-dashboard-card-new");
		ResourceUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_VF.getValue());
		fillResourceGeneralInformationPage(resource, user, true);
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.LifeCyleChangeButtons.CREATE.getValue());
		return null;

	}
	
	public static void showButtons(){
		String parentElementClassAttribute = "sdc-dashboard-import-element-container";
		WebElement fileInputElementWithVisible = GeneralUIUtils.getDriver().findElement(By.className(parentElementClassAttribute));
		GeneralUIUtils.unhideElement(fileInputElementWithVisible, parentElementClassAttribute);
		GeneralUIUtils.ultimateWait();
		SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Input buttons now visible..."));
	}
	
	public static void showButtonsADD(){
		try {
			GeneralUIUtils.ultimateWait();
			String parentElementClassAttribute = "sdc-dashboard-create-element-container";
			WebElement fileInputElementWithVisible = GeneralUIUtils.getDriver().findElement(By.className(parentElementClassAttribute));
			GeneralUIUtils.unhideElement(fileInputElementWithVisible, parentElementClassAttribute);
			GeneralUIUtils.ultimateWait();
		} catch (Exception e ){
			GeneralUIUtils.ultimateWait();
			String parentElementClassAttribute = "sdc-dashboard-create-element-container";
			WebElement fileInputElementWithVisible = GeneralUIUtils.getDriver().findElement(By.className(parentElementClassAttribute));
			GeneralUIUtils.unhideElement(fileInputElementWithVisible, parentElementClassAttribute);
			GeneralUIUtils.ultimateWait();
		}
		SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Input buttons now visible..."));
	}
	
	public static void clickOnElementByText(String textToClick, String customizationFoLog){
		String customizationFoLogLocal = customizationFoLog != null ? customizationFoLog : "";
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s %s", textToClick, customizationFoLogLocal));
		GeneralUIUtils.clickOnElementByText(textToClick);
	}
	
	public static  void createPNF(ResourceReqDetails resource, User user) throws Exception {
		ExtentTestActions.log(Status.INFO, "Going to create a new PNF.");
		createResource(resource, user, DataTestIdEnum.Dashboard.BUTTON_ADD_PNF);
	}
}
