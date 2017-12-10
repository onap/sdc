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
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openecomp.sdc.be.datatypes.elements.HeatParameterDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.ci.tests.businesslogic.ArtifactBusinessLogic;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.CompositionScreenEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.datatypes.HeatWithParametersDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.HomeUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openqa.selenium.WebElement;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;

public class VfArtifacts extends SetupCDTest{

	public static final String VSAEGW_FDNT_30_1607_E2E_ZIP = "2016-043_vsaegw_fdnt_30_1607_e2e.zip";
	public static final String VFW_FCGI_30_1607_E2E_ZIP = "2016-044_vfw_fcgi_30_1607_e2e.zip";
	public static final String FDNT_UPDATE_HEAT_PARAMS_ZIP = "FDNT_UpdateHeatParams.zip";
	public static final String FDNT_WITHOUT_ENV_FILES_ZIP = "FDNT_WithoutEnvFiles.zip";
	public static final String VLANDSLIDE_LDSA_30_1607_E2E_ZIP = "2016-014_vlandslide_ldsa_30_1607_e2e.zip";
	public static final String VJSA_VJSA_30_1610_E2E_ZIP = "2016-209_vjsa_vjsa_30_1610_e2e.zip";
	public static final String VLB_LMSP_30_1607_E2E_ZIP = "2016-045_vlb_lmsp_30_1607_e2e.zip";
	public static final String MOBT_MOBT_30_1607_E2E_ZIP = "2016-109_mobt_mobt_30_1607_e2e.zip";
	public static final String MOBT_MOBT_30_1607_E2E_DIFFERENT_PARAMS_ZIP = "2016-109_mobt_mobt_30_1607_e2e_DifferentParams.zip";
	public static final String VMSP_PXMC_30_1607_E2E_ZIP = "2016-042_vmsp_pxmc_30_1607_e2e.zip";
	public static final String VFW_FNAT_30_1607_E2E_ZIP = "2016-044_vfw_fnat_30_1607_e2e.zip";
	public static final String VLANDSLIDE_LDST_30_1607_E2E_ZIP = "2016-014_vlandslide_ldst_30_1607_e2e.zip";
	public static final String VIXIA_IXLA_30_1607_E2E_ZIP = "2016-017_vixia_ixla_30_1607_e2e.zip";
	private String filePath;
	private String vnfsRepositoryPath;
	private String createdEnvFilePath;
	private static final String PARAMETERS = "parameters";
	
	@BeforeMethod
	public void beforeTest() throws FileNotFoundException{
		filePath = getWindowTest().getDownloadDirectory();
		vnfsRepositoryPath = FileHandling.getFilePath("VfArtifacts");
		Config config = Utils.getConfig();
		createdEnvFilePath = config.getWindowsDownloadDirectory();
	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}
	
	
	@DataProvider(name = "heatEnvAndVersion", parallel = false)
//	parameters: VSP, updatedVsp, expectedHeatVersion, expectedHeatEnvVersion
	public Object[][] provideData() {

		return new Object[][] { 
			{ VSAEGW_FDNT_30_1607_E2E_ZIP, FDNT_UPDATE_HEAT_PARAMS_ZIP, "2", "2" }, //	expected heat version 2 and heatEnv 2
			{ VSAEGW_FDNT_30_1607_E2E_ZIP, FDNT_WITHOUT_ENV_FILES_ZIP, "1", "2" }, //	expected heat version 1 and heatEnv 2
			{VLANDSLIDE_LDSA_30_1607_E2E_ZIP, VJSA_VJSA_30_1610_E2E_ZIP, "1", "1" }, //	expected heat version 1 and heatEnv 1
			{VLB_LMSP_30_1607_E2E_ZIP, VLB_LMSP_30_1607_E2E_ZIP, "1", "2" }, //	expected heat version 1 and heatEnv 2(DE270634)
			{MOBT_MOBT_30_1607_E2E_ZIP, MOBT_MOBT_30_1607_E2E_DIFFERENT_PARAMS_ZIP, "2", "2" } //	expected heat version 2 and heatEnv 2
		};
	}
	
//	update first env file and verify parameters value
	@Test
	public void uploadUpdatedHeatEnv() throws Exception{
		
		String vnfFile = VMSP_PXMC_30_1607_E2E_ZIP;
		File updateEnvFile = null;
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String,Map<String,String>> vsp = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, vnfsRepositoryPath, vnfFile, getUser());
		String vspName = vsp.left;
		Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, vspName, "0.1");
		Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
		List<HeatWithParametersDefinition> envFilesList = ArtifactBusinessLogic.extractHeatWithParametersDefinition(deploymentArtifacts);
