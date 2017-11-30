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

package org.openecomp.sdc.ci.tests.utils;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaGroupsTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaImportsDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaInputsTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaMetadataDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaNodeTemplatesTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaParameterConstants;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaSubstitutionMappingsDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ImportRestUtils;
import org.openecomp.sdc.common.rest.api.RestResponseAsByteArray;
import org.openecomp.sdc.common.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public class ToscaParserUtils {

	private static Logger log = LoggerFactory.getLogger(ToscaParserUtils.class.getName());

	
	/**method get csarUUID and send GET API request toward BE 
	 * @param csarUUID
	 * @return
	 * @throws Exception
	 */
	public static ToscaDefinition parseToscaYamlToJavaObjectByCsarUuid(String csarUUID) throws Exception {
		
		ToscaDefinition toscaDefinition = null;
		String TOSCAMetaLocation = ToscaParameterConstants.TOSCA_META_PATH;
		Map<?, ?> map = getToscaYamlMap(csarUUID, TOSCAMetaLocation);
		assertNotNull("Tosca Entry-Definitions is null", map);
		if (map != null) {
			File definitionYamlLocation = (File) map.get(ToscaParameterConstants.ENTRY_DEFINITION);
			toscaDefinition = parseToscaYamlToJavaObject(definitionYamlLocation);
		}
		return toscaDefinition;

	}

	/**method read csar from location
	 * @param csarNameLocation - full path with csar name 
	 * @return
	 * @throws Exception
	 */
	public static ToscaDefinition parseToscaMainYamlToJavaObjectByCsarLocation(File csarNameLocation) throws Exception {
		
		ToscaDefinition toscaDefinition = null;
		String TOSCAMetaLocation = ToscaParameterConstants.TOSCA_META_PATH;
//		read file location of main yaml file(location+name) from TOSCA.meta file by 
		Map<?, ?> map = getToscaYamlMap(csarNameLocation, TOSCAMetaLocation);
		
		assertNotNull("Tosca Entry-Definitions is null", map);

		String definitionYamlLocation = (String) map.get(ToscaParameterConstants.ENTRY_DEFINITION);
		String csarPayload = getYamlPayloadFromCsar(csarNameLocation, definitionYamlLocation);
		toscaDefinition = parseToscaYamlPayloadToJavaObject(csarPayload);
		return toscaDefinition;

	}
	
	public static ToscaDefinition parseToscaAnyYamlToJavaObjectByCsarLocation(File csarNameLocation, String yamlLocation) throws Exception {
		
		ToscaDefinition toscaDefinition = null;
		String csarPayload = getYamlPayloadFromCsar(csarNameLocation, yamlLocation);
		toscaDefinition = parseToscaYamlPayloadToJavaObject(csarPayload);
		return toscaDefinition;

	}
	
	public static ToscaDefinition parseToscaYamlToJavaObject(File path) throws Exception {

		ToscaDefinition toscaDefinition = null;
		
//        File path = new File("C:/Data/D2.0/TOSCA_Ex/Definitions/tosca_definition_version.yaml");
        FileInputStream fis = null;
        try {
        	fis = new FileInputStream(path);
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
        
        Constructor constructor = initToscaDefinitionObject();
    	
        Yaml yaml = new Yaml(constructor);
        try {
        	toscaDefinition = (ToscaDefinition) yaml.load(fis);
		} catch (Exception e) {
			log.debug("Failed to parse tosca yaml file");
			System.out.println("Exception: " + e);
		} finally {
			fis.close();
		}
        return toscaDefinition;
        
	}

	public static ToscaDefinition parseToscaYamlPayloadToJavaObject(String payload){

		ToscaDefinition toscaDefinition = null;
        Constructor constructor = initToscaDefinitionObject();
    	
        Yaml yaml = new Yaml(constructor);
        try {
        	toscaDefinition = (ToscaDefinition) yaml.load(payload);
		} catch (Exception e) {
			log.debug("Failed to parse tosca yaml file");
			log.debug("Exception: " + e);
			System.out.println("Exception: " + e);
			assertTrue("Exception: " + e, false);
		}
        return toscaDefinition;
        
	}
	
	
	public static Constructor initToscaDefinitionObject() {
		Constructor toscaStructure = new Constructor(ToscaDefinition.class);
        toscaStructure.addTypeDescription(ToscaDefinition.getTypeDescription());
        toscaStructure.addTypeDescription(ToscaTopologyTemplateDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaNodeTemplatesTopologyTemplateDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaGroupsTopologyTemplateDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaSubstitutionMappingsDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaImportsDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaMetadataDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaInputsTopologyTemplateDefinition.getTypeDescription());
//    	toscaStructure.addTypeDescription(ToscaInputsDefinition.getTypeDescription());
//    	Skip properties which are found in YAML, but not found in POJO
    	PropertyUtils propertyUtils = new PropertyUtils();
    	propertyUtils.setSkipMissingProperties(true);
    	toscaStructure.setPropertyUtils(propertyUtils);
		return toscaStructure;
	}

	public static Map<?, ?> getToscaYamlMap(String csarUUID, String yamlFileLocation) throws Exception {
		String csarPayload = getCsarPayload(csarUUID, yamlFileLocation);
		if (csarPayload != null) {
			Yaml yaml = new Yaml();
			Map<?, ?> map = (Map<?, ?>) yaml.load(csarPayload);
			return map;
		}
		return null;
	}
	
	public static Map<?, ?> getToscaYamlMap(File csarPath, String yamlFileLocation) throws Exception {
		String csarPayload = getYamlPayloadFromCsar(csarPath, yamlFileLocation);
		if (csarPayload != null) {
			Yaml yaml = new Yaml();
			Map<?, ?> map = (Map<?, ?>) yaml.load(csarPayload);
			return map;
		}
		return null;
	}
	
	
	public static String getCsarPayload(String csarName, String yamlFileLocation) throws Exception {

		RestResponseAsByteArray csar = ImportRestUtils.getCsar(csarName, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		assertTrue("Return response code different from 200", csar.getHttpStatusCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		byte[] data = csar.getResponse();
		return getDataFromZipFileByBytes(yamlFileLocation, data);

	}

	public static String getYamlPayloadFromCsar(File csarName, String fileLocation) throws Exception {
		
		Path path = csarName.toPath();
		byte[] data = Files.readAllBytes(path);
		return getDataFromZipFileByBytes(fileLocation, data);
		
	}

	/** method get file data from zip data by file location in the zip structure 
	 * @param fileLocation
	 * @param data
	 * @return
	 */
	public static String getDataFromZipFileByBytes(String fileLocation, byte[] data) {
		Map<String, byte[]> readZip = null;
		if (data != null && data.length > 0) {
			readZip = ZipUtil.readZip(data);

		}
		byte[] artifactsBs = readZip.get(fileLocation);
		String str = new String(artifactsBs, StandardCharsets.UTF_8);
		return str;
	}
/*	public static Map<?, ?> getToscaYamlMap(String csarUUID, String fileLocation) throws Exception {
		String csarPayload = CsarValidationUtils.getCsarPayload(csarUUID, fileLocation);
		if (csarPayload != null) {
			Yaml yaml = new Yaml();
			Map<?, ?> map = (Map<?, ?>) yaml.load(csarPayload);
			return map;
		}
		return null;
	}

	public static ToscaDefinition getToscaDefinitionObjectByCsarUuid(String csarUUID) throws Exception {

		String TOSCAMetaLocation = "TOSCA-Metadata/TOSCA.meta";
		Map<?, ?> map = getToscaYamlMap(csarUUID, TOSCAMetaLocation);
		assertNotNull("Tosca Entry-Definitions is null", map);
		if (map != null) {
			String definitionYamlLocation = (String) map.get("Entry-Definitions");
			Map<?, ?> toscaMap = getToscaYamlMap(csarUUID, definitionYamlLocation);
			assertNotNull("Tosca definition is null", toscaMap);
			if (toscaMap != null) {
				ToscaDefinition toscaDefinition = new ToscaDefinition();
				Set<?> keySet = toscaMap.keySet();
				for (Object key : keySet) {
					ToscaKeysEnum toscaKey = ToscaKeysEnum.findToscaKey((String) key);
					switch (toscaKey) {
					case TOSCA_DEFINITION_VERSION:
						getToscaDefinitionVersion(toscaMap, toscaDefinition);
						break;
					case NODE_TYPES:
						getToscaNodeTypes(toscaMap, toscaDefinition);
						break;
					case TOPOLOGY_TEMPLATE:
						getToscaTopologyTemplate(toscaMap, toscaDefinition);
						break;
					case IMPORTS:
						// toscaMap.get("imports");
						break;
					default:
						break;
					}
				}
				return toscaDefinition;
			}
		}
		return null;

	}

	public static void getToscaDefinitionVersion(Map<?, ?> toscaMap, ToscaDefinition toscaDefinition) {
		if (toscaMap.get("tosca_definitions_version") != null) {
			toscaDefinition.setTosca_definitions_version((String) toscaMap.get("tosca_definitions_version"));
		}
	}

	// spec 90 page
	public static void getToscaNodeTypes(Map<?, ?> toscaMap, ToscaDefinition toscaDefinition) {
		@SuppressWarnings("unchecked")
		Map<String, Map<String, String>> nodeTypes = (Map<String, Map<String, String>>) toscaMap.get("node_types");
		Map<String, ToscaNodeTypesDefinition> listToscaNodeTypes = new HashMap<String, ToscaNodeTypesDefinition>();
		if (nodeTypes != null) {
			for (Map.Entry<String, Map<String, String>> entry : nodeTypes.entrySet()) {
				ToscaNodeTypesDefinition toscaNodeTypes = new ToscaNodeTypesDefinition();
				String toscaNodeName = entry.getKey();
				toscaNodeTypes.setName(toscaNodeName);

				Map<String, String> toscaNodeType = entry.getValue();
				if (toscaNodeType != null) {
					Set<Entry<String, String>> entrySet = toscaNodeType.entrySet();
					if (entrySet != null) {
						// boolean found = false;
						for (Entry<String, String> toscaNodeTypeMap : entrySet) {
							String key = toscaNodeTypeMap.getKey();
							if (key.equals("derived_from")) {
								String derivedFrom = toscaNodeTypeMap.getValue();
								toscaNodeTypes.setDerived_from(derivedFrom);
								// found = true;
								break;
							} else {
								continue;
							}

						}
						// if (found == false) {
						// System.out.println("Tosca file not valid,
						// derived_from not found");
						// }
					}

				}
//				listToscaNodeTypes.add(toscaNodeTypes);
				listToscaNodeTypes.put(toscaNodeName, toscaNodeTypes);
			}
			toscaDefinition.setNode_types(listToscaNodeTypes);
		}
	}

	public static void getToscaTopologyTemplate(Map<?, ?> toscaMap, ToscaDefinition toscaDefinition) {
		ToscaTopologyTemplateDefinition toscaTopologyTemplate = new ToscaTopologyTemplateDefinition();
		@SuppressWarnings("unchecked")
		Map<String, Map<String, Object>> topologyTemplateMap = (Map<String, Map<String, Object>>) toscaMap.get("topology_template");
//		List<ToscaNodeTemplatesTopologyTemplateDefinition> listToscaNodeTemplates = new ArrayList<>();
		Map<String,ToscaNodeTemplatesTopologyTemplateDefinition> mapToscaNodeTemplates = new HashMap<String, ToscaNodeTemplatesTopologyTemplateDefinition>();

		if (topologyTemplateMap != null) {
			getToscaNodeTemplates(topologyTemplateMap, mapToscaNodeTemplates);
		}
//		toscaTopologyTemplate.setToscaNodeTemplatesTopologyTemplateDefinition(listToscaNodeTemplates);
		toscaTopologyTemplate.setNode_templates(mapToscaNodeTemplates);
		toscaDefinition.setTopology_template(toscaTopologyTemplate);
	}

	public static void getToscaNodeTemplates(Map<String, Map<String, Object>> topologyTemplateMap, Map<String,ToscaNodeTemplatesTopologyTemplateDefinition> mapToscaNodeTemplates) {
		Map<String, Object> nodeTemplatesMap = topologyTemplateMap.get("node_templates");
		if (nodeTemplatesMap != null) {

			for (Entry<String, Object> nodeTemplates : nodeTemplatesMap.entrySet()) {
				ToscaNodeTemplatesTopologyTemplateDefinition toscaNodeTemplates = new ToscaNodeTemplatesTopologyTemplateDefinition();
				getToscaNodeTemplatesName(nodeTemplates, toscaNodeTemplates);

				@SuppressWarnings("unchecked")
				Map<String, Object> node = (Map<String, Object>) nodeTemplates.getValue();
				getNodeTemplatesType(toscaNodeTemplates, node);
				getToscaNodeTemplateProperties(toscaNodeTemplates, node);
				getToscaNodeTemplateRequirements(toscaNodeTemplates, node);
				mapToscaNodeTemplates.putAll(mapToscaNodeTemplates);
			}
		}
	}

	public static void getToscaNodeTemplateRequirements(ToscaNodeTemplatesTopologyTemplateDefinition toscaNodeTemplates, Map<String, Object> node) {
////		List<ToscaRequirementsNodeTemplatesDefinition> toscaRequirements = new ArrayList<>();
//		List<Map<String, ToscaRequirementsNodeTemplatesDefinition>> toscaRequirements = new ArrayList<>();
//		if (node.get("requirements") != null) {
//			@SuppressWarnings("unchecked")
//			List<Map<String, Object>> requirementList = (List<Map<String, Object>>) node.get("requirements");
//			for (int i = 0; i < requirementList.size(); i++) {
//				for (Map.Entry<String, Object> requirement : requirementList.get(i).entrySet()) {
//					ToscaRequirementsNodeTemplatesDefinition toscaRequirement = new ToscaRequirementsNodeTemplatesDefinition();
//					if (requirement.getKey() != null) {
//						String requirementName = requirement.getKey();
//						toscaRequirement.setName(requirementName);
//					} else {
//						log.debug("Tosca file not valid, requirements should contain name");
//					}
//
//					@SuppressWarnings("unchecked")
//					Map<String, String> requirementMap = (Map<String, String>) requirement.getValue();
//					Set<Entry<String, String>> entrySet = requirementMap.entrySet();
//					if (entrySet != null) {
//						for (Entry<String, String> requirementField : entrySet) {
//							String key = requirementField.getKey();
//							switch (key) {
//							case "capability":
//								if (requirementMap.get(key) != null) {
//									String capability = (String) requirementMap.get(key);
//									toscaRequirement.setCapability(capability);
//									break;
//								} else {
//									continue;
//								}
//							case "node":
//								if (requirementMap.get(key) != null) {
//									String requirementNode = (String) requirementMap.get(key);
//									toscaRequirement.setNode(requirementNode);
//									break;
//								} else {
//									continue;
//								}
//							case "relationship":
//								if (requirementMap.get(key) != null) {
//									String relationship = (String) requirementMap.get(key);
//									toscaRequirement.setRelationship(relationship);
//									break;
//								} else {
//									continue;
//								}
//							default:
//								break;
//							}
//						}
//					}
////					toscaRequirements.add(toscaRequirement);
//					toscaRequirements.add(requirementMap);
//				}
//			}
//		}
////		toscaNodeTemplates.setRequirements(toscaRequirements);
//		toscaNodeTemplates.setRequirements(requirements);
		
	}

	public static void getToscaNodeTemplateProperties(ToscaNodeTemplatesTopologyTemplateDefinition toscaNodeTemplates,
			Map<String, Object> node) {
//		List<ToscaPropertiesNodeTemplatesDefinition> listToscaProperties = new ArrayList<>();
		Map<String, Object> mapToscaProperties = new HashMap<>();
		if (node.get("properties") != null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> properties = (Map<String, Object>) node.get("properties");
			for (Map.Entry<String, Object> property : properties.entrySet()) {
				ToscaPropertiesNodeTemplatesDefinition toscaProperty = new ToscaPropertiesNodeTemplatesDefinition();
				String propertyName = property.getKey();
				Object propertyValue = property.getValue();
				toscaProperty.setName(propertyName);
				toscaProperty.setValue(propertyValue);
//				mapToscaProperties.add(toscaProperty);
				mapToscaProperties.put(propertyName, propertyValue);
			}
		}
		toscaNodeTemplates.setProperties(mapToscaProperties);
	}

	protected static void getNodeTemplatesType(ToscaNodeTemplatesTopologyTemplateDefinition toscaNodeTemplates,
			Map<String, Object> node) {
		if (node.get("type") != null) {
			String type = (String) node.get("type");
			toscaNodeTemplates.setType(type);
		} else {
			log.debug("Tosca file not valid, nodeTemplate should contain type");
		}
	}

	protected static void getToscaNodeTemplatesName(Entry<String, Object> nodeTemplates,
			ToscaNodeTemplatesTopologyTemplateDefinition toscaNodeTemplates) {
		String name = nodeTemplates.getKey();
		toscaNodeTemplates.setName(name);
	}*/
	
	
	
}
