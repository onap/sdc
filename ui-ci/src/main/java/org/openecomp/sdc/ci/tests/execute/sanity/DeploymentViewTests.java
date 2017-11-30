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

import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.DeploymentPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.verificator.DeploymentViewVerificator;
import org.openqa.selenium.WebElement;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

public class DeploymentViewTests extends SetupCDTest {
	
	private String filePath;
	
	@BeforeMethod
	public void beforeTest(){
		filePath = FileHandling.getFilePath("");
	}
	
	@DataProvider(name = "CSAR_VF_Files", parallel = false)
    public Object[][] createDataX() {
		return new Object[][] {{"vSeGWNew.csar"}, {"vSeGWNewDoubleMembers.csar"}, {"vSeGWNewSingleModule.csar"}};
    }
	
	
	@Test(dataProvider = "CSAR_VF_Files")
	public void deploymentScreenDCAEAssetImportCSARTest(String baseFileName) throws Exception{
    // 		
		setLog(baseFileName);		
//		getExtendTest().setDescription(baseFileName);
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, baseFileName, getUser());
		
		ResourceGeneralPage.getLeftMenu().moveToDeploymentViewScreen();
		List<WebElement> moduleRowsFromTable = GeneralUIUtils.getElementsByCSS(DataTestIdEnum.DeploymentScreen.MODULES.getValue());
		DeploymentViewVerificator verificator = new DeploymentViewVerificator(filePath + baseFileName);
		verificator.verifyDeploymentPageModules(moduleRowsFromTable);
		for(WebElement moduleRow : moduleRowsFromTable){
			String moduleRowText = moduleRow.getText();
			verificator.verifyDeploymentPageSubElements(moduleRowText.split("\\.\\.")[1]);
			DeploymentPage.updateModuleName(moduleRowText, "updatedName");
			String updatedModuleName = DeploymentPage.reconstructModuleName(moduleRowText.split("\\.\\."), "updatedName");
			verificator.verifyComponentNameChanged( moduleRowText, updatedModuleName);
			// Close module
			GeneralUIUtils.clickOnElementByText(updatedModuleName);
		}
	}
	
	@Test
	public void deploymentScreenDCAEAssetUpdateWithNewGroupCSAR_TC1368223_Test() throws Exception{		
		String baseFileName   = "baseUpdateMinusGroupFlowVF_NEW.csar";
		String updateFileName = "baseUpdateFlowVF_NEW.csar";
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating resource with %s groups ", 2));
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, baseFileName, getUser());
		
		Map<String, HashMap<String, String>> metaDataFromUI = DeploymentPage.collectMetaDataFromUI();
		metaDataFromUI.put("base_ldsa", new HashMap<String, String> (){ {put("version", "0"); 
                                                                         put("moduleID", "primary");}});
		
		// add new group, base_ldsa
		ResourceGeneralPage.getLeftMenu().moveToGeneralScreen();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating VF with new file, should be %s groups now", 3));
		ResourceUIUtils.updateVfWithCsar(filePath, updateFileName);		

		DeploymentViewVerificator.regularDepoymentScreenVerificator(metaDataFromUI, new DeploymentViewVerificator(filePath + updateFileName));		
		DeploymentViewVerificator.validateModuleNameUpadate();
	};
	
	@Test
	public void deploymentScreenDCAEAssetDeleteGroupFromCSAR_TC1368281_Test() throws Exception{		
		String baseFileName   = "baseUpdateFlowVF_NEW.csar";
		String updateFileName = "baseUpdateMinusGroupFlowVF_NEW.csar";
				
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating resource with %s groups ", 3));
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, baseFileName, getUser());
		
		Map<String, HashMap<String, String>> metaDataFromUI = DeploymentPage.collectMetaDataFromUI();
		
		// remove group base_ldsa
		ResourceGeneralPage.getLeftMenu().moveToGeneralScreen();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating VF with new file, should be %s groups now, base_ldsa group should be removed", 2));
		ResourceUIUtils.updateVfWithCsar(filePath, updateFileName);		
		
		// validate that group was removed
		DeploymentViewVerificator.regularDepoymentScreenVerificator(metaDataFromUI, new DeploymentViewVerificator(filePath + updateFileName));
		
		Map<String, HashMap<String, String>> metaDataFromUI2 = DeploymentPage.collectMetaDataFromUI();
		metaDataFromUI2.put("base_ldsa", new HashMap<String, String> (){ {put("version", "0"); 
		                                                                  put("moduleID", "primary");}});
		
		ResourceGeneralPage.getLeftMenu().moveToGeneralScreen();
		// add group base_ldsa
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating VF with new file, should be %s groups now, base_ldsa group should be added", 3));
		ResourceUIUtils.updateVfWithCsar(filePath, baseFileName);
		
		// validate that group was added
		DeploymentViewVerificator.regularDepoymentScreenVerificator(metaDataFromUI2, new DeploymentViewVerificator(filePath + baseFileName));
		DeploymentViewVerificator.validateModuleNameUpadate();
	}
	
	@Test
	public void deploymentScreenDCAEAssetUpdateWithNewGroupWithoutMembersCSAR_TC1368280_Test() throws Exception{
		
		String baseFileName    = "baseUpdateMinusGroupFlowVF_NEW.csar";
		String updateFileName  = "baseUpdateAddGroupNoMembersUpdateFlow_NEW.csar";
		String updateFileName2 = "baseUpdateFlowVF_NEW.csar";
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating resource with %s groups ", 2));
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, baseFileName, getUser());				
		
		// add new group without members, base_ldsa
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating VF with new file, should be %s groups now, base_ldsa group without members", 3));
		ResourceUIUtils.updateVfWithCsar(filePath, updateFileName);		
		
		Map<String, HashMap<String, String>> metaDataFromUI = DeploymentPage.collectMetaDataFromUI();
        
		// validate that group was added and no members exist
		DeploymentViewVerificator.regularDepoymentScreenVerificator(null, new DeploymentViewVerificator(filePath + updateFileName));
	    
		ResourceGeneralPage.getLeftMenu().moveToGeneralScreen();
		// add group base_ldsa with members
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating VF with new file, should be %s groups now, base_ldsa group with members", 3));
		ResourceUIUtils.updateVfWithCsar(filePath, updateFileName2);
		
		// validate that member was added to base_ldsa group
		DeploymentViewVerificator.regularDepoymentScreenVerificator(metaDataFromUI, new DeploymentViewVerificator(filePath + updateFileName2));
		DeploymentViewVerificator.validateModuleNameUpadate();
	};
	
	@Test
	public void deploymentScreenDCAEAssetImportCSARWithArtifactSection_TC1368282_1_Test() throws Exception{		
		String baseFileName   = "baseUpdateFlowTwoArtifactsToGroup_NEW.csar";
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating resource with %s groups, should be 4 artifacts in every group ", 3));
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, baseFileName, getUser());					

		DeploymentViewVerificator.regularDepoymentScreenVerificator(null, new DeploymentViewVerificator(filePath + baseFileName));
		DeploymentViewVerificator.validateModuleNameUpadate();
	};
	
	@Test
	public void deploymentScreenDCAEAssetImportCSARRemoveArtifact_TC1368282_2_Test() throws Exception{		
		String baseFileName   = "baseUpdateFlowTwoArtifactsToGroup_NEW.csar";
		String updateFileName = "baseUpdateFlowOneArtifactToGroup_NEW.csar";
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating resource with %s groups, should be 4 artifacts in every group ", 3));
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, baseFileName, getUser());
		
		Map<String, HashMap<String, String>> metaDataFromUI = DeploymentPage.collectMetaDataFromUI();
		
		ResourceGeneralPage.getLeftMenu().moveToGeneralScreen();
		// remove artifact from every group
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating VF with new file, should be 3 artifacts in every group"));
		ResourceUIUtils.updateVfWithCsar(filePath, updateFileName);

		DeploymentViewVerificator.regularDepoymentScreenVerificator(metaDataFromUI, new DeploymentViewVerificator(filePath + updateFileName));
		DeploymentViewVerificator.validateModuleNameUpadate();
	};
	
	@Test
	public void deploymentScreenDCAEAssetImportCSARAddArtifact_TC1368282_3_Test() throws Exception{		
		String baseFileName   = "baseUpdateFlowTwoArtifactsToGroup_NEW.csar";
		String updateFileName = "baseUpdateFlowOneArtifactToGroup_NEW.csar";
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating resource with %s groups, should be 3 artifacts in every group ", 3));
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, updateFileName, getUser());
		
		Map<String, HashMap<String, String>> metaDataFromUI = DeploymentPage.collectMetaDataFromUI();
		
		ResourceGeneralPage.getLeftMenu().moveToGeneralScreen();
		// add artifact to every group
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating VF with new file, should be 4 artifacts in every group"));
		ResourceUIUtils.updateVfWithCsar(filePath, baseFileName);

		DeploymentViewVerificator.regularDepoymentScreenVerificator(metaDataFromUI, new DeploymentViewVerificator(filePath + baseFileName));
		DeploymentViewVerificator.validateModuleNameUpadate();
	};
	
	@Test
	public void deploymentScreenDCAEAssetImportCSARMixArtifacts_TC1368282_4_Test() throws Exception{		
		String baseFileName   = "baseUpdateFlowTwoArtifactsToGroup_NEW.csar";
		String updateFileName = "baseUpdateMixedArtifacts_NEW.csar";
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating resource with %s groups, should be 4 artifacts in every group ", 3));
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, baseFileName, getUser());
		
		Map<String, HashMap<String, String>> metaDataFromUI = DeploymentPage.collectMetaDataFromUI();
		
		ResourceGeneralPage.getLeftMenu().moveToGeneralScreen();
		//mix artifacts between groups
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating VF with new file, mixing between artifacts and groups", 3));
		ResourceUIUtils.updateVfWithCsar(filePath, updateFileName);
		
		DeploymentViewVerificator.regularDepoymentScreenVerificator(metaDataFromUI, new DeploymentViewVerificator());
		DeploymentViewVerificator.validateModuleNameUpadate();
	};
	
	@Test
	public void deploymentScreenDCAEAssetUpdateVFModule_TC1296437_Test() throws Exception{
		String baseFileName   = "baseUpdateMinusGroupFlowVF_NEW.csar";
		String updateFileName = "baseUpdateFlowVF_NEW.csar";
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating resource with %s groups ", 2));
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, baseFileName, getUser());
//		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating %s group version, should be %s ", moduleRowText, metaDataFromUI.get(moduleRowText.split("\\.\\.")[1])));
		
		Map<String, HashMap<String, String>> metaDataFromUI = DeploymentPage.collectMetaDataFromUI();
		metaDataFromUI.put("base_ldsa", new HashMap<String, String> (){ {put("version", "0"); 
                                                                         put("moduleID", "primary");}});
		
		DeploymentViewVerificator.validateEditPopover();
		
		ResourceGeneralPage.getLeftMenu().moveToGeneralScreen();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating VF with new file, should be %s groups now", 3));
		ResourceUIUtils.updateVfWithCsar(filePath, updateFileName);		
        
		assertTrue(resourceMetaData.getName().equals(ResourceGeneralPage.getNameText()));
		DeploymentViewVerificator.regularDepoymentScreenVerificator(metaDataFromUI, new DeploymentViewVerificator(filePath + updateFileName));		
		DeploymentViewVerificator.validateModuleNameUpadate();
	}
	

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
