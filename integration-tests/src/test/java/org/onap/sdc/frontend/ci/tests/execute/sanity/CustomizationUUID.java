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
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.verificator.CustomizationUUIDVerificator;
import org.onap.sdc.frontend.ci.tests.verificator.ServiceVerificator;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.ArtifactInfo;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasElement;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasManager;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.DeploymentArtifactPage;
import org.onap.sdc.frontend.ci.tests.pages.GeneralPageElements;
import org.onap.sdc.frontend.ci.tests.pages.HomePage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceGeneralPage;
import org.onap.sdc.frontend.ci.tests.pages.ServiceGeneralPage;
import org.onap.sdc.frontend.ci.tests.utilities.ArtifactUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ResourceUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ServiceUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class CustomizationUUID extends SetupCDTest {

    private static final String DESCRIPTION = "kuku";
    private static final String ARTIFACT_LABEL = "artifact3";
    private static final String ARTIFACT_LABEL_UPDATE = "artifactUpdate";
    private static final String GET_ARTIFACT_LIST_BY_CLASS_NAME = "i-sdc-designer-sidebar-section-content-item-artifact";
    private static final String HEAT_FILE_YAML_NAME = "Heat-File.yaml";
    private static final String HEAT_FILE_YAML_UPDATE_NAME = "Heat-File-Update.yaml";
    private String filePath;

    private CustomizationUUID() {
    }

    @BeforeMethod
    public void beforeTest() {
        filePath = FileHandling.getFilePath("");
    }

    @Test
    public void uniqueCustomizationUUIDforeachVFi() throws Exception {


        ResourceReqDetails vfMetaData = createNewResourceWithArtifactCertifyState();

        List customizationUUIDs = new ArrayList<String>();
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);


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


        ResourceReqDetails vfMetaData = createNewResourceWithArtifactCertifyState();

        List customizationUUIDs = new ArrayList<>();
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);

        DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CanvasElement VFiElement1 = addElemntToCanvas(vfMetaData, canvasManager);

        ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());

        canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
        addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);

        ServiceGeneralPage.clickCheckoutButton();
        canvasManager = CanvasManager.getCanvasManager();

        ArtifactInfo artifact = new ArtifactInfo(filePath, HEAT_FILE_YAML_NAME, DESCRIPTION, ARTIFACT_LABEL, ArtifactTypeEnum.SNMP_POLL.getType());

        canvasManager.clickOnCanvaElement(VFiElement1);
        CompositionPage.showDeploymentArtifactTab();
        CompositionPage.clickAddArtifactButton();
        ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact, CompositionPage.artifactPopup());


        ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());
        canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
        addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);


        ServiceGeneralPage.clickCheckoutButton();
        canvasManager = CanvasManager.getCanvasManager();
        canvasManager.clickOnCanvaElement(VFiElement1);
        CompositionPage.showDeploymentArtifactTab();
        List<WebElement> actualArtifactList = GeneralUIUtils.getWebElementsListBy(By.className(GET_ARTIFACT_LIST_BY_CLASS_NAME));
        GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_ITEM.getValue() + ARTIFACT_LABEL);
        SetupCDTest.getExtendTest().log(Status.INFO, "Going to delete " + HEAT_FILE_YAML_NAME + " artifact" + " and check if deleted");
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.DELETE.getValue() + ARTIFACT_LABEL);
        GeneralPageElements.clickOKButton();


        ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());
        canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
        addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);


        CustomizationUUIDVerificator.validateCustomizationUUIDuniqueness(customizationUUIDs);

    }


    @Test
    public void uniqueCustomizationUUIDchangeVFiVersion() throws Exception {


        ResourceReqDetails vfMetaData = createNewResourceWithArtifactCertifyState();

        List customizationUUIDs = new ArrayList<>();
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);

        DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CanvasElement VFiElement1 = addElemntToCanvas(vfMetaData, canvasManager);

        ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());

        canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
        addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);

        HomePage.navigateToHomePage();
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
        ResourceGeneralPage.clickCheckoutButton();
        ResourceGeneralPage.clickCertifyButton(vfMetaData.getName());


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


        ResourceReqDetails vfMetaData = createNewResourceWithArtifactCertifyState();

        List customizationUUIDs = new ArrayList<>();
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);

        DeploymentArtifactPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CanvasElement VFiElement1 = addElemntToCanvas(vfMetaData, canvasManager);

        ServiceGeneralPage.clickCheckinButton(serviceMetadata.getName());

        canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
        addCanvasElementToList(customizationUUIDs, canvasManager, VFiElement1);

        HomePage.navigateToHomePage();
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
        ResourceGeneralPage.clickCheckoutButton();
        //TODO Andrey should click on certify button
        ResourceGeneralPage.clickCertifyButton(vfMetaData.getName());

        canvasManager = findServiceAndNavigateToCanvas(serviceMetadata);
        ServiceGeneralPage.clickCheckoutButton();
        canvasManager = CanvasManager.getCanvasManager();
        CanvasElement contrailPortElement = canvasManager.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.CONTRAIL_PORT);
        canvasManager.linkElements(contrailPortElement, VFiElement1);

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

    public ResourceReqDetails createNewResourceWithArtifactCertifyState() throws Exception {
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

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();

        CompositionPage.searchForElement(NormativeTypesEnum.COMPUTE.name());
        CanvasManager canvasManagerVF = CanvasManager.getCanvasManager();
        CanvasElement VFiElement1 = canvasManagerVF.createElementOnCanvas(DataTestIdEnum.LeftPanelCanvasItems.COMPUTE);

        //TODO Andrey should click on certify button
        ResourceGeneralPage.clickCertifyButton(vfMetaData.getName());
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

    public static void changeDeleteAndValidateVersionOnGeneralPage(String previousVersion, String currentVersion, String serviceName) throws Exception {
        GeneralPageElements.selectVersion("V" + previousVersion);
        ServiceVerificator.verifyVersionUI(previousVersion);
        GeneralUIUtils.clickJSOnElementByText("latest version");
        ServiceVerificator.verifyVersionUI(currentVersion);
        GeneralPageElements.clickTrashButtonAndConfirm();
        GeneralUIUtils.findComponentAndClick(serviceName);
        ServiceVerificator.verifyVersionUI(previousVersion);
    }

}
