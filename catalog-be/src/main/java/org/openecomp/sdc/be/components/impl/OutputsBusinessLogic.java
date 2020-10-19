/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020, Nordix Foundation. All rights reserved.
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.property.PropertyDeclarationOrchestrator;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstOutputsMap;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
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
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("outputsBusinessLogic")
public class OutputsBusinessLogic extends BaseBusinessLogic {

    private static final String CREATE_OUTPUT = "CreateOutput";

    private static final Logger log = Logger.getLogger(OutputsBusinessLogic.class);
    private static final String FAILED_TO_FIND_COMPONENT_ERROR = "Failed to find component %s, error: %s";
    private static final String FAILED_TO_FIND_OUTPUT_UNDER_COMPONENT_ERROR = "Failed to find output %s under component %s, error: %s";
    private static final String GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_GROUP = "Going to execute rollback on create group.";
    private static final String GOING_TO_EXECUTE_COMMIT_ON_CREATE_GROUP = "Going to execute commit on create group.";
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(
        OutputsBusinessLogic.class.getName());

    private final PropertyDeclarationOrchestrator propertyDeclarationOrchestrator;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private final DataTypeBusinessLogic dataTypeBusinessLogic;

    @Autowired
    public OutputsBusinessLogic(IElementOperation elementDao,
                                IGroupOperation groupOperation,
                                IGroupInstanceOperation groupInstanceOperation,
                                IGroupTypeOperation groupTypeOperation,
                                InterfaceOperation interfaceOperation,
                                InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                PropertyDeclarationOrchestrator propertyDeclarationOrchestrator,
                                ComponentInstanceBusinessLogic componentInstanceBusinessLogic,
                                DataTypeBusinessLogic dataTypeBusinessLogic,
                                ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
            interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
        this.propertyDeclarationOrchestrator = propertyDeclarationOrchestrator;
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
        this.dataTypeBusinessLogic = dataTypeBusinessLogic;
    }

    @Override
    public Either<List<OutputDefinition>, ResponseFormat> declareOutputProperties(final String userId,
                                                                                  final String componentId,
                                                                                  final ComponentTypeEnum componentTypeEnum,
                                                                                  final ComponentInstOutputsMap componentInstOutputsMap) {

        return createMultipleOutputs(userId, componentId, componentTypeEnum, componentInstOutputsMap, true, false);
    }

