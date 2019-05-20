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
import org.openecomp.sdc.be.components.impl.CommonImportManager.ElementTypeEnum;
import org.openecomp.sdc.be.components.impl.model.ToscaTypeImportData;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.utils.TypeCompareUtils;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("groupTypeImportManager")
public class GroupTypeImportManager {

    private static final Logger log = Logger.getLogger(GroupTypeImportManager.class);
    private final GroupTypeOperation groupTypeOperation;
    private final ComponentsUtils componentsUtils;
    private final ToscaOperationFacade toscaOperationFacade;
    private final CommonImportManager commonImportManager;

    public GroupTypeImportManager(GroupTypeOperation groupTypeOperation, ComponentsUtils componentsUtils, ToscaOperationFacade toscaOperationFacade, CommonImportManager commonImportManager) {
        this.groupTypeOperation = groupTypeOperation;
        this.componentsUtils = componentsUtils;
        this.toscaOperationFacade = toscaOperationFacade;
        this.commonImportManager = commonImportManager;
    }

    public Either<List<ImmutablePair<GroupTypeDefinition, Boolean>>, ResponseFormat> createGroupTypes(ToscaTypeImportData toscaTypeImportData) {
        return commonImportManager.createElementTypes(toscaTypeImportData, this::createGroupTypesFromYml, this::upsertGroupTypesByDao);
    }

    private Either<List<GroupTypeDefinition>, ActionStatus> createGroupTypesFromYml(String groupTypesYml) {
        return commonImportManager.createElementTypesFromYml(groupTypesYml, this::createGroupType);
    }

    private Either<List<ImmutablePair<GroupTypeDefinition, Boolean>>, ResponseFormat> upsertGroupTypesByDao(List<GroupTypeDefinition> groupTypesToCreate) {
        return commonImportManager.createElementTypesByDao(groupTypesToCreate, this::validateGroupType, groupType -> new ImmutablePair<>(ElementTypeEnum.GROUP_TYPE, groupType.getType()),
                groupTypeOperation::getLatestGroupTypeByType, groupTypeOperation::addGroupType, this::updateGroupType);
    }

    private Either<GroupTypeDefinition, StorageOperationStatus> updateGroupType(GroupTypeDefinition newGroupType, GroupTypeDefinition oldGroupType) {
        Either<GroupTypeDefinition, StorageOperationStatus> validationRes = groupTypeOperation.validateUpdateProperties(newGroupType);
        if (validationRes.isRight()) {
            log.error("#updateGroupType - One or all properties of group type {} not valid. status is {}", newGroupType, validationRes.right().value());
            return validationRes;
        }
        
        if (TypeCompareUtils.isGroupTypesEquals(newGroupType, oldGroupType)) {
            return TypeCompareUtils.typeAlreadyExists();
        }
        
        return groupTypeOperation.updateGroupType(newGroupType, oldGroupType);
    }

    private Either<ActionStatus, ResponseFormat> validateGroupType(GroupTypeDefinition groupType) {
        Either<ActionStatus, ResponseFormat> result = Either.left(ActionStatus.OK);
        if (groupType.getMembers() != null) {
            if (groupType.getMembers().isEmpty()) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GROUP_MEMBER_EMPTY, groupType.getType());
                result = Either.right(responseFormat);
            } else {
                for (String member : groupType.getMembers()) {
                    // Verify that such Resource exist
                    Either<org.openecomp.sdc.be.model.Resource, StorageOperationStatus> eitherMemberExist = toscaOperationFacade.getLatestByToscaResourceName(member);
                    if (eitherMemberExist.isRight()) {
                        StorageOperationStatus operationStatus = eitherMemberExist.right().value();
                        log.debug("Error when fetching parent resource {}, error: {}", member, operationStatus);
                        ActionStatus convertFromStorageResponse = componentsUtils.convertFromStorageResponse(operationStatus);
                        BeEcompErrorManager.getInstance().logBeComponentMissingError("Import GroupType", "resource", member);
                        result = Either.right(componentsUtils.getResponseFormat(convertFromStorageResponse, member));
                        break;
                    }
                }

            }
        }
        return result;
    }

    private GroupTypeDefinition createGroupType(String groupTypeName, Map<String, Object> toscaJson) {

        GroupTypeDefinition groupType = new GroupTypeDefinition();

        if (toscaJson != null) {
            // Description
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DESCRIPTION.getElementName(), groupType::setDescription);
            // Derived From
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName(), groupType::setDerivedFrom);
            // Properties
            CommonImportManager.setProperties(toscaJson, groupType::setProperties);
            // Metadata
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.METADATA.getElementName(), groupType::setMetadata);
            // Capabilities
            Map<String, CapabilityDefinition> capabilities = createCapabilities(toscaJson);
            groupType.setCapabilities(capabilities);
            // Members
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.MEMBERS.getElementName(), groupType::setMembers);

            groupType.setType(groupTypeName);

            groupType.setHighestVersion(true);

            groupType.setVersion(TypeUtils.FIRST_CERTIFIED_VERSION_VERSION);
        }
        return groupType;
    }

    /**
     * @param toscaJson
     * @return
     */
    private Map<String, CapabilityDefinition> createCapabilities(Map<String, Object> toscaJson) {
        CapabilityTypeToscaJsonHolder capabilityTypeToscaJsonHolder = new CapabilityTypeToscaJsonHolder();
        commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.CAPABILITIES.getElementName(), capabilityTypeToscaJsonHolder::setCapabilityTypeToscaJson);
        Map<String, CapabilityDefinition> capabilities;
        if (capabilityTypeToscaJsonHolder.isEmpty()) {
            capabilities = Collections.emptyMap();
        }
        else {
            capabilities = commonImportManager.createElementTypesMapFromToscaJsonMap(this::createCapability, capabilityTypeToscaJsonHolder.getCapabilityTypeToscaJson());
        }
        return capabilities;
    }
    
    private class CapabilityTypeToscaJsonHolder {
        private Map<String, Object> capabilityTypeToscaJson;

        public Map<String, Object> getCapabilityTypeToscaJson() {
            return capabilityTypeToscaJson;
        }
        
        public boolean isEmpty() {
            return capabilityTypeToscaJson == null;
        }

        public void setCapabilityTypeToscaJson(Map<String, Object> capabilityTypeToscaJson) {
            this.capabilityTypeToscaJson = capabilityTypeToscaJson;
        }
    }
    
    private CapabilityDefinition createCapability(String capabilityName, Map<String, Object> toscaJson) {
        CapabilityDefinition capability = new CapabilityDefinition();

        capability.setName(capabilityName);
        commonImportManager.setField(toscaJson, ToscaTagNamesEnum.TYPE.getElementName(), capability::setType);
        // Properties
        CommonImportManager.setProperties(toscaJson, pl -> capability.setProperties(map(pl)));

        return capability;
    }

    /**
     * @param pl
     * @return
     */
    private List<ComponentInstanceProperty> map(List<PropertyDefinition> pl) {
        return pl.stream()
                .map(ComponentInstanceProperty::new)
                .collect(Collectors.toList());
    }

}
