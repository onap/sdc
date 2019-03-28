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

package org.openecomp.sdc.be.model.operations.impl;

import static org.springframework.util.CollectionUtils.isEmpty;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.HealingTitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component("capability-operation")
public class CapabilityOperation extends AbstractOperation {


    private static final Logger log = Logger.getLogger(CapabilityOperation.class.getName());

    private final CapabilityTypeOperation capabilityTypeOperation;
    private final PropertyOperation propertyOperation;
    

    public CapabilityOperation(CapabilityTypeOperation capabilityTypeOperation, PropertyOperation propertyOperation) {
        this.capabilityTypeOperation = capabilityTypeOperation;
        this.propertyOperation = propertyOperation;
    }
    
    
    @VisibleForTesting
    public void setTitanGenericDao(HealingTitanGenericDao titanGenericDao) {
        this.titanGenericDao = titanGenericDao;
    }

    public Either<CapabilityData, TitanOperationStatus> addCapabilityToGraph(String resourceId, CapabilityTypeData capTypeData, CapabilityDefinition capabilityDefinition) {

        log.debug("#addCapabilityToGraph - capabilityDefinition={}", capabilityDefinition);

        String capUniqueId = UniqueIdBuilder.buildCapabilityUid(resourceId, capabilityDefinition.getName());
        CapabilityData capabilityData = buildCapabilityData(capabilityDefinition, capUniqueId);

        log.debug("addCapabilityToGraph - Before adding capability to graph. capabilityTypeData = {}", capabilityData);
        Either<CapabilityData, TitanOperationStatus> createCapResult = titanGenericDao.createNode(capabilityData, CapabilityData.class);
        log.debug("addCapabilityToGraph - After adding capability to graph. status is = {}", createCapResult);

        if (createCapResult.isRight()) {
            TitanOperationStatus operationStatus = createCapResult.right().value();
            log.error("addCapabilityToGraph - Failed to add capability of type {} to graph. status is {}", capabilityDefinition.getType(), operationStatus);
            return createCapResult;
        }
        
        createCapResult = connectToCapabilityType(capabilityData, capTypeData)
                                .left()
                                .bind(res -> createCapabilityProperties(capabilityData, capTypeData))
                                .left()
                                .map(res -> capabilityData);
        
        return createCapResult;
    }
    
    private Either<GraphRelation, TitanOperationStatus> connectToCapabilityType(CapabilityData capabilityData, CapabilityTypeData capabilityTypeData) {
        
        Map<String, Object> properties = new HashMap<>();

        String capabilityName = capabilityData.getCapabilityDataDefinition().getName();
        properties.put(GraphEdgePropertiesDictionary.NAME.getProperty(), capabilityName);
        
        return titanGenericDao.createRelation(capabilityData, capabilityTypeData, GraphEdgeLabels.CAPABILITY_IMPL, properties);

    }
    

    /**
     * @param capabilites
     * @return
     */
    public Either<List<CapabilityDefinition>, TitanOperationStatus> getCapabilitiesWithProps(List<ImmutablePair<CapabilityData, GraphEdge>> capabilites) {
        List<Either<CapabilityDefinition, TitanOperationStatus>> listFilledCapabilitiesResults = capabilites.stream()
                                                        .map(ImmutablePair::getLeft)
                                                        .map(this::toCapabilityDefinitionWithProps)
                                                        .collect(Collectors.toList());
        
        Optional<TitanOperationStatus> status = listFilledCapabilitiesResults.stream().filter(Either::isRight)
                                                               .map(res -> res.right().value())
                                                               .findFirst();
        
        if (status.isPresent()) {
            return Either.right(status.get());
        }
        
        List<CapabilityDefinition> listCapabilities = listFilledCapabilitiesResults.stream()
                                                                                    .map(res -> res.left().value())
                                                                                    .collect(Collectors.toList());
        
        return Either.left(listCapabilities);
    }
    