    private Either<List<OutputDefinition>, ResponseFormat> createMultipleOutputs(final String userId,
                                                                                final String componentId,
                                                                                final ComponentTypeEnum componentType,
                                                                                final ComponentInstOutputsMap componentInstOutputsMapUi,
                                                                                final boolean shouldLockComp,
                                                                                final boolean inTransaction) {

        Either<List<OutputDefinition>, ResponseFormat> result = null;
        Component component = null;
        try {
            validateUserExists(userId);
            component = getAndValidateComponentForCreate(userId, componentId, componentType, shouldLockComp);
            result = propertyDeclarationOrchestrator.declarePropertiesToOutputs(component, componentInstOutputsMapUi)
                .left()
                .bind(outputsToCreate -> prepareOutputsForCreation(userId, componentId, outputsToCreate))
                .right()
                .map(componentsUtils::getResponseFormat);

            return result;

        } catch (final ByResponseFormatComponentException e) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, getClass().getName(),
                "Failed to createMultipleOutputs: Exception thrown: ", e);
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
            if (shouldLockComp && component != null) {
                graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
            }

        }
    }

    private Component getAndValidateComponentForCreate(final String userId,
                                                       final String componentId,
                                                       final ComponentTypeEnum componentType,
                                                       final boolean shouldLockComp) {
        final ComponentParametersView componentParametersView = getBaseComponentParametersView();
        final Component component = validateComponentExists(componentId, componentType, componentParametersView);
        if (shouldLockComp) {
            lockComponent(component, CREATE_OUTPUT);
        }
        validateCanWorkOnComponent(component, userId);
        return component;
    }

    private Either<List<OutputDefinition>, StorageOperationStatus> prepareOutputsForCreation(final String userId,
                                                                                             final String componentId,
                                                                                             final List<OutputDefinition> outputsToCreate) {
        final Map<String, OutputDefinition> outputsToPersist = MapUtil.toMap(outputsToCreate, OutputDefinition::getName);
        assignOwnerIdToOutputs(userId, outputsToPersist);
        outputsToPersist.values()
            .forEach(output -> output.setConstraints(componentInstanceBusinessLogic.setOutputConstraint(output)));

        return toscaOperationFacade.addOutputsToComponent(outputsToPersist, componentId)
            .left()
            .map(persistedOutputs -> outputsToCreate);
    }

    private void assignOwnerIdToOutputs(final String userId, final Map<String, OutputDefinition> outputsToCreate) {
        outputsToCreate.values().forEach(outputDefinition -> outputDefinition.setOwnerId(userId));
    }

    private ComponentParametersView getBaseComponentParametersView() {
        final ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.disableAll();
        componentParametersView.setIgnoreOutputs(false);
        componentParametersView.setIgnoreComponentInstances(false);
        componentParametersView.setIgnoreComponentInstancesOutputs(false);
        componentParametersView.setIgnoreComponentInstancesProperties(false);
        componentParametersView.setIgnorePolicies(false);
        componentParametersView.setIgnoreGroups(false);
        componentParametersView.setIgnoreUsers(false);
        return componentParametersView;
    }

    /**
     * Delete output from component
     * @param componentId component id
     * @param userId user id
     * @param outputId output property id
     */
    public OutputDefinition deleteOutput(final String componentId, final String userId, final String outputId) {

        final Either<OutputDefinition, ResponseFormat> deleteEither;
        if (log.isDebugEnabled()) {
            log.debug("Going to delete output id: {}", outputId);
        }
        validateUserExists(userId);
        final ComponentParametersView componentParametersView = getBaseComponentParametersView();
        componentParametersView.setIgnoreInterfaces(false);
        componentParametersView.setIgnoreDataType(false);
        componentParametersView.setIgnoreProperties(false);
        componentParametersView.setIgnoreOutputs(false);

        final Either<Component, StorageOperationStatus> componentEither =
            toscaOperationFacade.getToscaElement(componentId, componentParametersView);
        if (componentEither.isRight()) {
            throw new ByActionStatusComponentException(
                componentsUtils.convertFromStorageResponse(componentEither.right().value()));
        }
        final Component component = componentEither.left().value();

        // Validate outputId is child of the component
        Optional<OutputDefinition> optionalOutputDefinition = component.getOutputs().stream().
            // filter by ID
                filter(output -> output.getUniqueId().equals(outputId)).
            // Get the output
                findAny();
        if (optionalOutputDefinition.isEmpty()) {
            throw new ByActionStatusComponentException(ActionStatus.OUTPUT_IS_NOT_CHILD_OF_COMPONENT, outputId,
                componentId);
        }

        final OutputDefinition outputDefinition = optionalOutputDefinition.get();
        lockComponent(componentId, component, "deleteOutput");
        // Delete output operations
        boolean failed = false;
        try {
            StorageOperationStatus storageOperationStatus =
                toscaOperationFacade.deleteOutputOfResource(component, outputDefinition.getName());
            if (storageOperationStatus != StorageOperationStatus.OK) {
                log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, getClass().getName(),
                    String.format("Component id: %s delete output id: %s failed", componentId, outputId));
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(storageOperationStatus),
                    component.getName());
            }

            storageOperationStatus =
                propertyDeclarationOrchestrator.unDeclarePropertiesAsOutputs(component, outputDefinition);
            if (storageOperationStatus != StorageOperationStatus.OK) {
                log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, getClass().getName(), String
                    .format("Component id: %s update properties declared as output for output id: %s failed",
                        componentId, outputId));
                throw new ByActionStatusComponentException(
                    componentsUtils.convertFromStorageResponse(storageOperationStatus), component.getName());
            }
            return outputDefinition;
        } catch (final ComponentException e) {
            failed = true;
            throw e;
        } finally {
            unlockComponent(failed, component);
        }
    }

}