//		create env file and update it
		if(envFilesList.size()>0){
//		select index of env file to be updated 
			HeatWithParametersDefinition selectedEnvFileToUpdate = envFilesList.get(0);
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPageEnum.EDIT_ARTIFACT.getValue()+selectedEnvFileToUpdate.getHeatEnvLabel());
			updateEnvFile = ArtifactUIUtils.uploadCreatedUpdateParametersEnvFile(selectedEnvFileToUpdate, createdEnvFilePath);
			ArtifactUIUtils.verifyUpdatedEnvParameters(selectedEnvFileToUpdate, updateEnvFile);
		}
		else{
			SetupCDTest.getExtendTest().log(Status.INFO, "Resource does not contain HEAT files");
		}
	}

//	update all env files and verify parameters value in Deployment Artifact View
	@Test
	public void uploadUpdatedAllHeatEnv() throws Exception{
		
		String vnfFile = VFW_FNAT_30_1607_E2E_ZIP;
		File updateEnvFile = null;
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String,Map<String,String>> vsp = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, vnfsRepositoryPath, vnfFile, getUser());
		String vspName = vsp.left;
		Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, vspName, "0.1");
		Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
		List<HeatWithParametersDefinition> envFilesList = ArtifactBusinessLogic.extractHeatWithParametersDefinition(deploymentArtifacts);
		if(envFilesList.size()>0){
			for(HeatWithParametersDefinition selectedEnvFileToUpdate : envFilesList){
	//			create env file and update it
				GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPageEnum.EDIT_ARTIFACT.getValue()+selectedEnvFileToUpdate.getHeatEnvLabel());
				updateEnvFile = ArtifactUIUtils.uploadCreatedUpdateParametersEnvFile(selectedEnvFileToUpdate, createdEnvFilePath);
				ArtifactUIUtils.verifyUpdatedEnvParameters(selectedEnvFileToUpdate, updateEnvFile);
				}
		}else{
			SetupCDTest.getExtendTest().log(Status.INFO, "Resource does not contain HEAT files");
		}
	}
	
//	update all env files and verify parameters value in Composition
	@Test
	public void uploadUpdatedAllHeatEnvComposition() throws Exception{
		
		String vnfFile = VLANDSLIDE_LDST_30_1607_E2E_ZIP;
		File updateEnvFile = null;
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String,Map<String,String>> vsp = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, vnfsRepositoryPath, vnfFile, getUser());
		String vspName = vsp.left;
		Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, vspName, "0.1");
		Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
		List<HeatWithParametersDefinition> envFilesList = ArtifactBusinessLogic.extractHeatWithParametersDefinition(deploymentArtifacts);
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.StepsEnum.COMPOSITION.getValue());
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CompositionScreenEnum.DEPLOYMENT_ARTIFACT_TAB.getValue());
		if(envFilesList.size()>0){
			for(HeatWithParametersDefinition selectedEnvFileToUpdate : envFilesList){
	//			create env file and update it
				String dataTestId = DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_ITEM.getValue()+selectedEnvFileToUpdate.getHeatArtifactDisplayName();
				GeneralUIUtils.hoverOnAreaByTestId(dataTestId);
				GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPageEnum.EDIT_ARTIFACT.getValue()+selectedEnvFileToUpdate.getHeatArtifactDisplayName());
				updateEnvFile = ArtifactUIUtils.uploadCreatedUpdateParametersEnvFile(selectedEnvFileToUpdate, createdEnvFilePath);
				ArtifactUIUtils.verifyUpdatedEnvParameters(selectedEnvFileToUpdate, updateEnvFile, dataTestId);
				}
		}else{
			SetupCDTest.getExtendTest().log(Status.INFO, "Resource does not contain HEAT files");
		}
	}
	
