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

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.ToscaArtifactsScreenEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.PropertiesUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.ToscaParserUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class extendNode_TemplatePropertiesWithDefaultValues extends SetupCDTest {

	@DataProvider(name="customizeServiceVfUsedVlsCps") 
	public static Object[][] dataProviderCustomizeServiceVfUsedVlsCps() {
		return new Object[][] {
//			{"VL_US831517_1.yml", "VL_US831517_2.yml", "VL_US831517_3.yml", ResourceTypeEnum.VL, ComponentTypeEnum.SERVICE},
			{"CP_US831517_1.yml", "CP_US831517_2.yml", "CP_US831517_3.yml", ResourceTypeEnum.CP, ComponentTypeEnum.SERVICE},
			{"VL_US831517_1.yml", "VL_US831517_2.yml", "VL_US831517_3.yml", ResourceTypeEnum.VL, ComponentTypeEnum.RESOURCE},
			{"CP_US831517_1.yml", "CP_US831517_2.yml", "CP_US831517_3.yml", ResourceTypeEnum.CP, ComponentTypeEnum.RESOURCE},
			};
	}
	
	// US831517 - Story [BE] - Extend node_template properties with default values
	@Test(dataProvider="customizeServiceVfUsedVlsCps")
	public void customizeServiceVfUsedVlsCps(String fileName_vl1, String fileName_vl2, String fileName_vl3, ResourceTypeEnum resourceTypeEnum, ComponentTypeEnum componentTypeEnum) throws Exception {
		setLog("Extend node_template properties with default values");

		// import Resource
		LinkedList<String> assetsName = importThreeAsset(fileName_vl1, fileName_vl2, fileName_vl3, resourceTypeEnum);
		
		if(ComponentTypeEnum.SERVICE == componentTypeEnum) {
			ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
			ServiceUIUtils.createService(serviceMetadata, getUser());
		} else {
			ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
			ResourceUIUtils.createVF(resourceMetaData, getUser());
		}
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();		
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement resourceInstance1 = canvasManager.createElementOnCanvas(assetsName.get(0));
		CanvasElement resourceInstance2 = canvasManager.createElementOnCanvas(assetsName.get(1));
		CanvasElement resourceInstance3 = canvasManager.createElementOnCanvas(assetsName.get(2));
		
		canvasManager.clickOnCanvaElement(resourceInstance1);
		CompositionPage.showPropertiesAndAttributesTab();
		PropertiesUIUtils.changePropertyDefaultValueInComposition("property_1", "false");
		
		canvasManager.clickOnCanvaElement(resourceInstance2);
		CompositionPage.showPropertiesAndAttributesTab();
		PropertiesUIUtils.changePropertyDefaultValueInComposition("property_3", "customize");
		
		canvasManager.clickOnCanvaElement(resourceInstance3);
		CompositionPage.showPropertiesAndAttributesTab();
		PropertiesUIUtils.changePropertyDefaultValueInComposition("property_2", "customize derived");
		PropertiesUIUtils.changePropertyDefaultValueInComposition("property_5", "customize new");
		
		GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");
		ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
		GeneralUIUtils.clickOnElementByTestId(ToscaArtifactsScreenEnum.TOSCA_TEMPLATE.getValue());
		
		// TODO: Replace it by automatic download to path
		// TODO: After it remove
		File path = new File("C:\\Users\\rp955r\\Desktop\\US\\US831517\\TCExport\\TC1459238.yml");
		ToscaDefinition toscaDefinition = ToscaParserUtils.parseToscaYamlToJavaObject(path);
		
		Map<String, Object> vl_us831517_1 = new HashMap<String, Object>();
		vl_us831517_1.put("property_1", false);
		vl_us831517_1.put("property_2", "init_value_2");
		vl_us831517_1.put("property_3", "init_value_3");
		
		
		Map<String, Object> vl_us831517_2 = new HashMap<String, Object>();
		vl_us831517_2.put("property_1", false);
		vl_us831517_2.put("property_2", "init_value_2");
		vl_us831517_2.put("property_3", "customize");
		
		Map<String, Object> vl_us831517_3 = new HashMap<String, Object>();
		vl_us831517_3.put("property_1", true);
		vl_us831517_3.put("property_2", "customize derived");
		vl_us831517_3.put("property_3", "init_value_3");
		vl_us831517_3.put("property_4", false);
		vl_us831517_3.put("property_5", "customize new");
		
		Map<String, Map<String, Object>> predefinedProperties = new HashMap<String, Map<String, Object>>();
		predefinedProperties.put("VL_US831517_1", vl_us831517_1);
		predefinedProperties.put("VL_US831517_2", vl_us831517_2);
		predefinedProperties.put("VL_US831517_3", vl_us831517_3);
		
		validateNodeTemplatesProperties(predefinedProperties, toscaDefinition);
		
		RestCDUtils.deleteOnDemand();
	}
	
	@DataProvider(name="serviceVfUsedVlsCps") 
	public static Object[][] dataProviderServiceVfUsedVlsCps() {
		return new Object[][] {
			{"VL_US831517_1.yml", "VL_US831517_2.yml", "VL_US831517_3.yml", ResourceTypeEnum.VL, ComponentTypeEnum.SERVICE},
			{"CP_US831517_1.yml", "CP_US831517_2.yml", "CP_US831517_3.yml", ResourceTypeEnum.CP, ComponentTypeEnum.SERVICE},
			{"VL_US831517_1.yml", "VL_US831517_2.yml", "VL_US831517_3.yml", ResourceTypeEnum.VL, ComponentTypeEnum.RESOURCE},
			{"CP_US831517_1.yml", "CP_US831517_2.yml", "CP_US831517_3.yml", ResourceTypeEnum.CP, ComponentTypeEnum.RESOURCE},
			};
	}
	
	
	// US831517 - Story [BE] - Extend node_template properties with default values
	@Test(dataProvider="serviceVfUsedVlsCps")
	public void serviceVfUsedVlsCps(String fileName_vl1, String fileName_vl2, String fileName_vl3, ResourceTypeEnum resourceTypeEnum, ComponentTypeEnum componentTypeEnum) throws Exception {
		setLog("Extend node_template properties with default values");

		// import Resource
		LinkedList<String> assetsName = importThreeAsset(fileName_vl1, fileName_vl2, fileName_vl3, resourceTypeEnum);
		
		if(ComponentTypeEnum.SERVICE == componentTypeEnum) {
			ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
			ServiceUIUtils.createService(serviceMetadata, getUser());
		} else {
			ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
			ResourceUIUtils.createVF(resourceMetaData, getUser());
		}
		
		ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();		
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		canvasManager.createElementOnCanvas(assetsName.get(0));
		canvasManager.createElementOnCanvas(assetsName.get(1));
		canvasManager.createElementOnCanvas(assetsName.get(2));
		
		GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");
		ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
		GeneralUIUtils.clickOnElementByTestId(ToscaArtifactsScreenEnum.TOSCA_TEMPLATE.getValue());
		
		// TODO: Replace it by automatic download to path
		// TODO: After it remove
		File path = new File("C:\\Users\\rp955r\\Desktop\\US\\US831517\\TCExport\\TC1459238.yml");
		ToscaDefinition toscaDefinition = ToscaParserUtils.parseToscaYamlToJavaObject(path);
		
		Map<String, Object> vl_us831517_1 = new HashMap<String, Object>();
		vl_us831517_1.put("property_1", true);
		vl_us831517_1.put("property_2", "init_value_2");
		vl_us831517_1.put("property_3", "init_value_3");
		
		
		Map<String, Object> vl_us831517_2 = new HashMap<String, Object>();
		vl_us831517_2.put("property_1", false);
		vl_us831517_2.put("property_2", "init_value_2");
		vl_us831517_2.put("property_3", "new_value_3");
		
		Map<String, Object> vl_us831517_3 = new HashMap<String, Object>();
		vl_us831517_3.put("property_1", true);
		vl_us831517_3.put("property_2", "init_value_2");
		vl_us831517_3.put("property_3", "init_value_3");
		vl_us831517_3.put("property_4", false);
		vl_us831517_3.put("property_5", "init_value_5");
		
		Map<String, Map<String, Object>> predefinedProperties = new HashMap<String, Map<String, Object>>();
		predefinedProperties.put("VL_US831517_1", vl_us831517_1);
		predefinedProperties.put("VL_US831517_2", vl_us831517_2);
		predefinedProperties.put("VL_US831517_3", vl_us831517_3);
		
		validateNodeTemplatesProperties(predefinedProperties, toscaDefinition);
		
		RestCDUtils.deleteOnDemand();
	}
	
	private LinkedList<String> importThreeAsset(String fileName_vl1, String fileName_vl2, String fileName_vl3, ResourceTypeEnum resourceTypeEnum) throws Exception {
		LinkedList<String> assetsNames = new LinkedList<String>();
		
		String filePath = System.getProperty("filepath");
		if (filePath == null && System.getProperty("os.name").contains("Windows")) {
			filePath = FileHandling.getResourcesFilesPath() + File.separator + "US831517" + File.separator;
		}
		else if(filePath.isEmpty() && !System.getProperty("os.name").contains("Windows")){
			filePath = FileHandling.getBasePath() + File.separator + "Files" + File.separator + "US831517" + File.separator;
		}
		
		// import Resource
		ResourceReqDetails resourceMetaDataVl1 = ElementFactory.getDefaultResourceByType(resourceTypeEnum, getUser());
		assetsNames.add(resourceMetaDataVl1.getName());
		ResourceUIUtils.importVfc(resourceMetaDataVl1, filePath, fileName_vl1, getUser());
		GeneralPageElements.clickSubmitForTestingButton(resourceMetaDataVl1.getName());
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(resourceMetaDataVl1.getName());
		TesterOperationPage.certifyComponent(resourceMetaDataVl1.getName());
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		
		ResourceReqDetails resourceMetaDataVl2 = ElementFactory.getDefaultResourceByType(resourceTypeEnum, getUser());
		assetsNames.add(resourceMetaDataVl2.getName());
		ResourceUIUtils.importVfc(resourceMetaDataVl2, filePath, fileName_vl2, getUser());
		GeneralPageElements.clickCheckinButton(resourceMetaDataVl2.getName());
		
		ResourceReqDetails resourceMetaDataVl3 = ElementFactory.getDefaultResourceByType(resourceTypeEnum, getUser());
		assetsNames.add(resourceMetaDataVl3.getName());
		ResourceUIUtils.importVfc(resourceMetaDataVl3, filePath, fileName_vl3, getUser());
		GeneralPageElements.clickCheckinButton(resourceMetaDataVl2.getName());
		
		return assetsNames;
	}
	
	
	private static void validateNodeTemplatesProperties(Map<String, Map<String, Object>> predefinedMap, ToscaDefinition toscaDefinition) {
		
		for(String key: predefinedMap.keySet()) {
			Map<String, Object> nodeTemplateProperties = getNodeTemplatePropertiesByNodeTemplateType(key, toscaDefinition);
			
			predefinedMap.get(key).forEach((i,j) -> {
				Assert.assertEquals(nodeTemplateProperties.get(i), j, "Expected that the properties will be equal");
			});
		}

	}
	
	// Get properties by type
	private static Map<String, Object> getNodeTemplatePropertiesByNodeTemplateType(String nodeTemplateType, ToscaDefinition toscaDefinition) {
		Map<String, Object> propertiesMap = null;
		
		Set<String> nodeTemplates = getNodeTemplates(toscaDefinition);
		
		for(String nodeTemplate: nodeTemplates) {
			String currentNodeTemplateType = getNodeTemplateType(toscaDefinition, nodeTemplate);
			currentNodeTemplateType = currentNodeTemplateType.substring(currentNodeTemplateType.lastIndexOf(".") + 1);
			if(currentNodeTemplateType.equals(nodeTemplateType)) {
				propertiesMap = getNodeTemplateProperties(toscaDefinition, nodeTemplate);
				break;
			}
		}
		
		return propertiesMap;
	}
	
	// Get node templates
	private static Set<String> getNodeTemplates(ToscaDefinition toscaDefinition) {
		Set<String> resourceInstanceArray = toscaDefinition.getTopology_template().getNode_templates().keySet();
		return resourceInstanceArray;
	}
	
	// Get type of node template
	private static String getNodeTemplateType(ToscaDefinition toscaDefinition, String nodeTemplate) {
		return toscaDefinition.getTopology_template().getNode_templates().get(nodeTemplate).getType();
	}
		
	// Get properties of node template
	private static Map<String, Object> getNodeTemplateProperties(ToscaDefinition toscaDefinition, String nodeTemplate) {
		Map<String, Object> propertiesMap = toscaDefinition.getTopology_template().getNode_templates().get(nodeTemplate).getProperties();
		return propertiesMap;
	}
	
	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
