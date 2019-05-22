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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

@Component("groupTypeBusinessLogic")
public class GroupTypeBusinessLogic {

    private final GroupTypeOperation groupTypeOperation;
    private final TitanDao titanDao;
    private final UserValidations userValidations;
    private final ComponentsUtils componentsUtils;

    public GroupTypeBusinessLogic(GroupTypeOperation groupTypeOperation, TitanDao titanDao, UserValidations userValidations, ComponentsUtils componentsUtils) {
        this.groupTypeOperation = groupTypeOperation;
        this.titanDao = titanDao;
        this.userValidations = userValidations;
        this.componentsUtils = componentsUtils;
    }


    public List<GroupTypeDefinition> getAllGroupTypes(String userId, String internalComponentType) {
        try {
            userValidations.validateUserExists(userId, "get group types", true);
            Set<String> excludeGroupTypes = getExcludedGroupTypes(internalComponentType);
            return groupTypeOperation.getAllGroupTypes(excludeGroupTypes);
        } finally {
            titanDao.commit();
        }
    }

    public GroupTypeDefinition getLatestGroupTypeByType(String groupTypeName) {
        return groupTypeOperation.getLatestGroupTypeByType(groupTypeName, true)
                .left()
                .on(e -> failOnGetGroupType(e, groupTypeName));
    }

    public Set<String> getExcludedGroupTypes(String internalComponentType) {
        if (StringUtils.isEmpty(internalComponentType)) {
            return emptySet();
        }
        Map<String, Set<String>> excludedGroupTypesMapping = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludedGroupTypesMapping();
        Set<String> excludedTypes = excludedGroupTypesMapping.get(internalComponentType);
        return excludedTypes == null ? emptySet() : excludedTypes;
    }

    private GroupTypeDefinition failOnGetGroupType(StorageOperationStatus status, String groupType) {
        titanDao.rollback();
        if (status == StorageOperationStatus.NOT_FOUND) {
            throw new ByActionStatusComponentException(ActionStatus.GROUP_TYPE_IS_INVALID, groupType);
        } else {
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
    }
}
