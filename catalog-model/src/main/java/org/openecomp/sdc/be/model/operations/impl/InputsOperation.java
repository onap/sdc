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

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.operations.api.IInputsOperation;
import org.openecomp.sdc.be.resources.data.*;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("input-operation")
public class InputsOperation extends AbstractOperation implements IInputsOperation {

    private static final Logger log = Logger.getLogger(InputsOperation.class.getName());
    @Autowired
    PropertyOperation propertyOperation;

    public <ElementDefinition> JanusGraphOperationStatus findAllResourceElementsDefinitionRecursively(String resourceId, List<ElementDefinition> elements, NodeElementFetcher<ElementDefinition> singleNodeFetcher) {

        log.trace("Going to fetch elements under resource {}" , resourceId);
        JanusGraphOperationStatus
            resourceAttributesStatus = singleNodeFetcher.findAllNodeElements(resourceId, elements);

        if (resourceAttributesStatus != JanusGraphOperationStatus.OK) {
            return resourceAttributesStatus;
        }

        Either<ImmutablePair<ResourceMetadataData, GraphEdge>, JanusGraphOperationStatus> parentNodes = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource, ResourceMetadataData.class);

        if (parentNodes.isRight()) {
            JanusGraphOperationStatus parentNodesStatus = parentNodes.right().value();
            if (parentNodesStatus != JanusGraphOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance().logInternalFlowError("findAllResourceElementsDefinitionRecursively", "Failed to find parent elements of resource " + resourceId + ". status is " + parentNodesStatus, ErrorSeverity.ERROR);
                return parentNodesStatus;
            }
        }

