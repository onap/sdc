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

package org.openecomp.sdc.be.tosca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.tosca.model.SubstitutionMapping;
import org.openecomp.sdc.be.tosca.model.ToscaCapability;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaRequirement;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateCapability;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import fj.data.Either;

public class CapabiltyRequirementConvertor {
	private static CapabiltyRequirementConvertor instance;
	public final static String PATH_DELIMITER = ".";
	
	protected CapabiltyRequirementConvertor() {

	}

	public static synchronized CapabiltyRequirementConvertor getInstance() {
		if (instance == null) {
			instance = new CapabiltyRequirementConvertor();
		}
		return instance;
	}

	private static Logger log = LoggerFactory.getLogger(CapabiltyRequirementConvertor.class.getName());

	public Either<ToscaNodeTemplate, ToscaError> convertComponentInstanceCapabilties(ComponentInstance componentInstance, Map<String, DataTypeDefinition> dataTypes, ToscaNodeTemplate nodeTemplate) {

		Map<String, List<CapabilityDefinition>> capabilitiesInst = componentInstance.getCapabilities();
		if (capabilitiesInst != null && !capabilitiesInst.isEmpty()) {
			Map<String, ToscaTemplateCapability> capabilties = new HashMap<>();
			capabilitiesInst.entrySet().forEach(e -> {
				List<CapabilityDefinition> capList = e.getValue();
				if (capList != null && !capList.isEmpty()) {
					capList.forEach(c -> {
						convertOverridenProperties(componentInstance, dataTypes, capabilties, c);
					});
				}
			});
			if (capabilties != null && !capabilties.isEmpty()) {
				nodeTemplate.setCapabilities(capabilties);
			}
		}
		return Either.left(nodeTemplate);
	}

	private void convertOverridenProperties(ComponentInstance componentInstance, Map<String, DataTypeDefinition> dataTypes, Map<String, ToscaTemplateCapability> capabilties, CapabilityDefinition c) {
		List<ComponentInstanceProperty> properties = c.getProperties();
		if (properties != null && !properties.isEmpty()) {
			properties.stream().filter(p -> (p.getValueUniqueUid() != null)).forEach(p -> {
				convertOverridenProperty(componentInstance, dataTypes, capabilties, c, p);
			});
		}
	}

	private void convertOverridenProperty(ComponentInstance componentInstance, Map<String, DataTypeDefinition> dataTypes, Map<String, ToscaTemplateCapability> capabilties, CapabilityDefinition c, ComponentInstanceProperty p) {
		if (log.isDebugEnabled()) {
			log.debug("Exist overriden property {} for capabity {} with value {}", p.getName(), c.getName(), p.getValue());
		}
		ToscaTemplateCapability toscaTemplateCapability = capabilties.get(c.getName());
		if (toscaTemplateCapability == null) {
			toscaTemplateCapability = new ToscaTemplateCapability();
			capabilties.put(c.getName(), toscaTemplateCapability);
		}
		Map<String, Object> toscaCapProp = toscaTemplateCapability.getProperties();
		if (toscaCapProp == null) {
			toscaCapProp = new HashMap<>();
		}
		Object convertedValue = convertInstanceProperty(dataTypes, componentInstance, p);
		toscaCapProp.put(p.getName(), convertedValue);
		toscaTemplateCapability.setProperties(toscaCapProp);
	}

	private Object convertInstanceProperty(Map<String, DataTypeDefinition> dataTypes, ComponentInstance componentInstance, ComponentInstanceProperty prop) {
		log.debug("Convert property {} for instance {}", prop.getName(), componentInstance.getUniqueId());
		String propertyType = prop.getType();
		String innerType = null;
		if (prop.getSchema() != null && prop.getSchema().getProperty() != null) {
			innerType = prop.getSchema().getProperty().getType();
		}
		Object convertedValue = PropertyConvertor.getInstance().convertToToscaObject(propertyType, prop.getName(), prop.getValue(), innerType, dataTypes);
		return convertedValue;
	}

	public Either<ToscaNodeType, ToscaError> convertRequirements(Component component, ToscaNodeType nodeType) {
		List<Map<String, ToscaRequirement>> toscaRequirements = convertRequirementsAsList(component);
		if (!toscaRequirements.isEmpty()) {
			nodeType.setRequirements(toscaRequirements);
		}
		log.debug("Finish convert Requirements for node type");

		return Either.left(nodeType);
	}

