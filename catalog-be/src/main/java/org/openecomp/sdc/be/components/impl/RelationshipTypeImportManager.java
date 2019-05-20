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

package org.openecomp.sdc.be.components.impl;

import java.util.List;
import java.util.Map;

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.CommonImportManager.ElementTypeEnum;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.RelationshipTypeDefinition;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.RelationshipTypeOperation;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

@Component("relationshipTypeImportManager")
public class RelationshipTypeImportManager {

    private final RelationshipTypeOperation relationshipTypeOperation;
    private final CommonImportManager commonImportManager;
    private ComponentsUtils componentsUtils;

    public RelationshipTypeImportManager(RelationshipTypeOperation relationshipTypeOperation,
                                         CommonImportManager commonImportManager) {
        this.relationshipTypeOperation = relationshipTypeOperation;
        this.commonImportManager = commonImportManager;
    }

    public Either<List<ImmutablePair<RelationshipTypeDefinition, Boolean>>, ResponseFormat> createRelationshipTypes(
            String relationshipYml) {
        return createRelationshipTypes(relationshipYml, false);
    }

    private Either<List<ImmutablePair<RelationshipTypeDefinition, Boolean>>, ResponseFormat> createRelationshipTypes(
            String relationshipTypeYml, boolean inTransaction) {
        return commonImportManager.createElementTypes(relationshipTypeYml,
                relationshipTypesFromYml -> createRelationshipTypesFromYml(relationshipTypeYml),
                relationshipTypesToCreate -> createRelationshipTypesByDao(relationshipTypesToCreate,
                        inTransaction), ElementTypeEnum.RELATIONSHIP_TYPE);
    }

    private Either<List<RelationshipTypeDefinition>, ActionStatus> createRelationshipTypesFromYml(
            String relationshipTypeYml) {
        return commonImportManager.createElementTypesFromYml(relationshipTypeYml,
                this::createRelationshipType);
    }

    private Either<List<ImmutablePair<RelationshipTypeDefinition, Boolean>>, ResponseFormat> createRelationshipTypesByDao(
            List<RelationshipTypeDefinition> relationshipTypesToCreate, boolean inTransaction) {
        return commonImportManager.createElementTypesByDao(relationshipTypesToCreate, this::validateRelationshipType,
                relationshipType -> new ImmutablePair<>(ElementTypeEnum.RELATIONSHIP_TYPE, relationshipType.getType()),
                relationshipTypeName -> relationshipTypeOperation.getRelationshipTypeByName(relationshipTypeName)
                        .right().map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus),
                relationshipType -> relationshipTypeOperation.addRelationshipType(relationshipType, inTransaction),
                (newRelationshipType, oldRelationshipType) -> relationshipTypeOperation
                        .updateRelationshipType(newRelationshipType, oldRelationshipType, inTransaction));
    }


    private Either<ActionStatus, ResponseFormat> validateRelationshipType(RelationshipTypeDefinition relationshipType) {
        Either<ActionStatus, ResponseFormat> result = Either.left(ActionStatus.OK);
        if (relationshipType.getType() == null) {
            ResponseFormat responseFormat =
                    componentsUtils.getResponseFormat(ActionStatus.MISSING_RELATIONSHIP_TYPE, relationshipType.getType());
            result = Either.right(responseFormat);
        }
        return result;
    }

    private RelationshipTypeDefinition createRelationshipType(String relationshipTypeName,
                                                              Map<String, Object> toscaJson) {
        RelationshipTypeDefinition relationshipType = new RelationshipTypeDefinition();

        relationshipType.setType(relationshipTypeName);

        // Description
        commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DESCRIPTION.getElementName(),
                relationshipType::setDescription);
        // Derived From
        commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName(),
                relationshipType::setDerivedFrom);
        // Properties
        commonImportManager.setPropertiesMap(toscaJson, relationshipType::setProperties);
        //valid-target-types
        if(toscaJson.get("valid_target_types") instanceof List)
            relationshipType.setValidTargetTypes((List<String>) toscaJson.get("valid_target_types"));

        return relationshipType;
    }

}
