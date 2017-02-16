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

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.BreadCrumbsButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.CatalogFilterTitlesEnum;
import org.openecomp.sdc.ci.tests.datatypes.CheckBoxStatusEnum;
import org.openecomp.sdc.ci.tests.datatypes.CreateAndImportButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.MenuOptionsEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.TypesEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;

import com.relevantcodes.extentreports.LogStatus;

public final class GeneralUIUtils {
	
	private static int timeOut=60*3;

	public static final String FILE_NAME = "Valid_tosca_Mycompute.yml";

	/**************** DRIVERS ****************/
	private static WebDriver driver;

	public static void findComponentAndClick(String componentName) throws Exception {
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "finding component " + componentName);
		waitFordataTestIdVisibility("main-menu-input-search").sendKeys(componentName);
		WebElement foundComp = null;
		try {
			foundComp = waitFordataTestIdVisibility(componentName);
			foundComp.click();
			GeneralUIUtils.waitForLoader();
			waitFordataTestIdVisibility("formlifecyclestate");
		} catch (Exception e) {
			String msg = String.format("DID NOT FIND A COMPONENT NAMED %s", componentName);
			SetupCDTest.getExtendTest().log(LogStatus.FAIL, msg);
			System.out.println(msg);
			Assert.fail(msg);
		}
	}

	public static List<WebElement> getElemenetsFromTable(By by) {
		return GeneralUIUtils.getDriver().findElements(by);
	}

	private static List<WebElement> getNewButtonsList() {
		WebElement createButtonsArea = driver.findElement(By.className("w-sdc-dashboard-card-new"));
		createButtonsArea.click();
		List<WebElement> buttonsList = driver.findElements(By.className("w-sdc-dashboard-card-new-button"));
		return buttonsList;
	}

	public static final String FILE_PATH = System.getProperty("user.dir") + "\\src\\main\\resources\\Files\\";
	public static String fileName = "JDM_vfc.yml";
	public static final String toscaErrorMessage = "Invalid TOSCA template.";
	public static final String yamlError = "Invalid YAML file.";
	public static final String allReadyExistErro = "Imported resource already exists in ASDC Catalog.";

	public static WebElement hoverOnArea(String areaId) {
		Actions actions = new Actions(driver);
		WebElement area = getWebElementWaitForVisible(areaId);
		actions.moveToElement(area).perform();
		return area;
	}

	public static WebElement actionBuild(WebElement element) throws InterruptedException {
		// make an action on page//hover on element
		Actions build = new Actions(driver); // here you state ActionBuider
		build.moveToElement(element).build().perform();// hover the element.
		Thread.sleep(1000);
		return element;
	}

	public static File takeScreenshot(String zipFile, String dir, String testName) throws IOException {
		if (zipFile == null) {
			zipFile = testName;
		}
		try {
			File scrFile = ((TakesScreenshot) GeneralUIUtils.getDriver()).getScreenshotAs(OutputType.FILE);
			File filePath = new File(String.format("%s/%s.png", dir, zipFile));
			new File(dir).mkdirs();
			FileUtils.copyFile(scrFile, filePath);
			return filePath;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public static void errorMessagePopupHandle(@SuppressWarnings("rawtypes") Supplier func) throws Exception {
		try {
			WebElement errorMessagePopupHeader = getDriver().findElement(By.className("w-sdc-modal-head-text"));
			if (errorMessagePopupHeader.getText().equals("Error")) {
				WebElement okButton = getWebButton("OK");
				if (okButton.isDisplayed()) {
					okButton.click();
					func.get();
				}
			}
		} catch (Exception e) {
			throw new Exception("something went wrong, can't do anything");
		}
	}

	public static void waitForLoader() {
		waitForElements(By.className("tlv-loader"), 200, 3 * 60 * 1000);
	}

	public static void waitForLoaderOnboarding() {
		waitForElements(By.className("tlv-loader"), 200, 13 * 60 * 1000);
	}

	public static List<WebElement> waitForElements(By by, int napPeriod, int maxWaitMS) {
		boolean isKeepWaiting = true;
		int currentWaitTimeMS = 0;
		List<WebElement> elements = null;
		while (isKeepWaiting) {
			elements = getDriver().findElements(by);
			isKeepWaiting = elements.size() > 0;
			if (isKeepWaiting) {
				sleep(napPeriod);
				currentWaitTimeMS += napPeriod;
				if (currentWaitTimeMS > maxWaitMS) {
					isKeepWaiting = false;
				}
			}
		}

		return elements;

	}

	public static WebDriver getDriver() {
		return driver;
	}

	public static WebElement rihtPanelAPI() {
		return getWebElementWaitForVisible("tab-api");
	}

	public static void scrollDown() throws AWTException {
		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_DOWN);
		robot.keyRelease(KeyEvent.VK_DOWN);
	}

	// solution for "element not attached to the DOM anymore"
	public static List<WebElement> getWorkspaceElements() throws InterruptedException {
		Thread.sleep(1000);
		List<WebElement> assets = GeneralUIUtils.getEelementsByClassName("w-sdc-dashboard-card-body");
		return assets;
	}

	public static String getMethodName(Method method) {
		return method.getName();
	}

	public static FileWriter InitializeprintToTxt(String testName) {
		String idForTxtFile = new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(new Date());
		File file = new File(testName + idForTxtFile);
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, true);

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getLocalizedMessage());
		}
		return fw;
	}

	public static void closeFileWriter(FileWriter file) throws IOException {
		file.flush();
		file.close();
	}

	public static WebElement createAndImportButtons(CreateAndImportButtonsEnum type, WebDriver driver)
			throws InterruptedException {
		switch (type) {
		case IMPORT_CP:
		case IMPORT_VFC:
		case IMPORT_VL:
			hoverOnArea("importButtonsArea");
			return GeneralUIUtils.getWebElementWaitForVisible("importVFCbutton");

		case IMPORT_VF:
			hoverOnArea("importButtonsArea");
			return GeneralUIUtils.getWebElement(driver, "importVFbutton");
		case CREATE_SERVICE:
			hoverOnArea("AddButtonsArea", driver);
			GeneralUIUtils.getWebElementWaitForVisible("createServiceButton").click();
			;
			break;

		case CREATE_PRODUCT:
			GeneralUIUtils.getWebElement(driver, "createServiceButton").click();
			GeneralUIUtils.getWebElementWaitForVisible("createServiceButton").click();
			break;

		default:
			hoverOnArea("AddButtonsArea");
			driver.findElement(By.xpath("//*[@data-tests-id='createResourceButton']")).click();
			break;
		}
		return null;

	}

	public static String checkBoxLifeCyclestate(CheckBoxStatusEnum lifeCycle) {
		String Status = "IN DESIGN CHECK OUT";
		switch (lifeCycle) {
		case CHECKIN:
			Status = "IN DESIGN CHECK IN";
			if (GeneralUIUtils.getWebElementWaitForVisible(lifeCycle.getValue()).isDisplayed()) {
				GeneralUIUtils.getWebElementWaitForVisible(lifeCycle.getValue()).click();
			}
			break;
		case CHECKOUT:
			GeneralUIUtils.getWebElementWaitForVisible(lifeCycle.getValue()).click();
			Status = "IN DESIGN CHECK OUT";
			break;
		case IN_TESTING:
			GeneralUIUtils.getWebElementWaitForVisible(lifeCycle.getValue()).click();
			Status = "IN TESTING";
			break;
		case READY_FOR_TESTING:
			GeneralUIUtils.getWebElementWaitForVisible(lifeCycle.getValue()).click();
			Status = "READY FOR TESTING";
			break;
		case CERTIFIED:
			GeneralUIUtils.getWebElementWaitForVisible(lifeCycle.getValue()).click();
			Status = "CERTIFIED";
			break;
		}
		return Status;
	}

	public static String setFileTypeAndGetUniqId(ResourceTypeEnum fileType, ResourceReqDetails resourceDetails,
			User user) throws IOException, Exception {
		resourceDetails.setResourceType(fileType.toString());
		RestCDUtils.getResource(resourceDetails, user);
		return resourceDetails.getUniqueId();
	}

	public static void minimizeCatalogFilterByTitle(CatalogFilterTitlesEnum titlesEnum) {

		switch (titlesEnum) {
		case CATEGORIES:
			GeneralUIUtils.getWebElementWaitForVisible(titlesEnum.getValue()).click();
			break;
		case STATUS:
			GeneralUIUtils.getWebElementWaitForVisible(titlesEnum.getValue()).click();
			break;
		case TYPE:
			GeneralUIUtils.getWebElementWaitForVisible(titlesEnum.getValue()).click();
			break;
		default:
			break;
		}
		// webElementWaitForVisible.get(0).click();
		// }
	}

	public static WebElement getWebElementWaitForVisible(String dataTestId) {
		// try{
		return waitFordataTestIdVisibility(dataTestId);
		// }
		// catch(Exception e){
		// try{
		// WebElement errorMessagePopupHeader =
		// GeneralUIUtils.getDriver().findElement(By.className("w-sdc-modal-head-text"));
		// if (errorMessagePopupHeader.getText().equals("Error")){
		// WebElement okButton = GeneralUIUtils.getWebButton("OK");
		// if (okButton.isDisplayed()){
		//// takeScreenshot(LocalDateTime.now().toString().replaceAll(":", ""),
		// SetupCDTest.SCREENSHOT_LOG_DIR);
		// okButton.click();
		// return getWebElementWaitForVisible(dataTestId);
		// }
		// }
		// }
		// catch(Exception exception){
		// System.out.println(String.format("didn't find element with
		// data-tests-id of %s", dataTestId));
		// }
		// }
		// return null;

	}

	public static WebElement getWebElementById(String id) {
		WebDriverWait wait = new WebDriverWait(driver, 5);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='" + id + "']")));
	}

	public static WebElement getWebElementByName(String name) {
		return driver.findElement(By.name(name));
	}

	// New tedy , this function will get the web elements by The new attribute
	// value(data-tests-id)
	public static List<WebElement> getWebElements(String dataTestId) {
		return waitForElementsListVisibility(dataTestId);
	}

	// New tedy , this function will get the web element Button by The new
	// attribute value(data-tests-id)
	public static WebElement getWebButton(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
	}

	// New tedy , this function will wait till the web element be
	// visible(data-tests-id)
	public static Boolean waitForInvisibileElement(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait.until(
				ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
	}

	public static WebElement waitFordataTestIdVisibility(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
	}

	public static boolean clickcheckbox(String category) {
		try {

		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static WebElement waitForContainsdataTestIdVisibility2(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(driver, 5);
		return wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.xpath("//*[contains (@data-tests-id, '" + dataTestId + "'])")));
	}

	public static List<WebElement> waitForContainsdataTestIdVisibility(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(driver, 5);
		return wait.until(ExpectedConditions
				.visibilityOfAllElementsLocatedBy(By.xpath("//*[contains (@data-tests-id, '" + dataTestId + "'])")));
	}

	public static WebElement waitForClassNameVisibility(String className) {
		return waitForElementVisibility(By.className(className));
	}

	public static WebElement waitForElementVisibility(By by) {
		return waitForElementVisibility(by, 3 * 60);
	}

	public static WebElement waitForElementVisibility(By by, int duration) {
		WebDriverWait wait = new WebDriverWait(driver, duration);
		return wait.until(ExpectedConditions.visibilityOf(driver.findElement(by)));
	}

	public static List<WebElement> waitForElementsListVisibility(By by) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(by)));
	}
	
	public static boolean waitForElementsListInvisibility(By by) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
	}

	// New tedy , this function will wait till the web elements be
	// visible(data-tests-id)
	public static List<WebElement> waitForElementsListVisibility(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		List<WebElement> findElements = wait.until(ExpectedConditions
				.visibilityOfAllElementsLocatedBy(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
		if (findElements.size() > 0) {
			return findElements;
		}
		System.out.println("Elements not Exist!");
		return null;
	}

	public static List<WebElement> waitForElementsListVisibilityTestMethod(String dataTestId) {
		return driver.findElements(By.xpath("//*[@data-tests-id='" + dataTestId + "']"));
	}

	public static WebElement waitForBrowseButton(String dataTestId) {

		return driver.findElement(By.xpath("//*[@data-tests-id='" + dataTestId + "']"));
	}

	public static List<WebElement> getWebElementsListByDataTestId(String dataTestId) {
		return driver.findElements(By.xpath("//*[@data-tests-id='" + dataTestId + "']"));

	}

	public static WebElement getWebElementByDataTestId(String dataTestId) {
		return driver.findElement(By.xpath("//*[@data-tests-id='" + dataTestId + "']"));
	}

	public static WebElement waitUntilClickableButton(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait.until(ExpectedConditions
				.elementToBeClickable(driver.findElement(By.xpath("//*[@data-tests-id='" + dataTestId + "']"))));
	}

	// interface Throws {
	// <T,R, E extends Exception> R apply(T t) throws E;
	// }
	// public static <R> R swallowException(Throws<T,R,E> supplier){
	// R returnValue;
	// try{
	// returnValue = supplier.get();
	// }
	// catch(Exception e){
	// returnValue = null;
	// }
	// return returnValue;
	// }
	// Use this method only for special cases, otherwise use
	// org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils.getWebElementWaitForVisible(WebDriver,
	// String)

	public static boolean isElementPresent(String dataTestId) {
		try {
			driver.findElement(By.xpath("//*[@data-tests-id='" + dataTestId + "']"));
			return true;
		} catch (org.openqa.selenium.NoSuchElementException e) {
			return false;
		}
	}

	public static <R> R retryMethodOnException(Supplier<R> supplier) {
		boolean stopSearch = false;
		R ret = null;
		Exception throwMe = null;
		int timeElapsed = 0;
		while (!stopSearch) {
			try {
				ret = supplier.get();
			} catch (Exception e) {
				throwMe = e;
				GeneralUIUtils.sleep(250);
				timeElapsed += 250;
				if (timeElapsed > 5000) {
					stopSearch = true;
				}

			} finally {
				if (ret != null) {
					stopSearch = true;
				}
			}
		}
		if (ret == null) {
			throw new RuntimeException(throwMe);
		} else {
			return ret;
		}

	}

	// this method will login as tester and start test or accept Assets.
	public static void testerUser(Boolean startTest, Boolean accept, ResourceReqDetails resource) throws Exception {
		// GeneralUIUtils.getWebElement(ResourceUIUtils.getName()).click();
		String url = "http://localhost:8181/sdc1/proxy-tester1#/dashboard";
		sleep(2000);
		SetupCDTest.navigateToUrl(url);
		GeneralUIUtils.getWebElementWaitForVisible(resource.getName()).click();

		if (startTest) {
			clickStartTesting();
		}

		if (accept) {
			clickAccept();
		}
	}

	public static void governorUser(Boolean reject, Boolean approve, ResourceReqDetails resource) throws Exception {
		// GeneralUIUtils.getWebElement(ResourceUIUtils.getName()).click();
		String url = "http://localhost:8181/sdc1/proxy-governor1#/dashboard";
		sleep(2000);
		SetupCDTest.navigateToUrl(url);
		GeneralUIUtils.getWebElementWaitForVisible("w-sdc-dashboard-card-info");
		GeneralUIUtils.getWebElementWaitForVisible(resource.getName()).click();
		if (reject) {
			clickReject();
		}
		if (approve) {
			clickApprove();
		}
		sleep(1000);
	}

	public static void opsUser(Boolean disribute, Boolean reDisribute, ResourceReqDetails resource) throws Exception {
		// GeneralUIUtils.getWebElement(ResourceUIUtils.getName()).click();
		String url = "http://localhost:8181/sdc1/proxy-ops1#/dashboard";
		sleep(2000);
		SetupCDTest.navigateToUrl(url);
		sleep(2000);
		GeneralUIUtils.getWebElementWaitForVisible("w-sdc-dashboard-card-info");
		GeneralUIUtils.getWebElementWaitForVisible(resource.getName()).click();
		if (reDisribute) {
			clickReDistribute();
		}
		if (disribute) {
			clickDistribute();
		}
		sleep(1000);
	}

	// this function located select list by the data-test-id value and the item
	// to be selected..
	public static Select getSelectList(String item, String datatestsid) {
		Select selectlist = new Select(waitFordataTestIdVisibility(datatestsid));
		if (item != null) {
			selectlist.selectByVisibleText(item);
		}
		return selectlist;
	}

	// Define description area .
	public static String defineDescription(String descriptionText) {

		WebElement resourceDescriptionTextbox = GeneralUIUtils.getWebElementWaitForVisible("description");
		resourceDescriptionTextbox.clear();
		resourceDescriptionTextbox.sendKeys(descriptionText);
		return descriptionText;
	}

	public static WebElement catalogSearchBox(String searchText) {
		WebElement searchBox = GeneralUIUtils.getWebElementWaitForVisible("main-menu-input-search");
		searchBox.clear();
		searchBox.sendKeys(searchText);
		return searchBox;
	}

	// enum
	public static void selectMenuOptionbyname(List<WebElement> options, MenuOptionsEnum optionName)
			throws InterruptedException {

		for (WebElement webElement : options) {
			if (webElement.getText().equals(optionName.getValue())) {
				actionBuild(webElement).click();
			} else {
				System.out.println("No such element!");
			}
		}

	}

	// back to workspace by Clicking the ASDC Logo.!
	public static void clickASDCLogo() {
		WebDriverWait wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("ASDC")));
		WebElement ClickASDCLogo = driver.findElement(By.linkText("ASDC"));
		ClickASDCLogo.click();
		GeneralUIUtils.waitForLoader();
	}

	public static void clickExitSign() throws InterruptedException {
		WebElement exitSign = driver.findElement(By.xpath("//*[contains(@class, 'x-btn')]"));
		actionBuild(exitSign);
		Thread.sleep(1000);
		driver.findElement(By.xpath("//*[contains(@class, 'x-btn')]")).click();
	}

	public static void clickCreateButton() throws Exception {
		getWebButton("create/save").click();
		GeneralUIUtils.waitForLoader();
		getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.CHECK_IN.getValue());
	}

	public static void clickUpdateButton() throws Exception {
		GeneralUIUtils.sleep(500);
		clickCreateButton();
	}

	public static void checkOut() throws InterruptedException, AWTException {
		actionBuild(getWebButton("check_out"));
		getWebButton("check_out").click();
		waitForInvisibileElement("check_out");
	}

	public static void clickStartTesting() throws InterruptedException {
		actionBuild(getWebButton("start_testing"));
		getWebButton("start_testing").click();
		waitForInvisibileElement("start_testing");
		getWebButton("create/save").click();
	}

	public static void clickAccept() throws InterruptedException {
		actionBuild(getWebButton("accept"));
		getWebButton("accept").click();
		getWebElementWaitForVisible("checkindialog").sendKeys("Accept!");
		clickOkButton();
		sleep(1000);
	}

	public static void clickReject() throws InterruptedException {
		actionBuild(getWebButton("reject"));
		getWebButton("reject").click();
		waitForInvisibileElement("reject");
	}

	public static void clickApprove() throws InterruptedException {
		actionBuild(getWebButton("approve"));
		getWebButton("approve").click();
		waitForInvisibileElement("approve");
	}

	public static void clickDistribute() throws InterruptedException {
		actionBuild(getWebButton("distribute"));
		getWebButton("distribute").click();
		waitForInvisibileElement("redistribute");
	}

	public static void clickReDistribute() throws InterruptedException {
		actionBuild(getWebButton("redistribute"));
		getWebButton("redistribute").click();
	}

	public static void clickCancel() {
		getWebButton("cancel").click();
		waitForInvisibileElement("cancel");
	}

	public static void checkIn() throws InterruptedException {
		actionBuild(getWebButton("check_in"));
		getWebButton("check_in").click();
		getWebElementWaitForVisible("checkindialog").sendKeys("Check in!");
		clickOkButton();
		waitForInvisibileElement("checkindialog");
	}

	public static void clickSaveIcon() throws InterruptedException {
		actionBuild(GeneralUIUtils.waitFordataTestIdVisibility("create/save"));
		GeneralUIUtils.getWebButton(/* "delete_version" */"create/save").click();
		Thread.sleep(1000);

	}

	// Open menu of Created Object and select option.
	public static void openObjectMenuAndSelectOption(String uniqid, MenuOptionsEnum optionName)
			throws InterruptedException, AWTException {
		WebElement hoverOnMenu = actionBuild(getWebElementWaitForVisible(uniqid));
		List<WebElement> menuOptions = hoverOnMenu.findElement(By.xpath("./following-sibling::*[1]"))
				.findElements(By.xpath(".//*"));
		selectMenuOptionbyname(menuOptions, optionName);
	}

	// Get elements by className.
	public static WebElement getEelementByClassName(String element) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
			return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@class='" + element + "']")));
		} catch (Exception e) {
			return null;
		}
	}

	public static List<WebElement> getEelementsByClassName(String element) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait
				.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@class='" + element + "']")));
	}

	public static WebElement getEelementByContainsdatatestsid(String datatestId) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.xpath("//*[contains(@data-tests-id, '" + datatestId + "')]")));
	}

	// list
	public static List<WebElement> getEelementsByContainsDataTestsId(String datatestId) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait.until(ExpectedConditions
				.visibilityOfAllElementsLocatedBy(By.xpath("//*[contains(@data-tests-id, '" + datatestId + "')]")));
	}

	public static WebElement getEelementBycontainsClassName(String classname) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(@class, '" + classname + "')]")));
	}

	public static WebElement getEelementByLinkText(String linkText) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@text='" + linkText + "']")));
	}

	public static List<WebElement> getEelementsBycontainsClassName(String classname) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait.until(ExpectedConditions
				.visibilityOfAllElementsLocatedBy(By.xpath("//*[contains(@class, '" + classname + "')]")));
	}

	public static WebElement getButtonByClassName(String element) {
		WebDriverWait wait = new WebDriverWait(driver, 3 * 60);
		return wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@class='" + element + "']")));
	}

	public static void checkinCheckout(String elementName) throws Exception, Exception {
		checkIn();
		getWebElementWaitForVisible(elementName).click();
		;
		checkOut();

	}

	public static void moveToStep(DataTestIdEnum.StepsEnum Stepname) {
		moveToStep(Stepname.getValue());
	}

	public static void moveToStep(String dataTestId) {
		getWebButton(dataTestId).click();
		waitForLoader();
	}

	public static void editFile(String name) {
		WebElement editfilebutton = driver.findElement(By.id("edit" + name + ""));
		editfilebutton.click();
	}

	public static void deleteFile(String name) {
		WebElement deletebutton = driver.findElement(By.id("delete" + name + ""));
		deletebutton.click();
	}

	public static void downloadFile(String name) {
		WebElement downloadbutton = driver.findElement(By.id("download" + name + ""));
		downloadbutton.click();
	}

	public static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static void hasDriver() {
		try {
			driver.getCurrentUrl();
			driver.quit();
		} catch (NullPointerException e) {
		}
	}

	public static void initDriver() {
		try {
			boolean remoteTesting = SetupCDTest.config.isRemoteTesting();
			if (!remoteTesting) {
				System.out.println("opening LOCAL browser");
				driver = new FirefoxDriver();

			} else {
				System.out.println("opening REMOTE browser");
				String remoteEnvIP = SetupCDTest.config.getRemoteTestingMachineIP();
				String remoteEnvPort = SetupCDTest.config.getRemoteTestingMachinePort();
				DesiredCapabilities cap = new DesiredCapabilities();
				cap = DesiredCapabilities.firefox();
				cap.setPlatform(Platform.WINDOWS);
				cap.setBrowserName("firefox");

				String remoteNodeUrl = String.format(SetupCDTest.SELENIUM_NODE_URL, remoteEnvIP, remoteEnvPort);
				driver = new RemoteWebDriver(new URL(remoteNodeUrl), cap);
			}


		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

	}

	public static void windowZoomOut() {
		final int zoomOutFactor = 2;
		for (int i = 0; i < zoomOutFactor; i++) {
			driver.findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
		}
	}

	public static void defineTagsList(ResourceReqDetails resource, String[] resourceTags) {
		List<String> taglist = new ArrayList<String>();
		;
		WebElement resourceTagsTextbox = getWebElementWaitForVisible("i-sdc-tag-input");
		for (String tag : resourceTags) {
			resourceTagsTextbox.clear();
			resourceTagsTextbox.sendKeys(tag);
			resourceTagsTextbox.sendKeys(Keys.ENTER);
			taglist.add(tag);
			// waitForElements(By.className("sdc-loader"), 250, 15000);
		}
	}

	// public static List<WebElement> waitForElements(By by, int napPeriod, int
	// maxWaitMS){
	// List<WebElement> elements = null;
	//
	// elements = getDriver().findElements(by);
	// if( currentWaitTimeMS > maxWaitMS){
	// }
	// resource.setTags(taglist);
	// return elements;
	// }
	public static void selectTabInRightPallete(String className) throws Exception {
		WebElement tab = getEelementBycontainsClassName(className);
		tab.click();
	}

	public static WebElement getWebElement(WebDriver driver, String dataTestId) {
		return waitForElementVisibility(dataTestId);
	}

	public static void clickOkButton() throws InterruptedException {
		// actionBuild(getWebButton("OK"));
		// sleep(2000);
		getWebButton("OK").click();
	}

	public static WebElement waitForElementVisibility(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 30);
		return wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
	}

	public static WebElement deleteVersion() {
		return GeneralUIUtils.waitFordataTestIdVisibility("delete_version");
	}

	// public static List<WebElement> getFilterTitles() throws Exception {
	//
	// return
	// GeneralUIUtils.getEelementsByClassName("i-sdc-designer-leftbar-section-title-text");
	//
	// }

	public static void deleteVersionInUI() throws Exception {

		actionBuild(deleteVersion());
		deleteVersion().click();
		GeneralUIUtils.clickOkButton();
	}

	public static void uploadFileWithJavaRobot(String FilePath, String FileName) throws Exception {
		StringSelection sel = new StringSelection(FilePath + FileName);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
		Thread.sleep(1000);
		Robot robot = new Robot();
		robot.delay(1000);

		// Release Enter
		robot.keyRelease(KeyEvent.VK_ENTER);

		// Press CTRL+V
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);

		// Release CTRL+V
		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyRelease(KeyEvent.VK_V);
		Thread.sleep(1000);

		// Press Enter
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);
		Thread.sleep(3000);
	}

	public static String catalogFilterTypeChecBox(TypesEnum enumtype) throws Exception {
		String Type = enumtype.toString().toLowerCase();
		getWebElementWaitForVisible(enumtype.getValue()).click();
		return Type;
	}

	public static List<String> catalogFilterStatusChecBox(CheckBoxStatusEnum statusEnum) throws Exception {
		List<String> status = null;
		switch (statusEnum) {
		case IN_DESIGN:
			status = Arrays.asList("NOT_CERTIFIED_CHECKIN", "NOT_CERTIFIED_CHECKOUT");
			getWebElementWaitForVisible(statusEnum.getCatalogValue()).click();
			break;
		case READY_FOR_TESTING:
			status = Arrays.asList("READY_FOR_CERTIFICATION");
			getWebElementWaitForVisible(statusEnum.getCatalogValue()).click();
			break;
		case IN_TESTING:
			status = Arrays.asList("CERTIFICATION_IN_PROGRESS");
			getWebElementWaitForVisible(statusEnum.getCatalogValue()).click();
			break;
		case CERTIFIED:
			status = Arrays.asList("CERTIFIED");
			getWebElementWaitForVisible(statusEnum.getCatalogValue()).click();
			break;
		case DISTRIBUTED:
			status = Arrays.asList("CERTIFIED");
			getWebElementWaitForVisible(statusEnum.getCatalogValue()).click();
			break;
		}
		return status;
	}

	public static void clickBreadCrumbs(BreadCrumbsButtonsEnum button) {
		switch (button) {
		case CATALOG:
			GeneralUIUtils.getWebButton(button.getButton()).click();
			break;
		case HOME:
			GeneralUIUtils.getWebButton(button.getButton()).click();
			break;
		case ON_BOARDING:
			GeneralUIUtils.getWebButton(button.getButton()).click();
			break;
		default:
			break;
		}
	}

	public static void clickPrintScreen() {
		getEelementByClassName("e-sdc-small-print-screen").click();
	}

	public static void clickSubmitForTest() throws InterruptedException {
		getWebButton("submit_for_testing").click();
		WebElement commentText = getDriver().findElement(By.className("w-sdc-modal-body-email"));
		commentText.sendKeys("Submit For Test");
		sleep(3000);
		clickOkButton();
	}

	public static WebElement hoverOnArea(String areaId, WebDriver driver) {
		Actions actions = new Actions(driver);
		WebElement area = getWebElement(driver, areaId);
		actions.moveToElement(area).perform();
		return area;
	}

	public static WebElement moveToNextStep(DataTestIdEnum.StepsEnum Stepname) {
		return getWebButton(Stepname.getValue());
	}

	public static String getComponentVersion(String componentName) {
		return GeneralUIUtils.getWebElementWaitForVisible(componentName + "Version").getText();
	}

	public static void clickOnHTMLElementByDataTestId(String dataTestId) throws Exception {
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 20);
		StopWatch performanceMesuring = new StopWatch();
		performanceMesuring.start();
		WebElement element = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
		performanceMesuring.stop();
		element.click();
		performanceMesuring(dataTestId, performanceMesuring);

	}

	private static void performanceMesuring(String dataTestId, StopWatch performanceMesuring) {
		Reporter.log("click on: " + dataTestId);
		System.out.println("click on: " + dataTestId);
		Reporter.log("Total Load Time Until click on button : " + dataTestId + " "
				+ (((double) performanceMesuring.getTime()) / 1000) + " seconds");
		System.out.println("Total Load Time Until click on button : " + dataTestId + " "
				+ (((double) performanceMesuring.getTime()) / 1000) + " seconds");
		performanceMesuring.reset();
		performanceMesuring.start();
		Boolean waitForElementInVisibilityByClassName = GeneralUIUtils.waitForElementInVisibilityByClassName(driver,
				"tlv-loader");
		performanceMesuring.stop();
		Reporter.log("Total time before loader disappear: " + (((double) performanceMesuring.getTime()) / 1000)
				+ " seconds");
		System.out.println("Total time before loader disappear: " + (((double) performanceMesuring.getTime()) / 1000)
				+ " seconds");
	}

	public static Boolean waitForElementInVisibilityByClassName(WebDriver driver, String className) {
		WebDriverWait wait = new WebDriverWait(driver, 30);
		return wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className(className)));
	}

	public static void findComponentAndClick(ResourceReqDetails resource) throws Exception {

		WebElement searchTextbox = GeneralUIUtils.getWebElementWaitForVisible("main-menu-input-search");
		searchTextbox.clear();
		searchTextbox.sendKeys(resource.getName());
		clickOnHTMLElementByDataTestId(resource.getName());
	}

	public static void clickOnHTMLElementBylinkText(String linkText) throws Exception {
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 20);
		StopWatch performanceMesuring = new StopWatch();
		performanceMesuring.start();
		WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
		performanceMesuring.stop();
		element.click();
		performanceMesuring(linkText, performanceMesuring);
	}
	
    public static void ultimateWait(){
    	long startTime = System.nanoTime();                    

    	GeneralUIUtils.waitForLoader();
		GeneralUIUtils.waitForAngular();
		
		long estimateTime = System.nanoTime();
		long duration = TimeUnit.NANOSECONDS.toSeconds(estimateTime - startTime);
		if(duration > timeOut){
			SetupCDTest.getExtendTest().log(LogStatus.WARNING, String.format("Delays on page, %d seconds", duration));
		}
    }
    
    public static void waitForAngular(){
    	WebDriverWait wait = new WebDriverWait(getDriver(), 90, 100);
    	wait.until(AdditionalConditions.pageLoadWait());
    	wait.until(AdditionalConditions.angularHasFinishedProcessing());
    }

}
