/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.be.components.attribute.AttributeDeclarationOrchestrator;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetOutputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstOutputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("outputsBusinessLogic")
public class OutputsBusinessLogic extends BaseBusinessLogic {

    private static final String CREATE_OUTPUT = "CreateOutput";
    private static final Logger log = Logger.getLogger(OutputsBusinessLogic.class);
    private static final String FAILED_TO_FOUND_COMPONENT_ERROR = "Failed to found component {}, error: {}";
    private static final String GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_GROUP = "Going to execute rollback on create group.";
    private static final String GOING_TO_EXECUTE_COMMIT_ON_CREATE_GROUP = "Going to execute commit on create group.";
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(OutputsBusinessLogic.class);
    private static final String FAILED_TO_FOUND_COMPONENT_INSTANCE_OUTPUTS_COMPONENT_INSTANCE_ID = "Failed to found component instance outputs componentInstanceId: {}";
    private static final String FAILED_TO_FOUND_COMPONENT_INSTANCE_OUTPUTS_ERROR = "Failed to found component instance outputs {}, error: {}";
    private final AttributeDeclarationOrchestrator attributeDeclarationOrchestrator;

    @Autowired
    public OutputsBusinessLogic(final IElementOperation elementDao, final IGroupOperation groupOperation,
                                final IGroupInstanceOperation groupInstanceOperation, final IGroupTypeOperation groupTypeOperation,
                                final InterfaceOperation interfaceOperation, final InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                final AttributeDeclarationOrchestrator attributeDeclarationOrchestrator,
                                final ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.attributeDeclarationOrchestrator = attributeDeclarationOrchestrator;
    }