//	expected heat version 1 and heatEnv 0
	@Test
	// Download ENV file from VF level Update VSP.
	public void downloadEnvFromVFLevelUpdateVSP() throws Throwable {
		String vnfFile = VSAEGW_FDNT_30_1607_E2E_ZIP;
		String updatedVnfFile= VLANDSLIDE_LDSA_30_1607_E2E_ZIP;
		String downloadDirPath=SetupCDTest.getConfig().getWindowsDownloadDirectory();
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String, Map<String, String>> CreatedVsp= OnboardingUiUtils.onboardAndValidate(resourceReqDetails, vnfsRepositoryPath, vnfFile, getUser());
		String vspName = CreatedVsp.left;
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
		OnboardingUiUtils.updateVnfAndValidate(vnfsRepositoryPath, CreatedVsp, updatedVnfFile, getUser());
		//get updated vsp env files
		Map<String, File> currentZipEnvfiles=ArtifactBusinessLogic.createEnvFilesListFromCsar(vspName, downloadDirPath);
		GeneralUIUtils.findComponentAndClick(vspName);
		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
		List<String> deploymentArtifcatsList = DeploymentArtifactPage.getDeploymentArtifactsNamesWorkSpace();
		
		for (int i = 0; i < deploymentArtifcatsList.size(); i++) {
			if (DeploymentArtifactPage.getArtifactType(deploymentArtifcatsList.get(i)).equalsIgnoreCase(ArtifactTypeEnum.HEAT.getType())) {
				DeploymentArtifactPage.clickDownloadEnvArtifact(deploymentArtifcatsList.get(i));
				GeneralUIUtils.ultimateWait();
				File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir(downloadDirPath);
				ArtifactUIUtils.compareYamlFilesByPattern(latestFilefromDir, currentZipEnvfiles.get(deploymentArtifcatsList.get(i)), PARAMETERS);
				}
		}
	}
	
	@Test
	// Download ENV file from VF level Work-Space.
	public void downloadEnvFromVFLevelWorkSpace() throws AWTException, Exception {
		String vnfFile = VSAEGW_FDNT_30_1607_E2E_ZIP;
		String downloadDirPath=SetupCDTest.getConfig().getWindowsDownloadDirectory();
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String, Map<String, String>> vsp= OnboardingUiUtils.onboardAndValidate(resourceReqDetails, vnfsRepositoryPath, vnfFile, getUser());
		Map<String, File> currentZipEnvfiles=ArtifactBusinessLogic.createEnvFilesListFromCsar(vsp.left,downloadDirPath);
		GeneralUIUtils.findComponentAndClick(vsp.left);
		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
		List<String> deploymentArtifcatsList = DeploymentArtifactPage.getDeploymentArtifactsNamesWorkSpace();
		
		for (int i = 0; i < deploymentArtifcatsList.size(); i++) {
			
			if (DeploymentArtifactPage.getArtifactType(deploymentArtifcatsList.get(i)).equalsIgnoreCase(ArtifactTypeEnum.HEAT.getType())) {
				DeploymentArtifactPage.clickDownloadEnvArtifact(deploymentArtifcatsList.get(i));
				GeneralUIUtils.ultimateWait();
				File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir(downloadDirPath);
				ArtifactUIUtils.compareYamlFilesByPattern(latestFilefromDir,currentZipEnvfiles.get(deploymentArtifcatsList.get(i)), PARAMETERS);
			}
		}
	}
	
	@Test
	// Download ENV file from VF level Composition.
	public void downloadEnvVFLevelComposition() throws AWTException, Exception {
		
		String downloadDirPath=SetupCDTest.getConfig().getWindowsDownloadDirectory();
		String vnfFile = VSAEGW_FDNT_30_1607_E2E_ZIP;
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String, Map<String, String>> vsp= OnboardingUiUtils.onboardAndValidate(resourceReqDetails, vnfsRepositoryPath, vnfFile, getUser());
		Map<String, File> currentZipEnvfiles=ArtifactBusinessLogic.createEnvFilesListFromCsar(vsp.left,downloadDirPath);
		GeneralUIUtils.findComponentAndClick(vsp.left);
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
		CompositionPage.clickOnTabTestID(CompositionScreenEnum.DEPLOYMENT_ARTIFACT_TAB);
		List<WebElement> deploymentArtifcatsList = CompositionPage.getCompositionEnvArtifacts();
		
		for (int i = 0; i < deploymentArtifcatsList.size(); i++) {
	    String fileName = GeneralUIUtils.getDataTestIdAttributeValue(deploymentArtifcatsList.get(i)).replace(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_ENV.getValue(), "");
			if (GeneralUIUtils.isElementVisibleByTestId(GeneralUIUtils.getDataTestIdAttributeValue(deploymentArtifcatsList.get(i)))) {
				CompositionPage.clickDownloadEnvArtifactComposition(fileName).click();
				GeneralUIUtils.ultimateWait();
				File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir(downloadDirPath);
				ArtifactUIUtils.compareYamlFilesByPattern(latestFilefromDir,currentZipEnvfiles.get(fileName), PARAMETERS);
			}
		}
	}
	
	@Test
	// Download ENV file from VF level Update parameters in UI.
	public void downloadEnvVFLevelUpdateParameters() throws AWTException, Exception {
		
		String vnfFile = VFW_FCGI_30_1607_E2E_ZIP;
		String downloadDirPath=SetupCDTest.getConfig().getWindowsDownloadDirectory();
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String, Map<String, String>> CreatedVsp= OnboardingUiUtils.onboardAndValidate(resourceReqDetails, vnfsRepositoryPath, vnfFile, getUser());
		Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, CreatedVsp.left, "0.1");
        Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
        List<HeatWithParametersDefinition> envFilesList = ArtifactBusinessLogic.extractHeatWithParametersDefinition(deploymentArtifacts);
        
        for (int i = 0; i < envFilesList.size(); i++) {
        	String artifactName = envFilesList.get(i).getHeatArtifactDisplayName();
			if (envFilesList.get(i).getHeatArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT.getType())) {
				ExtentTestActions.log(Status.INFO, String.format("Opening the edit/view artifact parameters form of %s resource...", vnfFile));
				DeploymentArtifactPage.clickEditEnvArtifact(envFilesList.get(i).getHeatArtifactDisplayName());
				
				ExtentTestActions.log(Status.INFO, String.format("Going To locating all artifact parameters from UI of  %s artifact...", artifactName));
				Map<?, ?> dataToWriteInUI = ArtifactUIUtils.getDataToWriteInUI(envFilesList.get(i).getHeatParameterDefinition());
				Map<?, ?> data = dataToWriteInUI;
				ExtentTestActions.log(Status.INFO, String.format("Success to locate all artifact parameters from UI of  %s artifact...", artifactName));
				
				List<HeatParameterDataDefinition> listToSearchEnvParametersInUI = envFilesList.get(i).getHeatParameterDefinition();
				fillHeatEnvParametersInUi(data, listToSearchEnvParametersInUI);
				
				DeploymentArtifactPage.clickSaveEnvParameters();
				GeneralUIUtils.waitForLoader();
				ExtentTestActions.log(Status.INFO, String.format("Going to get the %s updated resource ...", CreatedVsp.left));
				resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, CreatedVsp.left, "0.1");
		        deploymentArtifacts = resource.getDeploymentArtifacts();
		        Map<String,List<HeatWithParametersDefinition>> envFilesListupdated = new HashMap<>();
				ExtentTestActions.log(Status.INFO, String.format("Mapping the %s artifact parameters ...", artifactName));
		        envFilesListupdated.put(artifactName,ArtifactBusinessLogic.extractHeatWithParametersDefinition(deploymentArtifacts));
		        List<HeatWithParametersDefinition> heatEnvUpdatedParameters=envFilesListupdated.get(artifactName);
				DeploymentArtifactPage. clickDownloadEnvArtifact(artifactName);

				Map<String,Object> mapExpectedProperties = new HashMap<>();
				for (HeatParameterDataDefinition param : heatEnvUpdatedParameters.get(i).getHeatParameterDefinition()) {
					mapExpectedProperties.put(param.getName(), ArtifactUIUtils.getValue(param));
				}
				ArtifactUIUtils.compareYamlParametersByPattern(mapExpectedProperties, FileHandling.getLastModifiedFileNameFromDir(downloadDirPath), PARAMETERS);
			}
        }
	}


	@Test
	public void checkDefaultCreatedEnvArtifacts() throws Exception{
		String vnfFile = VIXIA_IXLA_30_1607_E2E_ZIP;
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String,Map<String,String>> vsp = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, vnfsRepositoryPath, vnfFile, getUser());
		String vspName = vsp.left;
		Map<String, File> generatedEnvFileList = ArtifactBusinessLogic.createEnvFilesListFromCsar(vspName, filePath);
		HomeUtils.findComponentAndClick(vspName);
		GeneralUIUtils.moveToStep(StepsEnum.DEPLOYMENT_ARTIFACT);
		for(Entry<String, File> envFileEntry : generatedEnvFileList.entrySet()){
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT_ENV.getValue()+envFileEntry.getKey());
			ArtifactUIUtils.compareYamlFilesByPattern(envFileEntry.getValue(), FileHandling.getLastModifiedFileNameFromDir(), PARAMETERS);
			if(true){
				throw new SkipException("Test skipped, new artifact version design should be developed");
			}
			ArtifactUIUtils.validateArtifactVersionByTypeAndLabel(envFileEntry.getKey(), "1", ArtifactTypeEnum.HEAT_ENV);
			ArtifactUIUtils.validateArtifactVersionByTypeAndLabel(envFileEntry.getKey(), "1", ArtifactTypeEnum.HEAT);
		}
	}
	
