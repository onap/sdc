/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.impl;


import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component("combinationBusinessLogic")
public class CombinationBusinessLogic extends BaseBusinessLogic {

    @Autowired
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    private static Logger log = Logger.getLogger(ToscaElementOperation.class.getName());

    public Either<Combination, ResponseFormat> createCombination(Combination combination, Service service) {

        Combination convertToCombination = buildServiceToCombination(service, combination);
        validateCombinationExists(combination);
        storeInDb(service, convertToCombination);
        return null;
    }

    private void validateCombinationExists(Combination combination) {
        Either<GraphVertex, TitanOperationStatus> eitherVertex = titanDao.getVertexById(combination.getUniqueId());
        if (eitherVertex.isLeft()) {
            log.debug("Combination already exists");
        }
    }

    private Combination buildServiceToCombination(Service service, Combination combination) {
        String uniqueId = combination.getName();
        try {
            combination.setUniqueId(uniqueId);
            combination.setComponentInstances(service.getComponentInstances());
            combination.setComponentInstancesRelations(service.getComponentInstancesRelations());
            combination.setComponentInstancesAttributes(service.getComponentInstancesAttributes());
            combination.setComponentInstancesProperties(service.getComponentInstancesProperties());
            combination.setComponentInstancesInputs(service.getComponentInstancesInputs());
        } catch (Exception e) {
            log.debug("failed to convert to combination");
        }

        return combination;
    }

    private void storeInDb(Service service, Combination combination) {

        GraphVertex combinationVertex = new GraphVertex();
        combinationVertex = fillMetadata(combinationVertex, combination, service);
        Either<GraphVertex, TitanOperationStatus> createdVertex = titanDao.createVertex(combinationVertex);
        if (createdVertex.isRight()) {
            log.debug("error");
        } else {
            //  associateToCatalogRoot(createdVertex.left().value());
        }
        titanDao.commit();
    }

    private GraphVertex fillMetadata(GraphVertex combinationVertex, Combination combination, Service service) {
        combinationVertex.setUniqueId(combination.getName());
        combinationVertex.setLabel(VertexTypeEnum.COMBINATION);

        String combinationJson;
        try {
            combinationJson = (String) RepresentationUtils.toRepresentation(combination);
            combinationVertex.setJsonString(combinationJson);
            return combinationVertex;
        } catch (Exception e) {
            log.debug(""+e);
            return null;
        }
    }

    private StorageOperationStatus associateToCatalogRoot(GraphVertex nodeTypeVertex) {
        Either<GraphVertex, TitanOperationStatus> catalog = titanDao.getVertexByLabel(VertexTypeEnum.CATALOG_ROOT);
        if (catalog.isRight()) {
            log.debug("Failed to fetch catalog vertex. error {}", catalog.right().value());
            return DaoStatusConverter.convertTitanStatusToStorageStatus(catalog.right().value());
        }
        TitanOperationStatus createEdge = titanDao.createEdge(catalog.left().value(), nodeTypeVertex, EdgeLabelEnum.CATALOG_ELEMENT, null);

        return DaoStatusConverter.convertTitanStatusToStorageStatus(createEdge);
    }

