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

package org.openecomp.sdc.ci.tests.execute.setup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.UserCredentials;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.run.StartTest;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.rest.CatalogRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public abstract class SetupCDTest {

	public SetupCDTest() {
		// LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		// lc.getLogger("org.apache").setLevel(Level.INFO);
		//// System.setProperty("org.apache.commons.logging.Log",
		// "org.apache.commons.logging.impl.SimpleLog");
		//// System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire",
		// "OFF");
		//// System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient",
		// "OFF");

	}

	public static Logger logger = Logger.getLogger(SetupCDTest.class.getName());

	/**************** CONSTANTS ****************/
	private static final String CREDENTIALS_FILE = "credentials.yaml";
	private static final String REPORT_FILE_NAME = "ASDC_UI_Extent_Report.html";
	public static final String REPORT_FOLDER = "./ExtentReport/";
	public static final String SELENIUM_NODE_URL = "http://%s:%s/wd/hub";
	private static final String SCREENSHOT_FOLDER = REPORT_FOLDER + "screenshots/";
	private static final String SHORT_CSV_REPORT_FILE_NAME = "ShortReport.csv";
	private static final int NUM_OF_ATTEMPTS_TO_REFTRESH = 2;

	/**************** USERS ****************/
	protected static User designerUser;
	protected static User adminUser;
	protected static User testerUser;
	protected static User governorUser;
	protected static User opsUser;
	protected static User productManagerUser;

	public static Config config;

	/**************** PRIVATES ****************/
	private Map<?, ?> credentialsYamlFileMap;
	private static String url;
	private User user;
	private static boolean localEnv = true;
	private int refreshAttempts = 0;

	protected abstract UserRoleEnum getRole();

	protected ExtentReports extentReport;
	protected static ExtentTest extendTest;
	private static String screenshotFile;

	public static String getScreenshotFile() {
		return screenshotFile;
	}

	public static void setScreenshotFile(String screenshotFile) {
		SetupCDTest.screenshotFile = screenshotFile;
	}

	public static ExtentTest getExtendTest() {
		return extendTest;
	}

	private OnboardCSVReport csvReport;

	public OnboardCSVReport getCsvReport() {
		return csvReport;
	}

	/**************** BEFORE ****************/

	@BeforeSuite(alwaysRun = true)
	public void setEnvParameters() throws Exception {

		File dir = new File(REPORT_FOLDER);
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
		}
		extentReport = new ExtentReports(REPORT_FOLDER + REPORT_FILE_NAME);
		csvReport = new OnboardCSVReport(REPORT_FOLDER, SHORT_CSV_REPORT_FILE_NAME);

		System.out.println("Setup....");
		config = Utils.getConfig();
		setUrl();
	}

	@BeforeMethod(alwaysRun = true)
	public void setBrowserBeforeTest(java.lang.reflect.Method method) throws Exception {
		extendTest = extentReport.startTest(method.getName());
		extendTest.log(LogStatus.INFO, "Test started");
		setBrowserBeforeTest(getRole());
	}

	/**************** AFTER ****************/
	@AfterMethod(alwaysRun = true)
	public void quitAfterTest(ITestResult result) throws Exception {

		StringBuilder sb = new StringBuilder();
		if (result.getParameters().length != 0) {
			for (int i = 0; i < result.getParameters().length - 1; i++) {
				sb.append(result.getParameters()[i].toString() + ",");
			}
			sb.append(result.getParameters()[result.getParameters().length - 1].toString());
		}

		File imageFilePath = GeneralUIUtils.takeScreenshot(screenshotFile, SCREENSHOT_FOLDER, result.getName());
		final String absolutePath = new File(REPORT_FOLDER).toURI().relativize(imageFilePath.toURI()).getPath();
		if (result.getStatus() == ITestResult.SUCCESS) {
			extendTest.log(LogStatus.PASS, "Test Result : <span class='label success'>Success</span>");
			extendTest.log(LogStatus.PASS,
					"Finished the test with the following screenshot : " + extendTest.addScreenCapture(absolutePath));
			csvReport.writeRow(result.getName(), sb.toString(), "PASS");
		} else if (result.getStatus() == ITestResult.FAILURE || result.getStatus() == ITestResult.SKIP) {
			extendTest.log(LogStatus.ERROR, "ERROR - The following exepction occured");
			extendTest.log(LogStatus.ERROR, result.getThrowable());
			extendTest.log(LogStatus.ERROR,
					"Failure is described in the following screenshot : " + extendTest.addScreenCapture(absolutePath));
			extendTest.log(LogStatus.FAIL, "<span class='label failure'>Failure</span>");
			csvReport.writeRow(result.getName(), sb.toString(), "FAIL");
		}

		extentReport.endTest(extendTest);
		extentReport.flush();

		quitBrowser();
//		deleteCreatedComponents2(getCatalogAsMap());
	}

	@AfterClass(alwaysRun = true)
	public void afterSuite() {
		csvReport.closeFile();
	}

	/*************************************/

	private Map<String, ArrayList<Component>> getCatalogAsMap() throws IOException {
		RestResponse catalog = CatalogRestUtils.getCatalog(getUser().getUserId());
		Map<String, ArrayList<Component>> convertCatalogResponseToJavaObject = ResponseParser
				.convertCatalogResponseToJavaObject(catalog.getResponse());
		return convertCatalogResponseToJavaObject;
	}

	private void deleteCreatedComponents2(Map<String, ArrayList<Component>> convertCatalogResponseToJavaObject)
			throws IOException {
		final String userId = getUser().getUserId();
		ArrayList<Component> resourcesArrayList = convertCatalogResponseToJavaObject.get("resources");

		List<String> collect = resourcesArrayList.stream().filter(s -> s.getName().startsWith("ci"))
				.map(e -> e.getUniqueId()).collect(Collectors.toList());
		for (String uId : collect) {
			ResourceRestUtils.deleteResource(uId, userId);
		}

		resourcesArrayList = convertCatalogResponseToJavaObject.get("services");
		collect = resourcesArrayList.stream().filter(s -> s.getName().startsWith("ci")).map(e -> e.getUniqueId())
				.collect(Collectors.toList());
		for (String uId : collect) {
			ServiceRestUtils.deleteServiceById(uId, userId);
		}

		resourcesArrayList = convertCatalogResponseToJavaObject.get("products");
		collect = resourcesArrayList.stream().filter(s -> s.getName().startsWith("ci")).map(e -> e.getUniqueId())
				.collect(Collectors.toList());
		for (String uId : collect) {
			ProductRestUtils.deleteProduct(uId, userId);
		}

	}

	/**************** MAIN ****************/
	public static void main(String[] args) {
		System.out.println("---------------------");
		System.out.println("running test from CLI");
		System.out.println("---------------------");

		String attsdcFilePath = FileHandling.getBasePath() + File.separator + "conf" + File.separator + "sdc.yaml";
		System.setProperty("config.resource", attsdcFilePath);
		System.out.println("sdc.yaml file path is : " + attsdcFilePath);

		Object[] testSuitsList = FileHandling
				.getFileNamesFromFolder(FileHandling.getBasePath() + File.separator + "testSuites", ".xml");
		if (testSuitsList != null) {
			System.out.println(String.format("Found %s testSuite(s)", testSuitsList.length));
			args = Arrays.copyOf(testSuitsList, testSuitsList.length, String[].class);
			StartTest.main(args);
		}
	}

	/***********************************************************************************/

	protected static String setUrl() {
		url = config.getUrl();
		if (url == null) {
			String message = "no URL found";
			System.out.println(message);
			Assert.fail(message);
		} else if (!url.contains("localhost") && !url.contains("127.0.0.1")) {
			localEnv = false;
		}
		return url;
	}

	private Map<String, String> loadCredentialsFile() throws Exception {
		File credentialsFile = new File(
				FileHandling.getBasePath() + File.separator + "conf" + File.separator + CREDENTIALS_FILE);
		if (!credentialsFile.exists()) {
			credentialsFile = new File(FileHandling.getConfFilesPath() + CREDENTIALS_FILE);
		}
		credentialsYamlFileMap = (Map<String, String>) FileHandling.parseYamlFile(credentialsFile.getAbsolutePath());
		return (Map<String, String>) credentialsYamlFileMap;
	}

	protected UserCredentials getUserCredentialsFromFile(String userRole) throws Exception {
		Map<String, String> credentialsMap = (Map<String, String>) credentialsYamlFileMap.get(userRole);
		String user = (String) credentialsMap.get("username");
		String password = (String) credentialsMap.get("password");
		String firstname = (String) credentialsMap.get("firstname");
		String lastname = (String) credentialsMap.get("lastname");

		return new UserCredentials(user, password, firstname, lastname);
	}

	public UserCredentials updateUserUserId(String role) throws Exception {
		System.out.println("updating...");
		UserCredentials designerCredentialsFromFile = null;
		UserCredentials testerCredentialsFromFile = null;
		UserCredentials adminCredentialsFromFile = null;
		UserCredentials opsCredentialsFromFile = null;
		UserCredentials governorCredentialsFromFile = null;
		UserCredentials productCredentialsFromFile = null;
		UserCredentials productManagerCredentialsFromFile = null;

		String lowerCaseRole = role.toLowerCase();
		try {
			if (lowerCaseRole.equals("designer")) {
				designerCredentialsFromFile = getUserCredentialsFromFile("designer");
				designerUser.setUserId(designerCredentialsFromFile.getUserId());
				designerUser.setFirstName(designerCredentialsFromFile.getFirstName());
				designerUser.setLastName(designerCredentialsFromFile.getLastName());
				return designerCredentialsFromFile;
			} else if (lowerCaseRole.equals("tester")) {
				testerCredentialsFromFile = getUserCredentialsFromFile("tester");
				testerUser.setUserId(testerCredentialsFromFile.getUserId());
				testerUser.setFirstName(testerCredentialsFromFile.getFirstName());
				testerUser.setLastName(testerCredentialsFromFile.getLastName());
				return testerCredentialsFromFile;
			} else if (lowerCaseRole.equals("admin")) {
				adminCredentialsFromFile = getUserCredentialsFromFile("admin");
				adminUser.setUserId(adminCredentialsFromFile.getUserId());
				adminUser.setFirstName(adminCredentialsFromFile.getFirstName());
				adminUser.setLastName(adminCredentialsFromFile.getLastName());
				return adminCredentialsFromFile;
			} else if (lowerCaseRole.equals("ops")) {
				opsCredentialsFromFile = getUserCredentialsFromFile("ops");
				opsUser.setUserId(opsCredentialsFromFile.getUserId());
				opsUser.setFirstName(opsCredentialsFromFile.getFirstName());
				opsUser.setLastName(opsCredentialsFromFile.getLastName());
				return opsCredentialsFromFile;
			} else if (lowerCaseRole == "governor") {
				governorCredentialsFromFile = getUserCredentialsFromFile("governor");
				governorUser.setUserId(governorCredentialsFromFile.getUserId());
				governorUser.setFirstName(governorCredentialsFromFile.getFirstName());
				governorUser.setLastName(governorCredentialsFromFile.getLastName());
				return governorCredentialsFromFile;
			} else if (lowerCaseRole == "product_local") {
				productCredentialsFromFile = getUserCredentialsFromFile("product_local");
				productManagerUser.setUserId(productCredentialsFromFile.getUserId());
				productManagerUser.setFirstName(productCredentialsFromFile.getFirstName());
				productManagerUser.setLastName(productCredentialsFromFile.getLastName());
				return productCredentialsFromFile;
			} else if (lowerCaseRole == "product_manager") {
				productManagerCredentialsFromFile = getUserCredentialsFromFile("product_manager");
				productManagerUser.setUserId(productManagerCredentialsFromFile.getUserId());
				productManagerUser.setFirstName(productManagerCredentialsFromFile.getFirstName());
				productManagerUser.setLastName(productManagerCredentialsFromFile.getLastName());
				return productManagerCredentialsFromFile;
			}
		}

		catch (Exception e) {
			System.out.print("An exception occured...");
			System.out.println("->exception message is : " + e.getMessage());
		}

		return null;
	}

	public static void navigateToUrl(String url) throws Exception {

		try {
			WebDriver driver = GeneralUIUtils.getDriver();
			System.out.println("navigating to URL :" + url);
			driver.manage().window().maximize();
			driver.manage().deleteAllCookies();
			driver.navigate().to(url);
			GeneralUIUtils.windowZoomOut();
			GeneralUIUtils.waitForLoader();
		} catch (Exception e) {
			System.out.println("browser is unreachable");
			extendTest.log(LogStatus.ERROR, "browser is unreachable");
			Assert.fail("browser is unreachable");
		}
	}

	protected void loginToSystem(UserCredentials credentials, UserRoleEnum role) throws Exception {

		sendUserAndPasswordKeys(credentials);
		refreshAttempts = (refreshAttempts == 0) ? NUM_OF_ATTEMPTS_TO_REFTRESH : refreshAttempts;
		if (!getRole().equals(UserRoleEnum.ADMIN)) {
			try {
				if(!localEnv){
//					GeneralUIUtils.ultimateWait();
					WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 3 * 60);
					WebElement sdcApp = wait.until(ExpectedConditions.elementToBeClickable(By.id("app-image-SDC")));
					sdcApp.click();
					GeneralUIUtils.getDriver().switchTo().frame(1);
					GeneralUIUtils.waitFordataTestIdVisibility("main-menu-input-search");
				}
				else{
					enterToUserWorkspace();
				}
				
			} catch (Exception e) {
				refreshAttempts--;
				if (refreshAttempts <= 0) {
					System.out.println("ERR : Something is wrong with browser!");
					Assert.fail("ERR : Something is wrong with browser!");
				}
				System.out.println("trying again...");
				System.out.println(String.format("%s attempt(s) left", refreshAttempts));
				extendTest.log(LogStatus.INFO, "trying again...");
				extendTest.log(LogStatus.INFO, String.format("%s attempt(s) left", refreshAttempts));

				quitAndReLogin(role);
			}
		}
	}

	private void sendUserAndPasswordKeys(UserCredentials userId) {
		
		if (localEnv){
			System.out.println("Login with user : " + userId.getUserId());
			WebElement userNameTextbox = GeneralUIUtils.waitForElementVisibility(By.name("userid"));
			userNameTextbox.sendKeys(userId.getUserId());
			WebElement passwordTextbox = GeneralUIUtils.waitForElementVisibility(By.name("password"));
			passwordTextbox.sendKeys(userId.getPassword());
			
			WebElement submitButton = GeneralUIUtils.waitForElementVisibility(By.name("btnSubmit"));
			submitButton.click();
			WebElement buttonOK = GeneralUIUtils.waitForElementVisibility(By.name("successOK"));
			AssertJUnit.assertTrue(buttonOK.isDisplayed());
			buttonOK.click();
		}
		else
		{
			System.out.println("Login with user : " + userId.getUserId());
			WebElement userNameTextbox = GeneralUIUtils.getDriver().findElement(By.cssSelector("input[type='text']"));
			userNameTextbox.sendKeys(userId.getUserId());
			WebElement passwordTextbox = GeneralUIUtils.getDriver().findElement(By.cssSelector("input[type='password']"));
			passwordTextbox.sendKeys(userId.getPassword());
			
			GeneralUIUtils.getDriver().findElement(By.id("loginBtn")).click();
		}
		
		
	}

	public static String getUrl() {
		return url;
	}

	public static void setUrl(String url) {
		SetupCDTest.url = url;
	}

	public static Config getConfig() {
		return config;
	}
	
	public void loginToSystem(UserRoleEnum role){
		WebDriver driver = GeneralUIUtils.getDriver();
		WebDriverWait wait = new WebDriverWait(driver, 30);
		
		wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//*[@method='" + "post" + "']"))));
		
		WebElement userIdTextbox = GeneralUIUtils.waitForElementVisibility(By.name("userId"));
		userIdTextbox.sendKeys(role.getUserId());
		WebElement passwordTextbox = GeneralUIUtils.waitForElementVisibility(By.name("password"));
		passwordTextbox.sendKeys("123123a");
		
		wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//*[@value='" + "Submit" + "']")))).click();
		
		GeneralUIUtils.waitForLoader();
	}

	public void loginWithUser(UserRoleEnum role) {
		setUser(role);
		try {
			navigateToUrl(url);
			extendTest.log(LogStatus.INFO, String.format("login with user %s", role.name().toUpperCase()));
			if (localEnv) {
				loginToSystem(role);
				enterToUserWorkspace();
			}
			else{
				loadCredentialsFile();
				UserCredentials credentials = getUserCredentialsFromFile(role.name().toLowerCase());
				loginToSystem(credentials, role);
				user = credentials;
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void enterToUserWorkspace() {
		WebElement enterToUserWorkspaceButton = GeneralUIUtils.waitForElementVisibility(By.className("asdc-welcome-main-back-btn"), 3 * 60);
		enterToUserWorkspaceButton.click();
		System.out.println("Entering to system...");
		GeneralUIUtils.waitForLoader();
	}

	private void setUser(UserRoleEnum role) {
		user = new User();
		user.setUserId(role.getUserId());
		user.setFirstName(role.getFirstName());
		user.setRole(role.name());
		user.setLastName(role.getLastName());
	}

	public User getUser() {
		return user;
	}

	protected void setBrowserBeforeTest(UserRoleEnum role) {
		refreshAttempts = 0;
		System.out.println(String.format("Setup before test as %s", role.toString().toUpperCase()));
		GeneralUIUtils.initDriver();
		loginWithUser(role);
	}

	public User getUser(UserRoleEnum role) {
		User user = new User();
		user = new User();
		user.setUserId(role.getUserId());
		user.setFirstName(role.getFirstName());
		user.setRole(role.name());
		return user;
	}

	protected void quitAndReLogin(UserRoleEnum role) throws Exception {
		quitBrowser();
		if (localEnv) {
			loginToSystem(role);
		}
		setBrowserBeforeTest(role);
	}

	private void quitBrowser() {
		System.out.println("Closing browser...");
		GeneralUIUtils.getDriver().quit();
	}


	protected String getRandomComponentName(String prefix) {
		return prefix + randomNumber();
	}

	protected int randomNumber() {
		Random r = new Random();
		return r.nextInt(10000);
	}

}