    private Either<CapabilityDefinition, TitanOperationStatus> toCapabilityDefinitionWithProps(CapabilityData capabilityData) {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition(capabilityData.getCapabilityDataDefinition());
        return getCapabilityProperties(capabilityDefinition.getUniqueId(), capabilityDefinition.getType())
                    .left()
                    .map(props -> {
                        capabilityDefinition.setProperties(props); 
                        return capabilityDefinition;
                    });
    }
    
        
    /**
     * get all properties of the capability.
     *
     * the property definition is taken from the capability type.
     *
     * @param capabilityUid
     * @return
     */
    private Either<List<ComponentInstanceProperty>, TitanOperationStatus> getCapabilityProperties(String capabilityUid, String capabilityType) {
        Either<CapabilityTypeDefinition, TitanOperationStatus> capabilityTypeRes = capabilityTypeOperation.getCapabilityTypeByType(capabilityType);

        if (capabilityTypeRes.isRight()) {
            TitanOperationStatus status = capabilityTypeRes.right().value();
            return Either.right(status);
        }

        CapabilityTypeDefinition capabilityTypeDefinition = capabilityTypeRes.left().value();

        Either<Map<String, PropertyDefinition>, TitanOperationStatus> typesPropsRes = getPropertiesOfCapabilityTypeAndAcestors(capabilityTypeDefinition);
        if (typesPropsRes.isRight()) {
            TitanOperationStatus status = typesPropsRes.right().value();
            return Either.right(status);
        }
        
        Map<String, PropertyDefinition> capabilityTypeProperties = typesPropsRes.left().value();

        if (isEmpty(capabilityTypeProperties)) {
            return Either.right(TitanOperationStatus.OK);
        }

        Map<String, PropertyDefinition> uidToPropDefMap = capabilityTypeProperties.values().stream()
                                                            .collect(Collectors.toMap(PropertyDefinition::getUniqueId, Function.identity()));

        // Find all properties values on the capability
        Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, TitanOperationStatus> propertyValNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability), capabilityUid, GraphEdgeLabels.PROPERTY_VALUE,
                NodeTypeEnum.PropertyValue, PropertyValueData.class);

        if (propertyValNodes.isRight()) {
            return onLoadPropValuesFailure(propertyValNodes.right().value(), capabilityTypeProperties);
        }

        List<ImmutablePair<PropertyValueData, GraphEdge>> propValsRelationPairs = propertyValNodes.left().value();
        if (isEmpty(propValsRelationPairs)) {
            return Either.right(TitanOperationStatus.OK);
        }

        List<ComponentInstanceProperty> capabilityProperties = new ArrayList<>();

        for (ImmutablePair<PropertyValueData, GraphEdge> propValRelPair : propValsRelationPairs) {

            PropertyValueData propertyValueData = propValRelPair.getLeft();
            Either<ImmutablePair<PropertyData, GraphEdge>, TitanOperationStatus> propertyDefRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PropertyValue), propertyValueData.getUniqueId(), GraphEdgeLabels.PROPERTY_IMPL,
                    NodeTypeEnum.Property, PropertyData.class);
            if (propertyDefRes.isRight()) {
                TitanOperationStatus status = propertyDefRes.right().value();
                if (status == TitanOperationStatus.NOT_FOUND) {
                    status = TitanOperationStatus.INVALID_ID;
                }
                return Either.right(status);
            }

            ImmutablePair<PropertyData, GraphEdge> propertyDefPair = propertyDefRes.left().value();
            PropertyData propertyData = propertyDefPair.left;
            String propertyUniqueId = propertyData.getPropertyDataDefinition().getUniqueId();

            PropertyDefinition propertyDefinition = uidToPropDefMap.get(propertyUniqueId);
            ComponentInstanceProperty capabilityProperty = new ComponentInstanceProperty(propertyDefinition, propertyValueData.getValue(), propertyValueData.getUniqueId());

            capabilityProperties.add(capabilityProperty);
        }
        
        Set<String> processedProps = buildProcessedPropsSet(capabilityProperties);

        // Find all properties which does not have property value on the group.
        List<ComponentInstanceProperty> leftProps = filterCapabilityTypesProps(capabilityTypeProperties, processedProps);
        if (leftProps != null) {
            capabilityProperties.addAll(leftProps);
        }

        return Either.left(capabilityProperties);
    }


    /**
     * @param capabilityProperties
     * @return
     */
    private Set<String> buildProcessedPropsSet(List<ComponentInstanceProperty> capabilityProperties) {
        return capabilityProperties.stream()
                                    .map(ComponentInstanceProperty::getName)
                                    .collect(Collectors.toSet());
    }
    
    private Either<List<ComponentInstanceProperty>, TitanOperationStatus> onLoadPropValuesFailure(TitanOperationStatus status, Map<String, PropertyDefinition> capabilityTypeProperties) {
        if (status == TitanOperationStatus.NOT_FOUND) {
            return Either.left(buildPropsFromCapabilityTypeProps(capabilityTypeProperties));
        } else {
            return Either.right(status);
        }
    }


    /**
     * @param capabilityTypeProperties
     * @return
     */
    private List<ComponentInstanceProperty> buildPropsFromCapabilityTypeProps(Map<String, PropertyDefinition> capabilityTypeProperties) {
        return capabilityTypeProperties.values().stream()
                                                    .map(p -> new ComponentInstanceProperty(p, p.getDefaultValue(), null))
                                                    .collect(Collectors.toList());
    }


    /**
     * @param capabilityTypeRes
     * @param capabilityTypeDefinition
     * @return
     */
    private Either<Map<String, PropertyDefinition>, TitanOperationStatus> getPropertiesOfCapabilityTypeAndAcestors(CapabilityTypeDefinition capabilityTypeDefinition) {
        // Get the properties on the group type of this capability
        Map<String, PropertyDefinition> capabilityTypeProperties = capabilityTypeDefinition.getProperties();
        
        String derivedFrom = capabilityTypeDefinition.getDerivedFrom();
        if (!Strings.isNullOrEmpty(derivedFrom)) {
            Either<Map<String, PropertyDefinition>, TitanOperationStatus> parentPropsRes = capabilityTypeOperation.getAllCapabilityTypePropertiesFromAllDerivedFrom(derivedFrom);
            if(parentPropsRes.isRight()) {
                TitanOperationStatus status = parentPropsRes.right().value();
                return Either.right(status);
            }
            if (capabilityTypeProperties != null) {
                capabilityTypeProperties.putAll(parentPropsRes.left().value());
            } else {
                capabilityTypeProperties = parentPropsRes.left().value();
            }
        }
        
        return Either.left(capabilityTypeProperties);
    }
    
    
    /**
     * Create all property values of the capability and their 
     * relations to relevant properties of the capability type.
     *
     * @param capabilityDefintion
     * @param capabilityTypeData
     * @return
     */
    private Either<List<ComponentInstanceProperty>, TitanOperationStatus> createCapabilityProperties(CapabilityData capabilityData,
                                                                                                     CapabilityTypeData capabilityTypeData) {

        CapabilityDefinition capabilityDefintion = (CapabilityDefinition)capabilityData.getCapabilityDataDefinition();
        CapabilityTypeDefinition capabilityTypeDefinition = (CapabilityTypeDefinition)capabilityTypeData.getCapabilityTypeDataDefinition();

        Either<Map<String, PropertyDefinition>, TitanOperationStatus> typesPropsRes = getPropertiesOfCapabilityTypeAndAcestors(capabilityTypeDefinition);
        if (typesPropsRes.isRight()) {
            TitanOperationStatus status = typesPropsRes.right().value();
            return Either.right(status);
        }
        
        Map<String, PropertyDefinition> capabilityTypeProperties = typesPropsRes.left().value();
        
        if (isEmpty(capabilityTypeProperties) && !isEmpty(capabilityDefintion.getProperties())) {
            log.debug("#createCapabilityProperties - It's not valid if group capability has properties while corresponding capability type doesn't.");
            return Either.right(TitanOperationStatus.MATCH_NOT_FOUND);
        }

        Optional<TitanOperationStatus> error = capabilityDefintion.getProperties().stream()
                             .map(property -> createPropertyValue(property, capabilityData, capabilityTypeProperties.get(property.getName())))
                             .filter(Either::isRight)
                             .map(result -> result.right().value())
                             .findFirst();
        if (error.isPresent()) {
            return Either.right(error.get());
        }

        return Either.left(capabilityDefintion.getProperties());
    }


    /**
     * @param capabilityTypeProperties
     * @param excludePropsWithUniqueIds
     * @return
     */
    private List<ComponentInstanceProperty> filterCapabilityTypesProps(Map<String, PropertyDefinition> capabilityTypeProperties, 
                                                                   Set<String> excludePropsWithNames) {
        return capabilityTypeProperties.values().stream()
                .filter(p -> !excludePropsWithNames.contains(p.getName()))
                .map(p -> new ComponentInstanceProperty(p, p.getDefaultValue(), null))
                .collect(Collectors.toList());
    }

    private  Either<PropertyValueData, TitanOperationStatus> createPropertyValue(ComponentInstanceProperty capabilityProperty, 
                                                                              CapabilityData capabilityData,
                                                                              PropertyDefinition capTypePropertyDefinition) {
        if (capTypePropertyDefinition == null) {
            return Either.right(TitanOperationStatus.MATCH_NOT_FOUND);
        }
        
        CapabilityDefinition capabilityDefintion = (CapabilityDefinition)capabilityData.getCapabilityDataDefinition();
        
        Either<Integer, StorageOperationStatus> indexRes = 
                propertyOperation.increaseAndGetObjInstancePropertyCounter(capabilityDefintion.getUniqueId(), NodeTypeEnum.Capability);
        String uniqueId = UniqueIdBuilder.buildResourceInstancePropertyValueUid(capabilityDefintion.getUniqueId(), indexRes.left().value() );
        PropertyValueData propertyValueData = new PropertyValueData();
        propertyValueData.setUniqueId(uniqueId);
        propertyValueData.setValue(capabilityProperty.getValue());
        Either<PropertyValueData, TitanOperationStatus> propResult = titanGenericDao.createNode(propertyValueData, PropertyValueData.class);
        // It's not accepted if Capability Type doesn't have suitable property
        propResult = propResult.left()
                .bind(propValueData -> connectToProperty(propValueData, capTypePropertyDefinition))
                .left()
                .bind(graphRelation -> connectCapability(propertyValueData, capTypePropertyDefinition.getName(), capabilityData))
                .left()
                .map(graphRelation -> propertyValueData);
        
        propResult.left()
                    .foreachDoEffect(propValueData -> capabilityProperty.setUniqueId(uniqueId));
        
        return propResult;
    }
    
    private Either<GraphRelation, TitanOperationStatus> connectCapability(PropertyValueData propValueData, String name, CapabilityData capabilityData) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(GraphEdgePropertiesDictionary.NAME.getProperty(), name);

        return titanGenericDao.createRelation(capabilityData, propValueData, GraphEdgeLabels.PROPERTY_VALUE, properties);
    }
    
    private Either<GraphRelation, TitanOperationStatus> connectToProperty(PropertyValueData propValueData, PropertyDefinition propertyDefinition) {
        Either<PropertyData, TitanOperationStatus> dataTypesRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), 
                                                                            propertyDefinition.getUniqueId(), PropertyData.class);

        Map<String, Object> properties = new HashMap<>();
        properties.put(GraphEdgePropertiesDictionary.NAME.getProperty(), propertyDefinition.getName());
        
        return dataTypesRes.left()
                           .bind(propertyData -> titanGenericDao.createRelation(propValueData, propertyData, GraphEdgeLabels.PROPERTY_IMPL, properties));
    }
    

    private CapabilityData buildCapabilityData(CapabilityDefinition capabilityDefinition, String ctUniqueId) {

        CapabilityData capabilityData = new CapabilityData(capabilityDefinition);

        capabilityData.setUniqueId(ctUniqueId);
        Long creationDate = capabilityData.getCreationTime();
        if (creationDate == null) {
            creationDate = System.currentTimeMillis();
        }
        capabilityData.setCreationTime(creationDate);
        capabilityData.setModificationTime(creationDate);
        return capabilityData;
    }


    public StorageOperationStatus deleteCapability(CapabilityDefinition capabilityDef) {
        
        return titanGenericDao.deleteChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), capabilityDef.getUniqueId(), GraphEdgeLabels.PROPERTY_VALUE,
                                                    NodeTypeEnum.PropertyValue, PropertyValueData.class)
                 .left()
                 .bind(props -> titanGenericDao.deleteNode(new CapabilityData(capabilityDef), CapabilityData.class))
                 .right()
                 .map(DaoStatusConverter::convertTitanStatusToStorageStatus)
                 .right()
                 .on(capData -> StorageOperationStatus.OK);
    }
    
}
