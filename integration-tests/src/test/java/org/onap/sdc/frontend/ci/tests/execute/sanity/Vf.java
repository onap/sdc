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
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.ResourceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.validation.ErrorValidationUtils;
import org.onap.sdc.frontend.ci.tests.verificator.ServiceVerificator;
import org.onap.sdc.frontend.ci.tests.verificator.VfModuleVerificator;
import org.onap.sdc.frontend.ci.tests.verificator.VfVerificator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.onap.sdc.frontend.ci.tests.datatypes.ArtifactInfo;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasElement;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasManager;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.LifeCycleStateEnum;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.VendorSoftwareProductObject;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.DeploymentArtifactPage;
import org.onap.sdc.frontend.ci.tests.pages.GeneralPageElements;
import org.onap.sdc.frontend.ci.tests.pages.InformationalArtifactPage;
import org.onap.sdc.frontend.ci.tests.pages.PropertiesPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceGeneralPage;
import org.onap.sdc.frontend.ci.tests.pages.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.utilities.ArtifactUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.OnboardingUiUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ResourceUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Vf extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Vf.class);

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


    @Test
    public void vfcLinkedToComputeInVfFlow() throws Exception {
        String fileName = "vFW_VFC2.yml";
        ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());

        try {
            ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
            ResourceGeneralPage.clickCertifyButton(atomicResourceMetaData.getName());

            ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
            ResourceUIUtils.createVF(vfMetaData, getUser());

            DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
            CanvasManager canvasManager = CanvasManager.getCanvasManager();
            CompositionPage.searchForElement(String.format("%s %s", DataTestIdEnum.LeftPanelCanvasItems.COMPUTE.getValue(), "1.0"));
            CanvasElement computeElement = canvasManager.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.COMPUTE);
            CompositionPage.searchForElement(atomicResourceMetaData.getName());
            CanvasElement cpElement = canvasManager.createElementOnCanvas(atomicResourceMetaData.getName());
            AssertJUnit.assertNotNull(cpElement);
            ServiceVerificator.verifyNumOfComponentInstances(vfMetaData, "0.1", 2, getUser());

            canvasManager.linkElements(computeElement.getElementNameOnCanvas(), cpElement.getElementNameOnCanvas());

            vfMetaData.setVersion("0.1");
            VfVerificator.verifyLinkCreated(vfMetaData, getUser(), 1);
        } finally {
            ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "1.0");
        }

    }

    @Test
    public void addUpdateDeleteDeploymentArtifactToVfTest() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

        List<ArtifactInfo> deploymentArtifactList = new ArrayList<>();
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
    public void addUpdateDeleteInformationalArtifact() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

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
    public void addPropertiesToVfcInstanceInVfTest() throws Exception {

        if (true) {
//			throw new SkipException("Open bug 373762, can't update properties on CP or VFC instance  on Composition screen");
            SetupCDTest.getExtendTest().log(Status.INFO, "Open bug 373762, can't update properties on CP or VFC instance  on Composition screen");
        }

        String fileName = "vFW_VFC.yml";
        ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());

        try {
            ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
            ResourceGeneralPage.clickCheckinButton(atomicResourceMetaData.getName());

            ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
            ResourceUIUtils.createVF(vfMetaData, getUser());

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

    @Test
    public void changeInstanceVersionTest() throws Exception {

        ResourceReqDetails atomicResourceMetaData = null;
        ResourceReqDetails vfMetaData = null;
        CanvasManager vfCanvasManager;
        CanvasElement vfcElement = null;
        String fileName = "vFW_VFC3.yml";
        try {
            atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
            ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
            ResourceGeneralPage.clickCheckinButton(atomicResourceMetaData.getName());

            vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
            ResourceUIUtils.createVF(vfMetaData, getUser());
            ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
            vfCanvasManager = CanvasManager.getCanvasManager();
            CompositionPage.searchForElement(atomicResourceMetaData.getName());
            vfcElement = vfCanvasManager.createElementOnCanvas(atomicResourceMetaData.getName());

            CompositionPage.clickCertifyButton(vfMetaData.getName());
            assert (false);
        } catch (Exception e) {
            String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
            String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.VALIDATED_RESOURCE_NOT_FOUND.name());
            AssertJUnit.assertTrue(errorMessage.contains(checkUIResponseOnError));


            GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
            ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
            vfCanvasManager = CanvasManager.getCanvasManager();
            CompositionPage.changeComponentVersion(vfCanvasManager, vfcElement, "1.0");

            //verfication
            VfVerificator.verifyInstanceVersion(vfMetaData, getUser(), atomicResourceMetaData.getName(), "1.0");
        } finally {
            ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "1.0");
        }

    }


    @Test
    public void addAllInformationalArtifactPlaceholdersInVfTest() throws Exception {

        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();

        for (DataTestIdEnum.InformationalArtifactsPlaceholders informArtifact : DataTestIdEnum.InformationalArtifactsPlaceholders.values()) {
            ArtifactUIUtils.fillPlaceHolderInformationalArtifact(informArtifact, filePath, "asc_heat 0 2.yaml", informArtifact.getValue());
        }

        AssertJUnit.assertTrue(InformationalArtifactPage.checkElementsCountInTable(DataTestIdEnum.InformationalArtifactsPlaceholders.values().length));
    }

    @Test
    public void verifyToscaArtifactsExist() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        final int numOfToscaArtifacts = 2;
        ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
        AssertJUnit.assertTrue(ToscaArtifactsPage.checkElementsCountInTable(numOfToscaArtifacts));

        for (int i = 0; i < numOfToscaArtifacts; i++) {
            String typeFromScreen = ToscaArtifactsPage.getArtifactType(i);
            AssertJUnit.assertTrue(typeFromScreen.equals(ArtifactTypeEnum.TOSCA_CSAR.getType()) || typeFromScreen.equals(ArtifactTypeEnum.TOSCA_TEMPLATE.getType()));
        }
        ToscaArtifactsPage.clickCertifyButton(vfMetaData.getName());
        VfVerificator.verifyToscaArtifactsInfo(vfMetaData, getUser());
    }

    @Test
    public void vfCertificationTest() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        String vfName = vfMetaData.getName();

        ResourceGeneralPage.clickCheckinButton(vfName);
        GeneralUIUtils.findComponentAndClick(vfName);
        ResourceGeneralPage.clickCertifyButton(vfName);

        vfMetaData.setVersion("1.0");
        VfVerificator.verifyVFLifecycle(vfMetaData, getUser(), LifecycleStateEnum.CERTIFIED);

        GeneralUIUtils.findComponentAndClick(vfName);
        VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CERTIFIED);
    }

    @Test
    public void deleteVfCheckedoutTest() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        GeneralPageElements.clickTrashButtonAndConfirm();

        vfMetaData.setVersion("0.1");
        VfVerificator.verifyVfDeleted(vfMetaData, getUser());
    }

    @Test
    public void revertVfMetadataTest() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

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
    public void addDeploymentArtifactInCompositionScreenTest() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();

        ArtifactInfo artifact = new ArtifactInfo(filePath, "Heat-File.yaml", "kuku", "artifact3", "OTHER");
        CompositionPage.showDeploymentArtifactTab();
        CompositionPage.clickAddArtifactButton();
        ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact, CompositionPage.artifactPopup());

        List<WebElement> actualArtifactList = GeneralUIUtils.getWebElementsListBy(By.className("i-sdc-designer-sidebar-section-content-item-artifact"));
        AssertJUnit.assertEquals(1, actualArtifactList.size());
    }

    // future removed from ui

    @Test
    public void addDeploymentArtifactAndVerifyInCompositionScreen() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

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

    @Test
    public void checkoutVfTest() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        ResourceGeneralPage.clickCheckinButton(vfMetaData.getName());
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
        GeneralPageElements.clickCheckoutButton();

        vfMetaData.setVersion("0.2");
        VfVerificator.verifyVFLifecycle(vfMetaData, getUser(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKOUT);

        ResourceGeneralPage.clickCertifyButton(vfMetaData.getName());
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
        ResourceGeneralPage.clickCheckoutButton();

        vfMetaData.setVersion("1.1");
        vfMetaData.setUniqueId(null);
        VfVerificator.verifyVFLifecycle(vfMetaData, getUser(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKOUT);
    }

    @Test
    public void deleteInstanceFromVfCanvas() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
        CanvasElement computeElement = vfCanvasManager.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.COMPUTE);
        CanvasElement portElement = vfCanvasManager.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.PORT);

        vfCanvasManager.clickOnCanvaElement(computeElement);
        vfCanvasManager.deleteElementFromCanvas(computeElement);

        VfVerificator.verifyNumOfComponentInstances(vfMetaData, 1, getUser());
    }

    @Test
    public void changeInstanceNameInVfTest() throws Exception {
        final ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());
        Resource containerObject = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, vfMetaData.getName(), vfMetaData.getVersion());
        Resource computeObject = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, "Compute", "1.0");
        ComponentInstance componentInstanceDetails = AtomicOperationUtils.addComponentInstanceToComponentContainer(computeObject, containerObject, UserRoleEnum.DESIGNER, true).left().value();
        containerObject = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, vfMetaData.getName(), vfMetaData.getVersion());
        String intanceWithUpdatedName  = AtomicOperationUtils.updateComponentInstanceName("newName", containerObject, componentInstanceDetails.getName(), getUser(), true).getRight().getName();
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        assertEquals(intanceWithUpdatedName, "newName");
    }

    @Test
    public void submitVfForTestingWithNonCertifiedAsset() throws Exception {
        String fileName = "vFW_VFC4.yml";

        ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
        ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
        ResourceGeneralPage.clickCheckinButton(atomicResourceMetaData.getName());

        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());
        DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CompositionPage.searchForElement(atomicResourceMetaData.getName());
        canvasManager.createElementOnCanvas(atomicResourceMetaData.getName());

        try {
            CompositionPage.clickCertifyButton(vfMetaData.getName());
            assert (false);
        } catch (Exception e) {
            String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
            String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.VALIDATED_RESOURCE_NOT_FOUND.name());
            AssertJUnit.assertTrue(errorMessage.contains(checkUIResponseOnError));
        } finally {
            ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "0.1");
        }
    }

    @Test
    public void isDisabledAndReadOnlyInCheckin() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());
        ResourceGeneralPage.clickCheckinButton(vfMetaData.getName());
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
    public void exportToscaWithModulePropertiesVFTest() throws Exception {
        String vnfFile = "1-Vf-zrdm5bpxmc02-092017-(MOBILITY)_v2.0.zip";
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, FileHandling.getVnfRepositoryPath(), vnfFile, getUser());
        String vspName = vendorSoftwareProductObject.getName();
        ResourceGeneralPage.clickSubmitForTestingButton(vspName);
        Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, vspName, "0.1");
        VfModuleVerificator.validateSpecificModulePropertiesFromRequest(resource);
    }

    @Test
    public void canvasTestJS() throws Exception {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CanvasElement computeElement = canvasManager.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.COMPUTE);
        CanvasElement portElement = canvasManager.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.PORT);

        canvasManager.linkElements(computeElement.getElementNameOnCanvas(), portElement.getElementNameOnCanvas());

    }

    private void takeScreenshot(final String screenshotName, final String errorMsg) {
        try {
            ExtentTestActions.addScreenshot(Status.ERROR, screenshotName, errorMsg);
        } catch (final IOException e) {
            LOGGER.warn("Could not take screenshot", e);
        }
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
