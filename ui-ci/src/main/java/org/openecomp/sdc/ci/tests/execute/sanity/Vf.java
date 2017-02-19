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

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.InformationalArtifacts;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.ci.tests.datatypes.PropertyInfo;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GovernorOperationPage;
import org.openecomp.sdc.ci.tests.pages.InformationalArtifactPage;
import org.openecomp.sdc.ci.tests.pages.OpsOperationPage;
import org.openecomp.sdc.ci.tests.pages.PropertiesPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.PropertiesUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.verificator.ServiceVerificator;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Vf extends SetupCDTest {

	private String filePath;

	@BeforeMethod
	public void beforeTest() {
		filePath = System.getProperty("filepath");
		if (filePath == null) {
			filePath = FileHandling.getResourcesFilesPath();
		}
	}

	@Test
	public void createVF() throws Exception {

		// create Resource
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createResource(resourceMetaData, getUser());

	}

	@Test
	public void updateVF() throws Exception {

		// create Resource
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createResource(resourceMetaData, getUser());

		// update Resource
		String expectedDesc = "kuku";
		resourceMetaData.setDescription(expectedDesc);
		ResourceGeneralPage.defineDescription(expectedDesc);
		GeneralUIUtils.clickUpdateButton();

		VfVerificator.verifyVFUpdatedInUI(resourceMetaData);
	}

	@Test
	public void vfcLinkedToComputeInVfWithArtifactsFlow() throws Exception {
		// String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "vFW_VFC.yml";

		// import Resource
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(
				ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
		ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
		ArtifactUIUtils.fillPlaceHolderInformationalArtifact(InformationalArtifacts.CAPACITY, filePath,
				"asc_heat 0 2.yaml", "capacity");
		ArtifactUIUtils.fillPlaceHolderInformationalArtifact(InformationalArtifacts.FEATURES, filePath,
				"asc_heat 0 2.yaml", "features");
		ResourceGeneralPage.clickSubmitForTestingButton(atomicResourceMetaData.getName());

		quitAndReLogin(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
		TesterOperationPage.certifyComponent(atomicResourceMetaData.getName());

		quitAndReLogin(UserRoleEnum.DESIGNER);

		// create Resource
		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createResource(vfMetaData, getUser());

		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

		ArtifactInfo artifact1 = new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "OTHER");
		DeploymentArtifactPage.clickAddNewArtifact();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact1);
		ArtifactInfo artifact2 = new ArtifactInfo(filePath, "sample-xml-alldata-1-1.xml", "kuku", "artifact2",
				"YANG_XML");
		DeploymentArtifactPage.clickAddNewArtifact();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact2);

		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement computeElement = canvasManager.createElementOnCanvas(LeftPanelCanvasItems.COMPUTE);

		CompositionPage.searchForElement(atomicResourceMetaData.getName());
		CanvasElement cpElement = canvasManager.createElementOnCanvas(atomicResourceMetaData.getName());
		AssertJUnit.assertNotNull(cpElement);
		ServiceVerificator.verifyNumOfComponentInstances(vfMetaData, "0.1", 2, getUser());
		canvasManager.linkElements(cpElement, computeElement);

		vfMetaData.setVersion("0.1");
		VfVerificator.verifyLinkCreated(vfMetaData, getUser(), 1);

	}

	@Test
	public void addingDeploymentArtifactToVFInstanceInService() throws Exception {
		// String filePath = FileHandling.getResourcesFilesPath();
		// create Resource
		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createResource(vfMetaData, getUser());

		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

		List<ArtifactInfo> deploymentArtifactList = new ArrayList<ArtifactInfo>();
		deploymentArtifactList.add(new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "OTHER"));
		deploymentArtifactList
				.add(new ArtifactInfo(filePath, "sample-xml-alldata-1-1.xml", "cuku", "artifact2", "YANG_XML"));
		for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
			DeploymentArtifactPage.clickAddNewArtifact();
			ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact);
		}

		ResourceGeneralPage.clickSubmitForTestingButton(vfMetaData.getName());
		GeneralUIUtils.clickASDCLogo();

		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());

		ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CompositionPage.searchForElement(vfMetaData.getName());
		CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
		CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vfMetaData.getName());

		serviceCanvasManager.clickOnCanvaElement(vfElement);
		GeneralUIUtils.waitFordataTestIdVisibility("deployment-artifact-tab").click();
		GeneralUIUtils.waitFordataTestIdVisibility("add_Artifact_Button").click();
		GeneralUIUtils.waitForLoader();
		ArtifactInfo artifact3 = new ArtifactInfo(filePath, "Heat-File.yaml", "kuku", "artifact3",
				"DCAE_INVENTORY_TOSCA");
		deploymentArtifactList.add(artifact3);
		GeneralUIUtils.getWebElementWaitForVisible("artifact-label").sendKeys(artifact3.getArtifactLabel());
		DeploymentArtifactPage.artifactPopup().selectArtifactType(artifact3.getArtifactType());
		DeploymentArtifactPage.artifactPopup().insertDescription(artifact3.getDescription());
		DeploymentArtifactPage.artifactPopup().loadFile(artifact3.getFilepath(), artifact3.getFilename());
		GeneralUIUtils.getWebElementWaitForVisible("Done").click();
		GeneralUIUtils.waitForLoader();

		List<WebElement> actualArtifactList = GeneralUIUtils
				.waitForElementsListVisibility(By.className("i-sdc-designer-sidebar-section-content-item-artifact"));
		AssertJUnit.assertEquals(deploymentArtifactList.size(), actualArtifactList.size());

	}

	@Test
	public void distibuteVFCInVFInServiceTest() throws Exception {
		// String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "vFW_VFC.yml";

		// import Resource
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(
				ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
		ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
		ArtifactUIUtils.fillPlaceHolderInformationalArtifact(InformationalArtifacts.CAPACITY, filePath,
				"asc_heat 0 2.yaml", "capacity");
		ArtifactUIUtils.fillPlaceHolderInformationalArtifact(InformationalArtifacts.FEATURES, filePath,
				"asc_heat 0 2.yaml", "features");
		ResourceGeneralPage.clickSubmitForTestingButton(atomicResourceMetaData.getName());

		quitAndReLogin(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
		TesterOperationPage.certifyComponent(atomicResourceMetaData.getName());

		quitAndReLogin(UserRoleEnum.DESIGNER);

		// create Resource
		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createResource(vfMetaData, getUser());

		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

		List<ArtifactInfo> deploymentArtifactList = new ArrayList<ArtifactInfo>();
		deploymentArtifactList.add(new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "OTHER"));
		deploymentArtifactList
				.add(new ArtifactInfo(filePath, "sample-xml-alldata-1-1.xml", "kuku", "artifact2", "YANG_XML"));
		for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
			DeploymentArtifactPage.clickAddNewArtifact();
			ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact);
		}

		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
		CanvasElement computeElement = vfCanvasManager.createElementOnCanvas(LeftPanelCanvasItems.COMPUTE);
		CompositionPage.searchForElement(atomicResourceMetaData.getName());
		CanvasElement cpElement = vfCanvasManager.createElementOnCanvas(atomicResourceMetaData.getName());

		vfCanvasManager.linkElements(cpElement, computeElement);

		ResourceGeneralPage.clickSubmitForTestingButton(vfMetaData.getName());

		quitAndReLogin(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		TesterOperationPage.certifyComponent(vfMetaData.getName());

		quitAndReLogin(UserRoleEnum.DESIGNER);

		// create service
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());

		ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CompositionPage.searchForElement(vfMetaData.getName());
		CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
		CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vfMetaData.getName());

		ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());

		quitAndReLogin(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		TesterOperationPage.certifyComponent(serviceMetadata.getName());

		quitAndReLogin(UserRoleEnum.GOVERNOR);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		GovernorOperationPage.approveSerivce(serviceMetadata.getName());

		quitAndReLogin(UserRoleEnum.OPS);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		OpsOperationPage.distributeService();
		OpsOperationPage.displayMonitor();

		List<WebElement> rowsFromMonitorTable = OpsOperationPage.getRowsFromMonitorTable();
		AssertJUnit.assertEquals(1, rowsFromMonitorTable.size());

		String deploymentArtifactsSize = String.valueOf(deploymentArtifactList.size() + 1);

		OpsOperationPage.waitUntilArtifactsDistributed(deploymentArtifactsSize, 0);

	}

	@Test
	public void changesInVFCInstanceInVF() throws Exception {
		// String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "vFW_VFC.yml";

		// import Resource
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(
				ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
		ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
		ArtifactUIUtils.fillPlaceHolderInformationalArtifact(InformationalArtifacts.CAPACITY, filePath,
				"asc_heat 0 2.yaml", "capacity");

		InformationalArtifactPage.getLeftMenu().moveToPropertiesScreen();
		int propertiesCount = PropertiesPage.getElemenetsFromTable().size();

		PropertyInfo prop1 = new PropertyInfo("p1", "v1", "prop1", PropertyTypeEnum.STRING);

		PropertiesPage.clickAddPropertyArtifact();
		PropertiesUIUtils.addNewProperty(prop1);
		AssertJUnit.assertTrue(PropertiesPage.checkElementsCountInTable(propertiesCount + 1,
				() -> PropertiesPage.getElemenetsFromTable()));

		ResourceGeneralPage.clickSubmitForTestingButton(atomicResourceMetaData.getName());

		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createResource(vfMetaData, getUser());

		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
		CompositionPage.searchForElement(atomicResourceMetaData.getName());
		CanvasElement vfcElement = vfCanvasManager.createElementOnCanvas(atomicResourceMetaData.getName());

		vfCanvasManager.clickOnCanvaElement(vfcElement);
		CompositionPage.showPropertiesAndAttributesTab();
		List<WebElement> properties = CompositionPage.getProperties();
		for (int i = 0; i < 2; i++) {
			// WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver()
			// , 30);
			// WebElement findElement =
			// wait.until(ExpectedConditions.visibilityOf(properties.get(i).findElement(By.className("i-sdc-designer-sidebar-section-content-item-property-and-attribute-label"))));
			WebElement findElement = properties.get(i).findElement(
					By.className("i-sdc-designer-sidebar-section-content-item-property-and-attribute-label"));
			findElement.click();
			PropertiesPage.getPropertyPopup().insertPropertyDefaultValue("abc123");
			PropertiesPage.getPropertyPopup().clickSave();
			GeneralUIUtils.waitForInvisibileElement(DataTestIdEnum.PropertiesPageEnum.SAVE.getValue());
		}
		vfCanvasManager.moveToFreeLocation(vfMetaData.getName());
		GeneralUIUtils.waitFordataTestIdVisibility("deployment-artifact-tab").click();
		GeneralUIUtils.waitFordataTestIdVisibility("add_Artifact_Button").click();
		GeneralUIUtils.waitForLoader();
		ArtifactInfo artifact3 = new ArtifactInfo(filePath, "Heat-File.yaml", "kuku", "artifact3", "OTHER");
		GeneralUIUtils.getWebElementWaitForVisible("artifact-label").sendKeys(artifact3.getArtifactLabel());
		DeploymentArtifactPage.artifactPopup().selectArtifactType(artifact3.getArtifactType());
		DeploymentArtifactPage.artifactPopup().insertDescription(artifact3.getDescription());
		DeploymentArtifactPage.artifactPopup().loadFile(artifact3.getFilepath(), artifact3.getFilename());
		GeneralUIUtils.getWebElementWaitForVisible("Done").click();
		GeneralUIUtils.waitForLoader();

		quitAndReLogin(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
		TesterOperationPage.certifyComponent(atomicResourceMetaData.getName());

		quitAndReLogin(UserRoleEnum.DESIGNER);
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		vfCanvasManager = CanvasManager.getCanvasManager();
		vfCanvasManager.clickOnCanvaElement(vfcElement);
		// change version
		GeneralUIUtils.getWebElementByName("changeVersion");
		Select selectlist = new Select(GeneralUIUtils.getWebElementByName("changeVersion"));
		selectlist.selectByVisibleText("1.0");
		GeneralUIUtils.waitForLoader();

		// GeneralUIUtils.waitUntilClickableButton(DataTestIdEnum.LifeCyleChangeButtons.SUBMIT_FOR_TESTING.getValue()).click();
		// ResourceGeneralPage.clickSubmitForTestingButton(vfMetaData.getName());
		//
		// vfMetaData.setVersion("0.1");
		// VfVerificator.verifyVFLifecycle(vfMetaData, getUser(),
		// LifecycleStateEnum.READY_FOR_CERTIFICATION);
	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