    public Either<List<ComponentInstanceOutput>, ResponseFormat> getComponentInstanceOutputs(final String userId, final String componentId,
                                                                                             final String componentInstanceId) {
        validateUserExists(userId);
        final ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreOutputs(false);
        filters.setIgnoreComponentInstances(false);
        filters.setIgnoreComponentInstancesOutputs(false);
        final Either<Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);
        if (getComponentEither.isRight()) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, componentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        final Component component = getComponentEither.left().value();
        if (!ComponentValidations.validateComponentInstanceExist(component, componentInstanceId)) {
            final ActionStatus actionStatus = ActionStatus.COMPONENT_INSTANCE_NOT_FOUND;
            log.debug(FAILED_TO_FOUND_COMPONENT_INSTANCE_OUTPUTS_ERROR, componentInstanceId, actionStatus);
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_OUTPUTS, component.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                FAILED_TO_FOUND_COMPONENT_INSTANCE_OUTPUTS_COMPONENT_INSTANCE_ID, componentInstanceId);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        final Map<String, List<ComponentInstanceOutput>> ciOutputs = Optional.ofNullable(component.getComponentInstancesOutputs())
            .orElse(Collections.emptyMap());
        return Either.left(ciOutputs.getOrDefault(componentInstanceId, Collections.emptyList()));
    }

    @Override
    public Either<List<OutputDefinition>, ResponseFormat> declareAttributes(final String userId, final String componentId,
                                                                            final ComponentTypeEnum componentTypeEnum,
                                                                            final ComponentInstOutputsMap componentInstOutputsMap) {
        return createMultipleOutputs(userId, componentId, componentTypeEnum, componentInstOutputsMap, true, false);
    }

    private Either<List<OutputDefinition>, ResponseFormat> createMultipleOutputs(final String userId, final String componentId,
                                                                                 final ComponentTypeEnum componentType,
                                                                                 final ComponentInstOutputsMap componentInstOutputsMapUi,
                                                                                 final boolean shouldLockComp, final boolean inTransaction) {
        Either<List<OutputDefinition>, ResponseFormat> result = null;
        org.openecomp.sdc.be.model.Component component = null;
        try {
            validateUserExists(userId);
            component = getAndValidateComponentForCreate(userId, componentId, componentType, shouldLockComp);
            result = attributeDeclarationOrchestrator.declareAttributesToOutputs(component, componentInstOutputsMapUi).left()
                .bind(outputsToCreate -> prepareOutputsForCreation(userId, componentId, outputsToCreate)).right()
                .map(componentsUtils::getResponseFormat);
            return result;
        } catch (final ByResponseFormatComponentException e) {
            log.error("#createMultipleOutputs: Exception thrown: ", e);
            result = Either.right(e.getResponseFormat());
            return result;
        } finally {
            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.debug(GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_GROUP);
                    janusGraphDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_CREATE_GROUP);
                    janusGraphDao.commit();
                }
            }
            // unlock resource
            if (shouldLockComp && component != null) {
                graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
            }
        }
    }

    private org.openecomp.sdc.be.model.Component getAndValidateComponentForCreate(final String userId, final String componentId,
                                                                                  final ComponentTypeEnum componentType,
                                                                                  final boolean shouldLockComp) {
        final ComponentParametersView componentParametersView = getBaseComponentParametersView();
        final org.openecomp.sdc.be.model.Component component = validateComponentExists(componentId, componentType, componentParametersView);
        if (shouldLockComp) {
            // lock the component
            lockComponent(component, CREATE_OUTPUT);
        }
        validateCanWorkOnComponent(component, userId);
        return component;
    }

    private Either<List<OutputDefinition>, StorageOperationStatus> prepareOutputsForCreation(final String userId, final String cmptId,
                                                                                             final List<OutputDefinition> outputsToCreate) {
        final Map<String, OutputDefinition> outputsToPersist = MapUtil.toMap(outputsToCreate, OutputDefinition::getName);
        assignOwnerIdToOutputs(userId, outputsToPersist);
        return toscaOperationFacade.addOutputsToComponent(outputsToPersist, cmptId).left().map(persistedOutputs -> outputsToCreate);
    }

    private void assignOwnerIdToOutputs(final String userId, final Map<String, OutputDefinition> outputsToCreate) {
        outputsToCreate.values().forEach(outputDefinition -> outputDefinition.setOwnerId(userId));
    }

    private ComponentParametersView getBaseComponentParametersView() {
        final ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.disableAll();
        componentParametersView.setIgnoreOutputs(false);
        componentParametersView.setIgnoreAttributes(false);
        componentParametersView.setIgnoreComponentInstances(false);
        componentParametersView.setIgnoreComponentInstancesOutputs(false);
        componentParametersView.setIgnoreComponentInstancesAttributes(false);
        componentParametersView.setIgnoreUsers(false);
        return componentParametersView;
    }

    /**
     * Delete output from component
     *
     * @param componentId
     * @param userId
     * @param outputId
     * @return
     */
    public OutputDefinition deleteOutput(final String componentId, final String userId, final String outputId) {
        if (log.isDebugEnabled()) {
            log.debug("Going to delete output id: {}", outputId);
        }
        validateUserExists(userId);
        final ComponentParametersView componentParametersView = getBaseComponentParametersView();
        componentParametersView.setIgnoreAttributes(false);
        final Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentEither = toscaOperationFacade
            .getToscaElement(componentId, componentParametersView);
        if (componentEither.isRight()) {
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(componentEither.right().value()));
        }
        final org.openecomp.sdc.be.model.Component component = componentEither.left().value();
        // Validate outputId is child of the component
        final Optional<OutputDefinition> optionalOutput = component.getOutputs().stream().
            // filter by ID
                filter(output -> output.getUniqueId().equals(outputId)).
            // Get the output
                findAny();
        if (!optionalOutput.isPresent()) {
            throw new ByActionStatusComponentException(ActionStatus.OUTPUT_IS_NOT_CHILD_OF_COMPONENT, outputId, componentId);
        }
        final OutputDefinition outputForDelete = optionalOutput.get();
        // Lock component
        lockComponent(componentId, component, "deleteOutput");
        // Delete output operations
        boolean failed = false;
        try {
            final StorageOperationStatus status = toscaOperationFacade.deleteOutputOfResource(component, outputForDelete.getName());
            if (status != StorageOperationStatus.OK) {
                log.debug("Component id: {} delete output id: {} failed", componentId, outputId);
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(status), component.getName());
            }
            final StorageOperationStatus storageOperationStatus = attributeDeclarationOrchestrator
                .unDeclareAttributesAsOutputs(component, outputForDelete);
            if (storageOperationStatus != StorageOperationStatus.OK) {
                log.debug("Component id: {} update attributes declared as output for outputId: {} failed", componentId, outputId);
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(storageOperationStatus), component.getName());
            }
            return outputForDelete;
        } catch (final ComponentException e) {
            failed = true;
            throw e;
        } finally {
            unlockComponent(failed, component);
        }
    }

    public Either<List<OutputDefinition>, ResponseFormat> createOutputsInGraph(Map<String, OutputDefinition> outputs,
                                                                               final Component component,
                                                                               final String userId) {

        for (final Map.Entry<String, OutputDefinition> outputDefinition : outputs.entrySet()) {
            final OutputDefinition outputDefinitionValue = outputDefinition.getValue();
            outputDefinitionValue.setName(outputDefinition.getKey());

            final String value = outputDefinitionValue.getValue();
            if (value != null) {
                final List<String> getAttribute = (List<String>) ImportUtils.loadYamlAsStrictMap(value)
                    .get(ToscaGetFunctionType.GET_ATTRIBUTE.getFunctionName());
                if (getAttribute.size() == 2) {
                    final Optional<ComponentInstance> optionalComponentInstance = component.getComponentInstanceByName(getAttribute.get(0));
                    if (optionalComponentInstance.isPresent()) {
                        // From Instance
                        final ComponentInstance componentInstance = optionalComponentInstance.get();
                        outputDefinitionValue.setInstanceUniqueId(componentInstance.getUniqueId());
                        outputDefinitionValue.setParentUniqueId(userId);
                        outputDefinitionValue.setOwnerId(userId);
                        final List<AttributeDefinition> componentInstanceAttributes = componentInstance.getAttributes();
                        if (CollectionUtils.isNotEmpty(componentInstanceAttributes)) {
                            final Optional<ComponentInstanceAttribute> componentInstanceAttributeOptional = componentInstanceAttributes.stream()
                                .filter(ad -> ad.getName().equals(getAttribute.get(1))).map(ComponentInstanceAttribute::new).findFirst();
                            if (componentInstanceAttributeOptional.isPresent()) {
                                final ComponentInstanceAttribute componentInstanceAttribute = componentInstanceAttributeOptional.get();
                                outputDefinitionValue.setAttribute(componentInstanceAttribute);
                                outputDefinitionValue.setAttributeId(componentInstanceAttribute.getUniqueId());
                                outputDefinitionValue.setSchema(componentInstanceAttribute.getSchema());
                                outputDefinitionValue.setOutputPath(componentInstanceAttribute.getName());
                                final GetOutputValueDataDefinition getOutputValueDataDefinition = new GetOutputValueDataDefinition();
                                getOutputValueDataDefinition.setOutputName(outputDefinition.getKey());
                                getOutputValueDataDefinition.setOutputId(outputDefinitionValue.getUniqueId());
                                componentInstanceAttribute.setGetOutputValues(Collections.singletonList(getOutputValueDataDefinition));
                            }
                        }
                    } else {
                        // From SELF
                        outputDefinitionValue.setInstanceUniqueId(component.getUniqueId());
                    }
                }
            }
        }
        final List<OutputDefinition> resourceAttributes = component.getOutputs();
        if (resourceAttributes != null) {
            final Map<String, OutputDefinition> generatedOutputs = resourceAttributes.stream()
                .collect(Collectors.toMap(AttributeDataDefinition::getName, i -> i));
            final Either<Map<String, OutputDefinition>, String> mergeEither = ToscaDataDefinition.mergeDataMaps(generatedOutputs, outputs);
            if (mergeEither.isRight()) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.OUTPUT_ALREADY_EXIST, mergeEither.right().value()));
            }
            outputs = mergeEither.left().value();
        }

        final var associateOutputsEither = toscaOperationFacade.createAndAssociateOutputs(outputs, component.getUniqueId());
        if (associateOutputsEither.isRight()) {
            log.debug("Failed to create outputs under component {}. Status is {}", component.getUniqueId(), associateOutputsEither.right().value());

            return Either.right(
                componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(associateOutputsEither.right().value())));
        }
        return Either.left(associateOutputsEither.left().value());
    }

}
