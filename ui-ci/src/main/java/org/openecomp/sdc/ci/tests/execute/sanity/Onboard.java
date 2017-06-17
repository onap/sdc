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
import static org.testng.AssertJUnit.assertTrue;

import java.awt.AWTException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ArtifactsCorrelationManager;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GovernorOperationPage;
import org.openecomp.sdc.ci.tests.pages.HomePage;
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
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.clearspring.analytics.util.Pair;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.ExtentTest;

public class Onboard extends SetupCDTest {
	
	
	protected String makeDistributionValue;
	
	@Parameters({ "makeDistribution" })
	@BeforeMethod
	public void beforeTestReadParams(@Optional("true") String makeDistributionReadValue) {
		makeDistributionValue = makeDistributionReadValue;                             
	}
	
	public static Object[][] provideData(Object[] fileNamesFromFolder, String filepath) {
		Object[][] arObject = new Object[fileNamesFromFolder.length][];

		int index = 0;
		for (Object obj : fileNamesFromFolder) {
			arObject[index++] = new Object[] { filepath, obj };
		}
		return arObject;
	}

	@DataProvider(name = "VNF_List" , parallel = true)
	private static final Object[][] VnfList() throws Exception {
		String filepath = getFilePath();
		
		Object[] fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filepath);
		System.out.println(String.format("There are %s zip file(s) to test", fileNamesFromFolder.length));
		return provideData(fileNamesFromFolder, filepath);
	}

	public static String getFilePath() {
		String filepath = System.getProperty("filepath");
		if (filepath == null && System.getProperty("os.name").contains("Windows")) {
			filepath = FileHandling.getResourcesFilesPath() +"VNFs";
		}
		
		else if(filepath.isEmpty() && !System.getProperty("os.name").contains("Windows")){
				filepath = FileHandling.getBasePath() + File.separator + "Files" + File.separator +"VNFs";
		}
		return filepath;
	}
	
	@Test
	public void onboardVNFTestSanity() throws Exception, Throwable {
//		String filepath = getFilePath();
//		String vnfFile = "2016-043_vsaegw_fdnt_30_1607_e2e.zip";
		String filepath = getFilePath();
		Object[] fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filepath);
		String vnfFile = fileNamesFromFolder[0].toString();
		runOnboardToDistributionFlow(filepath, vnfFile);
	}

	public void runOnboardToDistributionFlow(String filepath, String vnfFile) throws Exception, AWTException {
		Pair<String,Map<String,String>> onboardAndValidate = OnboardingUtils.onboardAndValidate(filepath, vnfFile, getUser());
		String vspName = onboardAndValidate.left;
		
		DeploymentArtifactPage.getLeftPanel().moveToCompositionScreen();
		ExtentTestActions.addScreenshot(Status.INFO, "TopologyTemplate_" + vnfFile ,"The topology template for " + vnfFile + " is as follows : ");
		
		DeploymentArtifactPage.clickSubmitForTestingButton(vspName);

		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vspName);
		TesterOperationPage.certifyComponent(vspName);

		reloginWithNewRole(UserRoleEnum.DESIGNER);
		// create service
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());

		ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CompositionPage.searchForElement(vspName);
		CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
		CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vspName);
		ArtifactsCorrelationManager.addVNFtoServiceArtifactCorrelation(serviceMetadata.getName(), vspName);
		
		assertNotNull(vfElement);
		ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());
		ExtentTestActions.addScreenshot(Status.INFO, "ServiceComposition_" + vnfFile ,"The service topology is as follows : ");

		ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());

		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		TesterOperationPage.certifyComponent(serviceMetadata.getName());

		reloginWithNewRole(UserRoleEnum.GOVERNOR);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		GovernorOperationPage.approveSerivce(serviceMetadata.getName());

		reloginWithNewRole(UserRoleEnum.OPS);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		OpsOperationPage.distributeService();
		OpsOperationPage.displayMonitor();

		List<WebElement> rowsFromMonitorTable = OpsOperationPage.getRowsFromMonitorTable();
		AssertJUnit.assertEquals(1, rowsFromMonitorTable.size());

		OpsOperationPage.waitUntilArtifactsDistributed(0);
		
//		validateInputArtsVSouput(serviceMetadata.getName());

		getExtendTest().log(Status.INFO, String.format("The onboarding %s test is passed ! ", vnfFile));
	}

