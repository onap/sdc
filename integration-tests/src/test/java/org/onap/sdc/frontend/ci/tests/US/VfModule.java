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

package org.onap.sdc.frontend.ci.tests.US;

import com.aventstack.extentreports.Status;
import org.onap.sdc.frontend.ci.tests.pages.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.DeploymentArtifactPage;
import org.onap.sdc.frontend.ci.tests.pages.HomePage;
import org.onap.sdc.frontend.ci.tests.pages.ServiceGeneralPage;
import org.onap.sdc.backend.ci.tests.utils.general.FileHandling;
import org.openecomp.sdc.be.model.Service;
import org.onap.sdc.backend.ci.tests.data.providers.OnboardingDataProviders;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasElement;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasManager;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum.ToscaArtifactsScreenEnum;
import org.onap.sdc.backend.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.TypeHeatMetaDefinition;
import org.onap.sdc.backend.ci.tests.datatypes.VendorLicenseModel;
import org.onap.sdc.backend.ci.tests.datatypes.VendorSoftwareProductObject;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.execute.devCI.ArtifactFromCsar;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.ResourceGeneralPage;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaDefinition;
import org.onap.sdc.frontend.ci.tests.utilities.ArtifactUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.DownloadManager;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.OnboardingUiUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ServiceUIUtils;
import org.onap.sdc.backend.ci.tests.utils.CsarParserUtils;
import org.onap.sdc.backend.ci.tests.utils.ToscaParserUtils;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.general.OnboardingUtils;
import org.onap.sdc.backend.ci.tests.utils.general.VendorLicenseModelRestUtils;
import org.onap.sdc.backend.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.onap.sdc.frontend.ci.tests.verificator.ServiceVerificator;
import org.onap.sdc.frontend.ci.tests.verificator.VfModuleVerificator;
import org.testng.annotations.Test;

import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author al714h
 */

public class VfModule extends SetupCDTest {


    @Test
    public void checkVfModulesCountAndStructure() throws Exception, AWTException {

//		String filePath = "src\\main\\resources\\Files\\VNFs";
        String filepath = org.onap.sdc.frontend.ci.tests.utilities.FileHandling.getVnfRepositoryPath();
//		String vnfFile = "LDSA.zip";
//		String vnfFile = "FDNT.zip";
        List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileListExcludeToscaParserFailure();
        List<String> newRandomFileNamesFromFolder = OnboardingDataProviders.getRandomElements(1, fileNamesFromFolder);
        String filePath = FileHandling.getVnfRepositoryPath();
        String vnfFile = newRandomFileNamesFromFolder.get(0);
        getExtendTest().log(Status.INFO, String.format("Going to onboard the VNF %s......", vnfFile));
        System.out.println(String.format("Going to onboard the VNF %s......", vnfFile));

        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject createVendorSoftwareProduct = VendorSoftwareProductRestUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, filepath, getUser(),
            vendorLicenseModel);
        String vspName = createVendorSoftwareProduct.getName();
        //
        DownloadManager.downloadCsarByNameFromVSPRepository(vspName, createVendorSoftwareProduct.getVspId());
        File latestFilefromDir = org.onap.sdc.frontend.ci.tests.utilities.FileHandling.getLastModifiedFileNameFromDir();
        List<TypeHeatMetaDefinition> listTypeHeatMetaDefinition = CsarParserUtils.getListTypeHeatMetaDefinition(latestFilefromDir);
        //
        getExtendTest().log(Status.INFO, String.format("Searching for onboarded %s", vnfFile));
        HomePage.showVspRepository();
        getExtendTest().log(Status.INFO, String.format("Going to import %s......", vnfFile.substring(0, vnfFile.indexOf("."))));

        OnboardingUiUtils.importVSP(createVendorSoftwareProduct);

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

        // Verify deployment artifacts
        Map<String, Object> combinedMap = ArtifactFromCsar.combineHeatArtifacstWithFolderArtifacsToMap(latestFilefromDir.getAbsolutePath());
        LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts = ((LinkedList<HeatMetaFirstLevelDefinition>) combinedMap.get("Deployment"));
        for (HeatMetaFirstLevelDefinition deploymentArtifact : deploymentArtifacts) {
            if (deploymentArtifact.getType().equals("HEAT_ENV")) {
                continue;
            }
            System.out.println("--------------");
            System.out.println(deploymentArtifact.getFileName());
            System.out.println(deploymentArtifact.getType());
//			System.out.println(deploymentArtifact.getFileName().trim().substring(0, deploymentArtifact.getFileName().lastIndexOf(".")));
            if (deploymentArtifact.getFileName().contains(".")) {
                ArtifactUIUtils.validateArtifactNameVersionType(deploymentArtifact.getFileName().trim().substring(0, deploymentArtifact.getFileName().lastIndexOf(".")), "1", deploymentArtifact.getType());
            } else {
                ArtifactUIUtils.validateArtifactNameVersionType(deploymentArtifact.getFileName().trim(), "1", deploymentArtifact.getType());
            }

        }

        DeploymentArtifactPage.verifyArtifactsExistInTable(filepath, vnfFile);
//TODO Andrey should click on certify button
        DeploymentArtifactPage.clickCertifyButton(vspName);

        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CompositionPage.searchForElement(vspName);
        CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
        CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vspName);
        assertNotNull(vfElement);
        ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());

        GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");
        ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
        GeneralUIUtils.clickOnElementByTestId(ToscaArtifactsScreenEnum.TOSCA_TEMPLATE.getValue());
        latestFilefromDir = org.onap.sdc.frontend.ci.tests.utilities.FileHandling.getLastModifiedFileNameFromDir();

//		verification
        Service service = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, serviceMetadata.getName(), serviceMetadata.getVersion());
        ToscaDefinition toscaDefinition = ToscaParserUtils.parseToscaYamlToJavaObject(latestFilefromDir);

//		compare number of vf modules defined in HEAT.meta file vs Service TOSCA yaml
        VfModuleVerificator.compareNumberOfVfModules(listTypeHeatMetaDefinition, toscaDefinition);
        VfModuleVerificator.verifyGroupMetadata(toscaDefinition, service);

        getExtendTest().log(Status.INFO, String.format("Onboarding %s test is passed ! ", vnfFile));

    }

}
