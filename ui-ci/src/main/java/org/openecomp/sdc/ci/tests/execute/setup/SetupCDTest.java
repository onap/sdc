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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.littleshoot.proxy.impl.ClientToProxyConnection;
import org.littleshoot.proxy.impl.ProxyToServerConnection;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestManager;
import org.openecomp.sdc.ci.tests.api.SomeInterface;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.UserCredentials;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.sanity.Onboard;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentManager.suiteNameXml;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.run.StartTest;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;
import org.openecomp.sdc.ci.tests.utils.rest.AutomationUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import groovyjarjarantlr.Utils;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;

public abstract class SetupCDTest extends DriverFactory {
	
//	private static final String RE_RUN = "ReRun - ";
	private static final String RE_RUN = "<html><font color=\"red\">ReRun - </font></html>";
	private static final String WEB_SEAL_PASSWORD = "123123a";

	public SetupCDTest() {
		LoggerContext lc = (LoggerContext) LoggerFactory. getILoggerFactory();
		lc.getLogger("org.apache").setLevel(Level.INFO);	    
	}

	/**************** CONSTANTS ****************/
	private static final String CREDENTIALS_FILE = "credentials.yaml";
	private static final String REPORT_FILE_NAME = "SDC_UI_Extent_Report.html";
	protected static final String REPORT_FOLDER = "." + File.separator + "ExtentReport" + File.separator;
	protected static final String SCREENSHOT_FOLDER = REPORT_FOLDER + "screenshots" + File.separator;
	protected static final String HAR_FILES_FOLDER_NAME = "har_files";
	protected static final String HAR_FILES_FOLDER = REPORT_FOLDER + HAR_FILES_FOLDER_NAME + File.separator;
	

	private static final String SHORT_CSV_REPORT_FILE_NAME = "ShortReport.csv";
	private static final int NUM_OF_ATTEMPTS_TO_REFTRESH = 2;
	

	/**************** PRIVATES ****************/
	private static String url;
	private static boolean localEnv = true;
	private static OnboardCSVReport csvReport;
	private static Map<?, ?> credentials;
	
    protected static ITestContext myContext;
	
	
	/**************** METHODS ****************/
	public static ExtentTest getExtendTest() {
		SomeInterface testManager = new ExtentTestManager(); 
		return testManager.getTest();
	}
	public static WindowTest getWindowTest(){
		return WindowTestManager.getWindowMap();
	}
	
	public OnboardCSVReport getCsvReport() {
		return csvReport;
	}
	
	public static String getReportFolder() {
		return REPORT_FOLDER;
	}
	
	public static String getScreenshotFolder() {
		return SCREENSHOT_FOLDER;
	}
	
	public static String getHarFilesFolder() {
		return HAR_FILES_FOLDER;
	}
	
	
	protected abstract UserRoleEnum getRole();
	
	/**************** BEFORE ****************/

	@BeforeSuite(alwaysRun = true)
	public void setupBeforeSuite(ITestContext context) throws Exception {
		RestCDUtils.deleteOnDemand();
		myContext=context;
		setErrorConfigurationFile();
		setUrl();
		ExtentManager.initReporter(getReportFolder(), REPORT_FILE_NAME, context);		
		csvReport = new OnboardCSVReport(getReportFolder(), SHORT_CSV_REPORT_FILE_NAME);
	}
	
	private static void setErrorConfigurationFile() {
		if (!System.getProperty("os.name").contains("Windows")){
			String errorConfigurationFilename = getConfig().getErrorConfigurationFile();
			errorConfigurationFilename = errorConfigurationFilename.substring(errorConfigurationFilename.lastIndexOf("/") + 1, errorConfigurationFilename.length());
			getConfig().setErrorConfigurationFile(FileHandling.getBasePath() + File.separator + "conf" + File.separator + errorConfigurationFilename);
			if (new File(getConfig().getErrorConfigurationFile()).exists()){
				System.out.println("Found error-configuration.yaml in : " + getConfig().getErrorConfigurationFile());
			}
		}
	}
	
