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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Combination;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Position;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.CombinationOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.ui.model.UiCombination;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@org.springframework.stereotype.Component("combinationBusinessLogic")
public class CombinationBusinessLogic extends BaseBusinessLogic {

    private static final Logger log = Logger.getLogger(CombinationBusinessLogic.class);
    public static final String NATIVE_NETWORK_LINKABLE = "tosca.capabilities.network.Linkable";
    public static final String NATIVE_NETWORK_LINK_TO = "tosca.relationships.network.LinksTo";

    @Autowired
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @Autowired
    private ServiceBusinessLogic serviceBusinessLogic;

    public void setserviceBusinessLogic(ServiceBusinessLogic serviceBusinessLogic)
    {
        this.serviceBusinessLogic=serviceBusinessLogic;
    }

    @Autowired
    protected CombinationOperation combinationOperation;

    public Either<Combination, ResponseFormat> createCombination(UiCombination uiCombination, String serviceId, String userId) {

        try {
            User modifier = new User();
            modifier.setUserId(userId);

            Either<Service, ResponseFormat> getServiceResponse = serviceBusinessLogic.getService(serviceId.toLowerCase(), modifier);
            if (getServiceResponse.isRight()) {
                log.debug("failed to get service");

                return Either.right(componentsUtils.getResponseFormat(ActionStatus.SERVICE_NOT_FOUND));
            }
            Service service = getServiceResponse.left().value();



            Combination combination = new Combination(uiCombination);
            combination.setUniqueId(UniqueIdBuilder.buildCombinationUniqueid(service.getUniqueId()));

            Either<Boolean, ResponseFormat> validateCombinationExists = validateCombinationExists(combination.getUniqueId());
            if (validateCombinationExists.isRight()) {
                log.debug("Failed to validate");
                return Either.right(validateCombinationExists.right().value());
            }
            if (validateCombinationExists.left().value()) {
                log.debug("Combination Already exists");
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST));
            }

            Either<Combination, ResponseFormat> convertEither = buildServiceToCombination(service, combination);
            if (convertEither.isRight()) {
                log.debug("Failed to convert to combination");
                return convertEither;
            }
            Combination validatedCombination = convertEither.left().value();
            String combinationJson = (String) RepresentationUtils.toRepresentation(validatedCombination);
            Either <Combination, StorageOperationStatus> storeCombination = combinationOperation.createCombinationElement
                    (service, validatedCombination, combinationJson);

