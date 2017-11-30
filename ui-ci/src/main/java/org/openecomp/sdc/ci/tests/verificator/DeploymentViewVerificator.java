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

package org.openecomp.sdc.ci.tests.verificator;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.execute.devCI.ArtifactFromCsar;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.DeploymentPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaGroupsTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utils.ToscaParserUtils;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

public class DeploymentViewVerificator {
	
	private static List<String> currentPropertiesForUI = Arrays.asList("isBase", 
            "vf_module_label", 
            "vf_module_description", 
            "min_vf_module_instances", 
            "max_vf_module_instances", 
            "initial_count", 
            "vf_module_type", 
            "volume_group",
            "vfc_list",
            "availability_zone_count");

	private static List<String> currentPropertiesWithoutIsBaseForFile = Arrays.asList("vf_module_label", 
            "vf_module_description", 
            "min_vf_module_instances", 
            "max_vf_module_instances", 
            "initial_count", 
            "vf_module_type", 
            "volume_group",
            "vfc_list",
            "availability_zone_count");
	
	public static List<String> getCurrentProperties() {
		return currentPropertiesForUI;
	}
	
	public static List<String> getCurrentPropertiesWithoutIsBase() {
		return currentPropertiesWithoutIsBaseForFile;
	}
	
	
	public static final String partToReplace = "_group";
	
	private static Map<String, HashMap<String, List<String>>> deploymentViewData = new HashMap<String, HashMap<String, List<String>>>(){
		{
			HashMap<String, List<String>> segw_heat_c3_base , segw_heat_c3_VMs1 ;
			
			segw_heat_c3_base = new HashMap<String, List<String>>();
			segw_heat_c3_base.put("members", Arrays.asList("segw_internet_security_group", "segw_security_group", "int_layer2vlan_net"));
			segw_heat_c3_base.put("artifacts", Arrays.asList("segw_heat_c3_base.yml", "segw_heat_c3_base.env"));
			segw_heat_c3_base.put("properties", currentPropertiesForUI);
			put("segw_heat_c3_base", segw_heat_c3_base);
			segw_heat_c3_VMs1 = new HashMap<String, List<String>>();
			segw_heat_c3_VMs1.put("members", Arrays.asList("segw_oam_protected_0_port", 
										                    "fw_oam_int_layer2vlan_1_port", 
										                    "segw_0", "segw_internet_1_port", 
										                    "segw_layer2vlan_2_port",
										                    "fw_gn_0", "fw_gn_hsl_direct_3_port", 
										                    "fw_oam_oam_mgmt_0_port", 
										                    "fw_oam_hsl_direct_3_port", 
										                    "fw_gn_oam_mgmt_0_port",
										                    "fw_oam_oam_direct_2_port",
										                    "fw_gn_gn_direct_2_port",
										                    "fw_oam_0",
										                    "fw_gn_int_layer2vlan_1_port"));
			segw_heat_c3_VMs1.put("artifacts", Arrays.asList("segw_heat_c3_VMs1.yml", "segw_heat_c3_VMs1.env"));
			segw_heat_c3_VMs1.put("properties", currentPropertiesForUI);
	        put("segw_heat_c3_VMs1", segw_heat_c3_VMs1);
		}
	};
	
