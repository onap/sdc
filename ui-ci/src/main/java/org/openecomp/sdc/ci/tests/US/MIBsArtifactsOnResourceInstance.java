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

package org.openecomp.sdc.ci.tests.US;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.UploadArtifactPopup;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class MIBsArtifactsOnResourceInstance extends SetupCDTest {
	
	private String folder ="";
	
	@DataProvider(name="mibsArtifactCRUDUi") 
	public static Object[][] dataProviderMibsArtifactCRUDUi() {
		return new Object[][] {
			{"mibsvFW_VFC.yml", ResourceTypeEnum.VFC},
			{"mibsVL.yml", ResourceTypeEnum.VL},
			{"mibsCP.yml", ResourceTypeEnum.CP}
			};
	}
	
	// US820414
	// Artifact UI CRUD on VFC/VL/CP 
	// TODO: Change download validation from download artifact via external API to UI
	@Test(dataProvider="mibsArtifactCRUDUi")
	public void mibsArtifactCRUDUi(String fileName, ResourceTypeEnum resourceTypeEnum) throws Exception {
		setLog(fileName);
		String filePath = FileHandling.getFilePath(folder);
		
		// import Resource
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(resourceTypeEnum, getUser());
		ResourceUIUtils.importVfc(resourceMetaData, filePath, fileName, getUser());
		
		// get resourceUUID from BE
		String resourceUUID = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceMetaData.getName(), "0.1").getUUID();
		
		// 2. Upload MIBs artifacts - SNMP_TRAP & SNMP_POLL.
		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
		
		List<ArtifactInfo> deploymentArtifactList = new ArrayList<ArtifactInfo>();
		deploymentArtifactList.add(new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "SNMP_TRAP"));
		deploymentArtifactList.add(new ArtifactInfo(filePath, "sample-xml-alldata-1-1.xml", "cuku", "artifact2", "SNMP_POLL"));
		for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
			DeploymentArtifactPage.clickAddNewArtifact();
			ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact, new UploadArtifactPopup(true));
			
			assertTrue("Only created artifact need to be exist", DeploymentArtifactPage.checkElementsCountInTable(1));
			
			String artifactUUID = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.UUID.getValue() + deploymentArtifact.getArtifactLabel()).getText();
			ArtifactUIUtils.validateExistArtifactOnDeploymentInformationPage(deploymentArtifact.getArtifactLabel(), null, "1", deploymentArtifact.getArtifactType(), true, true, true, false);
			
			// Verify that uploaded correct file by download artifact via external api
			RestResponse restResponse = ArtifactRestUtils.getResourceDeploymentArtifactExternalAPI(resourceUUID, artifactUUID, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.RESOURCE.toString());
			File file = new File(deploymentArtifact.getFilepath() + deploymentArtifact.getFilename());
			String readFileToString = FileUtils.readFileToString(file);
			Assert.assertEquals(restResponse.getResponse(), readFileToString);
			
			DeploymentArtifactPage.clickEditArtifact(deploymentArtifact.getArtifactLabel());
			UploadArtifactPopup artifactPopup = new UploadArtifactPopup(true);
			artifactPopup.loadFile(filePath, "CP.yml");
			artifactPopup.clickDoneButton();
			
			assertTrue("Only updated artifact need to be exist", DeploymentArtifactPage.checkElementsCountInTable(1));
			Assert.assertNotEquals(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.UUID.getValue() + deploymentArtifact.getArtifactLabel()).getText(), artifactUUID);
			ArtifactUIUtils.validateExistArtifactOnDeploymentInformationPage(deploymentArtifact.getArtifactLabel(), null, "2", deploymentArtifact.getArtifactType(), true, true, true, false);
				
			// Verify that updated correct file by download artifact via external api
			artifactUUID = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.UUID.getValue() + deploymentArtifact.getArtifactLabel()).getText();
			restResponse = ArtifactRestUtils.getResourceDeploymentArtifactExternalAPI(resourceUUID, artifactUUID, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.RESOURCE.toString());
			file = new File(deploymentArtifact.getFilepath() + "CP.yml");
			readFileToString = FileUtils.readFileToString(file);
			Assert.assertEquals(restResponse.getResponse(), readFileToString);
			
			DeploymentArtifactPage.clickDeleteArtifact(deploymentArtifact.getArtifactLabel());
			DeploymentArtifactPage.clickOK();
			
			assertTrue("No artifact need to be exist", DeploymentArtifactPage.checkElementsCountInTable(0));
		}
		
	}

	@DataProvider(name="mibsArtifacsOnResourceInstanceShouldOnlyHaveDownloadOption") 
	public static Object[][] dataProviderMibsArtifacsOnResourceInstanceShouldOnlyHaveDownloadOption() {
		return new Object[][] {
//			{"mibs1vFW_VFC.yml", ResourceTypeEnum.VFC},
			// TODO: delete comment below when we will have support for VL on canvas
//			{"mibs1VL.yml", ResourceTypeEnum.VL},
			{"mibs1CP.yml", ResourceTypeEnum.CP}
			};
	}
	
	// US820414
	// Import VFC/VL/CP, upload MIBs artifacts then drag it on VF & verify that deployment artifact have only download option
	@Test(dataProvider="mibsArtifacsOnResourceInstanceShouldOnlyHaveDownloadOption")
	public void mibsArtifacsOnResourceInstanceShouldOnlyHaveDownloadOption(String fileName, ResourceTypeEnum resourceTypeEnum) throws Exception {
		
//		if(resourceTypeEnum.equals(ResourceTypeEnum.CP)){
//			throw new SkipException("Open bug 322930");			
//		}

		setLog(fileName);
		
		String filePath = FileHandling.getFilePath(folder);

		// import Resource
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(resourceTypeEnum, getUser());
		ResourceUIUtils.importVfc(resourceMetaData, filePath, fileName, getUser());
		
		// 2. Upload MIBs artifacts - SNMP_TRAP & SNMP_POLL.
		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
		
		List<ArtifactInfo> deploymentArtifactList = new ArrayList<ArtifactInfo>();
		deploymentArtifactList.add(new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "SNMP_TRAP"));
		deploymentArtifactList.add(new ArtifactInfo(filePath, "sample-xml-alldata-1-1.xml", "cuku", "artifact2", "SNMP_POLL"));
		for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
			DeploymentArtifactPage.clickAddNewArtifact();
			ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact, new UploadArtifactPopup(true));
		}
		assertTrue("artifact table does not contain artifacts uploaded", DeploymentArtifactPage.checkElementsCountInTable(deploymentArtifactList.size()));
		
		// 3. Check-in DataProvider resource.
		ResourceGeneralPage.clickCheckinButton(resourceMetaData.getName());
		
		// 4. Create VF.
		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createVF(vfMetaData, getUser());
		
		// 5. Click on composition.
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		
		// 6. Drag created DataProvider resource to canvas.
		CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
		CompositionPage.searchForElement(resourceMetaData.getName());
		CanvasElement resourceInstance = vfCanvasManager.createElementOnCanvas(resourceMetaData.getName());
		
		// 7. Click on DataProvider resource.
		vfCanvasManager.clickOnCanvaElement(resourceInstance);
		
		// 8. Click on deployment artifacts in right menu.
		CompositionPage.showDeploymentArtifactTab();
		
		// 9. Verify that each uploaded MIBs artifacts shows in deployment artifacts.
		// 10. Verify that only have download option.
		for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
			// Hover over webelement -> check that only dowload button displayed
			GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_DISPLAY_NAME.getValue() + deploymentArtifact.getArtifactLabel());
			Assert.assertEquals(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.DOWNLOAD.getValue() + deploymentArtifact.getArtifactLabel()).isDisplayed(), true);
			Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.DELETE.getValue() + deploymentArtifact.getArtifactLabel()), false);
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_DISPLAY_NAME.getValue() + deploymentArtifact.getArtifactLabel());
			Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.ArtifactPopup.MODAL_WINDOW.getValue()), false);
		}


	}

	// US820414
	// Create VF, upload MIBs artifacts then drag it on service & verify that deployment artifact have only download option
	@Test
	public void mibsArtifacsOnVFInstanceShouldOnlyHaveDownloadOption() throws Exception {
		String filePath = FileHandling.getFilePath(folder);

		// 1. Create VF.
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createVF(resourceMetaData, getUser());
			
		// 2. Upload MIBs artifacts - SNMP_TRAP & SNMP_POLL.
		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
		
		List<ArtifactInfo> deploymentArtifactList = new ArrayList<ArtifactInfo>();
		deploymentArtifactList.add(new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "SNMP_TRAP"));
		deploymentArtifactList.add(new ArtifactInfo(filePath, "sample-xml-alldata-1-1.xml", "cuku", "artifact2", "SNMP_POLL"));
		for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
			DeploymentArtifactPage.clickAddNewArtifact();
			ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact, new UploadArtifactPopup(true));
		}
		assertTrue("artifact table does not contain artifacts uploaded", DeploymentArtifactPage.checkElementsCountInTable(deploymentArtifactList.size()));
		
		// 3. Check-in VF.
		ResourceGeneralPage.clickCheckinButton(resourceMetaData.getName());
		
		// 4. Create service.
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();		
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		// 5. Click on composition.
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
			
		// 6. Drag created DataProvider s to canvas.
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CompositionPage.searchForElement(resourceMetaData.getName());
		CanvasElement resourceInstance = canvasManager.createElementOnCanvas(resourceMetaData.getName());
		
		// 7. Click on DataProvider resource.
		canvasManager.clickOnCanvaElement(resourceInstance);
		
		// 8. Click on deployment artifacts in right menu.
		CompositionPage.showDeploymentArtifactTab();
		
		// 9. Verify that each uploaded MIBs artifacts shows in deployment artifacts.
		// 10. Verify that only have download option.
		for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
			// Hover over webelement -> check that only dowload button displayed
			GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_DISPLAY_NAME.getValue() + deploymentArtifact.getArtifactLabel());
			Assert.assertEquals(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.DOWNLOAD.getValue() + deploymentArtifact.getArtifactLabel()).isDisplayed(), true);
			Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.DELETE.getValue() + deploymentArtifact.getArtifactLabel()), false);
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_DISPLAY_NAME.getValue() + deploymentArtifact.getArtifactLabel());
			Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.ArtifactPopup.MODAL_WINDOW.getValue()), false);
		}
	}

	

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