//	-------------------------------------------------------------------------------
	@Test(dataProvider = "heatEnvAndVersion")
	public void checkDefaultCreatedEnvArtifactsAfterVspUpdate(String vnfFile, String updatedVnfFile, String expectedHeatVersion, String expectedHeatEnvVersion) throws Throwable{
		String stringForLog = String.format("%s:%s:%s:%s", vnfFile, updatedVnfFile, expectedHeatVersion, expectedHeatEnvVersion);
		setLog(stringForLog);
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String,Map<String,String>> vsp = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, vnfsRepositoryPath, vnfFile, getUser());
		String vspName = vsp.left;
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
		OnboardingUiUtils.updateVnfAndValidate(vnfsRepositoryPath, vsp, updatedVnfFile, getUser());
		Map<String, File> generatedUpdatedEnvFileList = ArtifactBusinessLogic.createEnvFilesListFromCsar(vspName, filePath);
		HomeUtils.findComponentAndClick(vspName);
		GeneralUIUtils.moveToStep(StepsEnum.DEPLOYMENT_ARTIFACT);
		for(Entry<String, File> envFileEntry : generatedUpdatedEnvFileList.entrySet()){
//			TODO test will pass on case all objects on deployment view are visible 
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT_ENV.getValue()+envFileEntry.getKey());
			ArtifactUIUtils.compareYamlFilesByPattern(envFileEntry.getValue(), FileHandling.getLastModifiedFileNameFromDir(), PARAMETERS);
