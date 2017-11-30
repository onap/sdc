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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utils.ToscaParserUtils;
import org.testng.Assert;






public class Testing {

//	public static void main(String[] args) throws Exception {
//		// TODO Auto-generated method stub
//
//		File path = new File("C:\\Users\\rp955r\\Desktop\\US\\US831517\\TCExport\\TC1459238.yml");
//		ToscaDefinition toscaDefinition = ToscaParserUtils.parseToscaYamlToJavaObject(path);
//		
//		Map<String, Object> vl_us831517_1 = new HashMap<String, Object>();
//		vl_us831517_1.put("property_1", true);
//		vl_us831517_1.put("property_2", "init_value_2");
//		vl_us831517_1.put("property_3", "init_value_3");
//		
//		
//		Map<String, Object> vl_us831517_2 = new HashMap<String, Object>();
//		vl_us831517_2.put("property_1", false);
//		vl_us831517_2.put("property_2", "init_value_2");
//		vl_us831517_2.put("property_3", "new_value_3");
//		
//		Map<String, Object> vl_us831517_3 = new HashMap<String, Object>();
//		vl_us831517_3.put("property_1", true);
//		vl_us831517_3.put("property_2", "init_value_2");
//		vl_us831517_3.put("property_3", "init_value_3");
//		vl_us831517_3.put("property_4", false);
//		vl_us831517_3.put("property_5", "init_value_5");
//		
//		Map<String, Map<String, Object>> predefinedProperties = new HashMap<String, Map<String, Object>>();
//		predefinedProperties.put("VL_US831517_1", vl_us831517_1);
//		predefinedProperties.put("VL_US831517_2", vl_us831517_2);
//		predefinedProperties.put("VL_US831517_3", vl_us831517_3);
//		
//		validateNodeTemplatesProperties(predefinedProperties, toscaDefinition);
//		
//		
//		
//	}
	
	
	
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) throws Exception {
		ToscaDefinition toscaMainAmdocsDefinition, toscaMainVfDefinition, toscaMainServiceDefinition;
		File filesFolder = new File("C:/Users/al714h/Downloads/Design/");
		File importToscaFilesFolder = new File("C:/Git_work/sdc/catalog-be/src/main/resources/import/tosca/");
	
		File dataTypesLocation = new File(importToscaFilesFolder.getPath() + "/data-types/dataTypes.yml");
	
		File genericVfFileLocation = new File(importToscaFilesFolder.getPath() + "/heat-types/Generic_VF/Generic_VF.yml");
		File genericVfcFileLocation = new File (importToscaFilesFolder.getPath() + "/heat-types/Generic_VFC/Generic_VFC.yml");
		File genericPnfFileLocation = new File (importToscaFilesFolder.getPath() + "/heat-types/Generic_PNF/Generic_PNF.yml");
		File genericServiceFileLocation = new File (importToscaFilesFolder.getPath() + "/heat-types/Generic_Service/Generic_Service.yml");
	
		File amdocsCsarFileName = new File("/77e6b842669f441db20a83489da3f4be.csar");
		File VfCsarFileName = new File("/resource-Civfonboarded2016012VmxAv301072E2e1424cb9d-csar.csar");
		File serviceCsarFileName = new File("/service-Ciservicefeba0521131d-csar.csar");
		
		Map<String, DataTypeDefinition> parseDataTypesYaml = FileHandling.parseDataTypesYaml(dataTypesLocation.getAbsoluteFile().toString());
		System.out.println("start " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		
		toscaMainAmdocsDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + amdocsCsarFileName));
		toscaMainVfDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + VfCsarFileName));
		toscaMainServiceDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + serviceCsarFileName));
		ToscaTopologyTemplateDefinition topologyTemplate = toscaMainAmdocsDefinition.getTopology_template();
		ToscaDefinition objectHelper = ToscaParserUtils.parseToscaYamlToJavaObject(genericVfFileLocation);
		
		Map<String, Object> additionalInputs = new HashMap<>();
		Set<String> keySet = objectHelper.getNode_types().keySet();
		if(keySet != null){
			for(String key: keySet){
				additionalInputs = objectHelper.getNode_types().get(key).getProperties();
			}
		}
//		topologyTemplate.addInputs(additionalInputs);

		
		
		
//        toscaDefinition = ToscaParserUtils.parseToscaYamlToJavaObject(path);
        System.out.println("listTypeHeatMetaDefinition start " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
//        List<TypeHeatMetaDefinition> listTypeHeatMetaDefinition = CsarParserUtils.getListTypeHeatMetaDefinition(csarPath);
        System.out.println("get service start " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
//        System.out.println(listTypeHeatMetaDefinition);
//        for(TypeHeatMetaDefinition typeHeatMetaDefinition : listTypeHeatMetaDefinition){
//        	for(GroupHeatMetaDefinition groupHeatMetaDefinition : typeHeatMetaDefinition.getGroupHeatMetaDefinition()){
//        		List<HeatMetaFirstLevelDefinition> artifactList = groupHeatMetaDefinition.getArtifactList();
//        		boolean isBase = groupHeatMetaDefinition.getPropertyHeatMetaDefinition().getValue();
//        	}
//        	
//        }
        System.out.println("Finished");
        System.out.println("get service start " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        
        
    
	}

	
	

}
