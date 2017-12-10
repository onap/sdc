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

import static org.testng.AssertJUnit.assertTrue;

import java.awt.AWTException;
import java.util.Arrays;
import java.util.List;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.CompositionScreenEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.ServiceMetadataEnum;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.TopMenuButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.InputsPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.CatalogUIUtilitis;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.verificator.DeploymentViewVerificator;
import org.openecomp.sdc.ci.tests.verificator.ServiceVerificator;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.TestException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

public class Service extends SetupCDTest {
	
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
	public void createService() throws Exception {		
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());	
	}
	
	@Test
	public void validDefaultContactAndTagAfterCreateService() throws Exception{
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createServiceWithDefaultTagAndUserId(serviceMetadata, getUser());
		
		assertTrue("wrong userId", getUser().getUserId().equals(ResourceGeneralPage.getContactIdText()));
		
		List<String> actualTags = Arrays.asList(ServiceGeneralPage.getTags());
		assertTrue("wrong tags", (actualTags.size() == 1) && actualTags.get(0).equals(serviceMetadata.getName()));
	}
	
	@Test
	public void updateService() throws Exception {
		// Create Service
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
        // Update Service
		ServiceGeneralPage.deleteOldTags(serviceMetadata);
		serviceMetadata.setName(ElementFactory.getServicePrefix() + "UpdatedName" + serviceMetadata.getName());
		serviceMetadata.setDescription("updatedDescriptionSanity");
		serviceMetadata.setProjectCode("654321");
		serviceMetadata.setContactId("cs6543");
		serviceMetadata.getTags().addAll(Arrays.asList("updatedTag", "oneMoreUpdatedTag", "lastOne UpdatedTag"));
		ServiceUIUtils.setServiceCategory(serviceMetadata, ServiceCategoriesEnum.VOIP);
		ServiceUIUtils.fillServiceGeneralPage(serviceMetadata, getUser());
		GeneralPageElements.clickCreateButton();
		
		ServiceVerificator.verifyServiceUpdatedInUI(serviceMetadata);	
	}
	
	@Test
	public void deleteService() throws Exception {
		
		// create service
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		// Delete service
		//GeneralUIUtils.HighlightMyElement(GeneralUIUtils.getWebButton("delete_version"));
		GeneralPageElements.clickTrashButtonAndConfirm();
		
		// Verification
		CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
		CatalogUIUtilitis.catalogSearchBox(serviceMetadata.getName());
		ServiceVerificator.verifyServiceDeletedInUI(serviceMetadata);	
	}
	
	@Test
	public void checkoutServiceTest() throws Exception{
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		
		ArtifactInfo artifact = new ArtifactInfo(filePath, HEAT_FILE_YAML_NAME, DESCRIPTION, ARTIFACT_LABEL,"OTHER");
		CompositionPage.showDeploymentArtifactTab();
		CompositionPage.clickAddArtifactButton();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact, CompositionPage.artifactPopup());
		
		ResourceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		GeneralPageElements.clickCheckoutButton();
		
		serviceMetadata.setVersion("0.2");
		ServiceVerificator.verifyServiceLifecycle(serviceMetadata, getUser(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKOUT);
		ServiceVerificator.verifyVersionUI(serviceMetadata.getVersion());
		
		ResourceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		TesterOperationPage.certifyComponent(serviceMetadata.getName());
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		ResourceGeneralPage.clickCheckoutButton();
		
		serviceMetadata.setVersion("1.1");
		serviceMetadata.setUniqueId(null);
		ServiceVerificator.verifyServiceLifecycle(serviceMetadata, getUser(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKOUT);
		ServiceVerificator.verifyVersionUI(serviceMetadata.getVersion());
	}
	
	@Test
	public void submitServiceForTestingWithNonCertifiedAsset() throws Exception{

		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.createVF(atomicResourceMetaData, getUser());
		ResourceGeneralPage.clickSubmitForTestingButton(atomicResourceMetaData.getName());
		
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CompositionPage.searchForElement(atomicResourceMetaData.getName());
		canvasManager.createElementOnCanvas(atomicResourceMetaData.getName());
		
		try{
			CompositionPage.clickSubmitForTestingButton(serviceMetadata.getName());
			assert(false);
		}
		catch(Exception e){
			String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
			String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.VALIDATED_RESOURCE_NOT_FOUND.name());
			assertTrue(errorMessage.contains(checkUIResponseOnError));	
		}
		finally{
			ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "0.1");
		}
		
	}
	
	@Test
	public void createLinkService() throws Exception{
		String fileName2 = "vSeGW.csar";
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName2, getUser());
		ResourceGeneralPage.clickCheckinButton(resourceMetaData.getName());
		
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();		
		CompositionPage.searchForElement(resourceMetaData.getName());
		CanvasElement firstElement = canvasManager.createElementOnCanvas(resourceMetaData.getName());
		CanvasElement secondElement = canvasManager.createElementOnCanvas(resourceMetaData.getName());
		canvasManager.linkElements(firstElement, secondElement);		
	}
	
	@Test
	public void addDeploymentArtifactInCompositionScreenTest() throws Exception{
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		ArtifactInfo artifact = new ArtifactInfo(filePath, HEAT_FILE_YAML_NAME, DESCRIPTION, ARTIFACT_LABEL,"OTHER");
		CompositionPage.showDeploymentArtifactTab();
		CompositionPage.clickAddArtifactButton();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact, CompositionPage.artifactPopup());
		
		List<WebElement> actualArtifactList = GeneralUIUtils.getWebElementsListBy(By.className(GET_ARTIFACT_LIST_BY_CLASS_NAME));
		AssertJUnit.assertEquals(1, actualArtifactList.size());
		
		for(WebElement actualArtifactFileName : CompositionPage.getAllAddedArtifacts()){
			assertTrue(HEAT_FILE_YAML_NAME.equals(actualArtifactFileName.getText()));
		}				
		
	}
	
	@Test
	public void addInformationArtifactInCompositionScreenTest() throws Exception{
		String fileName        = HEAT_FILE_YAML_NAME;
		String descriptionText = DESCRIPTION;
		
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		ArtifactInfo artifactInfo = new ArtifactInfo(filePath, fileName, descriptionText, ARTIFACT_LABEL,"OTHER");
		CompositionPage.showInformationArtifactTab();
		List<WebElement> beforeArtifactList = GeneralUIUtils.getWebElementsListBy(By.className(GET_ARTIFACT_LIST_BY_CLASS_NAME));
		CompositionPage.clickAddArtifactButton();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(artifactInfo, CompositionPage.artifactPopup());
		
		List<WebElement> actualArtifactList = GeneralUIUtils.getWebElementsListBy(By.className(GET_ARTIFACT_LIST_BY_CLASS_NAME));
		assertTrue(String.format("Wrong number of artifacts, Expected: %s Actual: %s", beforeArtifactList.size() + 1, actualArtifactList.size()),
				                                                                  (beforeArtifactList.size() + 1) == actualArtifactList.size());
		for(DataTestIdEnum.InformationalArtifactsService artifact: DataTestIdEnum.InformationalArtifactsService.values()){
			ArtifactUIUtils.fillPlaceHolderInformationalArtifact(artifact, filePath, fileName, descriptionText);
		}
	    int numberOfFiles = CompositionPage.getAllAddedArtifacts().size();
	    assertTrue(String.format("Wrong number of artifacts, Expected: %s Actual: %s", (beforeArtifactList.size() + 1), numberOfFiles), (beforeArtifactList.size() + 1) == numberOfFiles);
	    
	    for(WebElement actualArtifactFileName : CompositionPage.getAllAddedArtifacts()){
			assertTrue(fileName.equals(actualArtifactFileName.getText()));
		}
	    
	}
	
	@Test
	public void addAPIArtifactInCompositionScreenTest() throws Exception{			
		String fileName        = HEAT_FILE_YAML_NAME,
			   descriptionText = DESCRIPTION,
		       url             = "http://kuku.com";
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		new ArtifactInfo(filePath, fileName, descriptionText, ARTIFACT_LABEL,"OTHER");
		CompositionPage.showAPIArtifactTab();

		for(DataTestIdEnum.APIArtifactsService artifact: DataTestIdEnum.APIArtifactsService.values()){
			ArtifactUIUtils.fillPlaceHolderAPIArtifact(artifact, filePath, fileName, descriptionText, url);
		}
	    int numberOfFiles = CompositionPage.getAllAddedArtifacts().size(),
	    numberOfPlacehoders = DataTestIdEnum.APIArtifactsService.values().length;
	    assertTrue(String.format("Wrong file count, should be %s files", numberOfPlacehoders), numberOfPlacehoders  == numberOfFiles);
	    
	    for(WebElement actualArtifactFileName : CompositionPage.getAllAddedArtifacts()){
			assertTrue(fileName.equals(actualArtifactFileName.getText()));
		}
	}
	
	@Test
	public void ManagmentWorkflowTest() throws Exception{		
		String descriptionText     = DESCRIPTION,
			   descriptionTextEdit = "kuku2";

		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		ServiceGeneralPage.getServiceLeftMenu().moveToManagmentWorkflow();
		ServiceGeneralPage.fillAndAddNewWorkflow(descriptionText, descriptionText);
		ServiceVerificator.verifyManagmentWorkflow(descriptionText, descriptionText);
		
		ServiceGeneralPage.clickAddWorkflow();
		ServiceGeneralPage.fillAndAddNewWorkflow(descriptionTextEdit, descriptionTextEdit);
	}
	
	@Test
	public void deleteChangeVersionTest() throws Exception{
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		
		ArtifactInfo artifact = new ArtifactInfo(filePath, HEAT_FILE_YAML_NAME, DESCRIPTION, ARTIFACT_LABEL,"OTHER");
		CompositionPage.showDeploymentArtifactTab();
		CompositionPage.clickAddArtifactButton();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact, CompositionPage.artifactPopup());
		
		ResourceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		GeneralPageElements.clickCheckoutButton();
		
		changeDeleteAndValidateVersionOnGeneralPage("0.1", "0.2", serviceMetadata.getName());
		
		GeneralPageElements.clickCheckoutButton();		
		ResourceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		TesterOperationPage.certifyComponent(serviceMetadata.getName());
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		ResourceGeneralPage.clickCheckoutButton();
		
		changeDeleteAndValidateVersionOnGeneralPage("1.0", "1.1", serviceMetadata.getName());
	}
	
	@Test
	public void compositionScreenRightSideButtonsTest() throws Exception{
		
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		
		CompositionPage.showInformationTab();
		ServiceVerificator.verifyOpenTabTitle(CompositionScreenEnum.INFORMATION);
		
		//feature removed from UI
//		CompositionPage.showCompositionTab();
//		ServiceVerificator.verifyOpenTabTitle(CompositionScreenEnum.COMPOSITION);
		
		CompositionPage.showDeploymentArtifactTab();
		ServiceVerificator.verifyOpenTabTitle(CompositionScreenEnum.DEPLOYMENT_ARTIFACT_TAB);
		
		CompositionPage.showInputsTab();
		assertTrue(CompositionPage.getOpenTabTitle().size() == 0);				
		
		CompositionPage.showAPIArtifactTab();
		ServiceVerificator.verifyOpenTabTitle(CompositionScreenEnum.API);
		
		CompositionPage.showInformationArtifactTab();
		ServiceVerificator.verifyOpenTabTitle(CompositionScreenEnum.INFORMATION_ARTIFACTS);
		
	}
	
	@Test
	public void addDeploymentArtifactToVFInstanceTest() throws Exception{
		
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ArtifactInfo artifact = new ArtifactInfo(filePath, HEAT_FILE_YAML_NAME, DESCRIPTION, ARTIFACT_LABEL,ArtifactTypeEnum.SNMP_POLL.getType());
		
		CanvasElement computeElement = createServiceWithRiArtifact(atomicResourceMetaData, serviceMetadata, artifact);
		checkArtifactIfAdded(1, HEAT_FILE_YAML_NAME);
		checkInService(serviceMetadata);
		clickOncanvasElement(computeElement);
		CompositionPage.showDeploymentArtifactTab();
		checkArtifactIfAdded(1, HEAT_FILE_YAML_NAME);				
	}

	@Test
	public void deleteDeploymentArtifactFromVFInstanceTest() throws Exception{
				
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ArtifactInfo artifact = new ArtifactInfo(filePath, HEAT_FILE_YAML_NAME, DESCRIPTION, ARTIFACT_LABEL,ArtifactTypeEnum.SNMP_POLL.getType());
		
		createServiceWithRiArtifact(atomicResourceMetaData, serviceMetadata, artifact);
		checkArtifactIfAdded(1, HEAT_FILE_YAML_NAME);
		List<WebElement> actualArtifactList = GeneralUIUtils.getWebElementsListBy(By.className(GET_ARTIFACT_LIST_BY_CLASS_NAME));
		deleteAndVerifyArtifact(actualArtifactList);
		
	}
	
	@Test
	public void deleteDeploymentArtifactFromVFInstanceNextVersionTest() throws Exception{
		
//		if(true){
//			throw new SkipException("Open bug 342260");			
//		}
		
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ArtifactInfo artifact = new ArtifactInfo(filePath, HEAT_FILE_YAML_NAME, DESCRIPTION, ARTIFACT_LABEL,ArtifactTypeEnum.SNMP_POLL.getType());
		
		CanvasElement computeElement = createServiceWithRiArtifact(atomicResourceMetaData, serviceMetadata, artifact);
		checkArtifactIfAdded(1, HEAT_FILE_YAML_NAME);
		checkInService(serviceMetadata);
		ResourceGeneralPage.clickCheckoutButton();
		clickOncanvasElement(computeElement);
		CompositionPage.showDeploymentArtifactTab();
		List<WebElement> actualArtifactList = GeneralUIUtils.getWebElementsListBy(By.className(GET_ARTIFACT_LIST_BY_CLASS_NAME));
		deleteAndVerifyArtifact(actualArtifactList);
//		change container version
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.GeneralElementsEnum.VERSION_HEADER.getValue());
		GeneralPageElements.selectVersion("V0.1");
		clickOncanvasElement(computeElement);
		CompositionPage.showDeploymentArtifactTab();
		checkArtifactIfAdded(1, HEAT_FILE_YAML_NAME);
		
	}