//			ArtifactUIUtils.validateArtifactVersionByTypeAndLabel(envFileEntry.getKey(), expectedHeatEnvVersion, ArtifactTypeEnum.HEAT_ENV);
//			ArtifactUIUtils.validateArtifactVersionByTypeAndLabel(envFileEntry.getKey(), expectedHeatVersion, ArtifactTypeEnum.HEAT);
		}
		if(true){
			throw new SkipException("Test skipped, new artifact version design should be developed");
		}
	}
	
//	expected heat version 1 and heatEnv 3
	@Test
	public void checkDefaultCreatedEnvArtifactsVspUpdatedWithSameVspTwice() throws Throwable{
		String vnfFile = VFW_FCGI_30_1607_E2E_ZIP;
		String updatedVnfFile = VFW_FCGI_30_1607_E2E_ZIP;
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String,Map<String,String>> vsp = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, vnfsRepositoryPath, vnfFile, getUser());
		String vspName = vsp.left;
		
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
		OnboardingUiUtils.updateVnfAndValidate(vnfsRepositoryPath, vsp, updatedVnfFile, getUser());
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
		OnboardingUiUtils.updateVnfAndValidate(vnfsRepositoryPath, vsp, updatedVnfFile, getUser());
		Map<String, File> generatedUpdatedSecondTimeEnvFileList = ArtifactBusinessLogic.createEnvFilesListFromCsar(vspName, filePath);
		HomeUtils.findComponentAndClick(vspName);
		GeneralUIUtils.moveToStep(StepsEnum.DEPLOYMENT_ARTIFACT);
		for(Entry<String, File> envFileEntry : generatedUpdatedSecondTimeEnvFileList.entrySet()){
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT_ENV.getValue()+envFileEntry.getKey());
			ArtifactUIUtils.compareYamlFilesByPattern(envFileEntry.getValue(), FileHandling.getLastModifiedFileNameFromDir(), PARAMETERS);
			if(true){
				throw new SkipException("Test skipped, new artifact version design should be developed");
			}
			ArtifactUIUtils.validateArtifactVersionByTypeAndLabel(envFileEntry.getKey(), "3", ArtifactTypeEnum.HEAT_ENV);
			ArtifactUIUtils.validateArtifactVersionByTypeAndLabel(envFileEntry.getKey(), "1", ArtifactTypeEnum.HEAT);
		}
	}
	
	
	public void downloadFile(Entry<String, File> envFileEntry) {
		int fileCountBefore = FileHandling.getFileCountFromDefaulDownloadDirectory();
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT_ENV.getValue()+envFileEntry.getKey());
		int fileCountAfter = FileHandling.getFileCountFromDefaulDownloadDirectory();
		assertTrue("Downloaded file is missing", (fileCountAfter - fileCountBefore) == 1 );
	}

	public static void fillHeatEnvParametersInUi(Map<?, ?> data,List<HeatParameterDataDefinition> listToSearchEnvParametersInUI) {
		ExtentTestActions.log(Status.INFO, String.format("Going to search parameters in UI and insert new current value to each parameter in UI..."));

		for (HeatParameterDataDefinition paramDefinition : listToSearchEnvParametersInUI){
			DeploymentArtifactPage.searchBoxEnv(paramDefinition.getName());
			WebElement currenValueField=GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.EnvParameterView.ENV_CURRENT_VALUE.getValue()+paramDefinition.getName());
			currenValueField.clear();
			currenValueField.sendKeys(data.get(paramDefinition.getName()).toString());
			GeneralUIUtils.ultimateWait();
			DeploymentArtifactPage.clearSearchBoxEnv();
		}
	}
	
}
