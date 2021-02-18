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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.ComponentParametersView;
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

    private static final Logger log = Logger.getLogger(OutputsBusinessLogic.class);
    private static final String FAILED_TO_FOUND_COMPONENT_ERROR = "Failed to found component {}, error: {}";
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(OutputsBusinessLogic.class);
    private static final String FAILED_TO_FOUND_COMPONENT_INSTANCE_OUTPUTS_COMPONENT_INSTANCE_ID = "Failed to found component instance outputs componentInstanceId: {}";
    private static final String FAILED_TO_FOUND_COMPONENT_INSTANCE_OUTPUTS_ERROR = "Failed to found component instance outputs {}, error: {}";

    @Autowired
    public OutputsBusinessLogic(final IElementOperation elementDao,
                                final IGroupOperation groupOperation,
                                final IGroupInstanceOperation groupInstanceOperation,
                                final IGroupTypeOperation groupTypeOperation,
                                final InterfaceOperation interfaceOperation,
                                final InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                final ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
            interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
    }

    public Either<List<ComponentInstanceOutput>, ResponseFormat> getComponentInstanceOutputs(final String userId,
                                                                                             final String componentId,
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
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_INPUTS, component.getComponentMetadataForSupportLog(),
                StatusCode.ERROR, FAILED_TO_FOUND_COMPONENT_INSTANCE_OUTPUTS_COMPONENT_INSTANCE_ID, componentInstanceId);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        final Map<String, List<ComponentInstanceOutput>> ciOutputs = Optional.ofNullable(component.getComponentInstancesOutputs())
            .orElse(Collections.emptyMap());

        return Either.left(ciOutputs.getOrDefault(componentInstanceId, Collections.emptyList()));
    }

}
