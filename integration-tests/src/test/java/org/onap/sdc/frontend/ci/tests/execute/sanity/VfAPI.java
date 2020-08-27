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

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaDefinition;
import org.onap.sdc.backend.ci.tests.utils.ToscaParserUtils;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.ResourceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResponseParser;
import org.onap.sdc.frontend.ci.tests.verificator.ServiceVerificator;
import org.onap.sdc.frontend.ci.tests.verificator.VfModuleVerificator;
import org.onap.sdc.frontend.ci.tests.verificator.VfVerificator;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.onap.sdc.frontend.ci.tests.datatypes.ArtifactInfo;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasElement;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasManager;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.LifeCycleStateEnum;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.frontend.ci.tests.datatypes.TopMenuButtonsEnum;
import org.onap.sdc.backend.ci.tests.datatypes.VendorSoftwareProductObject;
import org.onap.sdc.backend.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.AttFtpClient;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.DeploymentArtifactPage;
import org.onap.sdc.frontend.ci.tests.pages.GeneralPageElements;
import org.onap.sdc.frontend.ci.tests.pages.HomePage;
import org.onap.sdc.frontend.ci.tests.pages.InformationalArtifactPage;
import org.onap.sdc.frontend.ci.tests.pages.InputsPage;
import org.onap.sdc.frontend.ci.tests.pages.PropertiesPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceGeneralPage;
import org.onap.sdc.frontend.ci.tests.pages.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.utilities.ArtifactUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.CatalogUIUtilitis;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.OnboardingUiUtils;
import org.onap.sdc.frontend.ci.tests.utilities.PropertiesUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ResourceUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.RestCDUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class VfAPI extends SetupCDTest {

    private String filePath;

    @BeforeClass
    public void beforeClass() {
        filePath = FileHandling.getFilePath("");
    }

    @BeforeMethod
    public void beforeTest() {
        System.out.println("File repository is : " + filePath);
        getExtendTest().log(Status.INFO, "File repository is : " + filePath);
    }

    private ResourceReqDetails createVFviaAPI() {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating VF %s", vfMetaData.getName()));
        AtomicOperationUtils.createResourceByResourceDetails(vfMetaData, UserRoleEnum.DESIGNER, true).left().value();
        return vfMetaData;
    }


    @Test
    public void updateVF() throws Exception {

        // create Resource
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(resourceMetaData, getUser());

        // update Resource
        ResourceReqDetails updatedResource = new ResourceReqDetails();
        updatedResource.setName(ElementFactory.getResourcePrefix() + "UpdatedName" + resourceMetaData.getName());
        updatedResource.setDescription("kuku");
        updatedResource.setVendorName("updatedVendor");
        updatedResource.setVendorRelease("updatedRelease");
        updatedResource.setContactId("ab0001");
        updatedResource.setCategories(resourceMetaData.getCategories());
        updatedResource.setVersion("0.1");
        updatedResource.setResourceType(ResourceTypeEnum.VF.getValue());
        List<String> newTags = resourceMetaData.getTags();
        newTags.remove(resourceMetaData.getName());
        newTags.add(updatedResource.getName());
        updatedResource.setTags(newTags);
        ResourceUIUtils.updateResource(updatedResource, getUser());

        VfVerificator.verifyVFMetadataInUI(updatedResource);
        VfVerificator.verifyVFUpdated(updatedResource, getUser());
    }


    @Test
    public void vfcLinkedToComputeInVfFlowApi() throws Exception {
        String fileName = "vFW_VFC2.yml";
        ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());

        try {
            //Create VFC via API
            Resource vfc = AtomicOperationUtils.importResource(filePath, fileName).left().value();
            atomicResourceMetaData.setName(vfc.getName());

            //Certify VFC via API
            vfc = (Resource) AtomicOperationUtils.changeComponentState(vfc, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

            //Create VF via API
            ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
            AtomicOperationUtils.createResourceByResourceDetails(vfMetaData, UserRoleEnum.DESIGNER, true).left().value();

            //Go to Catalog and find the created VF
            CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
            GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

            //Composition - drag the created VFC and another element, link between them
            DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
            CanvasManager canvasManager = CanvasManager.getCanvasManager();
            CompositionPage.searchForElement(String.format("%s %s", DataTestIdEnum.LeftPanelCanvasItems.COMPUTE.getValue(), "1.0"));
            CanvasElement computeElement = canvasManager.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.COMPUTE);
            CompositionPage.searchForElement(atomicResourceMetaData.getName());
            CanvasElement cpElement = canvasManager.createElementOnCanvas(atomicResourceMetaData.getName());
            AssertJUnit.assertNotNull(cpElement);
            ServiceVerificator.verifyNumOfComponentInstances(vfMetaData, "0.1", 2, getUser());
            canvasManager.linkElements(cpElement, computeElement);

            vfMetaData.setVersion("0.1");
            VfVerificator.verifyLinkCreated(vfMetaData, getUser(), 1);
        } finally {
            ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "1.0");
        }

    }


    @Test
    public void addUpdateDeleteDeploymentArtifactToVfTestApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = createVFviaAPI();

        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

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
        AssertJUnit.assertTrue("artifact " + deploymentArtifactList.get(0).getArtifactLabel() + "is not deleted", DeploymentArtifactPage.checkElementsCountInTable(deploymentArtifactList.size() - 1));

        AssertJUnit.assertTrue("artifact " + deploymentArtifactList.get(1).getArtifactLabel() + "is not displayed", DeploymentArtifactPage.clickOnArtifactDescription(deploymentArtifactList.get(1).getArtifactLabel()).isDisplayed());
    }


    @Test
    public void addUpdateDeleteInformationalArtifactApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = createVFviaAPI();

        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

        ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();

        ArtifactInfo informationalArtifact = new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "OTHER");
        InformationalArtifactPage.clickAddNewArtifact();
        ArtifactUIUtils.fillAndAddNewArtifactParameters(informationalArtifact);

        AssertJUnit.assertTrue("artifact table does not contain artifacts uploaded", InformationalArtifactPage.checkElementsCountInTable(1));

        String newDescription = "new description";
        InformationalArtifactPage.clickEditArtifact(informationalArtifact.getArtifactLabel());
        InformationalArtifactPage.artifactPopup().insertDescription(newDescription);
        InformationalArtifactPage.artifactPopup().clickDoneButton();
        String actualArtifactDescription = InformationalArtifactPage.getArtifactDescription(informationalArtifact.getArtifactLabel());
        AssertJUnit.assertTrue("artifact description is not updated", newDescription.equals(actualArtifactDescription));

        InformationalArtifactPage.clickDeleteArtifact(informationalArtifact.getArtifactLabel());
        InformationalArtifactPage.clickOK();
        AssertJUnit.assertTrue("artifact " + informationalArtifact.getArtifactLabel() + "is not deleted", InformationalArtifactPage.checkElementsCountInTable(0));
    }


    @Test
    public void addPropertiesToVfcInstanceInVfTestApi() throws Exception {

        String fileName = "vFW_VFC.yml";
        ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());

        try {
            //Create VFC via API
            Resource vfc = AtomicOperationUtils.importResource(filePath, fileName).left().value();
            atomicResourceMetaData.setName(vfc.getName());

            //Certify VFC via API
            vfc = (Resource) AtomicOperationUtils.changeComponentState(vfc, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

            //Create VF via API
            ResourceReqDetails vfMetaData = createVFviaAPI();

            //Go to Catalog and find the created VF
            CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
            GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

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
                AssertJUnit.assertTrue(findElement.getText().equals(propertyValue));
            }
        } finally {
            ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "0.1");
        }
    }
	
