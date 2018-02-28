package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PolicyTypeOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class PolicyTypeBusinessLogic {

    private static final Logger log = LoggerFactory.getLogger(PolicyTypeBusinessLogic.class);
    private PolicyTypeOperation policyTypeOperation;
    private TitanDao titanDao;
    private ComponentsUtils componentsUtils;
    private UserValidations userValidations;

    public PolicyTypeBusinessLogic(PolicyTypeOperation policyTypeOperation, TitanDao titanDao, ComponentsUtils componentsUtils, UserValidations userValidations) {
        this.policyTypeOperation = policyTypeOperation;
        this.titanDao = titanDao;
        this.componentsUtils = componentsUtils;
        this.userValidations = userValidations;
    }

    public Either<List<PolicyTypeDefinition>, ResponseFormat> getAllPolicyTypes(String userId, String internalComponentType) {
        try {
            Set<String> excludedPolicyTypes = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludedPolicyTypesMapping().get(internalComponentType);
            return userValidations.validateUserExists(userId, "get policy types", true)
                                  .left()
                                  .bind(user -> getPolicyTypes(excludedPolicyTypes));
        } finally {
            titanDao.commit();
        }
    }

    private Either<List<PolicyTypeDefinition>, ResponseFormat> getPolicyTypes(Set<String> excludedTypes) {
        return policyTypeOperation.getAllPolicyTypes(excludedTypes)
                                  .right()
                                  .bind(this::convertToResponseFormatOrNotFoundErrorToEmptyList);
    }

    private Either<List<PolicyTypeDefinition>, ResponseFormat> convertToResponseFormatOrNotFoundErrorToEmptyList(StorageOperationStatus err) {
        log.debug("error when trying to fetch policy types: {}", err);
        return componentsUtils.convertToResponseFormatOrNotFoundErrorToEmptyList(err);
    }
}
