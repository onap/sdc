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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.InformationalArtifactsPlaceholders;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.ResourceMetadataEnum;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.InformationalArtifactPage;
import org.openecomp.sdc.ci.tests.pages.InputsPage;
import org.openecomp.sdc.ci.tests.pages.PropertiesPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.pages.ToscaArtifactsPage;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.PropertiesUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.verificator.ServiceVerificator;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

public class ImportDCAE extends SetupCDTest {

	private static final String SERVICE_INPUT_TEST_VF2_CSAR = "service_input_test_VF2.csar";
	private String filePath;
	
	@BeforeMethod
	public void beforeTest(){
		filePath = FileHandling.getFilePath("");
	}
	
	@Test
	public void updateDCAEAsset() throws Exception {
         ResourceReqDetails resourceMetaData = createDCAEAsset();

		// update Resource
		ResourceReqDetails updatedResource = new ResourceReqDetails();
		updatedResource.setName(ElementFactory.getResourcePrefix() + "UpdatedName" + resourceMetaData.getName());
		updatedResource.setDescription("kuku");
		updatedResource.setVendorName("updatedVendor");
		updatedResource.setVendorRelease("updatedRelease");
		updatedResource.setContactId("ab0001");
		updatedResource.setCategories(resourceMetaData.getCategories());
		updatedResource.setVersion("0.1");
 		List<String> newTags = resourceMetaData.getTags();
		newTags.remove(resourceMetaData.getName());
		newTags.add(updatedResource.getName());
		updatedResource.setTags(newTags);
		ResourceUIUtils.updateResource(updatedResource, getUser());

		VfVerificator.verifyVFMetadataInUI(updatedResource);
		VfVerificator.verifyVFUpdated(updatedResource, getUser());
	}
	
