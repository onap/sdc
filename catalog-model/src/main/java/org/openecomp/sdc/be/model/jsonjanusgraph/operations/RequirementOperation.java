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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@org.springframework.stereotype.Component("requirement-operation")
public class RequirementOperation extends BaseOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequirementOperation.class);

    public Either<List<RequirementDefinition>, StorageOperationStatus> addRequirement(
            String componentId,
            List<RequirementDefinition> requirementDefinitions) {
        return addOrUpdateRequirements( componentId, requirementDefinitions, false);
    }

    public Either<List<RequirementDefinition>, StorageOperationStatus> updateRequirement(
            String componentId,
            List<RequirementDefinition> requirementDefinitions) {
        return addOrUpdateRequirements( componentId, requirementDefinitions, true);
    }


    private Either<List<RequirementDefinition>, StorageOperationStatus> addOrUpdateRequirements(String componentId,
                                                                 List<RequirementDefinition> requirementDefinitions,
                                                                                                boolean isUpdateAction) {

        StorageOperationStatus statusRes = performUpdateToscaAction(isUpdateAction,
                componentId, Collections
                        .singletonList(convertToListRequirementDataDefinition(requirementDefinitions)));
        if (!statusRes.equals(StorageOperationStatus.OK)) {
            janusGraphDao.rollback();
            LOGGER.error("Failed to find the parent capability of capability type {}."
                    + " status is {}", componentId, statusRes);
            return Either.right(statusRes);
        }
        janusGraphDao.commit();
        return Either.left(requirementDefinitions);
    }

    public StorageOperationStatus deleteRequirements(Component component,
                                                     String requirementToDelete) {
        return deleteToscaDataElements(component.getUniqueId(),
                EdgeLabelEnum.REQUIREMENTS, Collections.singletonList(requirementToDelete));
    }

    private static ListRequirementDataDefinition convertToListRequirementDataDefinition(
            List<RequirementDefinition> requirementDefinitions) {
        List<RequirementDataDefinition> requirementDataDefinitions =
                new ArrayList<>(requirementDefinitions);
        return new ListRequirementDataDefinition(requirementDataDefinitions);
    }

    private StorageOperationStatus performUpdateToscaAction(boolean isUpdate,
                                                            String componentId, List<ListRequirementDataDefinition> toscaDataList) {
        if (isUpdate) {
            return updateToscaDataOfToscaElement(componentId, EdgeLabelEnum.REQUIREMENTS,
                    VertexTypeEnum.REQUIREMENTS, toscaDataList, JsonPresentationFields.CAPABILITY);
        } else {
            return addToscaDataToToscaElement(componentId, EdgeLabelEnum.REQUIREMENTS,
                    VertexTypeEnum.REQUIREMENTS, toscaDataList, JsonPresentationFields.CAPABILITY);
        }
    }
}
