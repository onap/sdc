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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.tosca.ToscaUtils.SubstituitionEntry;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fj.data.Either;
/**
 * Allows to convert requirements\capabilities of a component to requirements\capabilities of a substitution mappings section of a tosca template
 *
 */
@org.springframework.stereotype.Component("capabilty-requirement-convertor")
@Scope(value = "singleton")
public class CapabiltyRequirementConvertor {
	
	private static final String NO_CAPABILITIES = "No Capabilities for node type";
	private static CapabiltyRequirementConvertor instance;
	private static Logger logger = LoggerFactory.getLogger(CapabiltyRequirementConvertor.class.getName());
	public static final String PATH_DELIMITER = ".";
	
	@Autowired
	private ToscaOperationFacade toscaOperationFacade;
	
	protected CapabiltyRequirementConvertor() {}

	public static synchronized CapabiltyRequirementConvertor getInstance() {
		if (instance == null) {
			instance = new CapabiltyRequirementConvertor();
		}
		return instance;
	}
	/**
	 * Allows to convert capabilities of a component to capabilities of a substitution mappings section of a tosca template
	 * @param componentInstance
	 * @param dataTypes
	 * @param nodeTemplate
	 * @return
	 */
	public Either<ToscaNodeTemplate, ToscaError> convertComponentInstanceCapabilties(ComponentInstance componentInstance, Map<String, DataTypeDefinition> dataTypes, ToscaNodeTemplate nodeTemplate) {

		Map<String, List<CapabilityDefinition>> capabilitiesInst = componentInstance.getCapabilities();
		if (capabilitiesInst != null && !capabilitiesInst.isEmpty()) {
			Map<String, ToscaTemplateCapability> capabilties = new HashMap<>();
			capabilitiesInst.entrySet().forEach(e -> {
				List<CapabilityDefinition> capList = e.getValue();
				if (capList != null && !capList.isEmpty()) {
					capList.forEach(c -> convertOverridenProperties(componentInstance, dataTypes, capabilties, c));
				}
			});
			if (MapUtils.isNotEmpty(capabilties)) {
				nodeTemplate.setCapabilities(capabilties);
			}
		}
		return Either.left(nodeTemplate);
	}

	private void convertOverridenProperties(ComponentInstance componentInstance, Map<String, DataTypeDefinition> dataTypes, Map<String, ToscaTemplateCapability> capabilties, CapabilityDefinition c) {
		if (CollectionUtils.isNotEmpty(c.getProperties())) {
			c.getProperties()
			.stream()
			.filter(p -> p.getValue() != null || p.getDefaultValue() != null)
			.forEach(p -> convertOverridenProperty(componentInstance, dataTypes, capabilties, c, p));
		}
	}

