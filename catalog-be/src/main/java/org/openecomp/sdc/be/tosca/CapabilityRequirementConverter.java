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

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.utils.ComponentUtilities;
import org.openecomp.sdc.be.tosca.ToscaUtils.SubstitutionEntry;
import org.openecomp.sdc.be.tosca.model.*;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

/**
 * Allows to convert requirements\capabilities of a component to requirements\capabilities of a substitution mappings section of a tosca template
 *
 */
@org.springframework.stereotype.Component("capabilty-requirement-convertor")
@Scope(value = "singleton")
public class CapabilityRequirementConverter {

    private static final String NO_CAPABILITIES = "No Capabilities for node type";
    private static CapabilityRequirementConverter instance;
    private static final Logger logger = Logger.getLogger(CapabilityRequirementConverter.class);
    private static final String PATH_DELIMITER = ".";

    @Autowired
    private ToscaOperationFacade toscaOperationFacade;

    public CapabilityRequirementConverter() {}

    public static synchronized CapabilityRequirementConverter getInstance() {
        if (instance == null) {
            instance = new CapabilityRequirementConverter();
        }
        return instance;
    }

    public String buildCapabilityNameForComponentInstance( Map<String,Component> componentCache , ComponentInstance componentInstance, CapabilityDefinition c) {
        String prefix = buildCapReqNamePrefix(componentInstance.getNormalizedName());
        if(ComponentUtilities.isNotUpdatedCapReqName(prefix, c.getName(), c.getPreviousName())){
            return buildSubstitutedName(componentCache, c.getName(), c.getPreviousName(), c.getPath(), c.getOwnerId(), componentInstance)
                    .left()
                    .orValue(c.getName());
        }
        return c.getPreviousName();
    }

    private String buildCapReqNamePrefix(String normalizedName) {
        return normalizedName + PATH_DELIMITER;
    }

    /**
     * Allows to convert capabilities of a component to capabilities of a substitution mappings section of a tosca template
     * @param componentInstance
     * @param dataTypes
     * @param nodeTemplate
     * @return
     */
    public Either<ToscaNodeTemplate, ToscaError> convertComponentInstanceCapabilities(ComponentInstance componentInstance, Map<String, DataTypeDefinition> dataTypes, ToscaNodeTemplate nodeTemplate) {

        Map<String, List<CapabilityDefinition>> capabilitiesInst = componentInstance.getCapabilities();
        Map<String,Component> componentCache = new HashMap<>();
        if (capabilitiesInst != null && !capabilitiesInst.isEmpty()) {
            Map<String, ToscaTemplateCapability> capabilities = new HashMap<>();
            capabilitiesInst.entrySet().forEach( e -> {
                List<CapabilityDefinition> capList = e.getValue();
                if ( capList != null && !capList.isEmpty() ) {
                    capList.stream()
                            .forEach( c -> convertOverridenProperties( componentInstance, dataTypes, capabilities, c ,
                                buildCapabilityNameForComponentInstance( componentCache , componentInstance , c )));
                }
            });
            if (MapUtils.isNotEmpty(capabilities)) {
                nodeTemplate.setCapabilities(capabilities);
            }
        }
        return Either.left(nodeTemplate);
    }

    private void convertOverridenProperties(ComponentInstance componentInstance, Map<String, DataTypeDefinition> dataTypes, Map<String, ToscaTemplateCapability> capabilties, CapabilityDefinition c , String capabilityName) {
        if (isNotEmpty(c.getProperties())) {
            c.getProperties()
            .stream()
            .filter(p -> p.getValue() != null || p.getDefaultValue() != null)
            .forEach(p -> convertOverriddenProperty(componentInstance, dataTypes, capabilties , p ,capabilityName));
        }
    }

