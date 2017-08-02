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
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.verificator.CustomizationUUIDVerificator;
import org.openecomp.sdc.ci.tests.verificator.ServiceVerificator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

public class CustomizationUUID extends SetupCDTest {
	
	private static final String DESCRIPTION = "kuku";
	private static final String ARTIFACT_LABEL = "artifact3";
	private static final String ARTIFACT_LABEL_UPDATE = "artifactUpdate";
	private static final String GET_ARTIFACT_LIST_BY_CLASS_NAME = "i-sdc-designer-sidebar-section-content-item-artifact";
	private static final String HEAT_FILE_YAML_NAME = "Heat-File.yaml";
	private static final String HEAT_FILE_YAML_UPDATE_NAME = "Heat-File-Update.yaml";
	private String filePath;
	
	@BeforeMethod
	public void beforeTest(){
		filePath = FileHandling.getFilePath("");
	}
	
	@Test
	public void uniqueCustomizationUUIDforeachVFi() throws Exception {
		
		
		ResourceReqDetails vfMetaData = createNewResourceWithArtifactSubmitForTesting();
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		TesterOperationPage.certifyComponent(vfMetaData.getName());

		reloginWithNewRole(UserRoleEnum.DESIGNER);
		
		List customizationUUIDs = new ArrayList<String>();
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
					
		
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement VFiElement1 = addElemntToCanvas(vfMetaData, canvasManager);
		CanvasElement VFiElement2 = addElemntToCanvas(vfMetaData, canvasManager);
		CanvasElement VFiElement3 = addElemntToCanvas(vfMetaData, canvasManager);
		
		
		ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());
	
		canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement2);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement3);
		
		ServiceGeneralPage.clickCheckoutButton();
		canvasManager = CanvasManager.getCanvasManager();
		CanvasElement VFiElement4 = addElemntToCanvas(vfMetaData, canvasManager);
		CanvasElement VFiElement5 = addElemntToCanvas(vfMetaData, canvasManager);
		CanvasElement VFiElement6 = addElemntToCanvas(vfMetaData, canvasManager);
		
		ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement4);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement5);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement6);
		
		CustomizationUUIDVerificator.validateCustomizationUUIDuniqueness(customizationUUIDs);
			

		}


	@Test
	public void uniqueCustomizationUUIDafterArtifactCRUDofVFi() throws Exception {
		
		
		ResourceReqDetails vfMetaData = createNewResourceWithArtifactSubmitForTesting();
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		TesterOperationPage.certifyComponent(vfMetaData.getName());

		reloginWithNewRole(UserRoleEnum.DESIGNER);
		
		List customizationUUIDs = new ArrayList<>();
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
					
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement VFiElement1 = addElemntToCanvas(vfMetaData, canvasManager);
			
		ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		
		canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);
		
		//add artifact to VFI
		
		ServiceGeneralPage.clickCheckoutButton();
		canvasManager = CanvasManager.getCanvasManager();
	
		ArtifactInfo artifact = new ArtifactInfo(filePath, HEAT_FILE_YAML_NAME, DESCRIPTION, ARTIFACT_LABEL,ArtifactTypeEnum.SNMP_POLL.getType());
		
		canvasManager.clickOnCanvaElement(VFiElement1);
		CompositionPage.showDeploymentArtifactTab();
		CompositionPage.clickAddArtifactButton();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact, CompositionPage.artifactPopup());
		
		
		ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);
		
		
		//delete VFI artifacts
		
		ServiceGeneralPage.clickCheckoutButton();
		canvasManager = CanvasManager.getCanvasManager();
		canvasManager.clickOnCanvaElement(VFiElement1);
		CompositionPage.showDeploymentArtifactTab();
		List<WebElement> actualArtifactList = GeneralUIUtils.getWebElementsListBy(By.className(GET_ARTIFACT_LIST_BY_CLASS_NAME));
		GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_ITEM.getValue() + ARTIFACT_LABEL);
		SetupCDTest.getExtendTest().log(Status.INFO, "Going to delete "  +  HEAT_FILE_YAML_NAME + " artifact" + " and check if deleted");
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.DELETE.getValue() + ARTIFACT_LABEL);
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
		
		
		ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);
		
				
		CustomizationUUIDVerificator.validateCustomizationUUIDuniqueness(customizationUUIDs);
		
		}

	
	@Test
	public void uniqueCustomizationUUIDchangeVFiVersion() throws Exception {
		
		
		ResourceReqDetails vfMetaData = createNewResourceWithArtifactSubmitForTesting();
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		TesterOperationPage.certifyComponent(vfMetaData.getName());

		reloginWithNewRole(UserRoleEnum.DESIGNER);
		
		List customizationUUIDs = new ArrayList<>();
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
					
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement VFiElement1 = addElemntToCanvas(vfMetaData, canvasManager);
			
		ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		
		canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);
		
		//change VF version
		HomePage.navigateToHomePage();
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		ResourceGeneralPage.clickCheckoutButton();
		ResourceGeneralPage.clickSubmitForTestingButton(vfMetaData.getName());
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		TesterOperationPage.certifyComponent(vfMetaData.getName());
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		
		//update VFI version
		
		canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
		ServiceGeneralPage.clickCheckoutButton();
		canvasManager = CanvasManager.getCanvasManager();
		canvasManager.clickOnCanvaElement(VFiElement1);
		CompositionPage.changeComponentVersion(canvasManager, VFiElement1, "2.0");
		
		ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);
		
		CustomizationUUIDVerificator.validateCustomizationUUIDuniqueness(customizationUUIDs);
		
		}
	
	
	@Test
	public void uniqueCustomizationUUIDaddRelation() throws Exception {
		
		
		ResourceReqDetails vfMetaData = createNewResourceWithArtifactSubmitForTesting();
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		TesterOperationPage.certifyComponent(vfMetaData.getName());

		reloginWithNewRole(UserRoleEnum.DESIGNER);
		
		List customizationUUIDs = new ArrayList<>();
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
					
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement VFiElement1 = addElemntToCanvas(vfMetaData, canvasManager);
			
		ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		
		canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);
		
		//change VF version
		HomePage.navigateToHomePage();
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		ResourceGeneralPage.clickCheckoutButton();
		ResourceGeneralPage.clickSubmitForTestingButton(vfMetaData.getName());
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		TesterOperationPage.certifyComponent(vfMetaData.getName());
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		
		//update VFI version
		
		canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
		ServiceGeneralPage.clickCheckoutButton();
		canvasManager = CanvasManager.getCanvasManager();
		CompositionPage.searchForElement(NormativeTypesEnum.PORT.getFolderName());
		CanvasElement portElement = canvasManager.createElementOnCanvas(LeftPanelCanvasItems.PORT);
		canvasManager.linkElements(portElement, VFiElement1);
		
		canvasManager.clickOnCanvaElement(VFiElement1);
		
		
		ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
		addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);
		
		CustomizationUUIDVerificator.validateCustomizationUUIDuniqueness(customizationUUIDs);
		
		}
	
	
	
	public CanvasManager findServiceAndNavigateToCanvas(ServiceReqDetails serviceMetadata) throws Exception {
		CanvasManager canvasManager;
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
		canvasManager = CanvasManager.getCanvasManager();
		return canvasManager;
	}

	public ResourceReqDetails createNewResourceWithArtifactSubmitForTesting() throws Exception {
		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createResource(vfMetaData, getUser());

		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

		List<ArtifactInfo> deploymentArtifactList = new ArrayList<ArtifactInfo>();
		deploymentArtifactList.add(new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "OTHER"));
		deploymentArtifactList.add(new ArtifactInfo(filePath, "sample-xml-alldata-1-1.xml", "cuku", "artifact2", "YANG_XML"));
		for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
			DeploymentArtifactPage.clickAddNewArtifact();
			ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact);
		}
		AssertJUnit.assertTrue("artifact table does not contain artifacts uploaded", DeploymentArtifactPage.checkElementsCountInTable(deploymentArtifactList.size()));
		
		String newDescription = "new description";
		DeploymentArtifactPage.clickEditArtifact(deploymentArtifactList.get(0).getArtifactLabel());
		DeploymentArtifactPage.artifactPopup().insertDescription(newDescription);
		DeploymentArtifactPage.artifactPopup().clickDoneButton();
		String actualArtifactDescription = DeploymentArtifactPage.getArtifactDescription(deploymentArtifactList.get(0).getArtifactLabel());
		AssertJUnit.assertTrue("artifact description is not updated", newDescription.equals(actualArtifactDescription));
		
		DeploymentArtifactPage.clickDeleteArtifact(deploymentArtifactList.get(0).getArtifactLabel());
		DeploymentArtifactPage.clickOK();
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		
//		ResourceReqDetails vfcCompute = ElementFactory.getDefaultResource(NormativeTypesEnum.COMPUTE);
		CompositionPage.searchForElement(NormativeTypesEnum.COMPUTE.name());
		CanvasManager canvasManagerVF = CanvasManager.getCanvasManager();
		CanvasElement VFiElement1 = canvasManagerVF.createElementOnCanvas(LeftPanelCanvasItems.COMPUTE);
		
		
		ResourceGeneralPage.clickSubmitForTestingButton(vfMetaData.getName());
		return vfMetaData;
	}

	public void addCanvasElementToList(List customizationUUIDs, CanvasManager canvasManager, CanvasElement VFiElement1)
			throws Exception {
		canvasManager.clickOnCanvaElement(VFiElement1);
		WebElement VFi1customizationUUID = CompositionPage.getCustomizationUUID();
		customizationUUIDs.add(VFi1customizationUUID.getText());
	}

	public CanvasElement addElemntToCanvas(ResourceReqDetails vfMetaData, CanvasManager canvasManager)
			throws Exception {
		CompositionPage.searchForElement(vfMetaData.getName());
		CanvasElement VFiElement1 = canvasManager.createElementOnCanvas(vfMetaData.getName());
		return VFiElement1;
	}
	
		public static void changeDeleteAndValidateVersionOnGeneralPage(String previousVersion, String currentVersion, String serviceName) throws Exception{
		GeneralPageElements.selectVersion("V" + previousVersion);
		ServiceVerificator.verifyVersionUI(previousVersion);
		GeneralUIUtils.clickJSOnElementByText("latest version");
		ServiceVerificator.verifyVersionUI(currentVersion);
		GeneralPageElements.clickTrashButtonAndConfirm();
		GeneralUIUtils.findComponentAndClick(serviceName);
		ServiceVerificator.verifyVersionUI(previousVersion);		
	}	

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