	@Test
	public void vfcLinkedToComputeInDCAEAssetFlowTest() throws Exception {
		String fileName = "importVFC_VFC14.yml";
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		
		try{
			ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
			ResourceGeneralPage.clickSubmitForTestingButton(atomicResourceMetaData.getName());
			
			reloginWithNewRole(UserRoleEnum.TESTER);
			GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
			TesterOperationPage.certifyComponent(atomicResourceMetaData.getName());
	
			reloginWithNewRole(UserRoleEnum.DESIGNER);
			ResourceReqDetails resourceMetaData = createDCAEAsset();
	
			DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
			CanvasManager canvasManager = CanvasManager.getCanvasManager();
			CanvasElement computeElement = canvasManager.createElementOnCanvas(LeftPanelCanvasItems.COMPUTE);
			CompositionPage.searchForElement(atomicResourceMetaData.getName());
			CanvasElement cpElement = canvasManager.createElementOnCanvas(atomicResourceMetaData.getName());
			Assert.assertNotNull(cpElement);
			ServiceVerificator.verifyNumOfComponentInstances(resourceMetaData, "0.1", 4, getUser());
			
			canvasManager.linkElements(cpElement, computeElement);
	
			resourceMetaData.setVersion("0.1");
			VfVerificator.verifyLinkCreated(resourceMetaData, getUser(), 1);
		}
		finally{
			ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "1.0");
		}

	}
	
	@Test
	public void addUpdateDeleteDeploymentArtifactToDCAEAssetTest() throws Exception {
		createDCAEAsset();
		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

		List<ArtifactInfo> deploymentArtifactList = new ArrayList<ArtifactInfo>();
		deploymentArtifactList.add(new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", ArtifactTypeEnum.OTHER.getType()));
		deploymentArtifactList.add(new ArtifactInfo(filePath, "sample-xml-alldata-1-1.xml", "cuku", "artifact2", ArtifactTypeEnum.YANG_XML.getType()));
		for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
			DeploymentArtifactPage.clickAddNewArtifact();
			ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact);
		}
		assertTrue("artifact table does not contain artifacts uploaded", DeploymentArtifactPage.checkElementsCountInTable(deploymentArtifactList.size()));
		
		String newDescription = "new description";
		DeploymentArtifactPage.clickEditArtifact(deploymentArtifactList.get(0).getArtifactLabel());
		DeploymentArtifactPage.artifactPopup().insertDescription(newDescription);
		DeploymentArtifactPage.artifactPopup().clickDoneButton();
		String actualArtifactDescription = DeploymentArtifactPage.getArtifactDescription(deploymentArtifactList.get(0).getArtifactLabel());
		assertTrue("artifact description is not updated", newDescription.equals(actualArtifactDescription));
		
		DeploymentArtifactPage.clickDeleteArtifact(deploymentArtifactList.get(0).getArtifactLabel());
		DeploymentArtifactPage.clickOK();
		assertTrue("artifact "+ deploymentArtifactList.get(0).getArtifactLabel() + "is not deleted", DeploymentArtifactPage.checkElementsCountInTable(deploymentArtifactList.size() - 1));
		
		assertTrue("artifact "+ deploymentArtifactList.get(1).getArtifactLabel() + "is not displayed", DeploymentArtifactPage.clickOnArtifactDescription(deploymentArtifactList.get(1).getArtifactLabel()).isDisplayed());
	}
	
	@Test
	public void addUpdateDeleteInformationalArtifactDCAEAssetTest() throws Exception {
		createDCAEAsset();
		ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
		
		ArtifactInfo informationalArtifact = new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", ArtifactTypeEnum.OTHER.getType());
		InformationalArtifactPage.clickAddNewArtifact();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(informationalArtifact);
		
		assertTrue("artifact table does not contain artifacts uploaded", InformationalArtifactPage.checkElementsCountInTable(1));
		
		String newDescription = "new description";
		InformationalArtifactPage.clickEditArtifact(informationalArtifact.getArtifactLabel());
		InformationalArtifactPage.artifactPopup().insertDescription(newDescription);
		InformationalArtifactPage.artifactPopup().clickDoneButton();
		String actualArtifactDescription = InformationalArtifactPage.getArtifactDescription(informationalArtifact.getArtifactLabel());
		assertTrue("artifact description is not updated", newDescription.equals(actualArtifactDescription));
		
		InformationalArtifactPage.clickDeleteArtifact(informationalArtifact.getArtifactLabel());
		InformationalArtifactPage.clickOK();
		assertTrue("artifact "+ informationalArtifact.getArtifactLabel() + "is not deleted", InformationalArtifactPage.checkElementsCountInTable(0));
	}
	
	@Test
	public void addPropertiesToVfcInstanceInDCAEAssetTest() throws Exception {
		String fileName = "importVFC_VFC15.yml";
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		
		try{
			ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
			ResourceGeneralPage.clickCheckinButton(atomicResourceMetaData.getName());
	
			createDCAEAsset();
	
			ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
			CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
			CompositionPage.searchForElement(atomicResourceMetaData.getName());
			CanvasElement vfcElement = vfCanvasManager.createElementOnCanvas(atomicResourceMetaData.getName());
	
			vfCanvasManager.clickOnCanvaElement(vfcElement);
			CompositionPage.showPropertiesAndAttributesTab();
			List<WebElement> properties = CompositionPage.getProperties();
			String propertyValue = "abc123";
			for (int i = 0; i < 2; i++) {
				WebElement findElement = properties.get(i).findElement(By.className("i-sdc-designer-sidebar-section-content-item-property-and-attribute-label"));
				findElement.click();
				PropertiesPage.getPropertyPopup().insertPropertyDefaultValue(propertyValue);
				PropertiesPage.getPropertyPopup().clickSave();				
				
				findElement = properties.get(i).findElement(By.className("i-sdc-designer-sidebar-section-content-item-property-value"));
				assertTrue(findElement.getText().equals(propertyValue));
			}
		}
		finally{
			ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "0.1");
		}
	}
	
	@Test
	public void changeInstanceVersionDCAEAssetTest() throws Exception{
		ResourceReqDetails atomicResourceMetaData = null;
		ResourceReqDetails vfMetaData = null;
		CanvasManager vfCanvasManager;
		CanvasElement vfcElement = null;
		String fileName = "importVFC_VFC16.yml";
		try{
			atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
			ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
			ResourceGeneralPage.clickSubmitForTestingButton(atomicResourceMetaData.getName());
			
			vfMetaData = createDCAEAsset();
			ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
			vfCanvasManager = CanvasManager.getCanvasManager();
			CompositionPage.searchForElement(atomicResourceMetaData.getName());
			vfcElement = vfCanvasManager.createElementOnCanvas(atomicResourceMetaData.getName());			
		
			CompositionPage.clickSubmitForTestingButton(vfMetaData.getName());
			assert(false);
		}
		catch(Exception e){ 
			String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
			String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.VALIDATED_RESOURCE_NOT_FOUND.name());
			Assert.assertTrue(errorMessage.contains(checkUIResponseOnError));
			
			
			reloginWithNewRole(UserRoleEnum.TESTER);
			GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
			TesterOperationPage.certifyComponent(atomicResourceMetaData.getName());
			
			reloginWithNewRole(UserRoleEnum.DESIGNER);
			GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
			ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
			vfCanvasManager = CanvasManager.getCanvasManager();
			CompositionPage.changeComponentVersion(vfCanvasManager, vfcElement, "1.0");
			
			//verfication
			VfVerificator.verifyInstanceVersion(vfMetaData, getUser(), atomicResourceMetaData.getName(), "1.0");
		}
			
		finally{
			ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "1.0");
		}
		
	}
	
	// future removed from ui
	@Test(enabled = false)
	public void addUpdateDeleteSimplePropertiesToDCAEAssetTest() throws Exception{
		createDCAEAsset();
			
		ResourceGeneralPage.getLeftMenu().moveToPropertiesScreen();
		List<PropertyTypeEnum> propertyList = Arrays.asList(PropertyTypeEnum.STRING, PropertyTypeEnum.INTEGER);
		int propertiesCount = PropertiesPage.getElemenetsFromTable().size();	
		for (PropertyTypeEnum prop : propertyList){
			PropertiesUIUtils.addNewProperty(prop);
		}
		assertTrue(GeneralUIUtils.checkElementsCountInTable(propertiesCount + propertyList.size(), () -> PropertiesPage.getElemenetsFromTable()));
		VfVerificator.verifyPropertiesInUI(propertyList);
		PropertiesPage.verifyTotalProperitesField(propertiesCount + propertyList.size());

		PropertyTypeEnum prop = propertyList.get(0);
		prop.setDescription("updatedDescription");
		prop.setValue("value");
		PropertiesUIUtils.updateProperty(prop);
		
		PropertiesPage.clickDeletePropertyArtifact(prop.getName());
		assertTrue(GeneralUIUtils.checkElementsCountInTable(propertiesCount + propertyList.size() - 1, () -> PropertiesPage.getElemenetsFromTable()));
	}
	
	// future removed from ui
	@Test(enabled = false)
	public void DCAEAssetInstancesInputScreenTest() throws Exception{
		createDCAEAsset();
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
		
		Map<String, String> elementsIntancesMap = new HashMap<String, String>();
		for (LeftPanelCanvasItems element : Arrays.asList(LeftPanelCanvasItems.DATABASE)){
			CanvasElement elementOnCanvas = vfCanvasManager.createElementOnCanvas(element);
			vfCanvasManager.clickOnCanvaElement(elementOnCanvas);
			String selectedInstanceName = CompositionPage.getSelectedInstanceName();
			elementsIntancesMap.put(selectedInstanceName, element.getValue());
		}

		CompositionPage.moveToInputsScreen();
		int canvasElementsSize = vfCanvasManager.getCanvasElements().size() + 2;
		List<String> inputsNamesFromTable = InputsPage.getVFCInstancesNamesFromTable();
		assertTrue(String.format("Instances count is not as Expected: %s Actual: %s", canvasElementsSize, inputsNamesFromTable.size()), inputsNamesFromTable.size() == canvasElementsSize);
		
		for (String instanceName :inputsNamesFromTable){
			String resourceName = instanceName.split(" ")[0];
			ResourceReqDetails resource = new ResourceReqDetails();
			resource.setName(resourceName);
			resource.setVersion("1.0");
			if (resourceName.equals("Port")){
				resource.setResourceType(ResourceTypeEnum.CP.toString());
			} else {
			    resource.setResourceType(ResourceTypeEnum.VFC.toString());
			}
			RestResponse restResponse = RestCDUtils.getResource(resource, getUser());
			Map<String, String> propertiesNameTypeJson = ResponseParser.getPropertiesNameType(restResponse);
			
			List<WebElement> propertyRowsFromTable = InputsPage.getInstancePropertiesList(resourceName);
			assertTrue("Some properties are missing in table. Instance name is : " + resourceName, propertyRowsFromTable.size() == propertiesNameTypeJson.size());
			VfVerificator.verifyVfInputs(instanceName, propertiesNameTypeJson, propertyRowsFromTable);
			
			GeneralUIUtils.clickOnElementByText(resourceName);
		}		
	}
	
	@Test
	public void addAllInformationalArtifactPlaceholdersInDCAEAssetTest() throws Exception{		
		createDCAEAsset();
		ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
		
		for(InformationalArtifactsPlaceholders informArtifact : InformationalArtifactsPlaceholders.values()){
			ArtifactUIUtils.fillPlaceHolderInformationalArtifact(informArtifact, filePath,"asc_heat 0 2.yaml", informArtifact.getValue());
		}
		
		assertTrue(InformationalArtifactPage.checkElementsCountInTable(InformationalArtifactsPlaceholders.values().length));
	}
	
	@Test
	public void verifyToscaArtifactsExistDCAEAssetTest() throws Exception{
		ResourceReqDetails vfMetaData = createDCAEAsset();
		
		final int numOfToscaArtifacts = 2;
		ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
		assertTrue(ToscaArtifactsPage.checkElementsCountInTable(numOfToscaArtifacts));
		
		for(int i = 0; i < numOfToscaArtifacts; i++){
			String typeFromScreen = ToscaArtifactsPage.getArtifactType(i);
			assertTrue(typeFromScreen.equals(ArtifactTypeEnum.TOSCA_CSAR.getType()) || typeFromScreen.equals(ArtifactTypeEnum.TOSCA_TEMPLATE.getType()));
		}
		
		ToscaArtifactsPage.clickSubmitForTestingButton(vfMetaData.getName());
		VfVerificator.verifyToscaArtifactsInfo(vfMetaData, getUser());
	}
	
	@Test
	public void DCAEAssetCertificationTest() throws Exception{
		ResourceReqDetails vfMetaData = createDCAEAsset();
		
		String vfName = vfMetaData.getName();
		
		ResourceGeneralPage.clickCheckinButton(vfName);
		GeneralUIUtils.findComponentAndClick(vfName);
		ResourceGeneralPage.clickSubmitForTestingButton(vfName);
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfName);
		TesterOperationPage.certifyComponent(vfName);
		
		vfMetaData.setVersion("1.0");
		VfVerificator.verifyVFLifecycle(vfMetaData, getUser(), LifecycleStateEnum.CERTIFIED);
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		GeneralUIUtils.findComponentAndClick(vfName);
		VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CERTIFIED);
	}
	
	@Test
	public void deleteDCAEAssetCheckedoutTest() throws Exception{
		ResourceReqDetails vfMetaData = createDCAEAsset();
		
		GeneralPageElements.clickTrashButtonAndConfirm();
		
		vfMetaData.setVersion("0.1");
		VfVerificator.verifyVfDeleted(vfMetaData, getUser());
	}
	
	@Test
	public void revertDCAEAssetMetadataTest() throws Exception{
		ResourceReqDetails vfMetaData = createDCAEAsset();
		
		ResourceReqDetails vfRevertDetails = new ResourceReqDetails();
		vfRevertDetails.setName("ciUpdatedName");
		vfRevertDetails.setDescription("kuku");
		vfRevertDetails.setCategories(vfMetaData.getCategories());
		vfRevertDetails.setVendorName("updatedVendor");
		vfRevertDetails.setVendorRelease("updatedRelease");
		ResourceUIUtils.fillResourceGeneralInformationPage(vfRevertDetails, getUser(), false);
		
		GeneralPageElements.clickRevertButton();
		
		VfVerificator.verifyVFMetadataInUI(vfMetaData);
	}
	
	@Test
	public void addDeploymentArtifactInCompositionScreenDCAEAssetTest() throws Exception{
		createDCAEAsset();
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		
		ArtifactInfo artifact = new ArtifactInfo(filePath, "Heat-File.yaml", "kuku", "artifact3",ArtifactTypeEnum.OTHER.getType());
		CompositionPage.showDeploymentArtifactTab();
		CompositionPage.clickAddArtifactButton();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact, CompositionPage.artifactPopup());
		
		List<WebElement> actualArtifactList = GeneralUIUtils.getWebElementsListBy(By.className("i-sdc-designer-sidebar-section-content-item-artifact"));
		Assert.assertEquals(1, actualArtifactList.size());
	}
	
	// future removed from ui
	@Test(enabled = false)
	public void addPropertyInCompositionScreenDCAEAssetTest() throws Exception{
		createDCAEAsset();
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		
		CompositionPage.showPropertiesAndAttributesTab();
		List<PropertyTypeEnum> propertyList = Arrays.asList(PropertyTypeEnum.STRING, PropertyTypeEnum.INTEGER);
		int propertiesCount = CompositionPage.getProperties().size();
		for (PropertyTypeEnum prop : propertyList){
			PropertiesUIUtils.addNewProperty(prop);
		}
		assertTrue(GeneralUIUtils.checkElementsCountInTable(propertiesCount + propertyList.size(), () -> CompositionPage.getProperties()));
	}
	
	@Test
	public void addDeploymentArtifactAndVerifyInCompositionScreenDCAEAssetTest() throws Exception{
		createDCAEAsset();
		
		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

		ArtifactInfo deploymentArtifact = new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", ArtifactTypeEnum.OTHER.getType());
		DeploymentArtifactPage.clickAddNewArtifact();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact);
		assertTrue(DeploymentArtifactPage.checkElementsCountInTable(1));
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		
		CompositionPage.showDeploymentArtifactTab();
		List<WebElement> deploymentArtifactsFromScreen = CompositionPage.getDeploymentArtifacts();
		assertTrue(1 == deploymentArtifactsFromScreen.size());
		
		String actualArtifactFileName = deploymentArtifactsFromScreen.get(0).getText();
		assertTrue("asc_heat-0-2.yaml".equals(actualArtifactFileName));
	}
	
	@Test
	public void checkoutDCAEAssetTest() throws Exception{
		ResourceReqDetails vfMetaData = createDCAEAsset();
		
		ResourceGeneralPage.clickCheckinButton(vfMetaData.getName());
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		GeneralPageElements.clickCheckoutButton();
		
		vfMetaData.setVersion("0.2");
		VfVerificator.verifyVFLifecycle(vfMetaData, getUser(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKOUT);
		
		ResourceGeneralPage.clickSubmitForTestingButton(vfMetaData.getName());
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		TesterOperationPage.certifyComponent(vfMetaData.getName());
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		ResourceGeneralPage.clickCheckoutButton();
		
		vfMetaData.setVersion("1.1");
		vfMetaData.setUniqueId(null);
		VfVerificator.verifyVFLifecycle(vfMetaData, getUser(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKOUT);
	}
	
	@Test
	public void deleteInstanceFromDCAEAssetCanvas() throws Exception{
		ResourceReqDetails vfMetaData = createDCAEAsset();
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
		CanvasElement computeElement = CompositionPage.addElementToCanvasScreen(LeftPanelCanvasItems.COMPUTE, vfCanvasManager);
		
		vfCanvasManager.clickOnCanvaElement(computeElement);
		vfCanvasManager.deleteElementFromCanvas(computeElement);
				
		VfVerificator.verifyNumOfComponentInstances(vfMetaData, 2, getUser());
	}
	
	@Test
	public void changeInstanceNameInDCAEAssetTest() throws Exception{
		createDCAEAsset();
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
		CanvasElement computeElement = CompositionPage.addElementToCanvasScreen(LeftPanelCanvasItems.COMPUTE, vfCanvasManager);
		
		String updatedInstanceName = "updatedName";
		vfCanvasManager.updateElementNameInCanvas(computeElement, updatedInstanceName);
		
		String actualSelectedInstanceName = CompositionPage.getSelectedInstanceName();
		assertTrue(updatedInstanceName.equals(actualSelectedInstanceName));
	}
	
	@Test
	public void submitDCAEAssetForTestingWithNonCertifiedAsset() throws Exception{
		String fileName = "importVFC_VFC17.yml";

		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
		ResourceGeneralPage.clickSubmitForTestingButton(atomicResourceMetaData.getName());
		
		ResourceReqDetails vfMetaData = createDCAEAsset();
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CompositionPage.addElementToCanvasScreen(atomicResourceMetaData.getName(), canvasManager);
		
		try{
			CompositionPage.clickSubmitForTestingButton(vfMetaData.getName());
			assert(false);
		}
		catch(Exception e){ 
			String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
			String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.VALIDATED_RESOURCE_NOT_FOUND.name());
			Assert.assertTrue(errorMessage.contains(checkUIResponseOnError));	
		}
		finally{
			ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "0.1");
		}
	}
	
	@Test
	public void isDisabledAndReadOnlyInCheckinDCAEAssetTest() throws Exception{
		ResourceReqDetails vfMetaData = createDCAEAsset();
		ResourceGeneralPage.clickCheckinButton(vfMetaData.getName());
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		
		ResourceMetadataEnum[] fieldsForCheck = {ResourceMetadataEnum.RESOURCE_NAME,
								   ResourceMetadataEnum.DESCRIPTION, 
								   ResourceMetadataEnum.VENDOR_NAME, 
								   ResourceMetadataEnum.VENDOR_RELEASE,
								   ResourceMetadataEnum.CONTACT_ID,
								   ResourceMetadataEnum.CATEGORY,
								   ResourceMetadataEnum.TAGS};

		for (ResourceMetadataEnum field: fieldsForCheck){
			VfVerificator.verifyIsElementDisabled(field.getValue(), field.name());
		}
		VfVerificator.verifyIsElementDisabled(DataTestIdEnum.LifeCyleChangeButtons.CREATE.getValue(), DataTestIdEnum.LifeCyleChangeButtons.CREATE.name());
	}
	
	@Test
	public void removeFileFromGeneralPageDCAEAssetTest() throws Exception{
		String fileName2 = SERVICE_INPUT_TEST_VF2_CSAR;		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		ResourceUIUtils.importVfFromCsarNoCreate(resourceMetaData, filePath, fileName2, getUser());
		GeneralPageElements.clickDeleteFile();
		
		try{
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue(), 30);
			assert(false);
		}
		catch(Exception e){
			assert(true);	
		}
	}
	
	@Test
	public void activityLogDCAEAssetTest() throws Exception{
			createDCAEAsset();
			
	        ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
			
			ArtifactInfo informationalArtifact = new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", ArtifactTypeEnum.OTHER.getType());
			InformationalArtifactPage.clickAddNewArtifact();
			ArtifactUIUtils.fillAndAddNewArtifactParameters(informationalArtifact);
			
			ResourceGeneralPage.getLeftMenu().moveToActivityLogScreen();
			
			int numberOfRows = GeneralUIUtils.getElementsByCSS("div[class^='flex-container']").size();
			assertTrue("Wrong rows number, should be 2", numberOfRows == 2);
	}
	
	@Test
	public void checkinCheckoutChangeDeleteVersionDCAEAssetTest() throws Exception{
		ResourceReqDetails atomicResourceMetaData = createDCAEAsset();
		
		ResourceGeneralPage.clickCheckinButton(atomicResourceMetaData.getName());
		GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
		GeneralPageElements.clickCheckoutButton();		
		VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKOUT);
		
		GeneralPageElements.selectVersion("V0.1");
		VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKIN);
		GeneralUIUtils.clickJSOnElementByText("latest version");
		
		GeneralPageElements.clickTrashButtonAndConfirm();
		GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
		String actualVersion = GeneralUIUtils.getSelectedElementFromDropDown(DataTestIdEnum.GeneralElementsEnum.VERSION_HEADER.getValue()).getText();
		assertTrue("Expected version: V0.1, Actual version: " + actualVersion, actualVersion.equals("V0.1"));
	}
	
	@Test
	public void badFileDCAEAssetTest() throws Exception {
		String customFileName = "badVF.csar";
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());		
		try{
			ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, customFileName, getUser());
			assert(false);
		}
		catch(Exception e){
			String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
			String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.CSAR_INVALID.name());
			SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating error messdge...")); 
			Assert.assertTrue(errorMessage.contains(checkUIResponseOnError));	
		}
	}
	
	@Test
	public void validContactAfterCreateDCAEAssetTest() throws Exception{
		ResourceReqDetails resourceMetaData = createDCAEAsset();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating that userID equal to user that was logged in...")); 
		assertTrue("Wrong userId", resourceMetaData.getContactId().equals(ResourceGeneralPage.getContactIdText()));
	}			
	
	public ResourceReqDetails createDCAEAsset() throws Exception{
		String fileName2 = SERVICE_INPUT_TEST_VF2_CSAR;		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName2, getUser());
		resourceMetaData.setVersion("0.1");
		return resourceMetaData;
	}	
	
	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