            if (storeCombination.isRight()) {
                titanDao.rollback();
                log.debug("Failed to store combination");
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)));
            }
            titanDao.commit();
            Combination combinationElement = storeCombination.left().value();
            return Either.left(combinationElement);
        } catch (Exception e) {
            log.debug("create resource failed with exception", e);
            ResponseFormat errorResponse =componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            return  Either.right(errorResponse);
        }

    }

    public Either<Boolean, ResponseFormat> validateCombinationExists(String combinationId) {
        try {
            Either<GraphVertex, TitanOperationStatus> eitherVertex = titanDao.getVertexById(combinationId);
            if (eitherVertex.isLeft()) {
                log.debug("Combination already exists");
                return Either.left(true);
            }
            else {
                return Either.left(false);
            }
        } catch (Exception e) {
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)));
        }
    }

    private Either<Combination, ResponseFormat> buildServiceToCombination(Service service, Combination combination) {

            if(CollectionUtils.isEmpty(service.getComponentInstances())) {

                log.error("The service is empty");
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM));
            }
            combination.setComponentInstances(service.getComponentInstances());
            combination.setComponentInstancesRelations(service.getComponentInstancesRelations());
            combination.setComponentInstancesAttributes(service.getComponentInstancesAttributes());
            combination.setComponentInstancesProperties(service.getComponentInstancesProperties());
            combination.setComponentInstancesInputs(service.getComponentInstancesInputs());
            return Either.left(combination);

    }
  
    public void setCombinationOperation(CombinationOperation combinationOperation) {
        this.combinationOperation = combinationOperation;
    }

    public Either<Service, ResponseFormat> getService(String serviceId, User modifier) {
        return serviceBusinessLogic.getService(serviceId.toLowerCase(), modifier);
    }

    public Either<List<UiCombination>, ResponseFormat> getAllCombinations() {
        Either<List<String>, TitanOperationStatus> combinationStringListEither = combinationOperation.getAllCombinations();
        List<UiCombination> uiCombinationList = new ArrayList<>();
        if (combinationStringListEither.isRight()) {
            log.error("Failed to get all Combinations");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat
                    (componentsUtils.convertFromStorageResponse
                            (DaoStatusConverter.convertTitanStatusToStorageStatus
                                    (combinationStringListEither.right().value())));
            return Either.right(responseFormat);
        }
        List<String> combinationStringList = combinationStringListEither.left().value();
        try {
            for (String combinationJson : combinationStringList) {
                Combination combination = RepresentationUtils.fromRepresentation(combinationJson, Combination.class);
                UiCombination uiCombination = new UiCombination();
                uiCombination.setUniqueId(combination.getUniqueId());
                uiCombination.setName(combination.getName());
                uiCombination.setDescription(combination.getDesc());
                uiCombinationList.add(uiCombination);
            }
            return Either.left(uiCombinationList);
        } catch (Exception e) {
            log.error("Failed to Convert to Combination Object");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }
    public Either<Combination, ResponseFormat> getCombinationById (String combinationId) {

        Either<String, ResponseFormat> getCombinationEither = combinationOperation.getCombination(combinationId);
        if (getCombinationEither.isRight()) {
            return Either.right(getCombinationEither.right().value());
        }
        String combinationJson = getCombinationEither.left().value();
        Combination combination = RepresentationUtils.fromRepresentation(combinationJson, Combination.class);
        return Either.left(combination);
    }

    public ResponseFormat createCombinationInstance
            (String containerComponentType, String containerComponentId, String userId, ComponentInstance componentInstance) {

        boolean inTransaction = false;
        Component containerComponent;
        Combination combination;

        try {
            if (!containerComponentType.equals("services")) {
                log.error("Container component is not of service type");
                return componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            }

            User user = validateUserExists(userId, "create Component Instance", inTransaction);
            Either<Component, ResponseFormat> validateComponentExists =
                    validateComponentExists(containerComponentId, ComponentTypeEnum.SERVICE, null);
            if (validateComponentExists.isRight()) {
                return validateComponentExists.right().value();
            } else {
                containerComponent = validateComponentExists.left().value();
            }
            Either<Combination, ResponseFormat> getOriginComponentRes = getCombinationById (componentInstance.getComponentUid());

            if (getOriginComponentRes.isRight()) {
                return getOriginComponentRes.right().value();
            } else {
                combination = getOriginComponentRes.left().value();
            }

            log.debug("Try to create entry on graph");
            return handleCombinationInstance(containerComponent, combination, componentInstance, user);
        } catch (Exception e) {
            log.error("Exception Occurred");
            return componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
        }
    }

    private ResponseFormat handleCombinationInstance
            (Component containerComponent, Combination combination, ComponentInstance jsonObject,
             User user) {

        String containerComponentId = containerComponent.getUniqueId();
        Map<String, String> latestComponentCounterMap = new HashMap<>();
        String origId = "";

        Either<Component, StorageOperationStatus> componentRes;
        List<ComponentInstance> componentInstances = combination.getComponentInstances();
        List<RequirementCapabilityRelDef> componentInstancesRelations = combination.getComponentInstancesRelations();
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = combination.getComponentInstancesProperties();
        Position jsonObjPosition = new Position(jsonObject.getPosX(), jsonObject.getPosY());
        initPositionBoundaries(jsonObjPosition, componentInstances);

        // Creating map with new Service ID
        Map<String, List<ComponentInstanceProperty>> newComponentInstancesProperties = new HashMap<>();


        log.info("Start to handle {} combination component instance", combination.getName());
        try {
            //handle components
            Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "createCombinationInstance");
            if (lockComponent.isRight()) {
                return lockComponent.right().value();
            }
            for (ComponentInstance componentInstance : componentInstances) {
                origId = componentInstance.getUniqueId().substring(0,componentInstance.getUniqueId().indexOf('.'));
                String id = replaceOldContainerId(containerComponentId, componentInstance.getUniqueId());
                componentInstance.setUniqueId(id);
                componentRes = toscaOperationFacade.getToscaElement(componentInstance.getComponentUid());
                if (componentRes.isRight()) {
                    return componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                }
                Component childComponent = componentRes.left().value();
                componentRes = toscaOperationFacade.getToscaElement(containerComponent.getUniqueId());
                if (componentRes.isRight()) {
                    return componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                }
                containerComponent = componentRes.left().value();
                calculateNewPosition(jsonObjPosition, componentInstance);
                String componentName = (String) componentInstance.getToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_NAME);
                componentInstance.setToscaPresentationValue(JsonPresentationFields.NAME, componentName);
                componentInstance.setCustomizationUUID(UUID.randomUUID().toString());
                String counter = toscaOperationFacade.getLastestComponentInstanceCounter(containerComponent, childComponent.getName());
                latestComponentCounterMap.put(childComponent.getNormalizedName(), counter);
                Either<ImmutablePair<Component, String>, StorageOperationStatus> associateComponent = toscaOperationFacade
                        .addComponentInstanceToTopologyTemplate(containerComponent, childComponent,
                                componentInstance, false, user);
                if (associateComponent.isRight()) {
                    titanDao.rollback();
                    return componentsUtils.getResponseFormat
                            (componentsUtils.convertFromStorageResponse(associateComponent.right().value()));
                } else {
                    titanDao.commit();
                }
            }
            log.info("Finished to handle {} combination component instances", combination.getName());
            Either<List<ComponentInstance>, ResponseFormat> resultOp = Either.left(componentInstances);
            unlockComponent(resultOp, containerComponent);

            //handle component relations
            for (RequirementCapabilityRelDef relDef : componentInstancesRelations) {

                updateRelations (containerComponent, relDef, latestComponentCounterMap);

                Either<RequirementCapabilityRelDef, ResponseFormat> associateRelations = componentInstanceBusinessLogic.
                        associateRIToRI(containerComponent.getUniqueId(), user.getUserId(), relDef,
                                ComponentTypeEnum.SERVICE, true, true, true);

                if (associateRelations.isRight()) {
                    return associateRelations.right().value();
                }
            }
            log.info("Finished to handle {} combination component relations", combination.getName());

            log.info("Start to handle {} combination component properties", combination.getName());

            for (ComponentInstance componentInstance : componentInstances) {
                List<ComponentInstanceProperty> prop = null;
                String id = componentInstance.getUniqueId();
                id = origId + id.substring(id.indexOf('.'), id.length()-1) + '0';
                prop = componentInstancesProperties.get(id);
                newComponentInstancesProperties.put(componentInstance.getUniqueId(), prop);
            }

            //handle component properties
            for (ComponentInstance componentInstance : componentInstances) {

                Either<List<ComponentInstanceProperty>, ResponseFormat> updateProperties = componentInstanceBusinessLogic.
                        createOrUpdatePropertiesValues(ComponentTypeEnum.SERVICE, containerComponent.getUniqueId(),
                                componentInstance.getUniqueId(), newComponentInstancesProperties.get
                                        (componentInstance.getUniqueId()), user.getUserId());
                if (updateProperties.isRight()) {
                    log.debug("Failed to add properties");
                    return updateProperties.right().value();
                }
            }
        } catch (Exception e) {
            log.debug("Exception occured");
            return componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
        }
        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    private void updateRelations(Component containerComponent, RequirementCapabilityRelDef relDef, Map<String, String> latestComponentCounterMap) {

        String[] fromNode = relDef.getFromNode().split("\\.");
        String[] toNode = relDef.getToNode().split("\\.");
        String id;
        String[] capabilityOwnerId = relDef.getRelationships().get(0).getRelation().getCapabilityOwnerId().split("\\.");
        String[] requirementOwnerId = relDef.getRelationships().get(0).getRelation().getRequirementOwnerId().split("\\.");
        for (Map.Entry<String, String> entry : latestComponentCounterMap.entrySet()) {
            log.debug("Key = " + entry.getKey() + ", Value = " + entry.getValue());

            if (fromNode[2].contains(entry.getKey())) {
                id = fromNode[2].substring(0, fromNode[2].length() - 1) + entry.getValue();
                relDef.setFromNode(fromNode[0] + "." + fromNode[1] + "." + id);
            }
            if (toNode[2].contains(entry.getKey())) {
                id = toNode[2].substring(0, toNode[2].length() - 1) + entry.getValue();
                relDef.setToNode(toNode[0] + "." + toNode[1] + "." + id);
            }
            if (capabilityOwnerId[2].contains(entry.getKey())) {
                id = capabilityOwnerId[2].substring(0, capabilityOwnerId[2].length() - 1) + entry.getValue();
                relDef.getRelationships().get(0).getRelation().setCapabilityOwnerId(capabilityOwnerId[0] + "." + capabilityOwnerId[1] + "." + id);
            }
            if (requirementOwnerId[2].contains(entry.getKey())) {
                id = requirementOwnerId[2].substring(0, requirementOwnerId[2].length() - 1) + entry.getValue();
                relDef.getRelationships().get(0).getRelation().setRequirementOwnerId(requirementOwnerId[0] + "." + requirementOwnerId[1] + "." + id);
            }
        }

        mergeContainerIdWithRelations(containerComponent.getUniqueId(), relDef);
        String rl = relDef.getRelationships().get(0).getRelation().getRelationship().getType();
        if (rl.equals(NATIVE_NETWORK_LINK_TO)) {
            relDef.getRelationships().get(0).getRelation().getRelationship().setType(NATIVE_NETWORK_LINKABLE);
        }
    }

    private void mergeContainerIdWithRelations(String containerUniqueId, RequirementCapabilityRelDef relation) {

        //Fix fromNode
        String newId = replaceOldContainerId(containerUniqueId, relation.getFromNode());
        relation.setFromNode(newId);

        //Fix toNode
        newId = replaceOldContainerId(containerUniqueId, relation.getToNode());
        relation.setToNode(newId);

        //Fix Capability Owner Id
        newId = replaceOldContainerId(containerUniqueId, relation.getRelationships().get(0).getRelation().getCapabilityOwnerId());
        relation.getRelationships().get(0).getRelation().setCapabilityOwnerId(newId);

        //Fix RequirementOwnerId
        newId = replaceOldContainerId(containerUniqueId, relation.getRelationships().get(0).getRelation().getRequirementOwnerId());
        relation.getRelationships().get(0).getRelation().setRequirementOwnerId(newId);
    }

    private String replaceOldContainerId(String containerUniqueId, String id) {
        return containerUniqueId + id.substring(id.indexOf('.'));
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