	public Either<SubstitutionMapping, ToscaError> convertSubstitutionMappingRequirements(Component component, SubstitutionMapping substitutionMapping) {
		Map<String, String[]> toscaRequirements = convertSubstitutionMappingRequirementsAsMap(component);
		if (!toscaRequirements.isEmpty()) {
			substitutionMapping.setRequirements(toscaRequirements);
		}
		log.debug("Finish convert Requirements for node type");

		return Either.left(substitutionMapping);
	}

	private List<Map<String, ToscaRequirement>> convertRequirementsAsList(Component component) {
		Map<String, List<RequirementDefinition>> requirements = component.getRequirements();
		List<Map<String, ToscaRequirement>> toscaRequirements = new ArrayList<>();
		if (requirements != null) {
			boolean isNodeType = ToscaUtils.isAtomicType(component);
			for (Map.Entry<String, List<RequirementDefinition>> entry : requirements.entrySet()) {
				entry.getValue().stream().filter(r -> (!isNodeType || (isNodeType && component.getUniqueId().equals(r.getOwnerId())) || (isNodeType && r.getOwnerId() == null))).forEach(r -> {
					ImmutablePair<String, ToscaRequirement> pair = convertRequirement(component, isNodeType, r);
					Map<String, ToscaRequirement> requirement = new HashMap<>();

					requirement.put(pair.left, pair.right);
					toscaRequirements.add(requirement);
				});

				log.debug("Finish convert Requirements for node type");
			}
		} else {
			log.debug("No Requirements for node type");
		}
		return toscaRequirements;
	}

	private String getSubPathByFirstDelimiterAppearance(String path) {
		return path.substring(path.indexOf(PATH_DELIMITER) + 1);
	}
	
	private String getSubPathByLastDelimiterAppearance(String path) {
		return path.substring(path.lastIndexOf(PATH_DELIMITER) + 1);
	}
	
	//This function calls on Substitution Mapping region - the component is always non-atomic
	private Map<String, String[]> convertSubstitutionMappingRequirementsAsMap(Component component) {
		Map<String, List<RequirementDefinition>> requirements = component.getRequirements();
		Map<String,  String[]> toscaRequirements = new HashMap<>();
		if (requirements != null) {
			for (Map.Entry<String, List<RequirementDefinition>> entry : requirements.entrySet()) {
				entry.getValue().stream().forEach(r -> {
					String fullReqName = getRequirementPath(r);
					log.debug("the requirement {} belongs to resource {} ", fullReqName, component.getUniqueId());
					toscaRequirements.put(fullReqName, new String[]{r.getOwnerName(), getSubPathByFirstDelimiterAppearance(fullReqName)});
				});
				log.debug("Finish convert Requirements for node type");
			}
		} else {
			log.debug("No Requirements for node type");
		}
		return toscaRequirements;
	}

	private String getRequirementPath(RequirementDefinition r) {
		List<String> pathArray = Lists.reverse(r.getPath().stream()
				.map(path -> ValidationUtils.normalizeComponentInstanceName(getSubPathByLastDelimiterAppearance(path)))
				.collect(Collectors.toList()));
		return new StringBuilder().append(String.join(PATH_DELIMITER, pathArray)).append(PATH_DELIMITER).append(r.getName()).toString();
		
	}
	
	private ImmutablePair<String, ToscaRequirement> convertRequirement(Component component, boolean isNodeType, RequirementDefinition r) {
		String name = r.getName();
		if (!isNodeType) {
			name = getRequirementPath(r);
		}
		log.debug("the requirement {} belongs to resource {} ", name, component.getUniqueId());
		ToscaRequirement toscaRequirement = new ToscaRequirement();

		List<Object> occurences = new ArrayList<>();
		occurences.add(Integer.valueOf(r.getMinOccurrences()));
		if (r.getMaxOccurrences().equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
			occurences.add(r.getMaxOccurrences());
		} else {
			occurences.add(Integer.valueOf(r.getMaxOccurrences()));
		}
		toscaRequirement.setOccurrences(occurences);
		// toscaRequirement.setOccurrences(createOcurrencesRange(requirementDefinition.getMinOccurrences(),
		// requirementDefinition.getMaxOccurrences()));
		toscaRequirement.setNode(r.getNode());
		toscaRequirement.setCapability(r.getCapability());
		toscaRequirement.setRelationship(r.getRelationship());

		ImmutablePair<String, ToscaRequirement> pair = new ImmutablePair<String, ToscaRequirement>(name, toscaRequirement);
		return pair;
	}