//	protected synchronized void validateInputArtsVSouput(String serviceName) {
//		
//		
//		String filepath = System.getProperty("filepath");
//		if (filepath == null && System.getProperty("os.name").contains("Windows")) {
//			filepath = FileHandling.getResourcesFilesPath() + folder + File.separator;
//		}
//		
//		Set<Entry<String, Entry<String, LinkedList<HeatMetaFirstLevelDefinition>>>> serviceArtifactCorrelationMap = ArtifactsCorrelationManager.getServiceArtifactCorrelationMap(serviceName);
//		
//	}

	@Test(dataProvider = "VNF_List")
	public void onboardVNFTest(String filepath, String vnfFile) throws Exception, Throwable {
		setLog(vnfFile);
		System.out.println("printttttttttttttt - >" + makeDistributionValue);
		runOnboardToDistributionFlow(filepath, vnfFile);
	}

	
	@Test
	public void onboardUpdateVNFTest() throws Exception, Throwable {
		String filepath = getFilePath();
		Object[] fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filepath);
		String vnfFile = fileNamesFromFolder[0].toString();
		
		Pair<String,Map<String,String>> vsp = OnboardingUtils.onboardAndValidate(filepath, vnfFile, getUser());
		String vspName = vsp.left;
		ResourceGeneralPage.clickSubmitForTestingButton(vspName);

		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vspName);
		TesterOperationPage.certifyComponent(vspName);

		reloginWithNewRole(UserRoleEnum.DESIGNER);
		// create service
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());

		ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CompositionPage.searchForElement(vspName);
		CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
		CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vspName);
		assertNotNull(vfElement);
		ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());

		HomePage.navigateToHomePage();
		
		///update flow
		String updatedVnfFile = fileNamesFromFolder[1].toString();

		getExtendTest().log(Status.INFO, String.format("Going to update the VNF with %s......", updatedVnfFile));
		// update VendorSoftwareProduct
		OnboardingUtils.updateVnfAndValidate(filepath, vsp, updatedVnfFile, getUser());
		
		ResourceGeneralPage.clickSubmitForTestingButton(vspName);

		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vspName);
		TesterOperationPage.certifyComponent(vspName);

		reloginWithNewRole(UserRoleEnum.DESIGNER);
		
		// replace exiting VFI in service with new updated
		
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
		serviceCanvasManager = CanvasManager.getCanvasManager();
		CompositionPage.changeComponentVersion(serviceCanvasManager, vfElement, "2.0");
		ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());

		ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());

		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		TesterOperationPage.certifyComponent(serviceMetadata.getName());

		reloginWithNewRole(UserRoleEnum.GOVERNOR);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		GovernorOperationPage.approveSerivce(serviceMetadata.getName());
		
		if (!makeDistributionValue.equals("true")){
			
				reloginWithNewRole(UserRoleEnum.OPS);
				GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
				OpsOperationPage.distributeService();
				OpsOperationPage.displayMonitor();
		
				List<WebElement> rowsFromMonitorTable = OpsOperationPage.getRowsFromMonitorTable();
				AssertJUnit.assertEquals(1, rowsFromMonitorTable.size());
		
				OpsOperationPage.waitUntilArtifactsDistributed(0);
		}
		
		getExtendTest().log(Status.INFO, String.format("onboarding %s test is passed ! ", vnfFile));
		
		
	}

	@Test
	public void threeVMMSCsInServiceTest() throws Exception{
		String filepath = getFilePath();
		
		
		List<String> vmmscList = new ArrayList<String>();
		vmmscList = Arrays.asList(new File(filepath).list()).stream().filter(e -> e.contains("vmmsc") && e.endsWith(".zip")).collect(Collectors.toList());
		assertTrue("Did not find vMMSCs", vmmscList.size() > 0);
		
		Map<String, String> vspNames = new HashMap<String, String>(); 
		for (String vnfFile : vmmscList){
			getExtendTest().log(Status.INFO, String.format("going to onboard the VNF %s......", vnfFile));
			System.out.println(String.format("going to onboard the VNF %s......", vnfFile));

			OnboardingUtils.createVendorLicense(getUser());
			Pair<String,Map<String,String>> createVendorSoftwareProduct = OnboardingUtils.createVendorSoftwareProduct(vnfFile, filepath, getUser());

			getExtendTest().log(Status.INFO, String.format("searching for onboarded %s", vnfFile));
			HomePage.showVspRepository();
			getExtendTest().log(Status.INFO,String.format("going to import %s......", vnfFile.substring(0, vnfFile.indexOf("."))));
			OnboardingUtils.importVSP(createVendorSoftwareProduct);
			
			ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
			DeploymentArtifactPage.verifyArtifactsExistInTable(filepath, vnfFile);
			
			String vspName = createVendorSoftwareProduct.left;
			DeploymentArtifactPage.clickSubmitForTestingButton(vspName);
			
			vspNames.put(vnfFile, vspName);
		}
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		for (String vsp : vspNames.values()){
			GeneralUIUtils.findComponentAndClick(vsp);
			TesterOperationPage.certifyComponent(vsp);
		}
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		// create service
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();

		for (String vsp : vspNames.values()){
			CompositionPage.searchForElement(vsp);
			CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vsp);
			assertNotNull(vfElement);
		}
		ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", vspNames.values().size(), getUser());
		File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Info_" + getExtendTest().getModel().getName());
		final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
		SetupCDTest.getExtendTest().log(Status.INFO, "Three kinds of vMMSC are in canvas now." + getExtendTest().addScreenCaptureFromPath(absolutePath));
		
		ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());

		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		TesterOperationPage.certifyComponent(serviceMetadata.getName());

		reloginWithNewRole(UserRoleEnum.GOVERNOR);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		GovernorOperationPage.approveSerivce(serviceMetadata.getName());

		reloginWithNewRole(UserRoleEnum.OPS);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		OpsOperationPage.distributeService();
		OpsOperationPage.displayMonitor();

		List<WebElement> rowsFromMonitorTable = OpsOperationPage.getRowsFromMonitorTable();
		AssertJUnit.assertEquals(1, rowsFromMonitorTable.size());

		OpsOperationPage.waitUntilArtifactsDistributed(0);

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
