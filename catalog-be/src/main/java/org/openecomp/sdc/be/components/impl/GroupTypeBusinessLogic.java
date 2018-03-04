package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class GroupTypeBusinessLogic {

    private final GroupTypeOperation groupTypeOperation;
    private final TitanDao titanDao;
    private final UserValidations userValidations;

    public GroupTypeBusinessLogic(GroupTypeOperation groupTypeOperation, TitanDao titanDao, UserValidations userValidations) {
        this.groupTypeOperation = groupTypeOperation;
        this.titanDao = titanDao;
        this.userValidations = userValidations;
    }


    public List<GroupTypeDefinition> getAllGroupTypes(String userId, String internalComponentType) {
        try {
            userValidations.validateUserExists(userId, "get group types", true)
                    .left()
                    .on(this::onUserError);

            Set<String> excludeGroupTypes = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludedGroupTypesMapping().get(internalComponentType);
            return groupTypeOperation.getAllGroupTypes(excludeGroupTypes);
        } finally {
            titanDao.commit();
        }
    }

    private User onUserError(ResponseFormat responseFormat) {
        throw new ComponentException(responseFormat);
    }


}