	private static Map<String, HashMap<String, List<String>>> deploymentViewDataMixedArtifacts = new HashMap<String, HashMap<String, List<String>>>(){
		{
			HashMap<String, List<String>> module_1_ldsa, module_2_ldsa, base_ldsa;
			
			module_1_ldsa = new HashMap<String, List<String>>();
			module_1_ldsa.put("members", Stream.of("ltm_oam_protected_0_port", "ltm_dmz_direct_0_port", "ltm_server_0").collect(Collectors.toList()));
			module_1_ldsa.put("artifacts", Stream.of("module_1_ldsa.yaml", "module_1_ldsa.env", "base_ldsa.33.yaml", "module_1_ldsa.11.yaml").collect(Collectors.toList()));
			module_1_ldsa.put("properties", currentPropertiesForUI);
			put("module_1_ldsa", module_1_ldsa);
			module_2_ldsa = new HashMap<String, List<String>>();
			module_2_ldsa.put("members", Stream.of("ltm_server_0").collect(Collectors.toList()));
			module_2_ldsa.put("artifacts", Stream.of("module_2_ldsa.yaml", "module_2_ldsa.env", "base_ldsa.3.yaml", "module_2_ldsa.22.yaml").collect(Collectors.toList()));
			module_2_ldsa.put("properties", currentPropertiesForUI);
	        put("module_2_ldsa", module_2_ldsa);
	        base_ldsa = new HashMap<String, List<String>>();
	        base_ldsa.put("members", Stream.of("ldsa_sec_grp_1").collect(Collectors.toList()));
	        base_ldsa.put("artifacts", Stream.of("base_ldsa.yaml", "module_2_ldsa.2.yaml", "module_1_ldsa.1.yaml").collect(Collectors.toList()));
	        base_ldsa.put("properties", currentPropertiesForUI);
	        put("base_ldsa", base_ldsa);
		}
	};
	
	
	private Map<String, HashMap<String, List<String>>> deploymentViewDataFromFile;
	
	public DeploymentViewVerificator(String pathToCsar) throws Exception {		
		deploymentViewDataFromFile = buildDeploymentViewDataFromCSAR(pathToCsar);
	}
	