	public Map<String, ToscaCapability> convertCapabilities(Component component, Map<String, DataTypeDefinition> dataTypes) {
		Map<String, List<CapabilityDefinition>> capabilities = component.getCapabilities();
		Map<String, ToscaCapability> toscaCapabilities = new HashMap<>();
		if (capabilities != null) {
			boolean isNodeType = ToscaUtils.isAtomicType(component);
			for (Map.Entry<String, List<CapabilityDefinition>> entry : capabilities.entrySet()) {
				entry.getValue().stream().filter(c -> (!isNodeType || (isNodeType && component.getUniqueId().equals(c.getOwnerId())) || (isNodeType && c.getOwnerId() == null) )).forEach(c -> {
					convertCapabilty(component, toscaCapabilities, isNodeType, c, dataTypes);

				});
			}
		} else {
			log.debug("No Capabilities for node type");
		}

		return toscaCapabilities;
	}
	
	//This function calls on Substitution Mapping region - the component is always non-atomic
	public Map<String, String[]> convertSubstitutionMappingCapabilities(Component component, Map<String, DataTypeDefinition> dataTypes) {
		Map<String, List<CapabilityDefinition>> capabilities = component.getCapabilities();
		Map<String, String[]> toscaCapabilities = new HashMap<>();
		if (capabilities != null) {
			for (Map.Entry<String, List<CapabilityDefinition>> entry : capabilities.entrySet()) {
				entry.getValue().stream().forEach(c -> {
					String fullCapName = getCapabilityPath(c);
					log.debug("the capabilty {} belongs to resource {} ", fullCapName, component.getUniqueId());
					toscaCapabilities.put(fullCapName, new String[]{c.getOwnerName(), getSubPathByFirstDelimiterAppearance(fullCapName)});
				});
			}
		} else {
			log.debug("No Capabilities for node type");
		}

		return toscaCapabilities;
	}
	
	private String getCapabilityPath(CapabilityDefinition c)  {
		List<String> pathArray = Lists.reverse(c.getPath().stream()
				.map(path -> ValidationUtils.normalizeComponentInstanceName(getSubPathByLastDelimiterAppearance(path)))
				.collect(Collectors.toList()));
		return new StringBuilder().append(String.join(PATH_DELIMITER, pathArray)).append(PATH_DELIMITER).append(c.getName()).toString();
	}
	
	
	
	private void convertCapabilty(Component component, Map<String, ToscaCapability> toscaCapabilities, boolean isNodeType, CapabilityDefinition c, Map<String, DataTypeDefinition> dataTypes) {
		String name = c.getName();
		if (!isNodeType) {
			name = getCapabilityPath(c);
		}
		log.debug("the capabilty {} belongs to resource {} ", name, component.getUniqueId());
		ToscaCapability toscaCapability = new ToscaCapability();
		toscaCapability.setDescription(c.getDescription());
		toscaCapability.setType(c.getType());

		List<Object> occurences = new ArrayList<>();
		occurences.add(Integer.valueOf(c.getMinOccurrences()));
		if (c.getMaxOccurrences().equals(CapabilityDataDefinition.MAX_OCCURRENCES)) {
			occurences.add(c.getMaxOccurrences());
		} else {
			occurences.add(Integer.valueOf(c.getMaxOccurrences()));
		}
		toscaCapability.setOccurrences(occurences);

		toscaCapability.setValid_source_types(c.getValidSourceTypes());
		List<ComponentInstanceProperty> properties = c.getProperties();
		if (properties != null && !properties.isEmpty()) {
			Map<String, ToscaProperty> toscaProperties = new HashMap<>();
			for (PropertyDefinition property : properties) {
				ToscaProperty toscaProperty = PropertyConvertor.getInstance().convertProperty(dataTypes, property, true);
				toscaProperties.put(property.getName(), toscaProperty);
			}
			toscaCapability.setProperties(toscaProperties);
		}
		toscaCapabilities.put(name, toscaCapability);
	}

}
