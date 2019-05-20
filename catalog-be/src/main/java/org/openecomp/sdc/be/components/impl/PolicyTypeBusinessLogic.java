package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PolicyTypeOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

@Component
public class PolicyTypeBusinessLogic {

    private static final Logger log = Logger.getLogger(PolicyTypeBusinessLogic.class);
    private PolicyTypeOperation policyTypeOperation;
    private JanusGraphDao janusGraphDao;
    private ComponentsUtils componentsUtils;
    private UserValidations userValidations;

    public PolicyTypeBusinessLogic(PolicyTypeOperation policyTypeOperation, JanusGraphDao janusGraphDao, ComponentsUtils componentsUtils, UserValidations userValidations) {
        this.policyTypeOperation = policyTypeOperation;
        this.janusGraphDao = janusGraphDao;
        this.componentsUtils = componentsUtils;
        this.userValidations = userValidations;
    }

    @Transactional
    public List<PolicyTypeDefinition> getAllPolicyTypes(String userId, String internalComponentType) {
        Set<String> excludedPolicyTypes = getExcludedPolicyTypes(internalComponentType);
        userValidations.validateUserExists(userId, "get policy types", true);
        return getPolicyTypes(excludedPolicyTypes);
    }

    public Set<String> getExcludedPolicyTypes(String internalComponentType) {
        if (StringUtils.isEmpty(internalComponentType)) {
            return emptySet();
        }
        Map<String, Set<String>> excludedPolicyTypesMapping = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludedPolicyTypesMapping();
        Set<String> excludedTypes = excludedPolicyTypesMapping.get(internalComponentType);
        return excludedTypes == null ? emptySet() : excludedTypes;
    }

    private List<PolicyTypeDefinition> getPolicyTypes(Set<String> excludedTypes) {
        return policyTypeOperation.getAllPolicyTypes(excludedTypes);
    }

    private Either<List<PolicyTypeDefinition>, ResponseFormat> convertToResponseFormatOrNotFoundErrorToEmptyList(StorageOperationStatus err) {
        log.debug("error when trying to fetch policy types: {}", err);
        return componentsUtils.convertToResponseFormatOrNotFoundErrorToEmptyList(err);
    }
}