    public Either<ComponentInstance, ResponseFormat> createCombinationInstance
            (String containerComponentType, String containerComponentId, String userId, ComponentInstance componentInstance) {

        //Do validations
        boolean inTransaction = false;
        boolean needLock = true;
        Component containerComponent = null;
        Combination parentComponent;
        Either<ComponentInstance, ResponseFormat> resultOp = null;

        try {
            if (!containerComponentType.equals("services")) {
                log.error("Container component is not of service type");
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }

            User user = validateUserExists(userId, "create Component Instance", inTransaction);
            Either<Component, ResponseFormat> validateComponentExists =
                    validateComponentExists(containerComponentId, ComponentTypeEnum.SERVICE, null);
            if (validateComponentExists.isRight()) {
                return Either.right(validateComponentExists.right().value());
            } else {
                containerComponent = validateComponentExists.left().value();
            }
            Either<Combination, ResponseFormat> getOriginComponentRes = getCombination(componentInstance);

            if (getOriginComponentRes.isRight()) {
                return Either.right(getOriginComponentRes.right().value());
            } else {
                parentComponent = getOriginComponentRes.left().value();
            }

            if (needLock) {
                Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "createComponentInstance");
                if (lockComponent.isRight()) {
                    return Either.right(lockComponent.right().value());
                }
            }
            log.debug("Try to create entry on graph");
            resultOp = handleCombinationInstance(containerComponent, parentComponent, componentInstance, user);
            return resultOp;

        } finally {
            if (needLock)
                unlockComponent(resultOp, containerComponent);
        }
    }

    private Either<Combination, ResponseFormat> getCombination(ComponentInstance componentInstance) {

        Either<GraphVertex, TitanOperationStatus> graphVertexEither = titanDao.getVertexById(componentInstance.getComponentUid());
        if (graphVertexEither.isRight()) {
            log.error("failed  to get Vertex");
        }
        GraphVertex graphVertex = graphVertexEither.left().value();
        String combinationJson = graphVertex.getJsonString();
        Combination combination = RepresentationUtils.fromRepresentation(combinationJson, Combination.class);

        return Either.left(combination);
    }

    private Either<ComponentInstance, ResponseFormat> handleCombinationInstance
            (Component containerComponent, Combination parentComponent, ComponentInstance jsonObject,
             User user) {

        final String containerComponentId = containerComponent.getUniqueId();
        boolean allowDeleted = false;
        Either<Component, StorageOperationStatus> componentRes;
        Either<ImmutablePair<Component, String>, StorageOperationStatus> addResult;
        log.info("Start to handle {} combination component instance", parentComponent.getName());
        List<ComponentInstance> parentComponentInstances = parentComponent.getComponentInstances();
        List<RequirementCapabilityRelDef> parentComponentRelations = parentComponent.getComponentInstancesRelations();
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = parentComponent.getComponentInstancesProperties();
        Position jsonObjPosition = new Position(jsonObject.getPosX(), jsonObject.getPosY());
        initPositionBoundaries(jsonObjPosition, parentComponentInstances);

        Map<String, List<ComponentInstanceProperty>> newComponentInstancesProperties = new HashMap<>();
        for (Map.Entry<String, List<ComponentInstanceProperty>> entry : componentInstancesProperties.entrySet()) {
            log.debug("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            newComponentInstancesProperties.put(replaceOldContainerId(containerComponent.getUniqueId(), entry.getKey()), entry.getValue());
        }

        //handle components
        try {
            for (ComponentInstance componentInstance : parentComponentInstances) {
                String id = replaceOldContainerId(containerComponentId, componentInstance.getUniqueId());
                componentInstance.setUniqueId(id);
                componentRes = toscaOperationFacade.getToscaElement(componentInstance.getComponentUid());
                if (componentRes.isRight()) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
                }
                Component childComponent = componentRes.left().value();
                componentRes = toscaOperationFacade.getToscaElement(containerComponent.getUniqueId());
                if (componentRes.isRight()) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
                }
                containerComponent = componentRes.left().value();
                //calculateNewPosition(jsonObjPosition, componentInstance);
                addResult = toscaOperationFacade
                        .addComponentInstanceToTopologyTemplate(containerComponent, childComponent, componentInstance, allowDeleted, user);
                if (addResult.isRight()) {
                    log.debug("Failed to connect resources");
                }
                else {
                    titanDao.commit();
                }
            }
        } finally {
            Either<List<ComponentInstance>, ResponseFormat> resultOp = Either.left(parentComponentInstances);
            unlockComponent(resultOp, containerComponent);
        }
        //handle component relations
        for (RequirementCapabilityRelDef relDef : parentComponentRelations) {
            mergeContainerIdWithRelations(containerComponent.getUniqueId(), relDef);
            String rl = relDef.getRelationships().get(0).getRelation().getRelationship().getType();
            if (rl.equals("tosca.relationships.network.LinksTo")) {
                relDef.getRelationships().get(0).getRelation().getRelationship().setType("tosca.capabilities.network.Linkable");
            }

            Either<RequirementCapabilityRelDef, ResponseFormat> associateRelations = componentInstanceBusinessLogic.
                    associateRIToRI(containerComponent.getUniqueId(), user.getUserId(), relDef,
                            ComponentTypeEnum.SERVICE, true, false, true);
            if (associateRelations.isRight()) {
                log.debug("Failed to connect resources");
            }
        }
        log.info("Finished to handle {} combination component instance", parentComponent.getName());

        //handle component properties
        for (ComponentInstance componentInstance : parentComponentInstances) {

            Either<List<ComponentInstanceProperty>, ResponseFormat> updateProperties = componentInstanceBusinessLogic.
                    createOrUpdatePropertiesValues(ComponentTypeEnum.SERVICE, containerComponent.getUniqueId(),
                            componentInstance.getUniqueId(), newComponentInstancesProperties.get
                                    (componentInstance.getUniqueId()), user.getUserId());
            if (updateProperties.isRight()) {
                log.debug("Failed to connect resources");
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }

        }
        return null;
    }

    private void mergeContainerIdWithRelations(String containerUniqueId, RequirementCapabilityRelDef relation) {

        //Fix fromNode
        String newId = replaceOldContainerId(containerUniqueId, relation.getFromNode());
        relation.setFromNode(newId);

        //Fix toNode
        newId = replaceOldContainerId(containerUniqueId, relation.getFromNode());
        relation.setToNode(newId);

        //Fix Capability Owner Id
        newId = replaceOldContainerId(containerUniqueId, relation.getRelationships().get(0).getRelation().getCapabilityOwnerId());
        relation.getRelationships().get(0).getRelation().setCapabilityOwnerId(newId);

        //Fix RequirementOwnerId
        newId = replaceOldContainerId(containerUniqueId, relation.getRelationships().get(0).getRelation().getRequirementOwnerId());
        relation.getRelationships().get(0).getRelation().setRequirementOwnerId(newId);
    }

    private String replaceOldContainerId(String containerUniqueId, String id) {
        int index = id.indexOf('.');
        String substring = id.substring(index);
        return containerUniqueId + substring;
    }

    private void calculateNewPosition(Position position, ComponentInstance c) {
        Double newPosX = Math.abs(Double.parseDouble(c.getPosX()) - position.getOrigMinPosX()) + position.getPagePosX();
        Double newPosY = Math.abs(Double.parseDouble(c.getPosY()) - position.getOrigMinPosY()) + position.getPagePosY();

        position.setNewMaxPosX(Math.max(position.getNewMaxPosX(), newPosX));
        position.setNewMaxPosY(Math.max(position.getNewMaxPosY(), newPosY));

        position.setNewMinPosX(Math.min(position.getNewMinPosX(), newPosX));
        position.setNewMinPosY(Math.min(position.getNewMinPosY(), newPosY));

        c.setPosX(newPosX.toString());
        c.setPosY(newPosY.toString());
    }

    private void initPositionBoundaries (Position position, List<ComponentInstance> componentInstances) {
        position.setOrigMaxPosX(-Double.MAX_VALUE);
        position.setOrigMaxPosY(-Double.MAX_VALUE);
        position.setNewMaxPosX(-Double.MAX_VALUE);
        position.setNewMaxPosY(-Double.MAX_VALUE);

        for (ComponentInstance c : componentInstances) {
            position.setOrigMaxPosX(Math.max(position.getOrigMaxPosX(), Double.parseDouble(c.getPosX())));
            position.setOrigMaxPosY(Math.max(position.getOrigMaxPosY(), Double.parseDouble(c.getPosY())));
            position.setOrigMinPosX(Math.min(position.getOrigMinPosX(), Double.parseDouble(c.getPosX())));
            position.setOrigMinPosY(Math.min(position.getOrigMinPosY(), Double.parseDouble(c.getPosY())));
        }
    }
}