/*	@Test
	public void changeInstanceVersionTest() throws Exception{
		
		ResourceReqDetails atomicResourceMetaData = null;
		ResourceReqDetails vfMetaData = null;
		CanvasManager vfCanvasManager;
		CanvasElement vfcElement = null;
		String fileName = "vFW_VFC3.yml";
		try{
			atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
			ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
			ResourceGeneralPage.clickSubmitForTestingButton(atomicResourceMetaData.getName());
			
			vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
			ResourceUIUtils.createVF(vfMetaData, getUser());
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
			AssertJUnit.assertTrue(errorMessage.contains(checkUIResponseOnError));
			
			
			reloginWithNewRole(UserRoleEnum.TESTER);
			GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
			TesterOperationPage.certifyComponent(atomicResourceMetaData.getName());
			
			reloginWithNewRole(UserRoleEnum.DESIGNER);
			GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
			ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
			vfCanvasManager = CanvasManager.getCanvasManager();
			CompositionPage.changeComponentVersion(vfCanvasManager, vfcElement, "1.0");
			
			//verification
			VfVerificator.verifyInstanceVersion(vfMetaData, getUser(), atomicResourceMetaData.getName(), "1.0");
		}
			
		finally{
			ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "1.0");
		}
		
	}*/

    // future removed from ui
    @Test(enabled = true)
    public void addUpdateDeleteSimplePropertiesToVfTest() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        ResourceGeneralPage.getLeftMenu().moveToPropertiesScreen();
        List<PropertyTypeEnum> propertyList = Arrays.asList(PropertyTypeEnum.STRING, PropertyTypeEnum.INTEGER);
        int propertiesCount = PropertiesPage.getElemenetsFromTable().size();
        for (PropertyTypeEnum prop : propertyList) {
            PropertiesUIUtils.addNewProperty(prop);
        }
        AssertJUnit.assertTrue(GeneralUIUtils.checkElementsCountInTable(propertiesCount + propertyList.size(), () -> PropertiesPage.getElemenetsFromTable()));
        VfVerificator.verifyPropertiesInUI(propertyList);
        PropertiesPage.verifyTotalProperitesField(propertiesCount + propertyList.size());


        PropertyTypeEnum prop = propertyList.get(0);
        prop.setDescription("updatedDescription");
        prop.setValue("value");
        PropertiesUIUtils.updateProperty(prop);

        PropertiesPage.clickDeletePropertyArtifact(prop.getName());
        AssertJUnit.assertTrue(GeneralUIUtils.checkElementsCountInTable(propertiesCount + propertyList.size() - 1, () -> PropertiesPage.getElemenetsFromTable()));
    }

    // future removed from ui
    @Test(enabled = true)
    public void vfcInstancesInputScreenTest() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();

        Map<String, String> elementsIntancesMap = new HashMap<String, String>();
        for (DataTestIdEnum.LeftPanelCanvasItems element : Arrays.asList(DataTestIdEnum.LeftPanelCanvasItems.DATABASE, DataTestIdEnum.LeftPanelCanvasItems.BLOCK_STORAGE)) {
            CanvasElement elementOnCanvas = vfCanvasManager.createElementOnCanvas(element);
            vfCanvasManager.clickOnCanvaElement(elementOnCanvas);
            String selectedInstanceName = CompositionPage.getSelectedInstanceName();
            elementsIntancesMap.put(selectedInstanceName, element.getValue());
        }

        CompositionPage.moveToInputsScreen();
        int canvasElementsSize = vfCanvasManager.getCanvasElements().size();
        AssertJUnit.assertTrue("Instances count is not as expected: " + canvasElementsSize, InputsPage.checkElementsCountInTable(canvasElementsSize));

        for (String element : elementsIntancesMap.keySet()) {
            String resourceName = elementsIntancesMap.get(element);
            ResourceReqDetails resource = new ResourceReqDetails();
            resource.setName(resourceName);
            resource.setVersion("1.0");
            resource.setResourceType(ResourceTypeEnum.VFC.toString());
            RestResponse restResponse = RestCDUtils.getResource(resource, getUser());
            Map<String, String> propertiesNameTypeJson = ResponseParser.getPropertiesNameType(restResponse);

            List<WebElement> propertyRowsFromTable = InputsPage.getInstancePropertiesList(element);
            AssertJUnit.assertTrue("Some properties are missing in table. Instance name is : " + element, propertyRowsFromTable.size() == propertiesNameTypeJson.size());
            VfVerificator.verifyVfInputs(element, propertiesNameTypeJson, propertyRowsFromTable);

            GeneralUIUtils.clickOnElementByText(element);
        }

    }


    @Test
    public void addAllInformationalArtifactPlaceholdersInVfTestApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = createVFviaAPI();
        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

        ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
        int fileNameCounter = 0;
        String fileName;
        for (DataTestIdEnum.InformationalArtifactsPlaceholders informArtifact : DataTestIdEnum.InformationalArtifactsPlaceholders.values()) {
            fileName = HEAT_FILE_YAML_NAME_PREFIX + fileNameCounter + HEAT_FILE_YAML_NAME_SUFFIX;
            ArtifactUIUtils.fillPlaceHolderInformationalArtifact(informArtifact,
                    FileHandling.getFilePath("uniqueFileNames"), fileName,
                    informArtifact.getValue());
            fileNameCounter++;
        }
        assertThat(InformationalArtifactPage.checkElementsCountInTable(DataTestIdEnum.InformationalArtifactsPlaceholders.values().length)).isTrue();
    }

    @Test
    public void verifyToscaArtifactsExistApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = createVFviaAPI();

        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

        final int numOfToscaArtifacts = 2;
        ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
        AssertJUnit.assertTrue(ToscaArtifactsPage.checkElementsCountInTable(numOfToscaArtifacts));

        for (int i = 0; i < numOfToscaArtifacts; i++) {
            String typeFromScreen = ToscaArtifactsPage.getArtifactType(i);
            AssertJUnit.assertTrue(typeFromScreen.equals(ArtifactTypeEnum.TOSCA_CSAR.getType()) || typeFromScreen.equals(ArtifactTypeEnum.TOSCA_TEMPLATE.getType()));
        }

        //TODO Andrey should click on certify button
        ToscaArtifactsPage.clickCertifyButton(vfMetaData.getName());
        vfMetaData.setVersion("1.0");
        VfVerificator.verifyToscaArtifactsInfo(vfMetaData, getUser());
    }

    @Test(enabled = true)
    public void testDownload() throws Exception {
//		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
//		ResourceUIUtils.createResource(vfMetaData, getUser());
//		
//		final int numOfToscaArtifacts = 2;
//		ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
//		assertTrue(ToscaArtifactsPage.checkElementsCountInTable(numOfToscaArtifacts));
//		GeneralUIUtils.clickOnElementByTestId("download-Tosca Model");
//		System.out.println("download me");

        AttFtpClient attFtpClient = AttFtpClient.getFtpClient();

        File retrieveLastModifiedFileFromFTP = attFtpClient.retrieveLastModifiedFileFromFTP();
        attFtpClient.deleteFilesFromFTPserver();
    }

    @Test
    public void vfCertificationTestApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        Resource vf = AtomicOperationUtils.createResourceByResourceDetails(vfMetaData, UserRoleEnum.DESIGNER, true).left().value();

        //Certify  VF via API
        vf = (Resource) AtomicOperationUtils.changeComponentState(vf, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

        VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CERTIFIED);
    }

    @Test
    public void deleteVfCheckedoutTestApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = createVFviaAPI();

        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

        GeneralPageElements.clickTrashButtonAndConfirm();

        vfMetaData.setVersion("0.1");
        VfVerificator.verifyVfDeleted(vfMetaData, getUser());
    }

    @Test
    public void revertVfMetadataTestApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = createVFviaAPI();

        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

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
    public void addDeploymentArtifactInCompositionScreenTestApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = createVFviaAPI();

        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();

        ArtifactInfo artifact = new ArtifactInfo(filePath, "Heat-File.yaml", "kuku", "artifact3", "OTHER");
        CompositionPage.showDeploymentArtifactTab();
        CompositionPage.clickAddArtifactButton();
        ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact, CompositionPage.artifactPopup());

        List<WebElement> actualArtifactList = GeneralUIUtils.getWebElementsListBy(By.className("i-sdc-designer-sidebar-section-content-item-artifact"));
        AssertJUnit.assertEquals(1, actualArtifactList.size());
    }

    // future removed from ui
    @Test(enabled = true)
    public void addPropertyInCompositionScreenTest() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();

        CompositionPage.showPropertiesAndAttributesTab();
        List<PropertyTypeEnum> propertyList = Arrays.asList(PropertyTypeEnum.STRING, PropertyTypeEnum.INTEGER);
        int propertiesCount = CompositionPage.getProperties().size();
        for (PropertyTypeEnum prop : propertyList) {
            PropertiesUIUtils.addNewProperty(prop);
        }
        AssertJUnit.assertTrue(GeneralUIUtils.checkElementsCountInTable(propertiesCount + propertyList.size(), () -> CompositionPage.getProperties()));
    }

    @Test
    public void addDeploymentArtifactAndVerifyInCompositionScreenApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = createVFviaAPI();

        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

        ArtifactInfo deploymentArtifact = new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "OTHER");
        DeploymentArtifactPage.clickAddNewArtifact();
        ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact);
        AssertJUnit.assertTrue(DeploymentArtifactPage.checkElementsCountInTable(1));

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();

        CompositionPage.showDeploymentArtifactTab();
        List<WebElement> deploymentArtifactsFromScreen = CompositionPage.getDeploymentArtifacts();
        AssertJUnit.assertTrue(1 == deploymentArtifactsFromScreen.size());

        String actualArtifactFileName = deploymentArtifactsFromScreen.get(0).getText();
        AssertJUnit.assertTrue("asc_heat-0-2.yaml".equals(actualArtifactFileName));
    }
	
	/*@Test
	public void checkoutVfTest() throws Exception{
		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createVF(vfMetaData, getUser());
		
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
	}*/

    @Test
    public void deleteInstanceFromVfCanvasApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = createVFviaAPI();

        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
        CanvasElement computeElement = vfCanvasManager.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.COMPUTE);
        vfCanvasManager.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.PORT);

        vfCanvasManager.clickOnCanvaElement(computeElement);
        vfCanvasManager.deleteElementFromCanvas(computeElement);

        VfVerificator.verifyNumOfComponentInstances(vfMetaData, 1, getUser());
    }

    @Test
    public void changeInstanceNameInVfTestApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = createVFviaAPI();

        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
        CanvasElement computeElement = vfCanvasManager.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.COMPUTE);

        String updatedInstanceName = "updatedName";
        vfCanvasManager.updateElementNameInCanvas(computeElement, updatedInstanceName);

        String actualSelectedInstanceName = CompositionPage.getSelectedInstanceName();
        AssertJUnit.assertTrue(updatedInstanceName.equals(actualSelectedInstanceName));
    }
	
	
	/*@Test
	public void submitVfForTestingWithNonCertifiedAssetApi() throws Exception{
		String fileName = "vFW_VFC4.yml";
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		
		//Create VFC via API
		Resource vfc = AtomicOperationUtils.importResource(filePath, fileName).left().value();
		atomicResourceMetaData.setName(vfc.getName());
		
		//Submit VFC for testing via API
		vfc = (Resource) AtomicOperationUtils.changeComponentState(vfc, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true).getLeft();

		//Create VF via API
		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		AtomicOperationUtils.createResourceByResourceDetails(vfMetaData, UserRoleEnum.DESIGNER, true).left().value();
		
		//Go to Catalog and find the created VF
		CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
		GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
		
		DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CompositionPage.searchForElement(atomicResourceMetaData.getName());
		canvasManager.createElementOnCanvas(atomicResourceMetaData.getName());
		
		try{
			//TODO Andrey should click on certify button
			CompositionPage.clickCertifyButton(vfMetaData.getName());
			assert(false);
		}
		catch(Exception e){ 
			String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
			String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.VALIDATED_RESOURCE_NOT_FOUND.name());
			AssertJUnit.assertTrue(errorMessage.contains(checkUIResponseOnError));	
		}
		finally{
			ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "0.1");
		}
	}*/

    @Test
    public void isDisabledAndReadOnlyInCheckinApi() throws Exception {
        //Create VF via API
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        Resource vf = AtomicOperationUtils.createResourceByResourceDetails(vfMetaData, UserRoleEnum.DESIGNER, true).left().value();

        //Check in  VF via API
        vf = (Resource) AtomicOperationUtils.changeComponentState(vf, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

        //Go to Catalog and find the created VF
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

        DataTestIdEnum.ResourceMetadataEnum[] fieldsForCheck = {DataTestIdEnum.ResourceMetadataEnum.RESOURCE_NAME,
                DataTestIdEnum.ResourceMetadataEnum.DESCRIPTION, DataTestIdEnum.ResourceMetadataEnum.VENDOR_NAME, DataTestIdEnum.ResourceMetadataEnum.VENDOR_RELEASE,
                DataTestIdEnum.ResourceMetadataEnum.CONTACT_ID};

        for (DataTestIdEnum.ResourceMetadataEnum field : fieldsForCheck) {
            AssertJUnit.assertTrue(GeneralUIUtils.isElementReadOnly(field.getValue()));
        }

        AssertJUnit.assertTrue(GeneralUIUtils.isElementDisabled(DataTestIdEnum.ResourceMetadataEnum.CATEGORY.getValue()));
        AssertJUnit.assertTrue(GeneralUIUtils.isElementDisabled(DataTestIdEnum.LifeCyleChangeButtons.CREATE.getValue()));
    }

    @Test
    public void displayHomeAfterNavigationToOnboardingTest() throws Exception {
        //Production bug scenario: "Home" - Click on any VF/ Service - Copy the URL - Go to “ONBOARD” - Paste the URL - Review the breadcrumbs
        // Expected: "Home"       Actual: "Onboarding"

        //Create VF via API
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        Resource vf = AtomicOperationUtils.createResourceByResourceDetails(vfMetaData, UserRoleEnum.DESIGNER, true).left().value();

        //Check in  VF via API
        vf = (Resource) AtomicOperationUtils.changeComponentState(vf, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

        //Find the created VF on Home page
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        HomePage.navigateToHomePage();
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());

        //Copy current URL, navigate to OB screen and paste URL
        String url = GeneralUIUtils.copyCurrentURL();
        CompositionPage.moveToOnboardScreen();
        GeneralUIUtils.navigateToURL(url);
        GeneralUIUtils.ultimateWait();

        //Validate that main menu button is Home and not Onboarding
        String id = DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue();
        WebElement button = GeneralUIUtils.getWebElementByTestID(id);
        AssertJUnit.assertTrue(button.getAttribute("text").trim().equals("HOME"));
    }

    @Test
    public void exportToscaWithModulePropertiesVFTest() throws Exception {
        String vnfFile = "1-Vf-zrdm5bpxmc02-092017-(MOBILITY)_v2.0.zip";
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vsp = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, FileHandling.getVnfRepositoryPath(), vnfFile, getUser());
        String vspName = vsp.getName();
        ResourceGeneralPage.clickCertifyButton(vspName);
        Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, vspName, "1.0");
        VfModuleVerificator.validateSpecificModulePropertiesFromRequest(resource);
    }

    @Test
    public void exportToscaWithModulePropertiesTemplateCheckVFTest() throws Exception {
        String vnfFile = "1-Vf-zrdm5bpxmc02-092017-(MOBILITY)_v2.0.zip";
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        OnboardingUiUtils.onboardAndValidate(resourceReqDetails, FileHandling.getVnfRepositoryPath(), vnfFile, getUser());
        ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ToscaArtifactsScreenEnum.TOSCA_MODEL.getValue());
        File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();
        ToscaDefinition toscaDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(latestFilefromDir);
        VfModuleVerificator.validateSpecificModulePropertiesFromFile(toscaDefinition);
    }


    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
