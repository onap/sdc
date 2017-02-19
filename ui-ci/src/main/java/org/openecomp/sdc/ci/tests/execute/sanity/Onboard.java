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

package org.openecomp.sdc.ci.tests.execute.sanity;

import static org.testng.AssertJUnit.assertNotNull;

import java.awt.AWTException;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.GovernorOperationPage;
import org.openecomp.sdc.ci.tests.pages.OpsOperationPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.verificator.ServiceVerificator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.LogStatus;

public class Onboard extends SetupCDTest {
	

	public static Object[][] provideData(Object[] fileNamesFromFolder, String filepath) {
		Object[][] arObject = new Object[fileNamesFromFolder.length][];

		int index = 0;
		for (Object obj : fileNamesFromFolder) {
			arObject[index++] = new Object[] { filepath, obj };
		}
		return arObject;
	}

	@DataProvider(name = "VNF_List")
	private static final Object[][] VnfList() throws Exception {
		String filepath = getFilePath();
		Object[] fileNamesFromFolder = OnboardingUtils.getZipFileNamesFromFolder(filepath);
		System.out.println(String.format("There are %s zip file(s) to test", fileNamesFromFolder.length));
		return provideData(fileNamesFromFolder, filepath);
	}

	@Test(dataProvider = "VNF_List")
	public void onboardVNFTest(String filepath, String vnfFile) throws Exception, Throwable {
		SetupCDTest.setScreenshotFile(vnfFile);
		extendTest.setDescription(vnfFile);

		String vspName = onboardVNF(filepath, vnfFile);

		ResourceGeneralPage.clickSubmitForTestingButton(vspName);

		quitAndReLogin(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vspName);
		TesterOperationPage.certifyComponent(vspName);

		quitAndReLogin(UserRoleEnum.DESIGNER);
		// create service
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());

		ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CompositionPage.searchForElement(vspName);
		CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
		CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vspName);
		assertNotNull(vfElement);
		ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());

		ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());

		quitAndReLogin(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		TesterOperationPage.certifyComponent(serviceMetadata.getName());

		quitAndReLogin(UserRoleEnum.GOVERNOR);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		GovernorOperationPage.approveSerivce(serviceMetadata.getName());

//		quitAndReLogin(UserRoleEnum.OPS);
//		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
//		OpsOperationPage.distributeService();
//		OpsOperationPage.displayMonitor();
//
//		List<WebElement> rowsFromMonitorTable = OpsOperationPage.getRowsFromMonitorTable();
//		AssertJUnit.assertEquals(1, rowsFromMonitorTable.size());
//
//		OpsOperationPage.waitUntilArtifactsDistributed(0);
//
//		extendTest.log(LogStatus.PASS, String.format("onboarding %s test is passed ! ", vnfFile));
	}

	private String onboardVNF(String filepath, String vnfFile) throws Exception, Throwable {
		extendTest.log(LogStatus.INFO, String.format("going to onboard the VNF %s......", vnfFile));
		System.out.println(String.format("going to onboard the VNF %s......", vnfFile));

		OnboardingUtils.createVendorLicense(getUser());
		String vspName = OnboardingUtils.createVendorSoftwareProduct(vnfFile, filepath, getUser());
		GeneralUIUtils.getWebButton("repository-icon").click();
		extendTest.log(LogStatus.INFO, String.format("searching for onboarded %s", vnfFile));
		GeneralUIUtils.getWebElementWaitForVisible("onboarding-search").sendKeys(vspName);
		AssertJUnit.assertTrue(GeneralPageElements.checkElementsCountInTable(2));

		List<WebElement> elemenetsFromTable = GeneralPageElements.getElemenetsFromTable();
		GeneralUIUtils.waitForLoader();
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 30);
		WebElement findElement = wait.until(ExpectedConditions.visibilityOf(elemenetsFromTable.get(1)));
		findElement.click();
		GeneralUIUtils.waitForLoader();
		extendTest.log(LogStatus.INFO,
				String.format("going to import %s......", vnfFile.substring(0, vnfFile.indexOf("."))));
		GeneralUIUtils.getWebElementWaitForVisible("import-csar").click();
		GeneralUIUtils.getWebButton("create/save").click();
		GeneralUIUtils.waitForLoaderOnboarding();
		WebDriverWait wait2 = new WebDriverWait(GeneralUIUtils.getDriver(), 2 * 60);
		wait2.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//*[@data-tests-id='" + DataTestIdEnum.LifeCyleChangeButtons.CHECK_IN.getValue() + "']")));
		extendTest.log(LogStatus.PASS,
				String.format("succeeded to import %s......", vnfFile.substring(0, vnfFile.indexOf("."))));
		return vspName;
	}
	
	public static String getFilePath() {
		String filepath = System.getProperty("filepath");
		if (filepath == null && System.getProperty("os.name").contains("Windows")) {
			filepath = FileHandling.getResourcesFilesPath();
		}
		
		else if(filepath.isEmpty() && !System.getProperty("os.name").contains("Windows")){
				filepath = FileHandling.getBasePath() + File.separator + "Files";
		}
		return filepath;
	}
	
	@Test
	public void twoOnboardedVNFsInService() throws Exception, Throwable{
		
		String filepath = getFilePath();
		
		final String dnsScaling = "DNSscaling12.8.16.zip";
		final String  vLB = "vLB12.8.16.zip";
		
		String[] onboardList = {dnsScaling, vLB};
		
		Map<String, String> vspMap = new HashMap<String,String>();
		
		for (String vnf : onboardList){
			GeneralUIUtils.waitForElementsListInvisibility(By.className("ui-notification"));
			String vspName = onboardVNF(filepath, vnf);
			vspMap.put(vnf, vspName);
			ResourceGeneralPage.clickSubmitForTestingButton(vspName);
		}
		
		quitAndReLogin(UserRoleEnum.TESTER);
		for (String vspName : vspMap.values()){
			GeneralUIUtils.waitForElementsListInvisibility(By.className("ui-notification"));
			GeneralUIUtils.findComponentAndClick(vspName);
			TesterOperationPage.certifyComponent(vspName);
		}
		
		quitAndReLogin(UserRoleEnum.DESIGNER);
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
		
		Map<String, CanvasElement> canvasElements = new HashMap<String,CanvasElement>();
		for (String vspName : vspMap.values()){
			CompositionPage.searchForElement(vspName);
			CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vspName);
			assertNotNull(vfElement);
			canvasElements.put(vspName, vfElement);
		}
		ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 2, getUser());
		
	}
	
	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
