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

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.be.model.utils.TypeCompareUtils;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("capabilityTypeImportManager")
public class CapabilityTypeImportManager {

    private static final Logger log = Logger.getLogger(CapabilityTypeImportManager.class.getName());
    private final CapabilityTypeOperation capabilityTypeOperation;
    private final CommonImportManager commonImportManager;

    public CapabilityTypeImportManager(CapabilityTypeOperation capabilityTypeOperation, CommonImportManager commonImportManager) {
        this.capabilityTypeOperation = capabilityTypeOperation;
        this.commonImportManager = commonImportManager;

    }

    public Either<List<ImmutablePair<CapabilityTypeDefinition, Boolean>>, ResponseFormat> createCapabilityTypes(String capabilityTypesYml) {
        return  commonImportManager.createElementTypes(capabilityTypesYml, this::createCapabilityTypesFromYml, this::upsertCapabilityTypesByDao, CommonImportManager.ElementTypeEnum.CAPABILITY_TYPE);

    }

    private Either<List<CapabilityTypeDefinition>, ActionStatus> createCapabilityTypesFromYml(String capabilityTypesYml) {
        return commonImportManager.createElementTypesFromYml(capabilityTypesYml, this::createCapabilityType);

    }

    private Either<List<ImmutablePair<CapabilityTypeDefinition, Boolean>>, ResponseFormat> upsertCapabilityTypesByDao(List<CapabilityTypeDefinition> capabilityTypesToCreate) {
        return commonImportManager.createElementTypesByDao(capabilityTypesToCreate,
                capabilityType -> Either.left(ActionStatus.OK),
                capabilityType -> new ImmutablePair<>(CommonImportManager.ElementTypeEnum.CAPABILITY_TYPE, capabilityType.getType()),
                capabilityTypeOperation::getCapabilityType,
                capabilityTypeOperation::addCapabilityType,
                this::updateCapabilityType);
    }
    
    private Either<CapabilityTypeDefinition, StorageOperationStatus> updateCapabilityType(CapabilityTypeDefinition newCapabilityType, CapabilityTypeDefinition oldCapabilityType) {
        Either<CapabilityTypeDefinition, StorageOperationStatus> validationRes = capabilityTypeOperation.validateUpdateProperties(newCapabilityType);
        if (validationRes.isRight()) {
            log.error("#updateCapabilityType - One or all properties of capability type {} not valid. status is {}", newCapabilityType, validationRes.right().value());
            return validationRes;
        }
        
        if (TypeCompareUtils.isCapabilityTypesEquals(newCapabilityType, oldCapabilityType)) {
            return TypeCompareUtils.typeAlreadyExists();
        }
        
        return capabilityTypeOperation.updateCapabilityType(newCapabilityType, oldCapabilityType);
    }

    private CapabilityTypeDefinition createCapabilityType(String capabilityTypeName, Map<String, Object> toscaJson) {
        CapabilityTypeDefinition capabilityType = new CapabilityTypeDefinition();

        capabilityType.setType(capabilityTypeName);

        // Description
        commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DESCRIPTION.getElementName(), capabilityType::setDescription);
        // Derived From
        commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName(), capabilityType::setDerivedFrom);
        // Properties
        commonImportManager.setPropertiesMap(toscaJson, capabilityType::setProperties);

        return capabilityType;
    }

}
