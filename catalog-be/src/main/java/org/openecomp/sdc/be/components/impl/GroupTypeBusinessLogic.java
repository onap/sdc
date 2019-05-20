package org.openecomp.sdc.be.components.impl;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
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
    private final JanusGraphDao janusGraphDao;
    private final UserValidations userValidations;
    private final ComponentsUtils componentsUtils;

    public GroupTypeBusinessLogic(GroupTypeOperation groupTypeOperation, JanusGraphDao janusGraphDao, UserValidations userValidations, ComponentsUtils componentsUtils) {
        this.groupTypeOperation = groupTypeOperation;
        this.janusGraphDao = janusGraphDao;
        this.userValidations = userValidations;
        this.componentsUtils = componentsUtils;
    }


    public List<GroupTypeDefinition> getAllGroupTypes(String userId, String internalComponentType) {
        try {
            userValidations.validateUserExists(userId, "get group types", true);
            Set<String> excludeGroupTypes = getExcludedGroupTypes(internalComponentType);
            return groupTypeOperation.getAllGroupTypes(excludeGroupTypes);
        } finally {
            janusGraphDao.commit();
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
        janusGraphDao.rollback();
        if (status == StorageOperationStatus.NOT_FOUND) {
            throw new ComponentException(ActionStatus.GROUP_TYPE_IS_INVALID, groupType);
        } else {
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }
}
