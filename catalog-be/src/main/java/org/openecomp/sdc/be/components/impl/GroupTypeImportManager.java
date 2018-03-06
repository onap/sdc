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
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component("groupTypeImportManager")
public class GroupTypeImportManager {

    public static void main(String[] args) {

        List<PropertyDefinition> properties = new ArrayList<>();
        PropertyDefinition propertyDefintion = new PropertyDefinition();
        propertyDefintion.setName("aaa");
        properties.add(propertyDefintion);

        List<String> allParentsProps = new ArrayList<>();
        allParentsProps.add("aaa");
        allParentsProps.add("bbb");

        Set<String> alreadyExistPropsCollection = properties.stream().filter(p -> allParentsProps.contains(p.getName())).map(p -> p.getName()).collect(Collectors.toSet());
        System.out.println(alreadyExistPropsCollection);

    }

    private static final Logger log = LoggerFactory.getLogger(GroupTypeImportManager.class);
    @Resource
    private IGroupTypeOperation groupTypeOperation;
    @Resource
    private ComponentsUtils componentsUtils;
    @Resource
    private ToscaOperationFacade toscaOperationFacade;

    @Resource
    private CommonImportManager commonImportManager;

    public Either<List<ImmutablePair<GroupTypeDefinition, Boolean>>, ResponseFormat> createGroupTypes(String groupTypesYml) {
        return commonImportManager.createElementTypes(groupTypesYml, elementTypeYml -> createGroupTypesFromYml(elementTypeYml), groupTypesList -> createGroupTypesByDao(groupTypesList), ElementTypeEnum.GroupType);
    }

    private Either<List<GroupTypeDefinition>, ActionStatus> createGroupTypesFromYml(String groupTypesYml) {

        return commonImportManager.createElementTypesFromYml(groupTypesYml, (groupTypeName, groupTypeJsonData) -> createGroupType(groupTypeName, groupTypeJsonData));
    }

    private Either<List<ImmutablePair<GroupTypeDefinition, Boolean>>, ResponseFormat> createGroupTypesByDao(List<GroupTypeDefinition> groupTypesToCreate) {
        return commonImportManager.createElementTypesByDao(groupTypesToCreate, groupType -> validateGroupType(groupType), groupType -> new ImmutablePair<>(ElementTypeEnum.GroupType, groupType.getType()),
                groupTypeName -> groupTypeOperation.getLatestGroupTypeByType(groupTypeName), groupType -> groupTypeOperation.addGroupType(groupType), groupTypeOperation::upgradeGroupType);
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
            commonImportManager.setField(toscaJson, ToscaTagNamesEnum.DESCRIPTION.getElementName(), groupType::setDescription);
            // Derived From
            commonImportManager.setField(toscaJson, ToscaTagNamesEnum.DERIVED_FROM.getElementName(), groupType::setDerivedFrom);
            // Properties
            commonImportManager.setProperties(toscaJson, groupType::setProperties);
            // Metadata
            commonImportManager.setField(toscaJson, ToscaTagNamesEnum.METADATA.getElementName(), groupType::setMetadata);
            // Capabilities
            List<CapabilityTypeDefinition> capabilityTypes = createGroupCapabilityTypes(toscaJson);
            groupType.setCapabilityTypes(capabilityTypes);
            // Members
            commonImportManager.setField(toscaJson, ToscaTagNamesEnum.MEMBERS.getElementName(), groupType::setMembers);

            groupType.setType(groupTypeName);

            groupType.setHighestVersion(true);

            groupType.setVersion(ImportUtils.Constants.FIRST_CERTIFIED_VERSION_VERSION);
        }
        return groupType;
    }

    /**
     * @param toscaJson
     * @return
     */
    private List<CapabilityTypeDefinition> createGroupCapabilityTypes(Map<String, Object> toscaJson) {
        CapabilityTypeToscaJsonHolder capabilityTypeToscaJsonHolder = new CapabilityTypeToscaJsonHolder();
        commonImportManager.setField(toscaJson, ToscaTagNamesEnum.CAPABILITIES.getElementName(), capabilityTypeToscaJsonHolder::setCapabilityTypeToscaJson);
        List<CapabilityTypeDefinition> capabilityTypes;
        if (capabilityTypeToscaJsonHolder.isEmpty()) {
            capabilityTypes = Collections.emptyList();
        }
        else {
            capabilityTypes = commonImportManager.createElementTypesFromToscaJsonMap(this::createGroupCapabilityType, capabilityTypeToscaJsonHolder.getCapabilityTypeToscaJson());
        }
        return capabilityTypes;
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
    
    private CapabilityTypeDefinition createGroupCapabilityType(String capabilityTypeName, Map<String, Object> toscaJson) {
        CapabilityTypeDefinition capabilityType = new CapabilityTypeDefinition();

        commonImportManager.setField(toscaJson, ToscaTagNamesEnum.TYPE.getElementName(), capabilityType::setType);
        // Properties
        commonImportManager.setPropertiesMap(toscaJson, capabilityType::setProperties);

        return capabilityType;
    }

}
