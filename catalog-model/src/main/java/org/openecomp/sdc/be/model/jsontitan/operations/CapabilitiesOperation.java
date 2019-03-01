/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.model.jsontitan.operations;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@org.springframework.stereotype.Component("capabilities-operation")
public class CapabilitiesOperation extends BaseOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilitiesOperation.class);

    public Either<List<CapabilityDefinition>, StorageOperationStatus> addCapabilities(
            String componentId,
            List<CapabilityDefinition> capabilityDefinitions) {
        return addOrUpdateCapabilities(componentId, capabilityDefinitions, false);
    }

    public Either<List<CapabilityDefinition>, StorageOperationStatus> updateCapabilities(
            String componentId,
            List<CapabilityDefinition> capabilityDefinitions) {
        return addOrUpdateCapabilities(componentId, capabilityDefinitions, true);
    }

    private Either<List<CapabilityDefinition>, StorageOperationStatus> addOrUpdateCapabilities(String componentId,
                                                                                               List<CapabilityDefinition> capabilityDefinitions,
                                                                                               boolean isUpdateAction) {
        StorageOperationStatus statusRes = performUpdateToscaAction(isUpdateAction,
                componentId, Collections
                        .singletonList(convertToListCapabilityDataDefinition(capabilityDefinitions)));
        if (!statusRes.equals(StorageOperationStatus.OK)) {
            titanDao.rollback();
            LOGGER.error("Failed to find the parent capability of capability type {}."
                    + " status is {}", componentId, statusRes);
            return Either.right(statusRes);
        }
        titanDao.commit();
        return Either.left(capabilityDefinitions);
    }

    public StorageOperationStatus deleteCapabilities(Component component,
                                                     String capabilityIdToDelete) {
        return deleteToscaDataElements(component.getUniqueId(),
                EdgeLabelEnum.CAPABILITIES,
                Collections.singletonList(capabilityIdToDelete));
    }

    private static ListCapabilityDataDefinition convertToListCapabilityDataDefinition(
            List<CapabilityDefinition> capabilities) {
        List<CapabilityDataDefinition> capabilityDefinitions = new ArrayList<>(capabilities);
        return new ListCapabilityDataDefinition(capabilityDefinitions);
    }

    private StorageOperationStatus performUpdateToscaAction(boolean isUpdate,
                                                            String componentId,
                                                            List<ListCapabilityDataDefinition> toscaDataList) {
        if (isUpdate) {
            return updateToscaDataOfToscaElement(componentId, EdgeLabelEnum.CAPABILITIES,
                    VertexTypeEnum.CAPABILITIES, toscaDataList, JsonPresentationFields.TYPE);
        } else {
            return addToscaDataToToscaElement(componentId, EdgeLabelEnum.CAPABILITIES,
                    VertexTypeEnum.CAPABILITIES, toscaDataList, JsonPresentationFields.TYPE);
        }
    }
}