	public DeploymentViewVerificator() throws Exception {		
		deploymentViewDataFromFile = deploymentViewDataMixedArtifacts;
	}

	
	public  void verifyDeploymentPageSubElements(String moduleName) throws Exception{
		HashMap<String, List<String>> moduleProperties = getDeploymentViewData().get(moduleName);
		
		// add env placeholder to deployment view data
		if (!moduleProperties.get("artifacts").contains(moduleName + ".env")){
			moduleProperties.get("artifacts").add(moduleName + ".env");
		}
		
		List<WebElement> members, artifacts, properties;
		members = DeploymentPage.getGroupMembersList(moduleName);
		artifacts = DeploymentPage.getArtifactNames();
		properties = DeploymentPage.getPropertyNames();
		
		File imageFilePath = GeneralUIUtils.takeScreenshot(moduleName + UUID.randomUUID(), SetupCDTest.getScreenshotFolder(), null);
		final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating group  %s, should be %s members, %s artifacts " + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath), 
				                                                       moduleName, moduleProperties.get("members").size(), moduleProperties.get("artifacts").size()));
		
		assertTrue(moduleProperties.get("artifacts").size() == artifacts.size(), "Artifacts amount not as expected, expected " + moduleProperties.get("artifacts").size());		
		assertTrue(moduleProperties.get("artifacts").containsAll(artifacts.stream().
				                                                map(e -> e.getAttribute("textContent")).
				                                                collect(Collectors.toList())));
		assertTrue(moduleProperties.get("members").size() == members.size(), "Members amount not as expected, expected " + moduleProperties.get("members").size());
		assertTrue(moduleProperties.get("members").containsAll(members.stream().
												                map(e -> e.getAttribute("textContent")).
												                collect(Collectors.toList())));
		assertTrue(moduleProperties.get("properties").size() == properties.size(), "Properties amount not as expected, expected " + moduleProperties.get("properties").size());
		assertTrue(moduleProperties.get("properties").containsAll(properties.stream().
												                map(e -> e.getAttribute("textContent")).
												                collect(Collectors.toList())));
		DeploymentPage.clickOnProperties();
		DeploymentPage.clickOnArtifacts();
	}
	
	public  void verifyDeploymentPageModules(List<WebElement> modules){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating VF groups , should be %s groups ", getDeploymentViewData().size())); 
	    assertFalse(modules.isEmpty(), "No modules found");
	    assertTrue(modules.size() == getDeploymentViewData().size(), "Modules amount not as expected, expected " + getDeploymentViewData().size());
		for (WebElement module: modules){
		    assertTrue(getDeploymentViewData().containsKey(module.getText().split("\\.\\.")[1]));
		}
	}
	
	public static void verifyComponentNameChanged(String oldName, String newName){
		try{
			GeneralUIUtils.clickOnElementByText(oldName, 10);
			assertTrue(false, "Element name don't changed");
		} catch(Exception e){
			GeneralUIUtils.clickOnElementByText(newName);
		}
	}

	public  Map<String, HashMap<String, List<String>>> getDeploymentViewData() {
//		return deploymentViewData;
		return getDeploymentViewDataFromFile();
	}
	
	public static Map<String, HashMap<String, List<String>>> buildDeploymentViewDataFromCSAR(String pathToCSAR) throws Exception{
		ToscaDefinition toscaDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(pathToCSAR));
		Map<String, HashMap<String, List<String>>> deploymentViewDataFromFile = new HashMap<String, HashMap<String, List<String>>>();
		Map<String, ToscaGroupsTopologyTemplateDefinition> groups = toscaDefinition.getTopology_template().getGroups();
		List<String> keyList = groups.keySet().stream().collect(Collectors.toList());
		HashMap<String, List<String>> groupsToArtifacts = getDeploymentArtifactsMappedToGroupsFromCSAR(pathToCSAR);
		for(String groupKey: keyList){
			HashMap<String, List<String>> tempGroupMap = new HashMap<String, List<String>>();
			tempGroupMap.put("artifacts", groupsToArtifacts.get(convertAmdocsCsarGroupNameToSdcCsarGroupName(groupKey)));
			if (groups.get(groupKey).getMembers() == null){
				tempGroupMap.put("members", Arrays.asList());
			} else {
				tempGroupMap.put("members", groups.get(groupKey).getMembers());
			}			
			tempGroupMap.put("properties", currentPropertiesForUI);
			deploymentViewDataFromFile.put(convertAmdocsCsarGroupNameToSdcCsarGroupName(groupKey), tempGroupMap);
		}
		return deploymentViewDataFromFile;
	}
	
    public static HashMap<String, List<String>> getDeploymentArtifactsMappedToGroupsFromCSAR(String pathToFile) throws Exception {	
		Map<String, Object> combinedMap = ArtifactFromCsar.combineHeatArtifacstWithFolderArtifacsToMap(pathToFile);
		LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts = ((LinkedList<HeatMetaFirstLevelDefinition>) combinedMap.get("Deployment"));
		
		HashMap<String, List<String>> tempGroupMap = new HashMap<String, List<String>>();
		for(HeatMetaFirstLevelDefinition deploymentArtifact: deploymentArtifacts) {
			String groupName = deploymentArtifact.getFileName().trim().substring(0, deploymentArtifact.getFileName().indexOf("."));
			if(deploymentArtifact.getType().equals("HEAT") || deploymentArtifact.getType().equals("HEAT_NET") || deploymentArtifact.getType().equals("HEAT_VOL")) { 
				List<String> list = new ArrayList<>();
				list.add(deploymentArtifact.getFileName().trim());
				tempGroupMap.put(groupName, list);
			} else {
				// update current key 
				List<String> list = tempGroupMap.get(groupName);
				list.add(deploymentArtifact.getFileName().trim());
				tempGroupMap.put(groupName, list);
			}
		}
		return tempGroupMap;
	}
    

    public static void cleanFolders(String outputFolder) throws IOException {
		System.gc();
		FileUtils.cleanDirectory(new File(outputFolder));
		FileUtils.deleteDirectory(new File(outputFolder));
	}

	public static String unzipCsarFile(String pathToCsar) {
		File csarFile = new File(pathToCsar);
		
		
		File dir = new File(csarFile.getParent() + File.separator + "output"+ UUID.randomUUID() + File.separator + UUID.randomUUID());
		if(!dir.exists()) {
			dir.mkdirs();
		}

		String outputFolder = dir.getPath();
		ArtifactFromCsar.unZip(pathToCsar, outputFolder);
		return outputFolder;
	}
	
	public static void validateEditPopoverFields(String expectedVNFName, String expectedHeatName, String expectedModuleName){
		String VNFname = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DeploymentScreen.RESOURCE_NAME_ON_POPOVER.getValue()).getText();
		String heatName = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DeploymentScreen.NAME_INPUT.getValue()).getAttribute("value");
		String moduleName = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DeploymentScreen.MODULE_NAME_ON_POPOVER.getValue()).getText();
		assertTrue(expectedVNFName.equals(VNFname), String.format("VNF name Expected: %s, Actual: %s ", expectedVNFName, VNFname));
		assertTrue(expectedHeatName.equals(heatName), String.format("HEAT name Expected: %s, Actual: %s ", expectedHeatName, heatName ));
		assertTrue(expectedModuleName.equals(moduleName), String.format("Module name Expected: %s, Actual: %s ", expectedModuleName, moduleName));		
	}
	
	public static void validateEditPopoverButtons(String newName, String invalidModuleName, String validModueName ){
		DeploymentPage.updateAndCancel(newName, DataTestIdEnum.DeploymentScreen.X_BUTTON);
		verifyComponentNameChanged(invalidModuleName, validModueName);
		DeploymentPage.clickOnEditIcon();
		DeploymentPage.updateAndCancel(newName, DataTestIdEnum.DeploymentScreen.CANCEL);
		verifyComponentNameChanged(invalidModuleName, validModueName);
	}
	
	public static void validateEditPopover() throws Exception{
		String moduleRowText = GeneralUIUtils.getElementsByCSS(DataTestIdEnum.DeploymentScreen.MODULES.getValue()).get(0).getText();
		DeploymentPage.clickOnModuleName(moduleRowText);
		DeploymentPage.clickOnEditIcon();
		String[] splitedModuleName = moduleRowText.split("\\.\\.");
		
		validateEditPopoverFields(splitedModuleName[0], splitedModuleName[1], splitedModuleName[2]);
		
		String newName = "kuku";
		String newModuleName = DeploymentPage.reconstructModuleName(splitedModuleName, newName);
		validateEditPopoverButtons(newName, newModuleName, moduleRowText);				
	}

	private  Map<String, HashMap<String, List<String>>> getDeploymentViewDataFromFile() {
		return deploymentViewDataFromFile;
	}

	public static void validateModuleNameUpadate() throws Exception{
		List<WebElement> moduleRowsFromTable = GeneralUIUtils.getElementsByCSS(DataTestIdEnum.DeploymentScreen.MODULES.getValue());
		int i = 0;
		for(WebElement moduleRow :moduleRowsFromTable){
			String moduleRowText = moduleRow.getText();
			String updatedName = "updatedName" + i;
			DeploymentPage.updateModuleName(moduleRowText, updatedName);
			String updatedModuleName = DeploymentPage.reconstructModuleName(moduleRowText.split("\\.\\."), updatedName);
			verifyComponentNameChanged(moduleRowText, updatedModuleName);
			// Close module
			GeneralUIUtils.clickOnElementByText(updatedModuleName);
			i++;
		}		
	}

	public static void regularDepoymentScreenVerificator(Map<String, HashMap<String, String>> metaDataFromUI, DeploymentViewVerificator  verificator) throws Exception, InterruptedException {
		ResourceGeneralPage.getLeftMenu().moveToDeploymentViewScreen();
		List<WebElement> moduleRowsFromTable = GeneralUIUtils.getElementsByCSS(DataTestIdEnum.DeploymentScreen.MODULES.getValue());
		verificator.verifyDeploymentPageModules(moduleRowsFromTable);
		for(WebElement moduleRow :moduleRowsFromTable){
			String moduleRowText = moduleRow.getText();
			String middleName = moduleRowText.split("\\.\\.")[1];
			verificator.verifyDeploymentPageSubElements(middleName);
			if (metaDataFromUI != null){
				SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating %s group version, should be %s ", moduleRowText, metaDataFromUI.get(moduleRowText.split("\\.\\.")[1])));
				String groupVersion = DeploymentPage.getGroupVersion().split(":")[1].trim();
				String increasedVersion = String.valueOf(Integer.parseInt(metaDataFromUI.get(middleName).get("version")) + 1);
				assertTrue(groupVersion.equals(increasedVersion));
				if ( metaDataFromUI.get(middleName).get("moduleID") != "primary"){
					String moduleID = DeploymentPage.getModuleID();
					assertFalse(moduleID.equals(metaDataFromUI.get(middleName).get("moduleID")));
				}				
			}
			// Close module
			GeneralUIUtils.clickOnElementByText(moduleRowText);
		}
	}
	
	public static String convertAmdocsCsarGroupNameToSdcCsarGroupName(String originalString){
		return 	originalString.replace(partToReplace, "");	
	}
	
	
	

}
