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

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Set;

import org.openecomp.sdc.ci.tests.datatypes.enums.ToscaKeysEnum;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaGroupsTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaNodeTemplatesTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaSubstitutionMappingsDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.utils.validation.CsarValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public class ToscaParserUtils {

	private static Logger log = LoggerFactory.getLogger(ToscaParserUtils.class.getName());

	public static ToscaDefinition parseToscaYamlToJavaObject(String csarUUID) throws Exception {
		
		ToscaDefinition toscaDefinition = null;
		String TOSCAMetaLocation = "TOSCA-Metadata/TOSCA.meta";
		Map<?, ?> map = getToscaYamlMap(csarUUID, TOSCAMetaLocation);
		assertNotNull("Tosca Entry-Definitions is null", map);
		if (map != null) {
			File definitionYamlLocation = (File) map.get("Entry-Definitions");
			toscaDefinition = parseToscaYamlToJavaObject(definitionYamlLocation);
		}
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
        
        Constructor constructor = getConstructor();
    	
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
        Constructor constructor = getConstructor();
    	
        Yaml yaml = new Yaml(constructor);
        try {
        	toscaDefinition = (ToscaDefinition) yaml.load(payload);
		} catch (Exception e) {
			log.debug("Failed to parse tosca yaml file");
			System.out.println("Exception: " + e);
		}
        return toscaDefinition;
        
	}
	
	
	public static Constructor getConstructor() {
		Constructor constructor = new Constructor(ToscaDefinition.class);
        constructor.addTypeDescription(ToscaDefinition.getTypeDescription());
        constructor.addTypeDescription(ToscaTopologyTemplateDefinition.getTypeDescription());
    	constructor.addTypeDescription(ToscaNodeTemplatesTopologyTemplateDefinition.getTypeDescription());
    	constructor.addTypeDescription(ToscaGroupsTopologyTemplateDefinition.getTypeDescription());
    	constructor.addTypeDescription(ToscaSubstitutionMappingsDefinition.getTypeDescription());
    	
//    	Skip properties which are found in YAML, but not found in POJO
    	PropertyUtils propertyUtils = new PropertyUtils();
    	propertyUtils.setSkipMissingProperties(true);
    	constructor.setPropertyUtils(propertyUtils);
		return constructor;
	}

	public static Map<?, ?> getToscaYamlMap(String csarUUID, String fileLocation) throws Exception {
		String csarPayload = CsarValidationUtils.getCsarPayload(csarUUID, fileLocation);
		if (csarPayload != null) {
			Yaml yaml = new Yaml();
			Map<?, ?> map = (Map<?, ?>) yaml.load(csarPayload);
			return map;
		}
		return null;
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
