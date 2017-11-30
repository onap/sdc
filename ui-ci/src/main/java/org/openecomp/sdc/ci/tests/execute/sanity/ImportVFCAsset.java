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
import java.util.List;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.InformationalArtifactsPlaceholders;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.InformationalArtifactPage;
import org.openecomp.sdc.ci.tests.pages.PropertiesPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.pages.UploadArtifactPopup;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.PropertiesUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.verificator.PropertyVerificator;
import org.openecomp.sdc.ci.tests.verificator.VFCverificator;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ImportVFCAsset extends SetupCDTest {
	
	private ResourceReqDetails atomicResourceMetaData;
	
	private String filePath;
	@BeforeClass
	public void beforeClass(){
		filePath = FileHandling.getFilePath("");
	}
	
	@DataProvider(name = "assetFiles", parallel = false)
    public Object[][] createDataX() {
             return new Object[][] { { "importVFC_VFC9.yml" }, { "CP.yml" }, {"VL.yml"} };
    }
	
	@Test
	public void importVFCTest() throws Exception {
		String fileName = "importVFC_VFC1.yml";
		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
	}
	
	@Test
	public void importDuplicateVFCTest() throws Exception {
		String fileName = "importVFC_VFC2.yml";
		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
		ResourceGeneralPage.clickCheckinButton(atomicResourceMetaData.getName()); 
		
		ResourceReqDetails atomicResourceMetaDataDup = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_INFRASTRUCTURE, getUser());
		try{
			ResourceUIUtils.importVfc(atomicResourceMetaDataDup, filePath, fileName, getUser());
			assert(false);
		}
		catch(Exception e){
			String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
			String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.RESOURCE_ALREADY_EXISTS.name());
			Assert.assertTrue(errorMessage.contains(checkUIResponseOnError));	
		} 
	}
	
	@Test
	public void badFileVFCTest() throws Exception {
		String fileName = "importVFC_VFC3.yml";
		String customFileName = "Heat-File 1.yaml";

		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());		
		try{
			ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, customFileName, getUser());
			assert(false);
		}
		catch(Exception e){
			String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
			String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.INVALID_TOSCA_TEMPLATE.name());
			Assert.assertTrue(errorMessage.contains(checkUIResponseOnError));	
		}
	}
	
	@Test
	public void validContactAfterCreateVFCTest() throws Exception{
		String fileName = "importVFC_VFC4.yml";
		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
		
		assertTrue("wrong userId", atomicResourceMetaData.getContactId().equals(ResourceGeneralPage.getContactIdText()));
	}
	
	@Test
	public void validContactAfterUpdateVFCTest() throws Exception{
		String fileName = "importVFC_VFC5.yml";
		String userIdUpdated = "up1234";

		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
		
		ResourceUIUtils.defineUserId(userIdUpdated);
		assertTrue("userId is not updated",userIdUpdated.equals(ResourceGeneralPage.getContactIdText()));
	}
	
	@Test
	public void addUpdateDeleteDeploymentArtifactToVFCTest() throws Exception {
		String fileName = "importVFC_VFC6.yml";
		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());

		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

		List<ArtifactInfo> deploymentArtifactList = new ArrayList<ArtifactInfo>();
		deploymentArtifactList.add(new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "OTHER"));
		deploymentArtifactList.add(new ArtifactInfo(filePath, "sample-xml-alldata-1-1.xml", "cuku", "artifact2", "YANG_XML"));		
		for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
			DeploymentArtifactPage.clickAddNewArtifact();
			ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact, new UploadArtifactPopup(true) );
		}
		assertTrue("artifact table does not contain artifacts uploaded", DeploymentArtifactPage.checkElementsCountInTable(deploymentArtifactList.size()));
		
		String newDescription = "new description";
		DeploymentArtifactPage.updateDescription(newDescription, deploymentArtifactList.get(0));
		String actualArtifactDescription = DeploymentArtifactPage.getArtifactDescription(deploymentArtifactList.get(0).getArtifactLabel());
		assertTrue("artifact description is not updated", newDescription.equals(actualArtifactDescription));
		
		DeploymentArtifactPage.clickDeleteArtifact(deploymentArtifactList.get(0).getArtifactLabel());
		DeploymentArtifactPage.clickOK();
		assertTrue("artifact "+ deploymentArtifactList.get(0).getArtifactLabel() + "is not deleted", DeploymentArtifactPage.checkElementsCountInTable(deploymentArtifactList.size() - 1));
		
		assertTrue("artifact "+ deploymentArtifactList.get(1).getArtifactLabel() + "is not displayed", DeploymentArtifactPage.clickOnArtifactDescription(deploymentArtifactList.get(1).getArtifactLabel()).isDisplayed());
	}
	
	@Test
	public void addUpdateDeletePlaceholdersInformationalArtefactVFCTest() throws Exception{
		String fileName = "importVFC_VFC7.yml";
		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
		
        ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
        
		// create artifacts
        List<ArtifactInfo> informationalArtifactList = new ArrayList<ArtifactInfo>();
        informationalArtifactList.add(new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "OTHER"));
        informationalArtifactList.add(new ArtifactInfo(filePath, "sample-xml-alldata-1-1.xml", "cuuuuku", "artifact3", "HEAT"));
        for (ArtifactInfo informationalArtifact : informationalArtifactList) {
        	InformationalArtifactPage.clickAddNewArtifact();
			ArtifactUIUtils.fillAndAddNewArtifactParameters(informationalArtifact);
		}		
		assertTrue("artifact table does not contain artifacts uploaded", InformationalArtifactPage.checkElementsCountInTable(informationalArtifactList.size()));
		
		// update artifact description
		String newDescription = "new description";
		InformationalArtifactPage.clickEditArtifact(informationalArtifactList.get(0).getArtifactLabel());
		InformationalArtifactPage.artifactPopup().insertDescription(newDescription);
		InformationalArtifactPage.artifactPopup().clickDoneButton();
		String actualArtifactDescription = InformationalArtifactPage.getArtifactDescription(informationalArtifactList.get(0).getArtifactLabel());
		assertTrue("artifact description is not updated", newDescription.equals(actualArtifactDescription));
		
		// delete artifacts 
		for (ArtifactInfo informationalArtifact : informationalArtifactList) {
			InformationalArtifactPage.clickDeleteArtifact(informationalArtifact.getArtifactLabel());
			InformationalArtifactPage.clickOK();
		}
		
		assertTrue("not all artifacts is deleted", InformationalArtifactPage.checkElementsCountInTable(0));
		
		// fill placeholders
		for(InformationalArtifactsPlaceholders informArtifact : InformationalArtifactsPlaceholders.values()){
			ArtifactUIUtils.fillPlaceHolderInformationalArtifact(informArtifact, filePath,"asc_heat 0 2.yaml", informArtifact.getValue());
		}		
		InformationalArtifactPage.checkElementsCountInTable(InformationalArtifactsPlaceholders.values().length);
	}
	
	@Test
	public void addSimplePropertiesToVFCTest() throws Exception{
		String fileName = "importVFC_VFC8.yml";
		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
			
		ResourceGeneralPage.getLeftMenu().moveToPropertiesScreen();
		List<PropertyTypeEnum> propertyList = Arrays.asList(PropertyTypeEnum.STRING, PropertyTypeEnum.INTEGER, PropertyTypeEnum.FLOAT);
		int propertiesCount = PropertiesPage.getElemenetsFromTable().size();	
		for (PropertyTypeEnum prop : propertyList){
			PropertiesUIUtils.addNewProperty(prop);
		}
		assertTrue(GeneralUIUtils.checkElementsCountInTable(propertiesCount + propertyList.size(), () -> PropertiesPage.getElemenetsFromTable()));
		
	}
	
	@Test
	public void updateAfterCheckoutNewSimplePropertiesVFCTest() throws Exception{
		String fileName = "importVFC_VFC16.yml";
		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
			
		ResourceGeneralPage.getLeftMenu().moveToPropertiesScreen();
		List<PropertyTypeEnum> propertyList = Arrays.asList(PropertyTypeEnum.STRING, PropertyTypeEnum.INTEGER, PropertyTypeEnum.FLOAT);
		int propertiesCount = PropertiesPage.getElemenetsFromTable().size();	
		for (PropertyTypeEnum prop : propertyList){
			PropertiesUIUtils.addNewProperty(prop);
		}
		ResourceGeneralPage.clickCheckinButton(atomicResourceMetaData.getName());
		GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
		GeneralPageElements.clickCheckoutButton();
		ResourceGeneralPage.getLeftMenu().moveToPropertiesScreen();
		
		for (PropertyTypeEnum prop : propertyList){
			PropertiesUIUtils.updateProperty(prop);
		}
		assertTrue(GeneralUIUtils.checkElementsCountInTable(propertiesCount + propertyList.size(), () -> PropertiesPage.getElemenetsFromTable()));
		
		for (PropertyTypeEnum prop : propertyList){
				PropertiesPage.clickOnProperty(prop.getName());
				PropertyVerificator.validateEditVFCPropertiesPopoverFields(prop);
				PropertiesPage.getPropertyPopup().clickCancel();	
		}
		
	}
	
		
	@Test(dataProvider = "assetFiles")
	public void checkinCheckoutChangeDeleteVersionVFCTest(String customfileName) throws Exception{
		setLog(customfileName);		
//		getExtendTest().setDescription(customfileName);
		
		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, customfileName, getUser());
		
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
	public void certificationVFCTest() throws Exception{
		String fileName = "importVFC_VFC10.yml";
		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
		
		String vfName = atomicResourceMetaData.getName();
		
		ResourceGeneralPage.clickCheckinButton(vfName);
		GeneralUIUtils.findComponentAndClick(vfName);
		ResourceGeneralPage.clickSubmitForTestingButton(vfName);
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfName);
		TesterOperationPage.certifyComponent(vfName);
		
		atomicResourceMetaData.setVersion("1.0");
		VfVerificator.verifyVFLifecycle(atomicResourceMetaData, getUser(), LifecycleStateEnum.CERTIFIED);
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		GeneralUIUtils.findComponentAndClick(vfName);
		VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CERTIFIED);
	}
	
	@Test
	public void activityLogVFCTest() throws Exception{		
		String fileName = "importVFC_VFC11.yml";
		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
		
        ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
		
		ArtifactInfo informationalArtifact = new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "OTHER");
		InformationalArtifactPage.clickAddNewArtifact();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(informationalArtifact);
		
		ResourceGeneralPage.getLeftMenu().moveToActivityLogScreen();
		
		int numberOfRows = GeneralUIUtils.getElementsByCSS("div[class^='flex-container']").size();
		assertTrue("Wrong rows number, should be 2", numberOfRows == 2);
	}
	
	@Test
	public void removeFileFromGeneralPageVFCTest() throws Exception{
		String fileName = "importVFC_VFC12.yml";

		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfcNoCreate(atomicResourceMetaData, filePath, fileName, getUser());
		
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
	public void maxLengthGeneralInformationVFCTest() throws Exception{
		String fileName = "importVFC_VFC13.yml";
		atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, 
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());		
		ResourceUIUtils.fillMaxValueResourceGeneralInformationPage(atomicResourceMetaData);
		GeneralPageElements.clickUpdateButton();
		VFCverificator.verifyVFCUpdatedInUI(atomicResourceMetaData);
		GeneralPageElements.clickDeleteVersionButton();
	}	
	
	

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
