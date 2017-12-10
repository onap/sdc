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

import static org.testng.AssertJUnit.assertNotNull;

import java.awt.AWTException;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.ci.tests.dataProviders.OnbordingDataProviders;
import org.openecomp.sdc.ci.tests.datatypes.AmdocsLicenseMembers;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.ToscaArtifactsScreenEnum;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.TypeHeatMetaDefinition;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.devCI.ArtifactFromCsar;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.DownloadManager;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.CsarParserUtils;
import org.openecomp.sdc.ci.tests.utils.ToscaParserUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;
import org.openecomp.sdc.ci.tests.verificator.ServiceVerificator;
import org.openecomp.sdc.ci.tests.verificator.VfModuleVerificator;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;

/**
 * @author al714h
 *
 */

public class VfModule extends SetupCDTest {


	@Test
	public void checkVfModulesCountAndStructure() throws Exception, AWTException {

//		String filePath = "src\\main\\resources\\Files\\VNFs";
		String filepath = FileHandling.getVnfRepositoryPath();
//		String vnfFile = "LDSA.zip";
//		String vnfFile = "FDNT.zip";
		List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileListExcludeToscaParserFailure();
		List<String> newRandomFileNamesFromFolder = OnbordingDataProviders.getRandomElements(1, fileNamesFromFolder);
		String filePath = org.openecomp.sdc.ci.tests.utils.general.FileHandling.getVnfRepositoryPath();
		String vnfFile = newRandomFileNamesFromFolder.get(0);
		getExtendTest().log(Status.INFO, String.format("Going to onboard the VNF %s......", vnfFile));
		System.out.println(String.format("Going to onboard the VNF %s......", vnfFile));

		AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUiUtils.createVendorLicense(getUser());
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String, Map<String, String>> createVendorSoftwareProduct = OnboardingUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, filepath, getUser(), amdocsLicenseMembers);
		String vspName = createVendorSoftwareProduct.left;
		//
		DownloadManager.downloadCsarByNameFromVSPRepository(vspName, createVendorSoftwareProduct.right.get("vspId"));
		File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();
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
		for(HeatMetaFirstLevelDefinition deploymentArtifact: deploymentArtifacts) {
			if(deploymentArtifact.getType().equals("HEAT_ENV")) {
				continue;
			}
			System.out.println("--------------");
			System.out.println(deploymentArtifact.getFileName());
			System.out.println(deploymentArtifact.getType());
//			System.out.println(deploymentArtifact.getFileName().trim().substring(0, deploymentArtifact.getFileName().lastIndexOf(".")));
			if(deploymentArtifact.getFileName().contains(".")) {
				ArtifactUIUtils.validateArtifactNameVersionType(deploymentArtifact.getFileName().trim().substring(0, deploymentArtifact.getFileName().lastIndexOf(".")), "1", deploymentArtifact.getType());
			} else {
				ArtifactUIUtils.validateArtifactNameVersionType(deploymentArtifact.getFileName().trim(), "1", deploymentArtifact.getType());
			}
			
		}
		
		DeploymentArtifactPage.verifyArtifactsExistInTable(filepath, vnfFile);

		DeploymentArtifactPage.clickSubmitForTestingButton(vspName);

		// create service
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ServiceUIUtils.createService(serviceMetadata, getUser());

		ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CompositionPage.searchForElement(vspName);
		CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
		CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vspName);
		assertNotNull(vfElement);
		ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());

		GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");
		ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
		GeneralUIUtils.clickOnElementByTestId(ToscaArtifactsScreenEnum.TOSCA_TEMPLATE.getValue());
		latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();
		
//		verification
		Service service = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, serviceMetadata.getName(), serviceMetadata.getVersion());
		ToscaDefinition toscaDefinition = ToscaParserUtils.parseToscaYamlToJavaObject(latestFilefromDir);

//		compare number of vf modules defined in HEAT.meta file vs Service TOSCA yaml
		VfModuleVerificator.compareNumberOfVfModules(listTypeHeatMetaDefinition, toscaDefinition);
		VfModuleVerificator.verifyGroupMetadata(toscaDefinition, service);

		getExtendTest().log(Status.INFO, String.format("Onboarding %s test is passed ! ", vnfFile));

	}

	

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
