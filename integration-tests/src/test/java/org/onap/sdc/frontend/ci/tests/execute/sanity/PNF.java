/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.ResourceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.validation.ErrorValidationUtils;
import org.onap.sdc.frontend.ci.tests.verificator.VfVerificator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.ArtifactInfo;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasElement;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasManager;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.LifeCycleStateEnum;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.frontend.ci.tests.datatypes.TopMenuButtonsEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.TypesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.DeploymentArtifactPage;
import org.onap.sdc.frontend.ci.tests.pages.GeneralPageElements;
import org.onap.sdc.frontend.ci.tests.pages.GovernorOperationPage;
import org.onap.sdc.frontend.ci.tests.pages.InformationalArtifactPage;
import org.onap.sdc.frontend.ci.tests.pages.OpsOperationPage;
import org.onap.sdc.frontend.ci.tests.pages.PropertiesPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceGeneralPage;
import org.onap.sdc.frontend.ci.tests.pages.ServiceGeneralPage;
import org.onap.sdc.frontend.ci.tests.pages.TesterOperationPage;
import org.onap.sdc.frontend.ci.tests.pages.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.utilities.ArtifactUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.CatalogUIUtilitis;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ResourceUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ServiceUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

public class PNF extends SetupCDTest {

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
    public void updatePNF() throws Exception {

        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

        // update Resource
        ResourceReqDetails updatedResource = new ResourceReqDetails();
        updatedResource.setName(ElementFactory.getResourcePrefix() + "UpdatedName" + pnfMetaData.getName());
        updatedResource.setDescription("kuku");
        updatedResource.setVendorName("updatedVendor");
        updatedResource.setVendorRelease("updatedRelease");
        updatedResource.setContactId("ab0001");
        updatedResource.setCategories(pnfMetaData.getCategories());
        updatedResource.setVersion("0.1");
        updatedResource.setResourceType(ResourceTypeEnum.VF.getValue());
        List<String> newTags = pnfMetaData.getTags();
        newTags.remove(pnfMetaData.getName());
        newTags.add(updatedResource.getName());
        updatedResource.setTags(newTags);
        ResourceUIUtils.updateResource(updatedResource, getUser());

        VfVerificator.verifyVFMetadataInUI(updatedResource);
        VfVerificator.verifyVFUpdated(updatedResource, getUser());
    }