	private void convertOverridenProperty(ComponentInstance componentInstance, Map<String, DataTypeDefinition> dataTypes, Map<String, ToscaTemplateCapability> capabilties, CapabilityDefinition c, ComponentInstanceProperty p) {
		if (logger.isDebugEnabled()) {
			logger.debug("Exist overriden property {} for capabity {} with value {}", p.getName(), c.getName(), p.getValue());
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
		logger.debug("Convert property {} for instance {}", prop.getName(), componentInstance.getUniqueId());
		String propertyType = prop.getType();
		String innerType = null;
		if (prop.getSchema() != null && prop.getSchema().getProperty() != null) {
			innerType = prop.getSchema().getProperty().getType();
		}
		String propValue = prop.getValue() == null ? prop.getDefaultValue() : prop.getValue();
		return PropertyConvertor.getInstance().convertToToscaObject(propertyType, propValue, innerType, dataTypes);
	}
	/**
	 * Allows to convert requirements of a node type to tosca template requirements representation
	 * @param component
	 * @param nodeType
	 * @return
	 */
	public Either<ToscaNodeType, ToscaError> convertRequirements(Component component, ToscaNodeType nodeType) {
		List<Map<String, ToscaRequirement>> toscaRequirements = convertRequirementsAsList(component);
		if (!toscaRequirements.isEmpty()) {
			nodeType.setRequirements(toscaRequirements);
		}
		logger.debug("Finish convert Requirements for node type");

		return Either.left(nodeType);
	}

	/**
	 * Allows to convert component requirements to the tosca template substitution mappings requirements
	 * @param componentsCache
	 * @param component
	 * @param substitutionMappings
	 * @return
	 */
	public Either<SubstitutionMapping, ToscaError> convertSubstitutionMappingRequirements(Map<String,Component> componentsCache, Component component, SubstitutionMapping substitutionMappings) {
		Either<SubstitutionMapping, ToscaError> result = Either.left(substitutionMappings);
		Either<Map<String, String[]>, ToscaError> toscaRequirementsRes = convertSubstitutionMappingRequirementsAsMap(componentsCache, component);
		if(toscaRequirementsRes.isRight()){
			result = Either.right(toscaRequirementsRes.right().value());
			logger.error("Failed convert requirements for the component {}. ", component.getName());
		} else if (MapUtils.isNotEmpty(toscaRequirementsRes.left().value())) {
			substitutionMappings.setRequirements(toscaRequirementsRes.left().value());
			result = Either.left(substitutionMappings);
			logger.debug("Finish convert requirements for the component {}. ", component.getName());
		}
		return result;
	}

	private List<Map<String, ToscaRequirement>> convertRequirementsAsList(Component component) {
		Map<String, List<RequirementDefinition>> requirements = component.getRequirements();
		List<Map<String, ToscaRequirement>> toscaRequirements = new ArrayList<>();
		if (requirements != null) {
			for (Map.Entry<String, List<RequirementDefinition>> entry : requirements.entrySet()) {
				entry.getValue().stream().filter(r -> filter(component, r.getOwnerId())).forEach(r -> {
					ImmutablePair<String, ToscaRequirement> pair = convertRequirement(component, ModelConverter.isAtomicComponent(component), r);
					Map<String, ToscaRequirement> requirement = new HashMap<>();

					requirement.put(pair.left, pair.right);
					toscaRequirements.add(requirement);
				});
				logger.debug("Finish convert Requirements for node type");
			}
		} else {
			logger.debug("No Requirements for node type");
		}
		return toscaRequirements;
	}

	private boolean filter(Component component, String ownerId) {
		return !ModelConverter.isAtomicComponent(component) || isNodeTypeOwner(component, ownerId) || (ModelConverter.isAtomicComponent(component) && ownerId == null);
	}

	private boolean isNodeTypeOwner(Component component, String ownerId) {
		return ModelConverter.isAtomicComponent(component) && component.getUniqueId().equals(ownerId);
	}
	
	private String getSubPathByLastDelimiterAppearance(String path) {
		return path.substring(path.lastIndexOf(PATH_DELIMITER) + 1);
	}

	private Either<Map<String, String[]>, ToscaError> convertSubstitutionMappingRequirementsAsMap(Map<String, Component> componentsCache, Component component) {
		Map<String, List<RequirementDefinition>> requirements = component.getRequirements();
		Either<Map<String, String[]>, ToscaError> result;
		if (requirements != null) {
			result = buildAddSubstitutionMappingsRequirements(componentsCache, component, requirements);
		} else {
			result = Either.left(Maps.newHashMap());
			logger.debug("No requirements for substitution mappings section of a tosca template of the component {}. ", component.getName());
		}
		return result;
	}

	private Either<Map<String, String[]>, ToscaError> buildAddSubstitutionMappingsRequirements(Map<String, Component> componentsCache, Component component, Map<String, List<RequirementDefinition>> requirements) {
		
		Map<String, String[]> toscaRequirements = new HashMap<>();
		Either<Map<String, String[]>, ToscaError> result = null;
		for (Map.Entry<String, List<RequirementDefinition>> entry : requirements.entrySet()) {
			Optional<RequirementDefinition> failedToAddRequirement = entry.getValue()
					.stream()
					.filter(r->!addEntry(componentsCache, toscaRequirements, component, r.getName(), r.getParentName(), r.getPath()))
					.findAny();
			if(failedToAddRequirement.isPresent()){
				logger.error("Failed to convert requirement {} for substitution mappings section of a tosca template of the component {}. ",
						failedToAddRequirement.get().getName(), component.getName());
				result = Either.right(ToscaError.NODE_TYPE_REQUIREMENT_ERROR);
			}
			logger.debug("Finish convert requirements for the component {}. ", component.getName());
		}
		if(result == null){
			result = Either.left(toscaRequirements);
		}
		return result;
	}
	
	private Either<Map<String, String[]>, ToscaError> buildAddSubstitutionMappingsCapabilities(Map<String, Component> componentsCache, Component component, Map<String, List<CapabilityDefinition>> capabilities) {
		
		Map<String, String[]> toscaRequirements = new HashMap<>();
		Either<Map<String, String[]>, ToscaError> result = null;
		for (Map.Entry<String, List<CapabilityDefinition>> entry : capabilities.entrySet()) {
			Optional<CapabilityDefinition> failedToAddRequirement = entry.getValue()
					.stream()
					.filter(c->!addEntry(componentsCache, toscaRequirements, component, c.getName(), c.getParentName(), c.getPath()))
					.findAny();
			if(failedToAddRequirement.isPresent()){
				logger.error("Failed to convert capalility {} for substitution mappings section of a tosca template of the component {}. ",
						failedToAddRequirement.get().getName(), component.getName());
				result = Either.right(ToscaError.NODE_TYPE_CAPABILITY_ERROR);
			}
			logger.debug("Finish convert capalilities for the component {}. ", component.getName());
		}
		if(result == null){
			result = Either.left(toscaRequirements);
		}
		return result;
	}
	
	private boolean addEntry(Map<String,Component> componentsCache, Map<String, String[]> capReqMap, Component component, String name, String parentName, List<String> path){

		SubstituitionEntry entry = new SubstituitionEntry(name, parentName, "");
		
		if(shouldBuildSubstitutionName(component, path) && !buildSubstitutedNamePerInstance(componentsCache, component, name, path, entry)){
			return false;
		}
		logger.debug("The requirement/capability {} belongs to the component {} ", entry.getFullName(), component.getUniqueId());
		if (entry.getSourceName() != null) {
			addEntry(capReqMap, component, path, entry);
		}
		logger.debug("Finish convert the requirement/capability {} for the component {}. ", entry.getFullName(), component.getName());
		return true;
	
	}

	private boolean shouldBuildSubstitutionName(Component component, List<String> path) {
		return !ToscaUtils.isComplexVfc(component) && CollectionUtils.isNotEmpty(path) && path.iterator().hasNext();
	}

	private boolean buildSubstitutedNamePerInstance(Map<String, Component> componentsCache, Component component, String name, List<String> path, SubstituitionEntry entry) {
		Optional<ComponentInstance> ci = component.getComponentInstances().stream().filter(c->c.getUniqueId().equals(Iterables.getLast(path))).findFirst();
		if(ci.isPresent()){
			Either<String, Boolean> buildSubstitutedName = buildSubstitutedName(componentsCache, name, path, ci.get());
			if(buildSubstitutedName.isRight()){
				return false;
			}
			entry.setFullName(ci.get().getNormalizedName() + '.' + buildSubstitutedName.left().value());
			entry.setSourceName(buildSubstitutedName.left().value());
		} else {
			return false;
		}
		return true;
	}

	private void addEntry(Map<String, String[]> toscaRequirements, Component component, List<String> capPath, SubstituitionEntry entry) {
		Optional<ComponentInstance> findFirst = component.getComponentInstances().stream().filter(ci -> ci.getUniqueId().equals(Iterables.getLast(capPath))).findFirst();
		if (findFirst.isPresent()) {
			entry.setOwner(findFirst.get().getNormalizedName());
		}
		toscaRequirements.put(entry.getFullName(), new String[] { entry.getOwner(), entry.getSourceName() });
	}

	private Either<String, Boolean> buildSubstitutedName(Map<String, Component> componentsCache, String name, List<String> path, ComponentInstance instance) {
		
		Either<String, Boolean> result = null;
		Either<Component, Boolean> getOriginRes = getOriginComponent(componentsCache, instance);
		if(getOriginRes.isRight()){
			logger.debug("Failed to build substituted name for the capability/requirement {}. Failed to get an origin component with uniqueId {}", name, instance.getComponentUid());
			result = Either.right(false);
		}
		if(result == null){
			List<String> reducedPath = getReducedPath(path);
			reducedPath.remove(reducedPath.size()-1);
			result = buildSubstitutedName(componentsCache, getOriginRes.left().value(), reducedPath, name);
		}
		return result;
	}

	private String getRequirementPath(Component component, RequirementDefinition r) {
			
		// Evg : for the last in path take real instance name and not "decrypt" unique id. ( instance name can be change and not equal to id..)
		// dirty quick fix. must be changed as capability redesign
		List<String> capPath = r.getPath();
		String lastInPath = capPath.get(capPath.size() - 1);
		Optional<ComponentInstance> findFirst = component.getComponentInstances().stream().filter(ci -> ci.getUniqueId().equals(lastInPath)).findFirst();
		if (findFirst.isPresent()) {
			String lastInPathName = findFirst.get().getNormalizedName();

			if (capPath.size() > 1) {
				List<String> pathArray = Lists.reverse(capPath.stream().map(path -> ValidationUtils.normalizeComponentInstanceName(getSubPathByLastDelimiterAppearance(path))).collect(Collectors.toList()));

				return new StringBuilder().append(lastInPathName).append(PATH_DELIMITER).append(String.join(PATH_DELIMITER, pathArray.subList(1, pathArray.size() ))).append(PATH_DELIMITER).append(r.getName()).toString();
			}else{
				return new StringBuilder().append(lastInPathName).append(PATH_DELIMITER).append(r.getName()).toString();
			}
		}
		return "";
	}

	private ImmutablePair<String, ToscaRequirement> convertRequirement(Component component, boolean isNodeType, RequirementDefinition r) {
		String name = r.getName();
		if (!isNodeType) {
			name = getRequirementPath(component, r);
		}
		logger.debug("the requirement {} belongs to resource {} ", name, component.getUniqueId());
		ToscaRequirement toscaRequirement = new ToscaRequirement();

		List<Object> occurences = new ArrayList<>();
		occurences.add(Integer.valueOf(r.getMinOccurrences()));
		if (r.getMaxOccurrences().equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
			occurences.add(r.getMaxOccurrences());
		} else {
			occurences.add(Integer.valueOf(r.getMaxOccurrences()));
		}
		toscaRequirement.setOccurrences(occurences);
		toscaRequirement.setNode(r.getNode());
		toscaRequirement.setCapability(r.getCapability());
		toscaRequirement.setRelationship(r.getRelationship());

		return new ImmutablePair<>(name, toscaRequirement);
	}

	/**
	 * Allows to convert capabilities of a node type to tosca template capabilities
	 * @param component
	 * @param dataTypes
	 * @return
	 */
	public Map<String, ToscaCapability> convertCapabilities(Component component, Map<String, DataTypeDefinition> dataTypes) {
		Map<String, List<CapabilityDefinition>> capabilities = component.getCapabilities();
		Map<String, ToscaCapability> toscaCapabilities = new HashMap<>();
		if (capabilities != null) {
			boolean isNodeType = ModelConverter.isAtomicComponent(component);
			for (Map.Entry<String, List<CapabilityDefinition>> entry : capabilities.entrySet()) {
				entry.getValue().stream().filter(c -> filter(component, c.getOwnerId())).forEach(c -> convertCapabilty(component, toscaCapabilities, isNodeType, c, dataTypes));
			}
		} else {
			logger.debug(NO_CAPABILITIES);
		}

		return toscaCapabilities;
	}

	/**
	 * Allows to convert capabilities of a server proxy node type to tosca template capabilities
	 * @param component
	 * @param proxyComponent
	 * @param instanceProxy
	 * @param dataTypes
	 * @return
	 */
	public Map<String, ToscaCapability> convertProxyCapabilities(Component component, Component proxyComponent, ComponentInstance instanceProxy, Map<String, DataTypeDefinition> dataTypes) {
		Map<String, List<CapabilityDefinition>> capabilities = instanceProxy.getCapabilities();
		Map<String, ToscaCapability> toscaCapabilities = new HashMap<>();
		if (capabilities != null) {
			boolean isNodeType = ModelConverter.isAtomicComponent(component);
			for (Map.Entry<String, List<CapabilityDefinition>> entry : capabilities.entrySet()) {
				entry.getValue().stream().forEach(c -> convertCapabilty(proxyComponent, toscaCapabilities, isNodeType, c, dataTypes));
			}
		} else {
			logger.debug(NO_CAPABILITIES);
		}

		return toscaCapabilities;
	}

	/**
	 * Allows to convert component capabilities to the tosca template substitution mappings capabilities
	 * @param componentsCache
	 * @param component
	 * @return
	 */
	public Either<Map<String, String[]>, ToscaError> convertSubstitutionMappingCapabilities(Map<String, Component> componentsCache, Component component) {
		Map<String, List<CapabilityDefinition>> capabilities = component.getCapabilities();
		Either<Map<String, String[]>, ToscaError> res;
		if (capabilities != null) {
			res = buildAddSubstitutionMappingsCapabilities(componentsCache, component, capabilities);
		} else {
			res = Either.left(Maps.newHashMap());
			logger.debug(NO_CAPABILITIES);
		}
		return res;
	}
	
	private String getCapabilityPath(CapabilityDefinition c, Component component) {
		// Evg : for the last in path take real instance name and not "decrypt" unique id. ( instance name can be change and not equal to id..)
		// dirty quick fix. must be changed as capability redesign
		List<String> capPath = c.getPath();
		String lastInPath = capPath.get(capPath.size() - 1);
		Optional<ComponentInstance> findFirst = component.getComponentInstances().stream().filter(ci -> ci.getUniqueId().equals(lastInPath)).findFirst();
		if (findFirst.isPresent()) {
			String lastInPathName = findFirst.get().getNormalizedName();

			if (capPath.size() > 1) {
				List<String> pathArray = Lists.reverse(capPath.stream().map(path -> ValidationUtils.normalizeComponentInstanceName(getSubPathByLastDelimiterAppearance(path))).collect(Collectors.toList()));

				return new StringBuilder().append(lastInPathName).append(PATH_DELIMITER).append(String.join(PATH_DELIMITER, pathArray.subList(1, pathArray.size() ))).append(PATH_DELIMITER).append(c.getName()).toString();
			}else{
				return new StringBuilder().append(lastInPathName).append(PATH_DELIMITER).append(c.getName()).toString();
			}
		}
		return "";
	}

	private void convertCapabilty(Component component, Map<String, ToscaCapability> toscaCapabilities, boolean isNodeType, CapabilityDefinition c, Map<String, DataTypeDefinition> dataTypes) {
		String name = c.getName();
		if (!isNodeType) {
			name = getCapabilityPath(c, component);
		}
		logger.debug("the capabilty {} belongs to resource {} ", name, component.getUniqueId());
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
		if (CollectionUtils.isNotEmpty(properties)) {
			Map<String, ToscaProperty> toscaProperties = new HashMap<>();
			for (PropertyDefinition property : properties) {
				ToscaProperty toscaProperty = PropertyConvertor.getInstance().convertProperty(dataTypes, property, true);
				toscaProperties.put(property.getName(), toscaProperty);
			}
			toscaCapability.setProperties(toscaProperties);
		}
		toscaCapabilities.put(name, toscaCapability);
	}
	/**
	 * Allows to build substituted name of capability\requirement of the origin component instance according to the path 
	 * @param componentsCache
	 * @param originComponent
	 * @param path
	 * @param name
	 * @return
	 */
	public Either<String, Boolean> buildSubstitutedName(Map<String, Component> componentsCache, Component originComponent, List<String> path, String name) {
		StringBuilder substitutedName = new StringBuilder();
		boolean nameBuiltSuccessfully = true;
		Either<String, Boolean> result;
		if(CollectionUtils.isNotEmpty(path) && !ToscaUtils.isComplexVfc(originComponent)){
			List<String> reducedPath = getReducedPath(path);
			Collections.reverse(reducedPath);
			nameBuiltSuccessfully = appendNameRecursively(componentsCache, originComponent, reducedPath.iterator(), substitutedName);
		}
		if(nameBuiltSuccessfully){
			result = Either.left(substitutedName.append(name).toString());
		} else {
			result = Either.right(nameBuiltSuccessfully);
		}
		return result;
	}

	private List<String> getReducedPath(List<String> path) {
		List<String> pathCopy = Lists.newArrayList();
		path.stream().forEach(id -> {if(!pathCopy.contains(id))pathCopy.add(id);});
		return pathCopy;
	}

	private boolean appendNameRecursively(Map<String, Component> componentsCache, Component originComponent, Iterator<String> instanceIdIter, StringBuilder substitutedName) {
		if(CollectionUtils.isNotEmpty(originComponent.getComponentInstances()) && instanceIdIter.hasNext() && !ToscaUtils.isComplexVfc(originComponent)){
			String instanceId = instanceIdIter.next();
			Optional<ComponentInstance> instanceOpt = originComponent.getComponentInstances().stream().filter(i -> i.getUniqueId().equals(instanceId)).findFirst();
			if(!instanceOpt.isPresent()){
				logger.debug("Failed to find an instance with uniqueId {} on a component with uniqueId {}", instanceId, originComponent.getUniqueId());
				return false;
			}
			substitutedName.append(instanceOpt.get().getNormalizedName()).append('.');
			Either<Component, Boolean> getOriginRes = getOriginComponent(componentsCache, instanceOpt.get());
			if(getOriginRes.isRight()){
				return false;
			}
			appendNameRecursively(componentsCache, getOriginRes.left().value(), instanceIdIter, substitutedName);
		}
		return true;
	}

	Either<Component, Boolean> getOriginComponent(Map<String, Component> componentsCache, ComponentInstance instance) {
		Either<Component, Boolean> result;
		Either<Component, StorageOperationStatus> getOriginRes;
		if(componentsCache.containsKey(instance.getActualComponentUid())){
			result = Either.left(componentsCache.get(instance.getActualComponentUid()));
		} else {
			ComponentParametersView filter = getFilter(instance);
			getOriginRes = toscaOperationFacade.getToscaElement(instance.getActualComponentUid(), filter);
			if(getOriginRes.isRight()){
				logger.debug("Failed to get an origin component with uniqueId {}", instance.getActualComponentUid());
				result = Either.right(false);
			} else {
				result = Either.left(getOriginRes.left().value());
				componentsCache.put(getOriginRes.left().value().getUniqueId(), getOriginRes.left().value());
			}
		}
		return result;
	}

	private ComponentParametersView getFilter(ComponentInstance instance) {
		ComponentParametersView filter = new ComponentParametersView(true);
		filter.setIgnoreComponentInstances(false);
		if(instance.getIsProxy()){
			filter.setIgnoreCapabilities(false);
			filter.setIgnoreRequirements(false);
		}
		return filter;
	}

}
