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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.enums.ToscaKeysEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaNodeTemplatesTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaNodeTypesDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaPropertiesNodeTemplatesDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaRequirementsNodeTemplatesDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.CsarValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class ToscaParserUtils {

	private static Logger log = LoggerFactory.getLogger(ToscaParserUtils.class.getName());

	public static Map<?, ?> getToscaYamlMap(String csarUUID, String fileLocation) throws Exception {
		String csarPayload = CsarValidationUtils.getCsarPayload(csarUUID, fileLocation);
		if (csarPayload != null) {
			Yaml yaml = new Yaml();
			Map<?, ?> map = (Map<?, ?>) yaml.load(csarPayload);
			return map;
		}
		return null;
	}

	public static Map<String, Object> downloadAndParseToscaTemplate(User sdncModifierDetails, Component createdComponent) throws Exception {
		String artifactUniqeId = createdComponent.getToscaArtifacts().get("assettoscatemplate").getUniqueId();
		RestResponse toscaTemplate;

		if (createdComponent.getComponentType() == ComponentTypeEnum.RESOURCE) {
			toscaTemplate = ArtifactRestUtils.downloadResourceArtifactInternalApi(createdComponent.getUniqueId(), sdncModifierDetails, artifactUniqeId);

		} else {
			toscaTemplate = ArtifactRestUtils.downloadServiceArtifactInternalApi(createdComponent.getUniqueId(), sdncModifierDetails, artifactUniqeId);
		}
		BaseRestUtils.checkSuccess(toscaTemplate);

		ArtifactUiDownloadData artifactUiDownloadData = ResponseParser.parseToObject(toscaTemplate.getResponse(), ArtifactUiDownloadData.class);
		byte[] fromUiDownload = artifactUiDownloadData.getBase64Contents().getBytes();
		byte[] decodeBase64 = Base64.decodeBase64(fromUiDownload);
		Yaml yaml = new Yaml();

		InputStream inputStream = new ByteArrayInputStream(decodeBase64);

		Map<String, Object> load = (Map<String, Object>) yaml.load(inputStream);
		return load;
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
						enrichToscaDefinitionWithToscaVersion(toscaMap, toscaDefinition);
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

	public static void enrichToscaDefinitionWithToscaVersion(Map<?, ?> toscaMap, ToscaDefinition toscaDefinition) {
		if (toscaMap.get("tosca_definitions_version") != null) {
			toscaDefinition.setToscaDefinitionVersion(getToscaVersion(toscaMap));
		}
	}

	public static String getToscaVersion(Map<?, ?> toscaMap) {
		return (String) toscaMap.get("tosca_definitions_version");
	}

	// spec 90 page
	public static void getToscaNodeTypes(Map<?, ?> toscaMap, ToscaDefinition toscaDefinition) {
		@SuppressWarnings("unchecked")
		Map<String, Map<String, String>> nodeTypes = (Map<String, Map<String, String>>) toscaMap.get("node_types");
		List<ToscaNodeTypesDefinition> listToscaNodeTypes = new ArrayList<>();
		if (nodeTypes != null) {
			for (Entry<String, Map<String, String>> entry : nodeTypes.entrySet()) {
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
								toscaNodeTypes.setDerivedFrom(derivedFrom);
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
				listToscaNodeTypes.add(toscaNodeTypes);
			}
			toscaDefinition.setToscaNodeTypes(listToscaNodeTypes);
		}
	}

	public static void getToscaTopologyTemplate(Map<?, ?> toscaMap, ToscaDefinition toscaDefinition) {
		ToscaTopologyTemplateDefinition toscaTopologyTemplate = new ToscaTopologyTemplateDefinition();
		@SuppressWarnings("unchecked")
		Map<String, Map<String, Object>> topologyTemplateMap = (Map<String, Map<String, Object>>) toscaMap
				.get("topology_template");
		List<ToscaNodeTemplatesTopologyTemplateDefinition> listToscaNodeTemplates = new ArrayList<>();

		if (topologyTemplateMap != null) {
			getToscaNodeTemplates(topologyTemplateMap, listToscaNodeTemplates);
		}
		toscaTopologyTemplate.setToscaNodeTemplatesTopologyTemplateDefinition(listToscaNodeTemplates);
		toscaDefinition.setToscaTopologyTemplate(toscaTopologyTemplate);
	}

	public static void getToscaNodeTemplates(Map<String, Map<String, Object>> topologyTemplateMap,
			List<ToscaNodeTemplatesTopologyTemplateDefinition> listToscaNodeTemplates) {
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
				listToscaNodeTemplates.add(toscaNodeTemplates);
			}
		}
	}

	public static void getToscaNodeTemplateRequirements(ToscaNodeTemplatesTopologyTemplateDefinition toscaNodeTemplates,
			Map<String, Object> node) {
		List<ToscaRequirementsNodeTemplatesDefinition> toscaRequirements = new ArrayList<>();
		if (node.get("requirements") != null) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> requirementList = (List<Map<String, Object>>) node.get("requirements");
			for (int i = 0; i < requirementList.size(); i++) {
				for (Entry<String, Object> requirement : requirementList.get(i).entrySet()) {
					ToscaRequirementsNodeTemplatesDefinition toscaRequirement = new ToscaRequirementsNodeTemplatesDefinition();
					if (requirement.getKey() != null) {
						String requirementName = requirement.getKey();
						toscaRequirement.setName(requirementName);
					} else {
						log.debug("Tosca file not valid, requirements should contain name");
					}

					@SuppressWarnings("unchecked")
					Map<String, String> requirementMap = (Map<String, String>) requirement.getValue();
					Set<Entry<String, String>> entrySet = requirementMap.entrySet();
					if (entrySet != null) {
						for (Entry<String, String> requirementField : entrySet) {
							String key = requirementField.getKey();
							switch (key) {
							case "capability":
								if (requirementMap.get(key) != null) {
									String capability = (String) requirementMap.get(key);
									toscaRequirement.setCapability(capability);
									break;
								} else {
									continue;
								}
							case "node":
								if (requirementMap.get(key) != null) {
									String requirementNode = (String) requirementMap.get(key);
									toscaRequirement.setNode(requirementNode);
									break;
								} else {
									continue;
								}
							case "relationship":
								if (requirementMap.get(key) != null) {
									String relationship = (String) requirementMap.get(key);
									toscaRequirement.setRelationship(relationship);
									break;
								} else {
									continue;
								}
							default:
								break;
							}
						}
					}
					toscaRequirements.add(toscaRequirement);
				}
			}
		}
		toscaNodeTemplates.setRequirements(toscaRequirements);
	}

	public static void getToscaNodeTemplateProperties(ToscaNodeTemplatesTopologyTemplateDefinition toscaNodeTemplates,
			Map<String, Object> node) {
		List<ToscaPropertiesNodeTemplatesDefinition> listToscaProperties = new ArrayList<>();
		if (node.get("properties") != null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> properties = (Map<String, Object>) node.get("properties");
			for (Entry<String, Object> property : properties.entrySet()) {
				ToscaPropertiesNodeTemplatesDefinition toscaProperty = new ToscaPropertiesNodeTemplatesDefinition();
				String propertyName = property.getKey();
				Object propertyValue = property.getValue();
				toscaProperty.setName(propertyName);
				toscaProperty.setValue(propertyValue);
				listToscaProperties.add(toscaProperty);
			}
		}
		toscaNodeTemplates.setProperties(listToscaProperties);
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
	}
}