        if (parentNodes.isLeft()) {
            ImmutablePair<ResourceMetadataData, GraphEdge> parnetNodePair = parentNodes.left().value();
            String parentUniqueId = parnetNodePair.getKey().getMetadataDataDefinition().getUniqueId();
            JanusGraphOperationStatus addParentIntStatus = findAllResourceElementsDefinitionRecursively(parentUniqueId, elements, singleNodeFetcher);

            if (addParentIntStatus != JanusGraphOperationStatus.OK) {
                BeEcompErrorManager.getInstance().logInternalFlowError("findAllResourceElementsDefinitionRecursively", "Failed to find all resource elements of resource " + parentUniqueId, ErrorSeverity.ERROR);

                return addParentIntStatus;
            }
        }
        return JanusGraphOperationStatus.OK;
    }


    @Override
    public ImmutablePair<JanusGraphOperationStatus, String> findInputValue(String resourceInstanceId, String propertyId) {

        log.debug("Going to check whether the property {} already added to resource instance {}", propertyId, resourceInstanceId);

        Either<List<ComponentInstanceInput>, JanusGraphOperationStatus> getAllRes = getAllInputsOfResourceInstanceOnlyInputDefId(resourceInstanceId);
        if (getAllRes.isRight()) {
            JanusGraphOperationStatus status = getAllRes.right().value();
            log.trace("After fetching all properties of resource instance {}. Status is {}" ,resourceInstanceId, status);
            return new ImmutablePair<>(status, null);
        }

        List<ComponentInstanceInput> list = getAllRes.left().value();
        if (list != null) {
            for (ComponentInstanceInput instanceProperty : list) {
                String propertyUniqueId = instanceProperty.getUniqueId();
                String valueUniqueUid = instanceProperty.getValueUniqueUid();
                log.trace("Go over property {} under resource instance {}. valueUniqueId = {}" ,propertyUniqueId, resourceInstanceId, valueUniqueUid);
                if (propertyId.equals(propertyUniqueId) && valueUniqueUid != null) {
                    log.debug("The property {} already created under resource instance {}", propertyId, resourceInstanceId);
                    return new ImmutablePair<>(JanusGraphOperationStatus.ALREADY_EXIST, valueUniqueUid);
                }
            }
        }

        return new ImmutablePair<>(JanusGraphOperationStatus.NOT_FOUND, null);
    }

    /**
     * return all properties associated to resource instance. The result does contains the property unique id but not its type, default value...
     *
     * @param resourceInstanceUid
     * @return
     */
    public Either<List<ComponentInstanceInput>, JanusGraphOperationStatus> getAllInputsOfResourceInstanceOnlyInputDefId(String resourceInstanceUid) {

        return getAllInputsOfResourceInstanceOnlyInputDefId(resourceInstanceUid, NodeTypeEnum.ResourceInstance);

    }

    public Either<List<ComponentInstanceInput>, JanusGraphOperationStatus> getAllInputsOfResourceInstanceOnlyInputDefId(String resourceInstanceUid, NodeTypeEnum instanceNodeType) {

        Either<ComponentInstanceData, JanusGraphOperationStatus> findResInstanceRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(instanceNodeType), resourceInstanceUid, ComponentInstanceData.class);

        if (findResInstanceRes.isRight()) {
            JanusGraphOperationStatus status = findResInstanceRes.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.INVALID_ID;
            }
            return Either.right(status);
        }

        Either<List<ImmutablePair<InputValueData, GraphEdge>>, JanusGraphOperationStatus> propertyImplNodes = janusGraphGenericDao
            .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(instanceNodeType), resourceInstanceUid, GraphEdgeLabels.INPUT_VALUE, NodeTypeEnum.InputValue, InputValueData.class);

        if (propertyImplNodes.isRight()) {
            JanusGraphOperationStatus status = propertyImplNodes.right().value();
            return Either.right(status);
        }

        List<ImmutablePair<InputValueData, GraphEdge>> list = propertyImplNodes.left().value();
        if (list == null || list.isEmpty()) {
            return Either.right(JanusGraphOperationStatus.NOT_FOUND);
        }

        List<ComponentInstanceInput> result = new ArrayList<>();


        for (ImmutablePair<InputValueData, GraphEdge> propertyValueDataPair : list) {

            InputValueData propertyValueData = propertyValueDataPair.getLeft();
            String propertyValueUid = propertyValueData.getUniqueId();
            String value = propertyValueData.getValue();

            Either<ImmutablePair<InputsData, GraphEdge>, JanusGraphOperationStatus> inputNodes = janusGraphGenericDao
                .getParentNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), propertyValueData.getUniqueId(), GraphEdgeLabels.GET_INPUT, NodeTypeEnum.Input, InputsData.class);

            if (inputNodes.isRight()) {

                return Either.right(inputNodes.right().value());
            }

            InputsData input = inputNodes.left().value().left;
            String inputId = input.getPropertyDataDefinition().getUniqueId();

            Either<ImmutablePair<PropertyData, GraphEdge>, JanusGraphOperationStatus> propertyDefRes = janusGraphGenericDao
                .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.InputValue), propertyValueUid, GraphEdgeLabels.INPUT_IMPL, NodeTypeEnum.Property, PropertyData.class);
            if (propertyDefRes.isRight()) {
                JanusGraphOperationStatus status = propertyDefRes.right().value();
                if (status == JanusGraphOperationStatus.NOT_FOUND) {
                    status = JanusGraphOperationStatus.INVALID_ID;
                }
                return Either.right(status);
            }

            ImmutablePair<PropertyData, GraphEdge> propertyDefPair = propertyDefRes.left().value();
            PropertyData propertyData = propertyDefPair.left;
            Either<Edge, JanusGraphOperationStatus> inputsEges = janusGraphGenericDao
                .getIncomingEdgeByCriteria(propertyData, GraphEdgeLabels.INPUT, null);
            if (inputsEges.isRight()) {
                JanusGraphOperationStatus status = inputsEges.right().value();

                return Either.right(status);
            }
            Edge edge = inputsEges.left().value();
            String inputName = (String) janusGraphGenericDao
                .getProperty(edge, GraphEdgePropertiesDictionary.NAME.getProperty());

            ComponentInstanceInput resourceInstanceProperty = new ComponentInstanceInput(propertyData.getPropertyDataDefinition(), inputId, value, propertyValueUid);

            resourceInstanceProperty.setName(inputName);
            resourceInstanceProperty.setParentUniqueId(inputId);
            resourceInstanceProperty.setValue(value);
            resourceInstanceProperty.setValueUniqueUid(propertyValueData.getUniqueId());
            resourceInstanceProperty.setType(propertyData.getPropertyDataDefinition().getType());
            resourceInstanceProperty.setSchema(propertyData.getPropertyDataDefinition().getSchema());
            resourceInstanceProperty.setComponentInstanceId(resourceInstanceUid);

            result.add(resourceInstanceProperty);
        }


        return Either.left(result);
    }

    @Override
    public ComponentInstanceInput buildResourceInstanceInput(InputValueData propertyValueData, ComponentInstanceInput resourceInstanceInput) {

        String value = propertyValueData.getValue();
        String uid = propertyValueData.getUniqueId();
        ComponentInstanceInput instanceProperty = new ComponentInstanceInput(resourceInstanceInput, value, uid);
        instanceProperty.setPath(resourceInstanceInput.getPath());

        return instanceProperty;
    }


}
