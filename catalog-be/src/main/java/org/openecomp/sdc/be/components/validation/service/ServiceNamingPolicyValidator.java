package org.openecomp.sdc.be.components.validation.service;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

@org.springframework.stereotype.Component
public class ServiceNamingPolicyValidator implements ServiceFieldValidator {

    private static final Logger log = Logger.getLogger(ServiceNamingPolicyValidator.class.getName());
    private ComponentsUtils componentsUtils;

    public ServiceNamingPolicyValidator(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }
    @Override
    public void validateAndCorrectField(User user, Service service, AuditingActionEnum actionEnum) {
        Boolean isEcompGeneratedCurr = service.isEcompGeneratedNaming();
        String namingPolicyUpdate = service.getNamingPolicy();
        if (isEcompGeneratedCurr == null) {
            throw new ByActionStatusComponentException(ActionStatus.MISSING_ECOMP_GENERATED_NAMING);
        }
        if (isEcompGeneratedCurr) {
            if (!ValidationUtils.validateServiceNamingPolicyLength(namingPolicyUpdate)) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NAMING_POLICY_EXCEEDS_LIMIT, "" + ValidationUtils.SERVICE_NAMING_POLICY_MAX_SIZE);
                throw new ByResponseFormatComponentException(responseFormat);
            }
            if (StringUtils.isEmpty(namingPolicyUpdate)) {
                service.setNamingPolicy("");
                return;
            }
            if (!ValidationUtils.validateCommentPattern(namingPolicyUpdate)) {
                throw new ByActionStatusComponentException(ActionStatus.INVALID_NAMING_POLICY);
            }
            service.setNamingPolicy(namingPolicyUpdate);
        } else {
            if (!StringUtils.isEmpty(namingPolicyUpdate)) {
                log.warn("NamingPolicy must be empty for EcompGeneratedNaming=false");
            }
            service.setNamingPolicy("");
        }
    }
}