    private void convertOverriddenProperty(ComponentInstance componentInstance, Map<String, DataTypeDefinition> dataTypes, Map<String, ToscaTemplateCapability> capabilties, ComponentInstanceProperty p , String capabilityName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exist d property {} for capability {} with value {}", p.getName(), capabilityName, p.getValue());
        }
        ToscaTemplateCapability toscaTemplateCapability = capabilties.computeIfAbsent( capabilityName , key -> new ToscaTemplateCapability() );

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
        return PropertyConvertor.getInstance().convertToToscaObject(propertyType, propValue, innerType, dataTypes, false);
    }
    /**
     * Allows to convert requirements of a node type to tosca template requirements representation
     * @param component
     * @param nodeType
     * @return
     */
    public Either<ToscaNodeType, ToscaError> convertRequirements(Map<String, Component> componentsCache, Component component, ToscaNodeType nodeType) {
        List<Map<String, ToscaRequirement>> toscaRequirements = convertRequirementsAsList(componentsCache, component);
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
            logger.debug("Failed convert requirements for the component {}. ", component.getName());
        } else if (MapUtils.isNotEmpty(toscaRequirementsRes.left().value())) {
            substitutionMappings.setRequirements(toscaRequirementsRes.left().value());
            result = Either.left(substitutionMappings);
            logger.debug("Finish convert requirements for the component {}. ", component.getName());
        }
        return result;
    }

    private List<Map<String, ToscaRequirement>> convertRequirementsAsList(Map<String, Component> componentsCache, Component component) {
        Map<String, List<RequirementDefinition>> requirements = component.getRequirements();
        List<Map<String, ToscaRequirement>> toscaRequirements = new ArrayList<>();
        if (requirements != null) {
            for (Map.Entry<String, List<RequirementDefinition>> entry : requirements.entrySet()) {
                entry.getValue().stream().filter(r -> filter(component, r.getOwnerId())).forEach(r -> {
                    ImmutablePair<String, ToscaRequirement> pair = convertRequirement(componentsCache, component, ModelConverter.isAtomicComponent(component), r);
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

    private String dropLast( String path, String delimiter ) {
        if (isBlank(path) || isBlank(delimiter)){
            return path;
        }
        return path.substring(0, path.lastIndexOf(delimiter));
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
                    .filter(r->!addEntry(componentsCache, toscaRequirements, component, new SubstitutionEntry(r.getName(), r.getParentName(), ""), r.getPreviousName(), r.getOwnerId(), r.getPath()))
                    .findAny();
            if(failedToAddRequirement.isPresent()){
                logger.debug("Failed to convert requirement {} for substitution mappings section of a tosca template of the component {}. ",
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

        Map<String, String[]> toscaCapabilities = new HashMap<>();
        Either<Map<String, String[]>, ToscaError> result = null;
        for (Map.Entry<String, List<CapabilityDefinition>> entry : capabilities.entrySet()) {
            Optional<CapabilityDefinition> failedToAddRequirement = entry.getValue()
                    .stream()
                    .filter(c->!addEntry(componentsCache, toscaCapabilities, component, new SubstitutionEntry(c.getName(), c.getParentName(), ""), c.getPreviousName(), c.getOwnerId(), c.getPath()))
                    .findAny();
            if(failedToAddRequirement.isPresent()){
                logger.debug("Failed to convert capability {} for substitution mappings section of a tosca template of the component {}. ",
                        failedToAddRequirement.get().getName(), component.getName());
                result = Either.right(ToscaError.NODE_TYPE_CAPABILITY_ERROR);
            }
            logger.debug("Finish convert capabilities for the component {}. ", component.getName());
        }
        if(result == null){
            result = Either.left(toscaCapabilities);
        }
        return result;
    }

    private boolean addEntry(Map<String, Component> componentsCache, Map<String, String[]> capReqMap, Component component, SubstitutionEntry entry, String previousName, String ownerId, List<String> path){
    
        if(shouldBuildSubstitutionName(component, path) && !buildSubstitutedNamePerInstance(componentsCache, component, entry.getFullName(), previousName, path, ownerId, entry)){
            return false;
        }
        logger.debug("The requirement/capability {} belongs to the component {} ", entry.getFullName(), component.getUniqueId());
        if (StringUtils.isNotEmpty(entry.getSourceName())) {
            addEntry(capReqMap, component, path, entry);
        }
        logger.debug("Finish convert the requirement/capability {} for the component {}. ", entry.getFullName(), component.getName());
        return true;

    }

    private boolean shouldBuildSubstitutionName(Component component, List<String> path) {
        return ToscaUtils.isNotComplexVfc(component) && isNotEmpty(path) && path.iterator().hasNext();
    }

    private boolean buildSubstitutedNamePerInstance(Map<String, Component> componentsCache, Component component, String name, String previousName, List<String> path, String ownerId, SubstitutionEntry entry) {
        String fullName;
        String sourceName;
        String prefix;
        if(CollectionUtils.isNotEmpty(component.getGroups())) {
            Optional<GroupDefinition> groupOpt = component.getGroups().stream().filter(g -> g.getUniqueId().equals(ownerId)).findFirst();
            if (groupOpt.isPresent()) {
                prefix = buildCapReqNamePrefix(groupOpt.get().getNormalizedName());
                if(ComponentUtilities.isNotUpdatedCapReqName(prefix, name, previousName)){
                    sourceName = name;
                    fullName = prefix + sourceName;
                } else {
                    sourceName = previousName;
                    fullName = name;
                }
                entry.setFullName(fullName);
                entry.setSourceName(sourceName);
                entry.setOwner(groupOpt.get().getNormalizedName());
                return true;
            }
        }

        Optional<ComponentInstance> ci =
                component.safeGetComponentInstances().stream().filter(c->c.getUniqueId().equals(Iterables.getLast(path))).findFirst();
        if(!ci.isPresent()){
            logger.debug("Failed to find ci in the path is {} component {}", path, component.getUniqueId());

            Collections.reverse(path);

            logger.debug("try to reverse path {} component {}", path, component.getUniqueId());
            ci = component.safeGetComponentInstances().stream().filter(c->c.getUniqueId().equals(Iterables.getLast(path))).findFirst();
        }
        if(ci.isPresent()){
            prefix = buildCapReqNamePrefix(ci.get().getNormalizedName());
            if(ComponentUtilities.isNotUpdatedCapReqName(prefix, name, previousName)){
                Either<String, Boolean> buildSubstitutedName = buildSubstitutedName(componentsCache, name, previousName, path, ownerId, ci.get());
                if(buildSubstitutedName.isRight()){
                    logger.debug("Failed buildSubstitutedName name {}  path {} component {}", name, path, component.getUniqueId());
                    return false;
                }
                sourceName = buildSubstitutedName.left().value();
                fullName = prefix + sourceName;
            } else {
                sourceName = previousName;
                fullName = name;
            }
            entry.setFullName(fullName);
            entry.setSourceName(sourceName);
        } else {
            logger.debug("Failed to find ci in the path is {} component {}", path, component.getUniqueId());
            return false;
        }
        return true;
    }

    private void addEntry(Map<String, String[]> toscaRequirements, Component component, List<String> capPath, SubstitutionEntry entry) {
        Optional<ComponentInstance> findFirst = component.safeGetComponentInstances().stream().filter(ci -> ci.getUniqueId().equals(Iterables.getLast(capPath))).findFirst();
        findFirst.ifPresent(componentInstance -> entry.setOwner(componentInstance.getName()));
        if (StringUtils.isNotEmpty(entry.getOwner()) && StringUtils.isNotEmpty(entry.getSourceName())) {
            toscaRequirements.put(entry.getFullName(), new String[] { entry.getOwner(), entry.getSourceName() });
        }
    }

    public Either<String, Boolean> buildSubstitutedName(Map<String, Component> componentsCache, String name, String previousName, List<String> path, String ownerId, ComponentInstance instance) {
        if(StringUtils.isNotEmpty(previousName)){
            return Either.left(name);
        }
        Either<Component, Boolean> getOriginRes = getOriginComponent(componentsCache, instance);
        if(getOriginRes.isRight()){
            logger.debug("Failed to build substituted name for the capability/requirement {}. Failed to get an origin component with uniqueId {}", name, instance.getComponentUid());
            return Either.right(false);
        }
        List<String> reducedPath = ownerId !=null ? getReducedPathByOwner(path , ownerId ) : getReducedPath(path) ;
        logger.debug("reducedPath for ownerId {}, reducedPath {} ", ownerId, reducedPath);
        reducedPath.remove(reducedPath.size() - 1);
        return buildSubstitutedName(componentsCache, getOriginRes.left().value(), reducedPath, name, previousName);
    }

    private String buildReqNamePerOwnerByPath(Map<String, Component> componentsCache, Component component, RequirementDefinition r) {
        return buildCapReqNamePerOwnerByPath(componentsCache, component, r.getName(), r.getPreviousName(), r.getPath());
    }

    private ImmutablePair<String, ToscaRequirement> convertRequirement(Map<String, Component> componentsCache, Component component, boolean isNodeType, RequirementDefinition r) {
        String name = r.getName();
        if (!isNodeType && ToscaUtils.isNotComplexVfc(component)) {
            name = buildReqNamePerOwnerByPath(componentsCache, component, r);
        }
        logger.debug("the requirement {} belongs to resource {} ", name, component.getUniqueId());
        ToscaRequirement toscaRequirement = new ToscaRequirement();

        List<Object> occurrences = new ArrayList<>();
        occurrences.add(Integer.valueOf(r.getMinOccurrences()));
        if (r.getMaxOccurrences().equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
            occurrences.add(r.getMaxOccurrences());
        } else {
            occurrences.add(Integer.valueOf(r.getMaxOccurrences()));
        }
        toscaRequirement.setOccurrences(occurrences);
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
    public Map<String, ToscaCapability> convertCapabilities(Map<String, Component> componentsCache, Component component, Map<String, DataTypeDefinition> dataTypes) {
        Map<String, List<CapabilityDefinition>> capabilities = component.getCapabilities();
        Map<String, ToscaCapability> toscaCapabilities = new HashMap<>();
        if (capabilities != null) {
            boolean isNodeType = ModelConverter.isAtomicComponent(component);
            for (Map.Entry<String, List<CapabilityDefinition>> entry : capabilities.entrySet()) {
                entry.getValue().stream().filter(c -> filter(component, c.getOwnerId())).forEach(c -> convertCapability(componentsCache, component, toscaCapabilities, isNodeType, c, dataTypes , c.getName()));
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
    public Map<String, ToscaCapability> convertProxyCapabilities(Map<String, Component> componentCache, Component component, Component proxyComponent, ComponentInstance instanceProxy, Map<String, DataTypeDefinition> dataTypes) {
        Map<String, List<CapabilityDefinition>> capabilities = instanceProxy.getCapabilities();
        Map<String, ToscaCapability> toscaCapabilities = new HashMap<>();
        if (capabilities != null) {
            boolean isNodeType = ModelConverter.isAtomicComponent(component);
            for (Map.Entry<String, List<CapabilityDefinition>> entry : capabilities.entrySet()) {
                entry.getValue()
                        .stream()
                        .forEach(c -> convertProxyCapability(toscaCapabilities, c, dataTypes ,
                                buildCapabilityNameForComponentInstance( componentCache , instanceProxy , c )));
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

    private String buildCapNamePerOwnerByPath(Map<String, Component> componentsCache, CapabilityDefinition c, Component component) {
        return buildCapReqNamePerOwnerByPath(componentsCache, component, c.getName(), c.getPreviousName(), c.getPath());
    }

    private void convertProxyCapability(Map<String, ToscaCapability> toscaCapabilities, CapabilityDefinition c,
                                        Map<String, DataTypeDefinition> dataTypes, String capabilityName) {
        createToscaCapability(toscaCapabilities, c, dataTypes, capabilityName);
    }

    private void convertCapability(Map<String, Component> componentsCache, Component component, Map<String, ToscaCapability> toscaCapabilities, boolean isNodeType, CapabilityDefinition c, Map<String, DataTypeDefinition> dataTypes , String capabilityName) {
        String name = isNoneBlank(capabilityName) ? capabilityName : c.getName();
        if (!isNodeType && ToscaUtils.isNotComplexVfc(component)) {
            name = buildCapNamePerOwnerByPath(componentsCache, c, component);
        }
        logger.debug("The capability {} belongs to resource {} ", name, component.getUniqueId());
        createToscaCapability(toscaCapabilities, c, dataTypes, name);
    }

    private void createToscaCapability(Map<String, ToscaCapability> toscaCapabilities, CapabilityDefinition c,
                                       Map<String, DataTypeDefinition> dataTypes, String name) {
        ToscaCapability toscaCapability = new ToscaCapability();
        toscaCapability.setDescription(c.getDescription());
        toscaCapability.setType(c.getType());

        List<Object> occurrences = new ArrayList<>();
        occurrences.add(Integer.valueOf(c.getMinOccurrences()));
        if (c.getMaxOccurrences().equals(CapabilityDataDefinition.MAX_OCCURRENCES)) {
            occurrences.add(c.getMaxOccurrences());
        } else {
            occurrences.add(Integer.valueOf(c.getMaxOccurrences()));
        }
        toscaCapability.setOccurrences(occurrences);

        toscaCapability.setValid_source_types(c.getValidSourceTypes());
        List<ComponentInstanceProperty> properties = c.getProperties();
        if (isNotEmpty(properties)) {
            Map<String, ToscaProperty> toscaProperties = new HashMap<>();
            for (PropertyDefinition property : properties) {
                ToscaProperty toscaProperty = PropertyConvertor.getInstance().convertProperty(dataTypes, property, PropertyConvertor.PropertyType.CAPABILITY);
                toscaProperties.put(property.getName(), toscaProperty);
            }
            toscaCapability.setProperties(toscaProperties);
        }
        toscaCapabilities.put(name, toscaCapability);
    }

    private String buildCapReqNamePerOwnerByPath(Map<String, Component> componentsCache, Component component, String name, String previousName, List<String> path) {
        if (CollectionUtils.isEmpty(path)) {
            return name;
        }
        String ownerId = path.get(path.size() - 1);
        String prefix;
        if(CollectionUtils.isNotEmpty(component.getGroups())) {
            Optional<GroupDefinition> groupOpt = component.getGroups().stream().filter(g -> g.getUniqueId().equals(ownerId)).findFirst();
            if (groupOpt.isPresent()) {
                prefix = buildCapReqNamePrefix(groupOpt.get().getNormalizedName());
                if(ComponentUtilities.isNotUpdatedCapReqName(prefix, name, previousName)){
                    return prefix + name;
                }
                return name;
            }
        }
        Optional<ComponentInstance> ci = component.safeGetComponentInstances().stream().filter(c->c.getUniqueId().equals(Iterables.getLast(path))).findFirst();
        if(!ci.isPresent()){
            logger.debug("Failed to find ci in the path is {} component {}", path, component.getUniqueId());

            Collections.reverse(path);

            logger.debug("try to reverse path {} component {}", path, component.getUniqueId());
            ci = component.safeGetComponentInstances().stream().filter(c->c.getUniqueId().equals(Iterables.getLast(path))).findFirst();
        }
        if(ci.isPresent()){
            prefix = buildCapReqNamePrefix(ci.get().getNormalizedName());
            if(ComponentUtilities.isNotUpdatedCapReqName(prefix, name, previousName)){
                Either<String, Boolean> buildSubstitutedName = buildSubstitutedName(componentsCache, name, previousName, path, ownerId, ci.get());
                if(buildSubstitutedName.isRight()){
                    logger.debug("Failed buildSubstitutedName name {}  path {} component {}", name, path, component.getUniqueId());
                }
                return prefix + buildSubstitutedName.left().value();
            }
            return name;
        }
        return StringUtils.EMPTY;
    }
    /**
     * Allows to build substituted name of capability\requirement of the origin component instance according to the path
     * @param componentsCache
     * @param originComponent
     * @param path
     * @param name
     * @param previousName
     * @return
     */
    public Either<String, Boolean> buildSubstitutedName(Map<String, Component> componentsCache, Component originComponent, List<String> path, String name, String previousName) {
        if(StringUtils.isNotEmpty(previousName)){
            return Either.left(name);
        }
        StringBuilder substitutedName = new StringBuilder();
        boolean nameBuiltSuccessfully = true;
        if(isNotEmpty(path) && ToscaUtils.isNotComplexVfc(originComponent)){
            List<String> reducedPath = getReducedPath(path);
            Collections.reverse(reducedPath);
            nameBuiltSuccessfully = appendNameRecursively(componentsCache, originComponent, reducedPath.iterator(), substitutedName);
        }
        return nameBuiltSuccessfully ? Either.left(substitutedName.append(name).toString()) : Either.right(nameBuiltSuccessfully);
    }

    protected List<String> getReducedPathByOwner(List<String> path , String ownerId) {
        logger.debug("ownerId {}, path {} ", ownerId, path);
        if ( CollectionUtils.isEmpty(path) ){
            logger.debug("cannot perform reduce by owner, path to component is empty");
            return path;
        }
        if ( isBlank(ownerId) ){
            logger.debug("cannot perform reduce by owner, component owner is empty");
            return path;
        }
        //reduce by owner
        Map map = path.stream().collect( Collectors.toMap( it -> dropLast(it,PATH_DELIMITER) , Function.identity() , ( a , b ) ->  a.endsWith(ownerId) ? a : b ));
        //reduce list&duplicates and preserve order
        return path.stream().distinct().filter(it -> map.values().contains(it) ).collect(Collectors.toList());
    }

    private List<String> getReducedPath(List<String> path) {
        return path.stream().distinct().collect(Collectors.toList());
    }

    private boolean appendNameRecursively(Map<String, Component> componentsCache, Component originComponent, Iterator<String> instanceIdIter, StringBuilder substitutedName) {
        if(isNotEmpty(originComponent.getComponentInstances()) && instanceIdIter.hasNext() && ToscaUtils.isNotComplexVfc(originComponent)){
            String ownerId = instanceIdIter.next();
            Optional<ComponentInstance> instanceOpt = originComponent.getComponentInstances().stream().filter(i -> i.getUniqueId().equals(ownerId)).findFirst();
            if(instanceOpt.isPresent()){
                substitutedName.append(instanceOpt.get().getNormalizedName()).append(PATH_DELIMITER);
                Either<Component, Boolean> getOriginRes = getOriginComponent(componentsCache, instanceOpt.get());
                if(getOriginRes.isRight()){
                    return false;
                }
                appendNameRecursively(componentsCache, getOriginRes.left().value(), instanceIdIter, substitutedName);
            } else if(CollectionUtils.isNotEmpty(originComponent.getGroups())){
                Optional<GroupDefinition> groupOpt = originComponent.getGroups().stream().filter(g -> g.getUniqueId().equals(ownerId)).findFirst();
                if(!groupOpt.isPresent()){
                    logger.debug("Failed to find an capability owner with uniqueId {} on a component with uniqueId {}", ownerId, originComponent.getUniqueId());
                    return false;
                }
                substitutedName.append(groupOpt.get().getNormalizedName()).append(PATH_DELIMITER);
            } else {
                logger.debug("Failed to find an capability owner with uniqueId {} on a component with uniqueId {}", ownerId, originComponent.getUniqueId());
                return false;
            }
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
            filter.setIgnoreCategories(false);
        }
        if(instance.getOriginType() == OriginTypeEnum.VF){
            filter.setIgnoreGroups(false);
        }
        return filter;
    }

}