    @Test
    public void addUpdateDeleteInformationalArtifactPNFTest() throws Exception {
        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

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
    public void addPropertiesToVfcInstanceInPNFTest() throws Exception {

        if (true) {
//			throw new SkipException("Open bug 373762, can't update properties on CP or VFC instance  on Composition screen");
            SetupCDTest.getExtendTest().log(Status.INFO, "Open bug 373762, can't update properties on CP or VFC instance  on Composition screen");
        }

        String fileName = "CP02.yml";
        ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.CP, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());

        try {
            ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
            ResourceGeneralPage.clickCheckinButton(atomicResourceMetaData.getName());

            ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

            ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
            CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
            CompositionPage.searchForElement(atomicResourceMetaData.getName());
            CanvasElement cpElement = vfCanvasManager.createElementOnCanvas(atomicResourceMetaData.getName());

            vfCanvasManager.clickOnCanvaElement(cpElement);
            CompositionPage.showPropertiesAndAttributesTab();
            List<WebElement> properties = CompositionPage.getProperties();
            String propertyValue = "abc123";
            for (int i = 0; i < 2; i++) {
                WebElement findElement = properties.get(i).findElement(By.className("i-sdc-designer-sidebar-section-content-item-property-and-attribute-label"));
                findElement.click();
                PropertiesPage.getPropertyPopup().insertPropertyDefaultValue(propertyValue);
                PropertiesPage.getPropertyPopup().clickSave();

                findElement = properties.get(i).findElement(By.className("i-sdc-designer-sidebar-section-content-item-property-value"));
                SetupCDTest.getExtendTest().log(Status.INFO, "Validating properties");
                AssertJUnit.assertTrue(findElement.getText().equals(propertyValue));
            }
        } finally {
            ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "0.1");
        }
    }

    @Test
    public void changeInstanceVersionPNFTest() throws Exception {

        ResourceReqDetails atomicResourceMetaData = null;
        ResourceReqDetails pnfMetaData = null;
        CanvasManager vfCanvasManager;
        CanvasElement cpElement = null;
        String fileName = "CP03.yml";
        try {
            atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.CP, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
            ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
            //TODO Andrey should click on certify button
            ResourceGeneralPage.clickCertifyButton(atomicResourceMetaData.getName());

            pnfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.PNF, getUser());
            ResourceUIUtils.createPNF(pnfMetaData, getUser());
            ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
            vfCanvasManager = CanvasManager.getCanvasManager();
            CompositionPage.searchForElement(atomicResourceMetaData.getName());
            cpElement = vfCanvasManager.createElementOnCanvas(atomicResourceMetaData.getName());

            //TODO Andrey should click on certify button
            CompositionPage.clickCertifyButton(pnfMetaData.getName());
            assert (false);
        } catch (Exception e) {
            String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
            String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.VALIDATED_RESOURCE_NOT_FOUND.name());
            AssertJUnit.assertTrue(errorMessage.contains(checkUIResponseOnError));

			/*reloginWithNewRole(UserRoleEnum.TESTER);
			GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
			TesterOperationPage.certifyComponent(atomicResourceMetaData.getName());
			
			reloginWithNewRole(UserRoleEnum.DESIGNER);*/
            GeneralUIUtils.findComponentAndClick(pnfMetaData.getName());
            ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
            vfCanvasManager = CanvasManager.getCanvasManager();
            CompositionPage.changeComponentVersion(vfCanvasManager, cpElement, "1.0");

            //verfication
            VfVerificator.verifyInstanceVersion(pnfMetaData, getUser(), atomicResourceMetaData.getName(), "1.0");
        } finally {
            ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "1.0");
        }

    }

    @Test
    public void verifyToscaArtifactsExistPNFTest() throws Exception {
        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

        final int numOfToscaArtifacts = 2;
        ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
        AssertJUnit.assertTrue(ToscaArtifactsPage.checkElementsCountInTable(numOfToscaArtifacts));

        for (int i = 0; i < numOfToscaArtifacts; i++) {
            String typeFromScreen = ToscaArtifactsPage.getArtifactType(i);
            AssertJUnit.assertTrue(typeFromScreen.equals(ArtifactTypeEnum.TOSCA_CSAR.getType()) || typeFromScreen.equals(ArtifactTypeEnum.TOSCA_TEMPLATE.getType()));
        }
        //TODO Andrey should click on certify button
        ToscaArtifactsPage.clickCertifyButton(pnfMetaData.getName());
        pnfMetaData.setVersion("1.0");
        VfVerificator.verifyToscaArtifactsInfo(pnfMetaData, getUser());
    }

    @Test
    public void pnfCertificationTest() throws Exception {
        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

        String vfName = pnfMetaData.getName();

        ResourceGeneralPage.clickCheckinButton(vfName);
        GeneralUIUtils.findComponentAndClick(vfName);
        //TODO Andrey should click on certify button
        ResourceGeneralPage.clickCertifyButton(vfName);
		
		/*reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfName);
		TesterOperationPage.certifyComponent(vfName);*/

        pnfMetaData.setVersion("1.0");
        VfVerificator.verifyVFLifecycle(pnfMetaData, getUser(), LifecycleStateEnum.CERTIFIED);

        /*reloginWithNewRole(UserRoleEnum.DESIGNER);*/
        GeneralUIUtils.findComponentAndClick(vfName);
        VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CERTIFIED);
    }

    @Test
    public void deletePNFTest() throws Exception {
        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

        GeneralPageElements.clickTrashButtonAndConfirm();

        pnfMetaData.setVersion("0.1");
        VfVerificator.verifyVfDeleted(pnfMetaData, getUser());
    }

    @Test
    public void revertPNFMetadataTest() throws Exception {
        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

        ResourceReqDetails pvfRevertDetails = new ResourceReqDetails();
        pvfRevertDetails.setName("ciUpdatedName");
        pvfRevertDetails.setDescription("kuku");
        pvfRevertDetails.setCategories(pnfMetaData.getCategories());
        pvfRevertDetails.setVendorName("updatedVendor");
        pvfRevertDetails.setVendorRelease("updatedRelease");
        ResourceUIUtils.fillResourceGeneralInformationPage(pvfRevertDetails, getUser(), false);

        GeneralPageElements.clickRevertButton();

        VfVerificator.verifyVFMetadataInUI(pnfMetaData);
    }

    @Test
    public void checkoutPnfTest() throws Exception {
        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

        ResourceGeneralPage.clickCheckinButton(pnfMetaData.getName());
        GeneralUIUtils.findComponentAndClick(pnfMetaData.getName());
        GeneralPageElements.clickCheckoutButton();

        pnfMetaData.setVersion("0.2");
        VfVerificator.verifyVFLifecycle(pnfMetaData, getUser(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKOUT);
        //TODO Andrey should click on certify button
        ResourceGeneralPage.clickCertifyButton(pnfMetaData.getName());
		
		/*reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(pnfMetaData.getName());
		TesterOperationPage.certifyComponent(pnfMetaData.getName());
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);*/
        GeneralUIUtils.findComponentAndClick(pnfMetaData.getName());
        ResourceGeneralPage.clickCheckoutButton();

        pnfMetaData.setVersion("1.1");
        pnfMetaData.setUniqueId(null);
        VfVerificator.verifyVFLifecycle(pnfMetaData, getUser(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKOUT);
    }

    public ResourceReqDetails createPNFWithGenerateName() throws Exception {
        ResourceReqDetails pnfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.PNF, getUser());
        ResourceUIUtils.createPNF(pnfMetaData, getUser());
        return pnfMetaData;
    }

    @Test
    public void verifyPNF_UI_Limitations() throws Exception {
        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

        SetupCDTest.getExtendTest().log(Status.INFO, "Validating Deployment Artifact Left Side Menu not exist");
        assertTrue(GeneralUIUtils.isElementInvisibleByTestId("Deployment ArtifactLeftSideMenu"));
        SetupCDTest.getExtendTest().log(Status.INFO, "Validating Deployment Left Side Menu not exist");
        assertTrue(GeneralUIUtils.isElementInvisibleByTestId("DeploymentLeftSideMenu"));
    }

    @Test
    public void filteringCatalogByPNF() throws Exception {
        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

        String pnfName = pnfMetaData.getName();
        //TODO Andrey should click on certify button
        ResourceGeneralPage.clickCertifyButton(pnfName);
		/*reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(pnfName);
		TesterOperationPage.certifyComponent(pnfName);*/

        pnfMetaData.setVersion("1.0");
        VfVerificator.verifyVFLifecycle(pnfMetaData, getUser(), LifecycleStateEnum.CERTIFIED);

        /*reloginWithNewRole(UserRoleEnum.DESIGNER);*/
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        CatalogUIUtilitis.catalogFilterTypeChecBox(TypesEnum.valueOf("PNF"));
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating resource %s found", pnfName));
        GeneralUIUtils.clickOnElementByTestId(pnfName);
    }

    @Test
    public void addPNFtoServiceAndDistribute() throws Exception {
        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
        CompositionPage.searchForElement("ContrailPort");
        vfCanvasManager.createElementOnCanvas("ContrailPort");

        String pnfName = pnfMetaData.getName();
        //TODO Andrey should click on certify button
        ResourceGeneralPage.clickCertifyButton(pnfName);

		/*reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(pnfName);
		TesterOperationPage.certifyComponent(pnfName);*/

        pnfMetaData.setVersion("1.0");
        VfVerificator.verifyVFLifecycle(pnfMetaData, getUser(), LifecycleStateEnum.CERTIFIED);

        /*reloginWithNewRole(UserRoleEnum.DESIGNER);*/
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);
        DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CompositionPage.searchForElement(pnfName);
        CanvasElement pnfElement = canvasManager.createElementOnCanvas(pnfName);
        CanvasElement networkElement = canvasManager.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.CONTRAIL_VIRTUAL_NETWORK);

        canvasManager.linkElements(pnfElement, networkElement);
        String serviceName = serviceMetadata.getName();
        ServiceGeneralPage.clickSubmitForTestingButton(serviceName);
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceName);
        TesterOperationPage.certifyComponent(serviceName);

        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(serviceName);
        GovernorOperationPage.approveService(serviceName);

        reloginWithNewRole(UserRoleEnum.OPS);
        GeneralUIUtils.findComponentAndClick(serviceName);
        OpsOperationPage.distributeService();
    }

    @Test
    public void checkInfomationArtifactUploadLimitation() throws Exception {
        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();
        ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
        List<WebElement> webElements = GeneralUIUtils.getWebElementsListBy(By.xpath(DataTestIdEnum.ArtifactPageEnum.ADD_OTHER_ARTIFACT_BUTTON.getValue()));
        int numberOfElements = webElements.size();
        String buttonText = webElements.get(0).getText();
        SetupCDTest.getExtendTest().log(Status.INFO, "Verifying only one button exist: Add Other Artifact");
        assertTrue(buttonText.equalsIgnoreCase(("Add Other Artifact")));
        assertTrue(1 == numberOfElements, "There should be only one option for uploading artifact in PNF");
    }

    @Test
    public void checkNonCPVLObjectsNotExistInComosition() throws Exception {
        ResourceReqDetails pnfMetaData = createPNFWithGenerateName();
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        String pnfName = pnfMetaData.getName();
        // Searching for VFC element and make sure it not exist in PNF composition (Only VL/CP are allowed).
        CompositionPage.searchForElement("BlockStorage");
        SetupCDTest.getExtendTest().log(Status.INFO, "Verifying Element found");
        assertTrue(GeneralUIUtils.isElementInvisibleByTestId("BlockStorage"));
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