//	service version V0.1 default artifact, service version V0.2 updated artifact 	
	@Test
	public void updateDeploymentArtifactOnVFInstanceNextVersionTest() throws Exception{
		
//		if(true){
//			throw new SkipException("Open bug 322930");			
//		}
		
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ArtifactInfo artifact = new ArtifactInfo(filePath, HEAT_FILE_YAML_NAME, DESCRIPTION, ARTIFACT_LABEL,ArtifactTypeEnum.SNMP_POLL.getType());
		ArtifactInfo artifactUpdate = new ArtifactInfo(filePath, HEAT_FILE_YAML_UPDATE_NAME, DESCRIPTION, ARTIFACT_LABEL_UPDATE,ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType());
		
		CanvasElement computeElement = createServiceWithRiArtifact(atomicResourceMetaData, serviceMetadata, artifact);
		checkArtifactIfAdded(1, HEAT_FILE_YAML_NAME);
		checkInService(serviceMetadata);
		ResourceGeneralPage.clickCheckoutButton();
		clickOncanvasElement(computeElement);
		CompositionPage.showDeploymentArtifactTab();
		List<WebElement> actualArtifactList = GeneralUIUtils.getWebElementsListBy(By.className(GET_ARTIFACT_LIST_BY_CLASS_NAME));
		deleteAndVerifyArtifact(actualArtifactList);
//		upload new artifact
		addDeploymentArtifact(artifactUpdate, CanvasManager.getCanvasManager(), computeElement);
		checkArtifactIfAdded(1, HEAT_FILE_YAML_UPDATE_NAME);
//		change container version
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.GeneralElementsEnum.VERSION_HEADER.getValue());
		GeneralPageElements.selectVersion("V0.1");
		clickOncanvasElement(computeElement);
		CompositionPage.showDeploymentArtifactTab();
		checkArtifactIfAdded(1, HEAT_FILE_YAML_NAME);
		
	}
	
	public void clickOncanvasElement(CanvasElement computeElement) {
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		canvasManager.clickOnCanvaElement(computeElement);
	}

	public void checkInService(ServiceReqDetails serviceMetadata) throws Exception {
		ResourceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
	}

	public static void deleteAndVerifyArtifact(List<WebElement> actualArtifactList) {
		if (actualArtifactList.size()>0){
			GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_ITEM.getValue() + ARTIFACT_LABEL);
			SetupCDTest.getExtendTest().log(Status.INFO, "Going to delete "  +  HEAT_FILE_YAML_NAME + " artifact" + " and check if deleted");
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.DELETE.getValue() + ARTIFACT_LABEL);
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
			assertTrue("Artifact does not deleted", !GeneralUIUtils.waitForElementInVisibilityByTestId(By.className(GET_ARTIFACT_LIST_BY_CLASS_NAME)));
		}
	}


	public void checkArtifactIfAdded(Integer expectedNumOfARtifacts, String expectedArtifactName) {
		
		List<WebElement> actualArtifactList;
		actualArtifactList = GeneralUIUtils.getWebElementsListBy(By.className(GET_ARTIFACT_LIST_BY_CLASS_NAME));
		assertTrue("Expected artifact count is: " + expectedNumOfARtifacts + ", but was " + actualArtifactList.size(),expectedNumOfARtifacts==actualArtifactList.size());
		
		if(expectedNumOfARtifacts != 0){
			for(WebElement actualArtifactFileName : CompositionPage.getAllAddedArtifacts()){
				assertTrue("Artifact name does not match, expected " + expectedArtifactName + ", but was " + actualArtifactFileName.getText(), expectedArtifactName.equals(actualArtifactFileName.getText()));
			}
		}
		
	}


	public CanvasElement createServiceWithRiArtifact(ResourceReqDetails atomicResourceMetaData, ServiceReqDetails serviceMetadata, ArtifactInfo artifact) throws Exception, AWTException {
		ResourceUIUtils.createVF(atomicResourceMetaData, getUser());
		ResourceGeneralPage.clickSubmitForTestingButton(atomicResourceMetaData.getName());
		
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CompositionPage.searchForElement(atomicResourceMetaData.getName());
		CanvasElement computeElement = canvasManager.createElementOnCanvas(atomicResourceMetaData.getName());
		addDeploymentArtifact(artifact, canvasManager, computeElement);
		
		return computeElement;
	}


	public void addDeploymentArtifact(ArtifactInfo artifact, CanvasManager canvasManager, CanvasElement computeElement) throws Exception {
		canvasManager.clickOnCanvaElement(computeElement);
		CompositionPage.showDeploymentArtifactTab();
		CompositionPage.clickAddArtifactButton();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact, CompositionPage.artifactPopup());
	}
	
	@Test
	public void isDisabledAndReadOnlyInCheckin() throws Exception{
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		GeneralPageElements.clickCheckinButton(serviceMetadata.getName());
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		
		ServiceMetadataEnum[] fieldsForCheck = {ServiceMetadataEnum.SERVICE_NAME,
		                                        ServiceMetadataEnum.CONTACT_ID,
		                                        ServiceMetadataEnum.DESCRIPTION,
		                                        ServiceMetadataEnum.PROJECT_CODE,
		                                        ServiceMetadataEnum.TAGS};
		for (ServiceMetadataEnum field: fieldsForCheck){
			assertTrue(GeneralUIUtils.isElementReadOnly(field.getValue()));
		}
		
		assertTrue(GeneralUIUtils.isElementDisabled(ServiceMetadataEnum.CATEGORY.getValue()));
		assertTrue(GeneralUIUtils.isElementDisabled(DataTestIdEnum.LifeCyleChangeButtons.CREATE.getValue()));
	}
	
	// future removed from ui
    @Test(enabled = false)
	public void inputsTest() throws Exception{
		String fileName = "service_input_test_VF2.csar";
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());		
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName, getUser());
		GeneralPageElements.clickCheckinButton(resourceMetaData.getName());
		
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		String selectedInstanceName = addResourceToServiceInCanvas(resourceMetaData);
		
		GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");
		DeploymentArtifactPage.getLeftMenu().moveToInputsScreen();
		
		InputsPage.addInputToService(selectedInstanceName, "volume_id");
		InputsPage.deleteServiceInput(selectedInstanceName, "volume_id");
		
		// Trying to find deleted service input
		try{
			InputsPage.getServiceInput(selectedInstanceName, "volume_id");
			assert(false);
		}
		catch(TestException e){	
		}
	}
	
	@Test
	public void deploymentViewServiceTest() throws Exception{				
		String fileName2 = "vSeGWNew.csar";
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName2, getUser());
		ResourceGeneralPage.clickCheckinButton(resourceMetaData.getName());
		
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		addResourceToServiceInCanvas(resourceMetaData);
	
		GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");
		DeploymentArtifactPage.getLeftMenu().moveToDeploymentViewScreen();
		
		serviceMetadata.setVersion("0.1");
		List<WebElement> instanceRowsFromTable = GeneralUIUtils.getElementsByCSS("div[data-tests-id^='hierarchy-instance'] span[class^='expand-collapse-title-text']");
		for(WebElement instanceRow: instanceRowsFromTable){
			String instanceRowText = instanceRow.getText();
			List<WebElement> instanceModulesList = DeploymentPage.getInstanceModulesList(instanceRowText);
			for (WebElement instanceModule: instanceModulesList){
				String instanceModuleText = instanceModule.getText();
				ResourceUIUtils.clickOnElementByText(instanceModuleText, "instance");
								
				ServiceVerificator.verifyDeploymentPageSubElements(instanceModuleText.split("\\.\\.")[2], new DeploymentViewVerificator(filePath + fileName2));
				
				ServiceVerificator.verifyDisabledServiceProperties();
				String isBaseValue = ServiceVerificator.getVFModulePropertieValue(serviceMetadata, "isBase", instanceModuleText);
				if (isBaseValue.equals("false"))
					ServiceVerificator.verifyEnabledServiceProperties();
				
				ResourceUIUtils.clickOnElementByText(instanceModuleText, "instance");
			}												
		}			
	}
	
	@Test
	public void vfModuleCustomizationUUIDServiceTest() throws Exception{
		String fileName2 = "vSeGW.csar";
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName2, getUser());
		ResourceGeneralPage.clickCheckinButton(resourceMetaData.getName());
		
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		addResourceToServiceInCanvas(resourceMetaData);
	    
		serviceMetadata.setVersion("0.1");
		ServiceVerificator.verifyVFModuleCustomizationUUID(serviceMetadata);		
	}
	
	@Test
	public void checkoutCertifyRemainSameCustomizationUUIDServiceTest() throws Exception{
		String fileName2 = "vSeGW.csar";
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName2, getUser());
		ResourceGeneralPage.clickSubmitForTestingButton(resourceMetaData.getName());
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
		TesterOperationPage.certifyComponent(resourceMetaData.getName());
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());
		
		addResourceToServiceInCanvas(resourceMetaData);
	    
		serviceMetadata.setVersion("0.1");
		ServiceVerificator.verifyVFModuleCustomizationUUID(serviceMetadata);
		List<String> allVFModuleCustomizationUUIDs = ServiceVerificator.getAllVFModuleCustomizationUUIDs(serviceMetadata);
		
		ResourceGeneralPage.clickCheckinButton(serviceMetadata.getName());
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		GeneralPageElements.clickCheckoutButton();
		
		serviceMetadata.setVersion("0.2");
		assertTrue(ServiceVerificator.isEqualCustomizationUUIDsAfterChanges(allVFModuleCustomizationUUIDs, ServiceVerificator.getAllVFModuleCustomizationUUIDs(serviceMetadata)));
		
		ResourceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		TesterOperationPage.certifyComponent(serviceMetadata.getName());
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		ResourceGeneralPage.clickCheckoutButton();
		
		serviceMetadata.setVersion("1.1");
		serviceMetadata.setUniqueId(null);
		assertTrue(ServiceVerificator.isEqualCustomizationUUIDsAfterChanges(allVFModuleCustomizationUUIDs, ServiceVerificator.getAllVFModuleCustomizationUUIDs(serviceMetadata)));
	}


	public synchronized String addResourceToServiceInCanvas(ResourceReqDetails resourceMetaData) throws Exception {
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CompositionPage.searchForElement(resourceMetaData.getName());
		CanvasElement computeElement = canvasManager.createElementOnCanvas(resourceMetaData.getName());
		canvasManager.clickOnCanvaElement(computeElement);
		String selectedInstanceName = CompositionPage.getSelectedInstanceName();
		return selectedInstanceName;
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