	@BeforeMethod(alwaysRun = true )
	public void setBrowserBeforeTest(java.lang.reflect.Method method, ITestContext context) throws Exception {
		
		boolean emptyDataProvider = method.getAnnotation(Test.class).dataProvider().isEmpty();
		String className = method.getDeclaringClass().getName();
		if (emptyDataProvider && !className.contains("ToscaValidationTest") ) {
			System.out.println("ExtentReport instance started from BeforeMethod...");
			String suiteName = ExtentManager.getSuiteName(context);
			if (suiteName.equals(suiteNameXml.TESTNG_FAILED_XML_NAME.getValue())) {
				ExtentTestManager.startTest(RE_RUN + method.getName());
			}else{
				ExtentTestManager.startTest(method.getName());
			}
		
			ExtentTestManager.assignCategory(this.getClass());
			setBrowserBeforeTest(getRole());
		} else {
			System.out.println("ExtentReport instance started from Test...");
		}
		
		getConfig().setWindowsDownloadDirectory(getWindowTest().getDownloadDirectory());
		
		if (getConfig().getCaptureTraffic()){
			try{
			    MobProxy.getPoxyServer().newHar(method.getName() + ".har");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}		
	}
	
	/**************** AFTER ****************/
	@AfterMethod(alwaysRun = true)
	public void quitAfterTest(ITestResult result, ITestContext context) throws Exception {
		
		try{
			ReportAfterTestManager.report(result, context);
			GeneralUIUtils.closeErrorMessage();
		}
		finally{
			try {
				if (getConfig().getCaptureTraffic()){
					addTrafficFileToReport(result);				
				}
				
				if (result.getInstanceName().equals(Onboard.class.getName())  && result.getStatus() == ITestResult.FAILURE){
					System.out.println("Onboarding test failed, closign browser....");
					getExtendTest().log(Status.INFO, "Onboarding test failed, closign browser....");
					quitDriver();
				}
				else if (!getUser().getRole().toLowerCase().equals(UserRoleEnum.ADMIN.name().toLowerCase())){
					boolean navigateToHomePageSuccess = HomePage.navigateToHomePage();
					if (!navigateToHomePageSuccess){
						System.out.println("Navigating to homepage failed, reopening driver....");
						getExtendTest().log(Status.INFO, "Navigating to homepage failed, reopening driver....");
						quitDriver();
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
						getExtendTest().log(Status.ERROR, "Exception:"+ e.toString());
			} 
			
			
			
	    	ExtentTestManager.endTest();
	    	addResultToCSV(result, context);
//	    	ExtentManager.closeReporter();
	    	FileHandling.cleanCurrentDownloadDir();
    	}

	}
	public void addResultToCSV(ITestResult result, ITestContext context) {
		String suiteName = ExtentManager.getSuiteName(context);	    	
		ExtentTest test = getExtendTest();
		com.aventstack.extentreports.model.Test model = test.getModel();
		String name = model.getName();
		String status = model.getStatus().toString();
		if (suiteName.equals(suiteNameXml.TESTNG_FAILED_XML_NAME.getValue()) && !(result.getStatus() == ITestResult.SUCCESS)) {
			getCsvReport().writeRow(result.getInstanceName(), name.replace(RE_RUN,""), status);
		}
	}
	
	public void generateReport4Jenkins(ITestContext context) {
		String suiteName = ExtentManager.getSuiteName(context);	
//		String outputDirectory = context.getOutputDirectory();
	    JSONObject obj = new JSONObject();
	    String success = Integer.toString(context.getPassedTests().size());
	    String failed = Integer.toString(context.getFailedTests().size());
	    String total = Integer.toString(context.getFailedTests().size()+context.getPassedTests().size());
        obj.put("projectName", "SDC-ONAP-UI-Automation-"+suiteName);
        obj.put("projectVersion",  AutomationUtils.getOSVersion());
        obj.put("platform", "Linux");
        obj.put("total", total);
        obj.put("success", success);
        obj.put("failed", failed);
        
        try (FileWriter file = new FileWriter(getReportFolder() + "jenkinsResults.json")) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print(obj);
		
		
	}
	
	
	@Parameters({ "eraseAfterTests" })
	@AfterSuite(alwaysRun = true)	
	public void afterSuite2(@Optional("true") String eraseAfterTestsReadValue) throws Exception  {
//		public void afterSuite() throws Exception  {
		
		csvReport.closeFile();
		generateReport4Jenkins(myContext);
		
		if (Boolean.parseBoolean(eraseAfterTestsReadValue)){
			RestCDUtils.deleteOnDemand();
		} else {
			System.out.println("Resources will not be deleted according to suite configuration ...");
		}
		
		if (getConfig().getUseBrowserMobProxy()){
			MobProxy.getPoxyServer().stop();
		}
	}
	
	protected static String setUrl() {
		url = getConfig().getUrl();
		if (url == null) {
			String message = "no URL found";
			System.out.println(message);
			Assert.fail(message);
		} else if (!url.contains("localhost") && !url.contains("127.0.0.1")) {
			localEnv = false;
		}
		return url;
	}

	public static void loadCredentialsFile() throws Exception {
		if (credentials != null){
			return;
		}
		File credentialsFileRemote = new File(FileHandling.getBasePath() + File.separator + "conf" + File.separator + CREDENTIALS_FILE);
//		File credentialsFileLocal = new File(FileHandling.getConfFilesPath() + CREDENTIALS_FILE);
		File credentialsFileLocal = new File(FileHandling.getSdcVnfsPath() + File.separator + "conf" 
				+ File.separator + CREDENTIALS_FILE);
		File[] credentialFiles = {credentialsFileRemote, credentialsFileLocal};
		for (File credentialsFile : credentialFiles){
			if (credentialsFile.exists()){
				credentials = FileHandling.parseYamlFile(credentialsFile.getAbsolutePath());
				break;
			}
		}
	}

	private UserCredentials getUserCredentialsFromFile(String userRole) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, String> credentialsMap = (Map<String, String>) credentials.get(userRole);
		String user = (String) credentialsMap.get("username");
		String password = (String) credentialsMap.get("password");
		String firstname = (String) credentialsMap.get("firstname");
		String lastname = (String) credentialsMap.get("lastname");

		return new UserCredentials(user, password, firstname, lastname, userRole);
	}


	public static void navigateToUrl(String url) throws Exception {
		try {
			System.out.println("Deleting cookies...");
			deleteCookies();

			System.out.println("Navigating to URL : " + url);
			getDriver().navigate().to(url);
			GeneralUIUtils.waitForLoader();
			
			System.out.println("Zooming out...");
			GeneralUIUtils.windowZoomOutUltimate();
			
		} 
		catch (Exception e) {
			String msg = "Browser is unreachable";
			System.out.println(msg);
			getExtendTest().log(Status.ERROR, msg);
			Assert.fail(msg);
		}
	}
	private static void deleteCookies() throws Exception {
		getDriver().manage().deleteAllCookies();
		Thread.sleep(1000);
		
		int attempts = 0;
		final int max_attempts = 3;
		
		while (!getDriver().manage().getCookies().isEmpty() && attempts < max_attempts){
			getExtendTest().log(Status.INFO, "Trying to delete cookies one more time - " + (attempts + 1) + "/" + max_attempts + "attempts");
			String deleteCookiesJS = "document.cookie.split(';').forEach(function(c) { document.cookie = c.replace(/^ +/, '').replace(/=.*/, '=;expires=' + new Date().toUTCString() + ';path=/'); });";
			((JavascriptExecutor) getDriver()).executeScript(deleteCookiesJS);
			attempts++;
			
			if (attempts == max_attempts){
				String msg = "Did not delete cookies, can't login as user " + WindowTestManager.getWindowMap().getUser().getRole();
				System.out.println(msg);
				getExtendTest().log(Status.ERROR, msg);
				Assert.fail(msg);
			}
		}
	}
	
	protected void loginToSystem(UserRoleEnum role) throws Exception {
		UserCredentials credentials;
		if (localEnv){
			loginToSimulator(role);
			credentials = new UserCredentials(role.getUserId(), WEB_SEAL_PASSWORD, role.getFirstName(), role.getLastName(), role.name());
		}
		else{
			credentials = getUserFromFileByRole(role);
			sendUserAndPasswordKeys(credentials);
			WebElement submitButton = GeneralUIUtils.getWebElementBy(By.name("btnSubmit"), 30);
			submitButton.click();
			WebElement buttonOK = GeneralUIUtils.getWebElementBy(By.name("successOK"), 30);
			Assert.assertTrue(buttonOK.isDisplayed(), "OK button is not displayed.");
			buttonOK.click();
		}
		GeneralUIUtils.ultimateWait();                   
		getWindowTest().setUser(credentials);
	}
	protected UserCredentials getUserFromFileByRole(UserRoleEnum role) throws Exception {
		loadCredentialsFile();
		return getUserCredentialsFromFile(role.name().toLowerCase());
	}
	private void goToHomePage(UserRoleEnum role) throws Exception {
		try {
			getWindowTest().setRefreshAttempts(getWindowTest().getRefreshAttempts() == 0 ? NUM_OF_ATTEMPTS_TO_REFTRESH : getWindowTest().getRefreshAttempts());
			if (!role.equals(UserRoleEnum.ADMIN)) {
				
				WebElement closeButton = GeneralUIUtils.getClickableButtonBy(By.className("sdc-welcome-close"), 10);
				if (closeButton != null){
					closeButton.click();
				}
				
				if (!GeneralUIUtils.isElementVisibleByTestId(DataTestIdEnum.MainMenuButtons.HOME_BUTTON.getValue()))
				{
					restartBrowser(role);
				}
			}
		}
		catch (Exception e) {
			restartBrowser(role);
		}
	}
	private void restartBrowser(UserRoleEnum role) throws Exception {
		getWindowTest().setRefreshAttempts(getWindowTest().getRefreshAttempts() - 1);
		if (getWindowTest().getRefreshAttempts() <= 0) {
			System.out.println("ERR : Something is wrong with browser!");
			Assert.fail("ERR : Something is wrong with browser!");
		}
		System.out.println("Trying again...");
		getExtendTest().log(Status.INFO, "Trying again...");
		getExtendTest().log(Status.INFO, String.format("%s attempt(s) left", getWindowTest().getRefreshAttempts() ));
		System.out.println(String.format("%s attempt(s) left", getWindowTest().getRefreshAttempts() ));

		reloginWithNewRole(role);
	}
	
	public void loginToSimulator(UserRoleEnum role){
		WebDriver driver = GeneralUIUtils.getDriver();
		WebDriverWait wait = new WebDriverWait(driver, 30);
		
		wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//*[@method='" + "post" + "']"))));
		
		WebElement userIdTextbox = GeneralUIUtils.getWebElementBy(By.name("userId"));
		userIdTextbox.sendKeys(role.getUserId());
		WebElement passwordTextbox = GeneralUIUtils.getWebElementBy(By.name("password"));
		passwordTextbox.sendKeys(WEB_SEAL_PASSWORD);
		
		wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//*[@value='" + "Login" + "']")))).click();
	}

	private void sendUserAndPasswordKeys(UserCredentials userId) {
		System.out.println("Login as user : " + userId.getUserId());
		WebElement userNameTextbox = GeneralUIUtils.getWebElementBy(By.name("userid"));
		userNameTextbox.sendKeys(userId.getUserId());
		WebElement passwordTextbox = GeneralUIUtils.getWebElementBy(By.name("password"));
		passwordTextbox.sendKeys(userId.getPassword());
	}

	public void loginWithUser(UserRoleEnum role) {
		try {
			getExtendTest().log(Status.INFO, String.format("Login as user %s", role.name().toUpperCase()));
			loginToSystem(role);
			goToHomePage(role);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally{
			getWindowTest().setPreviousRole(getWindowTest().getUser().getRole());
		}
	}

	private void setUser(UserRoleEnum role) {
		User user = new User();
		user.setUserId(role.getUserId());
		user.setFirstName(role.getFirstName());
		user.setRole(role.name());
		user.setLastName(role.getLastName());
		
		getWindowTest().setUser(user);
	}

	public User getUser() {
		return getWindowTest().getUser();
	}

	private void setBrowserBeforeTest(UserRoleEnum role) {
		System.out.println(String.format("Setup before test as %s.", role.toString().toUpperCase() ));
		try {
			System.out.println("Previous role is : " + getWindowTest().getPreviousRole() + " ; Current role is : " + role.name());
			if (!getWindowTest().getPreviousRole().toLowerCase().equals(role.name().toLowerCase())){
				System.out.println("Roles are different, navigate and login");
				navigateAndLogin(role);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void navigateAndLogin(UserRoleEnum role) throws Exception {
		getWindowTest().setRefreshAttempts(getWindowTest().getRefreshAttempts() != 0 ? getWindowTest().getRefreshAttempts() : 0);
		setUser(role);
		navigateToUrl(url);
		loginWithUser(role);
		GeneralUIUtils.ultimateWait();
	}

	public User getUser(UserRoleEnum role) {
		User user = new User();
		user.setUserId(role.getUserId());
		user.setFirstName(role.getFirstName());
		user.setLastName(role.getLastName());
		user.setRole(role.name());
		return user;
	}

	protected void reloginWithNewRole(UserRoleEnum role) throws Exception {
		System.out.println(String.format("Setup before relogin as %s", role.toString().toUpperCase()));
		navigateAndLogin(role);
	}

	/*public static void setLocalUrl(UserRoleEnum role) {
		switch (role) {
			case ADMIN: {
				url = "http://localhost:8181/sdc1/proxy-admin1#/dashboard";
				break;
			}
			case DESIGNER: {
				url = "http://localhost:8181/sdc1/proxy-designer1#/dashboard";
				break;
			}
			case GOVERNOR: {
				url = "http://localhost:8181/sdc1/proxy-governor1#/dashboard";
				break;
			}
			case OPS: {
				url = "http://localhost:8181/sdc1/proxy-ops1#/dashboard";
				break;
			}
			case TESTER: {
				url = "http://localhost:8181/sdc1/proxy-tester1#/dashboard";
				break;
			}
			case PRODUCT_MANAGER1: {
				url = "http://localhost:8181/sdc1/proxy-pm1#/dashboard";
				break;
			}
			case PRODUCT_MANAGER2: {
				url = "http://localhost:8181/sdc1/proxy-pm2#/dashboard";
				break;
			}
			case PRODUCT_STRATEGIST1: {
				url = "http://localhost:8181/sdc1/proxy-ps1#/dashboard";
				break;
			}
			case PRODUCT_STRATEGIST2: {
				url = "http://localhost:8181/sdc1/proxy-ps2#/dashboard";
				break;
			}
			default: {
				break;
			}
		}
	}*/
	
	public void addTrafficFileToReport(ITestResult result) {
		try {				
			// Get the HAR data
			Har har = MobProxy.getPoxyServer().getHar();
			String shortUUID = UUID.randomUUID().toString().split("-")[0];
			File harFile = new File(getHarFilesFolder() + result.getName() + shortUUID + ".har");
			new File(getHarFilesFolder()).mkdirs();

			har.writeTo(harFile);
			
			String pathToFileFromReportDirectory = HAR_FILES_FOLDER_NAME + File.separator + harFile.getName();
			ExtentTestActions.addFileToReportAsLink(harFile, pathToFileFromReportDirectory, "File with captured traffic");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/*
	 * * Start section of test in ExtentReport with DataProvider parameters,
	 * should be started from test method, see example in onboardVNFTest
	 */
	public void setLog(String fromDataProvider) {
		
		String suiteName = ExtentManager.getSuiteName(myContext);
		if (suiteName.equals(suiteNameXml.TESTNG_FAILED_XML_NAME.getValue())) {
			ExtentTestManager.startTest(RE_RUN +Thread.currentThread().getStackTrace()[2].getMethodName() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + fromDataProvider);
		}else{
			ExtentTestManager.startTest(Thread.currentThread().getStackTrace()[2].getMethodName() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + fromDataProvider);
		}
		
		
		
		getWindowTest().setAddedValueFromDataProvider(fromDataProvider);
		ExtentTestManager.assignCategory(this.getClass());
		setBrowserBeforeTest(getRole());
	}
	
	
	
	/**************** MAIN ****************/
	public static void main(String[] args) {
		System.out.println("---------------------");
		System.out.println("running test from CLI");
		System.out.println("---------------------");

		String attsdcFilePath = FileHandling.getBasePath() + File.separator + "conf" + File.separator + "attsdc.yaml";
		System.setProperty("config.resource", attsdcFilePath);
		System.out.println("attsdc.yaml file path is : " + attsdcFilePath);
		
		String filepath = FileHandling.getBasePath() + File.separator + "Files" + File.separator;
		System.setProperty("filePath", filepath);
		System.out.println("filePath is : " + System.getProperty("filePath"));
		
		Object[] testSuitsList = FileHandling.filterFileNamesFromFolder(FileHandling.getBasePath() + File.separator + "testSuites", ".xml");
		if (testSuitsList != null) {
			System.out.println(String.format("Found %s testSuite(s)", testSuitsList.length));
			args = Arrays.copyOf(testSuitsList, testSuitsList.length, String[].class);
			StartTest.main(args);
		}
	}
}
